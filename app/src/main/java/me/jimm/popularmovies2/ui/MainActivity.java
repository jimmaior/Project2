package me.jimm.popularmovies2.ui;

import android.content.Intent;
import android.database.Cursor;
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

public class MainActivity extends AppCompatActivity implements MainFragment.Callback {

    /**
     * Member Variables
     */

    private final String TAG = MainActivity.class.getSimpleName();
    private static final String DETAIL_FRAGMENT = "DETAIL_FRAGMENT";
    private static final String MAIN_FRAGMENT = "MAIN_FRAGMENT";
    private boolean mTwoPane;
    private static String mCurrentSortOrder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        // toolbar support
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // check if the activity is using a layout which will contain two fragments
        if (findViewById(R.id.fragment_detail_container) != null) {
            // this is a two page layout
            mTwoPane = true;
            // therefore, we need to add the detail fragment to the fragment_detail_container
            if (savedInstanceState == null) {

                // build and init detail fragment
                DetailFragment fragment = new DetailFragment();
                String sortOrder = "favorite DESC, popularity DESC";
                Cursor c = getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI, null, null, null, sortOrder);
                int movieIdColIdx = c.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
                c.moveToFirst();
                int movieId = c.getInt(movieIdColIdx);
                Uri uri = MovieContract.MovieEntry.buildMovieUriByMovieId(movieId);
                Bundle bundle = new Bundle();
                bundle.putParcelable(DetailFragment.DETAIL_URI, uri);
                bundle.putInt("MOVIE_ID", movieId);
                fragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_detail_container, fragment, DETAIL_FRAGMENT)
                        .commit();
                // init activity
                mCurrentSortOrder = Utils.getSortOrderPreference(this);
            }
        }
        else {
            mTwoPane = false;
                if (savedInstanceState == null) {
                    mCurrentSortOrder = Utils.getSortOrderPreference(this);
                }
//            if (savedInstanceState == null) {
//                // add the fragment which contains the primary UI.
//                MainFragment mainFragment = new MainFragment();
//                getSupportFragmentManager().beginTransaction()
//                        .add(R.id.fragment_container, mainFragment, MAIN_FRAGMENT)
//                        .commit();
//            }
        }


        if (savedInstanceState == null) {
            Log.d(TAG, "initialize data");

            // clean up cache
            getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI, null, null);
            getContentResolver().delete(MovieContract.MovieReview.CONTENT_URI, null, null);
            getContentResolver().delete(MovieContract.MovieVideo.CONTENT_URI, null, null);

            // init data load from Movie Service
            final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, MovieService.class);
            intent.putExtra("sort_by", Utils.getSortOrderPreference(this));
            intent.putExtra("page", 1);
            intent.putExtra("command", "get_movie_data");
            startService(intent);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
            case (R.id.action_share): {
                // TODO: launch a Share Intent FROM DETAIL FRAGMENT!!!!!
                // http://developer.android.com/training/sharing/send.html and
                // http://stackoverflow.com/questions/574195/android-youtube-app-play-video-intent

                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=cxLG2wtE7TM")));
                Log.d(TAG, "onOptionItemSelected - Action_Share");
                break;
            }
            default: {
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri contentUri, int movieId) {
        Log.d(TAG, "onItemSelected - movieId:" + movieId + "; contentUri:" + contentUri.toString());

        Intent movieDetailIntent =  new Intent(Intent.ACTION_SYNC, null, this, MovieService.class);
        movieDetailIntent.putExtra("movie_id", movieId);
        movieDetailIntent.putExtra("page", 1);
        movieDetailIntent.putExtra("command", "get_movie_detail_data");
        startService(movieDetailIntent);


        if (mTwoPane) {
            // show movie details in this activity, by adding/replacing the detail fragment
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);
            args.putInt("MOVIE_ID", movieId);
            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_detail_container, fragment, DETAIL_FRAGMENT)
                    .commit();
        }
        else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("MOVIE_ID", movieId);
            intent.setData(contentUri);
            startActivity(intent);
        }
    }
}
