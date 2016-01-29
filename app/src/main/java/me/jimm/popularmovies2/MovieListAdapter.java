package me.jimm.popularmovies2;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
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
public class MovieListAdapter extends CursorAdapter {

    private final String TAG = MovieListAdapter.class.getSimpleName();
    private Context mContext;


    public MovieListAdapter(Context context, Cursor cursor, int flags) {
        super (context, cursor, flags);
        Log.d(TAG, "MovieListAdapter");
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    // this is where we fill in the view with the contents of the cursor
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
//
//        ImageView imageView;
//        if (convertView == null) {
//            imageView = new ImageView(mContext);
//            imageView.setAdjustViewBounds(true);
//            imageView.setPadding(4, 4, 4, 4);
//        } else {
//            imageView = (ImageView) convertView;
//        }
//
//        // using a placeholder really make the app run much smoother
//        Picasso.with(mContext)
//                .load(movie.getPosterPath())
//                .placeholder(R.drawable.ic_photo_white_48dp)
//                .into(imageView);
//
    }
}
