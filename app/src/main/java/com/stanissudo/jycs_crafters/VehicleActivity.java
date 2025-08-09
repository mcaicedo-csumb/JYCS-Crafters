package com.stanissudo.jycs_crafters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.databinding.ActivityVehicleBinding;
import com.stanissudo.jycs_crafters.utils.CarSelectorHelper;

import java.util.InputMismatchException;

/**
 * @author Ysabelle Kim
 * created: 8/1/2025 - 6:42 PM
 * @project JYCS-Crafters
 * file: VehicleActivity.java
 * @since 1.0.0
 * Explanation: VehicleActivity handles adding new vehicles to a user's account
 */
public class VehicleActivity extends BaseDrawerActivity {
    private com.stanissudo.jycs_crafters.databinding.ActivityVehicleBinding binding;
    private FuelTrackAppRepository repository;
    int loggedInUserId = -1;
    private String vehicleName = "";
    private String vehicleMake = "";
    private String vehicleModel = "";
    private int vehicleYear = 0;

    /**
     * onCreate() creates Vehicle activity to add vehicles
     * @param savedInstanceState Bundle object
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVehicleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = FuelTrackAppRepository.getRepository(getApplication());

        setSupportActionBar(binding.toolbar);

        binding.vehicleSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInformationFromDisplay();
                // TODO: create Vehicle entity to insert record into
                //insertRecord();
                // TODO: use loggedInUserId rather than -1 for mainActivityIntentFactory
                Intent intent = MainActivity.mainActivityIntentFactory(getApplicationContext(), -1);
                startActivity(intent);
            }
        });
    }

    /**
     * getInformationFromDisplay() sets the user's input from application fields to variables
     */
    private void getInformationFromDisplay() {

        try {
            vehicleName = binding.vehicleNameEditText.getText().toString();
            vehicleMake = binding.vehicleMakeEditText.getText().toString();
            vehicleModel = binding.vehicleModelEditText.getText().toString();
            vehicleYear = Integer.parseInt(binding.vehicleYearEditText.getText().toString());
        } catch (InputMismatchException e) {
            Log.e("TAG", "Error reading values.");
        }
    }

    /**
     * vehicleIntentFactory() returns an intent to change the screen
     * @param context context
     * @return intent
     */
    static Intent vehicleIntentFactory(Context context) {
        return new Intent(context, VehicleActivity.class);
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