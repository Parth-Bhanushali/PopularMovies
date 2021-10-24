package com.example.android.popularmovies_project.Fragments;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.popularmovies_project.Adapters.MovieListAdapter;
import com.example.android.popularmovies_project.Data.FavouriteContract.MovieEntry;
import com.example.android.popularmovies_project.Data.MoviesPreferences;
import com.example.android.popularmovies_project.Loaders.MoviesLoader;
import com.example.android.popularmovies_project.UI.Activities.MainActivity;
import com.example.android.popularmovies_project.Models.Movie;
import com.example.android.popularmovies_project.R;
import com.example.android.popularmovies_project.UI.Activities.SettingsActivity;
import com.example.android.popularmovies_project.Utils.LayoutUtils;
import com.example.android.popularmovies_project.UI.MovieItemDecoration;
import com.example.android.popularmovies_project.UI.MoviePosterAnimator;
import com.example.android.popularmovies_project.Utils.QueryUtils;

import java.util.ArrayList;
import java.util.List;

// TODO: clean up all the codes properly by removing unnecessary logs and Toast messages
public class MoviesFragment extends Fragment
        implements LoaderManager.LoaderCallbacks,
        SharedPreferences.OnSharedPreferenceChangeListener,
        MovieListAdapter.OnRetryClickListener {

    /*----------------------------- < DEFINE VARIABLES > ---------------------------------------------*/
    private Context mContext;

    private static final String LOG_TAG = MoviesFragment.class.getSimpleName();
    private static final boolean DEBUG_WITH_TOAST = false;
    private MoviesViewModel viewModel;
    private RecyclerView mRecyclerView;
    private View mInternetErrorContainer, mNoFavouriteMoviesContainer;
    private Button mRetryBTN;
    private ProgressBar mLoadingIndicator;
    private MovieListAdapter mAdapter;
    private GridLayoutManager layoutManager;
    private int numColumns;

    private static final String BASE_QUERY_URL = "https://api.themoviedb.org/3/movie";
    // We'll get it from shared preferences
    public static String QUERY_URL_SORT_PATH;
    private static final String QUERY_URL_API_KEY_PARAMETER_KEY = "api_key";
    private static final String QUERY_URL_API_KEY_PARAMETER_VALUE = "8879ae265a5c0a0d00ef828a6bfe4b1d";
    private static final String QUERY_URL_LANGUAGE_PARAMETER_KEY = "language";
    private static final String QUERY_URL_LANGUAGE_PARAMETER_VALUE = "en-US";
    private static final String QUERY_URL_PAGE_PARAMETER_KEY = "page";

//    private static final String baseQueryUrl = "https://api.themoviedb.org/3/movie/popular?api_key=8879ae265a5c0a0d00ef828a6bfe4b1d&language=en-US&page=";

    private static final int MOVIES_LOADER_ID = 0;
    private static final int FAVORITES_LOADER_ID = 7;

    private Cursor mFavoritesCursor;

    int INDEX__ID = 0;
    int INDEX_COL_MOVIE_ID = 1;
    int INDEX_COL_POSTER_PATH = 2;
    int INDEX_COL_BACKDROP_PATH = 3;
    int INDEX_COL_ORIGINAL_LANGUAGE = 4;
    int INDEX_COL_TITLE = 5;
    int INDEX_COL_OVERVIEW = 6;
    int INDEX_COL_VOTE_AVERAGE = 7;
    int INDEX_COL_RELEASE_DATE_MILLIS = 8;

    private boolean isScrolling = false;
    private int currentItems, scrolledOutItems, totalItems;

    // -5 is an arbitrary number, you can choose any. Just to differentiate the
    // value from standard visibility values
    private int containerErrorVisibility = -5;
    // when false, newly added null data will behave as normal loading &
    // when true, newly added null data will behave as error bar giving a chance to retry
    public static boolean internetErrorOccurred = false;

    private boolean isRecyclerViewScrollable;

    // Handles whether the loader has finished loading or not
    // will become true in onLoadFinished(...) callback method
    private boolean hasFinishedLoading = false;
    private boolean pagesFinished = false;

    private static boolean PREFERENCES_HAVE_BEEN_UPDATED = false;

    // For the progress bar which is shown in the middle of the screen
    private boolean indicatorWasThere = false;

    // Define the listener of the interface type
    // listener will the activity instance containing fragment
    private Callbacks mCallbacks;
    /*------------------------------------------------------------------------------------------------*/



    /*----------------------------- < REQUIRED EMPTY CONSTRUCTOR > -----------------------------------*/
    public MoviesFragment() {
        // Required empty public constructor
    }
    /*------------------------------------------------------------------------------------------------*/



    /*----------------------------- < FRAGMENT LIFECYCLE CALLBACKS > ---------------------------------*/

    // This event fires 1st, before creation of fragment or any views
    // The onAttach method is called when the Fragment instance is associated with an Activity.
    // This does not mean the Activity is fully initialized.
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        // Store the listener (activity) that will have events fired once the fragment is attached
        if (context instanceof Callbacks) {
            mCallbacks = (Callbacks) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement MoviesFragment.Callbacks");
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);

        viewModel = ViewModelProviders.of(this).get(MoviesViewModel.class);
        QUERY_URL_SORT_PATH = MoviesPreferences.getPreferredSortOrder(mContext);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(getString(R.string.key_loader_has_finished_loading))) {
                hasFinishedLoading = savedInstanceState.getBoolean(getString(R.string.key_loader_has_finished_loading));
            }
            if (savedInstanceState.containsKey(getString(R.string.key_pages_finished))) {
                pagesFinished = savedInstanceState.getBoolean(getString(R.string.key_pages_finished));
            }
            if (savedInstanceState.containsKey(getString(R.string.key_loading_indicator_was_there))) {
                indicatorWasThere = savedInstanceState.getBoolean(getString(R.string.key_loading_indicator_was_there));
            }
            if (savedInstanceState.containsKey(getString(R.string.key_container_error_visibility))) {
                containerErrorVisibility = savedInstanceState.getInt(getString(R.string.key_container_error_visibility));
            }
            if (savedInstanceState.containsKey(getString(R.string.key_internet_error_occurred))) {
                internetErrorOccurred = savedInstanceState.getBoolean(getString(R.string.key_internet_error_occurred));
            }
            if (savedInstanceState.containsKey(getString(R.string.key_is_recyclerview_scrollable))) {
                isRecyclerViewScrollable = savedInstanceState.getBoolean(getString(R.string.key_is_recyclerview_scrollable));
            }
        }
        numColumns = LayoutUtils.calculateNoOfColumns(mContext, 160);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_movies, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View rootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        setUp(rootView);

        PreferenceManager.getDefaultSharedPreferences(mContext)
                .registerOnSharedPreferenceChangeListener(this);
    }

    // This method is called after the parent Activity's onCreate() method has completed.
    // Accessing the view hierarchy of the parent activity must be done in the onActivityCreated.
    // At this point, it is safe to search for activity View objects by their ID, for example.
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // This will initialize the loader the first time activity is created
        // and when orientation changes while loader is loading its data.
        // This is to ensure that our loader callbacks will actually return us some data.
        // If loader has already loaded its data and returned to us in onLoadFinished()
        // and then orientation change happens, we don't want to unnecessary initialize it
        if (!hasFinishedLoading) {
            if (!QUERY_URL_SORT_PATH.equals(getString(R.string.value_sort_order_favorites))) {
                getLoaderManager().initLoader(MOVIES_LOADER_ID, null, this);
            }
        }
        if (QUERY_URL_SORT_PATH.equals(getString(R.string.value_sort_order_favorites))) {
            getLoaderManager().initLoader(FAVORITES_LOADER_ID, null, this);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (indicatorWasThere) {
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        // If there is a change in preferences, clear out or reset all the existing data
        // and restart the loader
        if (PREFERENCES_HAVE_BEEN_UPDATED) {
            Log.d(LOG_TAG, "onStart: preferences were updated");

            // we want to hide the details fragment pane at this point if there is any.
            mCallbacks.onPreferenceChanged();

            isRecyclerViewScrollable = false;

            QUERY_URL_SORT_PATH = MoviesPreferences.getPreferredSortOrder(mContext);
            invalidateData();
            hasFinishedLoading = false;
            pagesFinished = false;
            if (!QUERY_URL_SORT_PATH.equals(getString(R.string.value_sort_order_favorites))) {
                getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
            } else {
                getLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, this);
            }
            PREFERENCES_HAVE_BEEN_UPDATED = false;
        }

        // Should update toolbar scrolling here because we're doing the MAIN check for favorites here,
        // and it we may expect different behaviours(on/off) for toolbar scrolling for different orientations
        // when we've few movies which fits in 1 orientation but not in another one.
        //
        // Don't make any call to setToolbarScrolling() when isRecyclerViewScrollable is true because when its true,
        // android will automatically handle it for us as we've assign Ids in activity_main layout file.
        // Otherwise a bug will appear as when we come back from DetailActivity or when we're in favorites mode,
        // toolbar will appear when its already collapsed!
        if (QUERY_URL_SORT_PATH.equals(getString(R.string.value_sort_order_favorites))) {
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    boolean lRVScrollable = mRecyclerView.canScrollVertically(-1) || mRecyclerView.canScrollVertically(1);
                    // still not a good solution & may cause unexpected behaviours
                    if (((MainActivity) mContext).mTwoPane && lRVScrollable && layoutManager.findFirstCompletelyVisibleItemPosition() != 0) {
                        mCallbacks.onActionBarVisibilityRequired(false);
                    }
                    Log.d("RecyclerScroll", "lScrollable: " + lRVScrollable);

                    if (isRecyclerViewScrollable != lRVScrollable || !lRVScrollable || ((MainActivity) mContext).mTwoPane) {
                        isRecyclerViewScrollable = lRVScrollable;
                        ((MainActivity) mContext).setToolbarScrolling(isRecyclerViewScrollable);

                    }
                }
            });
        } else {
            if (!isRecyclerViewScrollable || ((MainActivity) mContext).mTwoPane)
                ((MainActivity) mContext).setToolbarScrolling(isRecyclerViewScrollable);
        }

        // At this time if loader is null, it means that () system probably has killed our
        // application process or killed our activity/fragment without any notice.
        if (getLoaderManager().getLoader(MOVIES_LOADER_ID) == null && !QUERY_URL_SORT_PATH.equals(getString(R.string.value_sort_order_favorites))) {
            if (DEBUG_WITH_TOAST)
                Toast.makeText(mContext, "Loader is null, will create one", Toast.LENGTH_SHORT).show();
            hasFinishedLoading = false;
            pagesFinished = false;
            getLoaderManager().initLoader(MOVIES_LOADER_ID, null, this);
        }
        if (DEBUG_WITH_TOAST)
            Toast.makeText(mContext, "Total items: " + viewModel.getSize(), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(getString(R.string.key_loader_has_finished_loading), hasFinishedLoading);
        outState.putBoolean(getString(R.string.key_pages_finished), pagesFinished);
        outState.putBoolean(getString(R.string.key_loading_indicator_was_there), indicatorWasThere);
        outState.putInt(getString(R.string.key_container_error_visibility), mInternetErrorContainer.getVisibility());
        outState.putBoolean(getString(R.string.key_internet_error_occurred), internetErrorOccurred);
        outState.putBoolean(getString(R.string.key_is_recyclerview_scrollable), isRecyclerViewScrollable);
        Log.d(LOG_TAG, "Data persisted");
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        PreferenceManager.getDefaultSharedPreferences(mContext)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    // This method is called when the fragment is no longer connected to the Activity
    // Any references saved in onAttach should be nulled out here to prevent memory leaks.
    @Override
    public void onDetach() {
        super.onDetach();
        this.mContext = null;
    }

    /*------------------------------------------------------------------------------------------------*/



    /*------------------------------- < LOADER CALLBACKS > -------------------------------------------*/
    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle bundle) {
        // When there are no movies available
        if (viewModel.getMovies().isEmpty()) {
            mLoadingIndicator.setVisibility(View.VISIBLE);
            indicatorWasThere = true;

            // we want to show the Full action bar at this point (not collapsed)
            mCallbacks.onActionBarVisibilityRequired(true);
        }
        if (id == MOVIES_LOADER_ID) {
            return new MoviesLoader(mContext, getQueryUrl(viewModel.page + 1));
        } else if (id == FAVORITES_LOADER_ID) {
            return new CursorLoader(mContext,
                    MovieEntry.CONTENT_URI,
                    MovieEntry.getColumns(),
                    null,
                    null,
                    null);
        } else {
            throw new UnsupportedOperationException("Unsupported Loader with id: " + id);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object result) {
        // This check ensures we only enter this method to add data to our adapter
        // only if it haven't finished loading. And once we enter and reach the end of this method,
        // we have finished the loading, so we want receive any(duplicate) data.
        if (!hasFinishedLoading) {
            if (loader.getId() == MOVIES_LOADER_ID && !QUERY_URL_SORT_PATH.equals(getString(R.string.value_sort_order_favorites))) {
                if ((result instanceof Boolean)) {
                    // We reached this point because of no internet connection
                    internetErrorOccurred = true;
                }
                else {
                    List<Movie> movies = (List<Movie>) result;
                    internetErrorOccurred = false;

                    // In order for this to work, we override .equals() method of Object class in the {@link Movie.class}.
                    // IMPORTANT check..
                    // this will compare all the {@link Movie} objects inside the list.
                    if (movies != null && !viewModel.getMovies().containsAll(movies)) {
                        // incrementing page here works as expected but I haven't tested it VERY thoroghly
                        viewModel.incrementPage();
                        mAdapter.addAll(movies);
//            mRecyclerView.smoothScrollToPosition(viewModel.getSize());
                    }
                    else if (movies == null) {
                        // Now that we have modified our MovieLoader class,
                        // we won't be entering into this case if we don't send the queryUrl as null to the Loader.
                        mAdapter.removeNullData();
                        if (DEBUG_WITH_TOAST)
                            Toast.makeText(mContext, "Movies list is returned null", Toast.LENGTH_SHORT).show();
                    }
                    else if (movies.isEmpty()) {
                        if (DEBUG_WITH_TOAST)
                            Toast.makeText(mContext, "Internet broken midway", Toast.LENGTH_LONG).show();

                        // We reached this point because of maybe internet broken midway while fetching from internet (in QueryUtils)
                        // check when we get emply list as result if app is misbehaving in future?
                        internetErrorOccurred = true;
                    }
                    else if (viewModel.getMovies().containsAll(movies)) {
                        mAdapter.removeNullData();
                        if (QueryUtils.getCurrentPage() > QueryUtils.getTotalPages()) {
                            if (DEBUG_WITH_TOAST)
                                Toast.makeText(mContext, "You're on invalid page!", Toast.LENGTH_LONG).show();
                        }
                        else {
                            if (DEBUG_WITH_TOAST)
                                Toast.makeText(mContext, "Movies already in the list", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                Log.d("CurrentPage", "Current Page: " + QueryUtils.getCurrentPage() + "Total Pages: " + QueryUtils.getTotalPages());
                hasFinishedLoading = true;

                if (internetErrorOccurred) {
                    if (viewModel.getSize() == 0) {
                        mInternetErrorContainer.setVisibility(View.VISIBLE);
                    } else {
                        // some movies already loaded
                        mAdapter.removeNullData();
                        mAdapter.addNullData();
                    }
                }
                if (indicatorWasThere) {
                    mLoadingIndicator.setVisibility(View.GONE);
                    indicatorWasThere = false;
                }
            }
        }

        if (loader.getId() == FAVORITES_LOADER_ID && QUERY_URL_SORT_PATH.equals(getString(R.string.value_sort_order_favorites))) {
            Cursor moviesCursor = (Cursor) result;
            if (moviesCursor != null && mFavoritesCursor != moviesCursor && moviesCursor.moveToFirst()) {
                mFavoritesCursor = moviesCursor;
                List<Movie> movies = new ArrayList<>();
                do {
                    int id = moviesCursor.getInt(INDEX_COL_MOVIE_ID);
                    String posterPath = moviesCursor.getString(INDEX_COL_POSTER_PATH);
                    String backdropPath = moviesCursor.getString(INDEX_COL_BACKDROP_PATH);
                    String originalLanguage = moviesCursor.getString(INDEX_COL_ORIGINAL_LANGUAGE);
                    String title = moviesCursor.getString(INDEX_COL_TITLE);
                    double voteAverage = moviesCursor.getDouble(INDEX_COL_VOTE_AVERAGE);
                    String overview = moviesCursor.getString(INDEX_COL_OVERVIEW);
                    long releaseDateMillis = moviesCursor.getLong(INDEX_COL_RELEASE_DATE_MILLIS);

                    Movie movie = new Movie(id, posterPath, backdropPath, originalLanguage, title, voteAverage, overview, releaseDateMillis);
                    Log.d("CursorResult", movie.toString());
                    movies.add(movie);
                } while (moviesCursor.moveToNext());

                // deletion or insertion probably happened here
                if (!movies.equals(viewModel.getMovies())) {
                    mAdapter.clearAll();
                    mAdapter.addAll(movies);
                    if (viewModel.getSize() > 0) {
                        mNoFavouriteMoviesContainer.setVisibility(View.GONE);
                    }

                    // update toolbar scrolling for favorites mode
                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            boolean lRVScrollable = mRecyclerView.canScrollVertically(-1) || mRecyclerView.canScrollVertically(1);
                            Log.d("RecyclerScrollFav", "Scrollable: " + lRVScrollable);

                            if (isRecyclerViewScrollable != lRVScrollable) {
                                isRecyclerViewScrollable = lRVScrollable;
                                ((MainActivity) mContext).setToolbarScrolling(isRecyclerViewScrollable);
                            }
                        }
                    });
                }
            } else if (moviesCursor != null && moviesCursor.getCount() == 0) {
                mAdapter.clearAll();
            }


            if (indicatorWasThere) {
                mLoadingIndicator.setVisibility(View.GONE);
                indicatorWasThere = false;
            }

            // Solves the bug of favourite movies container scrolling little bit up (post not required maybe)
            if (viewModel.getSize() == 0) {
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        mNoFavouriteMoviesContainer.setVisibility(View.VISIBLE);
                    }
                });

            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        Log.d(LOG_TAG, "Loader having id:" + loader.getId() + " is being resetted");
    }
    /*------------------------------------------------------------------------------------------------*/



    /*------------------------------ < PREFERENCE CALLBACK > -----------------------------------------*/
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        /*
         * Set the flag to true/false accordingly so that when control returns to {@link MoviesFragment}, it can refresh the
         * data
         */
        if (key.equals(getString(R.string.key_movies_sort_order))) {
            PREFERENCES_HAVE_BEEN_UPDATED = !QUERY_URL_SORT_PATH.equals(sharedPreferences.getString(key, ""));
        }
    }



    /*------------------------------ < MENU RELATED CALLBACKS > --------------------------------------*/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.moviesfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(mContext, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /*------------------------------------------------------------------------------------------------*/



    /*------------------------------ < MovieListAdapter CALLBACKS > ----------------------------------*/
    @Override
    public void onRetryClick() {
        if (!QUERY_URL_SORT_PATH.equals(getString(R.string.value_sort_order_favorites))) {
            mAdapter.removeNullData();
            internetErrorOccurred = false;
            mAdapter.addNullData();
            hasFinishedLoading = false;
            getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
        }
    }



    /*------------------------------ < OTHER HELPER METHODS > ----------------------------------------*/

    /**
     * Helper method to use in onViewCreated(...) method.
     */
    private void setUp(View rootView) {
        mRecyclerView = rootView.findViewById(R.id.movies_recycler_view);
        mInternetErrorContainer = rootView.findViewById(R.id.container_internet_error);
        mNoFavouriteMoviesContainer = rootView.findViewById(R.id.container_favorites_not_found);
        mRetryBTN = rootView.findViewById(R.id.retry_button);
        mLoadingIndicator = rootView.findViewById(R.id.loading_indicator);

        mAdapter = new MovieListAdapter(mContext, viewModel.getMovies(), this);

        // Fix the issue of RecyclerView creating ViewHolders excessively
        mRecyclerView.getRecycledViewPool().setMaxRecycledViews(MovieListAdapter.VIEW_TYPE_MOVIE, 50);
        mRecyclerView.setHasFixedSize(true);

        layoutManager = new GridLayoutManager(mContext, numColumns);
        mRecyclerView.setLayoutManager(layoutManager);

        // Required to take full screen width size for the VIEW_TYPE_LOADING
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (mAdapter.getItemViewType(position) == MovieListAdapter.VIEW_TYPE_MOVIE) {
                    return 1;
                }
                else {
                    return layoutManager.getSpanCount();
                }
            }
        });

        mRecyclerView.addItemDecoration(new MovieItemDecoration(getResources().getInteger(R.integer.recyclerview_decoration_offset)));
        mRecyclerView.setItemAnimator(new MoviePosterAnimator());
        mRecyclerView.setAdapter(mAdapter);

        if (containerErrorVisibility == View.VISIBLE) {
            mInternetErrorContainer.setVisibility(View.VISIBLE);
        } else if (containerErrorVisibility == View.GONE) {
            mInternetErrorContainer.setVisibility(View.GONE);
        }

        mRetryBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!QUERY_URL_SORT_PATH.equals(getString(R.string.value_sort_order_favorites))) {
                    hasFinishedLoading = false;
                    getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, MoviesFragment.this);
                    mInternetErrorContainer.setVisibility(View.GONE);
                }
            }
        });

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // detect & enable toolbar scrolling for popular or top rated sort orders mode
                // if its not enabled
                if (!isRecyclerViewScrollable && !QUERY_URL_SORT_PATH.equals(getString(R.string.value_sort_order_favorites))) {
                    // true only when either recyclerview is scrollable in either direction (up/down)
                    boolean lRVScrollable = mRecyclerView.canScrollVertically(-1) || mRecyclerView.canScrollVertically(1);

                    Log.d("RecyclerScroll", "Scrollable: " + lRVScrollable);
                    if (lRVScrollable) {
                        isRecyclerViewScrollable = lRVScrollable;
                        ((MainActivity) mContext).setToolbarScrolling(isRecyclerViewScrollable);
                    }
                }

                if (!QUERY_URL_SORT_PATH.equals(getString(R.string.value_sort_order_favorites)) && !internetErrorOccurred) {
                    currentItems = layoutManager.getChildCount();
                    scrolledOutItems = layoutManager.findFirstVisibleItemPosition();
                    totalItems = layoutManager.getItemCount();

                    if ((hasFinishedLoading && (layoutManager.findLastVisibleItemPosition() == viewModel.getSize() - 1) && !isScrolling) ||
                            isScrolling && hasFinishedLoading && (currentItems + scrolledOutItems >= totalItems - (layoutManager.getSpanCount() * 2)) && (!pagesFinished)) {
//                    isScrolling = false;
                        hasFinishedLoading = false;
                        loadMore();
                    } else if (!hasFinishedLoading) {
                        Log.d(LOG_TAG, "Previously created loader hasn't finished loading");
                    }
                }
            }
        });
    }

    private void loadMore() {
        if (viewModel.page < QueryUtils.getTotalPages()) {
            // Add null data only if there are some items in the adapter data
            if (mAdapter.getItemCount() > 0) {
                mAdapter.addNullData();
            }

            getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
        } else {
            Toast.makeText(mContext, getString(R.string.pages_finished_message), Toast.LENGTH_LONG).show();
            pagesFinished = true;
            hasFinishedLoading = true;
        }
    }

    private String getQueryUrl(int pageNo) {
        Uri uri = Uri.parse(BASE_QUERY_URL).buildUpon()
                .appendPath(QUERY_URL_SORT_PATH)
                .appendQueryParameter(QUERY_URL_API_KEY_PARAMETER_KEY, QUERY_URL_API_KEY_PARAMETER_VALUE)
                .appendQueryParameter(QUERY_URL_LANGUAGE_PARAMETER_KEY, QUERY_URL_LANGUAGE_PARAMETER_VALUE)
                .appendQueryParameter(QUERY_URL_PAGE_PARAMETER_KEY, String.valueOf(pageNo))
                .build();
        Log.d("MoviesQueryUrl", uri.toString());

        return uri.toString();
    }

    private void invalidateData() {
        internetErrorOccurred = false;
        mInternetErrorContainer.setVisibility(View.GONE);
        mNoFavouriteMoviesContainer.setVisibility(View.GONE);
        mAdapter.clearAll();
        viewModel.setPage(0);
    }

    /**
     * Call this method to set span counts for Movies RecyclerView.
     *
     * @param spans
     */
    public void setSpans(int spans) {
        if (layoutManager.getSpanCount() != spans)
            layoutManager.setSpanCount(spans);
    }

    public void setDefaultSpans() {
        layoutManager.setSpanCount(numColumns);
    }

    public void scrollToPos(final int position) {
        // scrollToPosition(int pos) works fine but no animation.
//        mRecyclerView.scrollToPosition(position);

        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                // smoothScrollToPosition does animation and it requires for the recyclerview to do a lot of work,
                // so sometimes it doesn't work properly. Hence it works fine on Post method when recyclerview had
                // done its other work.
                mRecyclerView.smoothScrollToPosition(position);
            }
        });
    }
    /*------------------------------------------------------------------------------------------------*/



    /*--------------------------- < VIEWMODEL FOR HOLDING MOVIES RELATED DATA > ------------------------*/
    public static class MoviesViewModel extends ViewModel {
        private List<Movie> movieList;
        private int page;

        public MoviesViewModel() {
            init();
        }

        private void init() {
            if (movieList == null) {
                movieList = new ArrayList<>();
                setPage(0);
            }
        }

        private void addMovies(List<Movie> movies) {
            movieList.addAll(movies);
        }

        private List<Movie> getMovies() {
            return movieList;
        }

        private int getSize() {return movieList == null ? 0 : movieList.size();}

        private void incrementPage() {page++;}

        private void setPage(int num) {
            page = num;
        }
    }
    /*------------------------------------------------------------------------------------------------*/



    /*---------------------------- < DEFINE INTERFACE > ----------------------------------------------*/

    // This will be used to communicate with Host activity and Host(Main) Activity must implement
    public interface Callbacks {
        // will be called when actionbar is required to show
        void onActionBarVisibilityRequired(boolean visibilityRequired);

        // Will notify the host activity that preferences have been changed.
        void onPreferenceChanged();
    }
    /*------------------------------------------------------------------------------------------------*/
}
