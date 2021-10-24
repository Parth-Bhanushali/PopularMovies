package com.example.android.popularmovies_project.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.popularmovies_project.Data.FavouriteContract.MovieEntry;
import com.example.android.popularmovies_project.Data.FavouriteContract.ReviewEntry;
import com.example.android.popularmovies_project.Data.FavouriteContract.TrailerEntry;

/**
 * Manages a local database for favourite movies.
 */
public class FavouriteDbHelper extends SQLiteOpenHelper {

    // The name of the database
    private static final String DATABASE_NAME = "favorites.db";

    // If you change the database schema, you must increment the database version
    private static final int DATABASE_VERSION = 1;

    public FavouriteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the favourite database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MovieEntry.SQL_CREATE_MOVIES_TABLE);
        db.execSQL(ReviewEntry.SQL_CREATE_REVIEWS_TABLE);
        db.execSQL(TrailerEntry.SQL_CREATE_TRAILERS_TABLE);
    }

    /**
     * This method discards the old table of data and calls onCreate to recreate a new one.
     * This only occurs when the version number for this database (DATABASE_VERSION) is incremented.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TrailerEntry.TABLE_NAME);
        onCreate(db);
    }
}
