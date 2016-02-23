package me.jimm.popularmovies2.models;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import me.jimm.popularmovies2.BuildConfig;
import me.jimm.popularmovies2.data.MovieContract.MovieEntry;
import me.jimm.popularmovies2.data.MovieContract.MovieReview;
import me.jimm.popularmovies2.data.MovieContract.MovieVideo;

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
    public static final String PENDING_RESULT = "pending_result";
    public static final String PENDING_MOVIE_ID = "pending_movie_id";
    public static final String RESULT = "result";
    public static final int RESULT_CODE = 1;

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

    private static final String VIDEO_API_KEY = "source";
    private static final String VIDEO_API_NAME = "name";
    private static final String VIDEO_API_TYPE = "type";
    private static final String VIDEO_API_SIZE = "size";
    private static final String VIDEO_API_YOUTUBE = "youtube";

    private static final String REVIEW_API_REVIEW_ID = "id";
    private static final String REVIEW_API_CONTENT = "content";
    private static final String REVIEW_API_AUTHOR = "author";
    private static final String REVIEW_API_URL = "url";

    // API Parameters ////////////////////////////////////////////////////////
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

        String command = intent.getStringExtra("command");
        Log.d(TAG, "onHandleIntent()-" + command);
        switch (command) {
            case "get_movie_data": {
                try {
                    String moviesJson = fetchMovies(intent);
                    ArrayList results = createMovieListArray(moviesJson);
                    saveMovies(results);
                } catch (Exception e) {
                    Log.e(TAG, "Error Occurred: " + e.getMessage());
                }
                break;
            }
            case "get_movie_detail_data": {
                try {
                    int movieId = intent.getIntExtra("movie_id", 0);
                    String movieDtlsJson = fetchMovieDetails(movieId);
                    saveMovieDetailData(movieDtlsJson);
                    PendingIntent reply = intent.getParcelableExtra(PENDING_RESULT);
                    reply.send(this, RESULT_CODE, intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error Occurred: " + e.getMessage());
                }
                break;
            }
            case "init_movie_data_and_detail": {
                try {
                    String moviesJson = fetchMovies(intent);
                    ArrayList results = createMovieListArray(moviesJson);
                    saveMovies(results);
                    int movieId = getMovieId(results, 0);
                    String movieDtlsJson = fetchMovieDetails(movieId);
                    saveMovieDetailData(movieDtlsJson);
                } catch (Exception e) {
                    e.getMessage();
                }
                break;
            }
            default: {
                Log.e(TAG, "Error: unsupported command '" + command + "'");
            }
        }
    }

    private int getMovieId(ArrayList movies, int position) {
        Log.d(TAG, "getMovieId");
        Movie firstMovie = (Movie) movies.get(position);
        return firstMovie.getMovieId();
    }

    @Nullable
    private String fetchMovies(Intent intent) {
        Log.d(TAG, "fetchMovies()");

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
            Log.e(TAG, "Error attempting to get the movie data." + ioe.getMessage(), ioe);
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
        Log.d(TAG, "fetchMovieDetails()-" + movieId);

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

    /**
     * parse the json string into video and review objects
     * @param jsonStringList json string representing a list of objects
     */
    private void saveMovieDetailData(String  jsonStringList) {
        Log.d(TAG, "saveMovieDetailData-" + jsonStringList);

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
                    movieReviewValues.put(MovieReview.COLUMN_MOVIE_ID, movieId);
                    movieReviewValues.put(MovieReview.COLUMN_AUTHOR, review.getString(REVIEW_API_AUTHOR));
                    movieReviewValues.put(MovieReview.COLUMN_CONTENT, review.getString(REVIEW_API_CONTENT));
                    movieReviewValues.put(MovieReview.COLUMN_REVIEW_ID, review.getString(REVIEW_API_REVIEW_ID));
                    movieReviewValues.put(MovieReview.COLUMN_URL, review.getString(REVIEW_API_URL));

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
                    movieTrailerValues.put(MovieVideo.COLUMN_MOVIE_ID, movieId);
                    movieTrailerValues.put(MovieVideo.COLUMN_NAME, video.getString(VIDEO_API_NAME));
                    movieTrailerValues.put(MovieVideo.COLUMN_TYPE, video.getString(VIDEO_API_TYPE));
                    movieTrailerValues.put(MovieVideo.COLUMN_KEY, video.getString(VIDEO_API_KEY));
                    movieTrailerValues.put(MovieVideo.COLUMN_SIZE, video.getString(VIDEO_API_SIZE));

                    // SAVE DATA TO CP
                    // either update or insert review data into ContentProvider
                    saveMovieVideoDtl(movieTrailerValues);
                }

            } catch (JSONException je) {
                Log.e(TAG, "Error parsing the Movie Details JSON String " + je.getMessage());
            }
    }

    /**
     * save content values to the database.  If the video key is absent, then save. Otherwise, ignore
     * @param values ContentValues to be persisted
     */
    private void saveMovieReviewDtl(ContentValues values) {
        Log.d(TAG, "saveMovieReviewDtl");
        if (values != null ) {
            String reviewId = values.getAsString(MovieReview.COLUMN_REVIEW_ID);
            String[] whereArgs = new String[1];
            whereArgs[0] = reviewId;
            // if the objects exists in db, skip
            Cursor row =  getContentResolver().query(
                    MovieReview.buildReviewUriByReviewId(reviewId),
                    null,
                    MovieReview.COLUMN_REVIEW_ID + " = ?",
                    whereArgs,
                    null
            );
            if (row != null && row.getCount() == 0 ) {
                getContentResolver().insert(MovieReview.CONTENT_URI, values);
            }
//          if (row != null) {row.close();}
        }
    }

    private void saveMovieVideoDtl(ContentValues values) {
        Log.d(TAG, "saveMovieVideoDtl");
        if (values != null) {
            String  key = values.getAsString(MovieVideo.COLUMN_KEY);
            String[] whereArgs = new String[1];
            whereArgs[0] = key;
            // if the objects exists in db, skip
            Cursor row =  getContentResolver().query(
                    MovieVideo.buildTrailerUriByKey(key),
                    null,
                    MovieVideo.COLUMN_KEY + " = ?",
                    whereArgs,
                    null
            );
            if (row != null && row.getCount() == 0) {
                getContentResolver().insert(MovieVideo.CONTENT_URI, values);
            }
//          if (row != null) {row.close();}
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

    private void saveMovies(ArrayList<Movie> movies) {
        Log.d(TAG, "saveMovies");

        Vector<ContentValues> cVVector = new Vector<>(movies.size());

        for (Movie movie : movies) {
            ContentValues movieValues = new ContentValues();
            movieValues.put(MovieEntry.COLUMN_MOVIE_ID, movie.getMovieId());
            movieValues.put(MovieEntry.COLUMN_FAVORITE, movie.isFavorite());
            movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
            movieValues.put(MovieEntry.COLUMN_TITLE, movie.getTitle());
            movieValues.put(MovieEntry.COLUMN_POPULARITY, movie.getPopularity());
            movieValues.put(MovieEntry.COLUMN_BACKDROP_PATH, movie.getBackdropPath());
            movieValues.put(MovieEntry.COLUMN_VOTE_COUNT, movie.getVoterCount());
            movieValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, movie.getVoterAverage());
            movieValues.put(MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
            movieValues.put(MovieEntry.COLUMN_OVERVIEW, movie.getOverview());

            cVVector.add(movieValues);
        }

        int inserted;
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray((cvArray));

            // if the database is empty, then do a bulkInsert,
            // if not loop through the data and update if the record exists, or insert it if it does not.
            Cursor rows = getContentResolver().query(MovieEntry.CONTENT_URI, null, null, null, null, null);
            int rowCount = rows.getCount();
            rows.close();
            if (rowCount > 0) {
                for ( ContentValues cv : cvArray ) {
                    int movieId = cv.getAsInteger(MovieEntry.COLUMN_MOVIE_ID);
                    String[] whereArgs = new String[1];
                    whereArgs[0] = Integer.toString(movieId);
                    Cursor data =  getContentResolver().query(MovieEntry.buildMovieUriByMovieId(movieId), null, MovieEntry.COLUMN_MOVIE_ID + " = ?", whereArgs, null);
                    if (data == null) {
                        Uri record = getContentResolver().insert(MovieEntry.CONTENT_URI, cv);
                    }
                    data.close();
                }
            } else {
                inserted = getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, cvArray);
                Log.d(TAG, "SavingMovieData complete. " + inserted + " inserted into CP");
            }
        }
    }
}
