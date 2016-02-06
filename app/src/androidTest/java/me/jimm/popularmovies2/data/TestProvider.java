package me.jimm.popularmovies2.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

import junit.framework.Test;

import me.jimm.popularmovies2.data.MovieContract.MovieEntry;
import me.jimm.popularmovies2.data.MovieContract.MovieReview;
import me.jimm.popularmovies2.data.MovieContract.MovieVideo;

/**
 * Created by generaluser on 1/22/16.
 */
public class TestProvider extends AndroidTestCase {

    private static final String TAG = TestProvider.class.getSimpleName();

    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(MovieEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(MovieReview.CONTENT_URI, null, null) ;
        mContext.getContentResolver().delete(MovieVideo.CONTENT_URI, null, null) ;

        Cursor c1 = mContext.getContentResolver().query(MovieEntry.CONTENT_URI,
                null, null, null, null);
        assertEquals("Error: Records not deleted from Movie Entry table during delete",
                0, c1.getCount());
        c1.close();

        Cursor c2 = mContext.getContentResolver().query(MovieReview.CONTENT_URI,
                null, null, null, null);
        assertEquals("Error: Records not deleted from Movie Review table during delete",
                0, c2.getCount());
        c2.close();

        Cursor c3 = mContext.getContentResolver().query(MovieVideo.CONTENT_URI,
                null, null, null, null);
        assertEquals("Error: Records not deleted from Movie Video table during delete",
                0, c3.getCount());
        c3.close();
    }

    public void deleteAllRecordsFromDb() {
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(MovieEntry.TABLE_NAME, null, null);
        db.delete(MovieVideo.TABLE_NAME, null, null);
        db.delete(MovieReview.TABLE_NAME, null, null);
        db.close();
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

        /*
        This test checks to make sure that the content provider is registered correctly.
        Students: Uncomment this test to make sure you've correctly registered the WeatherProvider.
     */
//    public void testProviderRegistry() {
//        PackageManager pm = mContext.getPackageManager();
//
//        // We define the component name based on the package name from the context and the
//        // WeatherProvider class.
//        ComponentName componentName = new ComponentName(mContext.getPackageName(),
//                WeatherProvider.class.getName());
//        try {
//            // Fetch the provider info using the component name from the PackageManager
//            // This throws an exception if the provider isn't registered.
//            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);
//
//            // Make sure that the registered authority matches the authority from the Contract.
//            assertEquals("Error: WeatherProvider registered with authority: " + providerInfo.authority +
//                    " instead of authority: " + WeatherContract.CONTENT_AUTHORITY,
//                    providerInfo.authority, WeatherContract.CONTENT_AUTHORITY);
//        } catch (PackageManager.NameNotFoundException e) {
//            // I guess the provider isn't registered correctly.
//            assertTrue("Error: WeatherProvider not registered at " + mContext.getPackageName(),
//                    false);
//        }
//    }


    public void testGetType() {

        //  MOVIE (DIR)	 		content://me.jimmaior.popularmovies2/movie
        String type = mContext.getContentResolver().getType(MovieEntry.CONTENT_URI);
        assertEquals("Error: the MovieEntry CONTENT_URI should return MovieEntry.CONTENT_DIR_TYPE",
                MovieEntry.CONTENT_DIR_TYPE, type);

        // MOVIE_BY_POPULARITY (DIR) 	content://me.jimmaior.popularmovies2/movie/popular
        // MOVIE_BY_USER_RATING (DIR)	content://me.jimmaior.popularmovies2/movie/user_rating
        // MOVIE_DTL_BY_MOVIE_ID (ITEM)	content://me.jimmaior.popularmovies2/movie/#
        // TRAILER_BY_MOVIE_ID (ITEM)	content://me.jimmaior.popularmovies2/trailer/#
        // REVIEWS_BY_MOVIE_ID (DIR)	content://me.jimmaior.popularmovies2/reviews/#

    }


    public void testInsertMovieEntry() {

        ContentValues movieValues = TestUtilities.createMovieData();
        Uri uri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, movieValues);
        assertNotNull(uri);

        Cursor providerCursor = mContext.getContentResolver().query(uri, null, null, null, null);

        // compare to the ones we created
        providerCursor.moveToFirst();
        TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating MovieEntry ",
                providerCursor, movieValues);

        // assert there are no other records in the cursor.
        assertEquals("Error: the cursor return more than one record, when only one was expected ",
                1, providerCursor.getCount());

        providerCursor.close();

    }


    public void testGetMovieDetailsById() {
        // I will know the movieId from the view
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase  db = dbHelper.getWritableDatabase();
        ContentValues cv = TestUtilities.createMovieData();
        long rowId = db.insert(MovieEntry.TABLE_NAME, null, cv);
        // Verify we got a row back.
        assertTrue("Error: Failure to insert Movie Entry values", rowId != -1);

        Cursor cursor = db.query(MovieEntry.TABLE_NAME, null, "_ID = " + rowId, null, null, null, null);
        // query CP for the movie data by movie_id

        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());

        int colTitleIdx = cursor.getColumnIndex("title");
        String actual = cursor.getString(colTitleIdx);
        String expected = cv.getAsString(MovieEntry.COLUMN_TITLE);
        assertEquals("Error: Return value is not equal to expected value.", expected, actual);

        cursor.close();

    }

    public void testGetMovieDetailsByMovieId() {
        // I will know the movieId from the view
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase  db = dbHelper.getWritableDatabase();
        ContentValues cv = TestUtilities.createMovieData();
        long rowId = db.insert(MovieEntry.TABLE_NAME, null, cv);
        Cursor c = getContext().getContentResolver().query(MovieContract.MovieEntry.buildMovieUri(rowId), null, null, null, null);
        DatabaseUtils.dumpCursor(c);
        assertEquals("Error: Cursor is empty.", 1, c.getCount());
        int idx = c.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID);
        int movieId = 0;
        c.moveToFirst();
        movieId = c.getInt(idx);
        assertTrue("Error: the cursor does not contain the movie id", movieId > 0);
        Cursor d = getContext().getContentResolver().query(MovieEntry.buildMovieUriByMovieId(movieId), null, null, null, null);
        d.moveToFirst();
        TestUtilities.validateCurrentRecord("Error: The expected movie data is not returned", d, cv);

    }

    public void testBulkInsert() {
        // create some movie data
        ContentValues[] testContentValues = TestUtilities.createBulkInsertMovieData();

        // register an observer for our bulk insert
        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, movieObserver);

        // do bulk insert
        int insertCount = mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, testContentValues);
        movieObserver.waitForNotificationOrFail();

        // unregister observer
        mContext.getContentResolver().unregisterContentObserver(movieObserver);

        assertEquals(TestUtilities.BULK_RECORDS_TO_INSERT, insertCount);

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null //MovieEntry.COLUMN_POPULARITY + " DESC"
        );

        // compare to the ones we created
        cursor.moveToFirst();
        for (int i = 0; i < TestUtilities.BULK_RECORDS_TO_INSERT; i++, cursor.moveToNext()) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating MovieEntry " + i,
                    cursor, testContentValues[i]);
        }

        cursor.close();
    }

    public void testBulkInsertVideoData() {
        // create some movie data
        ContentValues[] movieValues = TestUtilities.createBulkInsertMovieData();

        // register an observer for our bulk insert
        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, movieObserver);

        // do the bulk insert
        int insertMovieCount = mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, movieValues);
        movieObserver.waitForNotificationOrFail();
        // unregister observer
        mContext.getContentResolver().unregisterContentObserver(movieObserver);

        assertTrue(TestUtilities.BULK_RECORDS_TO_INSERT == insertMovieCount);

        // create and insert some videos into the database
        if (TestUtilities.BULK_RECORDS_TO_INSERT == insertMovieCount) {
            ContentValues[] videoValues = TestUtilities.createBulkInsertVideoData(movieValues);

            int insertVideoCount = mContext.getContentResolver().bulkInsert(MovieVideo.CONTENT_URI, videoValues);
            assertEquals("Error: The number of video data inserted into db is not same as the number of movies.",
                    insertMovieCount, insertVideoCount);

            Cursor cursor = mContext.getContentResolver().query(
                    MovieVideo.CONTENT_URI,  null, null, null, null);

            // compare to the ones we created
            cursor.moveToFirst();
            for (int i = 0; i < movieValues.length; i++, cursor.moveToNext()) {
                TestUtilities.validateCurrentRecord("testBulkInsertVideoData.  Error validating MovieVideo " + i,
                        cursor, videoValues[i]);
            }

            cursor.close();
        }
    }

    public void testBulkInsertReviewData() {
        // create some movie data
        ContentValues[] movieValues = TestUtilities.createBulkInsertMovieData();

        // register an observer for our bulk insert
        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, movieObserver);

        // do the bulk insert
        int insertMovieCount = mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, movieValues);
        movieObserver.waitForNotificationOrFail();
        // unregister observer
        mContext.getContentResolver().unregisterContentObserver(movieObserver);

        assertTrue(TestUtilities.BULK_RECORDS_TO_INSERT == insertMovieCount);

        // create and insert some review into the database
        if (TestUtilities.BULK_RECORDS_TO_INSERT == insertMovieCount) {
            ContentValues[] reviewValues = TestUtilities.createBulkInsertReviewData(movieValues);

            int insertReviewCount = mContext.getContentResolver().bulkInsert(MovieReview.CONTENT_URI, reviewValues);
            assertEquals("Error: The number of review data inserted into db is not same as 2 times the number of movies.",
                    insertMovieCount * 2, insertReviewCount);

            Cursor cursor = mContext.getContentResolver().query(
                    MovieReview.CONTENT_URI,  null, null, null, null);

            // compare to the ones we created
            cursor.moveToFirst();
            for (int i = 0; i <cursor.getCount(); i++, cursor.moveToNext()) {
                TestUtilities.validateCurrentRecord("testBulkInsertVideoData.  Error validating MovieVideo " + i,
                        cursor, reviewValues[i]);
            }

            cursor.close();
        }

    }

}
