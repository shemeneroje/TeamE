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

    private final List<Review> reviewList;

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.txtRestaurantName.setText(review.getRestaurantName());

        String ratingText = holder.txtRating.getContext().getString(R.string.rating_format, review.getRating());
        holder.txtRating.setText(ratingText);
        holder.txtComment.setText(review.getComment());

        if (review.getLiked()) {
            holder.txtLiked.setVisibility(View.VISIBLE);
        } else {
            holder.txtLiked.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView txtRestaurantName, txtRating, txtComment, txtLiked;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            txtRestaurantName = itemView.findViewById(R.id.txtRestaurantName);
            txtRating = itemView.findViewById(R.id.txtRating);
            txtComment = itemView.findViewById(R.id.txtComment);
            txtLiked = itemView.findViewById(R.id.txtLiked);
        }
    }
}

