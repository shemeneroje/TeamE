package com.example.savourit.feature.profile;
//for reviews collection in firestore database

public class Review {
    private String userId;
    private String restaurantId;
    private boolean like;

    public Review(String userId, String restaurantId, boolean like) {
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.like = like;
    }

    public String getUserId() {
        return userId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public boolean isLike() {
        return like;
    }
}
