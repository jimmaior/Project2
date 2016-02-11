package me.jimm.popularmovies2.models;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import me.jimm.popularmovies2.BuildConfig;
import me.jimm.popularmovies2.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;


public class MovieService extends IntentService {

    // public members
    public static final int STATUS_RUNNING = 100;
    public static final int STATUS_ERROR = -100;
    public static final int STATUS_FINISHED = 200;
    public static final String SORT_ORDER_EXTRA_KEY = "sort_by";
    public static final String MOVIE_API_PARAM_SORT_BY_POPULARITY = "popularity.desc";
    public static final String MOVIE_API_PARAM_SORT_BY_RATING = "vote_average.desc";

    // private members
    private static final String TAG = MovieService.class.getSimpleName();

    // JSON fields
    private static final String MOVIE_API_RESULTS = "results";
    private static final String MOVIE_API_REVIEWS = "reviews";
    private static final String MOVIE_API_TRAILERS = "trailers";
    private static final String MOVIE_API_BACKDROP_PATH = "backdrop_path";
    private static final String MOVIE_API_MOVIE_ID = "id";
    private static final String MOVIE_API_OVERVIEW = "overview";
    private static final String MOVIE_API_POPULARITY = "popularity";
    private static final String MOVIE_API_POSTER_PATH = "poster_path";
    private static final String MOVIE_API_RELEASE_DATE = "release_date";
    private static final String MOVIE_API_TITLE = "title";
    private static final String MOVIE_API_VOTER_AVERAGE = "vote_average";
    private static final String MOVIE_API_VOTER_COUNT = "vote_count";

    private static final String VIDEO_API_MOVIE_ID = "id";
    private static final String VIDEO_API_VIDEO_ID = "id";
    private static final String VIDEO_API_ISO_639_1 = "iso_639_1";
    private static final String VIDEO_API_KEY = "source";
    private static final String VIDEO_API_NAME = "name";
    private static final String VIDEO_API_TYPE = "type";
    private static final String VIDEO_API_SIZE = "size";
    private static final String VIDEO_API_SITE = "site";
    private static final String VIDEO_API_YOUTUBE = "youtube";

    private static final String REVIEW_API_MOVIE_ID = "id";
    private static final String REVIEW_API_REVIEW_ID = "id";
    private static final String REVIEW_API_CONTENT = "content";
    private static final String REVIEW_API_AUTHOR = "author";
    private static final String REVIEW_API_URL = "url";

    // API Parameters ////////////////////////////////////////////////////////
    // full poster path
    private static final String MOVIE_IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String MOVIE_VOTE_COUNT_GTE_VALUE = "10";
    private static final String MOVIE_IMAGE_SIZE = "w342/"; // seems better

    public MovieService() {
        super("MovieService");
    }

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent()");

        Bundle bundle = new Bundle();
        String command = intent.getStringExtra("command");
        switch (command) {
            case "get_movie_data": {
                try {
                    String moviesJson = fetchMovieData(intent);
                    ArrayList results = createMovieListArray(moviesJson);
                    bundle.putParcelableArrayList("results", results);
  //                  receiver.send(STATUS_FINISHED, bundle);
                    // TODO: remove save all movie data
                     saveMovieData(results);
                    // fetchMovieDetails(results);
                } catch (Exception e) {
                    bundle.putString(Intent.EXTRA_TEXT, e.toString());
 //                   receiver.send(STATUS_ERROR, bundle);
                }
                break;
            }
            case "get_movie_detail_data": {
                try {
                    // TODO: Requires the movieId instead of the results
                    int movieId = intent.getIntExtra("movie_id", 0);
                    String movieDtlsJson = fetchMovieDetails(movieId);
                    saveMovieDetailData(movieDtlsJson);
                } catch (Exception e) {
                    e.getMessage();
                }
                break;
            }
            case "save_favorite_movie": { }
            case "save_favorite_movie_details": {}
            default: {
                Log.e(TAG, "Error: unsupported command '" + command + "'" );
            }
        }
    }

    @Nullable
    private String fetchMovieData(Intent intent) {
        Log.d(TAG, "fetchMovieData()");

        // need to be declared outside of the try so they can be closed in the finally block
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String moviesJsonString = null;

        try {
            // construct the URL
            final String BASE_MOVIE_URL = "http://api.themoviedb.org/3/discover/movie?";
            final String SORT_BY_PARAM = "sort_by";
            final String PAGE_PARAM = "page";
            final String API_KEY_PARAM = "api_key";
            final String VOTE_COUNT_GTE  = "vote_count.gte";

            Uri builtUri = Uri.parse(BASE_MOVIE_URL).buildUpon()
                    .appendQueryParameter(SORT_BY_PARAM, intent.getStringExtra("sort_by") + ".desc")
                    .appendQueryParameter(VOTE_COUNT_GTE, MOVIE_VOTE_COUNT_GTE_VALUE)
                    .appendQueryParameter(PAGE_PARAM, Integer.toString(intent.getIntExtra("page", 1)))
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                    .build();
            URL url = new URL(builtUri.toString());
            Log.d(TAG, url.toString());
            // create the request to MOVIE_API
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");  // adding a new line character for debug readability
            }

            if (buffer.length() == 0) {
                // stream was empty, no point in parsing
                return null;
            } else {
                // get structured data from JSON String
                moviesJsonString = buffer.toString();
            }
        }
        catch (IOException ioe) {
            Log.e(TAG, "Error attempting to get the movie data" + ioe.getMessage(), ioe);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try{
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream" + e.getMessage(), e);
                }
            }
        }

        return moviesJsonString;
    }


    @Nullable
    private String fetchMovieDetails(int movieId) {
        Log.d(TAG, "fetchMovieDetails()");

        // need to be declared outside of the try so they can be closed in the finally block
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String movieDetailsJsonString = null;

        try {
            // construct the URL: example: http://api.themoviedb.org/3/movie/140607?api_key=XXX&append_to_response=reviews%2Ctrailers
            final String BASE_VIDEO_URL = "http://api.themoviedb.org/3/movie/";
            final String APPEND_TO_RESPONSE = "append_to_response";
            final String JSON_OBJECTS = "reviews,trailers";
            final String API_KEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(BASE_VIDEO_URL).buildUpon()
                    .appendPath(Integer.toString(movieId))
                    .appendQueryParameter(APPEND_TO_RESPONSE, JSON_OBJECTS)
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                    .build();
            URL url = new URL(builtUri.toString());

            // create the request to VIDEO API
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (inputStream == null) {
                // Nothing to do
            } else {
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");  // adding a new line character for debug readability
                }
            }

            if (buffer.length() == 0) {
                // stream was empty
            } else {
                // store the json objects to be saved in an array
                movieDetailsJsonString = buffer.toString();
            }
        }
        catch (IOException ioe) {
            Log.e(TAG, "Error attempting to get the video data. " + ioe.getMessage(), ioe);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try{
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream" + e.getMessage(), e);
                }
            }
        }

        return movieDetailsJsonString;
    }


    private void saveMovieDetailData(String  jsonStringList) {
        Log.d(TAG, "saveMovieDetailData: object to be processed:" + jsonStringList);

            // PARSE DATA INTO CONTENT VALUES
            try {
                JSONObject movieDtlObj = new JSONObject(jsonStringList);
                int movieId = movieDtlObj.getInt(MOVIE_API_MOVIE_ID);

                // REVIEWS
                JSONObject movieReviewObj = movieDtlObj.getJSONObject(MOVIE_API_REVIEWS);
                JSONArray movieReviewArray = movieReviewObj.getJSONArray(MOVIE_API_RESULTS);
                Log.d(TAG, "review count:" + movieReviewArray.length());
                for (int i = 0; i < movieReviewArray.length(); i++) {
                    ContentValues movieReviewValues = new ContentValues();
                    JSONObject review = movieReviewArray.getJSONObject(i);
                    movieReviewValues.put(MovieContract.MovieReview.COLUMN_MOVIE_ID, movieId);
                    movieReviewValues.put(MovieContract.MovieReview.COLUMN_AUTHOR, review.getString(REVIEW_API_AUTHOR));
                    movieReviewValues.put(MovieContract.MovieReview.COLUMN_CONTENT, review.getString(REVIEW_API_CONTENT));
                    movieReviewValues.put(MovieContract.MovieReview.COLUMN_REVIEW_ID, review.getString(REVIEW_API_REVIEW_ID));
                    movieReviewValues.put(MovieContract.MovieReview.COLUMN_URL, review.getString(REVIEW_API_URL));

                    // SAVE DATA TO CP
                    // either update or insert review data into ContentProvider
                    saveMovieReviewDtl(movieReviewValues);
                }

                // TRAILERS
                JSONObject movieTrailerObj = movieDtlObj.getJSONObject(MOVIE_API_TRAILERS);
                JSONArray movieTrailerArray = movieTrailerObj.getJSONArray(VIDEO_API_YOUTUBE);
                Log.d(TAG, "trailer count:" + movieTrailerArray.length());
                for (int i = 0; i < movieTrailerArray.length(); i++) {
                    ContentValues movieTrailerValues = new ContentValues();
                    JSONObject video = movieTrailerArray.getJSONObject(i);
                    movieTrailerValues.put(MovieContract.MovieVideo.COLUMN_MOVIE_ID, movieId);
                    movieTrailerValues.put(MovieContract.MovieVideo.COLUMN_NAME, video.getString(VIDEO_API_NAME));
                    movieTrailerValues.put(MovieContract.MovieVideo.COLUMN_TYPE, video.getString(VIDEO_API_TYPE));
                    //    movieTrailerValues.put(MovieContract.MovieVideo.COLUMN_SITE, video.getString(VIDEO_API_SITE));
                    movieTrailerValues.put(MovieContract.MovieVideo.COLUMN_KEY, video.getString(VIDEO_API_KEY));
                    //    movieTrailerValues.put(MovieContract.MovieVideo.COLUMN_VIDEO_ID, video.getString(VIDEO_API_VIDEO_ID));
                    //    movieTrailerValues.put(MovieContract.MovieVideo.COLUMN_ISO_639_1, video.getString(VIDEO_API_ISO_639_1));
                    movieTrailerValues.put(MovieContract.MovieVideo.COLUMN_SIZE, video.getString(VIDEO_API_SIZE));
                    //Log.d(TAG, movieTrailerArray.get(i).toString());

                    // SAVE DATA TO CP
                    // either update or insert review data into ContentProvider
                    saveMovieVideoDtl(movieTrailerValues);
                }


            } catch (JSONException je) {
                Log.e(TAG, "Error parsing the Movie Details JSON String " + je.getMessage());
            }
    }

    // TODO" Redo method insertOrUpdateReviewCp.  probably want to completely regenerate the table data each time
    private void saveMovieReviewDtl(ContentValues values) {
        Log.d(TAG, "saveMovieReviewDtl");
        if (values != null ) {
            int movieId = values.getAsInteger(MovieContract.MovieReview.COLUMN_MOVIE_ID);
            String[] whereArgs = new String[1];
            whereArgs[0] = Integer.toString(movieId);
            // if the objects exists in db, then update it.   Otherwise, insert the object
            Cursor row =  getContentResolver().query(
                    MovieContract.MovieReview.buildReviewUriByMovieId(movieId),
                    null,
                    MovieContract.MovieReview.COLUMN_MOVIE_ID + " = ?",
                    whereArgs,
                    null
            );
//            if (row != null && row.getCount() > 0 ) {
                //Log.d(TAG, "movie id  '" + movieId + "' exists in the CP");
//                getContentResolver().update(MovieContract.MovieReview.CONTENT_URI, values, MovieContract.MovieReview.COLUMN_MOVIE_ID + " = ?", whereArgs);
//            } else {
                //Log.d(TAG, "movie id '" + movieId + " ' does not exist in CP");
                getContentResolver().insert(MovieContract.MovieReview.CONTENT_URI, values);
//            }
            if (row != null) {row.close();}
        }
    }

    private void saveMovieVideoDtl(ContentValues values) {
        Log.d(TAG, "saveMovieVideoDtl");
        if (values != null) {
            int movieId = values.getAsInteger(MovieContract.MovieVideo.COLUMN_MOVIE_ID);
            String[] whereArgs = new String[1];
            whereArgs[0] = Integer.toString(movieId);
            // if the objects exists in db, then update it.   Otherwise, insert the object
            Cursor row =  getContentResolver().query(
                    MovieContract.MovieVideo.buildTrailerUriByMovieId(movieId),
                    null,
                    MovieContract.MovieVideo.COLUMN_MOVIE_ID  + " = ?",
                    whereArgs,
                    null
            );
//            if (row != null && row.getCount() > 0) {
                //Log.d(TAG, "movie id  '" + movieId + "' exists in the CP as '" + Integer.toString(row.getInt(columnIndex)) + "'");
//                getContentResolver().update(MovieContract.MovieVideo.CONTENT_URI, values, MovieContract.MovieVideo.COLUMN_MOVIE_ID + " = ?", whereArgs);
//            } else {
                //Log.d(TAG, "movie id '" + movieId + " ' does not exist in CP");
                getContentResolver().insert(MovieContract.MovieVideo.CONTENT_URI, values);
//            }
            if (row != null) {row.close();}
        }
    }
    
    
    private ArrayList createMovieListArray(String jsonString){
        Log.d(TAG, "createMovieListArray");

        ArrayList<Movie> movieList = new ArrayList<>();

        try {
            JSONObject movieListJson = new JSONObject(jsonString);
            JSONArray movieArray = movieListJson.getJSONArray(MOVIE_API_RESULTS);
            for(int i=0; i < movieArray.length(); i++){
                JSONObject movieJsonObject = movieArray.getJSONObject(i);
                Movie movie = new Movie();

                movie.setFavorite();
                movie.setMovieId(movieJsonObject.getInt(MOVIE_API_MOVIE_ID));
                movie.setTitle(movieJsonObject.getString(MOVIE_API_TITLE));
                movie.setReleaseDate(movieJsonObject.getString(MOVIE_API_RELEASE_DATE));
                movie.setVoterAverage(movieJsonObject.getDouble(MOVIE_API_VOTER_AVERAGE));
                movie.setVoterCount(movieJsonObject.getInt(MOVIE_API_VOTER_COUNT));
                movie.setOverview(movieJsonObject.getString(MOVIE_API_OVERVIEW));
                movie.setPosterPath(
                        MOVIE_IMAGE_BASE_URL +
                                MOVIE_IMAGE_SIZE +
                                movieJsonObject.getString(MOVIE_API_POSTER_PATH));
                movie.setBackdropPath(
                        MOVIE_IMAGE_BASE_URL +
                                MOVIE_IMAGE_SIZE +
                                movieJsonObject.getString(MOVIE_API_BACKDROP_PATH));
                movie.setPopularity( movieJsonObject.getDouble(MOVIE_API_POPULARITY));
                movie.setPopularity( movieJsonObject.getDouble(MOVIE_API_POPULARITY));

                movieList.add(movie);
            }

        } catch (JSONException je) {
            Log.e(TAG, "Error parsing the JSON string in method 'createMovieListArray'" + je.getMessage());
        }
        return movieList;
    }

    // TODO: update if existing records, otherwise, do bulk insert
    private void saveMovieData(ArrayList<Movie> movies) {
        Log.d(TAG, "saveMovieData");

        // used to insert movie data into the database
        Vector<ContentValues> cVVector = new Vector<>(movies.size());

        for (Movie movie : movies) {
            ContentValues movieValues = new ContentValues();
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie.getMovieId());
            movieValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, movie.isFavorite());
            movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
            movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.getTitle());
            movieValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, movie.getPopularity());
            movieValues.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH, movie.getBackdropPath());
            movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, movie.getVoterCount());
            movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.getVoterAverage());
            movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
            movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.getOverview());

            cVVector.add(movieValues);
        }

        int inserted = 0;
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray((cvArray));

            // if the database is empty, then do a bulkInsert,
            // if not get the record from the CP, and either update or insert
            Cursor rows = getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI, null, null, null, null, null);
            int rowCount = rows.getCount();
            rows.close();
            if (rowCount > 0) {
            }
            inserted = getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);
            Log.d(TAG, "SavingMovieData complete. " + inserted + " inserted into CP");

//                for ( ContentValues cv : cvArray ) {
//                    int movieId = cv.getAsInteger(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
//                    String[] whereArgs = new String[1];
//                    whereArgs[0] = Integer.toString(movieId);
//                    Cursor data =  getContentResolver().query(MovieContract.MovieEntry.buildMovieUriByMovieId(movieId), null, MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?", whereArgs, null);
//                    if (data != null) {
//                   //     int idx = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
//                        //Log.d(TAG, "movie id  '" + movieId + "' exists in the CP");
//                        inserted = getContentResolver().update(MovieContract.MovieEntry.buildMovieUriByMovieId(movieId), cv, MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?", whereArgs);
//                        //Log.d(TAG, "updated record" + inserted);
//                    } else {
//                        //Log.d(TAG, "movie id '" + movieId + " ' does not exist in CP");
//                        Uri record = getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, cv);
//                        //Log.d(TAG, "inserted record" + record.toString());
//                    }
//                    data.close();
//                }
//
//            } else {
//                inserted = getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);
//                //Log.d(TAG, "SavingMovieData complete. " + inserted + " inserted into CP");
//            }

        }

    }
}
