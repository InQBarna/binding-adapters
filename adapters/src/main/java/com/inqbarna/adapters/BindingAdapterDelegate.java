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