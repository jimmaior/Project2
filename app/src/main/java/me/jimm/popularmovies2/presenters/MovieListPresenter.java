package me.jimm.popularmovies2.presenters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;

import me.jimm.popularmovies2.models.MovieServiceReceiver;
import me.jimm.popularmovies2.models.MovieService;
import me.jimm.popularmovies2.R;
import me.jimm.popularmovies2.models.Movie;
import me.jimm.popularmovies2.models.MovieDataManager;
import me.jimm.popularmovies2.views.MovieListView;

/**
 * Created by generaluser on 12/29/15.
 */
public class MovieListPresenter extends BasePresenter implements
        MovieServiceReceiver.Receiver {

    // public Flags
    private static final String SAVED_MOVIE_LIST = "movie_list";
    private static final String SAVED_SORT_ORDER = "current_sort_order";

    private static final String TAG = MovieListPresenter.class.getSimpleName();
    private static final String ACTION_GET_MOVIE_DATA = "me.jimm.popularmovies2.presenters.GET_MOVIE_DATA";
    private static final String SORT_PREFERENCE = "sort_order_settings"; // ListPreference key


    private MovieDataManager mMovieDataManager;
    private MovieListView mMovieListView;
    private Context mContext;
    private Bundle mState;

    private boolean mIsLoadingData = false;

    public String getCurrentSortOrder() {
        return mCurrentSortOrder;
    }

    private String mCurrentSortOrder;

    public int getLastPage() {
        return mLastPage;
    }

    public void setLastPage(int lastPage) {
        mLastPage = lastPage;
    }

    private int mLastPage = 1;  // last page of data loaded, this variable is updated onScrolling
    private ArrayList<Movie> mMovies;


    public MovieListPresenter(Context context) {
        Log.d(TAG, "MovieListPresenter created");
        mContext = context;
        if (mMovies == null) {
            mMovies = new ArrayList<>();
        }
        mCurrentSortOrder = getSortOrderPreference();
        fetchMovieData();
    }

    public void setView(MovieListView view) {
        if (view != null) {
            mMovieListView = view;
        }
    }

   // public void updateView(int page) {
    public void updateView() {
        if (!mCurrentSortOrder.equals(getSortOrderPreference())) {
            mCurrentSortOrder = getSortOrderPreference();
            mMovies.clear();
            mLastPage=1;
            fetchMovieData();
        }
        else {
            mMovieListView.showLoading(true);
           // mLastPage = page;
            fetchMovieData();
            mMovieListView.showMovieList(mMovies);
            mMovieListView.showLoading(false);

        }
    }

    public void save() {
        if (mState == null ) {
            mState = new Bundle();
        }
        mState.putParcelableArrayList(SAVED_MOVIE_LIST, mMovies);
        mState.putString(SAVED_SORT_ORDER, mCurrentSortOrder);
    }

    public Bundle getSavedState() {
        return mState;
    }

    public void loadData() {
        mMovieListView.showMovieList(mMovies);
    }

    public Movie onItemClicked(int position) {
        Log.d(TAG, "onItemClick - Movie details at position: " + position);
        return mMovies.get(position);
    }

    public ArrayList<Movie> getMovies() {
            return mMovies;
    }

    private void fetchMovieData() {
        Log.d(TAG, "fetchMovieData");
        // start the service to interact with the MovieDB API
        // set a reference to the API Request response handler
        MovieServiceReceiver receiver = new MovieServiceReceiver(new Handler());
        receiver.setReceiver(this);

        final Intent intent = new Intent(ACTION_GET_MOVIE_DATA, null, mContext, MovieService.class);
        intent.putExtra("receiver", receiver);
        intent.putExtra("page", mLastPage);
        intent.putExtra("sort_by", mCurrentSortOrder);
        intent.putExtra("command", "get_movie_data");
        mContext.startService(intent);
    }

    public void onReceiveResponse(int resultCode, Bundle resultData) {
        Log.d(TAG, "onReceiveResponse - ResultCode:" + resultCode);
        switch (resultCode) {
            case MovieService.STATUS_RUNNING:
                mMovieListView.showLoading(true);
                break;
            case MovieService.STATUS_FINISHED:
                Log.d(TAG, "MovieDbApiService finished");
                ArrayList<Movie> m = resultData.getParcelableArrayList("results");
                mMovies.addAll(m);
                Log.d(TAG, "mMovies.size()=" + mMovies.size());
                loadData();
                mMovieListView.showLoading(false);
                break;
            case MovieService.STATUS_ERROR:
                Log.e(TAG, resultData.getString(Intent.EXTRA_TEXT));
                Log.e(TAG, "Error occurred while retrieving movie data. " +
                        resultData.get(Intent.EXTRA_TEXT));
                mMovieListView.showEmptyList();
                break;
            default:
                Log.e(TAG, "Undefined return code");
                break;
        }
    }

    public String getCurrentSortOrderPreference() {
        return getSortOrderPreference();
    }

    /**
     * retrieves the user preference for sort order as defined in 'Settings'
     * */
    private String getSortOrderPreference() {
        Log.d(TAG, "getSortOrderPreference");
        // http://stackoverflow.com/questions/2767354/default-value-of-android-preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String defaultValue = mContext.getResources().getString(R.string.pref_sort_default);
        String sortOrder = prefs.getString(SORT_PREFERENCE, defaultValue);
        if (sortOrder.equals("POPULARITY"))  {
            return MovieService.MOVIE_API_PARAM_SORT_BY_POPULARITY;
        } else if (sortOrder.equals("RATING")) {
            return MovieService.MOVIE_API_PARAM_SORT_BY_RATING;
        }
        else {
            Log.e(TAG, "Sort Order undefined: sortOrder='" + sortOrder + "'");
            return null;
        }
    }
}
