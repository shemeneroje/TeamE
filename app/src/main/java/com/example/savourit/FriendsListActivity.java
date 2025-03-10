package com.example.savourit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.savourit.adapters.FriendsAdapter;
import com.example.savourit.models.Friend;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FriendsListActivity extends AppCompatActivity {
    private RecyclerView recyclerViewFriends;
    private FriendsAdapter friendsAdapter;
    private List<Friend> friendsList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        recyclerViewFriends = findViewById(R.id.recyclerViewFriends);
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        friendsAdapter = new FriendsAdapter(this, friendsList, this::deleteFriend);


        recyclerViewFriends.setAdapter(friendsAdapter);

        loadFriends(friendsList);
    }

    private void loadFriends(List<Friend> friendsList) {
        db.collection("users").document(currentUserId).collection("friends")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    friendsList.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String friendId = document.getId();
                        String username = document.getString("username");
                        friendsList.add(new Friend(friendId, username));
                        friendsAdapter.notifyItemInserted(friendsList.size() - 1);
                    }
                });
    }

    private void openChat(Friend friend) {
        Intent intent = new Intent(FriendsListActivity.this, ChatActivity.class);
        intent.putExtra("friendId", friend.getUserId());
        startActivity(intent);
    }

    private void deleteFriend(Friend friend) {
        db.collection("users").document(currentUserId)
                .collection("friends").document(friend.getUserId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Friend removed", Toast.LENGTH_SHORT).show();
                    int index = friendsAdapter.getItemCount();
                    if (index >= 0) {
                        friendsList.remove(index);
                        friendsAdapter.notifyItemRemoved(index);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error removing friend", Toast.LENGTH_SHORT).show());
    }
}
