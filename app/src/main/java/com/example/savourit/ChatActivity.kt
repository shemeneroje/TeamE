package com.example.savourit

import android.os.Bundle
import android.widget.TextView

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ChatActivity : AppCompatActivity() {
    //Linru Wang
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val friendId = intent.getStringExtra("friendId")
        val txtChatTitle = findViewById<TextView>(R.id.txtChatTitle)

        txtChatTitle.text = "Chat with $friendId" // Placeholder, replace with username fetching later
    }
}