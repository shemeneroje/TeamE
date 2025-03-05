package com.example.savourit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PaymentActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText cardNumberEditText, expiryDateEditText, cvvEditText;
    private Button confirmPaymentButton;
    private ProgressBar progressBar;
    private Button btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Get the plan type (Plus or Premium) from the Intent
        String planType = getIntent().getStringExtra("planType");

        // Initialize UI elements
        cardNumberEditText = findViewById(R.id.card_number_edit_text);
        expiryDateEditText = findViewById(R.id.expiry_date_edit_text);
        cvvEditText = findViewById(R.id.cvv_edit_text);
        confirmPaymentButton = findViewById(R.id.confirm_payment_button);
        progressBar = findViewById(R.id.progress_bar);  // Initialize the progress bar
        btnReset = findViewById(R.id.reset_button);    // Initialize reset button

        // Handle Confirm Payment Button click
        confirmPaymentButton.setOnClickListener(v -> {
            // Logic for payment processing (could be done with Stripe or any payment provider)
            processPayment(planType);
        });
    }

    private void processPayment(String planType) {
        progressBar.setVisibility(View.VISIBLE);  // Show progress bar
        btnReset.setVisibility(View.INVISIBLE);    // Hide the reset button

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String userEmail = user.getEmail();
            String paymentConfirmationMessage = "Thank you for your payment! You have successfully upgraded to the " + planType + " plan.";

            Toast.makeText(PaymentActivity.this, paymentConfirmationMessage, Toast.LENGTH_SHORT).show();

            progressBar.setVisibility(View.INVISIBLE); // Hide progress bar
            btnReset.setVisibility(View.VISIBLE); // Show the reset button again

            // Redirect to the profile page or any other page you want
            Intent intent = new Intent(PaymentActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        } else {
            // User is not logged in, ask them to log in first
            Toast.makeText(PaymentActivity.this, "Please log in to proceed with the payment.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE); // Hide progress bar
            btnReset.setVisibility(View.VISIBLE); // Show the reset button again
        }
    }
}


