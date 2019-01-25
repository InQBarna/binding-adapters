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

package com.inqbarna.adapters;

import androidx.lifecycle.LifecycleOwner;
import androidx.databinding.ViewDataBinding;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

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
