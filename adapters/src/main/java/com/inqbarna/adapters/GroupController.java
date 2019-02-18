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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.ListIterator;

import timber.log.Timber;

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 08/05/2017
 */
public class GroupController {

    private static final Result EMPTY_RESULT = new ResultImpl();

    public GroupController() {
    }

    public static Result enableGroup(List<? extends GroupIndicator> items, GroupHead groupHead) {
        return updateGroupWithColor(items, groupHead, groupHead.groupSize(), true, groupHead.attributes().color());
    }
    public static Result disableGroup(List<? extends GroupIndicator> items, GroupHead groupHead) {
        return updateGroupWithColor(items, groupHead, groupHead.groupSize(), false, groupHead.attributes().color());
    }

    public static Result toggleGroup(List<? extends GroupIndicator> items, GroupHead groupHead) {
        return updateGroupWithColor(items, groupHead, groupHead.groupSize(), !groupHead.enabled(), groupHead.attributes().color());
    }

    public static Result updateGroupWithColor(List<? extends GroupIndicator> items, GroupIndicator target, int groupSize, boolean enable, int groupColor) {

        GroupAttributes attributes = new GroupAttributes();
        attributes.setColor(groupColor);

        return updateGroupWithAttributes(items, target, groupSize, enable, attributes);
    }

    @NonNull
    public static Result updateGroupWithAttributes(
            List<? extends GroupIndicator> items, GroupIndicator target, int groupSize, boolean enable, @NonNull GroupAttributes attributes) {
        int indexOf = items.indexOf(target);
        if (indexOf < 0) {
            Timber.w("Trying to enable group that is not within given items. Won't do anything");
            return EMPTY_RESULT;
        }

        int count = groupSize;

        ListIterator<? extends GroupIndicator> iterator = items.listIterator(indexOf);
        GroupAttributes headAttr = target.attributes();
        headAttr.setGroupHead(true).setGroupSize(groupSize);
        while (iterator.hasNext() && count > 0) {
            GroupIndicator indicator = iterator.next();
            indicator.setEnabled(enable);
            final GroupAttributes attr = indicator.attributes();
            if (!attr.isGroupHead()) {
                if (enable) {
                    attr.setNonGroupValues(headAttr);
                } else {
                    attr.reset();
                }
            }
            count--;
        }


        if (count > 0) {
            Timber.w("Requested group change for %d items, but there were %d items left unchanged because list is not big enough", groupSize, count);
        }

        return new ResultImpl(indexOf, groupSize - count);
    }

    public interface Result {
        void notifyOn(RecyclerView.Adapter adapter);
    }

    private static class ResultImpl implements Result {
        private final int mStart;
        private final int mCount;

        private ResultImpl() {
            this(-1, 0);
        }

        private ResultImpl(int start, int count) {
            mStart = start;
            mCount = count;
        }

        @Override
        public void notifyOn(RecyclerView.Adapter adapter) {
            if (mStart >= 0) {
                if (mStart + mCount > adapter.getItemCount()) {
                    Timber.w(
                            "Tried to apply notification on adapter with %d items, with start = %d and span = %d. That would be exceeding limits",
                            adapter.getItemCount(),
                            mStart,
                            mCount);
                    return;
                }
                adapter.notifyItemRangeChanged(mStart, mCount);
            }
        }
    }

}
