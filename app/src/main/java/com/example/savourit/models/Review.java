package com.example.savourit.models;

public class Review {
    private String restaurantName;
    private String comment;
    private double rating;
    private boolean liked;

    public Review() {

    } // Required for Firestore

    public Review(String restaurantName, String comment, double rating, boolean liked) {
        this.restaurantName = restaurantName;
        this.comment = comment;
        this.rating = rating;
        this.liked=liked;
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

    public boolean getLiked(){
        return liked;
    }
}