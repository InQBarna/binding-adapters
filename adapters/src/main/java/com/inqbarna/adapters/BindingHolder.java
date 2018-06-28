package com.inqbarna.adapters;

import android.arch.lifecycle.LifecycleOwner;
import android.databinding.ViewDataBinding;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;

import java.util.Collection;

/**
 * @author Ricard Aparicio <ricard.aparicio@inqbarna.com>
 * @version 1.0 17/08/16
 */
public class BindingHolder extends RecyclerView.ViewHolder implements GroupIndicator {
    private ViewDataBinding mDataBinding;

    private BasicIndicatorDelegate mIndicatorHolderDelegate;

    BindingHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        mDataBinding = binding;
        mIndicatorHolderDelegate = new BasicIndicatorDelegate();
    }

    @Override
    public GroupAttributes attributes() {
        return mIndicatorHolderDelegate.attributes();
    }

    @Override
    public boolean enabled() {
        return mIndicatorHolderDelegate.enabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        mIndicatorHolderDelegate.setEnabled(enabled);
    }

    public void bindValues(Collection<Pair<Integer, Object>> values) {
        if (null != values) {
            for (Pair<Integer, Object> val : values) {
                bindValue(val.first, val.second, false);
            }
            mDataBinding.executePendingBindings();
        }
    }

    public void setLifecycleOwner(LifecycleOwner owner) {
        mDataBinding.setLifecycleOwner(owner);
    }

    private void bindValue(int varId, Object val, boolean execPending) {
        mDataBinding.setVariable(varId, val);
        if (execPending) {
            mDataBinding.executePendingBindings();
        }
    }

    @Nullable
    public <T extends ViewDataBinding> T getDataBinding(Class<T> clazz) {
        if (clazz.isAssignableFrom(mDataBinding.getClass())) {
            return (T) mDataBinding;
        }
        return null;
    }

    SafeVariableBinding lockVars() {
        return new LockedVarsSet(this);
    }

    private static class LockedVarsSet implements SafeVariableBinding {
        private final BindingHolder mHolder;

        LockedVarsSet(BindingHolder holder) {
            mHolder = holder;
        }

        @Override
        public void bindValue(int variable, Object value) {
            mHolder.bindValue(variable, value, false);
        }

        @Override
        public void unlockVars() {
            mHolder.mDataBinding.executePendingBindings();
        }
    }
}
