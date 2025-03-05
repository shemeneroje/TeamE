package com.example.savourit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView nameTextView, emailTextView, firstNameTextView, lastNameTextView, usernameTextView;
    private Button resetPasswordButton, upgradePlanButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        emailTextView = findViewById(R.id.emailAddressTitle);
        usernameTextView = findViewById(R.id.usernameTitle);
        firstNameTextView = findViewById(R.id.firstNameTitle);
        lastNameTextView = findViewById(R.id.lastNameTitle);
        resetPasswordButton = findViewById(R.id.reset_password_button);
        upgradePlanButton = findViewById(R.id.upgrade_button);
        logoutButton = findViewById(R.id.logout_button); // Added logout button

        // Get current user details
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Display user email
            emailTextView.setText(user.getEmail());

            // Fetch user details from Firestore
            loadUserData(user.getUid());
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }

        // Handle Reset Password Button click
        resetPasswordButton.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, ForgotPasswordActivity.class))
        );

        // Handle Upgrade Plan Button click
        upgradePlanButton.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, UpgradePlanActivity.class))
        );

        // Handle Logout Button click
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void loadUserData(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Get user details
                    String username = document.getString("username");
                    String firstName = document.getString("firstName");
                    String lastName = document.getString("lastName");

                    // Set data to TextViews
                    usernameTextView.setText("Username: " + username);
                    firstNameTextView.setText("First Name: " + firstName);
                    lastNameTextView.setText("Last Name: " + lastName);
                } else {
                    Toast.makeText(ProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ProfileActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}


