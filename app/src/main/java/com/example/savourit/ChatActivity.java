package com.example.savourit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.savourit.adapters.ChatAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private TextView txtChatTitle;
    private EditText edtMessage;
    private Button btnSend;
    private ImageView btnBack;
    private RecyclerView recyclerChat;
    private ChatAdapter chatAdapter;
    private final List<String> messages = new ArrayList<>();
    private FirebaseFirestore db;
    private String chatRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Linru Wang
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        txtChatTitle = findViewById(R.id.txtChatTitle);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        recyclerChat = findViewById(R.id.recyclerChat);

        db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getUid();
        String friendId = getIntent().getStringExtra("friendId");

        if (friendId == null || currentUserId == null) {
            Toast.makeText(this, "Invalid user data!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chatAdapter = new ChatAdapter(messages);
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(chatAdapter);

        db.collection("users").document(friendId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String friendName = documentSnapshot.getString("username");
                    if (friendName != null) {
                        txtChatTitle.setText("Chat with " + friendName);
                    }
                });

        chatRoomId = currentUserId.compareTo(friendId) < 0 ? currentUserId + "_" + friendId : friendId + "_" + currentUserId;

        loadMessages();

        btnSend.setOnClickListener(view -> sendMessage());

        //BACK BUTTON
        btnBack.setOnClickListener(v -> finish());

    }

    private void loadMessages() {
        CollectionReference chatRef = db.collection("chats").document(chatRoomId).collection("messages");

        chatRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.w("Chat", "Listen failed.", e);
                        return;
                    }

                    messages.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String text = document.getString("text");
                        String senderName = document.getString("senderName");

                        if (senderName != null && text != null) {
                            messages.add(senderName + ": " + text);
                        }
                    }
                    chatAdapter.notifyDataSetChanged();
                    recyclerChat.scrollToPosition(messages.size() - 1);
                });
    }


    private void sendMessage() {
        String message = edtMessage.getText().toString().trim();
        if (message.isEmpty()) return;

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "You need to log in first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String senderEmail = auth.getCurrentUser().getEmail(); // Get email

        if (senderEmail == null) {
            Toast.makeText(this, "Email not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        //Query Firestore to find the user by email
        db.collection("users").whereEqualTo("email", senderEmail).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String senderName = querySnapshot.getDocuments().get(0).getString("username");

                        if (senderName == null || senderName.isEmpty()) {
                            senderName = "Unknown"; // Fallback if no username
                        }

                        //send message with senderName stored correctly
                        sendMessageToFirestore(senderName, message);
                    } else {
                        Toast.makeText(ChatActivity.this, "User not found in Firestore!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Error fetching sender name", Toast.LENGTH_SHORT).show();
                });
    }

    //send message with sender name
    private void sendMessageToFirestore(String senderName, String message) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("text", message);
        messageData.put("timestamp", System.currentTimeMillis());
        messageData.put("senderName", senderName); // Ensure senderName is stored

        db.collection("chats").document(chatRoomId).collection("messages")
                .add(messageData)
                .addOnSuccessListener(documentReference -> {
                    edtMessage.setText(""); // Clear input field
                })
                .addOnFailureListener(e ->
                        Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show());
    }

}
