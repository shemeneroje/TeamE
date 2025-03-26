package com.example.savourit.feature.friends.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.savourit.models.FriendsDetail;
import com.google.firebase.firestore.FirebaseFirestore;

public class FriendDetailsViewModel extends ViewModel {

    private final MutableLiveData<FriendsDetail> friendDetails = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<FriendsDetail> getFriendDetails() {
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
                        FriendsDetail friend = documentSnapshot.toObject(FriendsDetail.class);
                        friendDetails.setValue(friend);
                    } else {
                        Log.e("FriendDetailsVM", "Friend not found");
                    }
                })
                .addOnFailureListener(e -> Log.e("FriendDetailsVM", "Error fetching friend details", e));
    }
}
