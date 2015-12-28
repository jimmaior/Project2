package me.jimm.popularmovies2;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    
    private static final String TAG = SettingsFragment.class.getSimpleName();
    public static final String SORT_PREFERENCE_KEY = "sort_order_settings";

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        updatePreferenceSummary(findPreference(SORT_PREFERENCE_KEY));
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        Log.d(TAG, "onCreatePreferences()");
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        Log.d(TAG, "onSharedPreferenceChanged");
        updatePreferenceSummary(findPreference(key));
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    protected  void updatePreferenceSummary(Preference pref) {
        Log.d(TAG, "updatePreferenceSummary - pref=" + pref.getKey() );
        if (pref == null) {
            return;
        }

        if (pref instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) pref;
            listPreference.setSummary(listPreference.getEntry());
        }
    }
}
