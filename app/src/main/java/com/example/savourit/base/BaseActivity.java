package com.example.savourit.base;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
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
import com.example.savourit.feature.auth.login.LoginActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.example.savourit.LocaleHelper;

public class BaseActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private Toolbar toolbar;
    private FirebaseAuth auth;
    private ActionBarDrawerToggle toggle; // Ensure drawer toggle works

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // Initialize Views
        auth = FirebaseAuth.getInstance();
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Ensure toolbar is set

        // Properly obtain the NavController from the NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }
        // Set up the navigation UI

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_friends, R.id.nav_settings
        ).setOpenableLayout(drawerLayout).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

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
        setupDrawerToggle();
    }

    private void setupDrawerToggle() {
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }
    private void logoutUser() {

        auth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
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
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(newBase);
        String lang = prefs.getString("lang", "en");
        Context context = com.example.savourit.LocaleHelper.setLocale(newBase, lang);
        super.attachBaseContext(context);
    }
}

