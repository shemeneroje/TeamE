package com.example.savourit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private ImageButton btnFriends; // Changed from Button to ImageButton

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            // Redirect to LoginActivity if user is not logged in
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize Buttons
        btnFriends = findViewById(R.id.btnFriends);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Hide Friends button by default
        btnFriends.setVisibility(View.GONE);

        // Fetch User's Status from Firestore
        checkUserStatus(currentUser.getUid());

        // Go to Friends Page when clicking the icon
        btnFriends.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, FriendsActivity.class);
            startActivity(intent);
        });

        // Logout Button
        btnLogout.setOnClickListener(view -> logoutUser());
    }

    private void checkUserStatus(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");

                        // Debugging Log to check Firestore value
                        if (status != null) {
                            Log.d("MainActivity", "User status: " + status);
                        } else {
                            Log.e("MainActivity", "Status field is missing in Firestore!");
                        }

                        // Show Friends button only if status is "plus" or "premium"
                        if ("plus".equals(status) || "premium".equals(status)) {
                            btnFriends.setVisibility(View.VISIBLE);
                            btnFriends.post(() -> btnFriends.invalidate()); // 🔹 Force UI refresh
                            Log.d("MainActivity", "Friends button is now VISIBLE");
                        } else {
                            Log.d("MainActivity", "User does not have access to Friends Page.");
                        }
                    } else {
                        Log.e("MainActivity", "User document does not exist in Firestore!");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Failed to fetch status: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Failed to check status", Toast.LENGTH_SHORT).show();
                });
    }

    // Logout Function
    private void logoutUser() {
        auth.signOut(); // Logs out user

        // Redirect to Login Activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears previous activities
        startActivity(intent);
        finish();
    }
}
