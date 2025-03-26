package com.example.savourit.feature.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.Navigation;

import com.example.savourit.R;
import com.example.savourit.adapters.ChatAdapter;
import com.example.savourit.feature.chat.viewmodel.ChatViewModel;

public class ChatFragment extends Fragment {
    private TextView txtChatTitle;
    private EditText edtMessage;
    private Button btnSend;
    private ImageView btnBack;
    private RecyclerView recyclerChat;
    private ChatAdapter chatAdapter;
    private ChatViewModel chatViewModel;
    private String friendId;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtChatTitle = view.findViewById(R.id.txtChatTitle);
        edtMessage = view.findViewById(R.id.edtMessage);
        btnSend = view.findViewById(R.id.btnSend);
        btnBack = view.findViewById(R.id.btnBack);
        recyclerChat = view.findViewById(R.id.recyclerChat);

        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        // Retrieve friendId from navigation arguments
        if (getArguments() != null) {
            friendId = getArguments().getString("friendId");
        }

        if (friendId == null) {
            Toast.makeText(getContext(), R.string.error_invalid_user, Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
            return;
        }

        // Initialize RecyclerView
        chatAdapter = new ChatAdapter(chatViewModel.getMessages().getValue());
        recyclerChat.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerChat.setAdapter(chatAdapter);

        // Load chat details & messages
        chatViewModel.initializeChat(friendId);

        // Observe messages and update UI when data changes
        chatViewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            chatAdapter.updateMessages(messages);
            recyclerChat.scrollToPosition(messages.size() - 1);
        });

        chatViewModel.getFriendName().observe(getViewLifecycleOwner(), name ->
                txtChatTitle.setText(getString(R.string.chat_with, name))
        );

        // Send message
        btnSend.setOnClickListener(v -> {
            String message = edtMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                chatViewModel.sendMessage(message);
                edtMessage.setText(""); // Clear input field
            }
        });

        // Back Button
        btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
    }
}
