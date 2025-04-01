package com.example.savourit.feature.auth.signup;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.savourit.data.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpViewModel extends AndroidViewModel {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public MutableLiveData<Boolean> signUpSuccess = new MutableLiveData<>();
    public MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public SignUpViewModel(@NonNull Application application) {
        super(application);
    }

    public void signUpUser(String username, String firstName, String lastName, String email, String password) {
        if (username.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorMessage.setValue("All fields are required");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            User user = new User(username, firstName, lastName, email, false); // premium = false

                            db.collection("users").document(userId).set(user)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            signUpSuccess.setValue(true);
                                        } else {
                                            errorMessage.setValue("Failed to save user data");
                                        }
                                    });
                        }
                    } else {
                        errorMessage.setValue("Authentication failed: " + task.getException().getMessage());
                    }
                });
    }
}
