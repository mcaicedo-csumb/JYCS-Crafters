package com.stanissudo.jycs_crafters;

import static com.stanissudo.jycs_crafters.MainActivity.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.navigation.NavigationView;
import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.database.VehicleDAO_Impl;
import com.stanissudo.jycs_crafters.database.entities.FuelEntry;
import com.stanissudo.jycs_crafters.database.entities.Vehicle;
import com.stanissudo.jycs_crafters.databinding.ActivityVehicleBinding;
import com.stanissudo.jycs_crafters.utils.CarSelectorHelper;
import com.stanissudo.jycs_crafters.viewHolders.FuelLogAdapter;
import com.stanissudo.jycs_crafters.viewHolders.FuelLogViewModel;
import com.stanissudo.jycs_crafters.viewHolders.VehicleAdapter;
import com.stanissudo.jycs_crafters.viewHolders.VehicleViewModel;

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
    private ActivityVehicleBinding binding;
    FuelTrackAppRepository repository = FuelTrackAppRepository.getRepository(getApplication());

    private static final String VEHICLE_USER_ID = "com.stanissudo.jycs-crafters.VEHICLE_USER_ID";
    private SharedPreferences sharedPreferences;
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

        // ViewModel
        VehicleViewModel vm = new ViewModelProvider(this).get(VehicleViewModel.class);

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
        Intent intent = new Intent(context, VehicleActivity.class);
        intent.putExtra(VEHICLE_USER_ID, userId);
        return intent;
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