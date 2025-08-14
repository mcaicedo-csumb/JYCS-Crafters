package com.stanissudo.jycs_crafters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.android.material.navigation.NavigationView;
import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.databinding.ActivityGarageBinding;
import com.stanissudo.jycs_crafters.utils.CarSelectorHelper;
import com.stanissudo.jycs_crafters.viewHolders.GarageAdapter;
import com.stanissudo.jycs_crafters.viewHolders.GarageViewModel;
import com.stanissudo.jycs_crafters.viewHolders.SharedViewModel;
import com.stanissudo.jycs_crafters.viewHolders.VehicleViewModel;

/**
 * @author Ysabelle Kim
 * created: 8/11/2025 - 2:08 AM
 * @version VERSION
 * Explanation:
 * @project JYCS-Crafters
 * @name GarageActivity.java
 */
public class GarageActivity extends BaseDrawerActivity {
    private ActivityGarageBinding binding;
    private static final String GARAGE_USER_ID = "com.stanissudo.jycs-crafters.GARAGE_USER_ID";
    private SharedPreferences sharedPreferences;
    FuelTrackAppRepository repository;
    private GarageViewModel garageViewModel;
    private VehicleViewModel vehicleViewModel;
    private SharedViewModel sharedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGarageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        repository =  FuelTrackAppRepository.getRepository(getApplication());
        // ViewModels
        garageViewModel = new ViewModelProvider(this).get(GarageViewModel.class);
        vehicleViewModel = new ViewModelProvider(this).get(VehicleViewModel.class);
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        // Load vehicles for this user.
        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);
        garageViewModel.loadUserVehicles(userId);
        vehicleViewModel.getUserVehicles().observe(this, vehicles -> {
            if (vehicles == null || vehicles.isEmpty()) {
                Toast.makeText(this, "No vehicles found for this account.", Toast.LENGTH_SHORT).show();
                return;
            }

            // (a) Update helper's cache.
            CarSelectorHelper.loadVehicleData(this, vehicles);
        });

        // RecyclerView
        binding.garageDisplayRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        GarageAdapter adapter = new GarageAdapter(new GarageAdapter.Callbacks() {
            @Override
            public void onDeleteClicked(long id) {
                new AlertDialog.Builder(GarageActivity.this)
                        .setMessage("Delete this vehicle?")
                        .setPositiveButton("Delete", (d, w) -> garageViewModel.deleteById(id))
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onEditClicked(long id) {
                startActivity(AddVehicleActivity.editVehicleIntentFactory(
                        GarageActivity.this,
                        CarSelectorHelper.getSelectedOptionKey()
                ));
            }
        });
        binding.garageDisplayRecyclerView.setAdapter(adapter);

        // Keep the list in sync with the backing data.
        garageViewModel.getUserVehicles().observe(this, adapter::submitList);

        // TODO: on clicking a row, select this vehicle
        //vm.selectVehicle(vehicle);

        // Add horizontal divider in between items
        MaterialDividerItemDecoration divider =
                new MaterialDividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        divider.setLastItemDecorated(false); // no divider after the last item
        divider.setDividerThicknessResource(this,R.dimen.list_divider_thickness);
        binding.garageDisplayRecyclerView.addItemDecoration(divider);
    }
    static Intent garageIntentFactory(Context context, int userId) {
        Intent intent = new Intent(context, GarageActivity.class);
        intent.putExtra(GARAGE_USER_ID, userId);
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
