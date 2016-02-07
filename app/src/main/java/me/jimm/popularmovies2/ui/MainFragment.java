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
    private GridView mGridView;
    private MovieListAdapter mMovieListAdapter;
    private ProgressBar mProgressBar;
    private int mLastPage = 1;  // last page of data loaded, this variable is updated onScrolling
    // moved to Utuls: private static final String SORT_PREFERENCE = "sort_order_settings"; // ListPreference key
	private static final String PERSIST_MOVIE_LIST = "movie_list";
    private static final String PERSIST_SORT_ORDER = "current_sort_order";


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


    /*callback all Activities using this interface must implement for communication */
    public interface Callback {
        void onItemSelected(Uri movieDtlUri, int movieId);
    }

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

//        mCurrentSortOrder = Utils.getSortOrderPreference(getActivity());
//        updateMovieData(getActivity());


//        if (savedInstanceState != null) {
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
//
//        } else {
//            mMovies = new ArrayList();
//            mCurrentSortOrder = getSortOrderPreference();
//            updateMovieData(getActivity());
//        }
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

        return v;
    }

    public void onSortOrderChanged() {
        // TODO: update the detail fragment too.
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

    public void showMovieList(ArrayList<Movie> movies) {
        Log.d(TAG, "showMovieList: movies size=" + movies.size());
        // show the data
        mMovieListAdapter.notifyDataSetChanged();
    }

    public void showEmptyList() {
        Log.d(TAG, "showEmptyList");
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        Log.d(TAG, "onSaveInstanceState()");
  //      bundle.putParcelableArrayList(PERSIST_MOVIE_LIST, mMovies);
  //      bundle.putString(PERSIST_SORT_ORDER, mCurrentSortOrder);
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
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // TODO; wire up the sortOrder from SharedPreferences
        // TODO: build the appropriate URI for the Movie Content
        String sortPreference = Utils.getSortOrderPreference(getActivity());
        String sortOrder =  " favorite DESC, " + sortPreference + " DESC";
        Log.d(TAG, "onCreateLoader(): sortOrder=" + sortOrder );
        Uri movieBySortOrder = MovieContract.MovieEntry.buildMovieListUri();
        return new CursorLoader(getActivity(), movieBySortOrder, MOVIE_COLUMNS, null, null, sortOrder);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished()");
        showLoading(true);
         mMovieListAdapter.swapCursor(data);
        showLoading(false);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset()");
        showLoading(true);
        mMovieListAdapter.swapCursor(null);
        showLoading(false);
    }


    private void updateMovieData(Context context) {
        Log.d(TAG, "updateMovieData");
        String currentSortOrder = Utils.getSortOrderPreference(getActivity());
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, context, MovieService.class);
        if (currentSortOrder != null) {
            intent.putExtra("sort_by", currentSortOrder);
        } else {
            intent.putExtra("sort_by", Utils.getSortOrderPreference(getActivity()));
        }
        intent.putExtra("page", mLastPage);
        intent.putExtra("command", "get_movie_data");
        getActivity().startService(intent);
    }

//    /**
//     * retrieves the user preference for sort order as defined in 'Settings'
//     * */
//    private String getSortOrderPreference() {
//        Log.d(TAG, "getSortOrderPreference");
//        // http://stackoverflow.com/questions/2767354/default-value-of-android-preference
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        String defaultValue = getActivity().getResources().getString(R.string.pref_sort_default);
//        String sortOrder = prefs.getString(SORT_PREFERENCE, defaultValue);
//        if (sortOrder.equals("POPULARITY")) {
//            return MovieContract.MovieEntry.COLUMN_POPULARITY;
//        } else if (sortOrder.equals("RATING")) {
//            return MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE;
//        } else {
//            Log.e(TAG, "Sort Order undefined: sortOrder='" + sortOrder + "'");
//            return null;
//        }
//    }

    private void clearDatabase() {
        Log.d(TAG, "clearDatabase()");
        // clear the content provider
        getActivity().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI, null, null);

    }
}