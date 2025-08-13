package com.stanissudo.jycs_crafters;

import static com.stanissudo.jycs_crafters.MainActivity.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.navigation.NavigationView;
import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.databinding.ActivityFuelLogBinding;
import com.stanissudo.jycs_crafters.utils.CarSelectorHelper;
import com.stanissudo.jycs_crafters.viewHolders.FuelEntryViewModel;
import com.stanissudo.jycs_crafters.viewHolders.FuelLogAdapter;
import com.stanissudo.jycs_crafters.viewHolders.FuelLogViewModel;
import com.stanissudo.jycs_crafters.viewHolders.SharedViewModel;
import com.stanissudo.jycs_crafters.viewHolders.VehicleViewModel;


public class FuelLogActivity extends BaseDrawerActivity {
    private ActivityFuelLogBinding binding;
    private static final String FUEL_LOG_USER_ID = "com.stanissudo.gymlog.FUEL_LOG_USER_ID";
    private FuelLogViewModel viewModel;

    private VehicleViewModel vehicleViewModel;
    private SharedViewModel sharedViewModel;


@Override
protected void onCreate(Bundle savedInstanceState) {
    binding = ActivityFuelLogBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    super.onCreate(savedInstanceState);

    viewModel = new ViewModelProvider(this).get(FuelLogViewModel.class);
    // --- 1. Initialize ViewModels ---
    vehicleViewModel = new ViewModelProvider(this).get(VehicleViewModel.class);
    sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

    SharedPreferences sharedPreferences  = getSharedPreferences("login_prefs", MODE_PRIVATE);;
    vehicleViewModel.loadUserVehicles(sharedPreferences.getInt("userId", -1));
    vehicleViewModel.getUserVehicles().observe(this, vehicles -> {
        if (vehicles != null && !vehicles.isEmpty()) {
            // a) Push the fresh list into the helper (likely replaces an in-memory cache)
            CarSelectorHelper.loadVehicleData(this, vehicles);

            // b) Re-create / reset the dropdown’s adapter from that fresh list
            AutoCompleteTextView carSelectorDropdown = binding.toolbarDropdown;
            CarSelectorHelper.setupDropdown(this, carSelectorDropdown);

            // c) Pick the initial selection and propagate it
            Integer initialCarId = CarSelectorHelper.getSelectedOptionKey();
            if (initialCarId != -1) {
                sharedViewModel.selectCar(initialCarId);
            }
        }
    });

    // New RecyclerView
    binding.logDisplayRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    FuelLogAdapter adapter = new FuelLogAdapter(new FuelLogAdapter.Callbacks() {
        @Override public void onDeleteClicked(long id) {
            new AlertDialog.Builder(FuelLogActivity.this)
                    .setMessage("Delete this entry?")
                    .setPositiveButton("Delete", (d, w) -> viewModel.deleteById(id))
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        @Override
        public void onEditClicked(long id) {
            //int userId = getIntent().getIntExtra(FUEL_LOG_USER_ID, -1);
            startActivity(AddFuelEntryActivity.editIntentFactory(FuelLogActivity.this, CarSelectorHelper.getSelectedOptionKey(), (int) id));
        }
    });
    binding.logDisplayRecyclerView.setAdapter(adapter);


    // Observe ONCE
//    vm.entries.observe(this, adapter::submitList);
// Observe entries
    viewModel.entries.observe(this, adapter::submitList);


//    // Dropdown
    AutoCompleteTextView drop = binding.toolbarDropdown;
    CarSelectorHelper.setupDropdown(this, drop);

    // Initial car
    Integer initialCarId = CarSelectorHelper.getSelectedOptionKey();
    if (initialCarId != null && initialCarId > 0) {
        viewModel.setSelectedCarId(initialCarId);
    } else {
        Toast.makeText(this, "Select a car to load entries", Toast.LENGTH_SHORT).show();
    }
//
    // On change
    drop.setOnItemClickListener((parent, view, position, id) -> {
        String name = (String) parent.getItemAtPosition(position);
        CarSelectorHelper.setSelectedOption(this, name);
        Integer newCarId = CarSelectorHelper.getSelectedOptionKey();
        if (newCarId == null || newCarId <= 0) {
            Toast.makeText(this, "Invalid car selection", Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.setSelectedCarId(newCarId);
    });
}
    static Intent fuelLogIntentFactory(Context context) {
        Intent intent = new Intent(context, FuelLogActivity.class);
       // intent.putExtra(FUEL_LOG_USER_ID, userId);
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
