package com.example.savourit;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class AccountSettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        Button backButton = findViewById(R.id.back_to_settings_button);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }
}
