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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.database.entities.FuelEntry;
import com.stanissudo.jycs_crafters.databinding.ActivityFuelLogBinding;
import com.stanissudo.jycs_crafters.utils.CarSelectorHelper;
import com.stanissudo.jycs_crafters.viewHolders.FuelLogAdapter;
import com.stanissudo.jycs_crafters.viewHolders.FuelLogViewModel;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class FuelLogActivity extends BaseDrawerActivity {
    private ActivityFuelLogBinding binding;
    private static final String FUEL_LOG_USER_ID = "com.stanissudo.gymlog.FUEL_LOG_USER_ID";
    FuelTrackAppRepository repository = FuelTrackAppRepository.getRepository(getApplication());

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityFuelLogBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    // RecyclerView
    FuelLogAdapter adapter = new FuelLogAdapter();
    binding.logDisplayRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    binding.logDisplayRecyclerView.setAdapter(adapter);

    // ViewModel
    FuelLogViewModel vm = new ViewModelProvider(this).get(FuelLogViewModel.class);

    // Observe ONCE
    vm.entries.observe(this, adapter::submitList);

    // Dropdown
    AutoCompleteTextView drop = binding.toolbarDropdown;
    CarSelectorHelper.setupDropdown(this, drop);

    // Initial car
    Integer initialCarId = CarSelectorHelper.getSelectedOptionKey();
    if (initialCarId != null && initialCarId > 0) {
        vm.setSelectedCarId(initialCarId);
    } else {
        Toast.makeText(this, "Select a car to load entries", Toast.LENGTH_SHORT).show();
    }

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
