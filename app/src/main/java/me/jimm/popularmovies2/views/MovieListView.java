package me.jimm.popularmovies2.views;

import java.util.ArrayList;

import me.jimm.popularmovies2.models.Movie;

/**
 * Created by generaluser on 12/30/15.
 */
public interface MovieListView {

    void showMovieList(ArrayList<Movie> m);

    void showEmptyList();

    void showLoading(boolean b);


}
