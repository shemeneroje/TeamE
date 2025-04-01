package com.example.savourit.data.remote;

import com.example.savourit.data.models.RestaurantModel;
import com.example.savourit.feature.restaurant.Restaurant;
import com.example.savourit.utils.ExecutorUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FireStoreHelper {
    private static FireStoreHelper instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private FireStoreHelper() {}

    public static FireStoreHelper getInstance() {
        if (instance == null) {
            instance = new FireStoreHelper();
        }
        return instance;
    }
    public void submitRestaurantReview(String restaurantId, String reviewText, float rating, Runnable onSuccess, Runnable onFailure) {
        ExecutorUtils.getInstance().runInBackground(() -> {
            String userId = auth.getCurrentUser().getUid();
            Map<String, Object> data = new HashMap<>();
            data.put("rating", rating);
            data.put("review", reviewText);
            data.put("timestamp", FieldValue.serverTimestamp());

            db.collection("restaurants")
                    .document(restaurantId)
                    .collection("reviews")
                    .document(userId)
                    .set(data)
                    .addOnSuccessListener(v -> {
                        if (onSuccess != null) onSuccess.run();
                    })
                    .addOnFailureListener(e -> {
                        if (onFailure != null) onFailure.run();
                    });
        });
    }

    public void likeRestaurant(RestaurantModel model, Runnable onSuccess, Runnable onFailure) {
        ExecutorUtils.getInstance().runInBackground(() -> {
            String userId = auth.getCurrentUser().getUid();
            db.collection("users")
                    .document(userId)
                    .collection("liked_restaurants")
                    .document(model.getPlaceId())
                    .set(model)
                    .addOnSuccessListener(v -> onSuccess.run())
                    .addOnFailureListener(e -> onFailure.run());
        });
    }

    public void unlikeRestaurant(RestaurantModel model, Runnable onSuccess, Runnable onFailure) {
        ExecutorUtils.getInstance().runInBackground(() -> {
            String userId = auth.getCurrentUser().getUid();
            db.collection("users")
                    .document(userId)
                    .collection("liked_restaurants")
                    .document(model.getPlaceId())
                    .delete()
                    .addOnSuccessListener(v -> onSuccess.run())
                    .addOnFailureListener(e -> onFailure.run());
        });
    }

    public void getUserLikedRestaurantIds(FireStoreCallback<List<String>> callback) {
        ExecutorUtils.getInstance().runInBackground(() -> {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("liked_restaurants")
                .get()
                .addOnSuccessListener(query -> {
                    List<String> likedIds = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        likedIds.add(doc.getId());
                    }
                    callback.onSuccess(likedIds);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        });
    }

    public void getUserLikedRestaurants(FireStoreCallback<List<RestaurantModel>> callback) {
        ExecutorUtils.getInstance().runInBackground(() -> {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("liked_restaurants")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<RestaurantModel> restaurants = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        RestaurantModel restaurant = doc.toObject(RestaurantModel.class);
                        if (restaurant != null) {
                            restaurants.add(restaurant);
                        }
                    }
                    callback.onSuccess(restaurants);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure("Failed to load liked restaurants: " + e.getMessage());
                });
        });
    }


    public void getUserData(String userId, FireStoreCallback<DocumentSnapshot> callback) {
        ExecutorUtils.getInstance().runInBackground(() -> {
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            callback.onSuccess(document);
                        } else {
                            callback.onFailure("User data not found");
                        }
                    })
                    .addOnFailureListener(e -> callback.onFailure("Failed to load data: " + e.getMessage()));
        });
    }

    public interface FireStoreCallback<T> {
        void onSuccess(T result);
        void onFailure(String errorMsg);
    }

}