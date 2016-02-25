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
            LoaderManager.LoaderCallbacks<Cursor> {

    public static final int MOVIE_LOADER = 1;
    public static final int MOVIE_FAVORITES_LOADER = 100;

    private static final String TAG = MainFragment.class.getSimpleName();
    private static final String PERSIST_GRID_LIST_POSITION = "grid_list_position";
    private static final String PERSIST_LOADER_ID = "persist_loader_id";


    private int mPosition;
    private int mCurrentLoader;

    private GridView mGridView;
    private MovieListAdapter mMovieListAdapter;
    private ProgressBar mProgressBar;


    private static final String[] MOVIE_COLUMNS = {
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


    /**
     * A Callback for any Activity using this interface for communication
     */
    public interface Callback {
        void onItemSelected(int movieId);
    }

    public interface OnMoviesLoaderFinished {
        void onMoviesLoaded(int movieId);
    }

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        // prepare the CursorLoader
        getLoaderManager().initLoader(mCurrentLoader, null, this);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setRetainInstance(true);

        if (savedInstanceState != null) {

            if (savedInstanceState.containsKey(PERSIST_GRID_LIST_POSITION)) {
                mPosition = savedInstanceState.getInt(PERSIST_GRID_LIST_POSITION);
            }

            if (savedInstanceState.containsKey(PERSIST_LOADER_ID)) {
                mCurrentLoader = savedInstanceState.getInt(PERSIST_LOADER_ID);
            }
        } else {
            mPosition = GridView.INVALID_POSITION;
            mCurrentLoader = MOVIE_LOADER;
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
        mMovieListAdapter = new MovieListAdapter(getActivity(), null, 0);
        mGridView = (GridView) v.findViewById(R.id.movies_grid_view);
        mGridView.setAdapter(mMovieListAdapter);
        mGridView.setOnItemClickListener(this);

        return v;
    }


    @Override
    public void onResume() {
        Log.d(TAG, "onResume -  mCurrentLoader:" + mCurrentLoader);
        super.onResume();

        // refresh the current view
        LoaderManager loaderManager = getLoaderManager();
        if (loaderManager.getLoader(mCurrentLoader) != null ) {
            loaderManager.restartLoader(mCurrentLoader, null, this);
        } else {
            getLoaderManager().initLoader(mCurrentLoader, null, this);
        }
    }


    public void onSortOrderChanged() {
        Log.d(TAG, "onSortOrderChanged");
        mMovieListAdapter.notifyDataSetChanged();
        mPosition = GridView.INVALID_POSITION;
        updateMovieData(getActivity(), 1);
     }

    public void showLoading(boolean visible) {
        if (visible) {
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

        bundle.putInt(PERSIST_LOADER_ID, mCurrentLoader);


        super.onSaveInstanceState(bundle);
    }

    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Log.d(TAG, "onItemClick - Movie details at position: " + position);

        // id = the id of the cursor record at the clicked item
        Cursor cursor = (Cursor) mGridView.getItemAtPosition(position);
        if (cursor != null) {
            cursor.moveToPosition(position);
            int movieId = cursor.getInt(COL_MOVIE_ID);
            ((Callback) getActivity()).onItemSelected(movieId);
        }
        mPosition = position;
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader - id:" + id);
        String sortPreference = Utils.getSortOrderPreference(getActivity());
        String sortOrder = sortPreference + " DESC";
        Uri uri;
        CursorLoader cursorLoader =null;

        switch (id) {
            case MOVIE_LOADER: {
                uri = MovieContract.MovieEntry.buildMovieListUri();
                cursorLoader = new CursorLoader(getActivity(), uri, MOVIE_COLUMNS, null, null, sortOrder);
                break;
            }
            case MOVIE_FAVORITES_LOADER: {
                String selection = MovieContract.MovieEntry.COLUMN_FAVORITE + " = ?";
                String[] selectionArgs = new String[1];
                selectionArgs[0] = Integer.toString(1);
                uri = MovieContract.MovieEntry.buildFavoriteMovieListUri();
                cursorLoader = new CursorLoader(getActivity(), uri, MOVIE_COLUMNS, selection, selectionArgs, sortOrder);
                break;
            }
            default: {
                Log.e(TAG, "Error: No loader with id '" + id + "'");
                break;
            }
        }

        return cursorLoader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished() - loader param:" + loader);
        if (data.moveToFirst()) {
            showLoading(true);
            mMovieListAdapter.swapCursor(data);
            showLoading(false);

            // if we need to scroll to a previous position
            if (mPosition != GridView.INVALID_POSITION) {
                mGridView.smoothScrollToPosition(mPosition);
            }

            if (mPosition == GridView.INVALID_POSITION) {
                // anytime the grid position is invalid,
                // retrieve the movieId at the top of the list
                mPosition = 0;
                Cursor c = (Cursor) mGridView.getItemAtPosition(mPosition);
                if (c != null) {
                    int movieId = c.getInt(COL_MOVIE_ID);
                    ((OnMoviesLoaderFinished) getActivity()).onMoviesLoaded(movieId);
                    // c.close();
                }
            }
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset() - current loader:" + mCurrentLoader);
        showLoading(true);
        mMovieListAdapter.swapCursor(null);
        showLoading(false);
    }

    void getFavoriteMovies( ) {
        Log.d(TAG, "getFavoriteMovies");
        mPosition = mGridView.INVALID_POSITION;
        mCurrentLoader = MOVIE_FAVORITES_LOADER;

        // Cursor Loader
        LoaderManager loaderManager = getLoaderManager();
        if (loaderManager.getLoader(mCurrentLoader) != null ) {
            loaderManager.restartLoader(mCurrentLoader, null, this);
        } else {
            getLoaderManager().initLoader(mCurrentLoader, null, this);
        }
    }


    void getAllMovies() {
        Log.d(TAG, "getAllMovies");
         mPosition = mGridView.INVALID_POSITION;
        mCurrentLoader = MOVIE_LOADER;

        LoaderManager loaderManager = getLoaderManager();
        if (loaderManager.getLoader(mCurrentLoader) != null ) {
            loaderManager.restartLoader(mCurrentLoader, null, this);
        } else {
            getLoaderManager().initLoader(mCurrentLoader, null, this);
        }
     }

    private void updateMovieData(Context context, int loadPage) {
        Log.d(TAG, "updateMovieData");
        Intent intent = new Intent(Intent.ACTION_SYNC, null, context, MovieService.class);
        intent.putExtra(MovieService.SORT_ORDER_EXTRA_KEY, Utils.getSortOrderPreference(getActivity()));
        intent.putExtra("page", loadPage);
        intent.putExtra("command", "get_movie_data");
        getActivity().startService(intent);

        LoaderManager loaderManager = getLoaderManager();
        if (loaderManager.getLoader(mCurrentLoader) != null ) {
            loaderManager.restartLoader(mCurrentLoader, null, this);
        } else {
            getLoaderManager().initLoader(mCurrentLoader, null, this);
        }
    }
}