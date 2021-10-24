package com.example.android.popularmovies_project.Data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.example.android.popularmovies_project.R;

public class MoviesPreferences {
    private MoviesPreferences() {}

    public static String getPreferredSortOrder(Context context) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        String keyForSortOrder = context.getString(R.string.key_movies_sort_order);
        String defaultSortOrder = context.getString(R.string.default_value_sort_order);
        return prefs.getString(keyForSortOrder, defaultSortOrder);
    }
}
