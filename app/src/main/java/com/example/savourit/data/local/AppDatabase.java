package com.example.savourit.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.savourit.data.local.daos.UserDao;
import com.example.savourit.data.local.entities.UserEntity;

// Annotates this class as a Room database with a list of entities and the version number.
@Database(entities = {UserEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    // Abstract method that provides access to the UserDao.
    public abstract UserDao userDao();


    private static volatile AppDatabase INSTANCE;

    // Singleton pattern to provide a single instance of the database.
    // This method creates the database only once and provides the same instance every time it is called.
    public static AppDatabase getInstance(Context context) {
        // Check if the database instance is null.
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) { // Synchronize to prevent multiple threads from creating instances simultaneously.
                // Double-check if the INSTANCE is still null after entering the synchronized block.
                if (INSTANCE == null) {
                    // Build the database if it doesn't exist.
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "savourit_db") // Specify the database name.
                            .fallbackToDestructiveMigration() // Allows schema changes by destroying the database and rebuilding it.
                            .build(); // Build and return the database instance.
                }
            }
        }
        return INSTANCE; // Return the single instance of the database.
    }
}
