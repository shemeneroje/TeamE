package com.example.savourit;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class FriendDetailsActivity extends AppCompatActivity {
    //Linru Wang

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_details);

        TextView txtFriendUsername = findViewById(R.id.txtFriendUsername);
        TextView txtFriendStatus = findViewById(R.id.txtFriendStatus);
        TextView txtFriendEmail = findViewById(R.id.txtFriendEmail);
        Button btnBack = findViewById(R.id.btnBack);

        String friendId = getIntent().getStringExtra("friendId");

        if (friendId == null || friendId.isEmpty()) {
            Toast.makeText(this, "Friend ID is missing!", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if no valid friendId
            return;
        }

        // Fetch Friend's Details from Firestore
        FirebaseFirestore.getInstance().collection("users")
                .document(friendId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String status = documentSnapshot.getString("status");
                        String email = documentSnapshot.getString("email");

                        txtFriendUsername.setText(username != null ? username : "Unknown");
                        txtFriendStatus.setText(status != null ? status : "No status available");
                        txtFriendEmail.setText(email != null ? email : "No email available");

                        Log.d("FriendDetails", "Fetched Friend Data: " + documentSnapshot.getData());
                    } else {
                        Toast.makeText(this, "Friend data not found!", Toast.LENGTH_SHORT).show();
                        Log.e("FriendDetails", "Friend document does not exist!");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load friend details", Toast.LENGTH_SHORT).show();
                    Log.e("FriendDetails", "Error fetching friend data: " + e.getMessage());
                });

        // Back button
        btnBack.setOnClickListener(view -> finish());
    }
}
