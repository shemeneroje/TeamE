package com.example.savourit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savourit.R;
import com.example.savourit.models.Friend;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    //Linru Wang
    private List<Friend> friendsList;
    private OnFriendClickListener onFriendClickListener;
    private OnFriendLongClickListener onFriendLongClickListener;

    public interface OnFriendClickListener {
        void onFriendClick(Friend friend);
    }

    public interface OnFriendLongClickListener {
        void onFriendLongClick(Friend friend);
    }

    public FriendsAdapter(List<Friend> friendsList, OnFriendClickListener listener, OnFriendLongClickListener longClickListener) {
        this.friendsList = friendsList;
        this.onFriendClickListener = listener;
        this.onFriendLongClickListener = longClickListener;
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;

        public FriendViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.txtUsername);
        }
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friendsList.get(position);
        holder.usernameTextView.setText(friend.getUsername());

        //Click (for chat, view, delete)
        holder.usernameTextView.setOnClickListener(view -> {
            if (onFriendClickListener != null) {
                onFriendClickListener.onFriendClick(friend);
            }
        });

        // Long Click (Right-click equivalent) - Now handled safely
        holder.usernameTextView.setOnLongClickListener(view -> {
            onFriendLongClickListener.onFriendLongClick(friend);
            return true;  // Consume the event to prevent extra clicks
        });
    }

    public void updateList(List<Friend> newList) {
        this.friendsList.clear();
        this.friendsList.addAll(newList);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return friendsList.size();
    }
}
