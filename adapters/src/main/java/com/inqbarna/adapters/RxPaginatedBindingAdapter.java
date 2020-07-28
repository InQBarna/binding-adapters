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

package com.inqbarna.adapters;

import com.google.common.base.Preconditions;
import com.inqbarna.common.paging.PaginateConfig;
import com.inqbarna.common.paging.PaginatedAdapterDelegate;
import com.inqbarna.rxutil.paging.PageFactory;
import com.inqbarna.rxutil.paging.RxPagingAdapterDelegate;
import com.inqbarna.rxutil.paging.RxPagingCallback;
import com.inqbarna.rxutil.paging.RxPagingConfig;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 16/9/16
 */

public class RxPaginatedBindingAdapter<T extends TypeMarker> extends PaginatedBindingAdapter<T> {

    private RxPagingCallback mRxPagingCallback;

    public RxPaginatedBindingAdapter(
            @NonNull RxPagingCallback callback, @NonNull RxPagingConfig paginateConfig,
            @Nullable PaginatedAdapterDelegate.ProgressHintListener listener) {
        super(paginateConfig, listener);
        mRxPagingCallback = callback;
    }

    public RxPaginatedBindingAdapter(@NonNull RxPagingCallback callback, @NonNull RxPagingConfig paginateConfig) {
        this(callback, paginateConfig, null);
    }

    public RxPaginatedBindingAdapter(@NonNull RxPagingCallback callback) {
        this(callback, new RxPagingConfig.Builder().build(), null);
    }

    public RxPaginatedBindingAdapter() {
        super(new RxPagingConfig.Builder().build(), null);
    }

    public void setPaginateConfig(@NonNull RxPagingConfig paginateConfig) {
        super.setPaginateConfig(paginateConfig);
    }

    public void setRxCallbacks(@NonNull RxPagingCallback callbacks) {
        mRxPagingCallback = callbacks;
    }

    @Override
    protected PaginatedAdapterDelegate<T> createDelegate(
            PaginateConfig paginateConfig,
            @Nullable PaginatedAdapterDelegate.ProgressHintListener listener, PaginatedAdapterDelegate.ItemRemovedCallback<T> itemRemovedCallback) {
        if (!(paginateConfig instanceof RxPagingConfig)) {
            throw new IllegalArgumentException("Expected to have created RxPaginationConfig specifics");
        }
        Preconditions.checkState(mRxPagingCallback != null, "paging callback should be set before connecting the adapter to the RecyclerView");
        return new RxPagingAdapterDelegate<>(this, mRxPagingCallback, (RxPagingConfig) paginateConfig, listener, itemRemovedCallback);
    }


    public void setDataFactory(PageFactory<T> factory, int pageSize) {
        ((RxPagingAdapterDelegate<T>)getDelegate()).setDataFactory(factory, pageSize);
    }

    public void setDataFactory(PageFactory<T> factory, int displayPageSize, int requestPageSize) {
        ((RxPagingAdapterDelegate<T>)getDelegate()).setDataFactory(factory, displayPageSize, requestPageSize);
    }

}
