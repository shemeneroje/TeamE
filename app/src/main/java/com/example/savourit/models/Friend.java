package com.example.savourit.models;

public class Friend {
    private String userId;
    private String username;

    public Friend() {
        // Required empty constructor for Firebase
    }

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
