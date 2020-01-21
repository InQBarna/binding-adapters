/* 
 * Copyright 2014 InQBarna Kenkyuu Jo SL 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */ 

package com.inqbarna.rxutil.paging

import android.annotation.SuppressLint
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.subscribers.DisposableSubscriber
import timber.log.Timber
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


private const val DEBUG: Boolean = false

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 2018-03-22
 */
internal class RequestState<T>(val pageSize: Int, private val pageFactory: PageFactory<T>, private val rxPagingConfig: RxPagingConfig) : Disposable
{
    private var mOffset: Int = 0

    private val unboundedQueue: Queue<T>
    private val lock: Lock = ReentrantLock()
    private val stateCondition: Condition = lock.newCondition()
    internal var state: DelegateState = None
        get() = lock.withLock { field }
        private set(newState) {
            lock.withLock {
                val oldState = field
                if (oldState != newState) {
                    debug("State changed from '%s' to '%s'", oldState, newState)
                    field = newState
                    onStateUpdated(oldState, newState)
                    stateCondition.signalAll()
                }
            }
        }

    private fun onStateUpdated(oldState: DelegateState, newState: DelegateState) {
        when (newState) {
            is Error -> {
                if (!newState.hasRecovery) {
                    dispose()
                } else {
                    when (oldState) {
                        is Loading -> delegateDisposable.remove(oldState.task)
                        is Delivering -> oldState.prefetchTask?.let { delegateDisposable.remove(it) }
                    }
                }
            }
            is Complete -> dispose()
            is Delivering -> {
                when (oldState) {
                    is Loading -> if (!newState.prefetching) delegateDisposable.remove(oldState.task)
                    is Delivering -> oldState.prefetchTask?.let { delegateDisposable.remove(it) }
                }
            }
        }
    }

    private val delegateDisposable: CompositeDisposable = CompositeDisposable(
            Disposables.fromAction {
                lock.withLock {
                    debug("We're being disposed!")
                    if (state !is Error) {
                        state = Complete
                    }
                }
            }
    )
    override fun isDisposed(): Boolean = delegateDisposable.isDisposed
    override fun dispose() = delegateDisposable.dispose()

    init {
        val initialData = pageFactory.initialData
        unboundedQueue = LinkedList(initialData)
        mOffset = initialData.size
        if (initialData.isNotEmpty()) {
            state = Delivering(mOffset < pageSize)
        }
    }

    fun blockingNext(): T? {
        return lock.withLock {
            try {
                while (true) {
                    val currentState = state
                    when (currentState) {
                        is Error -> {
                            val size = unboundedQueue.size
                            when {
                                size > 0 -> return@withLock unboundedQueue.poll()
                                currentState.hasRecovery -> {
                                    debug("Awaiting for recovery error...")
                                    stateCondition.await()
                                }
                                else -> return@withLock null
                            }
                        }
                        is Loading, is Delivering -> {
                            val size = unboundedQueue.size
                            if (size < pageSize) {
                                debug("Remaining only %d items", size)
                                setupRequestNext()
                            }

                            return@withLock if (size > 0) unboundedQueue.poll()
                            else {
                                while (unboundedQueue.size == 0 && (state is Loading || (state as? Delivering)?.prefetching == true)) {
                                    debug("Awaiting for elements to arrive")
                                    stateCondition.await()
                                }

                                return if (unboundedQueue.size > 0) {
                                    unboundedQueue.poll()
                                } else {
                                    if ((state as? Delivering)?.lastDelivered == true) {
                                        state = Complete
                                    }
                                    null
                                }
                            }
                        }
                        Complete -> throw UnsupportedOperationException("Shouldn't request after completed")
                        None -> setupRequestNext()
                    }
                }
                // We supress the warning, because removing it causes not infer type correctly
                @Suppress("UNREACHABLE_CODE")
                return@withLock null
            } catch (e: InterruptedException) {
                state = Error(e)
                debug("Blocking next error exit...")
                null
            }
        }
    }

    private fun setupRequestNext() {
        try {
            lock.withLock {

                val currentState = state
                val skip = when (currentState) {
                    is Loading, Complete -> true
                    is Error -> !currentState.hasRecovery
                    is Delivering -> currentState.prefetching || currentState.lastDelivered
                    None -> false
                }

                if (skip) {
                    debug("Skipping next request: current State = %s", currentState)
                    return
                }

                if (!isDisposed) {
                    val subscriber: DisposableSubscriber<T> = object : DisposableSubscriber<T>() {
                        var delivered: Int = 0
                        override fun onComplete() {
                            lock.withLock {
                                if (delivered > pageSize) {
                                    Timber.e("Error paging, delivered %d items out of the %d requested", delivered, pageSize)
                                } else {
                                    debug("Delivered %d items", delivered)
                                }
                                state = Delivering(delivered < pageSize)
                                mOffset += delivered
                                debug("Request completed...")
                            }
                        }

                        override fun onNext(t: T) {
                            lock.withLock {
                                unboundedQueue.offer(t)
                                state = Delivering(this)
                                debug("Received new item %s", t)
                                delivered++
                            }
                        }

                        override fun onError(e: Throwable) {
                            processError(e)
                        }
                    }
                    delegateDisposable.add(subscriber)
                    state = Loading(subscriber)
                    Flowable.fromPublisher(pageFactory.nextPageObservable(mOffset, pageSize)).subscribe(subscriber)
                } else {
                    debug("Request 'requested' but not started!! (state: %s, disposed: %s)", state, isDisposed)
                }
            }
        } catch (e: Throwable) {
            processError(e)
        }
    }

    private fun processError(e: Throwable) {
        rxPagingConfig.errorHandlingModel.processError(
                PageError(
                        mOffset,
                        e,
                        { state = Error(e) },
                        {
                            val pageRetry = PageRetry(e)
                            debug("Error... retry generated!!")
                            state = Error(e, pageRetry)
                            pageRetry
                        }
                )
        )
    }

    private inner class PageRetry(error: Throwable) : BaseRetry(error) {
        override fun performRetry() {
            lock.withLock {
                setupRequestNext()
                if (state !is Loading) {
                    debug("Failed to perform retry!")
                    state = Error(error)
                }
            }
        }

        override fun performAbort() {
            lock.withLock {
                state = Error(error)
            }
        }
    }
}

@SuppressLint("TimberArgCount")
private fun debug(fmt: String, vararg args: Any?) {
    if (DEBUG) {
        Timber.d("[%s] $fmt", Thread.currentThread().name, *args)
    }
}
