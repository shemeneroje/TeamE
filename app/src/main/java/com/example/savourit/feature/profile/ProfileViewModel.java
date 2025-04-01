package com.example.savourit.feature.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.savourit.data.remote.FireStoreHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

public class ProfileViewModel extends AndroidViewModel {

    private final FirebaseAuth auth;

    private final MutableLiveData<String> firstName = new MutableLiveData<>();
    private final MutableLiveData<String> lastName = new MutableLiveData<>();
    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> username = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        auth = FirebaseAuth.getInstance();
        loadUserData();
    }

    public LiveData<String> getFirstName() {
        return firstName;
    }

    public LiveData<String> getLastName() {
        return lastName;
    }

    public LiveData<String> getEmail() {
        return email;
    }

    public LiveData<String> getUsername() {
        return username;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void logout() {
        auth.signOut();
    }

    private void loadUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            error.setValue("User not logged in");
            return;
        }

        FireStoreHelper.getInstance().getUserData(currentUser.getUid(), new FireStoreHelper.FireStoreCallback<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot document) {
                firstName.postValue(document.getString("firstName"));
                lastName.postValue(document.getString("lastName"));
                email.postValue(document.getString("email"));
                username.postValue(document.getString("username"));
            }

            @Override
            public void onFailure(String errorMsg) {
                error.postValue(errorMsg);
            }
        });
    }
}
