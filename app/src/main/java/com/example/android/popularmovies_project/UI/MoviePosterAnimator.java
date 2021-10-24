package com.example.android.popularmovies_project.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.animation.DecelerateInterpolator;

import com.example.android.popularmovies_project.Adapters.MovieListAdapter;
import com.example.android.popularmovies_project.Fragments.MoviesFragment;
import com.example.android.popularmovies_project.Models.Movie;
import com.example.android.popularmovies_project.R;

import java.util.List;

public class MoviePosterAnimator extends DefaultItemAnimator {

    // Check https://robots.thoughtbot.com/android-interpolators-a-visual-guide
    // To know about interpolators
    private static final DecelerateInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    // Handles whether on not we want to do something when item gets clicked
    private static boolean canClick = true;

    private AnimationCallbacks mCallbacks;

    /**
     * Whether or not any recyclerview item should do anything when got clicked.
     * Use this if you don't want other items to do anything when any one of them is being animated.
     */
    public static boolean canClick() {
        return canClick;
    }

    /**
     * When an item is changed, ItemAnimator can decide whether it wants to re-use the same ViewHolder
     * for animations or RecyclerView should create a copy of the item and ItemAnimator will use both
     * to run the animation (e.g. If the payload list is not empty, DefaultItemAnimator returns true.
     *
     * @param viewHolder
     * @return
     */
    @Override
    public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
        return true;
    }

    /**
     * Called by the RecyclerView before the layout begins. Item animator should record necessary information
     * about the View before it is potentially rebound, moved or removed.
     * The data returned from this method will be passed to the related animate** methods.
     *
     * @param state
     * @param viewHolder
     * @param changeFlags
     * @param payloads
     * @return
     */
    @NonNull
    @Override
    public ItemHolderInfo recordPreLayoutInformation(@NonNull RecyclerView.State state,
                                                     @NonNull RecyclerView.ViewHolder viewHolder,
                                                     int changeFlags,
                                                     @NonNull List<Object> payloads) {
        mCallbacks = (AnimationCallbacks) viewHolder.itemView.getContext();
        if (changeFlags == FLAG_CHANGED) {
            for (Object payload : payloads) {
                if (payload instanceof Bundle) {
                    return new MovieItemHolderInfo((Bundle) payload);
                }
            }
        }

        return super.recordPreLayoutInformation(state, viewHolder, changeFlags, payloads);
    }


    /**
     * Called by the RecyclerView when an adapter item is present both before and after the layout and RecyclerView
     * has received a notifyItemChanged(int) call for it. This method may also be called when notifyDataSetChanged()
     * is called and adapter has stable ids so that RecyclerView could still rebind views to the same ViewHolders.
     */
    @Override
    public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder,
                                 @NonNull RecyclerView.ViewHolder newHolder,
                                 @NonNull ItemHolderInfo preInfo,
                                 @NonNull ItemHolderInfo postInfo) {

        if (preInfo instanceof MovieItemHolderInfo) {
            MovieItemHolderInfo moviesItemHolderInfo =
                    (MovieItemHolderInfo) preInfo;
            MovieListAdapter.ViewHolder holder = (MovieListAdapter.ViewHolder) newHolder;

            if (moviesItemHolderInfo.bInfo.containsKey("action") &&
                    moviesItemHolderInfo.bInfo.containsKey("movie")) {

                String action = moviesItemHolderInfo.bInfo.getString("action");
                Movie movie = moviesItemHolderInfo.bInfo.getParcelable("movie");
                if (MovieListAdapter.ACTION_MOVIE_ITEM_CLICKED.equals(action)) {
                    animatePhoto(holder, movie);
                }
            }
        }

        return true;
    }


    private void animatePhoto(final MovieListAdapter.ViewHolder holder, final Movie movie) {
        final Context context = holder.itemView.getContext();

        holder.itemView.setScaleY(0.0f);
        holder.itemView.setScaleX(0.0f);

        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator scaleMoviePoster = ObjectAnimator.ofPropertyValuesHolder(holder.itemView,
                PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.95f, 1.0f),
                PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.95f, 1.0f));
        scaleMoviePoster.setInterpolator(DECELERATE_INTERPOLATOR);
//        scaleMoviePoster.setDuration(3500);

        scaleMoviePoster.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                canClick = false;
                super.onAnimationStart(animation);

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                dispatchAnimationFinished(holder);
                canClick = true;

                BitmapDrawable bitmapDrawable = (BitmapDrawable) ((MovieListAdapter.MovieViewHolder) holder).posterImageView.getDrawable();
                Bitmap poster = null;
                if (bitmapDrawable != null) {
                    Bitmap actualImage = bitmapDrawable.getBitmap();
                    Bitmap errorPlaceholder = BitmapFactory.decodeResource(context.getResources(), R.drawable.broken_image_placeholder);
                    Bitmap placeholder = BitmapFactory.decodeResource(context.getResources(), R.drawable.placeholder);

                    if (!actualImage.sameAs(placeholder) && !actualImage.sameAs(errorPlaceholder) && !MoviesFragment.QUERY_URL_SORT_PATH.equals(context.getString(R.string.value_sort_order_favorites))) {
                        poster = actualImage;
                    }
                }
                mCallbacks.onMoviePosterAnimationFinished(movie, holder.getAdapterPosition(), poster);

            }
        });

        animatorSet.play(scaleMoviePoster);
        animatorSet.start();
    }


    private static class MovieItemHolderInfo extends ItemHolderInfo {
        private Bundle bInfo;
        private MovieItemHolderInfo(Bundle bundle) {
            this.bInfo = bundle;
        }
    }


    /**
     * Callback interface which activity/fragment (same context) using this class as an animator on RecyclerView
     * must implement.
     */
    public interface AnimationCallbacks {
        /**
         * callback when Movie item finishes its animation and now ready to do
         * things like opening a new activity or fragment etc.
         * @param movie
         */
        void onMoviePosterAnimationFinished(Movie movie, int position, Bitmap posterBitmap);
    }
}
