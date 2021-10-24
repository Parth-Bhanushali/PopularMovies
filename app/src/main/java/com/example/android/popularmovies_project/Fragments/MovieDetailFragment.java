package com.example.android.popularmovies_project.Fragments;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies_project.Adapters.ReviewListAdapter;
import com.example.android.popularmovies_project.Adapters.TrailerListAdapter;
import com.example.android.popularmovies_project.Data.FavouriteContract.MovieEntry;
import com.example.android.popularmovies_project.Data.FavouriteContract.ReviewEntry;
import com.example.android.popularmovies_project.Data.FavouriteContract.TrailerEntry;
import com.example.android.popularmovies_project.Loaders.ReviewsLoader;
import com.example.android.popularmovies_project.Loaders.TrailersLoader;
import com.example.android.popularmovies_project.Models.Movie;
import com.example.android.popularmovies_project.Models.Review;
import com.example.android.popularmovies_project.Models.Trailer;
import com.example.android.popularmovies_project.R;
import com.example.android.popularmovies_project.Utils.MovieUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks,
        TrailerListAdapter.Callbacks,
        ReviewListAdapter.Callbacks {

    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    private static final boolean DEBUG_WITH_TOAST = false;
    private static final boolean DEBUG_PICASSO = false;

    public static final String ARG_MOVIE = "ARG_MOVIE";
    public static final String ARG_POSTER_BITMAP = "ARG_POSTER_BITMAP";
    private Movie movie;

    private static final int TRAILERS_LOADER_ID = 0;
    private static final int REVIEWS_LOADER_ID = 1;

    private static final int CURSOR_TRAILERS_LOADER_ID = 56;
    private static final int CURSOR_REVIEWS_LOADER_ID = 57;

    private Context mContext;

    private ImageView detailPosterIV;
    private TextView detailTitleTV, detailReleaseDateTV, detailOverviewTV;
    private List<ImageView> ratingStarViews = new ArrayList<>();
    private Button watchTrailerBTN, favouriteBTN;

    private List<Trailer> trailers;
    private RecyclerView mTrailerRV;
    private TrailerListAdapter mTrailerAdapter;

    private TextView detailReviewHeaderTV;
    private List<Review> reviews;
    private RecyclerView mReviewRV;
    private ReviewListAdapter mReviewAdapter;

    private static final String BASE_QUERY_URL = "https://api.themoviedb.org/3/movie";
    // We'll get this from arguments
    private int PATH_MOVIE_ID;
    private static final String POST_PATH_VIDEOS = "videos";
    private static final String POST_PATH_REVIEWS = "reviews";
    private static final String QUERY_URL_API_KEY_PARAMETER_KEY = "api_key";
    private static final String QUERY_URL_API_KEY_PARAMETER_VALUE = "8879ae265a5c0a0d00ef828a6bfe4b1d";
    private static final String QUERY_URL_LANGUAGE_PARAMETER_KEY = "language";
    private static final String QUERY_URL_LANGUAGE_PARAMETER_VALUE = "en-US";

    private boolean isFavouriteMovie = false;

    private static final int INDEX_REVIEW__ID = 0;
    private static final int INDEX_REVIEW_COL_REVIEW_ID = 1;
    private static final int INDEX_REVIEW_COL_AUTHOR = 2;
    private static final int INDEX_REVIEW_COL_CONTENT = 3;
    private static final int INDEX_REVIEW_COL_URL = 4;
    private static final int INDEX_REVIEW_COL_MOVIE_ID_KEY = 5;

    private static final int INDEX_TRAILER__ID = 0;
    private static final int INDEX_TRAILER_COL_TRAILER_ID = 1;
    private static final int INDEX_TRAILER_COL_KEY = 2;
    private static final int INDEX_TRAILER_COL_NAME = 3;
    private static final int INDEX_TRAILER_COL_SITE = 4;
    private static final int INDEX_TRAILER_COL_TYPE = 5;
    private static final int INDEX_TRAILER_COL_SIZE = 6;
    private static final int INDEX_TRAILER_COL_MOVIE_ID_KEY = 7;

    private Bitmap posterBitmap;

    public MovieDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(getString(R.string.key_trailers))) {
                trailers = savedInstanceState.getParcelableArrayList(getString(R.string.key_trailers));
            }
            if (savedInstanceState.containsKey(getString(R.string.key_reviews))) {
                reviews = savedInstanceState.getParcelableArrayList(getString(R.string.key_reviews));
            }
            if (savedInstanceState.containsKey(getString(R.string.key_poster))) {
                posterBitmap = savedInstanceState.getParcelable(getString(R.string.key_poster));
            }
        } else {
            trailers = new ArrayList<Trailer>();
            reviews = new ArrayList<Review>();
        }

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_MOVIE)) {
                movie = args.getParcelable(ARG_MOVIE);
                PATH_MOVIE_ID = movie.getId();
            }
            if (args.containsKey(ARG_POSTER_BITMAP)) {
                if (posterBitmap == null) {
                    try {
                        BitmapFactory.decodeStream(mContext.openFileInput(args.getString(ARG_POSTER_BITMAP)));
                    } catch (Exception e) {
                        e.printStackTrace();
                        posterBitmap = null;
                    }
                }
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_movie_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View rootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        detailPosterIV = rootView.findViewById(R.id.detail_poster);
        detailTitleTV = rootView.findViewById(R.id.detail_title);
        detailReleaseDateTV = rootView.findViewById(R.id.detail_release_date);
        detailOverviewTV = rootView.findViewById(R.id.detail_overview);
        detailReviewHeaderTV = rootView.findViewById(R.id.detail_reviews_header);
        watchTrailerBTN = rootView.findViewById(R.id.detail_button_watch_trailer);
        favouriteBTN = rootView.findViewById(R.id.detail_button_favourite);
        mTrailerRV = rootView.findViewById(R.id.trailer_recycler_view);
        mReviewRV = rootView.findViewById(R.id.review_recycler_view);

        ratingStarViews.add((ImageView) rootView.findViewById(R.id.rating_first_star));
        ratingStarViews.add((ImageView) rootView.findViewById(R.id.rating_second_star));
        ratingStarViews.add((ImageView) rootView.findViewById(R.id.rating_third_star));
        ratingStarViews.add((ImageView) rootView.findViewById(R.id.rating_fourth_star));
        ratingStarViews.add((ImageView) rootView.findViewById(R.id.rating_fifth_star));

        mTrailerAdapter = new TrailerListAdapter(mContext, trailers, this);
        LinearLayoutManager trailerLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mTrailerRV.setLayoutManager(trailerLayoutManager);
        mTrailerRV.setHasFixedSize(true);
        mTrailerRV.setAdapter(mTrailerAdapter);

        if (reviews.isEmpty()) {
            detailReviewHeaderTV.setVisibility(View.GONE);
        } else {
            detailReviewHeaderTV.setVisibility(View.VISIBLE);
            mReviewRV.setVisibility(View.VISIBLE);
        }

        mReviewAdapter = new ReviewListAdapter(mContext, reviews, this);
        LinearLayoutManager reviewLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mReviewRV.setLayoutManager(reviewLayoutManager);
        mReviewRV.setHasFixedSize(true);
        mReviewRV.setAdapter(mReviewAdapter);

        if (isFavourite(movie)) {
            isFavouriteMovie = true;
            favouriteBTN.setText(getString(R.string.fav_button_remove_from_favorites));
        } else {
            favouriteBTN.setText(getString(R.string.fav_button_mark_as_favourite));
        }


        File dir = mContext.getDir("postersDir", Context.MODE_PRIVATE);

        if (DEBUG_PICASSO)
            Picasso.with(mContext).setIndicatorsEnabled(true);

        if (posterBitmap != null) {
            detailPosterIV.setImageBitmap(posterBitmap);
        } else {
            if (isFavouriteMovie) {
                Picasso.with(mContext)
                        .load(new File(dir, movie.getTitle() + ".jpg"))
                        .config(Bitmap.Config.RGB_565)
                        .into(detailPosterIV, new Callback() {
                            @Override
                            public void onSuccess() {
                                if (posterBitmap == null)
                                    posterBitmap = ((BitmapDrawable) detailPosterIV.getDrawable()).getBitmap();
                            }

                            @Override
                            public void onError() {
                                Picasso.with(mContext)
                                        .load(MovieUtils.getImageLink(MovieUtils.IMAGE_QUALITY_W342, movie.getPosterPath()))
                                        .config(Bitmap.Config.RGB_565)
                                        .placeholder(R.drawable.broken_image_placeholder)
                                        .into(detailPosterIV, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                BitmapDrawable bitmapDrawable = (BitmapDrawable) detailPosterIV.getDrawable();
                                                Bitmap bitmap = bitmapDrawable.getBitmap();
                                                if (posterBitmap == null)
                                                    posterBitmap = bitmap;

                                                saveToInternalStorage(bitmap, movie.getTitle());
                                            }

                                            @Override
                                            public void onError() {
                                                // We need to show some placeholder/error images
                                            }
                                        });
                            }
                        });
            } else {
                Picasso.with(mContext)
                        .load(MovieUtils.getImageLink(MovieUtils.IMAGE_QUALITY_W342, movie.getPosterPath()))
                        .config(Bitmap.Config.RGB_565)
                        .error(R.drawable.broken_image_placeholder)
                        .into(detailPosterIV, new Callback() {
                            @Override
                            public void onSuccess() {
                                if (posterBitmap == null)
                                    posterBitmap = ((BitmapDrawable) detailPosterIV.getDrawable()).getBitmap();
                            }

                            @Override
                            public void onError() {
                            }
                        });
            }
        }

        if (trailers.isEmpty()) {
            disableTrailerButton();
        } else {
            for (Trailer trailer : trailers) {
                if (trailer.getType().equals("Trailer")) {
                    enableTrailerButton(trailer);
                    break;
                } else {
                    disableTrailerButton();
                }
            }
        }

        detailTitleTV.setText(movie.getTitle());
        detailReleaseDateTV.setText(getString(R.string.movie_released_date, MovieUtils.toDateString(movie.getReleaseDateMillis())));
        if (!movie.getOverview().isEmpty()) {
            detailOverviewTV.setText(movie.getOverview());
        } else {
            rootView.findViewById(R.id.detail_overview_header).setVisibility(View.GONE);
            detailOverviewTV.setVisibility(View.GONE);
        }
        updateRatingStars();

        watchTrailerBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (watchTrailerBTN.getTag() != null) {
                    if (DEBUG_WITH_TOAST)
                        Toast.makeText(mContext, "Open trailer video", Toast.LENGTH_SHORT).show();
                    watch((Trailer) watchTrailerBTN.getTag());
                }
            }
        });


        favouriteBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFavouriteMovie) {
                    insertMovieToDatabase(movie);

                    BitmapDrawable bitmapDrawable = (BitmapDrawable) detailPosterIV.getDrawable();
                    if (bitmapDrawable != null) {
                        Bitmap bitmap = bitmapDrawable.getBitmap();
                        Bitmap errorPlaceholder = BitmapFactory.decodeResource(getResources(), R.drawable.broken_image_placeholder);

                        // save only if it is not the placeholder, but actual poster image
                        if (!bitmap.sameAs(errorPlaceholder))
                            saveToInternalStorage(bitmap, movie.getTitle());
                    }
                }
                else {
                    removeMovieFromDatabase(movie);

                    // remove image from directory
                    removeImageFromStorage(movie.getTitle());
                }
            }
        });

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!isFavouriteMovie || !checkIfTrailersExistInDB(movie)) {
            getLoaderManager().initLoader(TRAILERS_LOADER_ID, null, this);
        } else {
            getLoaderManager().initLoader(CURSOR_TRAILERS_LOADER_ID, null, this);
        }

        if (!isFavouriteMovie || !checkIfReviewsExistInDB(movie)) {
            getLoaderManager().initLoader(REVIEWS_LOADER_ID, null, this);
        } else {
            getLoaderManager().initLoader(CURSOR_REVIEWS_LOADER_ID, null, this);
        }
    }

    private void updateRatingStars() {
        if (movie != null && movie.getVoteAverage() != 0) {
            // Original rating average is out of 10.
            // To get out of 5, we are dividing it by 2.
            float userRating = (float) (movie.getVoteAverage() / 2);
            int integerPart = (int) userRating;

            // Fill full stars
            for (int i = 0; i < integerPart; i++) {
                ratingStarViews.get(i).setImageResource(R.drawable.ic_star_black_24dp);
            }

            // Fill half star
            if (Math.round(userRating) > integerPart) {
                ratingStarViews.get(integerPart).setImageResource(R.drawable.ic_star_half_black_24dp);
            }
        } else {
            for (int i=0; i< ratingStarViews.size(); i++) {
                ratingStarViews.get(i).setVisibility(View.GONE);
            }
        }
    }


    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle bundle) {
        switch (id) {
            case TRAILERS_LOADER_ID:
                return new TrailersLoader(mContext, getTrailersQueryUrl());
            case REVIEWS_LOADER_ID:
                return new ReviewsLoader(mContext, getReviewsQueryUrl());
            case CURSOR_TRAILERS_LOADER_ID:
                return new CursorLoader(mContext,
                        TrailerEntry.buildTrailersUri(movie.getId()),
                        TrailerEntry.getColumns(),
                        null,
                        null,
                        null);
            case CURSOR_REVIEWS_LOADER_ID:
                return new CursorLoader(mContext,
                        ReviewEntry.buildReviewsUri(movie.getId()),
                        ReviewEntry.getColumns(),
                        null,
                        null,
                        null);
            default:
                throw new IllegalArgumentException("Loader with id: " + id + " is not supported");
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object resultList) {
        Log.d(LOG_TAG, "Results received");
        switch (loader.getId()) {
            case TRAILERS_LOADER_ID:
                if (resultList != null) {
                    // Here, we're sure that it will be the List of Trailers.
                    List<Trailer> result = (List<Trailer>) resultList;
                    if (result.isEmpty() && trailers.isEmpty()) {
                        mTrailerRV.setVisibility(View.GONE);
                        return;
                    }
                    if (!trailers.containsAll(result)) {
                        trailers.addAll(result);
                        insertTrailersForMovie(movie, trailers);
                        mTrailerAdapter.notifyDataSetChanged();

                        for (Trailer trailer : result) {
                            if (trailer.getType().equals("Trailer")) {
                                enableTrailerButton(trailer);
                                break;
                            }
                        }
                        mTrailerRV.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (trailers.isEmpty())
                        mTrailerRV.setVisibility(View.GONE);
                }
                break;

            case CURSOR_TRAILERS_LOADER_ID:
                if (resultList != null) {
                    if (DEBUG_WITH_TOAST)
                        Toast.makeText(mContext, "Loading trailers from database", Toast.LENGTH_SHORT).show();
                    Cursor trailersCursor = (Cursor) resultList;
                    if (trailersCursor.getCount() == 0 && trailers.isEmpty()) {
                        mTrailerRV.setVisibility(View.GONE);
                        return;
                    }

                    if (trailersCursor.moveToFirst()) {
                        List<Trailer> trailersResult = new ArrayList<>();
                        do {
                            String id = trailersCursor.getString(INDEX_TRAILER_COL_TRAILER_ID);
                            String key = trailersCursor.getString(INDEX_TRAILER_COL_KEY);
                            String name = trailersCursor.getString(INDEX_TRAILER_COL_NAME);
                            String site = trailersCursor.getString(INDEX_TRAILER_COL_SITE);
                            int size = trailersCursor.getInt(INDEX_TRAILER_COL_SIZE);
                            String type = trailersCursor.getString(INDEX_TRAILER_COL_TYPE);

                            Trailer trailer = new Trailer(id, key, name, site, size, type);
                            trailersResult.add(trailer);
                        } while (trailersCursor.moveToNext());

                        if (!trailers.containsAll(trailersResult)) {
                            trailers.addAll(trailersResult);
                            mTrailerAdapter.notifyDataSetChanged();

                            for (Trailer trailer : trailersResult) {
                                if (trailer.getType().equals("Trailer")) {
                                    enableTrailerButton(trailer);
                                    break;
                                }
                            }

                            Log.d(LOG_TAG, "Cursor Trailers Total: " + trailersResult.size());
                            mTrailerRV.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    if (trailers.isEmpty())
                        mTrailerRV.setVisibility(View.GONE);
                }
                break;

            case REVIEWS_LOADER_ID:
                if (resultList != null) {
                    List<Review> result = (List<Review>) resultList;
                    if (result.isEmpty() && reviews.isEmpty()) {
                        mReviewRV.setVisibility(View.GONE);
                        return;
                    }

                    if (!reviews.containsAll(result)) {
                        reviews.addAll(result);
                        insertReviewsForMovie(movie, reviews);

                        mReviewAdapter.notifyDataSetChanged();

                        Log.d(LOG_TAG, "Reviews total: " + result.size());
                        detailReviewHeaderTV.setVisibility(View.VISIBLE);
                        mReviewRV.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (reviews.isEmpty())
                        mReviewRV.setVisibility(View.GONE);
                }
                break;

            case CURSOR_REVIEWS_LOADER_ID:
                if (resultList != null) {
                    if (DEBUG_WITH_TOAST)
                        Toast.makeText(mContext, "Loading reviews from database", Toast.LENGTH_SHORT).show();
                    Cursor reviewsCursor = (Cursor) resultList;
                    if (reviewsCursor.getCount() == 0 && reviews.isEmpty()) {
                        mReviewRV.setVisibility(View.GONE);
                        return;
                    }

                    if (reviewsCursor.moveToFirst()) {
                        List<Review> reviewsResult = new ArrayList<>();
                        do {
                            String author = reviewsCursor.getString(INDEX_REVIEW_COL_AUTHOR);
                            String content = reviewsCursor.getString(INDEX_REVIEW_COL_CONTENT);
                            String reviewId = reviewsCursor.getString(INDEX_REVIEW_COL_REVIEW_ID);
                            String url = reviewsCursor.getString(INDEX_REVIEW_COL_URL);

                            Review review = new Review(author, content, reviewId, url);
                            reviewsResult.add(review);
                        } while (reviewsCursor.moveToNext());

                        if (!reviews.containsAll(reviewsResult)) {
                            reviews.addAll(reviewsResult);
                            mReviewAdapter.notifyDataSetChanged();

                            Log.d(LOG_TAG, "Cursor Reviews Total: " + reviewsResult.size());
                            detailReviewHeaderTV.setVisibility(View.VISIBLE);
                            mReviewRV.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    if (reviews.isEmpty())
                    mReviewRV.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        switch (loader.getId()) {
            case TRAILERS_LOADER_ID:
                Log.d(LOG_TAG, "Trailers Loader will be resetted");
                break;

            case REVIEWS_LOADER_ID:
                Log.d(LOG_TAG, "Reviews Loader will be resetted");
                break;

            case CURSOR_TRAILERS_LOADER_ID:
                Log.d(LOG_TAG, "Cursor Trailers Loader will be resetted");
                break;

            case CURSOR_REVIEWS_LOADER_ID:
                Log.d(LOG_TAG, "Cursor Reviews Loader will be resetted");
                break;
        }

    }


    @Override
    public void watch(Trailer trailer) {
        Log.d(LOG_TAG, "Youtube link: " + MovieUtils.getTrailerLink(trailer.getKey()));

        // App Intent
        Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            intent.setData(Uri.parse("vnd.youtube:" + trailer.getKey()));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // web intent
            intent.setData(Uri.parse(MovieUtils.getTrailerLink(trailer.getKey())));
            startActivity(intent);
        }
    }

    @Override
    public void readReview(Review review) {
        Log.d(LOG_TAG, "Review link: " + review.getUrl());

        // web intent
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(review.getUrl()));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, getString(R.string.browser_not_installed_message), Toast.LENGTH_SHORT).show();
        }
    }

    private String getTrailersQueryUrl() {
        Uri uri = Uri.parse(BASE_QUERY_URL).buildUpon()
                .appendPath(String.valueOf(PATH_MOVIE_ID))
                .appendPath(POST_PATH_VIDEOS)
                .appendQueryParameter(QUERY_URL_API_KEY_PARAMETER_KEY, QUERY_URL_API_KEY_PARAMETER_VALUE)
                .appendQueryParameter(QUERY_URL_LANGUAGE_PARAMETER_KEY, QUERY_URL_LANGUAGE_PARAMETER_VALUE)
                .build();
        Log.d("TrailersQueryUrl", uri.toString());

        return uri.toString();
    }

    private String getReviewsQueryUrl() {
        Uri uri = Uri.parse(BASE_QUERY_URL).buildUpon()
                .appendPath(String.valueOf(PATH_MOVIE_ID))
                .appendPath(POST_PATH_REVIEWS)
                .appendQueryParameter(QUERY_URL_API_KEY_PARAMETER_KEY, QUERY_URL_API_KEY_PARAMETER_VALUE)
                .appendQueryParameter(QUERY_URL_LANGUAGE_PARAMETER_KEY, QUERY_URL_LANGUAGE_PARAMETER_VALUE)
                .build();
        Log.d("ReviewsQueryUrl", uri.toString());

        return uri.toString();
    }

    private void enableTrailerButton(Trailer tag) {
        watchTrailerBTN.setEnabled(true);
        if (watchTrailerBTN.getTag() == null)
            watchTrailerBTN.setTag(tag);
        watchTrailerBTN.getBackground().setTint(getResources().getColor(R.color.colorTrailerButtonEnabled));
        watchTrailerBTN.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void disableTrailerButton() {
        if (watchTrailerBTN.isEnabled()) {
            watchTrailerBTN.setEnabled(false);
            watchTrailerBTN.getBackground().setTint(getResources().getColor(R.color.colorTrailerButtonDisabled));
            watchTrailerBTN.setTextColor(getResources().getColor(R.color.colorButtonDisabledText));
        }
    }


    // TODO: These all database related methods below should be refactored and should be called in
    //  proper places and make sure to read from database as less as possible.
    //  With performance concerns, these methods should run in asynctask (different thread) ?
    private long insertMovieToDatabase(Movie movie) {
        Uri insertedUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, movie.toContentValues());
        long movieIdKey = MovieEntry.getIdFromUri(insertedUri);
        Log.d(LOG_TAG, "Movie _Id" + movieIdKey);

        isFavouriteMovie = true;
        favouriteBTN.setText(getString(R.string.fav_button_remove_from_favorites));

        if (reviews != null && !reviews.isEmpty()) {
            insertReviewsForMovie(movie, reviews);
        }
        if (trailers != null && !trailers.isEmpty()) {
            insertTrailersForMovie(movie, trailers);
        }
        return movieIdKey;
    }

    private void removeMovieFromDatabase(Movie movie) {
        // Warning: before removing the movie, delete its reviews and trailers first,
        // because internally it relies on joining, so if movie is deleted first,
        // then the delete statement of reviews/trailers won't get required associated information
        // of that particular movie and will cause serious problems while inserting the same data for same
        // movieId because its not delete properly.
        int reviewsRemovedCount = mContext.getContentResolver().delete(
                ReviewEntry.buildReviewsUri(movie.getId()),
                null,
                null);
        Log.d(LOG_TAG, "Total reviews removed: " + reviewsRemovedCount);

        int trailersRemovedCount = mContext.getContentResolver().delete(
                TrailerEntry.buildTrailersUri(movie.getId()),
                null,
                null
        );
        Log.d(LOG_TAG, "Total trailers removed: " + trailersRemovedCount);


        int moviesRemovedCount = mContext.getContentResolver().delete(
                MovieEntry.buildSpecificMovieUri(movie.getId()),
                null,
                null);

        Log.d(LOG_TAG, "Total movies removed: " + moviesRemovedCount);


        isFavouriteMovie = false;
        favouriteBTN.setText(getString(R.string.fav_button_mark_as_favourite));
    }

    private boolean checkIfReviewsExistInDB(Movie ofWhich) {
        boolean reviewsAvailable = false;
        Cursor cursor = mContext.getContentResolver().query(
                ReviewEntry.buildReviewsUri(ofWhich.getId()),
                new String[]{ReviewEntry.getColumns()[0]},
                null,
                null,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            Log.d("ReviewsFromDb", ofWhich.getTitle() + " contains reviews in database");
            reviewsAvailable = true;
            cursor.close();
        } else {
            Log.d("ReviewsFromDb", ofWhich.getTitle() + " doesn't contain any reviews in database");
        }
        return reviewsAvailable;
    }

    private boolean checkIfTrailersExistInDB(Movie ofWhich) {
        boolean trailersAvailable = false;
        Cursor cursor = mContext.getContentResolver().query(
                TrailerEntry.buildTrailersUri(ofWhich.getId()),
                new String[]{TrailerEntry.getColumns()[0]},
                null,
                null,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            Log.d("TrailersFromDb", ofWhich.getTitle() + " contains trailers in database");
            trailersAvailable = true;
            cursor.close();
        } else {
            Log.d("ReviewsFromDb", ofWhich.getTitle() + " doesn't contain any reviews in database");
        }
        return trailersAvailable;
    }

    private void insertReviewsForMovie(Movie movie, List<Review> reviews) {
        if (isFavouriteMovie) {
            if (checkIfReviewsExistInDB(movie)) {
                Log.d("ReviewsDb", "Reviews already available for this movie");
                return;
            }
            long movieRowId = getMovieRowId(movie.getId());
            for (Review review : reviews) {
                ContentValues value = new ContentValues();
                value.putAll(review.toContentValues());
                value.put(ReviewEntry.COLUMN_MOVIE_ID_KEY, movieRowId);

                mContext.getContentResolver().insert(
                        ReviewEntry.buildReviewsUri(movie.getId()),
                        value
                );
            }
            Log.d("ReviewsDb", "Reviews for _id: " + movieRowId + " inserted successfully");
        } else {
            Log.d("ReviewsDb", "No need to insert reviews in db");
        }
    }

    private void insertTrailersForMovie(Movie movie, List<Trailer> trailers) {
        if (isFavouriteMovie) {
            if (checkIfTrailersExistInDB(movie)) {
                Log.d("TrailersDb", "Trailers already available for this movie");
                return;
            }
            long movieRowId = getMovieRowId(movie.getId());
            for (Trailer trailer : trailers) {
                ContentValues value = new ContentValues();
                value.putAll(trailer.toContentValues());
                value.put(ReviewEntry.COLUMN_MOVIE_ID_KEY, movieRowId);

                mContext.getContentResolver().insert(
                        TrailerEntry.buildTrailersUri(movie.getId()),
                        value
                );
            }
            Log.d("TrailersDb", "Trailers for _id: " + movieRowId + " inserted successfully");
        } else {
            Log.d("TrailersDb", "No need to insert trailers in db");
        }
    }

    private long getMovieRowId(int movieId) {
        long _id = -1;
        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.buildSpecificMovieUri(movieId),
                new String[]{MovieEntry._ID},
                null,
                null,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            _id = cursor.getLong(cursor.getColumnIndex(MovieEntry._ID));
            Log.d("Movie_ID", "Movie row id is: " + _id);
            cursor.close();
        } else {
            Log.d("Movie_ID", "Failed to get movie row id");
        }
        return _id;
    }

    private boolean isFavourite(Movie movie) {
        boolean isFavourite = false;
        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                new String[]{MovieEntry._ID},
                MovieEntry.COLUMN_MOVIE_ID + " = " + movie.getId(),
                null,
                null
        );
        if (cursor != null) {
            isFavourite = cursor.getCount() != 0;
            cursor.close();
        }
        return isFavourite;
    }

    private void saveToInternalStorage(Bitmap bitmapImage, String imageName) {
        // To avoid null pointer exception, because this method can be called any time when Picasso
        // loads the image and rarely we might not have proper context available may be
        // due to very fast orientation change when poster is exactly about to load completely and save.
        if (mContext != null) {
            ContextWrapper cw = new ContextWrapper(mContext.getApplicationContext());
            // path to /data/data/yourapp/app_postersDir
            File directory = cw.getDir("postersDir", Context.MODE_PRIVATE);
            // Create image file
            File mypath = new File(directory, imageName + ".jpg");

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mypath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Log.d("AbsolutePostersDirPath", directory.getAbsolutePath());
        }
    }

    /**
     * Deletes the attached image of the movie poster having the name as same as movie's title
     *
     * @param imageName is the title of the image/poster
     */
    private void removeImageFromStorage(String imageName) {
        File dir = mContext.getDir("postersDir", Context.MODE_PRIVATE);
//        File file = new File("/data/data/com.example.android.popularmovies_project/app_postersDir", imageName + ".jpg");
        File file = new File(dir, imageName + ".jpg");
        file.delete();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("ReviewsSurviving", "Reviews size: " + reviews.size());
        Log.d("TrailersSurviving", "Trailers size: " + trailers.size());
        outState.putParcelableArrayList(getString(R.string.key_reviews), (ArrayList<Review>) reviews);
        outState.putParcelableArrayList(getString(R.string.key_trailers), (ArrayList<Trailer>) trailers);
        outState.putParcelable(getString(R.string.key_poster), posterBitmap);
    }
}
