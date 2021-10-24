package com.example.android.popularmovies_project.Loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.android.popularmovies_project.Models.Trailer;
import com.example.android.popularmovies_project.Utils.QueryUtils;

import java.util.List;

public class TrailersLoader extends AsyncTaskLoader<List<Trailer>> {

    private static final String LOG_TAG = TrailersLoader.class.getSimpleName();
    private String queryUrl;
    private List<Trailer> mTrailerList;

    public TrailersLoader(@NonNull Context context, String requestUrl) {
        super(context);
        queryUrl = requestUrl;
    }

    @Nullable
    @Override
    public List<Trailer> loadInBackground() {
        if (queryUrl == null) {
            return null;
        }

        mTrailerList = QueryUtils.fetchTrailerList(queryUrl);
        return mTrailerList;
    }

    @Override
    protected void onStartLoading() {
        if (mTrailerList != null) {
            Log.d(LOG_TAG, "Delivering existing data");
            deliverResult(mTrailerList);
        } else {
            forceLoad();
        }
    }
}
