package com.inqbarna.iqloaders.paged;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Ricard on 14/9/15.
 */
public abstract class BindableViewHolder<T> extends RecyclerView.ViewHolder
{
    public BindableViewHolder(View itemView) {
        super(itemView);
    }
    public abstract void bindTo(T data);


}
