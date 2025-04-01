package com.example.savourit.feature.payments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.savourit.R;
import com.example.savourit.base.BaseActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PaymentActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText cardNumberEditText, expiryDateEditText, cvvEditText;
    private Button confirmPaymentButton;
    private ProgressBar progressBar;
    private Button btnReset;
    private PaymentViewModel viewModel;
    private String planType = "Premium";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        mAuth = FirebaseAuth.getInstance();
        planType = getIntent().getStringExtra("planType");

        viewModel = new ViewModelProvider(this).get(PaymentViewModel.class);

        // UI init
        cardNumberEditText = findViewById(R.id.card_number_edit_text);
        expiryDateEditText = findViewById(R.id.expiry_date_edit_text);
        cvvEditText = findViewById(R.id.cvv_edit_text);
        confirmPaymentButton = findViewById(R.id.confirm_payment_button);
        progressBar = findViewById(R.id.progress_bar);
        btnReset = findViewById(R.id.reset_button);

        confirmPaymentButton.setOnClickListener(v -> processPayment(planType));

        observeViewModel();
    }

    private void processPayment(String planType) {
        viewModel.upgradeToPremium();
        progressBar.setVisibility(View.VISIBLE);
        btnReset.setVisibility(View.INVISIBLE);   // Hide reset button

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String paymentConfirmationMessage = "Thank you for your payment! You have successfully upgraded to the " + planType + " plan.";

            Toast.makeText(PaymentActivity.this, paymentConfirmationMessage, Toast.LENGTH_SHORT).show();

            progressBar.setVisibility(View.INVISIBLE);
            btnReset.setVisibility(View.VISIBLE);

            finish();
        } else {
            Toast.makeText(PaymentActivity.this, "Please log in to proceed with the payment.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            btnReset.setVisibility(View.VISIBLE);
        }
    }
    private void observeViewModel() {

        viewModel.upgradeSuccess.observe(this, success -> {
            if (success) {
                progressBar.setVisibility(View.INVISIBLE);
                btnReset.setVisibility(View.VISIBLE);
                Intent intent = new Intent(PaymentActivity.this, BaseActivity.class);
                intent.putExtra("navigate_to_profile", true);
                startActivity(intent);
                finish();
            }
        });

        viewModel.errorMessage.observe(this, error -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            btnReset.setVisibility(View.VISIBLE);
        });
    }
}
