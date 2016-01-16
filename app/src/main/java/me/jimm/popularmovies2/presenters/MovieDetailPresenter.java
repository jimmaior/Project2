package me.jimm.popularmovies2.presenters;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.jimm.popularmovies2.models.Movie;
import me.jimm.popularmovies2.views.MovieDetailView;

/**
 * Created by generaluser on 1/2/16.
 */
public class MovieDetailPresenter {

    private static final String TAG = MovieDetailPresenter.class.getSimpleName();
    private MovieDetailView mMovieDetailView;
    private Movie mMovie;

    public MovieDetailPresenter(Movie movie){
        if (mMovie == null) {
            mMovie = movie;
        }
    }

    public void setView(MovieDetailView view) {
        if (view != null) {
            mMovieDetailView = view;
        }
    }

    public void loadMovieDetails() {
        mMovieDetailView.showMovieTitle();
        mMovieDetailView.showOverview();
        mMovieDetailView.showPoster();
        mMovieDetailView.showRating();
        mMovieDetailView.showReleaseDate();
    }

    public String getMovieTitle(){
        return mMovie.getTitle();
    }

    public String getPosterPath(){
        return mMovie.getPosterPath();
    }

    public String getReleaseDate() {
        return formatReleaseDate(mMovie.getReleaseDate());
    }

    public String getRating() {
        return formatRating(mMovie.getUserRating());
    }

    public String getOverview() {
        return  mMovie.getOverview();
    }

    // private methods
    /**
     * converts the user ratings provided by the API as a double
     * into a single digit formatted as a string
     * */
    private String formatRating(double rating) {
        String formattedRating;
        formattedRating = Long.toString(Math.round(rating));
        return formattedRating;
    }

    /**
     * converts a string date in the form of yyyy-mm-dd to yyyy
     * */
    private String formatReleaseDate(String sDate) {
        String year = null;
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        try {
            Date date = yearFormat.parse(sDate);
            year = yearFormat.format(date);

        } catch (java.text.ParseException pe) {
            Log.e(TAG, pe.getMessage());
        }
        return year;
    }
}
