package com.example.savourit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.savourit.base.BaseActivity;
import com.example.savourit.feature.auth.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000; // 2 seconds splash duration
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Apply language before anything
        String lang = prefs.getString("lang", "en");
        LocaleHelper.setLocale(this, lang);

        // Apply dark mode BEFORE super.onCreate
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);

        // Full screen splash
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // Firebase stuff...
        auth = FirebaseAuth.getInstance();
        new Handler().postDelayed(() -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                startActivity(new Intent(this, BaseActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
        }, SPLASH_TIME_OUT);
    }
}
