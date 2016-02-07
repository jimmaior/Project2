package me.jimm.popularmovies2.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.ListPreference;
import android.util.Log;

import me.jimm.popularmovies2.R;

/**
 * Created by generaluser on 2/7/16.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();
    private static final String FRAG_SETTINGS_TAG = "FRAG_SETTINGS_TAG";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_settings);

        SettingsFragment settingsFragment = new SettingsFragment();

        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_settings_container, settingsFragment, FRAG_SETTINGS_TAG)
            .commit();

    }


}
