package com.example.savourit.data.local.entities;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public class UserEntity {

    @PrimaryKey(autoGenerate = false)
    @NonNull
    public String uid;

    public String username;
    public String firstName;
    public String lastName;
    public String email;
    public boolean premium;

    public UserEntity(String uid, String username, String firstName, String lastName, String email, boolean premium) {
        this.uid = uid;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.premium = premium;
    }
}

