package me.jimm.popularmovies2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import me.jimm.popularmovies2.data.MovieContract.MovieEntry;
import me.jimm.popularmovies2.data.MovieContract.MovieReview;
import me.jimm.popularmovies2.data.MovieContract.MovieVideo;
import me.jimm.popularmovies2.utils.PollingCheck;

/**
 * Created by jimmaior on 1/20/16.
 */
public class TestUtilities extends AndroidTestCase{


    // public members
    public static final int BULK_RECORDS_TO_INSERT = 20;

    private static final String TAG = TestUtilities.class.getSimpleName();

    // private members
    static final long TEST_MOVIE_RELEASE_DATE = 1419033600L;  // December 20th, 2014
    static final int TEST_FAVORITE_TRUE = 1;
    static final int TEST_FAVORITE_FALSE = 0;
    static final String TEST_MOVIE_PLOT = "Thirty years after defeating the Galactic Empire,...";
    static final int TEST_MOVIE_ID = 140607;
    static final String TEST_MOVIE_POSTER_URL = "/fYzpM9GmpBlIC893fNjoWCwE24H.jpg";
    static final String TEST_MOVIE_BACKDROP = "/njv65RTipNSTozFLuF85jL0bcQe.jpg";
    static final String TEST_MOVIE_TITLE = "Star Wars: The Force Awakens";
    static final double TEST_MOVIE_POPULARITY = 57.865219;
    static final double TEST_MOVIE_VOTE_AVERAGE = 7.84;
    static final int TEST_MOVIE_VOTE_COUNT = 2517;

    static final String TEST_REVIEW_AUTHOR = "Frank Ochieng";
    static final String TEST_REVIEW_CONTENT = "So where were you when the Science Fiction...";
    static final String TEST_REVIEW_ID = "5675fd7792514179e7003dc5";
    static final String TEST_REVIEW_URL = "http://j.mp/1ODjyR4";

    static final String TEST_VIDEO_ISO_639_1 = "en";
    static final String TEST_VIDEO_KEY = "7GqClqvlObY";
    static final String TEST_VIDEO_NAME = "Official Trailer";
    static final String TEST_VIDEO_SITE = "YouTube";
    static final String TEST_VIDEO_SIZE = "1080";
    static final String TEST_VIDEO_TYPE = "Trailer";
    static final String TEST_VIDEO_ID = "56437778c3a36870e30027df";


    static ContentValues[]  createBulkInsertMovieData() {
        ContentValues[] returnContentValues = new ContentValues[BULK_RECORDS_TO_INSERT];
        for (int i = 0; i < BULK_RECORDS_TO_INSERT; i++) {
            ContentValues movieValues = new ContentValues();
            movieValues.put(MovieEntry.COLUMN_BACKDROP_PATH, "backdrop_path" + i);
            movieValues.put(MovieEntry.COLUMN_OVERVIEW, "overview" + i);
            movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, "release date" + i);
            movieValues.put(MovieEntry.COLUMN_POSTER_PATH, "poster path" + i);
            movieValues.put(MovieEntry.COLUMN_TITLE, "title" + i);
            movieValues.put(MovieEntry.COLUMN_MOVIE_ID, i);
            movieValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, ((i+1) * 5.25) / 1.55 );
            movieValues.put(MovieEntry.COLUMN_VOTE_COUNT, i + 10 + (i+1) );
            movieValues.put(MovieEntry.COLUMN_POPULARITY, i * 5.5);
            movieValues.put(MovieEntry.COLUMN_FAVORITE, 0);

            returnContentValues[i] = movieValues;
        }
        return returnContentValues;
    }

    static ContentValues[] createBulkInsertVideoData(ContentValues[] movieValues) {
        ContentValues[] returnContentValues = new ContentValues[movieValues.length];

        for (int i = 0; i < movieValues.length; i++) {
            ContentValues videoValues = new ContentValues();
            videoValues.put(MovieVideo.COLUMN_MOVIE_ID, movieValues[i].getAsInteger(MovieEntry.COLUMN_MOVIE_ID));
            videoValues.put(MovieVideo.COLUMN_VIDEO_ID, i);
            videoValues.put(MovieVideo.COLUMN_TYPE, "Trailer");
            videoValues.put(MovieVideo.COLUMN_SITE, "YouTube");
            videoValues.put(MovieVideo.COLUMN_SIZE, 1028 * (i + 1));
            videoValues.put(MovieVideo.COLUMN_KEY, "ascdefgh12a");
            videoValues.put(MovieVideo.COLUMN_ISO_639_1, "en");
            videoValues.put(MovieVideo.COLUMN_NAME, "name" + i + "Teaser");

            returnContentValues[i] = videoValues;
        }
        return returnContentValues;
    }

    static ContentValues[] createBulkInsertReviewData(ContentValues[] movieValues) {
        int movieReviewsCount = movieValues.length * 2;
        ContentValues[] returnContentValues = new ContentValues[movieReviewsCount];
        int[] movie_id = new int[movieReviewsCount];

        int m = 0;
        for (int k = 0; k < movieValues.length * 2; k = k+2) {
            movie_id[k] = movieValues[m].getAsInteger(MovieEntry.COLUMN_MOVIE_ID);
            movie_id[k+1] = movieValues[m].getAsInteger(MovieEntry.COLUMN_MOVIE_ID);
            m = m + 1;
        }

        for (int i = 0; i < movieReviewsCount; i++) {
            ContentValues reviewValues = new ContentValues();
            reviewValues.put(MovieReview.COLUMN_MOVIE_ID, movie_id[i]);
            reviewValues.put(MovieReview.COLUMN_AUTHOR, "author" + i);
            reviewValues.put(MovieReview.COLUMN_CONTENT, "content..." + i);
            reviewValues.put(MovieReview.COLUMN_REVIEW_ID, i);
            reviewValues.put(MovieReview.COLUMN_URL, "/some_url.jpg");

            returnContentValues[i] = reviewValues;
        }

        return returnContentValues;
    }


    static long insertMovieData(Context context) {
        MovieDbHelper dbHelper = new MovieDbHelper(context);
        SQLiteDatabase  db = dbHelper.getWritableDatabase();
        ContentValues cv = TestUtilities.createMovieData();
        long rowId = db.insert(MovieEntry.TABLE_NAME, null, cv);
        // Verify we got a row back.
        assertTrue("Error: Failure to insert Movie Entry values", rowId != -1);
        return rowId;
    }


    // some default data
    static ContentValues createMovieData() {
        ContentValues movieData = new ContentValues();
        movieData.put(MovieEntry.COLUMN_FAVORITE, TEST_FAVORITE_FALSE);
        movieData.put(MovieEntry.COLUMN_BACKDROP_PATH, TEST_MOVIE_BACKDROP);
        movieData.put(MovieEntry.COLUMN_OVERVIEW, TEST_MOVIE_PLOT);
        movieData.put(MovieEntry.COLUMN_MOVIE_ID, TEST_MOVIE_ID);
        movieData.put(MovieEntry.COLUMN_POSTER_PATH, TEST_MOVIE_POSTER_URL);
        movieData.put(MovieEntry.COLUMN_RELEASE_DATE, TEST_MOVIE_RELEASE_DATE);
        movieData.put(MovieEntry.COLUMN_POPULARITY, TEST_MOVIE_POPULARITY);
        movieData.put(MovieEntry.COLUMN_TITLE, TEST_MOVIE_TITLE);
        movieData.put(MovieEntry.COLUMN_VOTE_AVERAGE, TEST_MOVIE_VOTE_AVERAGE);
        movieData.put(MovieEntry.COLUMN_VOTE_COUNT, TEST_MOVIE_VOTE_COUNT);
        return movieData;
    }

    static ContentValues createReviewData(long id) {
        ContentValues reviewData = new ContentValues();
        reviewData.put(MovieReview.COLUMN_AUTHOR, TEST_REVIEW_AUTHOR);
        reviewData.put(MovieReview.COLUMN_CONTENT, TEST_REVIEW_CONTENT);
        reviewData.put(MovieReview.COLUMN_REVIEW_ID, TEST_REVIEW_ID);
        reviewData.put(MovieReview.COLUMN_URL, TEST_REVIEW_URL);
        return reviewData;
    }

    static ContentValues createVideoData(long id) {
        ContentValues videoData = new ContentValues();
        videoData.put(MovieVideo.COLUMN_ISO_639_1, TEST_VIDEO_ISO_639_1);
        videoData.put(MovieVideo.COLUMN_KEY, TEST_VIDEO_KEY);
        videoData.put(MovieVideo.COLUMN_NAME, TEST_VIDEO_NAME);
        videoData.put(MovieVideo.COLUMN_SITE, TEST_VIDEO_SITE);
        videoData.put(MovieVideo.COLUMN_SIZE, TEST_VIDEO_SIZE);
        videoData.put(MovieVideo.COLUMN_TYPE, TEST_VIDEO_TYPE);
        videoData.put(MovieVideo.COLUMN_VIDEO_ID, TEST_VIDEO_ID);
        return videoData;
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        //DatabaseUtils.dumpCursor(valueCursor);
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = formatDecimal(entry.getValue().toString());
            String cursorValue = formatDecimal(valueCursor.getString(idx));
            assertEquals("Value from cursor '" + cursorValue +
                    "' did not match the expected value '" +
                    expectedValue + "' for column '" + columnName +  "'. " + error, expectedValue, cursorValue);
        }
    }

    static private String formatDecimal(String value) {
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);
        if (StringUtils.isNumeric(value.toString()) ) {
            return df.format( Double.parseDouble(value) );
        } else {
            return null;
        }
    }

    // tests the ContentObserver callbacks using the PollingClass from the Android CTS tests
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;

        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }

    }


    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
