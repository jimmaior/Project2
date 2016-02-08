package me.jimm.popularmovies2.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;

import me.jimm.popularmovies2.MovieListAdapter;
import me.jimm.popularmovies2.R;
import me.jimm.popularmovies2.Utils;
import me.jimm.popularmovies2.data.MovieContract;
import me.jimm.popularmovies2.models.Movie;
import me.jimm.popularmovies2.models.MovieService;


public class MainFragment extends Fragment implements
        AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor>{


     private static final String TAG = MainFragment.class.getSimpleName();

    // moved to MainActivity private static String mCurrentSortOrder;
    private static int MOVIE_LOADER = 1;
    private int mPosition = GridView.INVALID_POSITION;
    private GridView mGridView;
    private MovieListAdapter mMovieListAdapter;
    private ProgressBar mProgressBar;
    private int mLastPage = 1;  // last page of data loaded, this variable is updated onScrolling
    // moved to Utuls: private static final String SORT_PREFERENCE = "sort_order_settings"; // ListPreference key
	private static final String PERSIST_MOVIE_LIST = "movie_list";
    private static final String PERSIST_SORT_ORDER = "current_sort_order";
    private static final String PERSIST_GRID_LIST_POSITION = "grid_list_position";


    private static final String[] MOVIE_COLUMNS  = {
            // TODO: modify these comments and determine if they are still relevant
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_FAVORITE,
            MovieContract.MovieEntry.COLUMN_BACKDROP_PATH,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_POPULARITY,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_VOTE_COUNT,
    };

    // these indices are tied to MOVIE_COLUMNS.
    // If MOVIE_COLUMNS changes, these must change too
    static final int COL__ID = 0;
    static final int COL_FAVORITE = 1;
    static final int COL_BACKDROP_PATH = 2;
    static final int COL_MOVIE_ID = 3;
    static final int COL_OVERVIEW = 4;
    static final int COL_POPULARITY = 5;
    static final int COL_POSTER_URL = 6;
    static final int COL_RELEASE_DATE = 7;
    static final int COL_TITLE = 8;
    static final int COL_VOTE_AVERAGE = 9;
    static final int COL_VOTE_COUNT = 10;


    /** A Callback for any Activity using this interface for communication */
    public interface Callback {
        void onItemSelected(Uri movieDtlUri, int movieId);
    }

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        // prepare the CursorLoader
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
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
        mMovieListAdapter = new MovieListAdapter(getActivity(), null, 0);
        mGridView = (GridView) v.findViewById(R.id.movies_grid_view);
        mGridView.setAdapter(mMovieListAdapter);
        mGridView.setOnItemClickListener(this);

        // register gridview as a scroll listener
        mGridView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemCount) {
                Log.d(TAG, "onLoadMore");
                mLastPage = page;
                updateMovieData(getActivity());
                return true;
            }
        });

        // restore transient data, if exists
        if (savedInstanceState != null && savedInstanceState.containsKey(PERSIST_GRID_LIST_POSITION)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(PERSIST_GRID_LIST_POSITION);
        }

        return v;
    }

    public void onSortOrderChanged() {
        Log.d(TAG, "onSortOrderChanged");
        // TODO: update the detail fragment too.
        mMovieListAdapter.notifyDataSetChanged();
        mLastPage = 1;
        updateMovieData(getActivity());
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);

    }

    public void showLoading(boolean visible) {
        Log.d(TAG, "showLoading");
        if (visible == true) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        Log.d(TAG, "onSaveInstanceState()");
        if (mPosition != GridView.INVALID_POSITION) {
            bundle.putInt(PERSIST_GRID_LIST_POSITION, mPosition);
        }

        //      bundle.putParcelableArrayList(PERSIST_MOVIE_LIST, mMovies);
        //      bundle.putString(PERSIST_SORT_ORDER, mCurrentSortOrder);

        super.onSaveInstanceState(bundle);
    }

    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Log.d(TAG, "onItemClick - Movie details at position: " + position);

        // id = the id of the cursor record at the clicked item
        Log.d(TAG, "id=" + Long.toString(id));
        Cursor cursor = (Cursor) mGridView.getItemAtPosition(position);
        if (cursor != null) {
            cursor.moveToPosition(position);
            int movieId = cursor.getInt(COL_MOVIE_ID);
            Uri uri = MovieContract.MovieEntry.buildMovieUriByMovieId(movieId);
            ((Callback) getActivity()).onItemSelected(uri, movieId);
        }
        mPosition = position;
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // TODO; wire up the sortOrder to SharedPreferences
        // TODO: build the appropriate URI for the Movie Content
        String sortPreference = Utils.getSortOrderPreference(getActivity());
        String sortOrder =  " favorite DESC, " + sortPreference + " DESC";
        Log.d(TAG, "onCreateLoader(): sortOrder=" + sortOrder);
        Uri movieBySortOrder = MovieContract.MovieEntry.buildMovieListUri();
        return new CursorLoader(getActivity(), movieBySortOrder, MOVIE_COLUMNS, null, null, sortOrder);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished()");
        showLoading(true);
         mMovieListAdapter.swapCursor(data);
        showLoading(false);

        // if we need to scroll to a previous position
        if (mPosition != GridView.INVALID_POSITION) {
            mGridView.smoothScrollToPosition(mPosition);
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset()");
        showLoading(true);
        mMovieListAdapter.swapCursor(null);
        showLoading(false);
    }


    private void updateMovieData(Context context) {
        Log.d(TAG, "updateMovieData");

        final Intent intent = new Intent(Intent.ACTION_SYNC, null, context, MovieService.class);
        intent.putExtra(MovieService.SORT_ORDER_EXTRA_KEY, Utils.getSortOrderPreference(getActivity()));
        intent.putExtra("page", mLastPage);
        intent.putExtra("command", "get_movie_data");
        getActivity().startService(intent);
    }
}