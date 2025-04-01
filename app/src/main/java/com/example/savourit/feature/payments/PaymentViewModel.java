package com.example.savourit.feature.payments;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.savourit.data.local.AppDatabase;
import com.example.savourit.data.local.AppPrefs;
import com.example.savourit.data.local.entities.UserEntity;
import com.example.savourit.utils.ExecutorUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class PaymentViewModel extends AndroidViewModel {

    public MutableLiveData<Boolean> upgradeSuccess = new MutableLiveData<>();
    public MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final AppDatabase database;

    public PaymentViewModel(@NonNull Application application) {
        super(application);
        this.database = AppDatabase.getInstance(application.getApplicationContext());
    }

    public void upgradeToPremium() {
        if (auth.getCurrentUser() == null) {
            errorMessage.postValue("User not logged in.");
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).update("isPremium", true)
                .addOnSuccessListener(unused -> {
                    ExecutorUtils.getInstance().runInBackground(() -> {
                        UserEntity user = database.userDao().getLoggedInUser();
                        if (user != null) {
                            user.premium = true;
                            database.userDao().insertUser(user);
                        }
                        AppPrefs.resetLaunchCount(getApplication());
                        upgradeSuccess.postValue(true);
                    });
                })
                .addOnFailureListener(e -> errorMessage.postValue("Failed to upgrade: " + e.getMessage()));
    }
}
