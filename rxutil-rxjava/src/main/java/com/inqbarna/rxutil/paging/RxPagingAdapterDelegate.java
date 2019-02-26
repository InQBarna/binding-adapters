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

package com.inqbarna.rxutil.paging;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.inqbarna.common.paging.PaginatedAdapterDelegate;
import com.inqbarna.common.paging.PaginatedList;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.RxReactiveStreams;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func3;
import rx.observables.AsyncOnSubscribe;

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 16/9/16
 */

public class RxPagingAdapterDelegate<T> extends PaginatedAdapterDelegate<T> {

    private RxPaginatedList.Callbacks mCallbacks = new RxPaginatedList.Callbacks() {
        @Override
        public void onError(Throwable throwable) {
            mRxPagingCallback.onError(throwable);
            endProgress();
        }

        @Override
        public void onCompleted() {
            mRxPagingCallback.onCompleted();
            endProgress();
        }

        @Override
        public void onItemsAdded(int startPos, int size) {
            RxPagingConfig pgConfig = (RxPagingConfig) getPaginateConfig();
            if (pgConfig.notifyAsInsertions) {
                onFinishAddItems(startPos, size);
            } else {
                endProgress();
            }
        }
    };

    private RxPagingCallback mRxPagingCallback;
    private Subscription mActiveSubscription;

    public RxPagingAdapterDelegate(
            RecyclerView.Adapter adapter, @NonNull RxPagingCallback rxPagingCallback, RxPagingConfig paginateConfig,
            @Nullable ProgressHintListener loadingListener, ItemRemovedCallback<T> itemRemovedCallback) {
        super(adapter, loadingListener, paginateConfig, itemRemovedCallback);
        mRxPagingCallback = rxPagingCallback;
    }

    @Override
    protected void setItemsInternal(PaginatedList<T> items, boolean endLoad) {
        setDataFactory(asPageFactory(items), items.size(), items.size(), endLoad);
    }

    private PageFactory<T> asPageFactory(PaginatedList<T> items) {
        final List<T> allItems = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            allItems.add(items.get(i));
        }

        return new PageFactory<T>() {
            @Override
            public List<? extends T> getInitialData() {
                return allItems;
            }

            @Override
            public Publisher<? extends T> nextPageObservable(int start, int size) {
                return RxReactiveStreams.toPublisher(Observable.empty());
            }
        };
    }

    public void setDataFactory(PageFactory<T> factory, int displayPageSize, int requestPageSize) {
        setDataFactory(factory, displayPageSize, requestPageSize, false);
    }

    private void setDataFactory(PageFactory<T> factory, int displayPageSize, int requestPageSize, boolean endLoad) {
        setDataStream(createStreamObservable(factory, displayPageSize, requestPageSize), endLoad);
    }

    private Observable<? extends List<? extends T>> createStreamObservable(PageFactory<T> factory, int displayPageSize, int requestPageSize) {
        return createStream(factory, displayPageSize, requestPageSize);
    }

    private Observable<List<T>> createStream(final PageFactory<T> factory, final int displayPageSize, final int requestPageSize) {
        return Observable.create(
                AsyncOnSubscribe.createStateful(
                        () -> new RequestState<>(requestPageSize, factory, (RxPagingConfig) getPaginateConfig()),
                        (Func3<RequestState<T>, Long, Observer<Observable<? extends T>>, RequestState<T>>) (state, aLong, observableObserver) -> {
                            Throwable error = state.getError();
                            if (null != error) {
                                observableObserver.onError(error);
                            } else if (state.getCompleted()) {
                                observableObserver.onCompleted();
                            } else {
                                Observable<? extends T> ob = state.nextObservable();
                                observableObserver.onNext(ob);
                            }
                            return state;
                        }
                )
        ).buffer(displayPageSize).observeOn(AndroidSchedulers.mainThread(), 1);
    }

    private void setDataStream(Observable<? extends List<? extends T>> stream, boolean endLoad) {
        if (null != mActiveSubscription) {
            mActiveSubscription.unsubscribe();
        }
        final PaginatedList<T> items = RxPaginatedList.create(stream, mCallbacks, (RxPagingConfig) getPaginateConfig());
        mActiveSubscription = ((RxPaginatedList<T>)items);
        beginProgress();
        super.setItemsInternal(items, endLoad);
    }

    @Override
    public void addNextPage(Collection<? extends T> pageItems, boolean lastPage) {
        throw new UnsupportedOperationException("This delegate does not support this operation");
    }

    public void setDataFactory(PageFactory<T> factory, int pageSize) {
        setDataFactory(factory, pageSize, pageSize);
    }
}
