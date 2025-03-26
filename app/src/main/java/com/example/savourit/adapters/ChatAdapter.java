package com.example.savourit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savourit.R;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<String> messages = new ArrayList<>();

    public ChatAdapter(List<String> messages) {
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
        holder.messageTextView.setText(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    //New method to update messages dynamically
    public void updateMessages(List<String> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged(); // Notify adapter of data change
    }

    //New method to add a single new message
    public void addMessage(String message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }
}
