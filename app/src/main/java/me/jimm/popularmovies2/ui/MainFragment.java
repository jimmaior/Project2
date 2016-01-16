package me.jimm.popularmovies2.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;

import me.jimm.popularmovies2.MovieListAdapter;
import me.jimm.popularmovies2.PresenterManager;
import me.jimm.popularmovies2.R;
import me.jimm.popularmovies2.models.Movie;
import me.jimm.popularmovies2.presenters.MovieListPresenter;
import me.jimm.popularmovies2.views.MovieListView;


public class MainFragment extends Fragment implements
        MovieListView,
        AdapterView.OnItemClickListener{

    private MovieListPresenter mMovieListPresenter;
   // private PresenterManager mPresenterManager;
    private static final String TAG = MainFragment.class.getSimpleName();

    private GridView mGridView;
    private MovieListAdapter mMovieListAdapter;
    private ProgressBar mProgressBar;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        if (savedInstanceState != null) {


            mMovieListPresenter = PresenterManager.getInstance().restorePresenter(savedInstanceState);
//            if (savedInstanceState.containsKey(PERSIST_MOVIE_LIST)) {
//                mMovies = savedInstanceState.getParcelableArrayList(PERSIST_MOVIE_LIST);
//            } else {
//               mMovies = new ArrayList();
//            }
//            if (savedInstanceState.containsKey(PERSIST_SORT_ORDER)) {
//                mCurrentSortOrder = savedInstanceState.getString(PERSIST_SORT_ORDER);
//            } else {
//                mCurrentSortOrder = getSortOrderPreference();
//            }

        } else {

            mMovieListPresenter = new MovieListPresenter(getContext());
            mMovieListPresenter.setView(this);
         }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View v =  inflater.inflate(R.layout.fragment_main, container, false);

        // progressbar
        mProgressBar  = (ProgressBar) v.findViewById(R.id.progress_bar);
        mProgressBar.setIndeterminate(true);


        // grid
        mMovieListAdapter = new MovieListAdapter(getActivity(), mMovieListPresenter.getMovies());
        mGridView = (GridView) v.findViewById(R.id.movies_grid_view);
        mGridView.setAdapter(mMovieListAdapter);
        mGridView.setOnItemClickListener(this);


        // register gridview as a scroll listener
        mGridView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemCount) {
                Log.d(TAG, "onLoadMore");
                mMovieListPresenter.setLastPage(page);
                mMovieListPresenter.updateView();
                return true;
            }
        });

        return v;
    }

    public void showLoading(boolean visible) {
        Log.d(TAG, "showLoading");
        if (visible == true) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        else {mProgressBar.setVisibility(View.GONE);}
    }

    public void showMovieList(ArrayList<Movie> movies) {
        Log.d(TAG, "showMovieList: movies size=" + movies.size());
        // show the data
        mMovieListAdapter.notifyDataSetChanged();
    }

    public void showEmptyList() {
        Log.d(TAG, "showEmptyList");
    }

    @Override
    public void onPause() {
        super.onPause();
        mMovieListPresenter.unbindView();
        Log.d(TAG, "onPause" +
                "; mCurrentSortOrder=" + mMovieListPresenter.getCurrentSortOrder()+
                "; getSortOrderPreference()=" + mMovieListPresenter.getCurrentSortOrderPreference());

    }

    @Override
    public void onResume() {
        super.onResume();
        mMovieListPresenter.bindView(this);

        Log.d(TAG, "onResume" +
                "; mCurrentSortOrder=" + mMovieListPresenter.getCurrentSortOrder()+
                "; getSortOrderPreference()=" + mMovieListPresenter.getCurrentSortOrderPreference());
        mMovieListPresenter.updateView();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        Log.d(TAG, "onSaveInstanceState()");
        PresenterManager.getInstance().savePresenter(mMovieListPresenter, bundle);
    }

    // TODO what is this id parameter?
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Log.d(TAG, "onItemClick - Movie details at position: " + position);

        Movie movie = mMovieListPresenter.onItemClicked(position);
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra("movie", movie);
        startActivity(intent);
    }

}