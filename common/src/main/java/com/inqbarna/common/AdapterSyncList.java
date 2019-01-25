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

package com.inqbarna.common;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.AbstractList;
import java.util.List;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 29/9/16
 */
public class AdapterSyncList<T> extends AbstractList<T> {
    private           List<T>              mWrappedList;
    @Nullable private RecyclerView.Adapter mAdapter;

    public AdapterSyncList(List<T> wrappedList, @Nullable RecyclerView.Adapter adapter) {
        mWrappedList = wrappedList;
        mAdapter = adapter;
    }

    @Override
    public int size() {
        return mWrappedList.size();
    }

    @Override
    public T get(int index) {
        return mWrappedList.get(index);
    }

    @Override
    public T set(int index, T element) {
        final T set = mWrappedList.set(index, element);
        if (null != mAdapter) {
            mAdapter.notifyItemChanged(index);
        }
        return set;
    }

    @Override
    public void add(int index, T element) {
        mWrappedList.add(index, element);
        if (null != mAdapter) {
            mAdapter.notifyItemInserted(index);
        }
    }

    @Override
    public T remove(int index) {
        final T remove = mWrappedList.remove(index);
        if (null != mAdapter) {
            mAdapter.notifyItemRemoved(index);
        }
        return remove;
    }
}
