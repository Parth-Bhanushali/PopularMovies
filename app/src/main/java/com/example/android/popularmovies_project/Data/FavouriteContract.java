package com.example.android.popularmovies_project.Data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the favourite database.
 */
public class FavouriteContract {

    // The authority, which is how your code knows which Content Provider to access
    public static final String CONTENT_AUTHORITY = "com.example.android.popularmovies_project";

    // The base content URI = "content://" + <authority>
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Define the possible paths for accessing data in this contract
    // This is the path for the "movies" directory
    public static final String PATH_MOVIES = "movies";
    // This is the path for the "trailers" directory
    public static final String PATH_TRAILERS = "trailers";
    // This is the path for the "reviews" directory
    public static final String PATH_REVIEWS = "reviews";


    private FavouriteContract() {}

    /*
     * Inner class that defines the contents of the movies table
     */
    public static final class MovieEntry implements BaseColumns {

        // MovieEntry content URI = base content URI + path
        // content://com.example.android.popularmovies_project/movies
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_ORIGINAL_LANGUAGE = "original_language";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_RELEASE_DATE_MILLIS = "release_date_millis";


        public static final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MOVIE_ID + " INTEGER NOT NULL UNIQUE, " +
                COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                COLUMN_BACKDROP_PATH + " TEXT NOT NULL, " +
                COLUMN_ORIGINAL_LANGUAGE + " TEXT NOT NULL, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                COLUMN_RELEASE_DATE_MILLIS + " INTEGER NOT NULL " +

                ");";

        private static final String[] COLUMNS = {_ID, COLUMN_MOVIE_ID, COLUMN_POSTER_PATH, COLUMN_BACKDROP_PATH,
                COLUMN_ORIGINAL_LANGUAGE, COLUMN_TITLE, COLUMN_OVERVIEW, COLUMN_VOTE_AVERAGE,
                COLUMN_RELEASE_DATE_MILLIS};

        private MovieEntry() {}


        /**
         * @param id is the rowId (_id of the movies table).
         * @return
         */
        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        /**
         * @param uri
         * @return the rowId (_id of the movies table).
         */
        public static long getIdFromUri(Uri uri) {
            return ContentUris.parseId(uri);
        }

        /**
         * @param movieId is the actual id that {@link com.example.android.popularmovies_project.Models.Movie}s uniquely
         *               contains
         * @return
         */
        public static Uri buildSpecificMovieUri(int movieId) {
            return ContentUris.withAppendedId(CONTENT_URI, movieId);
        }

        /**
         * @param uri
         * @return the movieId that each {@link com.example.android.popularmovies_project.Models.Movie}s uniquely contains
         */
        public static int getSpecificMovieId(Uri uri) {
            return (int) ContentUris.parseId(uri);
        }

        public static String[] getColumns() {
            return COLUMNS.clone();
        }
    }


    /*
     * Inner class that defines the contents of the reviews table
     */
    public static final class ReviewEntry implements BaseColumns {
        // ReviewEntry content URI = base content URI + path
        // content://com.example.android.popularmovies_project/reviews
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEWS).build();

        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;

        public static final String TABLE_NAME = "reviews";

        public static final String COLUMN_REVIEW_ID = "review_id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_URL = "url";
        // Column having foreign key for {@link MovieEntry}'s (movies table).
        public static final String COLUMN_MOVIE_ID_KEY = "movie_id_key";


        public static final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_REVIEW_ID + " TEXT NOT NULL, " +
                COLUMN_AUTHOR + " TEXT NOT NULL, " +
                COLUMN_CONTENT + " TEXT NOT NULL, " +
                COLUMN_URL + " TEXT NOT NULL UNIQUE, " +
                COLUMN_MOVIE_ID_KEY + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + COLUMN_MOVIE_ID_KEY + ") REFERENCES " + MovieEntry.TABLE_NAME +
                " (" + MovieEntry._ID + " )" +

                ");";

        private static final String[] COLUMNS = {ReviewEntry.TABLE_NAME + "." + _ID, COLUMN_REVIEW_ID, COLUMN_AUTHOR, COLUMN_CONTENT,
                COLUMN_URL, COLUMN_MOVIE_ID_KEY};

        private ReviewEntry() {}


        /**
         * @param movieId is the actual id that {@link com.example.android.popularmovies_project.Models.Movie}s uniquely
         *                contains
         * @return the Uri which will accesses all the reviews of specified movie (differentiated by movieId)
         */
        public static Uri buildReviewsUri(int movieId) {
            return MovieEntry.CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(movieId))
                    .appendPath(PATH_REVIEWS)
                    .build();
        }

        /**
         * @param uri is the Uri which accesses all the reviews of specific movie (differentiated by movieId)
         * @return the movieId that each {@link com.example.android.popularmovies_project.Models.Movie}s uniquely contains
         */
        public static int getMovieIdFromUri(Uri uri) {
            // content://<authority>/movies/13389/reviews
            return Integer.valueOf(uri.getPathSegments().get(1));
        }

        public static String[] getColumns() {
            return COLUMNS.clone();
        }
    }


    /*
     * Inner class that defines the contents of the trailers table
     */
    public static final class TrailerEntry implements BaseColumns {
        // TrailerEntry content URI = base content URI + path
        // content://com.example.android.popularmovies_project/trailers
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILERS).build();

        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILERS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILERS;

        public static final String TABLE_NAME = "trailers";

        public static final String COLUMN_TRAILER_ID = "trailer_id";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_SIZE = "size";
        public static final String COLUMN_MOVIE_ID_KEY = "movie_id_key";


        public static final String SQL_CREATE_TRAILERS_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_TRAILER_ID + " TEXT NOT NULL, " +
                COLUMN_KEY + " TEXT NOT NULL UNIQUE, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_SITE + " TEXT NOT NULL, " +
                COLUMN_TYPE + " TEXT NOT NULL, " +
                COLUMN_SIZE + " INTEGER NOT NULL, " +
                COLUMN_MOVIE_ID_KEY + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + COLUMN_MOVIE_ID_KEY + ") REFERENCES " + MovieEntry.TABLE_NAME +
                " (" + MovieEntry._ID + " )" +

                ");";

        private static final String[] COLUMNS = {TrailerEntry.TABLE_NAME + "." + _ID, COLUMN_TRAILER_ID, COLUMN_KEY, COLUMN_NAME,
                COLUMN_SITE, COLUMN_TYPE, COLUMN_SIZE, COLUMN_MOVIE_ID_KEY};

        private TrailerEntry() {}


        /**
         * @param movieId is the actual id that {@link com.example.android.popularmovies_project.Models.Movie}s uniquely
         *                contains
         * @return the Uri which will accesses all the trailers of specified movie (differentiated by movieId)
         */
        public static Uri buildTrailersUri(int movieId) {
            return MovieEntry.CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(movieId))
                    .appendPath(PATH_TRAILERS)
                    .build();
        }

        /**
         * @param uri is the Uri which accesses all the trailers of specific movie (differentiated by movieId)
         * @return the movieId that each {@link com.example.android.popularmovies_project.Models.Movie}s uniquely contains
         */
        public static int getMovieIdFromUri(Uri uri) {
            // content://<authority>/movies/13389/trailers
            return Integer.valueOf(uri.getPathSegments().get(1));
        }

        public static String[] getColumns() {
            return COLUMNS.clone();
        }
    }
}
