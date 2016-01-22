package me.jimm.popularmovies2;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.squareup.picasso.Picasso;

import me.jimm.popularmovies2.models.Movie;

/**
 * Created by generaluser on 1/2/16.
 */
public class MovieListAdapter extends BaseAdapter{

    private final String TAG = MovieListAdapter.class.getSimpleName();
    private Context mContext;
    private List<Movie> mMovieList;


    public MovieListAdapter(Context context, ArrayList movies) {
        Log.d(TAG, "MovieListAdapter");
        mContext = context;
        if (mMovieList != null) {
            mMovieList = new ArrayList<>();
            mMovieList = movies;
        } else {
            mMovieList = movies;
        }
    }

    public int getCount() {
        return mMovieList.size();
    }


    public Movie getItem(int position){
        if (mMovieList != null) {
            return mMovieList.get(position);
        }
        else {
            return null;
        }
    }

    public long getItemId(int position) {
        return mMovieList.get(position).getMovieId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Movie movie = getItem(position);
        Log.d(TAG, "movie_id=" + movie.getMovieId());

        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setAdjustViewBounds(true);
            imageView.setPadding(4, 4, 4, 4);
        } else {
            imageView = (ImageView) convertView;
        }

        // using a placeholder really make the app run much smoother
        Picasso.with(mContext)
                .load(movie.getPosterPath())
                .placeholder(R.drawable.ic_photo_white_48dp)
                .into(imageView);

        return imageView;
    }

    public void clearAll() {
        mMovieList.clear();
    }

    public void add(Collection<Movie> movies) {
        mMovieList.addAll(movies);
        notifyDataSetChanged();
    }
}
