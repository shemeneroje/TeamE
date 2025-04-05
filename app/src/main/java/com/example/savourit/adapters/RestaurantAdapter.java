package com.example.savourit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.savourit.R;
import com.example.savourit.data.models.RestaurantModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    // List of restaurants to display in the RecyclerView
    private final List<RestaurantModel> restaurantList;
    // Flag to indicate if the user is a premium user
    private final boolean isPremiumUser;
    // Listener for item interactions like like, rate, and select
    private final OnItemInteractionListener listener;
    // Map to track the liked status of restaurants by their placeId
    private final Map<String, Boolean> likedStatusMap;

    // Interface to handle interaction events like like, rate, and select
    public interface OnItemInteractionListener {
        void onLike(RestaurantModel model);  // Method to handle like action
        void onRate(RestaurantModel model);  // Method to handle rate action
        void onSelect(RestaurantModel model); // Method to handle select action
    }

    // Constructor for initializing the adapter
    public RestaurantAdapter(List<RestaurantModel> list,
                             boolean isPremium,
                             Map<String, Boolean> likedStatusMap,
                             OnItemInteractionListener listener) {
        this.restaurantList = list;
        this.isPremiumUser = isPremium;
        // If likedStatusMap is null, initialize it with an empty map
        this.likedStatusMap = likedStatusMap != null ? likedStatusMap : new HashMap<>();
        this.listener = listener;
    }

    // Method to update the liked status map and refresh the RecyclerView
    public void updateLikedStatus(Map<String, Boolean> newStatusMap) {
        likedStatusMap.clear();
        likedStatusMap.putAll(newStatusMap);
        notifyDataSetChanged();  // Notify the adapter that data has changed
    }

    // Method to get the position of a restaurant by its placeId
    public int getPositionByPlaceId(String placeId) {
        for (int i = 0; i < restaurantList.size(); i++) {
            if (restaurantList.get(i).getPlaceId().equals(placeId)) {
                return i;  // Return position if the placeId matches
            }
        }
        return -1;  // Return -1 if the placeId is not found
    }

    // ViewHolder class for holding the views of each restaurant item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Views for the restaurant name, description, image, like button, and rate button
        TextView name, description;
        ImageView image, likeIcon;
        Button rate;
        LinearLayout premiumActions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views by finding them by their IDs
            name = itemView.findViewById(R.id.restaurantName);
            description = itemView.findViewById(R.id.restaurantDescription);
            image = itemView.findViewById(R.id.restaurantImage);
            likeIcon = itemView.findViewById(R.id.btn_Like);
            rate = itemView.findViewById(R.id.Rate);
            premiumActions = itemView.findViewById(R.id.premiumActions);
        }
    }

    // Called to create a new ViewHolder for the RecyclerView
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the restaurant item layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaurant_card, parent, false);
        return new ViewHolder(view);  // Return the new ViewHolder
    }

    // Called to bind data to the ViewHolder for each restaurant item
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the restaurant model for the current position
        RestaurantModel model = restaurantList.get(position);
        holder.name.setText(model.getName());  // Set restaurant name
        holder.description.setText(model.getAddress());  // Set restaurant description

        // Use Glide to load the restaurant image into the ImageView
        Glide.with(holder.itemView.getContext())
                .load(model.getImageUrl())
                .into(holder.image);

        // Set a click listener on the item view to trigger the onSelect method
        holder.itemView.setOnClickListener(v -> listener.onSelect(model));

        // If the user is premium, show premium actions (like and rate buttons)
        if (isPremiumUser) {
            holder.premiumActions.setVisibility(View.VISIBLE);

            // Check if the restaurant is liked
            boolean isLiked = model.isLiked();
            model.setLiked(isLiked);  // Update the liked status of the restaurant
            holder.likeIcon.setImageResource(
                    isLiked ? R.drawable.ic_like : R.drawable.ic_like_empty
            );  // Set the like icon based on the liked status

            // Set click listeners for the like and rate buttons
            holder.likeIcon.setOnClickListener(v -> listener.onLike(model));
            holder.rate.setOnClickListener(v -> listener.onRate(model));
        } else {
            // Hide premium actions if the user is not premium
            holder.premiumActions.setVisibility(View.GONE);
        }
    }

    // Returns the total number of items in the dataset
    @Override
    public int getItemCount() {
        return restaurantList.size();
    }
}
