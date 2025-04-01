package com.example.savourit.data.local.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.savourit.data.local.entities.UserEntity;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);

    @Query("SELECT * FROM user LIMIT 1")
    UserEntity getLoggedInUser();

    @Query("DELETE FROM user")
    void clearUser();

    @Query("SELECT premium FROM user LIMIT 1")
    boolean isCurrentUserPremium();

}
