package com.example.savourit.feature.auth.signup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.savourit.R;
import com.example.savourit.feature.auth.login.LoginActivity;

public class SignUpActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextFirstName, editTextLastName, editTextEmail, editTextPassword;
    private Button buttonSignUp;

    private SignUpViewModel signUpViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize UI
        editTextUsername = findViewById(R.id.editTextUname);
        editTextFirstName = findViewById(R.id.editTextFName);
        editTextLastName = findViewById(R.id.editTextLName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignUp = findViewById(R.id.buttonSignUp);

        // Initialize ViewModel
        signUpViewModel = new ViewModelProvider(this).get(SignUpViewModel.class);

        buttonSignUp.setOnClickListener(v -> {
            signUpViewModel.signUpUser(
                    editTextUsername.getText().toString().trim(),
                    editTextFirstName.getText().toString().trim(),
                    editTextLastName.getText().toString().trim(),
                    editTextEmail.getText().toString().trim(),
                    editTextPassword.getText().toString().trim()
            );
        });

        signUpViewModel.signUpSuccess.observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Sign-up successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finish();
            }
        });

        signUpViewModel.errorMessage.observe(this, error -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }
}
