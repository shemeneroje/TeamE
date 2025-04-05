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

// FireStoreHelper is a singleton class to interact with Firebase Firestore database
public class FireStoreHelper {
    // Declare Firestore and FirebaseAuth instances to interact with Firebase.
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // Singleton instance of FireStoreHelper.
    private static FireStoreHelper instance;

    // Private constructor to prevent instantiation from other classes.
    private FireStoreHelper() {}

    // Get the singleton instance of FireStoreHelper.
    public static FireStoreHelper getInstance() {
        if (instance == null) {
            instance = new FireStoreHelper();
        }
        return instance;
    }

    // Method to submit a review for a restaurant.
    public void submitRestaurantReview(String restaurantId, String reviewText, float rating, Runnable onSuccess, Runnable onFailure) {
        ExecutorUtils.getInstance().runInBackground(() -> {
            // Get the current user ID.
            String userId = auth.getCurrentUser().getUid();

            // Prepare the data to be stored in the database.
            Map<String, Object> data = new HashMap<>();
            data.put("rating", rating);
            data.put("review", reviewText);
            data.put("timestamp", FieldValue.serverTimestamp());

            // Add the review data to the Firestore database.
            db.collection("restaurants")
                    .document(restaurantId)
                    .collection("reviews")
                    .document(userId)
                    .set(data)
                    .addOnSuccessListener(v -> {
                        if (onSuccess != null) onSuccess.run(); // Run success callback if review submission is successful.
                    })
                    .addOnFailureListener(e -> {
                        if (onFailure != null) onFailure.run(); // Run failure callback if an error occurs.
                    });
        });
    }

    // Method to like a restaurant.
    public void likeRestaurant(RestaurantModel model, Runnable onSuccess, Runnable onFailure) {
        ExecutorUtils.getInstance().runInBackground(() -> {
            String userId = auth.getCurrentUser().getUid(); // Get current user ID.

            // Add the restaurant to the liked restaurants collection for the current user.
            db.collection("users")
                    .document(userId)
                    .collection("liked_restaurants")
                    .document(model.getPlaceId())
                    .set(model) // Save the restaurant data.
                    .addOnSuccessListener(v -> onSuccess.run()) // Run success callback.
                    .addOnFailureListener(e -> onFailure.run()); // Run failure callback.
        });
    }

    // Method to unlike a restaurant.
    public void unlikeRestaurant(RestaurantModel model, Runnable onSuccess, Runnable onFailure) {
        ExecutorUtils.getInstance().runInBackground(() -> {
            String userId = auth.getCurrentUser().getUid(); // Get current user ID.

            // Remove the restaurant from the liked restaurants collection.
            db.collection("users")
                    .document(userId)
                    .collection("liked_restaurants")
                    .document(model.getPlaceId())
                    .delete() // Delete the restaurant from the liked list.
                    .addOnSuccessListener(v -> onSuccess.run()) // Run success callback.
                    .addOnFailureListener(e -> onFailure.run()); // Run failure callback.
        });
    }

    // Method to get the list of IDs of restaurants liked by the user.
    public void getUserLikedRestaurantIds(FireStoreCallback<List<String>> callback) {
        ExecutorUtils.getInstance().runInBackground(() -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Retrieve all liked restaurant documents for the current user.
            db.collection("users")
                    .document(userId)
                    .collection("liked_restaurants")
                    .get()
                    .addOnSuccessListener(query -> {
                        List<String> likedIds = new ArrayList<>();
                        for (DocumentSnapshot doc : query.getDocuments()) {
                            likedIds.add(doc.getId()); // Add the document ID (restaurant ID) to the list.
                        }
                        callback.onSuccess(likedIds); // Return the list of liked restaurant IDs.
                    })
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage())); // Return failure message if error occurs.
        });
    }

    // Method to get the list of restaurant objects liked by the user.
    public void getUserLikedRestaurants(FireStoreCallback<List<RestaurantModel>> callback) {
        ExecutorUtils.getInstance().runInBackground(() -> {
            String userId = auth.getCurrentUser().getUid();

            // Retrieve all liked restaurant documents for the current user.
            db.collection("users")
                    .document(userId)
                    .collection("liked_restaurants")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<RestaurantModel> restaurants = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            RestaurantModel restaurant = doc.toObject(RestaurantModel.class); // Convert document to RestaurantModel object.
                            if (restaurant != null) {
                                restaurants.add(restaurant); // Add the restaurant to the list.
                            }
                        }
                        callback.onSuccess(restaurants); // Return the list of restaurant models.
                    })
                    .addOnFailureListener(e -> {
                        callback.onFailure("Failed to load liked restaurants: " + e.getMessage()); // Return error message if failed.
                    });
        });
    }

    // Method to retrieve user data by user ID.
    public void getUserData(String userId, FireStoreCallback<DocumentSnapshot> callback) {
        ExecutorUtils.getInstance().runInBackground(() -> {
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            callback.onSuccess(document); // Return the user data if found.
                        } else {
                            callback.onFailure("User data not found"); // Return error if no data found.
                        }
                    })
                    .addOnFailureListener(e -> callback.onFailure("Failed to load data: " + e.getMessage())); // Return failure message on error.
        });
    }

    // FireStoreCallback interface to handle success and failure for Firestore operations.
    public interface FireStoreCallback<T> {
        void onSuccess(T result); // Success callback method.
        void onFailure(String errorMsg); // Failure callback method.
    }
}
