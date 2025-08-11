package com.stanissudo.jycs_crafters;

import android.content.Context;
import android.content.Intent;
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
import com.stanissudo.jycs_crafters.databinding.ActivityVehicleGarageBinding;
import com.stanissudo.jycs_crafters.utils.CarSelectorHelper;
import com.stanissudo.jycs_crafters.viewHolders.FuelLogAdapter;
import com.stanissudo.jycs_crafters.viewHolders.FuelLogViewModel;
import com.stanissudo.jycs_crafters.viewHolders.VehicleAdapter;
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
    private ActivityVehicleGarageBinding binding;
    private static final String GARAGE_USER_ID = "com.stanissudo.jycs-crafters.GARAGE_USER_ID";
    FuelTrackAppRepository repository = FuelTrackAppRepository.getRepository(getApplication());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVehicleGarageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // RecyclerView
        VehicleAdapter adapter = new VehicleAdapter();
        binding.garageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.garageRecyclerView.setAdapter(adapter);

        // ViewModel
        VehicleViewModel vm = new ViewModelProvider(this).get(VehicleViewModel.class);

        // Observe ONCE
        vm.getUserVehicles().observe(this, adapter::submitList);

        // Dropdown
        AutoCompleteTextView drop = binding.toolbarDropdown;
        CarSelectorHelper.setupDropdown(this, drop);

        // Initial car
        vm.loadUserVehicles(Integer.parseInt(GARAGE_USER_ID));

        // On change
        drop.setOnItemClickListener((parent, view, position, id) -> {
            String name = (String) parent.getItemAtPosition(position);
            CarSelectorHelper.setSelectedOption(this, name);
            Integer newCarId = CarSelectorHelper.getSelectedOptionKey();
            if (newCarId == null || newCarId <= 0) {
                Toast.makeText(this, "Invalid car selection", Toast.LENGTH_SHORT).show();
                return;
            }
            vm.setSelectedCarId(newCarId);
        });
    }
    static Intent fuelLogIntentFactory(Context context, int userId) {
        Intent intent = new Intent(context, FuelLogActivity.class);
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
