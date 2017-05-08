package com.inqbarna.adapters;

import android.support.v7.widget.RecyclerView;

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
        return updateGroup(items, groupHead, groupHead.groupSize(), true, groupHead.color());
    }
    public static Result disableGroup(List<? extends GroupIndicator> items, GroupHead groupHead) {
        return updateGroup(items, groupHead, groupHead.groupSize(), false, groupHead.color());
    }

    public static Result updateGroup(List<? extends GroupIndicator> items, GroupIndicator target, int groupSize, boolean enable, int groupColor) {
        int indexOf = items.indexOf(target);
        if (indexOf < 0) {
            Timber.w("Trying to enable group that is not within given items. Won't do anything");
            return EMPTY_RESULT;
        }

        int count = groupSize;

        ListIterator<? extends GroupIndicator> iterator = items.listIterator(indexOf);
        while (iterator.hasNext() && count > 0) {
            GroupIndicator indicator = iterator.next();
            indicator.setEnabled(enable);
            if (Integer.MIN_VALUE != groupColor) {
                indicator.setColor(groupColor);
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