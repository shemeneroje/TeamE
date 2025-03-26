package com.example.savourit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savourit.R;
import com.example.savourit.models.MessageModel;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<MessageModel> messages = new ArrayList<>();

    public ChatAdapter(List<MessageModel> messages) {
        this.messages = messages;
    }


    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;

        public ChatViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.txtMessage);
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        MessageModel message = messages.get(position);
        String formatted = holder.messageTextView.getContext()
                .getString(R.string.chat_message_format, message.getSenderUsername(), message.getText());
        holder.messageTextView.setText(formatted);
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateMessages(List<MessageModel> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }


    //New method to add a single new message
    public void addMessage(MessageModel message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }
}