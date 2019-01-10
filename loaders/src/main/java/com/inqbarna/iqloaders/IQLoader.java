package com.inqbarna.iqloaders;

import android.content.Context;
import androidx.loader.content.AsyncTaskLoader;

/**
 * IQLoader allows to load every type of data asynchronously.
 * <p/>
 * Loaders may be used across multiple Activities (assuming they aren't bound to
 * the LoaderManager), so NEVER hold a reference to the context directly. Doing
 * so will cause you to leak an entire Activity's context. The superclass
 * constructor will store a reference to the Application Context instead, and
 * can be retrieved with a call to getContext().
 *
 * @param <T> Idea: http://www.androiddesignpatterns.com/2012/08/implementing-loaders.html
 */
public abstract class IQLoader<T> extends AsyncTaskLoader<IQProvider<T>> {

    // We hold a reference to the Loader's data here.
    protected IQProvider<T> mData;
    private boolean registeredDataObserver;

    /**
     * @param ctx - Current context.
     */
    public IQLoader(Context ctx) {
        super(ctx);
    }

    /**
     * This method performs the asynchronous load.
     */
    @Override
    public abstract IQProvider<T> loadInBackground();

    /**
     * This method deliver the results to the registered listener.
     */
    @Override
    public void deliverResult(IQProvider<T> data) {
        if (isReset()) {
            return;
        }

        mData = data;

        loading = false;
        if (isStarted()) {
            // If the Loader is in a started state, deliver the results to the
            // client. The superclass method does this for us.
            super.deliverResult(data);
        }
    }

    // Very Hacky... don't like, but for now in crumbit need to update list contents, but not whole list...
    protected IQProvider<T> abortResult() {
        return mData;
    }

    /**
     * This method implement the Loader's state-dependent behavior.
     */
    @Override
    protected void onStartLoading() {
        if (mData != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(mData);
        }

        if (!registeredDataObserver) {
            // Begin monitoring the underlying data source.
            registeredDataObserver = true;
            onRegisterDataObserver();
        }

        if (takeContentChanged() || mData == null) {
            // When the observer detects a change, it should call onContentChanged()
            // on the Loader, which will cause the next call to takeContentChanged()
            // to return true. If this is ever the case (or if the current data is
            // null), we force a new load.

            loading = true;
            forceLoad();
        }
    }

    private boolean loading;

    /**
     * Request if this particular loader is currently doing background task
     *
     * @return true if any background operation is in progress, false otherwise
     */
    public final boolean isLoading() {
        return loading;
    }

    @Override
    protected void onStopLoading() {
        // The Loader is in a stopped state, so we should attempt to cancel the
        // current load (if there is one).
        loading = false;
        cancelLoad();

        // Note that we leave the observer as is; Loaders in a stopped state
        // should still monitor the data source for changes so that the Loader
        // will know to force a new load if it is ever started again.
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader has been stopped.
        onStopLoading();

        // At this point we can release the resources associated with 'mData'.
        mData = null;

        // The Loader is being reset, so we should stop monitoring for changes.
        if (registeredDataObserver) {
            onUnregisterDataObserver();
            registeredDataObserver = false;
        }
    }

    /**
     * Observer which receives notifications when the data changes.
     */
    protected abstract void onRegisterDataObserver();

    protected abstract void onUnregisterDataObserver();
}
