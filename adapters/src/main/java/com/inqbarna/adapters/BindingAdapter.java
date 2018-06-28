package com.inqbarna.adapters;

import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.google.common.base.Preconditions;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;

import java.util.Map;

import timber.log.Timber;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 6/9/16
 */
public abstract class BindingAdapter extends RecyclerView.Adapter<BindingHolder> {
    protected static final boolean DEBUG = false;
    private final BindingAdapterDelegate mAdapterDelegate;
    private final RecyclerView.AdapterDataObserver mGroupingResetObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            mRangeMap = null;
            mSpanSizeLookup = null;
            mSpanCount = null;
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            onChanged();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            onChanged();
        }
    };

    private RangeMap<Integer, GroupAttributes> mRangeMap = null;
    private RecyclerView mRecyclerView;
    private GridLayoutManager.SpanSizeLookup mSpanSizeLookup;
    private Integer mSpanCount;
    private LifecycleOwner lifecycleOwner;

    protected BindingAdapter() {
        this(null);
    }

    protected BindingAdapter(ItemBinder binder) {
        mAdapterDelegate = new BindingAdapterDelegate();
        if (null != binder) {
            mAdapterDelegate.setItemBinder(binder);
        }
        registerAdapterDataObserver(mGroupingResetObserver);
    }

    public void setItemBinder(ItemBinder itemBinder) {
        mAdapterDelegate.setItemBinder(itemBinder);
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final BindingHolder bindingHolder = mAdapterDelegate.onCreateViewHolder(parent, viewType);
        if (null != lifecycleOwner) {
            bindingHolder.setLifecycleOwner(lifecycleOwner);
        }
        if (DEBUG) {
            Timber.d("Creating holder [%s] of type: 0x%s", getHolderId(bindingHolder), Integer.toHexString(viewType));
        }
        return bindingHolder;
    }

    public void setLifecycleOwner(LifecycleOwner owner) {
        lifecycleOwner = owner;
    }

    private String getHolderId(@NonNull BindingHolder holder) {
        return "0x" + Integer.toHexString(holder.hashCode());
    }

    public void setOverrideComponent(android.databinding.DataBindingComponent overrideComponent) {
        mAdapterDelegate.setOverrideComponent(overrideComponent);
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        if (DEBUG) {
            Timber.d("Binding holder [%s] at pos: %d [Layout pos: %d]", getHolderId(holder), position, holder.getLayoutPosition());
        }
        TypeMarker dataAt = getDataAt(position);
        mAdapterDelegate.onBindViewHolder(holder, position, dataAt);
        if (dataAt instanceof GroupIndicator) {
            GroupIndicator indicator = (GroupIndicator) dataAt;
            final RangeMap<Integer, GroupAttributes> rangeMap = ensureMap();
            final Map.Entry<Range<Integer>, GroupAttributes> entry = rangeMap.getEntry(position);
            boolean enabled = indicator.enabled();
            GroupAttributes holderAttrs = holder.attributes();
            if (enabled) {
                holder.setEnabled(true);
                holderAttrs.setNonGroupValues(indicator.attributes());
                if (null != entry) {
                    if (isTopData(entry.getKey(), position, getSpanCount(), getSpanSizeLookup())) {
                        GroupAttributes headAttrs = entry.getValue();
                        holderAttrs.setGroupMarginTop(headAttrs.groupMarginTop());
                    } else {
                        holderAttrs.setGroupMarginTop(0);
                    }

                    if (isBottomData(entry.getKey(), position, getSpanCount(), getSpanSizeLookup())) {
                        GroupAttributes headAttrs = entry.getValue();
                        holderAttrs.setGroupMarginBottom(headAttrs.groupMarginBottom());
                    } else {
                        holderAttrs.setGroupMarginBottom(0);
                    }
                } else {
                    holderAttrs.setGroupMarginBottom(0).setGroupMarginTop(0);
                }
            } else {
                holder.setEnabled(false);
                holderAttrs.reset();
            }
        }
        onHolderJustBound(holder);
    }

    protected void onHolderJustBound(BindingHolder holder) {
        /* no-op. Override if needed */
    }

    @Override
    @CallSuper
    public void onViewRecycled(BindingHolder holder) {
        super.onViewRecycled(holder);
        if (DEBUG) {
            Timber.d("Holder recycled [%s] at pos pos: %d [Layout pos: %d]", getHolderId(holder), holder.getAdapterPosition(), holder.getLayoutPosition());
        }
    }

    @Nullable
    private GridLayoutManager.SpanSizeLookup getSpanSizeLookup() {
        if (null == mSpanSizeLookup) {
            initValues();
        }
        return mSpanSizeLookup;
    }

    @Nullable
    private void initValues() {
        if (null == mRecyclerView || !(mRecyclerView.getLayoutManager() instanceof GridLayoutManager)) {
            mSpanCount = 1;
            mSpanSizeLookup = new FixedSpanCount();
        } else {
            final GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
            mSpanSizeLookup = layoutManager.getSpanSizeLookup();
            mSpanCount = layoutManager.getSpanCount();
        }
    }

    private int getSpanCount() {
        if (null == mSpanCount) {
            initValues();
        }
        return mSpanCount;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
        mSpanSizeLookup = null;
        mSpanCount = null;
    }

    private boolean isBottomData(Range<Integer> range, int position, int numColumns, @Nullable GridLayoutManager.SpanSizeLookup spanSizeLookup) {
        Preconditions.checkArgument(range.hasUpperBound());
        Preconditions.checkArgument(range.contains(position));
        int pos = range.upperEndpoint();
        if (range.upperBoundType() == BoundType.OPEN) {
            pos--;
        }

        while (numColumns > 0) {
            if (null != spanSizeLookup) {
                numColumns -= spanSizeLookup.getSpanSize(pos);
            } else {
                numColumns--;
            }
            pos--;
        }

        return pos < position;
    }

    private boolean isTopData(Range<Integer> range, int position, int numColumns, @Nullable GridLayoutManager.SpanSizeLookup spanSizeLookup) {
        Preconditions.checkArgument(range.hasLowerBound());
        Preconditions.checkArgument(range.contains(position));
        int pos = range.lowerEndpoint();
        if (range.lowerBoundType() == BoundType.OPEN) {
            pos++;
        }

        while (numColumns > 0) {
            if (null != spanSizeLookup) {
                numColumns -= spanSizeLookup.getSpanSize(pos);
            } else {
                numColumns--;
            }
            pos++;
        }

        return pos > position;
    }

    @NonNull
    private RangeMap<Integer, GroupAttributes> ensureMap() {
        if (null == mRangeMap) {
            mRangeMap = generateMap();
        }
        return mRangeMap;
    }

    private RangeMap<Integer, GroupAttributes> generateMap() {
        final ImmutableRangeMap.Builder<Integer, GroupAttributes> builder = ImmutableRangeMap.builder();
        for (int i = 0; i < getItemCount(); i++) {
            final TypeMarker dataAt = getDataAt(i);
            if (dataAt instanceof GroupIndicator) {
                GroupIndicator indicator = (GroupIndicator) dataAt;
                final GroupAttributes attributes = indicator.attributes();
                if (indicator.enabled() && attributes.isGroupHead()) {
                    final int groupSize = attributes.groupSize();
                    Preconditions.checkArgument(groupSize >= 1, "Group size is required to be greater or equal to 1, but it's %d", groupSize);
                    builder.put(Range.closedOpen(i, i + groupSize), attributes);
                }
            }
        }
        return builder.build();
    }

    public abstract TypeMarker getDataAt(int position);

    @Override
    public int getItemViewType(int position) {
        return getDataAt(position).getItemType();
    }

    private static class FixedSpanCount extends GridLayoutManager.SpanSizeLookup {
        @Override
        public int getSpanSize(int position) {
            return 1;
        }
    }
}
