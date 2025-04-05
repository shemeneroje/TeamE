package com.example.savourit.feature.update;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.savourit.R;
import com.example.savourit.feature.payments.PaymentActivity;

public class UpgradePlanActivity extends AppCompatActivity {

    private LinearLayout upgradeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade_plan);

        // Initialize buttons
        upgradeButton = findViewById(R.id.payment_button_container);

        // Handle "Upgrade to Plus" button click
        upgradeButton.setOnClickListener(v -> {
            // Start the PaymentActivity for Plus version upgrade
            Intent intent = new Intent(UpgradePlanActivity.this, PaymentActivity.class);
            intent.putExtra("planType", "Plus");
            startActivity(intent);
        });

    }
}
