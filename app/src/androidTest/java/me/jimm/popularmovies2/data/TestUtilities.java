package me.jimm.popularmovies2.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

import me.jimm.popularmovies2.data.MovieContract.MovieEntry;
import me.jimm.popularmovies2.data.MovieContract.MovieReview;
import me.jimm.popularmovies2.data.MovieContract.MovieVideo;
/**
 * Created by jimmaior on 1/20/16.
 */
public class TestUtilities extends AndroidTestCase{

    private static final String TAG = TestUtilities.class.getSimpleName();

    // private members
    static final long TEST_MOVIE_RELEASE_DATE = 1419033600L;  // December 20th, 2014
    static final int TEST_FAVORITE_TRUE = 1;
    static final int TEST_FAVORITE_FALSE = 0;
    static final String TEST_MOVIE_PLOT = "The history of the world";
    static final int TEST_MOVIE_ID = 1234;
    static final String TEST_MOVIE_POSTER_URL = "/weUSwMdQIa3NaXVzwUoIIcAi85d.jpg";
    static final String TEST_MOVIE_REVIEW_KEY = "5675fd7792514179e7003dc5";
    static final String TEST_MOVIE_VIDEO_KEY = "568aca3bc3a368362801437e";
    static final String TEST_MOVIE_TITLE = "ANT-MAN";
    static final double TEST_MOVIE_USER_RATING = 7.5;

    static final String TEST_REVIEW_AUTHOR = "Jim Maioriello";
    static final String TEST_REVIEW_CONTENT = "This is a great movie!";
    static final String TEST_REVIEW_ID = "5675fd7792514179e7003dc5";
    static final String TEST_REVIEW_URL = "http://j.mp/1ODjyR4";

    static final String TEST_VIDEO_ISO_639_1 = "en";
    static final String TEST_VIDEO_KEY = "sGbxmsDFVnE";
    static final String TEST_VIDEO_NAME = "Official Trailer";
    static final String TEST_VIDEO_SITE = "YouTube";
    static final String TEST_VIDEO_SIZE = "1080";
    static final String TEST_VIDEO_TYPE = "Trailer";
    static final String TEST_VIDEO_ID = "568aca3bc3a368362801437e";

    // some default data
    static ContentValues createMovieData(long id) {
        ContentValues movieData = new ContentValues();
        movieData.put(MovieEntry.COLUMN_FAVORITE, TEST_FAVORITE_FALSE);
        movieData.put(MovieEntry.COLUMN_OVERVIEW, TEST_MOVIE_PLOT);
        movieData.put(MovieEntry.COLUMN_MOVIE_ID, TEST_MOVIE_ID);
        movieData.put(MovieEntry.COLUMN_POSTER_URL, TEST_MOVIE_POSTER_URL);
        movieData.put(MovieEntry.COLUMN_RELEASE_DATE, TEST_MOVIE_RELEASE_DATE);
        movieData.put(MovieEntry.COLUMN_FK_REVIEW_KEY, TEST_MOVIE_REVIEW_KEY);
        movieData.put(MovieEntry.COLUMN_TITLE, TEST_MOVIE_TITLE);
        movieData.put(MovieEntry.COLUMN_USER_RATING, TEST_MOVIE_USER_RATING);
        movieData.put(MovieEntry.COLUMN_FK_VIDEO_KEY, TEST_MOVIE_VIDEO_KEY);
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
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }



}
