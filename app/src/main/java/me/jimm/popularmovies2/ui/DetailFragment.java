package me.jimm.popularmovies2.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.jimm.popularmovies2.R;
import me.jimm.popularmovies2.Utils;
import me.jimm.popularmovies2.data.MovieContract;
import me.jimm.popularmovies2.models.MovieService;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = DetailFragment.class.getSimpleName();

    private static final int MOVIE_DETAIL_LOADER = 2;
    static final String DETAIL_URI = "URI";

    private static final String[] MOVIE_COLUMNS  = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_FAVORITE,
            MovieContract.MovieEntry.COLUMN_BACKDROP_PATH,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_POPULARITY,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_VOTE_COUNT,
    };

    // these indices are tied to MOVIE_COLUMNS.
    // If MOVIE_COLUMNS changes, these must change too
    static final int COL__ID = 0;
    static final int COL_FAVORITE = 1;
    static final int COL_BACKDROP_PATH = 2;
    static final int COL_MOVIE_ID = 3;
    static final int COL_OVERVIEW = 4;
    static final int COL_POPULARITY = 5;
    static final int COL_POSTER_URL = 6;
    static final int COL_RELEASE_DATE = 7;
    static final int COL_TITLE = 8;
    static final int COL_VOTE_AVERAGE = 9;
    static final int COL_VOTE_COUNT = 10;

    // members
    private int mMovieId;
    private Uri mMovieDtlByMovieIdUri;

    private TextView mTvTitle;
    private ImageView mIvPoster;
    private TextView mTvReleaseDate;
    private TextView mTvRating;
    private TextView mTvOverview;
    private CheckBox mCbFavorite;
    private LinearLayout mLlReviews;
    private LinearLayout mLlTrailers;

    // default constructor is required
    public DetailFragment() {}

    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        // initialize fragment members
        mMovieId = getArguments().getInt("MOVIE_ID");
        mMovieDtlByMovieIdUri = getArguments().getParcelable(DETAIL_URI);

        // Loader
        getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, this);
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
        mCbFavorite = (CheckBox) v.findViewById(R.id.cb_favorite);

        // Favorites Checkbox
        mCbFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d(TAG, "compoundButton state:" + compoundButton.isChecked() + "; b:" + b);
                String[] args = new String[1];
                args[0] = Integer.toString(mMovieId);
                ContentValues contentValues = new ContentValues();
                contentValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, b);
                // TODO: Save the Movie Data to the CP as a Favorite
                getActivity().getContentResolver().update(MovieContract.MovieEntry.buildMovieUriUpdateFavoriteByMovieId(mMovieId),
                        contentValues, MovieContract.MovieEntry.COLUMN_MOVIE_ID + "= ?", args);
            }
        });

        // Trailers
        mLlTrailers = (LinearLayout) v.findViewById(R.id.ll_trailers);

        // reviews
        mLlReviews = (LinearLayout) v.findViewById(R.id.ll_reviews);

        return v;
    }

    public Loader<Cursor> onCreateLoader(int loader, Bundle args) {
        Log.d(TAG, "onLoadCreated");
        Uri uri;
        String[] whereArgs = new String[1];
        whereArgs[0] = Integer.toString(mMovieId);
        uri = mMovieDtlByMovieIdUri;
        Log.d(TAG, "uri:" + uri);
        return new CursorLoader(getContext(), uri, MOVIE_COLUMNS, MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?", whereArgs, null);
        //return new CursorLoader(getContext(), uri, MOVIE_COLUMNS, null, null, null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    Log.d(TAG, "onLoadFinished data.moveToFirst():" + data.moveToFirst());

        if (data.moveToFirst()) {

            // Movie Title
            mTvTitle.setText(data.getString(COL_TITLE));

            // Poster
            Picasso.with(getActivity())
                    .load(data.getString(COL_POSTER_URL))
                    .placeholder(R.drawable.ic_photo_white_48dp)
                    .into(mIvPoster);

            // Release Date
            mTvReleaseDate.setText(formatReleaseDate(data.getString(COL_RELEASE_DATE)));

            // Rating
            mTvRating.setText(formatRating(data.getDouble(COL_VOTE_AVERAGE)));

            // Overview
            mTvOverview.setText(data.getString(COL_OVERVIEW));

            // Favorite
            int isFavorite = data.getInt(COL_FAVORITE);
            if (isFavorite == 1) {
                mCbFavorite.setChecked(true);
            } else if (isFavorite == 0) {
                mCbFavorite.setChecked(false);
            } else {
                // do nothing
            }

            // Reviews
            int movieIdColumnIdx = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
            int movieId = data.getInt(movieIdColumnIdx);
            final String[] whereArgs = new String[1];
            whereArgs[0] = Integer.toString(movieId);
            Cursor reviews = getActivity().getContentResolver().query(
                    MovieContract.MovieReview.buildReviewUriByMovieId(movieId),
                    null, MovieContract.MovieReview.COLUMN_MOVIE_ID + "= ?", whereArgs, null);
            //DatabaseUtils.dumpCursor(reviews);
            if (reviews != null) {
                int reviewCount = reviews.getCount();
                reviews.moveToFirst();
                for (int i = 0; i < reviewCount; i++, reviews.moveToNext()) {
                    int contentIdx = reviews.getColumnIndex(MovieContract.MovieReview.COLUMN_CONTENT);
                    // TODO: Add Authors to the Review View
                    int authorIdx = reviews.getColumnIndex(MovieContract.MovieReview.COLUMN_AUTHOR);
                    TextView reviewTxtVw = new TextView(getActivity());
                    reviewTxtVw.setText(reviews.getString(contentIdx));
                    reviewTxtVw.setTextColor(Color.WHITE);
                    reviewTxtVw.setId(i);
                    mLlReviews.addView(reviewTxtVw);
                }
                reviews.close();
            }

            // Trailers
            Cursor videos = getActivity().getContentResolver().query(
                    MovieContract.MovieVideo.buildTrailerUriByMovieId(movieId),
                    null, MovieContract.MovieVideo.COLUMN_MOVIE_ID + "=?", whereArgs, null);
            //DatabaseUtils.dumpCursor(videos);
            if (videos != null) {
                int videoCount = videos.getCount();
                videos.moveToFirst();
                for (int i = 0; i < videoCount; i++, videos.moveToNext()) {
                    int typeIdx = videos.getColumnIndex(MovieContract.MovieVideo.COLUMN_TYPE);
                    String typeStr = videos.getString(typeIdx);
                    if (typeStr.equals("Trailer")) {
                        // New TrailerViewGroup
                        RelativeLayout trailerLayout = new RelativeLayout(getActivity());
                        // ImageView
                        ImageView trailerIv = new ImageView(getActivity());
                        int keyIdx = videos.getColumnIndex(MovieContract.MovieVideo.COLUMN_KEY);
                        String videoKey = videos.getString(keyIdx);
                        String url = "http://img.youtube.com/vi/" +
                                videoKey
                                + "/mqdefault.jpg";
                        Picasso.with(getActivity())
                                .load(url)
                                .placeholder(R.drawable.placeholder107x60)
                                .into(trailerIv);
                        trailerIv.setId(i);
                        trailerIv.setPadding(4,4,4,4);
                        trailerIv.setTag(R.id.trailer_key, videoKey);

                        // image button
                        ImageButton playTrailerBtn = new ImageButton(getActivity());
                        playTrailerBtn.setId(i);

                        RelativeLayout.LayoutParams layoutParams = new
                                RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT
                        );

                        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                        playTrailerBtn.setLayoutParams(layoutParams);

                        playTrailerBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.d(TAG, "Trailer onClick- clicked on ID:" + view.getId());
                                // the click occurs on the ImageButton, but the tag is located on the
                                // sibling ImageView
                                ViewParent parent = view.getParent();
                                ViewGroup viewGroup = (ViewGroup)  parent;
                                ImageView taggedIv = (ImageView) viewGroup.findViewById(view.getId());
                                if (taggedIv != null) {
                                    String key = (String) taggedIv.getTag(R.id.trailer_key);
                                    String url = "https://www.youtube.com/watch?v=" + key;
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                                }
                            }
                        });

                        trailerLayout.addView(trailerIv);
                        trailerLayout.addView(playTrailerBtn);

                        mLlTrailers.addView(trailerLayout);

                    }
                }
                videos.close();
            }

        }   // end if
    }

    public void onLoaderReset(Loader<Cursor> loader) {

        Log.d(TAG, "onLoaderReset");
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
