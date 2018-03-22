package com.inqbarna.rxutil.paging

import android.support.v7.widget.RecyclerView
import com.inqbarna.common.paging.PaginatedAdapterDelegate
import com.inqbarna.common.paging.PaginatedList
import io.reactivex.Emitter
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiConsumer
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Publisher
import java.util.*
import java.util.concurrent.Callable

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 16/9/16
 */

class RxPagingAdapterDelegate<T>(
        adapter: RecyclerView.Adapter<*>,
        private val mRxPagingCallback: RxPagingCallback,
        paginateConfig: RxPagingConfig,
        loadingListener: PaginatedAdapterDelegate.ProgressHintListener?
) : PaginatedAdapterDelegate<T>(adapter, loadingListener, paginateConfig) {

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
        setDataFactory(factory, displayPageSize, requestPageSize, false)
    }

    private fun setDataFactory(factory: PageFactory<T>, displayPageSize: Int, requestPageSize: Int, endLoad: Boolean) {
        setDataStream(createStreamObservable(factory, displayPageSize, requestPageSize), endLoad)
    }

    private fun createStreamObservable(factory: PageFactory<T>, displayPageSize: Int, requestPageSize: Int): Flowable<out List<T>> {
        return createStream(factory, displayPageSize, requestPageSize)
    }

    private fun createStream(factory: PageFactory<T>, displayPageSize: Int, requestPageSize: Int): Flowable<List<T>> {
        return Flowable.generate(
                Callable<RequestState<T>> { RequestState(requestPageSize, factory) },
                BiConsumer<RequestState<T>, Emitter<T>> { state, emitter ->
                    val nextItem = state.blockingNext()
                    val error = state.error
                    when {
                        null != error -> emitter.onError(error)
                        state.completed -> emitter.onComplete()
                        else -> {
                            if (null == nextItem) {
                                emitter.onError(NullPointerException("Unexpected null value at this point, or complete or on error should come before this"))
                                return@BiConsumer
                            }
                            emitter.onNext(nextItem)
                        }
                    }
                }
        ).subscribeOn(Schedulers.computation(), false).buffer(displayPageSize).observeOn(AndroidSchedulers.mainThread(), false, 1)
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
}
