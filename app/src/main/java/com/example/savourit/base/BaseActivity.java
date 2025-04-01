package com.example.savourit.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.savourit.MainActivity;
import com.example.savourit.R;
import com.example.savourit.data.local.AppDatabase;
import com.example.savourit.data.local.AppPrefs;
import com.example.savourit.data.local.entities.UserEntity;
import com.example.savourit.utils.Constants;
import com.example.savourit.utils.ExecutorUtils;
import com.example.savourit.utils.components.Dialogs;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private Toolbar toolbar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // Initialize Views
        auth = FirebaseAuth.getInstance();
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up Navigation Controller
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // Only show hamburger on home screen
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home
        ).setOpenableLayout(drawerLayout).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Toolbar title changes with destination
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            toolbar.setTitle(destination.getLabel());
        });

        // Handle navigation drawer menu item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_logout) {
                Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
                logoutUser();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else {
                boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
                if (handled) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return handled;
            }
        });

        // Update user info in nav header
        updateNavHeader();

        // Profile header click
        setupProfilePage();

        // Handle external redirection to profile
        boolean navigateToProfile = getIntent().getBooleanExtra("navigate_to_home", false);
        if (navigateToProfile) {
            drawerLayout.post(() -> navController.navigate(R.id.nav_home));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void setupProfilePage() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        ImageView profileImage = headerView.findViewById(R.id.profile_image);
        profileImage.setOnClickListener(v -> {
            if (navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() != R.id.nav_profile) {
                navController.navigate(R.id.nav_profile);
            }
        });
    }

    private void updateNavHeader() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        TextView navUserName = headerView.findViewById(R.id.user_name);
        TextView navUserEmail = headerView.findViewById(R.id.user_email);

        navUserEmail.setText(currentUser.getEmail());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String firstName = document.getString("firstName");
                        String lastName = document.getString("lastName");
                        navUserName.setText(firstName + " " + lastName);
                    }
                })
                .addOnFailureListener(e -> navUserName.setText("User"));
    }

    private void logoutUser() {
        auth.signOut();
        AppDatabase.getInstance(this).userDao().clearUser();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ExecutorUtils.getInstance().runInBackground(() -> {
            UserEntity user = AppDatabase.getInstance(this).userDao().getLoggedInUser();
            if (user != null && !user.premium) {
                int count = AppPrefs.incrementLaunchCount(this);
                if (count % Constants.LAUNCH_COUNT_THRESHOLD_FOR_PREMIUM_DIALOG == 0) {
                    runOnUiThread(() -> {
                        Dialogs.showPremiumDialog(this);
                    });
                }
            }
        });
    }
}
