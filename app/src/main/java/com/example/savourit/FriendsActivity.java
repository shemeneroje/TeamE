package com.example.savourit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.savourit.adapters.FriendsAdapter;
import com.example.savourit.models.Friend;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity {
    private FriendsAdapter friendsAdapter;
    private final List<Friend> friendsList = new ArrayList<>();
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up RecyclerView
        RecyclerView recyclerFriends = findViewById(R.id.recyclerFriends);
        recyclerFriends.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Friends Adapter
        friendsAdapter = new FriendsAdapter(this, friendsList, this::deleteFriend);


        recyclerFriends.setAdapter(friendsAdapter);

        // Load Friends List
        loadFriends();

        findViewById(R.id.btnAddFriend).setOnClickListener(view -> addFriend());
        findViewById(R.id.btnBack).setOnClickListener(view -> {
            Intent intent = new Intent(FriendsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void loadFriends() {
        if (auth.getCurrentUser() == null) {
            Log.e("FriendsActivity", "User not logged in");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        List<String> friendsToLoad = new ArrayList<>();
        friendsList.clear();

        //Use a single listener to avoid fetching duplicate data
        db.collection("friends")
                .whereEqualTo("userID1", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String friendId = document.getString("userID2");
                        if (friendId != null && !friendsToLoad.contains(friendId)) {
                            friendsToLoad.add(friendId);
                        }
                    }
                    checkAndFetchFriendUsernames(friendsToLoad); //Pass unique list to fetch usernames
                });

        db.collection("friends")
                .whereEqualTo("userID2", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String friendId = document.getString("userID1");
                        if (friendId != null && !friendsToLoad.contains(friendId)) {
                            friendsToLoad.add(friendId);
                        }
                    }
                    checkAndFetchFriendUsernames(friendsToLoad); //Avoid fetching duplicates
                });
    }


    private void checkAndFetchFriendUsernames(List<String> friendIds) {
        for (String friendId : friendIds) {
            db.collection("users").document(friendId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String username = documentSnapshot.getString("username");
                        if (username != null) {
                            //Prevent adding duplicate friends
                            boolean alreadyExists = false;
                            for (Friend friend : friendsList) {
                                if (friend.getUserId().equals(friendId)) {
                                    alreadyExists = true;
                                    break;
                                }
                            }

                            if (!alreadyExists) {
                                friendsList.add(new Friend(friendId, username));
                                friendsAdapter.notifyItemInserted(friendsList.size() - 1);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("FriendsActivity", "Error fetching friend's username", e));
        }
    }


    private void onFriendLongClick(Friend friend) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Friend")
                .setMessage("Do you want to remove " + friend.getUsername() + "?")
                .setPositiveButton("Yes", (dialog, which) -> deleteFriend(friend))
                .setNegativeButton("No", null)
                .show();
    }

    private void showFriendOptions(Friend friend) {
        String[] options = {"View Details", "Delete Friend", "Send Message"};

        new AlertDialog.Builder(this)
                .setTitle("Choose an action for " + friend.getUsername())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) viewFriendDetails(friend);
                    else if (which == 1) deleteFriend(friend);
                    else sendMessage(friend);
                })
                .show();
    }

    private void viewFriendDetails(Friend friend) {
        Intent intent = new Intent(this, FriendDetailsActivity.class);
        intent.putExtra("friendId", friend.getUserId());
        startActivity(intent);
    }
    private void openFriendDetails(Friend friend) {
        Intent intent = new Intent(FriendsActivity.this, FriendDetailsActivity.class);
        intent.putExtra("friendId", friend.getUserId());
        startActivity(intent);
    }


    private void deleteFriend(Friend friend) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = user.getUid();
        String friendshipId = currentUserId.compareTo(friend.getUserId()) < 0 ?
                currentUserId + "_" + friend.getUserId() :
                friend.getUserId() + "_" + currentUserId;

        db.collection("friends").document(friendshipId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, friend.getUsername() + " removed from friends", Toast.LENGTH_SHORT).show();
                    friendsList.remove(friend);
                    friendsAdapter.notifyDataSetChanged(); // Update RecyclerView
                })
                .addOnFailureListener(e -> Log.e("FriendsActivity", "Error deleting friend", e));
    }


    private void sendMessage(Friend friend) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("friendId", friend.getUserId());
        startActivity(intent);
    }

    private void addFriend() {
        if (auth.getCurrentUser() == null) return;

        String enteredUsername = ((EditText) findViewById(R.id.edtFriendUsername)).getText().toString().trim();
        if (enteredUsername.isEmpty()) {
            Toast.makeText(this, "Enter a username!", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("users").whereEqualTo("username", enteredUsername).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String friendUserId = querySnapshot.getDocuments().get(0).getId();
                    if (friendUserId.equals(currentUserId)) {
                        Toast.makeText(this, "You cannot add yourself!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String friendshipId = currentUserId.compareTo(friendUserId) < 0 ?
                            currentUserId + "_" + friendUserId : friendUserId + "_" + currentUserId;

                    db.collection("friends").document(friendshipId).get()
                            .addOnSuccessListener(friendshipDoc -> {
                                if (friendshipDoc.exists()) {
                                    Toast.makeText(this, "You are already friends!", Toast.LENGTH_SHORT).show();
                                } else {
                                    db.collection("friends").document(friendshipId)
                                            .set(new HashMap<>(Map.of("userID1", currentUserId, "userID2", friendUserId)))
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(this, "Friend added successfully!", Toast.LENGTH_SHORT).show();
                                                updateFriendList(friendUserId);
                                            })
                                            .addOnFailureListener(e -> Log.e("FriendsActivity", "Error adding friend", e));
                                }
                            });
                });
    }

    private void updateFriendList(String friendUserId) {
        db.collection("users").document(friendUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String username = documentSnapshot.getString("username");
                    if (username != null) {
                        friendsList.add(new Friend(friendUserId, username));
                        friendsAdapter.notifyItemInserted(friendsList.size() - 1);
                    }
                })
                .addOnFailureListener(e -> Log.e("FriendsActivity", "Error loading new friend", e));
    }
    private void logoutUser() {
        FirebaseAuth.getInstance().signOut(); // Logs out user

        // Redirect to Login Activity
        Intent intent = new Intent(FriendsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears previous activities
        startActivity(intent);
        finish();
    }

}
