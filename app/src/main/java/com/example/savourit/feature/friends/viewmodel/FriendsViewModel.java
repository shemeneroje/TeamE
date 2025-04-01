package com.example.savourit.feature.friends.viewmodel;

import android.app.Activity;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.savourit.models.Friend;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FriendsViewModel extends ViewModel {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<Friend>> friendsList = new MutableLiveData<>(new ArrayList<>());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LiveData<List<Friend>> getFriendsList() {
        return friendsList;
    }

    public void loadFriends() {
        executorService.execute(() -> {
            if (auth.getCurrentUser() == null) {
                Log.e("FriendsViewModel", "User not logged in");
                return;
            }

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
        });
    }

    private void fetchFriendUsernames(List<String> friendIds) {
        List<Friend> updatedList = new ArrayList<>();

        for (String friendId : friendIds) {
            db.collection("users").document(friendId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String username = documentSnapshot.getString("username");
                        String accountType = documentSnapshot.getString("accountType"); // Fetch account type
                        String email = documentSnapshot.getString("email"); // Fetch email

                        if (username != null) {
                            updatedList.add(new Friend(friendId, username, accountType, email));
                            friendsList.postValue(updatedList);
                        }
                    })
                    .addOnFailureListener(e -> Log.e("FriendsViewModel", "Error fetching friend's details", e));
        }
    }

    public void addFriend(String enteredUsername) {
        executorService.execute(() -> {
            if (auth.getCurrentUser() == null) return;

            String currentUserId = auth.getCurrentUser().getUid();

            db.collection("users").whereEqualTo("username", enteredUsername).get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (querySnapshot.isEmpty()) {
                            Log.e("FriendsViewModel", "User not found!");
                            return;
                        }

                        String friendUserId = querySnapshot.getDocuments().get(0).getId();
                        if (friendUserId.equals(currentUserId)) {
                            Log.e("FriendsViewModel", "Cannot add yourself!");
                            return;
                        }

                        String friendshipId = currentUserId.compareTo(friendUserId) < 0 ?
                                currentUserId + "_" + friendUserId : friendUserId + "_" + currentUserId;

                        db.collection("friends").document(friendshipId).get()
                                .addOnSuccessListener(friendshipDoc -> {
                                    if (friendshipDoc.exists()) {
                                        Log.e("FriendsViewModel", "Already friends!");
                                    } else {
                                        Map<String, String> friendData = new HashMap<>();
                                        friendData.put("userID1", currentUserId);
                                        friendData.put("userID2", friendUserId);

                                        db.collection("friends").document(friendshipId)
                                                .set(friendData)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("FriendsViewModel", "Friend added successfully!");
                                                    loadFriends();
                                                });
                                    }
                                });
                    });
        });
    }

    public void deleteFriend(Friend friend) {
        executorService.execute(() -> {
            String currentUserId = FirebaseAuth.getInstance().getUid();
            if (currentUserId == null) return;

            String friendshipId = currentUserId.compareTo(friend.getUserId()) < 0 ?
                    currentUserId + "_" + friend.getUserId() :
                    friend.getUserId() + "_" + currentUserId;

            db.collection("friends").document(friendshipId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FriendsViewModel", "Friend removed");
                        loadFriends();
                    });
        });
    }
    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}