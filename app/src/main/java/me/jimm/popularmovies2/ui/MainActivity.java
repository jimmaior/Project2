package me.jimm.popularmovies2.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import me.jimm.popularmovies2.R;
import me.jimm.popularmovies2.SettingsFragment;

public class MainActivity extends AppCompatActivity implements MainFragment.Callback {

    private final String TAG = MainActivity.class.getSimpleName();
    private static final String DETAIL_FRAGMENT = "DETAIL_FRAGMENT";
    private static final String MAIN_FRAGMENT = "MAIN_FRAGMENT";
    private boolean mTwoPane;


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
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_detail_container, new DetailFragment(), DETAIL_FRAGMENT)
                        .commit();
            }
        }
        else {
            mTwoPane = false;
//            if (savedInstanceState == null) {
//                // add the fragment which contains the primary UI.
//                MainFragment mainFragment = new MainFragment();
//                getSupportFragmentManager().beginTransaction()
//                        .add(R.id.fragment_container, mainFragment, MAIN_FRAGMENT)
//                        .commit();
//            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.d(TAG, "onOptionItemSelected - Settings");

            // TODO: Settings Support
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.fragment_container, new SettingsFragment(), "settings")
//                    .addToBackStack(null)   // necessary to return to the MainFragment from the settingsFragment
//                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri contentUri, int movieId) {
        Log.d(TAG, "onItemSelected - movieId:" + movieId + "; contentUri:" + contentUri.toString());
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
            intent.putExtra("movie_id", movieId);
            intent.setData(contentUri);
            startActivity(intent);
        }
    }
}
