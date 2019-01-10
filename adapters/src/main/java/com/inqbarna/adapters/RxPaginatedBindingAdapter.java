package com.inqbarna.adapters;

import androidx.annotation.Nullable;

import com.inqbarna.common.paging.PaginateConfig;
import com.inqbarna.common.paging.PaginatedAdapterDelegate;
import com.inqbarna.rxutil.paging.PageFactory;
import com.inqbarna.rxutil.paging.RxPagingAdapterDelegate;
import com.inqbarna.rxutil.paging.RxPagingCallback;
import com.inqbarna.rxutil.paging.RxPagingConfig;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 16/9/16
 */

public class RxPaginatedBindingAdapter<T extends TypeMarker> extends PaginatedBindingAdapter<T> {

    private final RxPagingCallback mRxPagingCallback;

    public RxPaginatedBindingAdapter(
            RxPagingCallback callback, RxPagingConfig paginateConfig,
            @Nullable PaginatedAdapterDelegate.ProgressHintListener listener) {
        super(paginateConfig, listener);
        mRxPagingCallback = callback;
    }

    public RxPaginatedBindingAdapter(RxPagingCallback callback, RxPagingConfig paginateConfig) {
        this(callback, paginateConfig, null);
    }

    public RxPaginatedBindingAdapter(RxPagingCallback callback) {
        this(callback, new RxPagingConfig.Builder().build(), null);
    }

    @Override
    protected PaginatedAdapterDelegate<T> createDelegate(
            PaginateConfig paginateConfig,
            @Nullable PaginatedAdapterDelegate.ProgressHintListener listener, PaginatedAdapterDelegate.ItemRemovedCallback<T> itemRemovedCallback) {
        if (!(paginateConfig instanceof RxPagingConfig)) {
            throw new IllegalArgumentException("Expected to have created RxPaginationConfig specifics");
        }
        return new RxPagingAdapterDelegate<>(this, mRxPagingCallback, (RxPagingConfig) paginateConfig, listener, itemRemovedCallback);
    }


    public void setDataFactory(PageFactory<T> factory, int pageSize) {
        ((RxPagingAdapterDelegate<T>)getDelegate()).setDataFactory(factory, pageSize);
    }

    public void setDataFactory(PageFactory<T> factory, int displayPageSize, int requestPageSize) {
        ((RxPagingAdapterDelegate<T>)getDelegate()).setDataFactory(factory, displayPageSize, requestPageSize);
    }

}
