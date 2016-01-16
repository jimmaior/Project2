package me.jimm.popularmovies2.ui;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import me.jimm.popularmovies2.R;
import me.jimm.popularmovies2.models.Movie;
import me.jimm.popularmovies2.presenters.MovieDetailPresenter;
import me.jimm.popularmovies2.views.MovieDetailView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements MovieDetailView {

    private static final String TAG = DetailFragment.class.getSimpleName();
    private MovieDetailPresenter mMovieDetailPresenter;

    // members
    private TextView mTvTitle;
    private ImageView mIvPoster;
    private TextView mTvReleaseDate;
    private TextView mTvRating;
    private TextView mTvOverview;


    public DetailFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");
        Bundle b = getArguments();
        Movie movie = b.getParcelable("movie");
        mMovieDetailPresenter = new MovieDetailPresenter(movie);
        mMovieDetailPresenter.setView(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_detail, container, false);

        mTvTitle = (TextView) v.findViewById(R.id.tv_title);
        mIvPoster = (ImageView) v.findViewById(R.id.iv_poster);
        mTvReleaseDate = (TextView) v.findViewById(R.id.tv_release_date);
        mTvRating = (TextView) v.findViewById(R.id.tv_user_rating);
        mTvOverview = (TextView) v.findViewById(R.id.tv_overview);

        mMovieDetailPresenter.loadMovieDetails();
        return v;
    }

    @Override
    public void showMovieTitle() {
        mTvTitle.setText(mMovieDetailPresenter.getMovieTitle());
    }

    @Override
    public void showPoster(){

        Picasso.with(getActivity())
                .load(mMovieDetailPresenter.getPosterPath())
                .into(mIvPoster);
    }

    @Override
    public void showReleaseDate(){
        mTvReleaseDate.setText(mMovieDetailPresenter.getReleaseDate());
    }

    @Override
    public void showRating(){
        mTvRating.setText(mMovieDetailPresenter.getRating());
    }

    @Override
    public void showOverview(){
        mTvOverview.setText(mMovieDetailPresenter.getOverview());
    }



}
