package me.jimm.popularmovies2.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntegerRes;

/**
 * Created by jimmaior on 11/30/15.
 */
public class Movie implements Parcelable{

    // constants
    private final static String KEY_ID = "ID";
    private final static String KEY_TITLE = "TITLE";
    private final static String KEY_USER_RATING = "USER_RATING";
    private final static String KEY_POPULARITY = "POPULARITY";
    private final static String KEY_POSTER_PATH = "POSTER_PATH";
    private final static String KEY_RELEASE_DATE = "RELEASE_DATE";
    private final static String KEY_OVERVIEW = "OVERVIEW";

    // members
    private int mId;
    private String mTitle;
    private double mUserRating;
    private double mPopularity;
    private String mPosterPath;
    private String mReleaseDate;
    private String mOverview;

    public Movie() {}

    public Movie(int id, String title, double rating, double popularity, String posterPath, String releaseDate, String plot) {
        this.mId = id;
        this.mTitle = title;
        this.mUserRating = rating;
        this.mPopularity = popularity;
        this.mPosterPath = posterPath;
        this.mReleaseDate = releaseDate;
        this.mOverview = plot;
    }

    public Movie(Parcel in) {

        if (in != null) {
            Bundle b = in.readBundle();
            this.mId = b.getInt(KEY_ID);
            this.mTitle = b.getString(KEY_TITLE);
            this.mOverview = b.getString(KEY_OVERVIEW);
            this.mPopularity = b.getDouble(KEY_POPULARITY);
            this.mPosterPath = b.getString(KEY_POSTER_PATH);
            this.mReleaseDate = b.getString(KEY_RELEASE_DATE);
            this.mUserRating = b.getDouble(KEY_USER_RATING);
        }

    }

    public int getMovieId() {
        return mId;
    }

    public void setMovieId(int movieId) {
        mId = movieId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public double getPopularity() {
        return mPopularity;
    }

    public void setPopularity(double popularity) {
        this.mPopularity = popularity;
    }

    public double getUserRating() {
        return mUserRating;
    }

    public void setUserRating(double userRating) {
        mUserRating = userRating;
    }

    public String getPosterPath() {
        return mPosterPath;
    }

    public void setPosterPath(String posterPath) {
        mPosterPath = posterPath;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        mReleaseDate = releaseDate;
    }

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(String overview) {
        mOverview = overview;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel destination, int flag) {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_ID,this.getMovieId());
        bundle.putString(KEY_TITLE,this.getTitle());
        bundle.putDouble(KEY_USER_RATING, this.getUserRating());
        bundle.putDouble(KEY_POPULARITY, this.getPopularity());
        bundle.putString(KEY_POSTER_PATH, getPosterPath());
        bundle.putString(KEY_RELEASE_DATE, this.getReleaseDate());
        bundle.putString(KEY_OVERVIEW, this.getOverview());
        destination.writeBundle(bundle);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
