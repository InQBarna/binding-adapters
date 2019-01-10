package com.inqbarna.rxutil.paging

import androidx.recyclerview.widget.RecyclerView
import com.inqbarna.common.AdapterSyncList
import com.inqbarna.common.paging.PaginatedAdapterDelegate
import com.inqbarna.common.paging.PaginatedList
import io.reactivex.Flowable
import io.reactivex.subscribers.DisposableSubscriber

/**
 * @author David García <david.garcia></david.garcia>@inqbarna.com>
 * @version 1.0 16/9/16
 */

class RxPaginatedList<U> private constructor(
        stream: Flowable<out List<U>>,
        private val callbacks: Callbacks,
        private val rxConfig: RxPagingConfig
) : DisposableSubscriber<List<U>>(), PaginatedList<U> {

    private val data: MutableList<U> = mutableListOf()
    private var completed: Boolean = false

    internal interface Callbacks : RxPagingCallback {
        fun onItemsAdded(startPos: Int, size: Int)
    }

    init {
        completed = false
        stream.subscribe(this)
    }

    override fun onStart() {
        requestNext()
    }

    override fun get(location: Int): U? {
        return data.getOrNull(location)
    }

    override fun size(): Int {
        return data.size
    }

    override fun hasMorePages(): Boolean = !completed

    override fun requestNext() {
        if (!isDisposed) {
            request(1)
            return
        }
        throw IllegalStateException("You requested data after completed!")
    }

    override fun appendPageItems(items: Collection<U>, last: Boolean) {
        throw UnsupportedOperationException("This is not supported, only internal data flow will be accepted")
    }

    override fun clear(itemRemovedCallback: PaginatedAdapterDelegate.ItemRemovedCallback<U>?) {
        if (null != itemRemovedCallback) {
            val iterator = data.iterator()
            while (iterator.hasNext()) {
                itemRemovedCallback.onItemRemoved(iterator.next())
                iterator.remove()
            }
        } else {
            data.clear()
        }
        completed = true
        callbacks.onCompleted()
        dispose()
    }

    override fun onComplete() {
        completed = true
        callbacks.onCompleted()
        dispose()
    }

    override fun onError(e: Throwable) {
        completed = true
        callbacks.onError(e)
        dispose()
    }

    override fun onNext(us: List<U>) {
        val startSize = data.size
        data.addAll(us)
        callbacks.onItemsAdded(startSize, us.size)
    }

    override fun editableList(callbackAdapter: RecyclerView.Adapter<*>?): List<U> {
        return AdapterSyncList(data, callbackAdapter)
    }

    companion object {
        internal fun <T> create(stream: Flowable<out List<T>>, callbacks: Callbacks, config: RxPagingConfig): PaginatedList<T> {
            return RxPaginatedList(stream, callbacks, config)
        }
    }

}
