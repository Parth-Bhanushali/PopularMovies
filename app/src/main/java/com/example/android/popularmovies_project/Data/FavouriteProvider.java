package com.example.android.popularmovies_project.Data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.example.android.popularmovies_project.Data.FavouriteContract.MovieEntry;
import com.example.android.popularmovies_project.Data.FavouriteContract.ReviewEntry;
import com.example.android.popularmovies_project.Data.FavouriteContract.TrailerEntry;

import java.util.Arrays;
import java.util.HashSet;

public class FavouriteProvider extends ContentProvider {

    static final int MOVIES = 100;
    static final int MOVIE_BY_MOVIEID = 101;
    static final int TRAILERS_BY_MOVIEID = 201;
    static final int REVIEWS_BY_MOVIEID = 301;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder sReviewsQueryBuilder, sTrailersQueryBuilder;

    private FavouriteDbHelper mDbHelper;


    // movies.movie_id = ?
    private static final String sMovieByMovieIdSelection =
            MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID + " = ? ";
    // movies.movie_id = ?
    private static final String sReviewsByMovieIdSelection =
            MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID + " = ? ";
    // movies.movie_id = ?
    private static final String sTrailersByMovieIdSelection =
            MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID + " = ? ";

    static {
        sReviewsQueryBuilder = new SQLiteQueryBuilder();
        sTrailersQueryBuilder = new SQLiteQueryBuilder();

        // This is an inner join which looks like
        // reviews INNER JOIN movies ON reviews.movie_id_key = movies._id
        sReviewsQueryBuilder.setTables(
                ReviewEntry.TABLE_NAME + " INNER JOIN " + MovieEntry.TABLE_NAME + " ON " +
                        ReviewEntry.TABLE_NAME + "." + ReviewEntry.COLUMN_MOVIE_ID_KEY + " = " +
                        MovieEntry.TABLE_NAME + "." + MovieEntry._ID);

        // This is an inner join which looks like
        // trailers INNER JOIN movies ON trailers.movie_id_key = movies._id
        sTrailersQueryBuilder.setTables(
                TrailerEntry.TABLE_NAME + " INNER JOIN " + MovieEntry.TABLE_NAME + " ON " +
                        TrailerEntry.TABLE_NAME + "." + TrailerEntry.COLUMN_MOVIE_ID_KEY + " = " +
                        MovieEntry.TABLE_NAME + "." + MovieEntry._ID);
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FavouriteContract.CONTENT_AUTHORITY;

        // /movies  for all movies
        matcher.addURI(authority, FavouriteContract.PATH_MOVIES, MOVIES);
        // /movies/#     for specific movie
        matcher.addURI(authority, FavouriteContract.PATH_MOVIES + "/#", MOVIE_BY_MOVIEID);

        // /movies/#/reviews     for all reviews of specific movie
        matcher.addURI(authority, FavouriteContract.PATH_MOVIES + "/#/" + FavouriteContract.PATH_REVIEWS, REVIEWS_BY_MOVIEID);
        // /movies/#/trailers    for all trailers of specific movie
        matcher.addURI(authority, FavouriteContract.PATH_MOVIES + "/#/" + FavouriteContract.PATH_TRAILERS, TRAILERS_BY_MOVIEID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new FavouriteDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                return MovieEntry.CONTENT_DIR_TYPE;
            case MOVIE_BY_MOVIEID:
                return MovieEntry.CONTENT_ITEM_TYPE;
            case REVIEWS_BY_MOVIEID:
                return ReviewEntry.CONTENT_DIR_TYPE;
            case TRAILERS_BY_MOVIEID:
                return TrailerEntry.CONTENT_DIR_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        final int match = sUriMatcher.match(uri);
        Cursor retCursor;
        checkColumns(projection, match);
        switch (match) {
            case MOVIES:
                retCursor = mDbHelper.getReadableDatabase().query(
                        MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case MOVIE_BY_MOVIEID:
                retCursor = getMovieByMovieId(uri, projection, sortOrder);
                break;
            case REVIEWS_BY_MOVIEID:
                // Extract the id of the movie from the uri and
                // make selection and selectionArgs to work appropriately so that
                // we can end up showing trailers/reviews of selected movies.

                // Here, id refers to actual movie id
                retCursor = getReviewsByMovieId(uri, projection, sortOrder);
                break;
            case TRAILERS_BY_MOVIEID:
                retCursor = getTrailersByMovieId(uri, projection, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri retUri;
        switch (match) {
            case MOVIES: {
                long _id = db.insert(MovieEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    retUri = MovieEntry.buildMovieUri(_id);
                else
                    throw new SQLException("Failed to insert row into: " + uri);
                break;
            }
            case REVIEWS_BY_MOVIEID: {
                long _id = db.insert(ReviewEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    retUri = ReviewEntry.CONTENT_URI;
                else
                    throw new SQLException("Failed to insert row into: " + uri);
                break;
            }
            case TRAILERS_BY_MOVIEID: {
                long _id = db.insert(TrailerEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    retUri = TrailerEntry.CONTENT_URI;
                else
                    throw new SQLException("Failed to insert row into: " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return retUri;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case REVIEWS_BY_MOVIEID: {
                db.beginTransaction();
                int insertedReviewsCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ReviewEntry.TABLE_NAME,
                                null,
                                value);
                        if (_id != -1) {
                            insertedReviewsCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return insertedReviewsCount;
            }
            case TRAILERS_BY_MOVIEID: {
                db.beginTransaction();
                int insertedTrailersCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ReviewEntry.TABLE_NAME,
                                null,
                                value);
                        if (_id != -1) {
                            insertedTrailersCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return insertedTrailersCount;
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case MOVIES: {
                rowsDeleted = db.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case MOVIE_BY_MOVIEID: {
                rowsDeleted = db.delete(MovieEntry.TABLE_NAME,
                        sMovieByMovieIdSelection,
                        new String[]{String.valueOf(MovieEntry.getSpecificMovieId(uri))});
                break;
            }
            case REVIEWS_BY_MOVIEID: {
                int movieId = ReviewEntry.getMovieIdFromUri(uri);
//                selection = ReviewEntry.COLUMN_MOVIE_ID_KEY + " IN (SELECT _id FROM movies WHERE movies.movie_id = ? )";
                selection = ReviewEntry.COLUMN_MOVIE_ID_KEY + " IN (SELECT " + MovieEntry._ID + " FROM " + MovieEntry.TABLE_NAME
                        + " WHERE " + sMovieByMovieIdSelection + ")";


                // Complete SQLite delete statement should look like,
                // DELETE FROM reviews WHERE movie_id_key IN (SELECT _id FROM movies WHERE movies.movie_id = ?);
                // ? would be the movieId
                rowsDeleted = db.delete(ReviewEntry.TABLE_NAME,
                        selection,
                        new String[]{String.valueOf(movieId)});
                break;
            }
            case TRAILERS_BY_MOVIEID: {
                int movieId = TrailerEntry.getMovieIdFromUri(uri);
                selection = TrailerEntry.COLUMN_MOVIE_ID_KEY + " IN (SELECT " + MovieEntry._ID + " FROM " + MovieEntry.TABLE_NAME
                        + " WHERE " + sMovieByMovieIdSelection + ")";

                rowsDeleted = db.delete(TrailerEntry.TABLE_NAME,
                        selection,
                        new String[]{String.valueOf(movieId)});
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // NOTE: our application doesn't use this method as it don't even need to.
        //  So rather than leaving it empty, basic implementation is provided.
        //  In case if you need the functionality, this method implementation would be similar to the
        //  delete method provided above, so you can make changes in future as per requirement.
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case MOVIES:
                rowsUpdated = db.update(MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case REVIEWS_BY_MOVIEID:
                rowsUpdated = db.update(ReviewEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case TRAILERS_BY_MOVIEID:
                rowsUpdated = db.update(TrailerEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mDbHelper.close();
        super.shutdown();
    }


    /* ------------------------------------------ < HELPER METHODS > ------------------------------------- */
    private void checkColumns(String[] projection, int match) {
        if (projection != null) {
            String[] allColumns;
            switch (match) {
                case MOVIES:
                    allColumns = MovieEntry.getColumns();
                    break;
                case MOVIE_BY_MOVIEID:
                    allColumns = MovieEntry.getColumns();
                    break;
                case REVIEWS_BY_MOVIEID:
                    allColumns = ReviewEntry.getColumns();
                    break;
                case TRAILERS_BY_MOVIEID:
                    allColumns = TrailerEntry.getColumns();
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown Uri with match: " + match);
            }
            HashSet<String> availableColumns = new HashSet<>(Arrays.asList(allColumns));
            HashSet<String> requestedColumns = new HashSet<>(Arrays.asList(projection));
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }

    private Cursor getMovieByMovieId(Uri uri, String[] projection, String sortOrder) {
        int movieId = MovieEntry.getSpecificMovieId(uri);
        String selection = sMovieByMovieIdSelection;
        String[] selectionArgs = new String[]{Integer.toString(movieId)};

        return mDbHelper.getReadableDatabase().query(
                MovieEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getReviewsByMovieId(Uri uri, String[] projection, String sortOrder) {
        // e.g. 13389 is movieId
        int movieId = ReviewEntry.getMovieIdFromUri(uri);
        return sReviewsQueryBuilder.query(mDbHelper.getReadableDatabase(),
                projection,
                sReviewsByMovieIdSelection,
                new String[]{Integer.toString(movieId)},
                null,
                null,
                sortOrder);
    }

    private Cursor getTrailersByMovieId(Uri uri, String[] projection, String sortOrder) {
        int movieId = TrailerEntry.getMovieIdFromUri(uri);
        return sTrailersQueryBuilder.query(mDbHelper.getReadableDatabase(),
                projection,
                sTrailersByMovieIdSelection,
                new String[]{String.valueOf(movieId)},
                null,
                null,
                sortOrder);
    }
    /* --------------------------------------------------------------------------------------------------- */
}


// Note: Here in our case, # (In matcher's uri) would be any integer movie id (not rowId)