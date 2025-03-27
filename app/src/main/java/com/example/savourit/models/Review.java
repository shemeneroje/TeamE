package com.example.savourit.models;
public class Review {
    private String userId;
    private String restaurantId;
    private String restaurantName;
    private String comment;
    private double rating;
    private boolean liked;
    private long timestamp;

    public Review() {} // Firestore requires empty constructor

    public Review(String userId, String restaurantId, boolean liked) {
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.liked = liked;
        this.comment = "";
        this.restaurantName = "";
        this.rating = 0.0;
        this.timestamp = System.currentTimeMillis();
    }


    // Getters and setters here


    public String getUserId() {
        return userId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getComment() {
        return comment;
    }

    public double getRating() {
        return rating;
    }

    public boolean isLiked() {
        return liked;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
}
