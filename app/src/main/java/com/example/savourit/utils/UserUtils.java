package com.example.savourit.utils;

import android.content.Context;
import android.util.Log;

import com.example.savourit.data.local.AppDatabase;
import com.example.savourit.data.local.entities.UserEntity;
import com.example.savourit.data.local.daos.UserDao;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserUtils {

    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static AppDatabase appDatabase; // ✅ Global instance

    public static void init(Context context) {
        if (appDatabase == null) {
            appDatabase = AppDatabase.getInstance(context.getApplicationContext());
        }
    }

    private static UserDao getUserDao() {
        if (appDatabase == null) {
            throw new IllegalStateException("UserUtils not initialized. Call UserUtils.init(context) in Application or BaseActivity.");
        }
        return appDatabase.userDao();
    }

    public interface UserCallback {
        void onUserFetched(UserEntity user);
    }

    public interface PremiumStatusCallback {
        void onResult(boolean isPremium);
    }

    public static void syncUserFromFirestoreToLocal(Context context, Runnable onSuccess, Runnable onFailure) {
        init(context); // just in case

        if (auth.getCurrentUser() == null) {
            onFailure.run();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String username = document.getString("username");
                        String firstName = document.getString("firstName");
                        String lastName = document.getString("lastName");
                        String email = document.getString("email");
                        Boolean isPremium = document.getBoolean("isPremium");

                        if (username == null || firstName == null || lastName == null || email == null) {
                            onFailure.run();
                            return;
                        }

                        UserEntity user = new UserEntity(
                                uid, username, firstName, lastName, email, Boolean.TRUE.equals(isPremium)
                        );

                        ExecutorUtils.getInstance().runInBackground(() -> {
                            getUserDao().insertUser(user);
                            onSuccess.run();
                        });

                    } else {
                        Log.w("UserUtils", "No user found in Firestore");
                        onFailure.run();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserUtils", "Failed to fetch user from Firestore", e);
                    onFailure.run();
                });
    }

    public static void getLoggedInUser(UserCallback callback) {
        ExecutorUtils.getInstance().runInBackground(() -> {
            UserEntity user = getUserDao().getLoggedInUser();
            if (callback != null) {
                callback.onUserFetched(user);
            }
        });
    }

    public static void isUserPremium(PremiumStatusCallback callback) {
        ExecutorUtils.getInstance().runInBackground(() -> {
            boolean premium = getUserDao().isCurrentUserPremium();
            if (callback != null) {
                callback.onResult(premium);
            }
        });
    }
}
