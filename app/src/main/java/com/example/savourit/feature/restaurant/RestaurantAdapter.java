package com.example.savourit.feature.restaurant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savourit.R;
import com.example.savourit.data.models.RestaurantModel;
import com.example.savourit.data.remote.FireStoreHelper;
import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    private List<RestaurantModel> restaurantList;
    private Context context;

    public RestaurantAdapter(Context context, List<RestaurantModel> restaurantList) {
        this.context = context;
        this.restaurantList = restaurantList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_resturant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RestaurantModel restaurant = restaurantList.get(position);
        holder.txtName.setText(restaurant.getName());
        holder.setLiked(restaurant.isLiked());

        holder.btnLike.setOnClickListener(v -> {
            boolean currentlyLiked = restaurant.isLiked();

            if (currentlyLiked) {
                FireStoreHelper.getInstance().unlikeRestaurant(
                        restaurant,
                        () -> {
                            restaurant.setLiked(false);
                            holder.setLiked(false);
                        },
                        () -> holder.setLiked(true)
                );
            } else {
                FireStoreHelper.getInstance().likeRestaurant(
                        restaurant,
                        () -> {
                            restaurant.setLiked(true);
                            holder.setLiked(true);
                        },
                        () -> holder.setLiked(false)
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        ImageButton btnLike;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            btnLike = itemView.findViewById(R.id.btnLike);
        }

        void setLiked(boolean isLiked) {
            btnLike.setImageResource(isLiked ? R.drawable.ic_like : R.drawable.ic_like_empty);
        }
    }
}
