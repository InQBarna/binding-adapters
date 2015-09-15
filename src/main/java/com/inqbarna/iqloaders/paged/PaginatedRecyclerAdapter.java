package com.inqbarna.iqloaders.paged;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Ricard on 14/9/15.
 */
public abstract class PaginatedRecyclerAdapter<T, VH extends BindableViewHolder<T>> extends RecyclerView.Adapter<VH> {

    private static final int PROGRESS_TYPE = 0;
    private static final int FIRST_USER_TYPE = 1;
    private List<T> items;
    private boolean completed;
    private final Context context;
    private OnLastItemShowedListener listener;

    public interface OnLastItemShowedListener {
        void onLastItemShowed();
    }

    public void setOnLastItemShowedListener(OnLastItemShowedListener listener) {
        this.listener = listener;
    }

    public PaginatedRecyclerAdapter(Context ctxt) {
        this(ctxt, null, false);
    }

    public PaginatedRecyclerAdapter(Context context, List<T> items) {
        this(context, items, false);
    }

    public PaginatedRecyclerAdapter(Context context, List<T> items, boolean completed) {
        this.context = context;
        this.items = items;
        this.completed = completed;
    }

    public void updateData(List<T> items, boolean completed) {
        updateData(items, completed, true);
    }

    protected void updateData(List<T> items, boolean completed, boolean withNotify) {
        this.items = items;
        this.completed = completed;
        if (withNotify)
            notifyDataSetChanged();
    }

    protected boolean isComplete() {
        return completed;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        // switch type
        switch(viewType) {
            case PROGRESS_TYPE:
                return getLastElemHolder(parent);
            default:
                return getHolderForType(parent, viewType);
        }

    }

    protected abstract VH getLastElemHolder(ViewGroup parent);
    protected abstract VH getHolderForType(ViewGroup parent, int viewType);


    @Override
    public void onBindViewHolder(VH holder, int position) {
        int itemViewType = holder.getItemViewType();
        switch (itemViewType) {
            case PROGRESS_TYPE:
                if (listener != null) {
                    listener.onLastItemShowed();
                }
                break;
            default:
                holder.bindTo(getItem(position));
                break;
        }
    }

    public T getItem(int position) {
        return items.get(position);
    }
    protected List<T> getItems() {
        return items;
    }

    @Override
    public int getItemViewType(int position) {
        if (!completed && position == getItemCount()-1) {
            return PROGRESS_TYPE;
        } else {
            return getItemTypeForPos(position);
        }
    }

    protected int getItemTypeForPos(int pos) {
        return FIRST_USER_TYPE;
    }

    @Override
    public int getItemCount() {
        int i = getActualElementCount() + (completed ? 0 : 1);
        return i;
    }

    public int getActualElementCount() {
        if (null == items)
            return 0;
        return items.size();
    }
}
