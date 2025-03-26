package com.example.savourit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class UpgradePlanActivity extends AppCompatActivity {

    private Button upgradePlusButton, upgradePremiumButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade_plan);

        // Initialize buttons
        upgradePlusButton = findViewById(R.id.upgrade_plus_button);
        upgradePremiumButton = findViewById(R.id.upgrade_premium_button);

        // Handle "Upgrade to Plus" button click
        upgradePlusButton.setOnClickListener(v -> {
            // Start the PaymentActivity for Plus version upgrade
            Intent intent = new Intent(UpgradePlanActivity.this, PaymentActivity.class);
            intent.putExtra("planType", "Plus");
            startActivity(intent);
        });

        // Handle "Upgrade to Premium" button click
        upgradePremiumButton.setOnClickListener(v -> {
            // Start the PaymentActivity for Premium version upgrade
            Intent intent = new Intent(UpgradePlanActivity.this, PaymentActivity.class);
            intent.putExtra("planType", "Premium");
            startActivity(intent);
        });
    }
}
