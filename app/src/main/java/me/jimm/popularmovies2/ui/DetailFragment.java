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

import java.text.SimpleDateFormat;
import java.util.Date;

import me.jimm.popularmovies2.R;
import me.jimm.popularmovies2.model.Movie;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    private static final String TAG = DetailFragment.class.getSimpleName();

    // members
    private Movie mMovie;
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
        mMovie = b.getParcelable("movie");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_detail, container, false);

        mTvTitle = (TextView) v.findViewById(R.id.tv_title);
        mTvTitle.setText(mMovie.getTitle());

        mIvPoster = (ImageView) v.findViewById(R.id.iv_poster);
        Picasso.with(getActivity())
                .load(mMovie.getPosterPath())
                .into(mIvPoster);

        mTvReleaseDate = (TextView) v.findViewById(R.id.tv_release_date);
        mTvReleaseDate.setText(formatReleaseDate(mMovie.getReleaseDate()));

        mTvRating = (TextView) v.findViewById(R.id.tv_user_rating);
        mTvRating.setText(formatRating(mMovie.getUserRating()));

        mTvOverview = (TextView) v.findViewById(R.id.tv_overview);
        mTvOverview.setText(mMovie.getOverview());

        return v;
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
