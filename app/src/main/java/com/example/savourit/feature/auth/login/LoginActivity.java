package com.example.savourit.feature.auth.login;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.savourit.R;
import com.example.savourit.base.BaseActivity;
import com.example.savourit.feature.auth.signup.SignUpActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private LinearLayout buttonLogin;
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        spannable();

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.orange));

        // Initialize UI
        editTextEmail = findViewById(R.id.et_email);
        editTextPassword = findViewById(R.id.et_password);
        buttonLogin = findViewById(R.id.login_button_container);

        // ViewModel setup
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            loginViewModel.loginUser(email, password);
        });

        // Observers
        loginViewModel.loginSuccess.observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, BaseActivity.class));
                finish();
            }
        });

        loginViewModel.errorMessage.observe(this, message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        );
    }

    private void spannable() {
        String fullText = "Don't have an account? Sign up";
        SpannableString spannableString = new SpannableString(fullText);

        int signUpColor = ContextCompat.getColor(this, R.color.orange);
        int defaultColor = ContextCompat.getColor(this, R.color.colorNormalTextColor);
        int startIndex = fullText.indexOf("Sign up");
        int endIndex = startIndex + "Sign up".length();

        spannableString.setSpan(new ForegroundColorSpan(signUpColor), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(defaultColor), 0, startIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(signUpColor);
                ds.setUnderlineText(true);
            }
        }, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView textView = findViewById(R.id.tv_do_not_have_account);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
    }
}
