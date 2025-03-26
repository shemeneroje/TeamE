package com.example.savourit;


import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView emailTextView, usernameTextView;
    private RecyclerView recyclerLikedRestaurants;
    private RestaurantAdapter restaurantAdapter;
    private List<Restaurant> likedRestaurants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailTextView = findViewById(R.id.emailAddressTitle);
        usernameTextView = findViewById(R.id.usernameTitle);
        recyclerLikedRestaurants = findViewById(R.id.recyclerLiked);

        likedRestaurants = new ArrayList<>();
        recyclerLikedRestaurants.setLayoutManager(new LinearLayoutManager(this));
        restaurantAdapter = new RestaurantAdapter(this, likedRestaurants);
        recyclerLikedRestaurants.setAdapter(restaurantAdapter);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            emailTextView.setText(user.getEmail());
            loadUserData(user.getUid());
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String username = document.getString("username");
                    usernameTextView.setText("Username: " + username);
                    loadLikedRestaurants(userId);
                } else {
                    Toast.makeText(ProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ProfileActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLikedRestaurants(String userId) {
        db.collection("reviews").whereEqualTo("userId", userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String restaurantId = document.getString("restaurantId");
                            loadRestaurantDetails(restaurantId);
                        }
                    }
                });
    }

    private void loadRestaurantDetails(String restaurantId) {
        db.collection("restaurants").document(restaurantId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot restaurantDoc = task.getResult();
                if (restaurantDoc.exists()) {
                    String restaurantName = restaurantDoc.getString("name");
                    likedRestaurants.add(new Restaurant(restaurantId, restaurantName));
                    restaurantAdapter.notifyItemInserted(likedRestaurants.size() - 1);
                }
            } else {
                Toast.makeText(ProfileActivity.this, "Failed to load restaurant data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}





