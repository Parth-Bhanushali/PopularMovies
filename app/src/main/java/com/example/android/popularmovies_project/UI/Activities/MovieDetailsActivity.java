package com.example.android.popularmovies_project.UI.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.popularmovies_project.Fragments.MovieDetailFragment;
import com.example.android.popularmovies_project.Models.Movie;
import com.example.android.popularmovies_project.R;
import com.example.android.popularmovies_project.Utils.LayoutUtils;
import com.example.android.popularmovies_project.Utils.MovieUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class MovieDetailsActivity extends AppCompatActivity {

    private static final String LOG_TAG = MovieDetailsActivity.class.getSimpleName();
    private static final boolean DEBUG_WITH_TOAST = false;

    private Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView backdropIV;
    private View scrimTopDown, scrim;

    private int statusBarColor;
    private int statusBarAlpha = 0;

    private boolean shouldChangeInitialOffset = true;
    private int collapsingToolbarColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        init(savedInstanceState);
        hideScrimViews();


        Intent intent = getIntent();
        Movie movie = intent.getParcelableExtra(MovieDetailFragment.ARG_MOVIE);
        String posterBitmapFileName = intent.getStringExtra(MovieDetailFragment.ARG_POSTER_BITMAP);
        Log.d(LOG_TAG, movie.toString());
        collapsingToolbarLayout.setTitle(movie.getTitle());
        String link = MovieUtils.getImageLink(MovieUtils.IMAGE_QUALITY_ORIGINAL, movie.getBackdropPath());
        loadAppBarImage(link);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(MovieDetailFragment.ARG_MOVIE,
                    movie);
            arguments.putString(MovieDetailFragment.ARG_POSTER_BITMAP,
                    posterBitmapFileName);
            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
        }
    }

    private void init(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(getString(R.string.key_color_statusbar))) {
                statusBarColor = savedInstanceState.getInt(getString(R.string.key_color_statusbar));
            }
            if (savedInstanceState.containsKey(getString(R.string.key_color_collapsing_toolbar))) {
                collapsingToolbarColor = savedInstanceState.getInt(getString(R.string.key_color_collapsing_toolbar));
            }
            shouldChangeInitialOffset = false;
        } else {
            statusBarColor = getResources().getColor(R.color.detailsPreStatusBarColor);
            collapsingToolbarColor = getResources().getColor(R.color.detailsPreCollapsingToolbarBackgroundColor);
        }

        changeStatusBarColor();

        toolbar = findViewById(R.id.toolbar_details);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        appBarLayout = findViewById(R.id.appbar_details);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar_details);
        collapsingToolbarLayout.setBackgroundColor(collapsingToolbarColor);
        backdropIV = findViewById(R.id.backdrop_image_view);
        scrimTopDown = findViewById(R.id.scrim_topdown);
        scrim = findViewById(R.id.scrim);

        setUpAppBar();
    }

    /**
     * Must be called after finding all the views in the Activity.
     */
    private void setUpAppBar() {
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float range = (float) -appBarLayout.getTotalScrollRange();
                int alpha = (int) (255 * (1.0f - (float) verticalOffset / range));
                backdropIV.setImageAlpha(alpha);
                if (scrimTopDown.getVisibility() == View.VISIBLE && scrim.getVisibility() == View.VISIBLE) {
                    scrimTopDown.getBackground().setAlpha(alpha);
                    scrim.getBackground().setAlpha(alpha);
                }

                if (alpha <= 120) {
                    statusBarAlpha = (int) (255 - (alpha * 2.125));
                } else {
                    statusBarAlpha = 0;
                }

                changeStatusBarColor();

                Log.d(LOG_TAG, "ImageView Alpha: " + backdropIV.getImageAlpha());
                Log.d(LOG_TAG, "Scrim TopDown Alpha: " + scrimTopDown.getBackground().getAlpha());
                Log.d(LOG_TAG, "Scrim Alpha: " + scrim.getBackground().getAlpha());
            }
        });

        appBarLayout.post(new Runnable() {
            @Override
            public void run() {
                if (shouldChangeInitialOffset) {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
                    AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
                    behavior.setTopAndBottomOffset(-(backdropIV.getHeight() / 4));
                }
            }
        });
    }

    private void showScrimViews() {
        scrimTopDown.setVisibility(View.VISIBLE);
        scrim.setVisibility(View.VISIBLE);
    }

    private void hideScrimViews() {
        scrimTopDown.setVisibility(View.INVISIBLE);
        scrim.setVisibility(View.INVISIBLE);
    }

    private void loadAppBarImage(String backdropLink) {
        Picasso.with(this)
                .load(backdropLink)
                .config(Bitmap.Config.RGB_565)
                .into(backdropIV, new Callback() {
                    @Override
                    public void onSuccess() {
                        showScrimViews();

                        if (collapsingToolbarColor != 0 && statusBarColor != 0) {
                            BitmapDrawable bitmapDrawable = (BitmapDrawable) backdropIV.getDrawable();
                            Bitmap bitmap = bitmapDrawable.getBitmap();
                            fetchToolbarColors(bitmap);
                        }
                    }

                    @Override
                    public void onError() {
                        if (DEBUG_WITH_TOAST)
                            Toast.makeText(MovieDetailsActivity.this, "Unable to load image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchToolbarColors(Bitmap bitmap) {
        Palette.from(bitmap)
                .generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(@Nullable Palette palette) {
                        if (palette == null) {
                            Log.e(LOG_TAG, "Can't generate the palette.");
                            return;
                        }
                        Palette.Swatch swatch = palette.getMutedSwatch();
                        if (swatch != null) {
                            int rgb = swatch.getRgb();
                            // setup collapsing toolbar color generated by Palette.
                            collapsingToolbarLayout.setBackgroundColor(rgb);

                            // Assign a new color value for statusbar darker than chosen color from Palette.
                            statusBarColor = LayoutUtils.manipulateColor(rgb, (float) 0.8);

                            // At this point, we will call this method to change the color of status bar with whatever
                            // alpha value assigned already.
                            changeStatusBarColor();

                            collapsingToolbarColor = rgb;
                        }
                    }
                });
    }

    /**
     * Changes statusbar color with the color assigned in statusBarColor and alpha value assigned in statusBarAlpha.
     * <p>
     * Call this method as soon as value/s of {@literal statusBarColor} &/or {@literal statusBarAlpha} changes.
     */
    private void changeStatusBarColor() {
        if (statusBarAlpha >= 0 && statusBarAlpha <= 255)
            getWindow().setStatusBarColor(ColorUtils.setAlphaComponent(statusBarColor, statusBarAlpha));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(getString(R.string.key_color_statusbar), statusBarColor);
        outState.putInt(getString(R.string.key_color_collapsing_toolbar), collapsingToolbarColor);
        super.onSaveInstanceState(outState);
    }
}
