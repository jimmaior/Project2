package me.jimm.popularmovies2.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.Collection;

import com.squareup.picasso.Picasso;

import me.jimm.popularmovies2.MovieListAdapter;
import me.jimm.popularmovies2.R;
import me.jimm.popularmovies2.data.MovieContract;
import me.jimm.popularmovies2.models.Movie;
import me.jimm.popularmovies2.models.MovieService;
import me.jimm.popularmovies2.models.MovieServiceReceiver;


public class MainFragment extends Fragment implements
        MovieServiceReceiver.Receiver,
        AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor>{

    public MovieServiceReceiver mReceiver;

     private static final String TAG = MainFragment.class.getSimpleName();

    private String mCurrentSortOrder;
    private static int MOVIE_LOADER = 1;
    private GridView mGridView;
    private MovieListAdapter mMovieListAdapter;
    private MovieDbAdapter  mMovieDbAdapter;
    private ArrayList<Movie> mMovies;
    private ProgressBar mProgressBar;
    private int mLastPage = 1;  // last page of data loaded, this variable is updated onScrolling
    private static final String SORT_PREFERENCE = "sort_order_settings"; // ListPreference key
	private static final String PERSIST_MOVIE_LIST = "movie_list";
    private static final String PERSIST_SORT_ORDER = "current_sort_order";


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
            if (savedInstanceState.containsKey(PERSIST_MOVIE_LIST)) {
                mMovies = savedInstanceState.getParcelableArrayList(PERSIST_MOVIE_LIST);
            } else {
               mMovies = new ArrayList();
            }
            if (savedInstanceState.containsKey(PERSIST_SORT_ORDER)) {
                mCurrentSortOrder = savedInstanceState.getString(PERSIST_SORT_ORDER);
            } else {
                mCurrentSortOrder = getSortOrderPreference();
            }

        } else {
            mMovies = new ArrayList();
            mCurrentSortOrder = getSortOrderPreference();
            fetchMovieData(getActivity());
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
        mMovieDbAdapter = new MovieDbAdapter(getActivity());
        mGridView = (GridView) v.findViewById(R.id.movies_grid_view);
        //mGridView.setAdapter(mMovieListAdapter);
        mGridView.setAdapter(mMovieDbAdapter);
        mGridView.setOnItemClickListener(this);


        // register gridview as a scroll listener
        mGridView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemCount) {
                Log.d(TAG, "onLoadMore");
                mLastPage = page;
                fetchMovieData(getActivity());
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
        mMovieDbAdapter.notifyDataSetChanged();
    }

    public void showEmptyList() {
        Log.d(TAG, "showEmptyList");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mReceiver != null) {
            mReceiver.setReceiver(null);
        }
        Log.d(TAG, "onPause" +
                "; mCurrentSortOrder=" + mCurrentSortOrder +
                "; getSortOrderPreference()=" + getSortOrderPreference());
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume" +
                "; mCurrentSortOrder=" + mCurrentSortOrder +
                "; getSortOrderPreference()=" + getSortOrderPreference());

        if (mReceiver != null ) {
            mReceiver.setReceiver(this);    // reset the receiver
        }
        // if the scroll preference has changed when resuming, then repopulate the movies list
        if (!mCurrentSortOrder.equals(getSortOrderPreference())) {
            mCurrentSortOrder = getSortOrderPreference();
            mMovies.clear();
            mLastPage = 1;
            fetchMovieData(getActivity());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        Log.d(TAG, "onSaveInstanceState()");
        bundle.putParcelableArrayList(PERSIST_MOVIE_LIST, mMovies);
        bundle.putString(PERSIST_SORT_ORDER, mCurrentSortOrder);
    }

    // TODO what is this id parameter?
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Log.d(TAG, "onItemClick - Movie details at position: " + position);

        Movie movie = mMovies.get(position);
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra("movie", movie);
        startActivity(intent);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // TODO; wire up the sortOrder from SharedPreferences
        // TODO: build the appropriate URI for the Movie Content
        String sortOrder = "";
        Uri movieBySortOrder = MovieContract.MovieEntry.buildMovieListUri();
        return new CursorLoader(getActivity(), movieBySortOrder, MOVIE_COLUMNS, null, null, sortOrder );
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
         mMovieListAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieListAdapter.swapCursor(null);
    }

    public void onReceiveResponse(int resultCode, Bundle resultData) {
        Log.d(TAG, "onReceiveResponse - ResultCode:" + resultCode);
        switch (resultCode) {
            case MovieService.STATUS_RUNNING:
                showLoading(true);
                break;
            case MovieService.STATUS_FINISHED:
                Log.d(TAG, "MovieDbApiService finished");
                ArrayList<Movie> m = resultData.getParcelableArrayList("results");
                mMovies.addAll(m);
                Log.d(TAG, "mMovies.size()=" + mMovies.size());
                showMovieList(mMovies);
                showLoading(false);
                break;
            case MovieService.STATUS_ERROR:
                Log.e(TAG, resultData.getString(Intent.EXTRA_TEXT));
                Log.e(TAG, "Error occurred while retrieving movie data. " +
                        resultData.get(Intent.EXTRA_TEXT));
                showEmptyList();
                break;
            default:
                Log.e(TAG, "Undefined return code");
                break;
        }
    }

    private void fetchMovieData(Context context) {
        Log.d(TAG, "fetchMovieData");
        // start the service to interact with the MovieDB API
        // set a reference to the API Request response handler
        mReceiver = new MovieServiceReceiver(new Handler());
        mReceiver.setReceiver(this);

        final Intent intent = new Intent(Intent.ACTION_SYNC, null, context, MovieService.class);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("page", mLastPage);
        intent.putExtra("sort_by", mCurrentSortOrder);
        intent.putExtra("command", "get_movie_data");
        getActivity().startService(intent);
    }

    /**
     * retrieves the user preference for sort order as defined in 'Settings'
     * */
    private String getSortOrderPreference() {
        Log.d(TAG, "getSortOrderPreference");
        // http://stackoverflow.com/questions/2767354/default-value-of-android-preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String defaultValue = getActivity().getResources().getString(R.string.pref_sort_default);
        String sortOrder = prefs.getString(SORT_PREFERENCE, defaultValue);
        if (sortOrder.equals("POPULARITY")) {
            return MovieService.MOVIE_API_PARAM_SORT_BY_POPULARITY;
        } else if (sortOrder.equals("RATING")) {
            return MovieService.MOVIE_API_PARAM_SORT_BY_RATING;
        } else {
            Log.e(TAG, "Sort Order undefined: sortOrder='" + sortOrder + "'");
            return null;
        }
    }

    private class MovieDbAdapter extends BaseAdapter{

        private final String TAG = MovieDbAdapter.class.getSimpleName();
        private Context mContext;

        public MovieDbAdapter(Context c) {
            Log.d(TAG, "MovieDbAdapter Constructor");
            mContext = c;
        }

        public int getCount() {
            return mMovies.size();
        }

        public Object getItem(int position) {
            if (mMovies != null) {
                return mMovies.get(position);
            } else {
                return null;
            }
        }

        public long getItemId(int position) {
            return mMovies.get(position).getMovieId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Movie movie = (Movie) getItem(position);

            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setAdjustViewBounds(true);
                imageView.setPadding(4, 4, 4, 4);
            } else {
                imageView = (ImageView) convertView;
            }

            // using a placeholder really make the app run much smoother
            Picasso.with(mContext)
                    .load(movie.getPosterPath())
                    .placeholder(R.drawable.ic_photo_white_48dp)
                    .into(imageView);

            return imageView;
        }

        public void clearAll() {
            mMovies.clear();
        }

        public void add(Collection<Movie> movies) {
            mMovies.addAll(movies);
            notifyDataSetChanged();
        }


    }
}