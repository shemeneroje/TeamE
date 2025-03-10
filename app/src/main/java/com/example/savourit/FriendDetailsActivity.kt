package com.example.savourit

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendDetailsActivity : AppCompatActivity() {
    //Linru Wang
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_details)

        val friendId = intent.getStringExtra("friendId")

        //Fetch and display friend details from Firestore
        FirebaseFirestore.getInstance().collection("users")
            .document(friendId!!)
            .get()
            .addOnSuccessListener { document ->
                findViewById<TextView>(R.id.txtFriendUsername).text = document.getString("username")
                findViewById<TextView>(R.id.txtFriendEmail).text = document.getString("email")
            }
    }
}
