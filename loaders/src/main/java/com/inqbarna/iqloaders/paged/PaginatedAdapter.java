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

package com.inqbarna.iqloaders.paged;

import java.util.List;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;

public abstract class PaginatedAdapter<T> extends BaseAdapter {
	public interface OnLastItemShowedListener {
		void onLastItemShowed();
	}

	private final Context context;
	private final LayoutInflater inflater;

	private List<T> items;
	private boolean completed;
	private OnLastItemShowedListener listener;

	public PaginatedAdapter(Context ctxt) {
		this(ctxt, null, false);
	}

	public PaginatedAdapter(Context context, List<T> items) {
		this(context, items, false);
	}
	
	public PaginatedAdapter(Context context, List<T> items, boolean completed) {
		this.context = context;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
	
	public void setOnLastItemShowedListener(OnLastItemShowedListener listener) {
		this.listener = listener;
	}

	public Context getContext() {
		return context;
	}

	@Override
	public int getCount() {
		return getItemCount() + (completed ? 0 : 1);
	}

    public int getItemCount() {
        if (null == items)
            return 0;
        return items.size();
    }

    protected boolean isComplete() {
        return completed;
    }

    protected List<T> getItems() {
        return items;
    }

    protected void replaceItems(List<T> replaceItems) {
        replaceItems(replaceItems, true);
    }

    protected void replaceItems(List<T> replaceItems, boolean withNotify) {
        this.items = replaceItems;
        if (withNotify)
            notifyDataSetChanged();
    }

	@Override
	public T getItem(int position) {
		return items.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (!completed && position == getCount()-1) {
			if (convertView == null) {
				final ProgressBar progressBar = new ProgressBar(context);
				progressBar.setIndeterminate(true);
                progressBar.setClickable(true);

				convertView = progressBar;
			}
			if (listener != null) {
				listener.onLastItemShowed();
			}
		} else {
			convertView = getItemView(position, convertView, parent, inflater);
		}
		return convertView;
	}

	public abstract View getItemView(int position, View convertView, ViewGroup parent, LayoutInflater inflater);

	@Override
	public final int getItemViewType(int position) {
		if (!completed && position == getCount()-1) {
			return 0;
		} else {
			return getItemType(position) + 1;
		}
	}

	@Override
	public final int getViewTypeCount() {
		return getItemTypeCount() + 1;
	}

	public int getItemType(int position) {
		return 0;
	}

	public int getItemTypeCount() {
		return 1;
	}
	
	public void removeItem(Object object){
		items.remove(object);
		notifyDataSetChanged();
	}
}
