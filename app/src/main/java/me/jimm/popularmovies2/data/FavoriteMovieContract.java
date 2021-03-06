package me.jimm.popularmovies2.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by generaluser on 1/18/16.
 *
 * definition of table and columns for the movie database.
 *
 */
public class FavoriteMovieContract {

    public static final String VERSION = "1.0";
    public static final String CONTENT_AUTHORITY = "me.jimm.popularmovies2";
    public static final Uri BASE_CONTENT_URI =Uri.parse("content://" + CONTENT_AUTHORITY);

    // paths
    public static final String PATH_FAV_MOVIE = "fav_movie_entry";
    public static final String PATH_FAV_MOVIE_REVIEW = "fav_movie_review";
    public static final String PATH_FAV_MOVIE_VIDEO = "fav_movie_video";


    /* Inner Class to define the table contents of the Movie Table */
    public static final class FavMovieEntry implements BaseColumns {

        // CONTENT_URI
        public static final Uri CONTENT_URI = BASE_CONTENT_URI
                .buildUpon().appendPath(PATH_FAV_MOVIE).build();

        // CONTENT_TYPE
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAV_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAV_MOVIE;

        // Table Name
        public static final String TABLE_NAME = "fav_movie_entry";

        // columns
        public static final String COLUMN_FAVORITE = "favorite";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_VOTE_COUNT = "vote_count";


        // content://me.jimm.popularmovies2/fav_movie_entry/id=?
        public static Uri buildFavMovieUriById(long id) {

            return ContentUris.withAppendedId(FavMovieEntry.CONTENT_URI, id);
        }

        // content://me.jimm.popularmovies2/fav_movie_entry/#
        public static Uri buildFavMovieUriByMovieId(int movieId) {

            return CONTENT_URI.buildUpon().appendPath(Integer.toString(movieId)).build();
        }

        //// content://me.jimm.popularmovies2/fav_movie_entry
        public static Uri buildFavMovieListUri() {

            return FavMovieEntry.CONTENT_URI;

        }

    }

    /** Inner class to define the reviews associated with a movie */
    public static final class FavMovieReview implements BaseColumns {

        // CONTENT_URI
        public static final Uri CONTENT_URI = BASE_CONTENT_URI
                .buildUpon()
                .appendPath(PATH_FAV_MOVIE_REVIEW)
                .build();

        // CONTENT_TYPE
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAV_MOVIE_REVIEW;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAV_MOVIE_REVIEW;;

        // TABLE NAME
        public static final String  TABLE_NAME = "fav_movie_review";

        // COLUMNS
        public static final String COLUMN_REVIEW_ID = "review_id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_MOVIE_ID = "movie_id";

        // content://me.jimm.popularmovies2/fav_movie_review/id=?
        public static Uri buildFavMovieReviewUri(long id) {

            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // content://me.jimm.popularmovies2/fav_movie_review/#/reviews
        public static Uri buildFavMovieReviewUriByMovieId(int movieId) {
            return FavMovieReview.CONTENT_URI
                    .buildUpon().appendPath(Integer.toString(movieId)).appendPath("reviews").build();
        }
    }

    /*Inner class to define the video resources associated with a movie */
    public static final class FavMovieVideo implements BaseColumns {

        // CONTENT_URI
        public static final Uri CONTENT_URI = BASE_CONTENT_URI
                .buildUpon()
                .appendPath(PATH_FAV_MOVIE_VIDEO)
                .build();

        // CONTENT_TYPE
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAV_MOVIE_VIDEO;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAV_MOVIE_VIDEO;;

        // TABLE NAME
        public static final String  TABLE_NAME = "fav_movie_video";

        // COLUMNS
        public static final String COLUMN_ISO_639_1 = "iso_639_1";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_SIZE = "size";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_VIDEO_ID = "video_id";
        public static final String COLUMN_MOVIE_ID = "movie_id";

        // content://me.jimm.popularmovies2/fav_movie_video/id=?
        public static Uri buildVideoUri(long id) {

            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // content://me.jimm.popularmovies2/fav_movie_video/#/trailer
        public static Uri buildTrailerUriByMovieId(int movieId) {
            return FavMovieVideo.CONTENT_URI
                    .buildUpon().appendPath(Integer.toString(movieId)).appendPath("trailer").build();
        }

    }


}
