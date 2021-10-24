package com.example.android.popularmovies_project.Adapters;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.android.popularmovies_project.Fragments.MoviesFragment;
import com.example.android.popularmovies_project.Models.Movie;
import com.example.android.popularmovies_project.R;
import com.example.android.popularmovies_project.UI.MoviePosterAnimator;
import com.example.android.popularmovies_project.Utils.MovieUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.ViewHolder> {

    private static final boolean DEBUG_PICASSO = false;
    public static final int VIEW_TYPE_MOVIE = 0;
    public static final int VIEW_TYPE_LOADING = 1;
    public static final int VIEW_TYPE_INTERNET_ERROR = 2;
    public static final String ACTION_MOVIE_ITEM_CLICKED = "action_movie_poster_clicked";
    private final OnRetryClickListener mListener;
    private Context context;
    private List<Movie> movies;
    private File postersDir;
    private int holdersCount;

    public MovieListAdapter(Context context, List<Movie> movies, OnRetryClickListener onRetryClickListener) {
        this.context = context;
        this.movies = movies;
        postersDir = context.getDir("postersDir", Context.MODE_PRIVATE);
        mListener = onRetryClickListener;
    }


    @NonNull
    @Override
    public MovieListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("Holder", "onCreateViewHolder (" + ++holdersCount + ")");
        View view;
        ViewHolder viewHolder;

        switch (viewType) {
            case VIEW_TYPE_MOVIE:
                view = LayoutInflater.from(context)
                        .inflate(R.layout.list_item_movie, parent, false);

                viewHolder = new MovieViewHolder(view);

                final ViewHolder finalViewHolder = viewHolder;
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (MoviePosterAnimator.canClick()) {
                            int adapterPosition = finalViewHolder.getAdapterPosition();

                            Movie movie = getItem(adapterPosition);
                            if (movie != null) {
                                onMovieClick(adapterPosition, movie);
                            }
                        }

                    }
                });
                break;
            case VIEW_TYPE_LOADING:
                view = LayoutInflater.from(context)
                        .inflate(R.layout.row_progress, parent, false);
                viewHolder = new ProgressViewHolder(view);
                break;
            case VIEW_TYPE_INTERNET_ERROR:
                view = LayoutInflater.from(context)
                        .inflate(R.layout.row_error, parent, false);

                viewHolder = new ErrorViewHolder(view);
                ((ErrorViewHolder) viewHolder).rowRetryBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onRetryClick();
                    }
                });
                break;
            default:
                throw new UnsupportedOperationException("ViewType: " + viewType + " is not supported");
        }
        return viewHolder;
    }

    private void onMovieClick(int position, Movie movie) {
        Bundle data = new Bundle();
        data.putString("action", ACTION_MOVIE_ITEM_CLICKED);
        data.putParcelable("movie", movie);

        // This data(bundle) will be passed as payload for ItemHolderInfo in our animator class
        notifyItemChanged(position, data);

        // You can save any information in database at this point
    }

    @Override
    public void onBindViewHolder(@NonNull MovieListAdapter.ViewHolder viewHolder, int position) {
        Log.d("MovieListAdapter", "position: " + position);
        if (viewHolder instanceof MovieViewHolder) {
            Movie movie = movies.get(position);

            loadPoster(viewHolder, movie);

        } else if (viewHolder instanceof ProgressViewHolder) {
            // Logic for progressbar if needed.
        } else if (viewHolder instanceof ErrorViewHolder) {
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            holder.itemView.setAlpha(1);
        }
    }

    @Override
    public int getItemCount() {
        return movies == null ? 0 : movies.size();
    }

    // Convenient method to get the Movie item at given position
    private Movie getItem(int position) {
        if (position != RecyclerView.NO_POSITION)
            return movies.get(position);
        else
            return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) != null) {
            return VIEW_TYPE_MOVIE;
        } else {
            if (MoviesFragment.internetErrorOccurred) {
                return VIEW_TYPE_INTERNET_ERROR;
            }
            return VIEW_TYPE_LOADING;
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof MovieViewHolder) {
            ((MovieViewHolder) holder).cleanUp();
        }
    }

    /**
     * Adding null data will indicate adapter to show loading indicator
     */
    public void addNullData() {
        movies.add(null);
        notifyItemInserted(movies.size() - 1);
    }

    /**
     * Remove null data before adding actual data to the adapter
     * This should remove the loading indicator
     * You should call this in onLoadFinished(...) if no data is going to passed in adapter
     * & addNullData() is called already.
     * If you're passing some data to adapter, then in that case you don't need to call this method.
     */
    public void removeNullData() {
        if (getItemCount() != 0) {
            if (movies.get(movies.size() - 1) == null) {
                movies.remove(movies.size() - 1);
                notifyItemRemoved(movies.size());
            }
        }
    }

    public void addAll(List<Movie> movieList) {
        removeNullData();
        movies.addAll(movieList);
        notifyDataSetChanged();
    }

    public void clearAll() {
        movies.clear();
        notifyDataSetChanged();
    }


    private void loadPoster(final MovieListAdapter.ViewHolder viewHolder, final Movie movie) {

        if (DEBUG_PICASSO) {
            Picasso.with(context).setIndicatorsEnabled(true);
            Picasso.with(context).setLoggingEnabled(true);
        }

        // Still, need to show some placeholder/error images
        if (MoviesFragment.QUERY_URL_SORT_PATH.equals(context.getString(R.string.value_sort_order_favorites))) {
            Picasso.with(context)
                    .load(new File(postersDir, movie.getTitle() + ".jpg"))
                    .config(Bitmap.Config.RGB_565)
                    .placeholder(R.drawable.placeholder)
                    .into(((MovieViewHolder) viewHolder).posterImageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(context)
                                    .load(MovieUtils.getImageLink(MovieUtils.IMAGE_QUALITY_W342, movie.getPosterPath()))
                                    .config(Bitmap.Config.RGB_565)
                                    .placeholder(R.drawable.broken_image_placeholder)
                                    .into(((MovieViewHolder) viewHolder).posterImageView, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            BitmapDrawable bitmapDrawable = (BitmapDrawable) ((MovieViewHolder) viewHolder).posterImageView.getDrawable();
                                            Bitmap bitmap = bitmapDrawable.getBitmap();
                                            saveToInternalStorage(bitmap, movie.getTitle());
                                        }

                                        @Override
                                        public void onError() {

                                        }
                                    });
                        }
                    });
        } else {
            Picasso.with(context)
                    .load(MovieUtils.getImageLink(MovieUtils.IMAGE_QUALITY_W342, movie.getPosterPath()))
                    .config(Bitmap.Config.RGB_565)
                    .error(R.drawable.broken_image_placeholder)
                    .into(((MovieViewHolder) viewHolder).posterImageView);
        }
    }

    private void saveToInternalStorage(Bitmap bitmapImage, String imageName) {
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
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


    public interface OnRetryClickListener {
        void onRetryClick();
    }

    public class MovieViewHolder extends ViewHolder {
        public final View mView;
        public ImageView posterImageView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            posterImageView = itemView.findViewById(R.id.iv_poster);
            mView = itemView;
        }

        public void cleanUp() {
            final Context context = mView.getContext();
            Picasso.with(context).cancelRequest(posterImageView);
        }
    }

    private class ProgressViewHolder extends ViewHolder {
        private ProgressBar loadingIndicator;

        public ProgressViewHolder(View itemView) {
            super(itemView);
            loadingIndicator = itemView.findViewById(R.id.progressbar);
        }
    }

    private class ErrorViewHolder extends ViewHolder {
        private Button rowRetryBTN;

        public ErrorViewHolder(View itemView) {
            super(itemView);
            rowRetryBTN = itemView.findViewById(R.id.row_error_retry_button);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

}
