package com.inqbarna.adapters;

import androidx.databinding.DataBindingUtil;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

class BindingAdapterDelegate {
    private       ItemBinder                               mItemBinder;
    private       androidx.databinding.DataBindingComponent mOverrideComponent;

    BindingAdapterDelegate() {
    }

    public void setItemBinder(com.inqbarna.adapters.ItemBinder itemBinder) {
        mItemBinder = itemBinder;
    }

    com.inqbarna.adapters.BindingHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
        if (View.NO_ID == viewType) {
            throw new java.lang.IllegalArgumentException("Unexpected layout resource");
        }
        checkBinder();

        final androidx.databinding.ViewDataBinding dataBinding;
        if (null == mOverrideComponent) {
            dataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), viewType, parent, false);
        } else {
            dataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), viewType, parent, false, mOverrideComponent);
        }
        return new com.inqbarna.adapters.BindingHolder(dataBinding);
    }

    void setOverrideComponent(androidx.databinding.DataBindingComponent overrideComponent) {
        mOverrideComponent = overrideComponent;
    }

    void onBindViewHolder(com.inqbarna.adapters.BindingHolder holder, int position, TypeMarker data) {
        checkBinder();
        final com.inqbarna.adapters.SafeVariableBinding variableBinding = holder.lockVars();
        mItemBinder.bindVariables(variableBinding, position, data);
        variableBinding.unlockVars();
    }

    private void checkBinder() {
        if (null == mItemBinder) {
            throw new java.lang.IllegalStateException("ItemBinder not assigned yet!");
        }
    }
}