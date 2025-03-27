package com.example.savourit.feature.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.savourit.models.Review;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.example.savourit.R;
import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    private List<Restaurant> restaurantList;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public RestaurantAdapter(Context context, List<Restaurant> restaurantList) {
        this.context = context;
        this.restaurantList = restaurantList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_restaurant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);
        holder.txtName.setText(restaurant.getName());

        // Check if the user liked the restaurant
        isRestaurantLiked(restaurant.getId(), isLiked -> {
            if (isLiked) {
                holder.btnLike.setImageResource(R.drawable.ic_liked);
            } else {
                holder.btnLike.setImageResource(R.drawable.ic_unliked);
            }
        });

        holder.btnLike.setOnClickListener(v -> {
            // Toggle the like status
            if (holder.btnLike.getDrawable().getConstantState() == context.getResources().getDrawable(R.drawable.ic_unliked).getConstantState()) {
                likeRestaurant(restaurant.getId());
                holder.btnLike.setImageResource(R.drawable.ic_liked);
            } else {
                unlikeRestaurant(restaurant.getId());
                holder.btnLike.setImageResource(R.drawable.ic_liked);
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
    }

    private String getUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    private void likeRestaurant(String restaurantId) {
        String userId = getUserId();
        if (userId == null) return;

        DocumentReference reviewDoc = db.collection("reviews")
                .document(userId + "_" + restaurantId);

        reviewDoc.set(new Review(userId, restaurantId, true))
                .addOnSuccessListener(aVoid -> {
                    // Successfully liked the restaurant
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void unlikeRestaurant(String restaurantId) {
        String userId = getUserId();
        if (userId == null) return;

        DocumentReference reviewDoc = db.collection("reviews")
                .document(userId + "_" + restaurantId);

        reviewDoc.set(new Review(userId, restaurantId, false))
                .addOnSuccessListener(aVoid -> {
                    // Successfully unliked the restaurant
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void isRestaurantLiked(String restaurantId, OnLikedStatusListener listener) {
        String userId = getUserId();
        if (userId == null) return;

        db.collection("reviews").document(userId + "_" + restaurantId)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        boolean isLiked = documentSnapshot.getBoolean("like");
                        listener.onLikedStatusChecked(isLiked);
                    } else {
                        listener.onLikedStatusChecked(false);
                    }
                });
    }

    public interface OnLikedStatusListener {
        void onLikedStatusChecked(boolean isLiked);
    }
}
