package com.example.savourit.models;

public class Friend {
    private String userId;
    private String username;

    public Friend() {} // Required for Firestore

    public Friend(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
