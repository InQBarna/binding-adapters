package com.inqbarna.navigation

import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.google.common.base.Optional
import com.inqbarna.navigation.base.AppRoute
import com.inqbarna.navigation.base.Navigator
import com.inqbarna.navigation.base.ResultToken
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 09/07/2018
 */
interface NavigationResponsesCallback {
    /**
     * This method should check if this navigation result is handy for us, and then return true if it was properly handled
     */
    fun onNavigationResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean
}

interface NavigationResponses {
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean
    fun addCallback(callback: NavigationResponsesCallback)
    fun removeCallback(callback: NavigationResponsesCallback)
}

interface ResultsHandler : NavigationResponsesCallback {
    fun addResultsHandler(token: ResultToken, handler: (Int, Intent?) -> Unit)
    fun removeResultsHandler(token: ResultToken)
}

interface NavigationResult<T> {
    suspend fun awaitResult(): T
}
interface CoroutineResultsHandler :
    NavigationResponsesCallback {
    fun <T> addResultProcessor(token: ResultToken, processor: (Int, Intent?) -> T): NavigationResult<T>
}

// Note: Do not use this method, only for tests!!!
@VisibleForTesting
internal fun <T> NavigationResult<T>.setTestResult(res: T) {
    val impl = (this as? StandardCoroutineResponseHandler.NavigationResultImpl<T>) ?: throw IllegalArgumentException("Expected NavigationResultImpl, but got ${this::class}")
    impl.setResult(Result.success(res))
}

// Note: Do not use this method, only for tests!!!
@VisibleForTesting
internal fun <T> NavigationResult<T>.setTestFailure(error: Exception) {
    val impl = (this as? StandardCoroutineResponseHandler.NavigationResultImpl<T>) ?: throw IllegalArgumentException("Expected NavigationResultImpl, but got ${this::class}")
    impl.setResult(Result.failure(error))
}

internal fun <T> CoroutineResultsHandler.resultForToken(token: ResultToken): NavigationResult<T> {
    val impl = (this as? StandardCoroutineResponseHandler) ?: throw IllegalArgumentException("Expected StandardCoroutineResponseHandler, but got ${this::class}")
    @Suppress("UNCHECKED_CAST")
    return impl[token] as NavigationResult<T>
}

private class StandardCoroutineResponseHandler :
    CoroutineResultsHandler {

    internal class NavigationResultImpl<T>(private val processor: (Int, Intent?) -> T) :
        NavigationResult<T> {
        private var response: Optional<Result<T>> = Optional.absent()
        private var cont: CancellableContinuation<T>? = null
        internal fun processResults(responseCode: Int, data: Intent?) {
            val result = try {
                Result.success(processor(responseCode, data))
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                } else {
                    Result.failure<T>(e)
                }
            }

            setResult(result)
        }

        @VisibleForTesting
        internal fun setResult(res: Result<T>) {
            synchronized(this) {
                response = Optional.of(res)
                cont?.resumeWith(res)
            }
        }

        internal fun cancel() {
            synchronized(this) {
                response = Optional.of(Result.failure(CancellationException()))
                cont?.cancel()
            }
        }

        override suspend fun awaitResult(): T {
            return suspendCancellableCoroutine { cont ->
                synchronized(this@NavigationResultImpl) {
                    if (response.isPresent) {
                        val result = response.get()
                        if (result.isFailure && result.exceptionOrNull() is CancellationException) {
                            cont.cancel()
                        } else {
                            cont.resumeWith(result)
                        }
                    } else {
                        this@NavigationResultImpl.cont = cont
                    }
                }
            }
        }
    }

    @VisibleForTesting
    internal operator fun get(token: ResultToken): NavigationResult<*> {
        return handlers[token]!!
    }

    private val handlers = mutableMapOf<ResultToken, NavigationResultImpl<*>>()
    override fun onNavigationResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        synchronized(this) {
            for ((token, response) in handlers) {
                if (token.shouldRespondTo(requestCode)) {
                    response.processResults(resultCode, data)
                    removeHandler(token)
                    return true
                }
            }
        }
        return false
    }

    override fun <T> addResultProcessor(token: ResultToken, processor: (Int, Intent?) -> T): NavigationResult<T> {
        return synchronized(handlers) {
            val result =
                StandardCoroutineResponseHandler.NavigationResultImpl(
                    processor
                )
            handlers.put(token, result)?.cancel()
            result
        }
    }

    private fun removeHandler(token: ResultToken) {
        synchronized(token) {
            handlers.remove(token)
        }
    }
}

/**
    Navigate installing result handler.

    WARNING: This won't work if activity holding the navigator is destroyed, so keep the usages of this method to a minimum, prefering manual registration
    of receiver VM at onCreate()
 */
fun Navigator.navigateForResult(appRoute: AppRoute, handler: (Int, Intent?) -> Unit) {
    val resultsHandler = standardResultHandler()
    responsesManager().addCallback(resultsHandler)
    resultsHandler.addResultsHandler(appRoute.createResultToken()) { resultCode: Int, data: Intent? ->
        handler(resultCode, data)
        responsesManager().removeCallback(resultsHandler)
    }
    navigateTo(appRoute)
}

suspend fun <T : Any> Navigator.awaitNavigationResult(destination: AppRoute, processor: (Int, Intent?) -> T): T {
    val resultHandler = standardCoroutinesResultHandler()
    responsesManager().addCallback(resultHandler)

    val deferred = coroutineScope {
        async(Dispatchers.Main) {
            val navResult = resultHandler.addResultProcessor(destination.createResultToken(), processor)
            navigateTo(destination)
            val result = navResult.awaitResult()
            responsesManager().removeCallback(resultHandler)
            result
        }
    }
    return deferred.await()
}

fun standardResultHandler(): ResultsHandler = StandardResultHandler()
fun standardCoroutinesResultHandler(): CoroutineResultsHandler = StandardCoroutineResponseHandler()

private class StandardResultHandler : ResultsHandler {
    private val handlers = mutableMapOf<ResultToken, (Int, Intent?) -> Unit>()
    override fun addResultsHandler(token: ResultToken, handler: (Int, Intent?) -> Unit) {
        synchronized(handlers) {
            handlers[token] = handler
        }
    }

    override fun removeResultsHandler(token: ResultToken) {
        synchronized(handlers) {
            handlers.remove(token)
        }
    }

    override fun onNavigationResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        synchronized(handlers) {
            for ((token, handler) in handlers) {
                if (token.shouldRespondTo(requestCode)) {
                    handler(resultCode, data)
                    handlers.remove(token)
                    return true
                }
            }
        }
        return false
    }
}
