package me.jimm.popularmovies2.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import me.jimm.popularmovies2.R;

public class DetailActivity extends AppCompatActivity {

    private static final String DETAIL_FRAGMENT_TAG = "detail";
    private static final String TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // create a new fragment to be placed in the activity layout
        DetailFragment detailFragment = new DetailFragment();

        // this activity was started to display detailed movie data, so we need to get that
        // data and pass it to the detail fragment as an argument
        detailFragment.setArguments(getIntent().getExtras());

        // add the fragment to the fragment manager
        getSupportFragmentManager().beginTransaction()
                .add(R.id.detail_fragment_container, detailFragment, DETAIL_FRAGMENT_TAG)
                .commit();
    }
}
