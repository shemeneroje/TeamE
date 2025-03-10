package com.example.savourit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword, edtUsername;
    private Button btnSignup, btnGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtUsername = findViewById(R.id.edtUsername);
        btnSignup = findViewById(R.id.btnSignup);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);

        btnSignup.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String username = edtUsername.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                edtEmail.setError("Required");
                edtPassword.setError("Required");
                edtUsername.setError("Required");
                return;
            }

            registerUser(email, password, username);
        });
        btnGoToLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
    }

    private void registerUser(String email, String password, String username) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = auth.getCurrentUser().getUid();

                    //Store default user details in Firestore
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("username", username);
                    userData.put("email", email);
                    userData.put("status", "free"); // Default status

                    db.collection("users").document(userId).set(userData)
                            .addOnSuccessListener(aVoid -> {
                                //Redirect to Main Page
                                startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> Log.e("Signup", "Failed to add user: " + e.getMessage()));
                })
                .addOnFailureListener(e -> Log.e("Signup", "Registration failed: " + e.getMessage()));
    }

}
