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

    private final List<RestaurantModel> restaurantList;
    private final boolean isPremiumUser;
    private final OnItemInteractionListener listener;
    private final Map<String, Boolean> likedStatusMap;

    public interface OnItemInteractionListener {
        void onLike(RestaurantModel model);
        void onRate(RestaurantModel model);
        void onSelect(RestaurantModel model);
    }

    public RestaurantAdapter(List<RestaurantModel> list,
                             boolean isPremium,
                             Map<String, Boolean> likedStatusMap,
                             OnItemInteractionListener listener) {
        this.restaurantList = list;
        this.isPremiumUser = isPremium;
        this.likedStatusMap = likedStatusMap != null ? likedStatusMap : new HashMap<>();
        this.listener = listener;
    }

    public void updateLikedStatus(Map<String, Boolean> newStatusMap) {
        likedStatusMap.clear();
        likedStatusMap.putAll(newStatusMap);
        notifyDataSetChanged();
    }

    public int getPositionByPlaceId(String placeId) {
        for (int i = 0; i < restaurantList.size(); i++) {
            if (restaurantList.get(i).getPlaceId().equals(placeId)) {
                return i;
            }
        }
        return -1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, description;
        ImageView image, likeIcon;
        Button rate;
        LinearLayout premiumActions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.restaurantName);
            description = itemView.findViewById(R.id.restaurantDescription);
            image = itemView.findViewById(R.id.restaurantImage);
            likeIcon = itemView.findViewById(R.id.btn_Like);
            rate = itemView.findViewById(R.id.Rate);
            premiumActions = itemView.findViewById(R.id.premiumActions);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaurant_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RestaurantModel model = restaurantList.get(position);
        holder.name.setText(model.getName());
        holder.description.setText(model.getAddress());

        Glide.with(holder.itemView.getContext())
                .load(model.getImageUrl())
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> listener.onSelect(model));

        if (isPremiumUser) {
            holder.premiumActions.setVisibility(View.VISIBLE);

            boolean isLiked = model.isLiked();
            model.setLiked(isLiked);
            holder.likeIcon.setImageResource(
                    isLiked ? R.drawable.ic_like : R.drawable.ic_like_empty
            );

            holder.likeIcon.setOnClickListener(v -> listener.onLike(model));
            holder.rate.setOnClickListener(v -> listener.onRate(model));
        } else {
            holder.premiumActions.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }
}