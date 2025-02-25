package com.example.savourit

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.savourit.adapters.FriendsAdapter
import com.example.savourit.models.Friend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsActivity : AppCompatActivity() {
    //Linru wang
    private lateinit var recyclerFriends: RecyclerView
    private lateinit var btnAddFriend: Button
    private val friendsList = mutableListOf<Friend>()
    private lateinit var friendsAdapter: FriendsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        recyclerFriends = findViewById(R.id.recyclerFriends)
        btnAddFriend = findViewById(R.id.btnAddFriend)

        recyclerFriends.layoutManager = LinearLayoutManager(this)
        friendsAdapter = FriendsAdapter(friendsList) { friend -> showFriendOptions(friend) }

        recyclerFriends.adapter = friendsAdapter

        loadFriends()

        val btnLogout = findViewById<Button>(R.id.btnLogout)

        btnLogout.setOnClickListener {
            logoutUser()
        }

        btnAddFriend.setOnClickListener {
            addFriend()
        }
    }

    private fun loadFriends() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("FriendsActivity", "User not logged in")
            return
        }

        val userId = currentUser.uid
        val db = FirebaseFirestore.getInstance()

        Log.d("FriendsActivity", "Loading friends for user: $userId")

        db.collection("friends")
            .whereEqualTo("userID1", userId)
            .get()
            .addOnSuccessListener { documents ->
                friendsList.clear()
                for (document in documents) {
                    val friendId = document.getString("userID2") ?: continue
                    Log.d("FriendsActivity", "Found friend ID: $friendId")

                    // Fetch the friend's username from Firestore
                    db.collection("users")
                        .document(friendId)
                        .get()
                        .addOnSuccessListener { friendDoc ->
                            val friendUsername = friendDoc.getString("username") ?: "No Username"
                            Log.d("FriendsActivity", "Friend username: $friendUsername")

                            friendsList.add(Friend(userId = friendId, username = friendUsername))
                            friendsAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Log.e("FriendsActivity", "Error fetching friend's username", e)
                        }
                }

                // Also check where user is userID2
                db.collection("friends")
                    .whereEqualTo("userID2", userId)
                    .get()
                    .addOnSuccessListener { secondDocuments ->
                        for (document in secondDocuments) {
                            val friendId = document.getString("userID1") ?: continue
                            Log.d("FriendsActivity", "Found friend ID: $friendId")

                            db.collection("users")
                                .document(friendId)
                                .get()
                                .addOnSuccessListener { friendDoc ->
                                    val friendUsername = friendDoc.getString("username") ?: "No Username"
                                    Log.d("FriendsActivity", "Friend username: $friendUsername")

                                    friendsList.add(Friend(userId = friendId, username = friendUsername))
                                    friendsAdapter.notifyDataSetChanged()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FriendsActivity", "Error fetching friend's username", e)
                                }
                        }
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("FriendsActivity", "Error loading friends: ", exception)
            }
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut() // sign out from Firebase
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Redirect user to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clears back stack
        startActivity(intent)
        finish()
    }

    private fun addFriend() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("FriendsActivity", "User not logged in")
            return
        }

        val enteredUsername = findViewById<EditText>(R.id.edtFriendUsername).text.toString().trim()
        if (enteredUsername.isEmpty()) {
            Log.e("FriendsActivity", "Username field is empty")
            return
        }

        val db = FirebaseFirestore.getInstance()

        Log.d("FriendsActivity", "Searching for username: $enteredUsername")

        // Search for user by username
        db.collection("users")
            .whereEqualTo("username", enteredUsername)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val friendUserId = documents.documents[0].id
                    val currentUserId = currentUser.uid

                    Log.d("FriendsActivity", "Found friend UID: $friendUserId")

                    // 🔑 Generate unique friendship ID
                    val friendshipId = if (currentUserId < friendUserId) {
                        "${currentUserId}_$friendUserId"
                    } else {
                        "${friendUserId}_$currentUserId"
                    }

                    val friendData = hashMapOf(
                        "userID1" to currentUserId,
                        "userID2" to friendUserId
                    )

                    Log.d("FriendsActivity", "Saving friendship in Firestore: $friendshipId")

                    // Store friendship in Firestore
                    db.collection("friends")
                        .document(friendshipId)
                        .set(friendData)
                        .addOnSuccessListener {
                            Log.d("FriendsActivity", "Friend added successfully")
                            Toast.makeText(this, "Friend added successfully!", Toast.LENGTH_SHORT).show()

                            // Refresh friends list immediately after adding a new friend
                            loadFriends()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FriendsActivity", "Error adding friend", exception)
                        }
                } else {
                    Log.e("FriendsActivity", "User not found in Firestore")
                    Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FriendsActivity", "Error searching for user", exception)
            }
    }
    //Show menu when a friend is clicked
    private fun showFriendOptions(friend: Friend) {
        val options = arrayOf("View Details", "Delete Friend", "Send Message")

        AlertDialog.Builder(this)
            .setTitle("Choose an action for ${friend.username}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewFriendDetails(friend)  // View details
                    1 -> deleteFriend(friend)  // Delete friend
                    2 -> sendMessage(friend)  // Send a message
                }
            }
            .show()
    }

    //View Friend Details
    private fun viewFriendDetails(friend: Friend) {
        val intent = Intent(this, FriendDetailsActivity::class.java)
        intent.putExtra("friendId", friend.userId)
        startActivity(intent)
    }

    //Delete Friend
    private fun deleteFriend(friend: Friend) {
        val db = FirebaseFirestore.getInstance()

        // Find and delete the friendship document
        db.collection("friends")
            .whereEqualTo("userID1", friend.userId)
            .whereEqualTo("userID2", FirebaseAuth.getInstance().uid)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete().addOnSuccessListener {
                        Toast.makeText(this, "${friend.username} removed from friends", Toast.LENGTH_SHORT).show()
                        loadFriends()  // Refresh the list
                    }
                }
            }
    }

    // Send Message
    private fun sendMessage(friend: Friend) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("friendId", friend.userId)
        startActivity(intent)
    }
}



