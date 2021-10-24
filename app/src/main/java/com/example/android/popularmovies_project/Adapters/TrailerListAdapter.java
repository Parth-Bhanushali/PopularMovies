package com.example.android.popularmovies_project.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies_project.Models.Trailer;
import com.example.android.popularmovies_project.R;
import com.example.android.popularmovies_project.Utils.MovieUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TrailerListAdapter extends RecyclerView.Adapter<TrailerListAdapter.ViewHolder> {

    private static final String LOG_TAG = TrailerListAdapter.class.getSimpleName();
    private final Callbacks mCallbacks;
    private Context context;
    private List<Trailer> trailers;

    public TrailerListAdapter(Context context, List<Trailer> trailerList, Callbacks callbacks) {
        this.context = context;
        this.trailers = trailerList;
        this.mCallbacks = callbacks;
    }

    @NonNull
    @Override
    public TrailerListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.list_item_trailer, parent, false);

        final ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPos = holder.getAdapterPosition();
                Trailer trailer = getItem(adapterPos);

                mCallbacks.watch(trailer);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TrailerListAdapter.ViewHolder viewHolder, int position) {
        Trailer trailer = getItem(position);

        float marginRight = 0;
        if (position + 1 != getItemCount()) {
            marginRight = context.getResources().getDimension(R.dimen.trailer_thumbnail_margin);
        }

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) viewHolder.itemView.getLayoutParams();
        params.rightMargin = getPixels(marginRight / 2);

        Picasso.with(context)
                .load(MovieUtils.getTrailerThumbnailLink(trailer.getKey()))
                .into(viewHolder.thumbnailIV);
    }

    @Override
    public int getItemCount() {
        return trailers.size();
    }

    // Convenient method to get the Movie item at given position
    private Trailer getItem(int position) {
        if (position != RecyclerView.NO_POSITION)
            return trailers.get(position);
        else
            return null;
    }

    /**
     * Helper method to convert dp value to pixels.
     *
     * @param dp value
     * @return pixel value of given dp.
     */
    private int getPixels(float dp) {
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
        return px;
    }

    /**
     * Adapter will use this callback methods to communicate with fragment/activity
     * in which recycler view is using this adapter.
     * Fragment/Activity containing trailers(adapter) must implement.
     */
    public interface Callbacks {
        void watch(Trailer trailer);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView thumbnailIV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailIV = itemView.findViewById(R.id.trailer_thumbnail);
        }
    }
}
