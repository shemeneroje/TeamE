package com.example.savourit.feature.friends;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.savourit.R;
import com.example.savourit.adapters.FriendsAdapter;
import com.example.savourit.models.Friend;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsFragment extends Fragment {

    private RecyclerView recyclerFriends;
    private FriendsAdapter friendsAdapter;
    private final List<Friend> friendsList = new ArrayList<>();
    private final List<Friend> fullFriendsList = new ArrayList<>();
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public FriendsFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerFriends = view.findViewById(R.id.recyclerFriends);
        Button btnAddFriend = view.findViewById(R.id.btnAddFriend);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        friendsAdapter = new FriendsAdapter(friendsList, this::navigateToFriendDetails, this::onFriendClick);
        recyclerFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerFriends.setAdapter(friendsAdapter);

        loadFriends();

        btnAddFriend.setOnClickListener(view1 -> addFriend());

        setupSearch(view);
    }

    private void navigateToFriendDetails(Friend friend) {
        if (friend == null || friend.getUserId() == null) {
            Log.e("FriendsFragment", "Error: friend or friendId is null.");
            Toast.makeText(requireContext(), "Error loading friend details", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Bundle bundle = new Bundle();
            bundle.putString("friendId", friend.getUserId());
            Navigation.findNavController(requireView()).navigate(R.id.friends_to_friends_detail, bundle);
        } catch (Exception e) {
            Log.e("FriendsFragment", "Navigation error: " + e.getMessage());
            Toast.makeText(requireContext(), "Navigation error", Toast.LENGTH_SHORT).show();
        }
    }
    private void navigateToChat(Friend friend) {
        if (friend == null || friend.getUserId() == null) {
            Log.e("FriendsFragment", "Error: friend or friendId is null.");
            Toast.makeText(requireContext(), "Error opening chat", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("friendId", friend.getUserId());

        try {
            Navigation.findNavController(requireView()).navigate(R.id.friends_to_chat, bundle);
        } catch (Exception e) {
            Log.e("FriendsFragment", "Navigation error: " + e.getMessage());
            Toast.makeText(requireContext(), "Navigation error", Toast.LENGTH_SHORT).show();
        }
    }


    private void onFriendClick(Friend friend) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose an action for " + friend.getUsername());

        String[] options = {"Chat", "View Details", "Delete Friend"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Chat
                    navigateToChat(friend);
                    break;
                case 1: // View Details
                    navigateToFriendDetails(friend);
                    break;
                case 2: // Delete Friend
                    deleteFriend(friend);
                    break;
            }
        });

        builder.show();
    }

    private void loadFriends() {
        if (auth.getCurrentUser() == null) {
            return;
        }
        friendsList.clear(); //This line prevents duplicates
        friendsAdapter.notifyDataSetChanged(); //reset view

        String userId = auth.getCurrentUser().getUid();
        List<String> friendsToLoad = new ArrayList<>();

        db.collection("friends").whereEqualTo("userID1", userId).get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String friendId = document.getString("userID2");
                        if (friendId != null) friendsToLoad.add(friendId);
                    }
                    fetchFriendUsernames(friendsToLoad);
                });

        db.collection("friends").whereEqualTo("userID2", userId).get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String friendId = document.getString("userID1");
                        if (friendId != null) friendsToLoad.add(friendId);
                    }
                    fetchFriendUsernames(friendsToLoad);
                });
    }

    private void fetchFriendUsernames(List<String> friendIds) {
        List<Friend> tempFriends = new ArrayList<>();

        for (String friendId : friendIds) {
            db.collection("users").document(friendId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String username = documentSnapshot.getString("username");
                        String accountType = documentSnapshot.getString("accountType");
                        String email = documentSnapshot.getString("email");

                        if (username != null) {
                            tempFriends.add(new Friend(friendId, username, accountType, email));

                            if (tempFriends.size() == friendIds.size()) {
                                friendsList.clear();
                                fullFriendsList.clear();
                                friendsList.addAll(tempFriends);
                                fullFriendsList.addAll(tempFriends);
                                friendsAdapter.updateList(new ArrayList<>(friendsList));
                            }
                        }
                    });
        }
    }


    //Add search functionality
    private void setupSearch(View view) {
        EditText searchBar = view.findViewById(R.id.edtSearch);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFriends(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterFriends(String query) {
        if (query.trim().isEmpty()) {
            friendsAdapter.updateList(new ArrayList<>(fullFriendsList));  // restore original
            return;
        }

        List<Friend> filteredList = new ArrayList<>();
        for (Friend friend : fullFriendsList) {
            if (friend.getUsername().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(friend);
            }
        }
        friendsAdapter.updateList(filteredList);
    }



    private void addFriend() {
        if (auth.getCurrentUser() == null) return;

        String enteredUsername = ((EditText) requireView().findViewById(R.id.edtFriendUsername)).getText().toString().trim();
        if (enteredUsername.isEmpty()) {
            Toast.makeText(requireContext(), "Enter a username!", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("users").whereEqualTo("username", enteredUsername).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(requireContext(), "User not found!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String friendUserId = querySnapshot.getDocuments().get(0).getId();
                    if (friendUserId.equals(currentUserId)) {
                        Toast.makeText(requireContext(), "You cannot add yourself!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String friendshipId = currentUserId.compareTo(friendUserId) < 0 ?
                            currentUserId + "_" + friendUserId : friendUserId + "_" + currentUserId;

                    db.collection("friends").document(friendshipId).get()
                            .addOnSuccessListener(friendshipDoc -> {
                                if (friendshipDoc.exists()) {
                                    Toast.makeText(requireContext(), "You are already friends!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Map<String, String> friendData = new HashMap<>();
                                    friendData.put("userID1", currentUserId);
                                    friendData.put("userID2", friendUserId);

                                    db.collection("friends").document(friendshipId)
                                            .set(friendData)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(requireContext(), "Friend added successfully!", Toast.LENGTH_SHORT).show();
                                                updateFriendList(friendUserId);
                                            });
                                }
                            });
                });
    }

    private void updateFriendList(String friendUserId) {
        db.collection("users").document(friendUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String username = documentSnapshot.getString("username");
                    String accountType = documentSnapshot.getString("accountType");
                    String email = documentSnapshot.getString("email");

                    if (username != null) {
                        friendsList.add(new Friend(friendUserId, username, accountType, email));
                        friendsAdapter.notifyItemInserted(friendsList.size() - 1);
                    }
                });
    }



    private void deleteFriend(Friend friend) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return;

        String friendshipId = currentUserId.compareTo(friend.getUserId()) < 0 ?
                currentUserId + "_" + friend.getUserId() :
                friend.getUserId() + "_" + currentUserId;

        db.collection("friends").document(friendshipId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), friend.getUsername() + " removed from friends", Toast.LENGTH_SHORT).show();
                    int index = friendsList.indexOf(friend);
                    if (index >= 0) {
                        friendsList.remove(index);
                        friendsAdapter.notifyItemRemoved(index);
                    }
                })
                .addOnFailureListener(e -> Log.e("FriendsFragment", "Error deleting friend", e));
    }



}
