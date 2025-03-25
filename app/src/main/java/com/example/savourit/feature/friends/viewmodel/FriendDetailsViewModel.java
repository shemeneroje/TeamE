package com.example.savourit.feature.friends.viewmodel;

import android.util.Log;
import com.example.savourit.models.Friend;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.firestore.FirebaseFirestore;

public class FriendDetailsViewModel extends ViewModel {

    private final MutableLiveData<Friend> friendDetails = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<Friend> getFriendDetails() {
        return friendDetails;
    }

    public void loadFriendDetails(String friendId) {
        if (friendId == null || friendId.isEmpty()) {
            Log.e("FriendDetailsVM", "Invalid friendId");
            return;
        }

        db.collection("users").document(friendId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Friend friend = documentSnapshot.toObject(Friend.class);
                        if (friend != null) {
                            friend.setUserId(documentSnapshot.getId()); //set userId from doc ID
                            friendDetails.postValue(friend);
                        }

                    } else {
                        Log.e("FriendDetailsVM", "Friend not found");
                    }
                })
                .addOnFailureListener(e -> Log.e("FriendDetailsVM", "Error fetching friend details", e));
    }
}
