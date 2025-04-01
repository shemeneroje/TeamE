package com.example.savourit.data.models;

public class User {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private boolean premium; // Changed from accountType

    public User() {
    }

    public User(String username, String firstName, String lastName, String email, boolean premium) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.premium = premium;
    }

    // Getters
    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public boolean isPremium() { return premium; }

    // Setters
    public void setUsername(String username) { this.username = username; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setPremium(boolean premium) { this.premium = premium; }
}
