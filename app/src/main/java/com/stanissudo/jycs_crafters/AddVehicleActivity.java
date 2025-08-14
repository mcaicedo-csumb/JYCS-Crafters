package com.stanissudo.jycs_crafters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;
import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.database.entities.Vehicle;
import com.stanissudo.jycs_crafters.databinding.ActivityVehicleBinding;
import com.stanissudo.jycs_crafters.viewHolders.VehicleViewModel;

/**
 * @author Ysabelle Kim
 * created: 8/1/2025 - 6:42 PM
 * @project JYCS-Crafters
 * file: AddVehicleActivity.java
 * @since 1.0.0
 * Explanation: <p>AddVehicleActivity handles adding new vehicles to a user's account.
 * If the user chose to edit an existing vehicle from GarageActivity, it pulls this
 * vehicle's information from the database and prepopulates the input fields.</p>
 */
public class AddVehicleActivity extends BaseDrawerActivity {
    private ActivityVehicleBinding binding;
    FuelTrackAppRepository repository;

    private static final String VEHICLE_USER_ID = "com.stanissudo.jycs-crafters.VEHICLE_USER_ID";
    private int userId;
    private VehicleViewModel viewModel;
    /**
     * True if this Activity was launched to edit an existing record.
     */
    private boolean isEdit;
    /**
     * The LogID of the record being edited. Only meaningful if {@link #isEdit} is true.
     */
    private int editVehicleID;

    /**
     * Intent extra key: primary key of a {@link Vehicle} to edit. Only present in EDIT mode.
     */
    public static final String EXTRA_VEHICLE_ID = "EXTRA_VEHICLE_ID";

    /**
     * vehicleIntentFactory() returns an intent to change the screen
     *
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
     * @param context   Caller context
     * @param vehicleID Primary key of the existing {@link Vehicle}
     * @return Intent ready to pass to {@link Context#startActivity(Intent)}
     */
    public static Intent editVehicleIntentFactory(Context context, int vehicleID) {
        return new Intent(context, AddVehicleActivity.class)
                .putExtra(EXTRA_VEHICLE_ID, vehicleID);
    }

    /**
     * onCreate() creates Vehicle activity to add vehicles
     *
     * @param savedInstanceState Bundle object
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVehicleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // ViewModel
        viewModel = new ViewModelProvider(this).get(VehicleViewModel.class);
        repository = FuelTrackAppRepository.getRepository(getApplication());

        // Extract intent extras.
        editVehicleID = getIntent().getIntExtra(EXTRA_VEHICLE_ID, -1);
        isEdit = editVehicleID > 0;
        setTitle(isEdit ? "Edit Vehicle" : "Add Vehicle");

        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        // EDIT mode: prefill from DB without triggering calculations.
        if (isEdit) {
            viewModel.getVehicleByID(editVehicleID).observe(this, e -> {
                if (e == null) return;

                // Prefill fields.
                binding.vehicleNameEditText.setText((e.getName()));
                binding.vehicleMakeEditText.setText(e.getMake());
                binding.vehicleModelEditText.setText(e.getModel());
                binding.vehicleYearEditText.setText(String.valueOf(e.getYear()));
            });
        }

        // Save action: validate + insert/update.
        binding.vehicleSaveButton.setOnClickListener(v -> onSave());
    }

    /**
     * onSave() gathers user inputs, perform basic validation, then run an asynchronous odometer sanity check
     * before committing insert/update via the {@link VehicleViewModel}.
     */
    private void onSave() {
        // Read fields (fallback to 0 on parse issues)
        String name = binding.vehicleNameEditText.getText().toString();
        String make = binding.vehicleMakeEditText.getText().toString();
        String model = binding.vehicleModelEditText.getText().toString();
        int year = safeInt(text(binding.vehicleYearEditText));

        // Build entity
        Vehicle vehicle = new Vehicle();
        if (isEdit) vehicle.setVehicleID(editVehicleID);
        vehicle.setUserId(userId);
        vehicle.setName(name);
        vehicle.setMake(make);
        vehicle.setModel(model);
        vehicle.setYear(year);

        // Synchronous field validation (basic required checks)
        if (!validateInputsBasic(vehicle)) return;

        try {
            if (isEdit) viewModel.update(vehicle);
            else viewModel.insert(vehicle);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to add or edit vehicle...", Toast.LENGTH_LONG).show();
            finish();
        }
        finish();
    }

    /**
     * validateInputsBasic(Vehicle) performs quick client-side validation for required fields and non-negative values.
     *
     * @param vehicle The entity being validated
     * @return true if basic checks pass; false otherwise (with a Toast shown)
     */
    private boolean validateInputsBasic(Vehicle vehicle) {
        if (vehicle.getName().equals(null)) {
            Toast.makeText(this, "The vehicle must have a name.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (vehicle.getMake().equals(null)) {
            // Message says > 0, but code allows 0. Change to <= 0 if you want to enforce strictly > 0.
            Toast.makeText(this, "The vehicle must have a make (manufacturer).", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (vehicle.getModel().equals(null)) {
            // Message says > 0, but code allows 0. Change to <= 0 if you want to enforce strictly > 0.
            Toast.makeText(this, "The vehicle must have a model.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (vehicle.getYear() <= 1885) {
            Toast.makeText(this, "A valid year is required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * safeInt(String) parses int or return 0 on failure.
     */
    private int safeInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * text(TextView) returns trimmed text from any TextView (empty string if null).
     */
    private static String text(android.widget.TextView tv) {
        return tv.getText() == null ? "" : tv.getText().toString().trim();
    }

    /**
     * getDrawerLayout() from BaseDrawerActivity gets the layout of the sidebar
     *
     * @return drawerLayout
     */
    @Override
    protected DrawerLayout getDrawerLayout() {
        return binding.drawerLayout;
    }

    /**
     * getNavigationView() from BaseDrawerActivity gets the sidebar
     *
     * @return navView
     */
    @Override
    protected NavigationView getNavigationView() {
        return binding.navView;
    }

    /**
     * getToolbar() from BaseDrawerActivity gets the top toolbar
     *
     * @return toolbar
     */
    @Override
    protected Toolbar getToolbar() {
        return binding.toolbar;
    }
}