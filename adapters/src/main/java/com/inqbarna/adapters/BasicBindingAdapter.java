package com.inqbarna.adapters;

import android.databinding.ViewDataBinding;

import com.inqbarna.adapters.BindingAdapter;
import com.inqbarna.adapters.TypeMarker;
import com.inqbarna.adapters.VariableBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 15/9/16
 */

public class BasicBindingAdapter<T extends TypeMarker> extends BindingAdapter<T> {
    private List<T> mData;

    public BasicBindingAdapter(ItemBinder binder) {
        setItemBinder(binder);
        mData = new ArrayList<>();
    }

    public void setItems(List<? extends T> items) {
        mData.clear();
        if (null != items) {
            mData.addAll(items);
        }
        notifyDataSetChanged();
    }

    public void addItems(List<? extends T> items) {
        if (null != items) {
            final int start = mData.size();
            mData.addAll(items);
            notifyItemRangeInserted(start, items.size());
        }
    }

    @Override
    protected T getDataAt(int position) {
        return mData.get(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * @author David García <david.garcia@inqbarna.com>
     * @version 1.0 16/9/16
     */
    public static class BasicItemBinder<T> implements ItemBinder {
        private final T   mTagHandler;
        private final int mHandlers;
        private final int mModel;

        public BasicItemBinder(T tagHandler, int handlers, int model) {
            mTagHandler = tagHandler;
            mHandlers = handlers;
            mModel = model;
        }

        @Override
        public void setHandlers(ViewDataBinding dataBinding, int viewType) {
            dataBinding.setVariable(mHandlers, mTagHandler);
        }

        @Override
        public void bindVariables(VariableBinding variableBinding, int pos, TypeMarker dataAtPos) {
            variableBinding.bindValue(mModel, dataAtPos);
        }
    }
}
