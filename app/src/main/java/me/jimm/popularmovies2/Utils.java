package me.jimm.popularmovies2;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import me.jimm.popularmovies2.data.MovieContract;

/**
 * Created by generaluser on 2/7/16.
 */
public class Utils {

    /**
     *  Member Variables
     */
    private static final String TAG = Utils.class.getSimpleName();
    private static final String SORT_PREFERENCE = "sort_order_settings"; // ListPreference key

    /**
     * retrieves the user preference for sort order as defined in 'Settings'
     * */
    public static String getSortOrderPreference(Context context) {
        Log.d(TAG, "getSortOrderPreference");
        // http://stackoverflow.com/questions/2767354/default-value-of-android-preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultValue = context.getResources().getString(R.string.pref_sort_default);
        String sortOrder = prefs.getString(SORT_PREFERENCE, defaultValue);
        if (sortOrder.equals("POPULARITY")) {
            return MovieContract.MovieEntry.COLUMN_POPULARITY;
        } else if (sortOrder.equals("RATING")) {
            return MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE;
        } else {
            Log.e(TAG, "Sort Order undefined: sortOrder='" + sortOrder + "'");
            return null;
        }
    }
}
