package com.stanissudo.jycs_crafters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.databinding.ActivityFuelLogBinding;
import com.stanissudo.jycs_crafters.utils.CarSelectorHelper;

public class FuelLogActivity extends BaseDrawerActivity {
    private ActivityFuelLogBinding binding;
    private static final String FUEL_LOG_USER_ID = "com.stanissudo.gymlog.FUEL_LOG_USER_ID";
    FuelTrackAppRepository repository = FuelTrackAppRepository.getRepository(getApplication());
    int loggedInUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFuelLogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loggedInUserId = getIntent().getIntExtra(FUEL_LOG_USER_ID, -1);
        AutoCompleteTextView carSelectorDropdown = binding.toolbarDropdown;
        CarSelectorHelper.setupDropdown(this, carSelectorDropdown);
    }

    static Intent fuelLogIntentFactory(Context context, int userId) {
        Intent intent = new Intent(context, FuelLogActivity.class);
        intent.putExtra(FUEL_LOG_USER_ID, userId);
        return intent;
    }
    @Override
    protected void onResume() {
        super.onResume();
        CarSelectorHelper.updateDropdownText(binding.toolbarDropdown);
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
