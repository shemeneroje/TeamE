package com.example.savourit.feature.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.savourit.R;

public class ForgotPasswordFragment extends Fragment {

    private EditText emailEditText;
    private Button resetPasswordButton;
    private TextView backToLoginText;

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        emailEditText = view.findViewById(R.id.email_edit_text);
        resetPasswordButton = view.findViewById(R.id.reset_password_button);
        backToLoginText = view.findViewById(R.id.back_to_login);

        // Reset password logic
        resetPasswordButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(getContext(), "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Call the method to send password reset email (example with Firebase Auth)
            sendPasswordResetEmail(email);
        });

        // Navigate back to login screen
        backToLoginText.setOnClickListener(v -> {
            // Code to go back to login screen
            // You can replace this with a Navigation action or an Intent
            getActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }

    private void sendPasswordResetEmail(String email) {
        // Placeholder for backend or Firebase integration
        // For Firebase Authentication, you would use:
        // FirebaseAuth.getInstance().sendPasswordResetEmail(email)
        //     .addOnCompleteListener(task -> {
        //         if (task.isSuccessful()) {
        //             Toast.makeText(getContext(), "Password reset link sent", Toast.LENGTH_SHORT).show();
        //         } else {
        //             Toast.makeText(getContext(), "Error sending reset link", Toast.LENGTH_SHORT).show();
        //         }
        //     });

        // Simulate sending reset email for demonstration
        Toast.makeText(getContext(), "Password reset link sent to: " + email, Toast.LENGTH_SHORT).show();
    }
}
