package me.jimm.popularmovies2.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by jimmaior on 1/18/16.
 */
public class MovieProvider extends ContentProvider {

    private static final String TAG = MovieProvider.class.getSimpleName();

    // URI Matcher used by this provider
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mMovieDbHelper;

    // CODES
    private static final int MOVIES = 100;
    private static final int MOVIE_BY_MOVIE_ID = 200;
    private static final int MOVIE_FAVORITE = 300;
    private static final int MOVIE_REVIEWS_BY_MOVIE_ID = 400;
    private static final int MOVIE_TRAILER_BY_MOVIE_ID = 500;
    private static final int MOVIE_REVIEWS = 600;
    private static final int MOVIE_VIDEO = 700;

    private static UriMatcher buildUriMatcher() {
        // returns a specific code depending on which URI is matched.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // codes and their URI
        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIES);
        matcher.addURI(authority, MovieContract.PATH_REVIEW, MOVIE_REVIEWS);
        matcher.addURI(authority, MovieContract.PATH_VIDEO, MOVIE_VIDEO);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE_BY_MOVIE_ID);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#/favorite", MOVIE_FAVORITE);
        matcher.addURI(authority, MovieContract.PATH_REVIEW + "/*/reviews", MOVIE_REVIEWS_BY_MOVIE_ID);
        matcher.addURI(authority, MovieContract.PATH_VIDEO + "/*/trailer", MOVIE_TRAILER_BY_MOVIE_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mMovieDbHelper = new MovieDbHelper(getContext());

        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES: {
                return MovieContract.MovieEntry.CONTENT_DIR_TYPE;
            }
            case MOVIE_BY_MOVIE_ID: {
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            }
            case MOVIE_FAVORITE: {
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            }
            case MOVIE_REVIEWS_BY_MOVIE_ID: {
                return MovieContract.MovieReview.CONTENT_DIR_TYPE;
            }
            case MOVIE_TRAILER_BY_MOVIE_ID: {
                return MovieContract.MovieVideo.CONTENT_ITEM_TYPE;
            }
            case MOVIE_REVIEWS: {
                return MovieContract.MovieReview.CONTENT_DIR_TYPE;
            }
            case MOVIE_VIDEO: {
                return MovieContract.MovieVideo.CONTENT_DIR_TYPE;
            }
            default: {
                throw new UnsupportedOperationException("Unknown Uri:" + uri);
            }

        }
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        Cursor returnCursor;
        int code = sUriMatcher.match(uri);

        switch (code) {
            // All Movies sorted by popularity
            case MOVIES: {
                returnCursor = mMovieDbHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                return  returnCursor;
            }

            case MOVIE_BY_MOVIE_ID: {
                returnCursor = mMovieDbHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                return  returnCursor;
            }

            case MOVIE_FAVORITE: {
                returnCursor = mMovieDbHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                return  returnCursor;
            }

            case MOVIE_REVIEWS_BY_MOVIE_ID: {
                returnCursor = mMovieDbHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                return  returnCursor;
            }

            case MOVIE_TRAILER_BY_MOVIE_ID: {
                returnCursor = mMovieDbHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                return  returnCursor;
            }

            case MOVIE_VIDEO: {
                returnCursor = mMovieDbHelper.getReadableDatabase().query(
                        MovieContract.MovieVideo.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                return returnCursor;
            }

            case MOVIE_REVIEWS: {
                returnCursor = mMovieDbHelper.getReadableDatabase().query(
                        MovieContract.MovieReview.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                return returnCursor;
            }

            default: {
                // by default, assume a bad URI
                throw new UnsupportedOperationException("Unknown uri:" + uri);
            }
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        Uri returnUri;

        switch (sUriMatcher.match(uri)) {
            case MOVIES: {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                // insert if the id is not currently in the database
                if (_id > 0) {
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                }
                break;
            }
            case MOVIE_VIDEO: {
                long _id = db.insert(MovieContract.MovieVideo.TABLE_NAME, null, values);
                // insert if the id is not currently in the database
                if (_id > 0) {
                    returnUri = MovieContract.MovieVideo.buildVideoUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                }
                break;
            } case MOVIE_REVIEWS: {
                long _id = db.insert(MovieContract.MovieReview.TABLE_NAME, null, values);
                // insert if the id is not currently in the database
                if (_id > 0) {
                    returnUri = MovieContract.MovieReview.buildReviewUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown Uri:" + uri);
            }
        }

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if (selection == null) selection = "1"; // if null is passed, this makes delete return the number of rows deleted

        switch (match) {
            case MOVIES: {
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            } case MOVIE_REVIEWS: {
                rowsDeleted = db.delete(MovieContract.MovieReview.TABLE_NAME, selection, selectionArgs);
                break;
            } case MOVIE_VIDEO: {
                rowsDeleted = db.delete(MovieContract.MovieVideo.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown Uri:" + uri);
            }
        }

        // notify the listeners here
        if (rowsDeleted != 0 ) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues data, String selection, String[] selectionArgs) {
        // similar to the delete function, but returns the number of rows updated
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;

        switch (match) {
            case MOVIES: {
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, data, selection, selectionArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown Uri:" + uri);
            }

        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values  ) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                         if (_id != -1 ) {
                             returnCount ++;
                         }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case MOVIE_REVIEWS: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieReview.TABLE_NAME, null, value);
                        if (_id != -1 ) {
                            returnCount ++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case MOVIE_VIDEO: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieVideo.TABLE_NAME, null, value);
                        if (_id != -1 ) {
                            returnCount ++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            default: {
                return super.bulkInsert(uri, values);
            }
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mMovieDbHelper.close();
        super.shutdown();
    }
}
