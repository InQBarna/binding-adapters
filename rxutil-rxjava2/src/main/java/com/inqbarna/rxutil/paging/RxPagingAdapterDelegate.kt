package com.inqbarna.rxutil.paging

import androidx.recyclerview.widget.RecyclerView
import com.inqbarna.common.paging.PaginatedAdapterDelegate
import com.inqbarna.common.paging.PaginatedList
import io.reactivex.Emitter
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiConsumer
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Publisher
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 16/9/16
 */

class RxPagingAdapterDelegate<T>(
        adapter: RecyclerView.Adapter<*>,
        private val mRxPagingCallback: RxPagingCallback,
        paginateConfig: RxPagingConfig,
        loadingListener: PaginatedAdapterDelegate.ProgressHintListener?,
        itemRemovedCallback: ItemRemovedCallback<T>
) : PaginatedAdapterDelegate<T>(adapter, loadingListener, paginateConfig, itemRemovedCallback) {

    private val callbacks = object : RxPaginatedList.Callbacks {
        override fun onError(throwable: Throwable) {
            mRxPagingCallback.onError(throwable)
            endProgress()
        }

        override fun onCompleted() {
            mRxPagingCallback.onCompleted()
            endProgress()
        }

        override fun onItemsAdded(startPos: Int, size: Int) {
            val pgConfig = getPaginateConfig() as RxPagingConfig
            if (pgConfig.notifyAsInsertions) {
                onFinishAddItems(startPos, size)
            } else {
                endProgress()
            }
        }
    }

    private var activeDisposable: Disposable? = null

    override fun setItemsInternal(items: PaginatedList<T>, endLoad: Boolean) {
        setDataFactory(asPageFactory(items), items.size(), items.size(), endLoad)
    }

    private fun asPageFactory(items: PaginatedList<T>): PageFactory<T> {
        val allItems = ArrayList<T>()
        for (i in 0 until items.size()) {
            allItems.add(items.get(i))
        }

        return object : PageFactory<T> {
            override val initialData: List<T> get() = allItems
            override fun nextPageObservable(startOffset: Int, pageSize: Int): Publisher<out T> = Flowable.empty()
        }
    }

    fun setDataFactory(factory: PageFactory<T>, displayPageSize: Int, requestPageSize: Int) {
        setDataFactory(factory, displayPageSize, requestPageSize, factory.initialData.isNotEmpty())
    }

    private fun setDataFactory(factory: PageFactory<T>, displayPageSize: Int, requestPageSize: Int, endLoad: Boolean) {
        setDataStream(createStreamObservable(factory, displayPageSize, requestPageSize), endLoad)
    }

    private fun createStreamObservable(factory: PageFactory<T>, displayPageSize: Int, requestPageSize: Int): Flowable<out List<T>> {
        return createStream(factory, displayPageSize, requestPageSize)
    }

    private fun createStream(factory: PageFactory<T>, displayPageSize: Int, requestPageSize: Int): Flowable<List<T>> {
        return Flowable.generate(
                Callable<RequestState<T>> { RequestState(requestPageSize, factory, paginateConfig as RxPagingConfig) },
                BiConsumer<RequestState<T>, Emitter<T>> { state, emitter ->
                    var nextItem: T?

                    acquireNext@ while (true) {
                        nextItem = state.blockingNext()

                        // Important, we read state after call to blockingNext, as it does change state there!
                        val loadState = state.state

                        when (loadState) {
                            is Error -> {
                                if (loadState.hasRecovery) {
                                    loadState.recovery?.setCallbacks(
                                            object : RetryCallbacks {
                                                override fun onRetryRequested() {
                                                    postAction { onRecoveryInProgress(true) }
                                                }

                                                override fun onRetryAborted() {
                                                    postAction { onRecoveryInProgress(false) }
                                                }
                                            }
                                    )
                                    postAction { disableProgressAwaitForRecovery() }
                                } else {
                                    emitter.onError(loadState.error)
                                    return@BiConsumer
                                }
                            }
                            Complete -> {
                                emitter.onComplete()
                                return@BiConsumer
                            }
                            else -> break@acquireNext
                        }
                    }

                    if (null == nextItem) {
                        throw IllegalStateException("We shouldn't reach this point if value is null")
                    }

                    emitter.onNext(nextItem)
                },
                Consumer { s -> s.dispose() }
        ).subscribeOn(Schedulers.from(PAGE_PREFETCH)).buffer(displayPageSize).observeOn(AndroidSchedulers.mainThread(), false, 1)
    }

    private fun postAction(action: () -> Unit) {
        AndroidSchedulers.mainThread().scheduleDirect(action)
    }

    private fun setDataStream(stream: Flowable<out List<T>>, endLoad: Boolean) {
        activeDisposable?.dispose()
        val items = RxPaginatedList.create(stream, callbacks, paginateConfig as RxPagingConfig)
        activeDisposable = items as RxPaginatedList<T>
        beginProgress()
        super.setItemsInternal(items, endLoad)
    }

    override fun addNextPage(pageItems: Collection<T>, lastPage: Boolean) {
        throw UnsupportedOperationException("This delegate does not support this operation")
    }

    fun setDataFactory(factory: PageFactory<T>, pageSize: Int) {
        setDataFactory(factory, pageSize, pageSize)
    }

    companion object {
        private val counter: AtomicInteger = AtomicInteger(0)
        private val PAGE_PREFETCH: ExecutorService = Executors.newCachedThreadPool { Thread(it, nextName()) }
        private fun nextName(): String {
            return "PagePrefetcher-${counter.getAndIncrement()}"
        }
    }

}
