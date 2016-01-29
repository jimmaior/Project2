package me.jimm.popularmovies2.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jimmaior on 11/30/15.
 */
public class Movie implements Parcelable{


    // members
    private int mMovieId;
    private String mTitle;
    private boolean mFavorite;
    private double mPopularity;
    private String mPosterPath;
    private String mBackdropPath;
    private String mReleaseDate;
    private String mOverview;
    private double mVoterAverage;
    private int mVoterCount;

    // Parcelable Constants
    private final static String KEY_MOVIE_ID = "MOVIE_ID";
    private final static String KEY_TITLE = "TITLE";
    private final static String KEY_FAVORITE = "FAVORITE";
    private final static String KEY_VOTER_AVERAGE = "VOTER_AVERAGE";
    private final static String KEY_VOTER_COUNT = "VOTER_COUNT";
    private final static String KEY_POPULARITY = "POPULARITY";
    private final static String KEY_POSTER_PATH = "POSTER_PATH";
    private final static String KEY_BACKDROP_PATH = "BACKDROP_PATH";
    private final static String KEY_RELEASE_DATE = "RELEASE_DATE";
    private final static String KEY_OVERVIEW = "OVERVIEW";


    public Movie() {}

    public Movie(int movieId, String title, double voterAverage, double popularity, String posterPath, String releaseDate, String overview, int voterCount, String backdropPath) {
        this.mMovieId = movieId;
        this.mTitle = title;
        this.mVoterAverage = voterAverage;
        this.mPopularity = popularity;
        this.mPosterPath = posterPath;
        this.mReleaseDate = releaseDate;
        this.mOverview = overview;
        this.mVoterCount = voterCount;
        this.mBackdropPath = backdropPath;
    }

    public Movie(Parcel in) {

        if (in != null) {
            Bundle b = in.readBundle();
            this.setMovieId(b.getInt(KEY_MOVIE_ID));
            this.setTitle(b.getString(KEY_TITLE));
            this.setOverview(b.getString(KEY_OVERVIEW));
            this.setPopularity(b.getDouble(KEY_POPULARITY));
            this.setPosterPath(b.getString(KEY_POSTER_PATH));
            this.setReleaseDate(b.getString(KEY_RELEASE_DATE));
            this.setVoterAverage(b.getDouble(KEY_VOTER_AVERAGE));
            this.setBackdropPath(b.getString(KEY_BACKDROP_PATH));
            this.setVoterCount(b.getInt(KEY_VOTER_COUNT));
            this.setFavorite(b.getBoolean(KEY_FAVORITE));

        }

    }

    public int getVoterCount() {
        return mVoterCount;
    }

    public void setVoterCount(int voterCount) {
        mVoterCount = voterCount;
    }

    public boolean isFavorite() {
        return mFavorite;
    }

    public void setFavorite(boolean favorite) {
        mFavorite = favorite;
    }

    public void setFavorite() {
        mFavorite = false;
    }

    /** Sqlite uses int for boolean, 0 = false, 1=true */
    public void setFavorite(int favorite) throws Exception {
        if (favorite == 0) {mFavorite = false;}
        else if (favorite == 1) {mFavorite = true;}
        else {throw new Exception("Error: Cannot resolve movie 'favorite' ");}
    }

    public String getBackdropPath() {
        return mBackdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        mBackdropPath = backdropPath;
    }

    public int getMovieId() {
        return mMovieId;
    }

    public void setMovieId(int movieId) {
        mMovieId = movieId;
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

    public double getVoterAverage() { return mVoterAverage; }

    public void setVoterAverage(double userRating) {
        mVoterAverage = userRating;
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
        bundle.putInt(KEY_MOVIE_ID,this.getMovieId());
        bundle.putString(KEY_BACKDROP_PATH, this.getBackdropPath());
        bundle.putBoolean(KEY_FAVORITE, this.isFavorite());
        bundle.putInt(KEY_VOTER_COUNT, this.getVoterCount());
        bundle.putString(KEY_TITLE,this.getTitle());
        bundle.putDouble(KEY_VOTER_AVERAGE, this.getVoterAverage());
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
