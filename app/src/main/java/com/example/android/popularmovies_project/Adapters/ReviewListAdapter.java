package com.example.android.popularmovies_project.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies_project.Models.Review;
import com.example.android.popularmovies_project.R;

import java.util.List;

public class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.ViewHolder> {

    private static final String LOG_TAG = ReviewListAdapter.class.getSimpleName();
    private final ReviewListAdapter.Callbacks mCallbacks;
    private Context context;
    private List<Review> reviews;

    public ReviewListAdapter(Context context, List<Review> reviewList, Callbacks callbacks) {
        this.context = context;
        this.reviews = reviewList;
        this.mCallbacks = callbacks;
    }

    @NonNull
    @Override
    public ReviewListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.list_item_review, parent, false);

        final ViewHolder holder = new ViewHolder(view);
        holder.contentTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPos = holder.getAdapterPosition();
                Review review = getItem(adapterPos);

                mCallbacks.readReview(review);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewListAdapter.ViewHolder viewHolder, int position) {
        Review review = getItem(position);

        viewHolder.authorTV.setText(context.getString(R.string.review_author_name, review.getAuthor()));
        viewHolder.contentTV.setText(review.getContent());
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    // Convenient method to get the Movie item at given position
    private Review getItem(int position) {
        if (position != RecyclerView.NO_POSITION)
            return reviews.get(position);
        else
            return null;
    }

    /**
     * Adapter will use this callback methods to communicate with fragment/activity
     * in which recycler view is using this adapter.
     * Fragment/Activity containing reviews(adapter) must implement.
     */
    public interface Callbacks {
        void readReview(Review review);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView authorTV;
        private TextView contentTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            authorTV = itemView.findViewById(R.id.review_author);
            contentTV = itemView.findViewById(R.id.review_content);
        }
    }
}
