package com.example.savourit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MySubscriptionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button plusSubButton = findViewById(R.id.plus_sub);
        Button premiumSubButton = findViewById(R.id.premium_sub);
        Button subButton = findViewById(R.id.sub_button);

        plusSubButton.setOnClickListener(v -> showSubscriptionFeatures(
                "Plus Subscription Features",
                "• You can add friends!\n• You can see their favorite restaurants!\n• And what opinion they have on them!"
        ));

        premiumSubButton.setOnClickListener(v -> showSubscriptionFeatures(
                "Premium Subscription Features",
                "• Includes the same features as the Plus version!\n• You will be given recommendations based on what you and your friends love!\n• More features coming soon!"
        ));

        subButton.setOnClickListener(v -> {
            Intent intent = new Intent(MySubscriptionActivity.this, PaymentActivity.class);
            startActivity(intent);
        });
    }

    private void showSubscriptionFeatures(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
