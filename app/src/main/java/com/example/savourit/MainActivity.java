package com.example.savourit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.savourit.base.BaseActivity;
import com.example.savourit.feature.auth.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000; // 2 seconds splash duration
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make the splash full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        new Handler().postDelayed(() -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                // User is already logged in, go to MainActivity
                startActivity(new Intent(this, BaseActivity.class));
            } else {
                // User is not logged in, go to LoginActivity
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish(); // Close splash screen
        }, SPLASH_TIME_OUT);
    }
}
