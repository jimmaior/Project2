package me.jimm.popularmovies2.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

/**
 * Created by jimmaior on 1/20/16.
 */
public class TestDb extends AndroidTestCase {

    private static final String TAG = TestDb.class.getSimpleName();

    // delete the database at the start of each test
    void deleteTheDatabase() {
        mContext.deleteDatabase((MovieDbHelper.DATABASE_NAME));
    }

    // is called before each test is executed.  ensures we always have a clean test
    public void setUp () {
       // deleteTheDatabase();
    }

    // test that the database schema is correct
    public void testCreateDb() throws Throwable {

        // build a HashSet of all of the table names
        // Note that there is another table in DB that stores Android metadata
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(MovieContract.MovieEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.MovieReview.TABLE_NAME);
        tableNameHashSet.add(MovieContract.MovieVideo.TABLE_NAME);

        // test the the database is created
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new MovieDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // ensure the tables were created
        Cursor c = db.rawQuery("SELECT name from sqlite_master WHERE type = 'table' ", null);
        assertTrue("Error: This means the database has not been created correctly", c.moveToFirst());


        // verify that the tables have been created
        int tableCount = 0;
        do {
            tableNameHashSet.remove(c.getString(0));
            tableCount += 1;
        } while (c.moveToNext());
        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        /*assertTrue("Error: Your database was created without the three required tables",
                tableNameHashSet.isEmpty()); */
        assertTrue("Error: Your database was created without the three required tables",
                tableNameHashSet.isEmpty());

        // now, does our Movie table contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.MovieEntry.TABLE_NAME + ")",
                null);
        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> movieColumnHashSet = new HashSet<>();
        movieColumnHashSet.add(MovieContract.MovieEntry._ID);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_FAVORITE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_OVERVIEW);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_POPULARITY);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_TITLE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_VOTE_COUNT);

        int movieTableColumnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(movieTableColumnNameIndex);
            movieColumnHashSet.remove(columnName);
        } while (c.moveToNext());
        // if this fails, it means that your database doesn't contain all of the required movie
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required movie entry columns",
                movieColumnHashSet.isEmpty());


        // now, do our MovieReview table contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.MovieReview.TABLE_NAME + ")",
                null);
        assertTrue("Error: This means that we were unable to query the database for MovieReview" +
                        " table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> movieReviewColumnHashSet = new HashSet<>();
        movieReviewColumnHashSet.add(MovieContract.MovieReview._ID);
        movieReviewColumnHashSet.add(MovieContract.MovieReview.COLUMN_AUTHOR);
        movieReviewColumnHashSet.add(MovieContract.MovieReview.COLUMN_CONTENT);
        movieReviewColumnHashSet.add(MovieContract.MovieReview.COLUMN_REVIEW_ID);
        movieReviewColumnHashSet.add(MovieContract.MovieReview.COLUMN_URL);
        movieReviewColumnHashSet.add(MovieContract.MovieReview.COLUMN_MOVIE_ID);

        int movieReviewColumnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(movieReviewColumnNameIndex);
            movieReviewColumnHashSet.remove(columnName);
        } while (c.moveToNext());
        // if this fails, it means that your database doesn't contain all of the required movie
        // review columns
        assertTrue("Error: The database doesn't contain all of the required MovieReview columns",
                movieReviewColumnHashSet.isEmpty());

        // now, do our MovieVideo table contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.MovieVideo.TABLE_NAME + ")",
                null);
        assertTrue("Error: This means that we were unable to query the database for MovieVideo" +
                        " table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> movieVideoColumnHashSet = new HashSet<>();
        movieVideoColumnHashSet.add(MovieContract.MovieVideo._ID);
        movieVideoColumnHashSet.add(MovieContract.MovieVideo.COLUMN_KEY);
        movieVideoColumnHashSet.add(MovieContract.MovieVideo.COLUMN_ISO_639_1);
        movieVideoColumnHashSet.add(MovieContract.MovieVideo.COLUMN_NAME);
        movieVideoColumnHashSet.add(MovieContract.MovieVideo.COLUMN_SITE);
        movieVideoColumnHashSet.add(MovieContract.MovieVideo.COLUMN_SIZE);
        movieVideoColumnHashSet.add(MovieContract.MovieVideo.COLUMN_TYPE);
        movieVideoColumnHashSet.add(MovieContract.MovieVideo.COLUMN_VIDEO_ID);
        movieVideoColumnHashSet.add(MovieContract.MovieVideo.COLUMN_MOVIE_ID);

        int movieVideoColumnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(movieVideoColumnNameIndex);
            movieVideoColumnHashSet.remove(columnName);
        } while (c.moveToNext());
        // if this fails, it means that your database doesn't contain all of the required movie
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required movie entry columns",
                movieVideoColumnHashSet.isEmpty());

        db.close();
    }


        public void testMovieEntryTable() {

            // First step: Get reference to writable database
            // If there's an error in those massive SQL table creation Strings,
            // errors will be thrown here when you try to get a writable database.
            MovieDbHelper dbHelper = new MovieDbHelper(mContext);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Second Step: Create ContentValues of what you want to insert
            // (you can use the createNorthPoleLocationValues if you wish)
            ContentValues testValues = TestUtilities.createMovieData();

            // Third Step: Insert ContentValues into database and get a row ID back
            long movieRowId;
            movieRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);

            // Verify we got a row back.
            assertTrue(movieRowId != -1);

            // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
            // the round trip.

            // Fourth Step: Query the database and receive a Cursor back
            // A cursor is your primary interface to the query results.
            Cursor cursor = db.query(
                    MovieContract.MovieEntry.TABLE_NAME,  // Table to Query
                    null, // all columns
                    null, // Columns for the "where" clause
                    null, // Values for the "where" clause
                    null, // columns to group by
                    null, // columns to filter by row groups
                    null // sort order
            );

            // Move the cursor to a valid database row and check to see if we got any records back
            // from the query
            assertTrue( "Error: No Records returned from movie query", cursor.moveToFirst() );

            // Fifth Step: Validate data in resulting Cursor with the original ContentValues
            // (you can use the validateCurrentRecord function in TestUtilities to validate the
            // query if you like)
            TestUtilities.validateCurrentRecord("Error: Movie Query Validation Failed",
                    cursor, testValues);

            // Move the cursor to demonstrate that there is only one record in the database
            assertFalse( "Error: More than one record returned from Movie query",
                    cursor.moveToNext() );

            // Sixth Step: Close Cursor and Database
            cursor.close();
            db.close();
        }


        public void testReviewTable() {
            // First insert the review, and then use the ReviewId to insert
            // the movie. Make sure to cover as many failure cases as you can.

            // Instead of rewriting all of the code we've already written in testLocationTable
            // we can move this code to insertLocation and then call insertLocation from both
            // tests. Why move it? We need the code to return the ID of the inserted location
            // and our testLocationTable can only return void because it's a test.

            // First step: Get reference to writable database

            // Create ContentValues of what you want to insert
            // (you can use the createWeatherValues TestUtilities function if you wish)

            // Insert ContentValues into database and get a row ID back

            // Query the database and receive a Cursor back

            // Move the cursor to a valid database row

            // Validate data in resulting Cursor with the original ContentValues
            // (you can use the validateCurrentRecord function in TestUtilities to validate the
            // query if you like)

            // Finally, close the cursor and database
        }


    }


