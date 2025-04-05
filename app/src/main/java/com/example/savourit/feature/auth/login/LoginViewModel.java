package com.example.savourit.feature.auth.login;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.savourit.data.local.AppDatabase;
import com.example.savourit.utils.UserUtils;
import com.google.firebase.auth.FirebaseAuth;

public class LoginViewModel extends AndroidViewModel {

    private FirebaseAuth mAuth ;
    public MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    public MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final AppDatabase database;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        mAuth = FirebaseAuth.getInstance();
        database = AppDatabase.getInstance(application.getApplicationContext());
    }

    public void loginUser(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            errorMessage.setValue("All fields are required");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserUtils.syncUserFromFirestoreToLocal(getApplication().getApplicationContext(),
                                () -> loginSuccess.postValue(true),   // onSuccess
                                () -> errorMessage.postValue("Failed to sync user data") // onFailure
                        );
                    } else {
                        errorMessage.setValue("Login failed: " + task.getException().getMessage());
                    }
                });
    }

}
