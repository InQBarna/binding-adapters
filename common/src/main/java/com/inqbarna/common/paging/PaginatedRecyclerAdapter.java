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

package com.inqbarna.common.paging;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collection;
import java.util.List;

/**
 * Created by Ricard on 14/9/15.
 *
 * @author Ricard
 * @author David García <david.garcia@inqbarna.com>
 */
public abstract class PaginatedRecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private final PaginatedAdapterDelegate<T> mPaginatedDelegate;

    public PaginatedRecyclerAdapter() {
        this(new PaginateConfig.Builder().build(), null);
    }

    public PaginatedRecyclerAdapter(
            PaginateConfig paginateConfig, @Nullable PaginatedAdapterDelegate.ProgressHintListener loadingListener) {
        mPaginatedDelegate = new PaginatedAdapterDelegate<>(this, loadingListener, paginateConfig);
    }

    public PaginatedRecyclerAdapter(@Nullable PaginatedAdapterDelegate.ProgressHintListener loadingListener) {
        this(new PaginateConfig.Builder().build(), loadingListener);
    }

    public void setLoadingIndicatorHint(@Nullable PaginatedAdapterDelegate.ProgressHintListener loadingListener) {
        mPaginatedDelegate.setLoadingIndicatorHint(loadingListener);
    }

    protected T getItem(int position) {
        return mPaginatedDelegate.getItem(position);
    }

    public void setItems(PaginatedList<T> items) {
        mPaginatedDelegate.setItems(items);
    }

    public void addNextPage(Collection<? extends T> pageItems, boolean lastPage) {
        mPaginatedDelegate.addNextPage(pageItems, lastPage);
    }

    private int getLastItemPosition() {
        return mPaginatedDelegate.getLastItemPosition();
    }

    @Override
    public int getItemCount() {
        return mPaginatedDelegate.getItemCount();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mPaginatedDelegate.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mPaginatedDelegate.onDetachedFromRecyclerView(recyclerView);
    }

    public List<T> editableList() {
        return mPaginatedDelegate.editableList();
    }
}
