package com.example.savourit;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        EditText cardNumber = findViewById(R.id.card_number);
        EditText billingAddress = findViewById(R.id.billing_address);
        EditText expiryMonth = findViewById(R.id.expiry_month);
        EditText expiryYear = findViewById(R.id.expiry_year);
        EditText cvv = findViewById(R.id.cvv_code);
        Button subscribeButton = findViewById(R.id.confirm_payment);

        subscribeButton.setOnClickListener(v -> {
            String cardNum = cardNumber.getText().toString().trim();
            String billingAddr = billingAddress.getText().toString().trim();
            String month = expiryMonth.getText().toString().trim();
            String year = expiryYear.getText().toString().trim();
            String cvvCode = cvv.getText().toString().trim();

            if (cardNum.isEmpty() || billingAddr.isEmpty() || month.isEmpty() || year.isEmpty() || cvvCode.isEmpty()) {
                showErrorDialog();
            } else {
                processSubscription();
            }
        });
    }

    private void processSubscription() {
        new AlertDialog.Builder(this)
                .setTitle("Processing Payment")
                .setMessage("Your subscription is being processed...")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> showSuccessDialog())
                .show();
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Payment Successful")
                .setMessage("Thank you for subscribing! Your premium features are now activated.")
                .setPositiveButton("OK", (dialog, which) -> finish()) // Closes PaymentActivity
                .show();
    }

    private void showErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Please enter valid payment details.")
                .setPositiveButton("OK", null)
                .show();
    }
}
