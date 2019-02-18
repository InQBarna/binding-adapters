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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.Observer;
import rx.RxReactiveStreams;

/**
 * Created by david on 17/09/16.
 */
class RequestState<T> {
    public final int pageSize;
    private int mOffset;
    private AtomicBoolean mCompleted;
    private AtomicReference<Throwable> mError;
    private PageFactory<T> mFactory;
    private final RxPagingConfig paginateConfig;

    RequestState(int pageSize, PageFactory<T> mFactory, RxPagingConfig paginateConfig) {
        this.pageSize = pageSize;
        this.mFactory = mFactory;
        this.paginateConfig = paginateConfig;
        mOffset = 0;
        mCompleted = new AtomicBoolean(false);
        mError = new AtomicReference<>(null);
        // TODO: [DG - 22/03/2018] Maybe add error handling on RxJava1... for now only on RxJava2
    }

    boolean getCompleted() {
        return mCompleted.get();
    }

    Throwable getError() {
        return mError.get();
    }

    Observable<? extends T> nextObservable() {
        int offset = mOffset;
        mOffset += pageSize;
        Observable<? extends T> observable = RxReactiveStreams.toObservable(mFactory.nextPageObservable(offset, pageSize));
        final AtomicInteger counter = new AtomicInteger(pageSize);

        return observable
                .doOnEach(
                        new Observer<T>() {
                            @Override
                            public void onCompleted() {
                                int remaining = counter.get();
                                if (remaining < 0) {
                                    mError.compareAndSet(null,
                                                         new IllegalArgumentException(
                                                                 "Given page observable produced " + (-1 * remaining) + " extra items, only " + pageSize + "expected"));
                                } else if (remaining > 0) {
                                    // Ok, source ended because we have fewer elements!
                                    mCompleted.compareAndSet(false, true);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                mError.compareAndSet(null, e);
                            }

                            @Override
                            public void onNext(T t) {
                                counter.decrementAndGet();
                            }
                        }
                );
    }
}
