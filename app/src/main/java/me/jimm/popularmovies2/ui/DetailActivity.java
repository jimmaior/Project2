package me.jimm.popularmovies2.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import me.jimm.popularmovies2.R;
import me.jimm.popularmovies2.Utils;

public class DetailActivity extends AppCompatActivity implements
        DetailFragment.Callback, MainFragment.OnMoviesLoaderFinished {

    private static final String DETAIL_FRAGMENT_TAG = "detail";
    private static final String TAG = DetailActivity.class.getSimpleName();

    // members
    private static int mMovieId = 0;
    private static Uri mMovieDtlByMovieIdUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

      //  setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // intent passed from StartActivity
        Intent intentFromMainActivity = getIntent();
        mMovieId = intentFromMainActivity.getIntExtra("MOVIE_ID", 0);
        mMovieDtlByMovieIdUri =  intentFromMainActivity.getData();

        if (savedInstanceState == null) {
            // create a new fragment to be placed in the activity layout
            DetailFragment detailFragment = new DetailFragment();

            // this activity was started to display detailed movie data, so we need to get that
            // data and pass it to the detail fragment as an argument
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, mMovieDtlByMovieIdUri);
            args.putInt("MOVIE_ID", mMovieId);
            detailFragment.setArguments(args);

            // add the fragment to the fragment manager
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_detail_container, detailFragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        int itemId = item.getItemId();

        switch (itemId) {
            case android.R.id.home:
//                Log.d(TAG, "onOptionItemSelected - home");
//                Intent intent = NavUtils.navigateUpTo().getParentActivityIntent(this);
//                NavUtils.navigateUpTo(this, intent);
                onBackPressed();
                return true;
           default: {
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
        // do nothing
    }
}
