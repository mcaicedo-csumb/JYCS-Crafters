package com.stanissudo.jycs_crafters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.navigation.NavigationView;
import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.databinding.ActivityGarageBinding;
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
    private ActivityGarageBinding binding;
    private static final String GARAGE_USER_ID = "com.stanissudo.jycs-crafters.GARAGE_USER_ID";
    private SharedPreferences sharedPreferences;
    FuelTrackAppRepository repository = FuelTrackAppRepository.getRepository(getApplication());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGarageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // RecyclerView
        VehicleAdapter adapter = new VehicleAdapter();
        binding.garageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.garageRecyclerView.setAdapter(adapter);

        // ViewModel
        VehicleViewModel vm = new ViewModelProvider(this).get(VehicleViewModel.class);

        // Observe ONCE
        vm.getUserVehicles().observe(this, adapter::submitList);

        sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User");
        int userId = sharedPreferences.getInt("userId", -1);

        // get list of vehicles
        vm.loadUserVehicles(userId);

        // TODO: on clicking a row, select this vehicle
        //vm.selectVehicle(vehicle);

        // click (+) to send to VehicleActivity
        binding.vehicleActivityAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = VehicleActivity.vehicleIntentFactory(getApplicationContext(), userId);
                startActivity(intent);
            }
        });
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
