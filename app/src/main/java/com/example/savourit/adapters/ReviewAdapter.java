package com.example.savourit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.savourit.R;
import com.example.savourit.models.Review;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final List<Review> reviewList; // List to store the review data

    // Constructor to initialize the review list
    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    // Called when RecyclerView needs a new ViewHolder to display an item.
    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView (item_review layout)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view); // Return a new ViewHolder for this item
    }

    // Called to bind data to the ViewHolder's views.
    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        // Get the review at the current position in the list
        Review review = reviewList.get(position);

        // Set restaurant name to the TextView
        holder.txtRestaurantName.setText(review.getRestaurantName());

        // Format the rating text (e.g., "Rating: 4.5")
        String ratingText = holder.txtRating.getContext().getString(R.string.rating_format, review.getRating());
        holder.txtRating.setText(ratingText);

        // Set the review comment to the TextView
        holder.txtComment.setText(review.getComment());

        // Check if the review was liked and set the visibility of the "liked" TextView
        if (review.getLiked()) {
            holder.txtLiked.setVisibility(View.VISIBLE); // Show "Liked" message if true
        } else {
            holder.txtLiked.setVisibility(View.GONE); // Hide the "Liked" message if false
        }
    }

    // Return the number of items in the list
    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    // ViewHolder class to hold the views for each item
    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView txtRestaurantName, txtRating, txtComment, txtLiked;

        // Constructor for ViewHolder, where we initialize the views
        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            // Bind the TextViews from the item layout to the ViewHolder
            txtRestaurantName = itemView.findViewById(R.id.txtRestaurantName);
            txtRating = itemView.findViewById(R.id.txtRating);
            txtComment = itemView.findViewById(R.id.txtComment);
            txtLiked = itemView.findViewById(R.id.txtLiked);
        }
    }
}
