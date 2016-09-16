package com.inqbarna.iqloaders.paged;

import com.inqbarna.common.paging.PaginatedList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by David García <david.garcia@inqbarna.com> on 24/11/14.
 */
class LoaderPaginatedList<U> implements PaginatedList<U> {

    private List<U> list;
    private boolean completed;
    private int lastPage;

    private IQListLoader<U> mLoader;

    public static <T> PaginatedList<T> fromData(Collection<? extends T> fullList) {
        return new LoaderPaginatedList<T>(null, new ArrayList<>(fullList), true, 1);
    }

    LoaderPaginatedList(IQListLoader<U> loader, List<U> list, boolean completed, int lastPage) {
        mLoader = loader;
        this.list = list;
        this.completed = completed;
        this.lastPage = lastPage;
    }

    LoaderPaginatedList(IQListLoader<U> loader, PageProvider<U> provider) throws Throwable {
        mLoader = loader;
        Collection<U> elems = provider.get();
        list = new ArrayList<U>(elems);
        lastPage = provider.getCurrentPage();
        completed = provider.isCompleted();
    }

    LoaderPaginatedList(IQListLoader<U> loader, LoaderPaginatedList<U> other) {
        mLoader = loader;
        this.list = other.list;
        this.lastPage = other.lastPage;
        this.completed = other.completed;
    }

    @Override
    public U get(int location) {
        return null != list ? list.get(location) : null;
    }

    @Override
    public int size() {
        return null != list ? list.size() : 0;
    }

    @Override
    public boolean hasMorePages() {
        return !completed;
    }

    @Override
    public void requestNext() {
        if (null != mLoader && !completed && !mLoader.isReset()) {
            mLoader.loadNextPage();
        }
    }

    @Override
    public void appendPageItems(Collection<? extends U> items, boolean last) {
        list.addAll(items);
        lastPage++;
        completed = last;
    }

    @Override
    public void clear() {
        list.clear();
        completed = true;
        mLoader = null;
    }

    int getLastPage() {
        return lastPage;
    }

    void addPage(PageProvider<U> provider) throws Throwable {
        Collection<U> newElements = provider.get();
        list.addAll(newElements);
        lastPage = provider.getCurrentPage();
        completed = provider.isCompleted();
    }
}
