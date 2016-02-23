package me.jimm.popularmovies2.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.jimm.popularmovies2.R;
import me.jimm.popularmovies2.data.MovieContract;

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

    // MOVIE_COLUMNS indices.
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
    private int mMovieId = 0;
    private Uri mMovieDtlByMovieIdUri = null;

    private TextView mTvTitle;
    private ImageView mIvPoster;
    private TextView mTvReleaseDate;
    private TextView mTvRating;
    private TextView mTvOverview;
    private CheckBox mCbFavorite;
    private LinearLayout mLlReviews;
    private LinearLayout mLlTrailers;
    private ShareActionProvider mShareActionProvider;

    public interface Callback {
        void onFavoriteClick(int movieId, boolean checkState);
    }

    // default constructor is required
    public DetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // has Action items on ActionBar
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_detail, menu);

        // initialize the share action provider
        MenuItem shareMenuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
       int itemId = item.getItemId();

        switch (itemId) {
            case (R.id.action_share): {
                Log.d(TAG, "onOptionItemSelected - Action_Share");
                shareMovieUrl();
            break;
            }
            default: {
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        // initialize fragment members
        if (getArguments() != null ) {
            mMovieId = getArguments().getInt("MOVIE_ID");
            mMovieDtlByMovieIdUri = getArguments().getParcelable(DETAIL_URI);
        } else {
            mMovieId = 0;
            mMovieDtlByMovieIdUri = MovieContract.MovieEntry.buildMovieUriByMovieId(mMovieId);
        }
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

        mCbFavorite.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                CompoundButton compoundButton = (CompoundButton) view;
                Log.d(TAG, "onCheckedChanged: compoundButton state:" + compoundButton.isChecked());
                ((Callback) getActivity()).onFavoriteClick(mMovieId, compoundButton.isChecked());
            }
        });

        // Trailers
        mLlTrailers = (LinearLayout) v.findViewById(R.id.ll_trailers);

        // reviews
        mLlReviews = (LinearLayout) v.findViewById(R.id.ll_reviews);

        return v;
    }

    public void updateDetailView(int movieId) {
        Log.d(TAG, "updateDetailView");
        mMovieId = movieId;
        mMovieDtlByMovieIdUri = MovieContract.MovieEntry.buildMovieUriByMovieId(mMovieId);
        getLoaderManager().restartLoader(MOVIE_DETAIL_LOADER, null, this);
    }

    public Loader<Cursor> onCreateLoader(int loader, Bundle args) {
        Log.d(TAG, "onCreateLoader");
        Uri uri;
        String[] whereArgs = new String[1];
        whereArgs[0] = Integer.toString(mMovieId);
        uri = mMovieDtlByMovieIdUri;
        Log.d(TAG, "creating a loader for uri:" + uri);
        return new CursorLoader(getContext(), uri, MOVIE_COLUMNS, MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?", whereArgs, null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    Log.d(TAG, "onLoadFinished data loaded?:" + data.moveToFirst());
    Log.d(TAG, "onLoadFinished data column count:" + data.getColumnCount());
        if (data.getColumnCount() > 0) {
            Log.d(TAG, "onLoadFinished data column name:" + data.getColumnName(0));
        }

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
                    null, MovieContract.MovieReview.COLUMN_MOVIE_ID + " = ?", whereArgs, null);
            if (reviews != null) {
                int reviewCount = reviews.getCount();
                reviews.moveToFirst();
                for (int i = 0; i < reviewCount; i++, reviews.moveToNext()) {
                    String content;
                    String author;
                    int contentIdx = reviews.getColumnIndex(MovieContract.MovieReview.COLUMN_CONTENT);
                    int authorIdx = reviews.getColumnIndex(MovieContract.MovieReview.COLUMN_AUTHOR);
                    author = reviews.getString(authorIdx) + " writes: ";
                    content = reviews.getString(contentIdx);
                    TextView authorTxtVw = new TextView(getActivity());
                    authorTxtVw.setText(author);
                    authorTxtVw.setTextColor(getResources().getColor(R.color.colorAccent));
                    TextView reviewTxtVw = new TextView(getActivity());
                    reviewTxtVw.setText(content);
                    reviewTxtVw.setTextColor(Color.WHITE);
                    reviewTxtVw.setId(i);
                    mLlReviews.addView(authorTxtVw);
                    mLlReviews.addView(reviewTxtVw);
                }
                reviews.close();
            }

            // Trailers
            Cursor videos = getActivity().getContentResolver().query(
                    MovieContract.MovieVideo.buildTrailerUriByMovieId(movieId),
                    null, MovieContract.MovieVideo.COLUMN_MOVIE_ID + " = ?", whereArgs, null);
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

            if (!data.isClosed()) {data.close(); }

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

    private String getVideoUrl(int position) {
        String key;
        if (mLlTrailers.getChildCount() > 0) {
            RelativeLayout trailerLayout = (RelativeLayout) mLlTrailers.getChildAt(position);
            //ImageView imageView = (ImageView) trailerLayout.findViewById(position);
            ImageView imageView = (ImageView) trailerLayout.getChildAt(position);
            key = imageView.getTag(R.id.trailer_key).toString();
            return "https://www.youtube.com/watch?v=" + key;
        } else {
            return null;
        }
    }

    private void shareMovieUrl() {
        Log.d(TAG, "shareMovieUrl");
        String url;
        url = getVideoUrl(0);
        if (url != null ) {
            Intent intent = new Intent();
            intent.setType("text/plain");
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, url);
            mShareActionProvider.setShareIntent(intent);
        } else {
            Toast.makeText(getActivity(), "Error: Could not retrieve the trailer url.", Toast.LENGTH_SHORT).show();
        }
    }

}
