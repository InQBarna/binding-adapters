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

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 23/02/2017
 */

public abstract class BindingListviewAdapter extends BaseAdapter {

    public final BindingAdapterDelegate mBindingAdapterDelegate;

    public BindingListviewAdapter() {
        this(null);
    }

    public BindingListviewAdapter(ItemBinder binder) {
        mBindingAdapterDelegate = new BindingAdapterDelegate();
        if (null != binder) {
            mBindingAdapterDelegate.setItemBinder(binder);
        }
    }

    public abstract TypeMarker getItem(int position);

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        BindingHolder holder = null;
        if (null != convertView) {
            holder = ((BindingHolder) convertView.getTag());
        }
        TypeMarker item = getItem(position);
        if (null == holder) {
            holder = mBindingAdapterDelegate.onCreateViewHolder(parent, item.getItemType());
            holder.itemView.setTag(holder);
        }
        mBindingAdapterDelegate.onBindViewHolder(holder, position, item);
        return holder.itemView;
    }

    @Override
    public final int getItemViewType(int position) {
        // TODO: [DG - 23/02/2017] extend support for mixed types?
        return 0;
    }

    @Override
    public final int getViewTypeCount() {
        // TODO: [DG - 23/02/2017] extend support for mixed types?
        return 1;
    }
}
