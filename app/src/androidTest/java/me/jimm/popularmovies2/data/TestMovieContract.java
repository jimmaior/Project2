package me.jimm.popularmovies2.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by jimmaior on 1/22/16.
 */
public class TestMovieContract extends AndroidTestCase {


    // test the URI builder for ListMoviesByPopularity
    // test the URI builder for ListMoviesByUserRating
    // test the URI builder for MovieDetailsByMovieId
    // test the Uri builder for MovieTrailerByMovieId
    // text the Uri builder for MovieReviewsByMovieId

    public void testBuildMovieListByPopularity() {}
    public void testBuildMovieListByUserRating() {}
    public void testBuildMovieDetailsByMovieId() {}
    public void testBuildMovieTrailerByMovieId() {}
    public void testBuildMovieReviewsByMovieId() {}


    public void testBuildMovieLocation() {
//        Uri locationUri = MovieContract.MovieEntry.buildMovieReview(TEST_MOVIE_REVIEW);
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
