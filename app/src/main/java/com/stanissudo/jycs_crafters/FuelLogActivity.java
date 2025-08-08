package com.stanissudo.jycs_crafters;

import static com.stanissudo.jycs_crafters.MainActivity.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.databinding.ActivityFuelLogBinding;
import com.stanissudo.jycs_crafters.utils.CarSelectorHelper;
import com.stanissudo.jycs_crafters.viewHolders.FuelLogAdapter;
import com.stanissudo.jycs_crafters.viewHolders.FuelLogViewModel;

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


        RecyclerView rv = findViewById(R.id.fuelRecycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        FuelLogAdapter adapter = new FuelLogAdapter();
        rv.setAdapter(adapter);

        int selectedCarId = -1;
        try {
            selectedCarId = CarSelectorHelper.getSelectedOptionKey();
            if (selectedCarId == -1) {
                throw new IllegalArgumentException("Invalid selected car ID");
            }
        } catch (Exception e) {
            Log.d(TAG, "Error getting selected car ID", e);
            Toast.makeText(this, "Error creating a record. Did you select your Car?", Toast.LENGTH_SHORT).show();
            return;
        }

        FuelLogViewModel vm = new ViewModelProvider(this).get(FuelLogViewModel.class);
        vm.getEntries(selectedCarId).observe(this, adapter::submitList);
    }

    public static Intent intent(Context ctx, int userId) {
        return new Intent(ctx, FuelLogActivity.class).putExtra(FUEL_LOG_USER_ID, userId);
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
