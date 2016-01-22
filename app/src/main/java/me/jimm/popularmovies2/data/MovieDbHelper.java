package me.jimm.popularmovies2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import  me.jimm.popularmovies2.data.MovieContract.MovieEntry;
import  me.jimm.popularmovies2.data.MovieContract.MovieReview;
import  me.jimm.popularmovies2.data.MovieContract.MovieVideo;


/**
 * Created by jimmaior on 1/18/16.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    private static final String TAG = MovieDbHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // create a table to hold movie data
        final String SQL_CREATE_MOVIE_TABLE =
            "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieEntry.COLUMN_FAVORITE + " INTEGER NOT NULL, " +       /* 0 is false, 1 is true */
                MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTER_URL + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_FK_REVIEW_KEY + " TEXT, " +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_USER_RATING + " REAL NOT NULL, " +
                MovieEntry.COLUMN_FK_VIDEO_KEY + " TEXT, " +

                // set up the movie entry table to contain foreign keys
                " FOREIGN KEY (" + MovieEntry.COLUMN_FK_REVIEW_KEY + ") REFERENCES " +
                MovieReview.TABLE_NAME + " (" + MovieReview._ID + "), " +

                " FOREIGN KEY (" + MovieEntry.COLUMN_FK_VIDEO_KEY + ") REFERENCES " +
                MovieVideo.TABLE_NAME + " (" + MovieVideo._ID + ") " +

                " );";

        final String SQL_CREATE_REVIEW_TABLE =
            "CREATE TABLE " + MovieReview.TABLE_NAME + " (" +
                MovieReview._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieReview.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                MovieReview.COLUMN_CONTENT + " TEXT NOT NULL, " +
                MovieReview.COLUMN_URL + " TEXT NOT NULL, " +
                MovieReview.COLUMN_REVIEW_ID + " TEXT NOT NULL " +
                " );";


        final String SQL_CREATE_VIDEO_TABLE =
            "CREATE TABLE " + MovieVideo.TABLE_NAME + " (" +
                MovieVideo._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieVideo.COLUMN_ISO_639_1 + " TEXT NOT NULL, " +
                MovieVideo.COLUMN_KEY + " TEXT NOT NULL, " +
                MovieVideo.COLUMN_NAME + " TEXT NOT NULL, " +
                MovieVideo.COLUMN_SITE +  " TEXT NOT NULL, " +
                MovieVideo.COLUMN_SIZE + " INTEGER NOT NULL, " +
                MovieVideo.COLUMN_TYPE + " TEXT NOT NULL, " +
                MovieVideo.COLUMN_VIDEO_ID + " TEXT NOT NULL " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_REVIEW_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_VIDEO_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // this database is only a cache for online data, so its upgrade policy
        // is to discard the data and start over.
        // Note: this method only fires when I update the version number of the database.
        // To update the schema without modifying the database, then comment out the next two lines
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieReview.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieVideo.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
    }
}
