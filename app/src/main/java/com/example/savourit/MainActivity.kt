package com.example.savourit

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    //Linru Wang
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            //LoginActivity if user is not logged in
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            //FriendsActivity if user is logged in
            startActivity(Intent(this, FriendsActivity::class.java))
            finish()
        }
    }
}
