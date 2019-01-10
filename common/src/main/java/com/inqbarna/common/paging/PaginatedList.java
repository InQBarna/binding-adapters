package com.inqbarna.common.paging;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collection;
import java.util.List;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 16/9/16
 */
public interface PaginatedList<U> {
    U get(int location);
    int size();
    boolean hasMorePages();
    void requestNext();
    void appendPageItems(Collection<? extends U> items, boolean last);
    void clear(@Nullable PaginatedAdapterDelegate.ItemRemovedCallback<U> itemRemovedCallback);
    List<U> editableList(@Nullable RecyclerView.Adapter callbackAdapter);
}
