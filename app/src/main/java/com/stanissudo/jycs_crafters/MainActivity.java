package com.stanissudo.jycs_crafters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.databinding.ActivityMainBinding;
import com.stanissudo.jycs_crafters.fragments.HomeFragment;
import com.stanissudo.jycs_crafters.fragments.SettingsFragment;
import com.stanissudo.jycs_crafters.utils.CarSelectorHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseDrawerActivity {

    private ActivityMainBinding binding;
    private FuelTrackAppRepository repository;
    public static final String TAG = "FuelTrackApp_Log";
    private static final String MAIN_ACTIVITY_USER_ID = "com.example.wk05hw03_gymlog.MAIN_ACTIVITY_USER_ID";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = FuelTrackAppRepository.getRepository(getApplication());

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
            binding.navView.setCheckedItem(R.id.nav_home);
        }
        AutoCompleteTextView carSelectorDropdown = binding.toolbarDropdown;
        CarSelectorHelper.setupDropdown(this, carSelectorDropdown);
    }

    static Intent mainActivityIntentFactory(Context context, int userID) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MAIN_ACTIVITY_USER_ID, userID);
        return intent;
    }
        
    @Override
    protected void onResume() {
        super.onResume();
        CarSelectorHelper.updateDropdownText(binding.toolbarDropdown);
    }

    public void showSettingsFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
        binding.navView.setCheckedItem(R.id.nav_settings);
    }

    private void showHomeFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        binding.navView.setCheckedItem(R.id.nav_home);
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
