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

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.inqbarna.common.paging.PaginateConfig;
import com.inqbarna.common.paging.PaginatedAdapterDelegate;
import com.inqbarna.common.paging.PaginatedList;

import java.util.Collection;
import java.util.List;

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 14/9/16
 */

public class PaginatedBindingAdapter<T extends TypeMarker> extends BindingAdapter {

    @Nullable private final PaginatedAdapterDelegate.ProgressHintListener mListener;
    private                 PaginatedAdapterDelegate<T>                   mDelegate;
    private                 PaginateConfig                                paginateConfig;

    private final PaginatedAdapterDelegate.ItemRemovedCallback<T> itemRemovedCallback = new PaginatedAdapterDelegate.ItemRemovedCallback<T>() {
        @Override
        public void onItemRemoved(T item) {
            onRemovingItem(item);
        }
    };

    protected void onRemovingItem(T item) {
        /* no-op */
    }

    public PaginatedBindingAdapter() {
        this(new PaginateConfig.Builder().build(), null);
    }

    public PaginatedBindingAdapter(
            PaginateConfig paginateConfig, @Nullable PaginatedAdapterDelegate.ProgressHintListener listener) {
        this.paginateConfig = paginateConfig;
        mListener = listener;
    }

    protected void setPaginateConfig(PaginateConfig paginateConfig) {
        this.paginateConfig = paginateConfig;
    }

    @Override
    public T getDataAt(int position) {
        return getDelegate().getItem(position);
    }

    @Override
    public int getItemCount() {
        return getDelegate().getItemCount();
    }

    public void setLoadingIndicatorHint(@Nullable PaginatedAdapterDelegate.ProgressHintListener loadingListener) {
        getDelegate().setLoadingIndicatorHint(loadingListener);
    }

    public T getItem(int position) {
        return getDelegate().getItem(position);
    }

    public void setItems(PaginatedList<T> items) {
        getDelegate().setItems(items);
    }

    public void addNextPage(Collection<? extends T> pageItems, boolean lastPage) {
        getDelegate().addNextPage(pageItems, lastPage);
    }

    public void clear() {
        getDelegate().clear();
    }

    public int getLastItemPosition() {
        return getDelegate().getLastItemPosition();
    }



    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        getDelegate().onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        getDelegate().onDetachedFromRecyclerView(recyclerView);
    }

    public List<T> editableList() {
        return getDelegate().editableList();
    }

    protected final PaginatedAdapterDelegate<T> getDelegate() {
        if (null == mDelegate) {
            mDelegate = createDelegate(paginateConfig, mListener, itemRemovedCallback);
        }
        return mDelegate;
    }

    protected PaginatedAdapterDelegate<T> createDelegate(
            PaginateConfig paginateConfig, @Nullable PaginatedAdapterDelegate.ProgressHintListener listener,
            PaginatedAdapterDelegate.ItemRemovedCallback<T> itemRemovedCallback) {
        return new PaginatedAdapterDelegate<T>(this, listener, this.paginateConfig, itemRemovedCallback);
    }
}
