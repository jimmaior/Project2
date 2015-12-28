package me.jimm.popularmovies2;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import me.jimm.popularmovies2.model.Movie;
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


public class MovieDbApiService extends IntentService {

    // public members
    public static final int STATUS_RUNNING = 100;
    public static final int STATUS_ERROR = -100;
    public static final int STATUS_FINISHED = 200;
    public static final String MOVIE_API_PARAM_SORT_BY_POPULARITY = "popularity.desc";
    public static final String MOVIE_API_PARAM_SORT_BY_RATING = "vote_average.desc";


    // private members
    private static final String TAG = MovieDbApiService.class.getSimpleName();

    // JSON fields
    private static final String MOVIE_API_RESULTS = "results";
    private static final String MOVIE_API_RESULTS_ID = "id";
    private static final String MOVIE_API_RESULTS_POPULARITY = "popularity";
    private static final String MOVIE_API_RESULTS_TITLE = "original_title";
    private static final String MOVIE_API_RESULTS_RELEASE_DATE = "release_date";
    private static final String MOVIE_API_RESULTS_RATING = "vote_average";
    private static final String MOVIE_API_RESULTS_PLOT = "overview";
    private static final String MOVIE_API_RESULTS_POSTER_PATH = "poster_path";


    // API Parameters ////////////////////////////////////////////////////////

    // full poster path
    private static final String MOVIE_IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String MOVIE_VOTE_COUNT_GTE_VALUE = "10";
    //private static final String MOVIE_IMAGE_SIZE = "w185/";
    //private static final String MOVIE_IMAGE_SIZE = "w342/"; // seems better
    private static final String MOVIE_IMAGE_SIZE = "w500/"; // seems better
    //private static final String MOVIE_IMAGE_SIZE = "original/"; // seems better



    public MovieDbApiService() {
        super("MovieService");
    }

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent()");
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");

        String command = intent.getStringExtra("command");
        Bundle bundle = new Bundle();
        if (command.equals("get_movie_data")) {
            receiver.send(STATUS_RUNNING, bundle.EMPTY);
            try {
                String json = handleActionFetchPopularMovies(intent);
                ArrayList results = createMovieListArray(json);
                bundle.putParcelableArrayList("results", results);
                receiver.send(STATUS_FINISHED, bundle);
            } catch (Exception e) {
                bundle.putString(Intent.EXTRA_TEXT, e.toString());
                receiver.send(STATUS_ERROR, bundle);
            }
        }
    }

    @Nullable
    private String handleActionFetchPopularMovies(Intent intent) {
        Log.d(TAG, "handleActionFetchPopularMovies()");

        // need to be declared outside of the try so they can be closed in the finally block
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String popularMoviesJsonString = null;

        try {
            // construct the URL
            final String BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
            final String SORT_BY_PARAM = "sort_by";
            final String PAGE_PARAM = "page";
            final String API_KEY_PARAM = "api_key";
            final String VOTE_COUNT_GTE  = "vote_count.gte";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_BY_PARAM, intent.getStringExtra("sort_by"))
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
                popularMoviesJsonString = buffer.toString();
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

        return popularMoviesJsonString;
    }


    private ArrayList createMovieListArray(String jsonString){
        Log.d(TAG, "createMovieListArray");

        ArrayList<Movie> movieList = new ArrayList<Movie>();

        try {
            JSONObject movieListJson = new JSONObject(jsonString);
            JSONArray movieArray = movieListJson.getJSONArray(MOVIE_API_RESULTS);
            for(int i=0; i < movieArray.length(); i++){
                JSONObject movieJsonObject = movieArray.getJSONObject(i);
                Movie movie = new Movie();

                movie.setMovieId(movieJsonObject.getInt(MOVIE_API_RESULTS_ID));
                movie.setTitle(movieJsonObject.getString(MOVIE_API_RESULTS_TITLE));
                movie.setReleaseDate(movieJsonObject.getString(MOVIE_API_RESULTS_RELEASE_DATE));
                movie.setUserRating(movieJsonObject.getDouble(MOVIE_API_RESULTS_RATING));
                movie.setOverview( movieJsonObject.getString(MOVIE_API_RESULTS_PLOT));
                movie.setPosterPath(
                        MOVIE_IMAGE_BASE_URL +
                                MOVIE_IMAGE_SIZE +
                        movieJsonObject.getString(MOVIE_API_RESULTS_POSTER_PATH));
                movie.setPopularity( movieJsonObject.getDouble(MOVIE_API_RESULTS_POPULARITY));

                movieList.add(movie);
            }

        } catch (JSONException je) {
            Log.e(TAG, "Error parsing the JSON string" + je.getMessage());
        }
        return movieList;
    }
}
