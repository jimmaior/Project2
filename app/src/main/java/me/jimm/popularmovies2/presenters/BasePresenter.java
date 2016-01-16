package me.jimm.popularmovies2.presenters;

import java.lang.ref.WeakReference;

/**
 * Created by generaluser on 12/31/15.
 */
public abstract class BasePresenter<M, V> {
    protected M mModel;
    protected V mView;

    public void setModel(M model) {
        resetState();
        this.mModel = model;
        if (setupDone()) {
            updateView();
        }
    }

    protected abstract void updateView();

    protected void resetState() {}

    public void bindView(V view) {
        this.mView = view;
        if (setupDone()) {
            updateView();
        }
    }

    public void unbindView() {
        this.mView = null;
    }

    protected V view() {
        if (mView == null) {
            return null;
        } else {
            return mView;
        }
    }

    protected boolean setupDone() {
        return view() != null && mModel != null;
    }

}
