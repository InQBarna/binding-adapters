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

import java.util.ArrayList;
import java.util.List;

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 23/02/2017
 */

public class BasicListViewAdapter<T extends TypeMarker> extends BindingListviewAdapter {
    private List<T> mData;

    public BasicListViewAdapter(int modelVar) {
        super(new BasicItemBinder(modelVar));
        mData = new ArrayList<>();
    }

    public void setItems(List<? extends T> items) {
        mData.clear();

        if (null != items) {
            mData.addAll(items);
        }
        notifyDataSetChanged();
    }

    @Override
    public T getItem(int position) {
        return mData.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
