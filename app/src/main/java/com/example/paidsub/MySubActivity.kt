package com.example.paidsub

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MySubscriptionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val monthlyButton = findViewById<Button>(R.id.plus_sub)
        val yearlyButton = findViewById<Button>(R.id.premium_sub)

        if (monthlyButton == null) Log.e("MySubscriptionActivity", "plus_sub button not found!")
        if (yearlyButton == null) Log.e("MySubscriptionActivity", "premium_sub button not found!")

        monthlyButton?.setOnClickListener {
            Log.d("MySubscriptionActivity", "Plus button clicked")
            showSubscriptionFeatures(
                "Plus Subscription Features",
                "You can add friends!\nYou can see their favourite restaurants!\nAnd what opinion they have on them!"
            )
        }

        yearlyButton?.setOnClickListener {
            Log.d("MySubscriptionActivity", "Premium button clicked")
            showSubscriptionFeatures(
                "Premium Subscription Features",
                "Includes the same features as the plus version!\nYou will be given recommendations based on what you and your friends love!\nGame?"
            )
        }
    }

    private fun showSubscriptionFeatures(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
