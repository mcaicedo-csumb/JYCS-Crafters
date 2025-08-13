package com.stanissudo.jycs_crafters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;
import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.database.entities.FuelEntry;
import com.stanissudo.jycs_crafters.database.entities.Vehicle;
import com.stanissudo.jycs_crafters.databinding.ActivityVehicleBinding;
import com.stanissudo.jycs_crafters.viewHolders.GarageViewModel;

import java.util.InputMismatchException;

/**
 * @author Ysabelle Kim
 * created: 8/1/2025 - 6:42 PM
 * @project JYCS-Crafters
 * file: AddVehicleActivity.java
 * @since 1.0.0
 * Explanation: AddVehicleActivity handles adding new vehicles to a user's account
 */
public class AddVehicleActivity extends BaseDrawerActivity {
    private ActivityVehicleBinding binding;
    FuelTrackAppRepository repository = FuelTrackAppRepository.getRepository(getApplication());

    private static final String VEHICLE_USER_ID = "com.stanissudo.jycs-crafters.VEHICLE_USER_ID";
    private SharedPreferences sharedPreferences;
    private String vehicleName = "";
    private String vehicleMake = "";
    private String vehicleModel = "";
    private int vehicleYear = 0;

    /** Intent extra key: primary key of a {@link Vehicle} to edit. Only present in EDIT mode. */
    public static final String EXTRA_VEHICLE_ID  = "EXTRA_VEHICLE_ID";

    /**
     * onCreate() creates Vehicle activity to add vehicles
     * @param savedInstanceState Bundle object
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVehicleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ViewModel
        GarageViewModel vm = new ViewModelProvider(this).get(GarageViewModel.class);

        setSupportActionBar(binding.toolbar);

        sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        binding.vehicleSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInformationFromDisplay();
                insertRecord(userId);
                Intent intent = GarageActivity.garageIntentFactory(getApplicationContext(), userId);
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
     * insertRecord() inserts a Vehicle record into the database
     */
    private void insertRecord(int userId){
        Vehicle vehicle = new Vehicle(userId, vehicleName, vehicleMake, vehicleModel, vehicleYear);
        repository.insertVehicle(vehicle);
    }

    /**
     * vehicleIntentFactory() returns an intent to change the screen
     * @param context context
     * @return intent
     */
    static Intent vehicleIntentFactory(Context context, int userId) {
        Intent intent = new Intent(context, AddVehicleActivity.class);
        intent.putExtra(VEHICLE_USER_ID, userId);
        return intent;
    }

    /**
     * Build an {@link Intent} to open this Activity in EDIT mode.
     *
     * @param context Caller context
     * @param vehicleID   Primary key of the existing {@link Vehicle}
     * @return Intent ready to pass to {@link Context#startActivity(Intent)}
     */
    public static Intent editVehicleIntentFactory(Context context, int vehicleID) {
        return new Intent(context, AddFuelEntryActivity.class)
                .putExtra(EXTRA_VEHICLE_ID, vehicleID);
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