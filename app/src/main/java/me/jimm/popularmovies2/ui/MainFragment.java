package me.jimm.popularmovies2.ui;

import android.content.*;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.Collection;

import com.squareup.picasso.Picasso;
import me.jimm.popularmovies2.MovieDbApiResponseReceiver;
import me.jimm.popularmovies2.MovieDbApiService;
import me.jimm.popularmovies2.R;
import me.jimm.popularmovies2.model.Movie;


public class MainFragment extends Fragment implements
        AdapterView.OnItemClickListener,
        MovieDbApiResponseReceiver.Receiver {

    public MovieDbApiResponseReceiver mReceiver;

    private static final String TAG = MainFragment.class.getSimpleName();
    private static final String SORT_PREFERENCE = "sort_order_settings"; // ListPreference key
    private static final String PERSIST_MOVIE_LIST = "movie_list";
    private static final String PERSIST_SORT_ORDER = "current_sort_order";

    private GridView mGridView;
    private MovieDbAdapter mMovieDbAdapter;
    private ArrayList<Movie> mMovies;
    private ProgressBar mProgressBar;

    private String mCurrentSortOrder;
    private int mLastPage = 1;  // last page of data loaded, this variable is onScrolling


    public MainFragment() {
        // Required empty public constructor
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

        mMovieDbAdapter = new MovieDbAdapter(getActivity());
        mGridView = (GridView) v.findViewById(R.id.movies_grid_view);
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

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause" +
                "; mCurrentSortOrder=" + mCurrentSortOrder +
                "; getSortOrderPreference()=" + getSortOrderPreference());
        if (mReceiver != null) {
            mReceiver.setReceiver(null);
        }
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


    public void onReceiveResponse(int resultCode, Bundle resultData) {
        Log.d(TAG, "onReceiveResponse - ResultCode:" + resultCode);
        switch (resultCode) {
            case MovieDbApiService.STATUS_RUNNING:
                mProgressBar.setVisibility(View.VISIBLE);
                break;
            case MovieDbApiService.STATUS_FINISHED:
                Log.d(TAG, "MovieDbApiService finished");
                ArrayList<Movie> m = resultData.getParcelableArrayList("results");
                mMovies.addAll(m);
                Log.d(TAG, "mMovies.size()=" + mMovies.size());
                mMovieDbAdapter.notifyDataSetChanged();
                mProgressBar.setVisibility(View.GONE);
                break;
            case MovieDbApiService.STATUS_ERROR:
                Log.e(TAG, resultData.getString(Intent.EXTRA_TEXT));
                Toast.makeText(getActivity(), "Error occurred while retrieving movie data." +
                        resultData.get(Intent.EXTRA_TEXT), Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(getActivity(), "Undefined return code", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        Log.d(TAG, "onSaveInstanceState()");
        bundle.putParcelableArrayList(PERSIST_MOVIE_LIST, mMovies);
        bundle.putString(PERSIST_SORT_ORDER, mCurrentSortOrder);

    }



    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Log.d(TAG, "onItemClick - Movie details at position: " + position);
        Movie m = mMovies.get(position);
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra("movie", m);
        startActivity(intent);
    }

    // Private Members /////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    private void fetchMovieData(Context context) {
        Log.d(TAG, "fetchMovieData");
        // start the service to interact with the MovieDB API
        // set a reference to the API Request response handler
        mReceiver = new MovieDbApiResponseReceiver(new Handler());
        mReceiver.setReceiver(this);

        final Intent intent = new Intent(Intent.ACTION_SYNC, null, context, MovieDbApiService.class);
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
        if (sortOrder.equals("POPULARITY"))  {
            return MovieDbApiService.MOVIE_API_PARAM_SORT_BY_POPULARITY;
        } else if (sortOrder.equals("RATING")) {
            return MovieDbApiService.MOVIE_API_PARAM_SORT_BY_RATING;
        }
        else {
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