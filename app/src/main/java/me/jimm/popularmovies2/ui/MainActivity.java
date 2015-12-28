package me.jimm.popularmovies2.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import me.jimm.popularmovies2.R;
import me.jimm.popularmovies2.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        // toolbar support
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // check that the activity is using a layout which will contain the fragment
        if (findViewById(R.id.fragment_container) != null) {
            // However, if app is restored from a previous state, then we don't need
            // to do anything
            if (savedInstanceState != null) {
                return;
            } else {
                // add the fragment which contains the primary UI.
                MainFragment mainFragment = new MainFragment();
                getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, mainFragment, "main")
                        .commit();
                }
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

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SettingsFragment(), "settings")
                    .addToBackStack(null)   // necessary to return to the MainFragment from the settingsFragment
                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
