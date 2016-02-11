package me.jimm.popularmovies2.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import me.jimm.popularmovies2.R;
import me.jimm.popularmovies2.data.MovieContract;
import me.jimm.popularmovies2.models.MovieService;

public class DetailActivity extends AppCompatActivity {

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
