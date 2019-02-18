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
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 14/9/16
 */
public class PaginatedAdapterDelegate<T> {

    public static final int DEFAULT_REQUEST_DISTANCE = 5;
    private final RecyclerView.Adapter mAdapter;
    private final ItemRemovedCallback<T> itemRemovedCallback;
    private       PaginatedList<T>     mList;
    private       ProgressHintListener mProgressHintListener;
    private       int                  mMinRequestDistance;
    private       boolean              mPageRequested;
    private final PaginateConfig       mPaginateConfig;
    private       boolean              mRecoveryInProgress;


    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            final int childCount = recyclerView.getChildCount();
            if (childCount == 0) {
                return;
            }
            final View lastChild = recyclerView.getChildAt(childCount - 1);
            final int lastVisiblePosition = recyclerView.getChildAdapterPosition(lastChild);

            if (hasMorePages() && (getLastItemPosition() - lastVisiblePosition) <= mMinRequestDistance) {
                innerRequestNext();
            }
        }
    };

    protected boolean hasMorePages() {
        return null != mList && mList.hasMorePages();
    }

    public PaginatedAdapterDelegate(RecyclerView.Adapter adapter, @Nullable ProgressHintListener loadingListener, PaginateConfig paginateConfig) {
        this(adapter, loadingListener, paginateConfig, null);
    }

    public PaginatedAdapterDelegate(RecyclerView.Adapter adapter, @Nullable ProgressHintListener loadingListener, PaginateConfig paginateConfig,
            ItemRemovedCallback<T> itemRemovedCallback) {
        this.itemRemovedCallback = itemRemovedCallback;
        mMinRequestDistance = DEFAULT_REQUEST_DISTANCE;
        mAdapter = adapter;
        mPaginateConfig = paginateConfig;
        mProgressHintListener = loadingListener;
    }

    void requestNextPage() {
        mList.requestNext();
    }

    protected PaginateConfig getPaginateConfig() {
        return mPaginateConfig;
    }

    public T getItem(int position) {
        return null != mList ? mList.get(position) : null;
    }

    public void setItems(PaginatedList<T> items) {
        setItemsInternal(items, true);
    }

    protected void setItemsInternal(PaginatedList<T> items, boolean endLoad) {
        mList = items;
        if (endLoad) {
            if (null != mProgressHintListener) {
                mProgressHintListener.setLoadingState(false);
            }
            mPageRequested = false;
        }
        mAdapter.notifyDataSetChanged();
    }

    protected boolean getPageRequested() {
        return mPageRequested;
    }

    public void clear() {
        if (null != mList) {
            mList.clear(itemRemovedCallback);
        }
        mList = null;
        endProgress();
    }

    protected void beginProgress() {
        if (null != mProgressHintListener) {
            mProgressHintListener.setLoadingState(true);
        }
        mPageRequested = true;
    }

    protected void endProgress() {
        if (null != mProgressHintListener) {
            mProgressHintListener.setLoadingState(false);
        }
        mPageRequested = false;
        mAdapter.notifyDataSetChanged();
    }

    protected void disableProgressAwaitForRecovery() {
        if (null != mProgressHintListener) {
            mProgressHintListener.setLoadingState(false);
        }
        mRecoveryInProgress = true;
    }

    protected void onRecoveryInProgress(boolean recovered) {
        mRecoveryInProgress = false;
        if (!recovered) {
            mPageRequested = false;
        } else {
            if (null != mProgressHintListener && mPageRequested) {
                mProgressHintListener.setLoadingState(true);
            }
        }
    }

    public void addNextPage(Collection<? extends T> pageItems, boolean lastPage) {
        if (null == mList) {
            throw new IllegalStateException("You need first to initialize the PaginatedList");
        }
        final int initialSize = mList.size();
        mList.appendPageItems(pageItems, lastPage);


        onFinishAddItems(initialSize, pageItems.size());
    }


    protected void onFinishAddItems(int startPos, int size) {
        if (null != mProgressHintListener) {
            mProgressHintListener.setLoadingState(false);
        }

        mPageRequested = false;
        mAdapter.notifyItemRangeInserted(startPos, size);
    }

    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    private void innerRequestNext() {
        if (!mPageRequested && null != mList) {
            mPageRequested = true;
            if (null != mProgressHintListener && !mRecoveryInProgress) {
                mProgressHintListener.setLoadingState(true);
            }
            requestNextPage();
        }
    }

    public void setLoadingIndicatorHint(@Nullable ProgressHintListener loadingListener) {
        mProgressHintListener = loadingListener;
    }

    public int getLastItemPosition() {
        return Math.max(0, getItemCount() - 1);
    }

    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(mScrollListener);
    }

    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        recyclerView.removeOnScrollListener(mScrollListener);
    }

    /**
     * @author David García <david.garcia@inqbarna.com>
     * @version 1.0 14/9/16
     */
    public interface ProgressHintListener {
        void setLoadingState(boolean loading);
    }

    public List<T> editableList() {
        if (null == mList) {
            // TODO: 29/9/16 What's actually the best return? Or is it better to crash... Returning ArrayList will be at least harmless, but could hide rare issues
            return new ArrayList<>();
        } else {
            return mList.editableList(mAdapter);
        }
    }


    public interface ItemRemovedCallback<T> {
        void onItemRemoved(T item);
    }
}
