package me.jimm.popularmovies2.ui;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;

import me.jimm.popularmovies2.MovieListAdapter;
import me.jimm.popularmovies2.PresenterManager;
import me.jimm.popularmovies2.R;
import me.jimm.popularmovies2.data.MovieContract;
import me.jimm.popularmovies2.models.Movie;
import me.jimm.popularmovies2.presenters.MovieListPresenter;
import me.jimm.popularmovies2.views.MovieListView;


public class MainFragment extends Fragment implements
        MovieListView,
        AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor>{

    private MovieListPresenter mMovieListPresenter;
   // private PresenterManager mPresenterManager;
    private static final String TAG = MainFragment.class.getSimpleName();

    private static int MOVIE_LOADER = 1;
    private GridView mGridView;
    private MovieListAdapter mMovieListAdapter;
    private ProgressBar mProgressBar;

    private static final String[] MOVIE_COLUMNS  = {
            // TODO: modify these comments and determine if they are still relevant
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_FAVORITE,
            MovieContract.MovieEntry.COLUMN_FK_REVIEW_KEY,
            MovieContract.MovieEntry.COLUMN_FK_VIDEO_KEY,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_POSTER_URL,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_USER_RATING,
    };

    // these indices are tied to FORECAST_COLUMNS.
    // If FORECAST_COLUMNS changes, these must change too
    static final int COL__ID = 0;
    static final int COL_FAVORITE = 1;
    static final int COL_FK_REVIEW_KEY = 2;
    static final int COL_FK_VIDEO_KEY = 3;
    static final int COL_MOVIE_ID = 4;
    static final int COL_OVERVIEW = 5;
    static final int COL_POSTER_URL = 6;
    static final int COL_RELEASE_DATE = 7;
    static final int COL_TITLE = 8;
    static final int COL_USER_RATING = 9;



    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // prepare the CursorLoader
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        if (savedInstanceState != null) {


            mMovieListPresenter = PresenterManager.getInstance().restorePresenter(savedInstanceState);
//            if (savedInstanceState.containsKey(PERSIST_MOVIE_LIST)) {
//                mMovies = savedInstanceState.getParcelableArrayList(PERSIST_MOVIE_LIST);
//            } else {
//               mMovies = new ArrayList();
//            }
//            if (savedInstanceState.containsKey(PERSIST_SORT_ORDER)) {
//                mCurrentSortOrder = savedInstanceState.getString(PERSIST_SORT_ORDER);
//            } else {
//                mCurrentSortOrder = getSortOrderPreference();
//            }

        } else {

            mMovieListPresenter = new MovieListPresenter(getContext());
            mMovieListPresenter.setView(this);
         }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View v =  inflater.inflate(R.layout.fragment_main, container, false);

        // progressbar
        mProgressBar  = (ProgressBar) v.findViewById(R.id.progress_bar);
        mProgressBar.setIndeterminate(true);


        // grid
        mMovieListAdapter = new MovieListAdapter(getActivity(), mMovieListPresenter.getMovies());
        mGridView = (GridView) v.findViewById(R.id.movies_grid_view);
        mGridView.setAdapter(mMovieListAdapter);
        mGridView.setOnItemClickListener(this);


        // register gridview as a scroll listener
        mGridView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemCount) {
                Log.d(TAG, "onLoadMore");
                mMovieListPresenter.setLastPage(page);
                mMovieListPresenter.updateView();
                return true;
            }
        });

        return v;
    }

    public void showLoading(boolean visible) {
        Log.d(TAG, "showLoading");
        if (visible == true) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        else {mProgressBar.setVisibility(View.GONE);}
    }

    public void showMovieList(ArrayList<Movie> movies) {
        Log.d(TAG, "showMovieList: movies size=" + movies.size());
        // show the data
        mMovieListAdapter.notifyDataSetChanged();
    }

    public void showEmptyList() {
        Log.d(TAG, "showEmptyList");
    }

    @Override
    public void onPause() {
        super.onPause();
        mMovieListPresenter.unbindView();
        Log.d(TAG, "onPause" +
                "; mCurrentSortOrder=" + mMovieListPresenter.getCurrentSortOrder() +
                "; getSortOrderPreference()=" + mMovieListPresenter.getCurrentSortOrderPreference());

    }

    @Override
    public void onResume() {
        super.onResume();
        mMovieListPresenter.bindView(this);

        Log.d(TAG, "onResume" +
                "; mCurrentSortOrder=" + mMovieListPresenter.getCurrentSortOrder() +
                "; getSortOrderPreference()=" + mMovieListPresenter.getCurrentSortOrderPreference());
        mMovieListPresenter.updateView();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        Log.d(TAG, "onSaveInstanceState()");
        PresenterManager.getInstance().savePresenter(mMovieListPresenter, bundle);
    }

    // TODO what is this id parameter?
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Log.d(TAG, "onItemClick - Movie details at position: " + position);

        Movie movie = mMovieListPresenter.onItemClicked(position);
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra("movie", movie);
        startActivity(intent);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // mMovieListAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // mMovieListAdapter.swapCursor(null);
    }

}