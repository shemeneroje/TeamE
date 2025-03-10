package com.example.savourit.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.savourit.ChatActivity;
import com.example.savourit.FriendDetailsActivity;
import com.example.savourit.R;
import com.example.savourit.models.Friend;
import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {
    private List<Friend> friendsList;
    private Context context;
    private OnFriendDeleteListener onFriendDeleteListener;

    public interface OnFriendDeleteListener {
        void onFriendDelete(Friend friend);
    }

    //Constructor
    public FriendsAdapter(Context context, List<Friend> friendsList, OnFriendDeleteListener deleteListener) {
        this.context = context;
        this.friendsList = friendsList;
        this.onFriendDeleteListener = deleteListener;
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

        //Click to show dialog with multiple options
        holder.usernameTextView.setOnClickListener(view -> {
            showOptionsDialog(friend);
        });

        //Long click to delete friend
        holder.usernameTextView.setOnLongClickListener(view -> {
            confirmDeleteFriend(friend);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    //Show a dialog with multiple friend options
    private void showOptionsDialog(Friend friend) {
        new AlertDialog.Builder(context)
                .setTitle("Friend Options")
                .setItems(new String[]{"View Details", "Send Message", "Delete Friend"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            openFriendDetails(friend);
                            break;
                        case 1:
                            openChat(friend);
                            break;
                        case 2:
                            confirmDeleteFriend(friend);
                            break;
                    }
                })
                .show();
    }

    //Open Friend Details Activity
    private void openFriendDetails(Friend friend) {
        Log.d("FriendsAdapter", "Viewing details for: " + friend.getUsername());
        Intent intent = new Intent(context, FriendDetailsActivity.class);
        intent.putExtra("friendId", friend.getUserId());
        context.startActivity(intent);
    }

    // Open Chat Activity
    private void openChat(Friend friend) {
        Log.d("FriendsAdapter", "Opening Chat with: " + friend.getUsername());
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("friendId", friend.getUserId());
        context.startActivity(intent);
    }

    //Confirm delete before removing friend
    private void confirmDeleteFriend(Friend friend) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Friend")
                .setMessage("Are you sure you want to remove " + friend.getUsername() + " from your friends?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (onFriendDeleteListener != null) {
                        onFriendDeleteListener.onFriendDelete(friend);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
