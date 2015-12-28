package me.jimm.popularmovies2.ui;

import android.util.Log;
import android.widget.AbsListView;

/**
 * Created by jimmaior on 12/15/15.
 * see:
 * older: https://github.com/codepath/android_guides/wiki/Endless-Scrolling-with-AdapterViews-and-RecyclerView
 * replaced byL http://benjii.me/2010/08/endless-scrolling-listview-in-android/
 */
public abstract class EndlessScrollListener implements AbsListView.OnScrollListener {


    private static final String TAG = EndlessScrollListener.class.getSimpleName();

    private int mVisibleThreshold = 5;      // min number of item below scroll position before loading more
    private int mCurrentPage = 1;           // current page number of data loaded
    private int mPreviousTotal = 0;   // item count in dataset after last load
    private boolean mIsLoading = true;   // status while loading


    public EndlessScrollListener() {}

    public EndlessScrollListener(int visibleThreshold) {
        this.mVisibleThreshold = visibleThreshold;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Log.d(TAG,
                "onScroll: firstVisibleItem=" + firstVisibleItem +
                "; visibleItemCount=" + visibleItemCount +
                "; totalItemCount=" + totalItemCount +
                "; mPreviousTotal=" + mPreviousTotal);
       if (mIsLoading) {
           if (totalItemCount > mPreviousTotal) {
               Log.d(TAG, "loading");
               mIsLoading = false;
               mPreviousTotal = totalItemCount;
               mCurrentPage++;
           }
       }
       if (!mIsLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + mVisibleThreshold)) {
           // load the next page
           Log.d(TAG, "onLoadMore");
           mIsLoading = onLoadMore(mCurrentPage, totalItemCount);

       }
    }

    public abstract boolean onLoadMore(int page, int totalItemCount);

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState){
        // do nothing
    }

}
