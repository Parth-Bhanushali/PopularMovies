package com.example.android.popularmovies_project.Loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.android.popularmovies_project.Models.Review;
import com.example.android.popularmovies_project.Utils.QueryUtils;

import java.util.List;

public class ReviewsLoader extends AsyncTaskLoader<List<Review>> {

    private static final String LOG_TAG = ReviewsLoader.class.getSimpleName();
    private String queryUrl;
    private List<Review> mReviewList;

    public ReviewsLoader(@NonNull Context context, String requestUrl) {
        super(context);
        queryUrl = requestUrl;
    }

    @Nullable
    @Override
    public List<Review> loadInBackground() {
        if (queryUrl == null) {
            return null;
        }

        mReviewList = QueryUtils.fetchReviewList(queryUrl);
        return mReviewList;
    }

    @Override
    protected void onStartLoading() {
        if (mReviewList != null) {
            Log.d(LOG_TAG, "Delivering existing data");
            deliverResult(mReviewList);
        } else {
            forceLoad();
        }
    }
}
