package com.inqbarna.adapters;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ObjectArrays;
import com.inqbarna.adapters.internal.DeferredOperation;
import com.inqbarna.adapters.internal.DeferredOperation.Type;
import com.inqbarna.common.AdapterSyncList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.internal.disposables.DisposableHelper;
import timber.log.Timber;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 15/9/16
 */

public class BasicBindingAdapter<T extends TypeMarker> extends BindingAdapter {
    public static final int INVALID_IDX = -1;

    @NonNull
    public static <T> DiffCallback<T> identityDiff() {
        return new DiffCallback<T>() {
            @Override
            public boolean areSameEntity(T a, T b) {
                return a == b;
            }

            @Override
            public boolean areContentEquals(T a, T b) {
                return a == b;
            }
        };
    }

    private static final UpdatesHandler MAIN_THREAD_HANDLER = new UpdatesHandler(Looper.getMainLooper());
    private static final Executor OFF_THREAD_EXECUTOR = Executors.newSingleThreadExecutor();

    private List<T>                 mData;
    private DiffCallback<? super T> diffCallback;
    private final AtomicReference<Disposable> updateTask;
    private final Executor offThreadExecutor;

    protected BasicBindingAdapter() {
        this(null);
    }

    public BasicBindingAdapter(ItemBinder binder) {
        this(binder, OFF_THREAD_EXECUTOR);
    }

    public BasicBindingAdapter(ItemBinder binder, Executor offThreadExecutor) {
        setItemBinder(binder);
        mData = new ArrayList<>();
        diffCallback = identityDiff();
        updateTask = new AtomicReference<>();
        this.offThreadExecutor = offThreadExecutor;
    }

    public void setItems(List<? extends T> items) {
        for (T anItem : mData) {
            onRemovingElement(anItem);
        }
        mData.clear();
        if (null != items) {
            mData.addAll(items);
        }
        printDbg("[SET ITEMS] Notify dataSetChanged");
        notifyDataSetChanged();
    }

    private final void printDbg(String fmt, Object... args) {
        if (DEBUG) {
            Timber.d(fmt, args);
        }
    }

    public void setDiffCallback(DiffCallback<? super T> diffCallback) {
        this.diffCallback = diffCallback;
    }

    public Single<List<? extends T>> updateItems(@NonNull List<? extends T> items) {
        return new Updater<>(this, items, offThreadExecutor);
    }

    private void onUpdateFinished(@NonNull DiffUtil.DiffResult diffResult, @NonNull List<? extends T> targetList, UpdateLogger logger) {

        // Initialize lists...
        final int originalSize = mData.size();
        List<DeferredOperation<T>> operations = new ArrayList<>(originalSize);
        for (int i = 0; i < originalSize; i++) {
            operations.add(new DeferredOperation<>(Type.Keep, mData.get(i)));
        }

        AtomicBoolean hasMoves = new AtomicBoolean(false);


        diffResult.dispatchUpdatesTo(new ListUpdateCallback() {

            @Override
            public void onInserted(int position, int count) {
                logger.debugMessage("%d Items inserted at %d", count, position);
                notifyItemRangeInserted(addOffsets(position), count);
                while (count > 0) {
                    operations.add(position, new DeferredOperation<>(Type.Placeholder));
                    position++;
                    count--;
                }
            }

            @Override
            public void onRemoved(int position, int count) {
                logger.debugMessage("%d Items removed from pos %d", count, position);
                notifyItemRangeRemoved(addOffsets(position), count);
                while (count > 0) {
                    operations.remove(position);
                    count--;
                }
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                logger.debugMessage("Item moved %d --> %d", fromPosition, toPosition);
                notifyItemMoved(addOffsets(fromPosition), addOffsets(toPosition));
                hasMoves.set(true);
                replaceWith(fromPosition, toPosition, null);
            }

            private void replaceWith(int fromPosition, int toPosition, T item) {
                if (fromPosition != toPosition) {
                    // This is a move
                    final DeferredOperation<T> remove = operations.remove(fromPosition);
                    operations.add(toPosition, remove);
                } else {
                    // This is just a change
                    operations.remove(fromPosition);
                    operations.add(toPosition, new DeferredOperation<>(Type.Keep, item));
                }
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                logger.debugMessage("%d items changed at position %d", count, position);
                notifyItemRangeChanged(addOffsets(position), count, payload);

                if (null != payload) {
                    List<? extends T> data;
                    if (payload instanceof List) {
                        data = (List<? extends T>) payload;
                    } else {
                        data = Collections.singletonList((T) payload);
                    }

                    if (data.size() != count) {
                        throw new IllegalArgumentException("Payload size is " + data.size() + " but count is: " + count);
                    }

                    logger.dbgPrintList(data, "Changed payload", "{PL}");


                    final Iterator<? extends T> replacementIter = data.iterator();
                    while (replacementIter.hasNext()) {
                        replaceWith(position, position, replacementIter.next());
                        position++;
                    }
                }

            }
        });


        // Apply results then!
        mData.clear();

        for (int i = 0, sz = operations.size(); i < sz; i++) {
            final DeferredOperation<T> operation = operations.get(i);
            final T operationData = operation.getData();
            switch (operation.getType()) {
                case Keep:
                    if (null == operationData) {
                        throw new IllegalArgumentException("Operation Data has not been assigned yet!! shouldn't happen here");
                    }
                    mData.add(operationData);
                    break;
                case Placeholder: {
                    T data = operationData;
                    if (data == null) {
                        data = targetList.get(i);
                    }
                    mData.add(i, data);
                }
                break;
            }
        }
    }

    private static class Updater<K extends TypeMarker> extends Single<List<? extends K>> implements Runnable, UpdateLogger {

        private final List<? extends K>      targetList;
        private final BasicBindingAdapter<K> adapter;
        private final List<K> srcList;
        private final DiffCallback<? super K> diffCallback;
        private DiffUtil.DiffResult diffResult;
        private Disposable mDisposable;
        private final Executor executor;

        private static final AtomicInteger DBG_COUNTER = new AtomicInteger(0);

        private final String debugName;

        private final DiffUtil.Callback _Callback = new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return srcList.size();
            }

            @Override
            public int getNewListSize() {
                return targetList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                final K first = Preconditions
                        .checkNotNull(srcList.get(oldItemPosition), "First element is null, comparing positions " + oldItemPosition + " to " + newItemPosition + " on " + this);
                final K second = Preconditions
                        .checkNotNull(targetList.get(newItemPosition), "Second element is null, comparing positions " + oldItemPosition + " to " + newItemPosition + " on " + this);
                return diffCallback.areSameEntity(first, second);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                final K first = Preconditions
                        .checkNotNull(srcList.get(oldItemPosition), "First element is null, comparing contents " + oldItemPosition + " to " + newItemPosition + " on " + this);
                final K second = Preconditions
                        .checkNotNull(targetList.get(newItemPosition), "Second element is null, comparing contents " + oldItemPosition + " to " + newItemPosition + " on " + this);
                return diffCallback.areContentEquals(first, second);
            }

            @Nullable
            @Override
            public Object getChangePayload(int oldItemPosition, int newItemPosition) {
                return targetList.get(newItemPosition);
            }
        };
        private SingleObserver<? super List<? extends K>> mObserver;

        public Updater(@NonNull BasicBindingAdapter<K> adapter, @NonNull List<? extends K> targetList, Executor executor) {
            debugName = "Updater-" + DBG_COUNTER.getAndIncrement();
            this.targetList = ImmutableList.copyOf(Preconditions.checkNotNull(targetList, "target list needs to be not null"));
            this.adapter = Preconditions.checkNotNull(adapter, "adapter may not be null");
            srcList = ImmutableList.copyOf(adapter.mData);
            diffCallback = adapter.diffCallback;
            this.executor = executor;

            debugMessage("Created (%s)", this);
        }

        @Override
        public void debugMessage(String format, Object... args) {
            if (!DEBUG) {
                return;
            }

            Object []newArgs;
            if (args == null) {
                newArgs = new Object[]{debugName};
            } else {
                newArgs = ObjectArrays.concat(debugName, args);
            }

            Timber.d("[%s] " + format, newArgs);
        }

        @Override
        protected void subscribeActual(SingleObserver<? super List<? extends K>> observer) {
            mDisposable = Disposables.empty();
            DisposableHelper.set(adapter.updateTask, mDisposable);
            this.mObserver = observer;
            this.mObserver.onSubscribe(mDisposable);
            startProcess();
        }

        @Override
        public String toString() {
            final MoreObjects.ToStringHelper stringHelper = MoreObjects.toStringHelper(this);
            stringHelper.add("SrcSize", srcList.size())
                        .add("DstSize", targetList.size());

            return stringHelper.toString();
        }

        private void startProcess() {
            if (!mDisposable.isDisposed()) {
                debugMessage("scheduling process");
                executor.execute(this);
            } else {
                debugMessage("Job cancelled before starting it!");
            }
        }

        @Override
        public void run() {
            if (mDisposable.isDisposed()) {
                debugMessage("Aborting before start computation");
                return;
            }
            debugMessage("Computing...");
            diffResult = DiffUtil.calculateDiff(_Callback);
            if (mDisposable.isDisposed()) {
                debugMessage("Aborting right after computation, results discarded");
                return;
            }
            debugMessage("Finished with result: %s", diffResult);
            BasicBindingAdapter.MAIN_THREAD_HANDLER.obtainMessage(UpdatesHandler.RESULTS_FINISHED, this).sendToTarget();
        }

        public void apply() {
            try {
                if (null != diffResult) {
                    debugMessage("================== START Applying results ==============");
                    if (!mDisposable.isDisposed()) {
                        dbgPrintList(adapter.mData, "Items in original list", "-");
                        dbgPrintList(targetList, "Items in desired list", "+");
                        adapter.onUpdateFinished(diffResult, targetList, this);
                        dbgPrintList(adapter.mData, "Items in resulting list", "==>");
                        assert targetList.size() == adapter.mData.size();
                        mObserver.onSuccess(adapter.mData);
                    } else {
                        debugMessage("Skip, target disposed!!");
                    }
                    debugMessage("================== DONE Applying results ==============");
                } else {
                    Timber.e("Some unknown error happened while processing");
                    if (!mDisposable.isDisposed()) {
                        mObserver.onError(new IllegalStateException("No diff could be computed"));

                    }
                }
            } catch (Throwable throwable) {
                Timber.e(throwable, "Error applying changes");
                if (!mDisposable.isDisposed()) {
                    mObserver.onError(throwable);
                }
            }
        }

        @Override
        public void dbgPrintList(List<?> list, String title, String symbol) {
            int originalSize = list.size();
            debugMessage("%s: %d", title, originalSize);
            for (int i = 0; i < originalSize; i++) {
                debugMessage(" %s %d: %s", symbol, i, list.get(i));
            }
        }
    }

    protected int addOffsets(int relativePos) {
        return relativePos;
    }

    protected int removeOffsets(int absPos) {
        return absPos;
    }

    protected void onRemovingElement(T item) {
        /* no-op */
    }

    public void addItems(List<? extends T> items) {
        addItems(INVALID_IDX, items);
    }

    public void addItems(int idx, List<? extends T> items) {
        if (null != items) {
            final int start = mData.size();
            boolean invalid = idx == INVALID_IDX;
            mData.addAll(invalid ? start : idx, items);
            notifyItemRangeInserted(invalid ? start : idx, items.size());
        }
    }

    public void removeItem(T item) {
        if (null != item) {
            final int i = mData.indexOf(item);
            if (i >= 0) {
                onRemovingElement(mData.remove(i));
                notifyItemRemoved(i);
            }
        }
    }

    protected List<T> getItemsInner() {
        return mData;
    }

    public List<T> editableList() {
        return new AdapterSyncList<>(mData, this);
    }

    @Override
    public T getDataAt(int position) {
        return mData.get(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Deprecated
    public static class OldBasicItemBinder<T> implements ItemBinder {
        private final T   mHandler;
        private final int mHandlerVar;
        private final int mModelVar;

        public OldBasicItemBinder(T handler, int handlerVar, int modelVar) {
            mHandler = handler;
            mHandlerVar = handlerVar;
            mModelVar = modelVar;
        }

        @Override
        public void bindVariables(VariableBinding variableBinding, int pos, TypeMarker dataAtPos) {
            variableBinding.bindValue(mModelVar, dataAtPos);
            variableBinding.bindValue(mHandlerVar, mHandler);
        }
    }

    public interface DiffCallback<T> {
        boolean areSameEntity(T a, T b);
        boolean areContentEquals(T a, T b);
    }

    private static class UpdatesHandler extends Handler {
        static final int RESULTS_FINISHED = 1;
        public UpdatesHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RESULTS_FINISHED:
                    Updater updater = (Updater) msg.obj;
                    updater.apply();
                    break;
            }
        }
    }

}
