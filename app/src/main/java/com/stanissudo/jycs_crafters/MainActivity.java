package com.stanissudo.jycs_crafters;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.stanissudo.jycs_crafters.databinding.ActivityMainBinding;
import com.stanissudo.jycs_crafters.fragments.HomeFragment;
import com.stanissudo.jycs_crafters.fragments.SettingsFragment;

public class MainActivity extends BaseDrawerActivity {

    private ActivityMainBinding binding;
    private SharedPreferences sharedPreferences;
    public static final String TAG = "FuelTrackApp_Log";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User");

        // Set username in drawer header
        NavigationView navView = binding.navView;
        View headerView = navView.getHeaderView(0);
        TextView usernameText = headerView.findViewById(R.id.nav_header_username);
        usernameText.setText(username);

        // Handle navigation item clicks
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showHomeFragment();
            } else if (id == R.id.nav_fuel_entry) {
                startActivity(new Intent(this, AddFuelEntryActivity.class));
            } else if (id == R.id.nav_settings) {
                showSettingsFragment();
            } else if (id == R.id.nav_logout) {
                logout();
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Back button handling
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
            }
        });

        // Default fragment
        if (savedInstanceState == null) {
            showHomeFragment();
            navView.setCheckedItem(R.id.nav_home);
        }
    }

    public void showSettingsFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SettingsFragment())
                .commit();
    }

    private void showHomeFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    private void logout() {
        sharedPreferences.edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected DrawerLayout getDrawerLayout() {
        return binding.drawerLayout;
    }

    @Override
    protected NavigationView getNavigationView() {
        return binding.navView;
    }

    @Override
    protected Toolbar getToolbar() {
        return binding.toolbar;
    }
}
