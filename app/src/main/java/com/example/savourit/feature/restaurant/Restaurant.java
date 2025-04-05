package com.example.savourit.feature.restaurant;

public class Restaurant {
    private String id;
    private String name;

    public Restaurant() {
        // Required empty constructor for Firestore
    }

    public Restaurant(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}