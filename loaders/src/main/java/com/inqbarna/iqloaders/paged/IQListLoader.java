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

package com.inqbarna.iqloaders.paged;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.core.os.OperationCanceledException;

import com.inqbarna.common.paging.PaginatedList;
import com.inqbarna.iqloaders.IQLoader;
import com.inqbarna.iqloaders.IQProvider;
import com.inqbarna.iqloaders.IQProviders;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class IQListLoader<T> extends IQLoader<PaginatedList<T>> {

    private static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_FIRST_PAGE = 1;
    private final int mFirstPage;

    private LoaderPaginatedList<T> mData;
    private int                    mPageSize;

    private static class Request {
        public static final int NO_SPECIAL = -1;
        public static final int RELOAD = -2;
        final int page;
        final int size;

        final int    requestCode;
        final Object payload;

        public Request(int page, int size) {
            this.page = page;
            this.size = size;
            requestCode = NO_SPECIAL;
            payload = false;
        }

        public Request(int requestCode, Object payload) {
            this.requestCode = requestCode;
            this.payload = payload;
            page = 0;
            size = 0;
        }

        public boolean isSpecialRequest() {
            return requestCode > 0;
        }
    }

    private final Deque<Request> mRequests = new LinkedList<>();
    private boolean mLoading;


	public IQListLoader(Context context) {
        this(context, DEFAULT_FIRST_PAGE, DEFAULT_PAGE_SIZE);
    }

    public IQListLoader(Context ctxt, int firstPage, int pageSize) {
        super(ctxt);
        this.mFirstPage = firstPage;
        mPageSize = pageSize;
        mRequests.offer(new Request(mFirstPage, mPageSize));
    }


    /**
     * Request to be called aside, this will be usefull for list update single item inside it for instance
     * @param requestCode
     * @param payload
     */
    protected void placeSpecialRequest(int requestCode, Object payload) {
        if (requestCode <= 0) {
            throw new IllegalArgumentException("request code needs to be > 0");
        }
        synchronized (mRequests) {
            addRequest(new Request(requestCode, payload));
        }
    }

    protected abstract boolean onServeSpecialRequest(int requestCode, List<T> inOutData, Object requestPayload);

    protected void requestReload() {
        synchronized (mRequests) {
            mRequests.clear();
            addRequest(new Request(Request.RELOAD, null));
        }
    }

    @Override
	public final IQProvider<PaginatedList<T>> loadInBackground() {

        synchronized (mRequests) {
            if (mLoading) {
                throw new IllegalStateException("We're loading and loadInBackground was called again! something bad happened");
            }
            mLoading = true;
        }

        int numRegularRequests = 0;

        try {
            LoaderPaginatedList<T> data = null;

            Request nextRequest;
            synchronized (mRequests) {
                // Shouldn't be null, if it is we're using onContentChange without placing a request
                nextRequest = mRequests.remove();
            }


            while (null != nextRequest) {

                if (nextRequest.isSpecialRequest()) {

                    if (nextRequest.requestCode == Request.RELOAD) {
                        PageProvider<T> listPageProvider = loadPageInBackground(mFirstPage, mPageSize);
                        data = new LoaderPaginatedList<>(this, listPageProvider);
                    } else {
                        if (null != mData) {
                            if (onServeSpecialRequest(nextRequest.requestCode, mData.editableList(), nextRequest.payload)) {
                                data = createReturnPage(null);
                            }
                        }
                        /* else: just allow special requests to alter previous data set. Request will be silently ignored */
                    }
                } else {
                    numRegularRequests++;
                    PageProvider<T> listPageProvider = loadPageInBackground(nextRequest.page, nextRequest.size);
                    if (null == data) {
                        data = createReturnPage(listPageProvider);
                    } else {
                        data.addPage(listPageProvider);
                    }
                }

                if (isLoadInBackgroundCanceled()) {
                    synchronized (mRequests) {
                        mRequests.offerFirst(nextRequest);
                    }
                    return IQProviders.fromError(new OperationCanceledException("Load was cancelled, result will be ignored"));
                } else {
                    synchronized (mRequests) {
                        nextRequest = mRequests.poll();
                    }
                }
            }

            if (null != data) {
                mData = data;
                return IQProviders.<PaginatedList<T>>fromResult(data);
            } else if (numRegularRequests == 0) {
                return abortResult();
            } else {
                return IQProviders.fromResult(null);
            }
        } catch (Throwable throwable) {
            return IQProviders.fromError(throwable);
        } finally {
            synchronized (mRequests) {
                mLoading = false;
            }
        }
    }

    @Nullable
    protected LoaderPaginatedList<T> createReturnPage(@Nullable PageProvider<T> provider) throws Throwable {
        LoaderPaginatedList<T> data = null;
        if (null != mData) {
            data = new LoaderPaginatedList<>(this, mData);
        }
        if (provider != null) {
            if (null == data) {
                data = new LoaderPaginatedList<>(this, provider);
            } else {
                data.addPage(provider);
            }
        }
        return data;
    }

    @Override
    public final void onContentChanged() {
        throw new UnsupportedOperationException("You are not allowed to call this method directly. Sorry man!");
    }

    public abstract PageProvider<T> loadPageInBackground(int page, int pageSize);

	void loadNextPage() {
        synchronized (mRequests) {

            final Iterator<Request> requestIterator = mRequests.descendingIterator();
            Request nextRequest = null;
            while (requestIterator.hasNext()) {
                final Request r = requestIterator.next();
                if (!r.isSpecialRequest()) {
                    nextRequest = new Request(r.page + 1, mPageSize);
                    break;
                }
            }

            if (null == nextRequest) {
                if (null == mData) {
                    nextRequest = new Request(mFirstPage, mPageSize);
                } else {
                    nextRequest = new Request(mData.getLastPage() + 1, mPageSize);
                }
            }

            addRequest(nextRequest);
        }
	}

    private void addRequest(Request nextRequest) {
        synchronized (mRequests) {
            mRequests.offer(nextRequest);

            if (!mLoading) {
                super.onContentChanged();
            }
        }
    }
}
