package com.example.android.popularmovies_project.Loaders;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.android.popularmovies_project.Models.Movie;
import com.example.android.popularmovies_project.Utils.NetworkUtils;
import com.example.android.popularmovies_project.Utils.QueryUtils;

import java.util.ArrayList;
import java.util.List;

public class MoviesLoader extends AsyncTaskLoader<Object> {

    private static final String LOG_TAG = MoviesLoader.class.getSimpleName();
    private String queryUrl;
    private List<Movie> mMovieList;

    public MoviesLoader(@NonNull Context context, String requestUrl) {
        super(context);
        queryUrl = requestUrl;
        onContentChanged();
    }

    @Nullable
    @Override
    public Object loadInBackground() {
        if (queryUrl == null) {
            return null;
        }

        if (!NetworkUtils.hasInternetConnection(getContext())) {
            // return Boolean indicating there's no intenet connection
            return false;
        }

        List<Movie> allMovies = QueryUtils.fetchMovieList(queryUrl);
        mMovieList = moviesContainingPosters(allMovies);
        return mMovieList;
    }

    @Override
    protected void onStartLoading() {
        // If loader already contains data, don't make any call to server
        // and just deliver the existing data with/without customized
        // else force it to load
        if (mMovieList != null) {
            Log.d(LOG_TAG, "Delivering existing data");
            deliverResult(mMovieList);
        } else if (takeContentChanged()) {
            // You can remove the handler later, its just for testing purposes
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(LOG_TAG, "Going to call the server request");
                    forceLoad();
                }
            }, 100);
        }
    }


    /**
     * Remove all the {@link Movie} which doesn't have any posters.
     * Use this method if you want the only movies which contains images.
     *
     * @param movieList is the list of {@link Movie} which includes both
     *                  non-poster and poster images {@link Movie} objects.
     * @return the list of {@link Movie}, which only has the Movie objects containing
     * poster images or null if the list received as argument is null
     */
    private List<Movie> moviesContainingPosters(List<Movie> movieList) {
        if (movieList == null) {
            Log.d("MovieLoader", "movielist comes null but will be returned as empty list");
            return new ArrayList<>();
        } else {
            int moviesRemoved = 0;
            for (int i = 0; i < movieList.size(); i++) {
                if (movieList.get(i).getPosterPath().equals("null")) {
                    moviesRemoved++;

                    movieList.remove(i);
                    // This is required because after deleting an item from the list this way with indices,
                    // the next element would take the place or index of the current one which was deleted
                    // and hence we would end up skipping the next element to the item to be removed
                    // bcz it is going to take the index of the removed one.
                    i--;
                }
            }
            Log.d(LOG_TAG, "Deleted " + moviesRemoved + " movies from this list which doesn't contain any poster images");
            return movieList;
        }
    }
}
