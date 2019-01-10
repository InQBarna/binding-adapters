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
