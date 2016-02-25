package me.jimm.popularmovies2.ui;


import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import me.jimm.popularmovies2.R;
import me.jimm.popularmovies2.Utils;
import me.jimm.popularmovies2.data.MovieContract;
import me.jimm.popularmovies2.models.MovieService;

public class MainActivity extends AppCompatActivity implements
        MainFragment.Callback, DetailFragment.Callback, MainFragment.OnMoviesLoaderFinished {

    /**
     * Member Variables
     */

    private final String TAG = MainActivity.class.getSimpleName();
    private static final String TAG_DETAIL_FRAGMENT = "TAG_DETAIL_FRAGMENT";
    private static final int DETAIL_REQUEST_CODE = 100;
    private static final String PERSIST_SORT_ORDER = "current_sort_order";
    private boolean mTwoPane;
    private static String mCurrentSortOrder;
    private int mMovieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        // toolbar support
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            init();
        } else {
            mCurrentSortOrder = savedInstanceState.getString(PERSIST_SORT_ORDER);
        }

        // check if the activity is using a layout which will contain two fragments
        if (findViewById(R.id.fragment_detail_container) != null) {
            // this is a two page layout
            mTwoPane = true;
            // therefore, we need to add the detail fragment to the fragment_detail_container
            if (savedInstanceState == null) {

                // build and init detail fragment
                DetailFragment fragment = new DetailFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_detail_container, fragment, TAG_DETAIL_FRAGMENT)
                        .commit();
            }
        }
        else {
            mTwoPane = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        Log.d(TAG, "onSaveInstanceState()");
        bundle.putString(PERSIST_SORT_ORDER, mCurrentSortOrder);

        super.onSaveInstanceState(bundle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCurrentSortOrder == null)  {
            mCurrentSortOrder = Utils.getSortOrderPreference(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume" +
                "; mCurrentSortOrder=" + mCurrentSortOrder +
                "; getSortOrderPreference()=" + Utils.getSortOrderPreference(this));

        if (mCurrentSortOrder != null && !mCurrentSortOrder.equals(Utils.getSortOrderPreference(this))) {
            // update the current sort order and refresh the movie data
            mCurrentSortOrder = Utils.getSortOrderPreference(this);
            MainFragment mainFragment = (MainFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_main);
            if (mainFragment != null) {
                mainFragment.onSortOrderChanged();
            }
        }
    }

    @Override
    public void onItemSelected(int movieId) {
        Log.d(TAG, "onItemSelected - populate movie detail cache with data for movieId:" + movieId);
        Intent movieDetailIntent = new Intent(this, MovieService.class);
        PendingIntent pendingMovieDetails = createPendingResult(DETAIL_REQUEST_CODE, movieDetailIntent, 0);
        movieDetailIntent.putExtra("movie_id", movieId);
        movieDetailIntent.putExtra("command", "get_movie_detail_data");
        movieDetailIntent.putExtra(MovieService.PENDING_RESULT, pendingMovieDetails);
        startService(movieDetailIntent);
    }

    @Override
    public void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        Log.d(TAG, "onActivityResult");

        if (req == DETAIL_REQUEST_CODE && res == MovieService.RESULT_CODE) {
            //int movieId = data.getIntExtra("movie_id", 0);
            mMovieId = data.getIntExtra("movie_id", 0);
            Log.d(TAG, "movie_id:" + mMovieId);

            Uri uri = MovieContract.MovieEntry.buildMovieUriByMovieId(mMovieId);

            if (mTwoPane) {
                // show movie details in this activity, by adding/replacing the detail fragment
				Log.d(TAG, "Show movie details for two pane");
                Bundle args = new Bundle();
                args.putParcelable(DetailFragment.DETAIL_URI, uri);
            	args.putInt("MOVIE_ID", mMovieId);
                DetailFragment fragment = new DetailFragment();
                fragment.setArguments(args);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_detail_container, fragment, TAG_DETAIL_FRAGMENT)
                        .commit();
            }
            else {
                Intent intent = new Intent(this, DetailActivity.class);
                intent.putExtra("MOVIE_ID", mMovieId);
                intent.setData(uri);
                 startActivity(intent);
            }
         } else {
            Log.d(TAG, "onActivityResult: req/res not matching");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemId = item.getItemId();

        switch (itemId) {
            case (R.id.action_settings): {
                Log.d(TAG, "onOptionItemSelected - Action_Settings");
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            }
            case (R.id.action_movies): {
                Log.d(TAG, "onOptionItemSelected - Action_Movies");
                showAllMovies();
                break;
            }
            case (R.id.action_favorite): {
                Log.d(TAG, "onOptionItemSelected - Action_Favorite");
                showFavoriteMovies();
                break;
            } default: {
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFavoriteClick(int movieId, boolean newCheckState) {
        Log.d(TAG, "onFavoriteClick");
        Utils.handleOnFavoriteClick(this, movieId, newCheckState);
    }

    @Override
    public void onMoviesLoaded(int movieId) {
        DetailFragment detailFragment = (DetailFragment)
                getSupportFragmentManager().findFragmentByTag(TAG_DETAIL_FRAGMENT);

        if (mTwoPane) {
            detailFragment.updateDetailView(movieId);
        } else {
            // do nothing
        }
    }

    private void showFavoriteMovies() {
        Log.d(TAG, "showFavoriteMovies");
        MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_main);
        if (fragment != null ) {
            Bundle args = new Bundle();
            args.putString("target_uri", "favorites");
            fragment.getFavoriteMovies();
        }
    }

    private void showAllMovies() {
        Log.d(TAG, "showAllMovies");
        MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_main);
        if (fragment != null ) {
            Bundle args = new Bundle();
            args.putString("target_uri", "favorites");
            fragment.getAllMovies();
        }
    }

    private void init() {
        Log.d(TAG, "init");
        // init data load from Movie Service
        Intent movieInitIntent = new Intent(Intent.ACTION_SYNC, null, this, MovieService.class);
        movieInitIntent.putExtra("sort_by", Utils.getSortOrderPreference(this));
        movieInitIntent.putExtra("page", 1);
        movieInitIntent.putExtra("command", "init_movie_data_and_detail");
        startService(movieInitIntent);

    }
}
