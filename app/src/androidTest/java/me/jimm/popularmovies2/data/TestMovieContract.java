package me.jimm.popularmovies2.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by jimmaior on 1/22/16.
 */
public class TestMovieContract extends AndroidTestCase {


    // intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final String TEST_MOVIE_REVIEW = "/North Pole";
    private static final long TEST_MOVIE_VIDEO = 1419033600L;  // December 20th, 2014

   
    public void testBuildMovieLocation() {
//        Uri locationUri = MovieContract.MovieEntry..buildMovieReview(TEST_MOVIE_REVIEW);
//        assertNotNull("Error: Null Uri returned.  You must fill-in buildMovieLocation in " +
//                        "MovieContract.",
//                locationUri);
//        assertEquals("Error: Movie location not properly appended to the end of the Uri",
//                TEST_Movie_LOCATION, locationUri.getLastPathSegment());
//        assertEquals("Error: Movie location Uri doesn't match our expected result",
//                locationUri.toString(),
//                "content://com.example.android.sunshine.app/Movie/%2FNorth%20Pole");
    }
}
