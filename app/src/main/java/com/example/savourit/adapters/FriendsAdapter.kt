package com.example.savourit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.savourit.R
import com.example.savourit.models.Friend

class FriendsAdapter(
    //Linru Wang
    private val friendsList: MutableList<Friend>,
    private val onFriendClick: (Friend) -> Unit  //Callback for friend click
) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.txtUsername)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friendsList[position]
        holder.usernameTextView.text = friend.username

        //Show menu when clicking a friend
        holder.usernameTextView.setOnClickListener {
            onFriendClick(friend)
        }
    }

    override fun getItemCount(): Int = friendsList.size
}
