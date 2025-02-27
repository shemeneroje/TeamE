package com.example.savourit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Switch notificationSwitch = findViewById(R.id.switch_notifications);
        Switch darkModeSwitch = findViewById(R.id.switch_dark_mode);
        Switch autoUpdateSwitch = findViewById(R.id.switch_auto_update);
        Button accountButton = findViewById(R.id.account_settings_button);

        if (notificationSwitch != null) {
            notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                    Toast.makeText(SettingsActivity.this,
                            isChecked ? "Notifications Enabled" : "Notifications Disabled", Toast.LENGTH_SHORT).show());
        }

        if (darkModeSwitch != null) {
            darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                    Toast.makeText(SettingsActivity.this,
                            isChecked ? "Dark Mode Enabled" : "Dark Mode Disabled", Toast.LENGTH_SHORT).show());
        }

        if (autoUpdateSwitch != null) {
            autoUpdateSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                    Toast.makeText(SettingsActivity.this,
                            isChecked ? "Auto-Update Enabled" : "Auto-Update Disabled", Toast.LENGTH_SHORT).show());
        }

        if (accountButton != null) {
            accountButton.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, AccountSettingsActivity.class);
                startActivity(intent);
            });
        }
    }
}
