package com.example.savourit.utils.components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.savourit.feature.payments.PaymentActivity;
import com.example.savourit.data.local.AppDatabase;
import com.example.savourit.data.local.AppPrefs;
import com.example.savourit.data.local.entities.UserEntity;
import com.example.savourit.utils.ExecutorUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Dialogs {

    public interface OnPremiumUpgradeListener {
        void onUpgraded();
    }

    public static void showPremiumDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Go Premium!")
                .setMessage("Upgrade to premium and unlock exclusive features.")
                .setPositiveButton("Upgrade", (dialog, which) -> {
                    Intent intent = new Intent(context, PaymentActivity.class);
                    intent.putExtra("planType", "Premium");
                    context.startActivity(intent);
                })

                .setNegativeButton("Maybe Later", null)
                .show();
    }

    private static void upgradeToPremium(Context context, OnPremiumUpgradeListener listener) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .update("isPremium", true)
                .addOnSuccessListener(unused -> {
                    ExecutorUtils.getInstance().runInBackground(() -> {
                        UserEntity user = AppDatabase.getInstance(context).userDao().getLoggedInUser();
                        if (user != null) {
                            user.premium = true;
                            AppDatabase.getInstance(context).userDao().insertUser(user);
                        }
                    });

                    AppPrefs.resetLaunchCount(context);
                    Toast.makeText(context, "You're now a premium user!", Toast.LENGTH_SHORT).show();

                    if (listener != null) {
                        listener.onUpgraded();
                    }
                });
    }
}
