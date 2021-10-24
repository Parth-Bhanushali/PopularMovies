package com.example.android.popularmovies_project.UI.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.example.android.popularmovies_project.Fragments.MovieDetailFragment;
import com.example.android.popularmovies_project.Fragments.MoviesFragment;
import com.example.android.popularmovies_project.Models.Movie;
import com.example.android.popularmovies_project.R;
import com.example.android.popularmovies_project.Utils.LayoutUtils;
import com.example.android.popularmovies_project.UI.MoviePosterAnimator;

import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity implements MoviesFragment.Callbacks,
        MoviePosterAnimator.AnimationCallbacks {

    public boolean mTwoPane;
    private int twoPaneSpanCount;
    private View mainFragment, detailContainer;
    private MoviesFragment moviesFragment;
    private boolean shouldToolbarScroll = true;
    private Toolbar toolbar;
    private AppBarLayout appBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        mainFragment = findViewById(R.id.frag_main);
        detailContainer = findViewById(R.id.movie_detail_container);

        moviesFragment = (MoviesFragment) getSupportFragmentManager().findFragmentById(R.id.frag_main);

        mTwoPane = detailContainer != null;

        if (mTwoPane) {
            mainFragment.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mainFragment.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int width = mainFragment.getMeasuredWidth();
                    twoPaneSpanCount = LayoutUtils.calculateNoOfColumns(MainActivity.this, 160, width);
                    detailContainer.setVisibility(View.GONE);
                }
            });
        }

        appBarLayout = findViewById(R.id.appbar_main);
        setAppBarDragging(false);
    }


    public void setToolbarScrolling(boolean shouldScroll) {
        if (shouldToolbarScroll) {
            // turn on scrolling
            if (shouldScroll) {
                AppBarLayout.LayoutParams toolbarLayoutParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
                toolbarLayoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
                toolbar.setLayoutParams(toolbarLayoutParams);

                CoordinatorLayout.LayoutParams appBarLayoutParams = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
                appBarLayoutParams.setBehavior(new AppBarLayout.Behavior());
                appBarLayout.setLayoutParams(appBarLayoutParams);
            }
            // turn off scrolling
            else {
                AppBarLayout.LayoutParams toolbarLayoutParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
                toolbarLayoutParams.setScrollFlags(0);
                toolbar.setLayoutParams(toolbarLayoutParams);

                CoordinatorLayout.LayoutParams appBarLayoutParams = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
                appBarLayoutParams.setBehavior(null);
                appBarLayout.setLayoutParams(appBarLayoutParams);
            }
        }
    }

    /**
     * Utility method for Enabling or disabling the scrolling of {@link AppBarLayout}
     * when {@link AppBarLayout} gets scrolled.
     */
    private void setAppBarDragging(final boolean shouldDrag) {
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();

        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return shouldDrag;
            }
        });

        params.setBehavior(behavior);
    }

    // Now we can define the action to take in the activity when the fragment event fires
    // This is implementing the `MoviesFragment.Callbacks` interface methods
    @Override
    public void onActionBarVisibilityRequired(boolean visibilityRequired) {
        Log.d("MainActivity", "callback received");
        appBarLayout.setExpanded(visibilityRequired);
    }

    @Override
    public void onPreferenceChanged() {
        if (mTwoPane) {
            detailContainer.setVisibility(View.GONE);
            moviesFragment.setDefaultSpans();
        }
        shouldToolbarScroll = true;
    }


    @Override
    public void onMoviePosterAnimationFinished(Movie movie, int position, Bitmap posterBitmap) {
        Log.d("Animation callback", "Success");
        Log.d("AnimationBitmap", "Poster bitmap available: " + (posterBitmap != null));

        if (!mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(MovieDetailFragment.ARG_MOVIE, movie);
            arguments.putString(MovieDetailFragment.ARG_POSTER_BITMAP, createImageFileFromBitmap(posterBitmap));
            Intent intent = new Intent(this, MovieDetailsActivity.class);
            intent.putExtras(arguments);
            startActivity(intent);
        } else {
            Bundle arguments = new Bundle();
            arguments.putParcelable(MovieDetailFragment.ARG_MOVIE, movie);
            arguments.putString(MovieDetailFragment.ARG_POSTER_BITMAP, createImageFileFromBitmap(posterBitmap));
            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);

            setToolbarScrolling(false);
            shouldToolbarScroll = false;
            onActionBarVisibilityRequired(true);

            moviesFragment.setSpans(twoPaneSpanCount);
            moviesFragment.scrollToPos(position);
            if (detailContainer.getVisibility() == View.GONE) {
                detailContainer.setVisibility(View.VISIBLE);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment)
                    .commit();
        }
    }

    /**
     * Saves bitmap to internal storage
     *
     * @param bitmapToTransfer is the bitmap that should be stored in storage,
     *                         which you want to pass between activities.
     * @return the name of the file
     */
    public String createImageFileFromBitmap(Bitmap bitmapToTransfer) {
        String fileName = null;
        if (bitmapToTransfer != null) {
            try {
                fileName = "tempImage";
                FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
                bitmapToTransfer.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                // cleanup
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
                fileName = null;
            }
        }
        return fileName;
    }
}
