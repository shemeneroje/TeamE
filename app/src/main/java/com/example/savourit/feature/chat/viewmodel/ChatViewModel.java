package com.example.savourit.feature.chat.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.savourit.models.MessageModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatViewModel extends ViewModel {
    private final FirebaseFirestore db;
    private final String currentUserId;
    private String chatRoomId;
    private final MutableLiveData<List<String>> messages;
    private final MutableLiveData<String> friendName;
    private final ExecutorService executorService;

    public ChatViewModel() {
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
        messages = new MutableLiveData<>(new ArrayList<>());
        friendName = new MutableLiveData<>();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<String>> getMessages() {
        return messages;
    }

    public LiveData<String> getFriendName() {
        return friendName;
    }

    public void initializeChat(String friendId) {
        if (currentUserId == null || friendId == null) return;

        chatRoomId = currentUserId.compareTo(friendId) < 0 ? currentUserId + "_" + friendId : friendId + "_" + currentUserId;

        // Fetch friend's name asynchronously
        executorService.execute(() -> {
            db.collection("users").document(friendId).get()
                    .addOnSuccessListener(documentSnapshot -> friendName.postValue(documentSnapshot.getString("username")));
        });

        // Fetch messages asynchronously
        executorService.execute(() -> {
            CollectionReference chatRef = db.collection("chats").document(chatRoomId).collection("messages");
            chatRef.orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener((querySnapshot, e) -> {
                        if (querySnapshot != null) {
                            List<String> newMessages = new ArrayList<>();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                newMessages.add(document.getString("text"));
                            }
                            messages.postValue(newMessages);
                        }
                    });
        });
    }

    public void sendMessage(String message) {
        if (chatRoomId == null) return;

        // Store message in Firestore on a background thread
        executorService.execute(() -> {
            db.collection("chats").document(chatRoomId).collection("messages")
                    .add(new MessageModel(message, System.currentTimeMillis()));
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
