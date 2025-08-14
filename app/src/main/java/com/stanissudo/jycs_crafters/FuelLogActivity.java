package com.stanissudo.jycs_crafters;

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

import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.android.material.navigation.NavigationView;
import com.stanissudo.jycs_crafters.databinding.ActivityFuelLogBinding;
import com.stanissudo.jycs_crafters.utils.CarSelectorHelper;
import com.stanissudo.jycs_crafters.viewHolders.FuelLogAdapter;
import com.stanissudo.jycs_crafters.viewHolders.FuelLogViewModel;
import com.stanissudo.jycs_crafters.viewHolders.SharedViewModel;
import com.stanissudo.jycs_crafters.viewHolders.VehicleViewModel;

/**
 * *  @author Stan Permiakov
 * *  created: 8/12/2025
 * *  @project JYCS-Crafters
 * *
 * Displays a list of fuel log entries for the currently selected vehicle.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Hosts the navigation drawer provided by {@link BaseDrawerActivity}.</li>
 *   <li>Loads the user's vehicles and wires the toolbar dropdown using {@link CarSelectorHelper}.</li>
 *   <li>Observes {@link FuelLogViewModel#entries} and renders them via {@link FuelLogAdapter}.</li>
 *   <li>Supports inline delete and edit actions on each list item.</li>
 * </ul>
 *
 * <h3>Lifecycle & Base class expectations</h3>
 * This Activity calls {@link #setContentView(int)} before {@link #onCreate(Bundle)} in the base
 * class because {@link BaseDrawerActivity} queries {@link #getDrawerLayout()},
 * {@link #getNavigationView()}, and {@link #getToolbar()} during its own {@code onCreate}.
 * Therefore, the view hierarchy must already be inflated.
 */
public class FuelLogActivity extends BaseDrawerActivity {

    /**
     * ViewBinding for this Activity's layout.
     */
    private ActivityFuelLogBinding binding;

    /**
     * Provides the paged/filtered list of entries for the selected car.
     */
    private FuelLogViewModel viewModel;
    /**
     * Supplies the current user's vehicles for the toolbar dropdown.
     */
    private VehicleViewModel vehicleViewModel;
    /**
     * Shared car selection model (kept in sync with the dropdown).
     */
    private SharedViewModel sharedViewModel;

    // --------------------------------------------------------------------------------------------
    // Lifecycle
    // --------------------------------------------------------------------------------------------

    /**
     * Wires the drawer UI, dropdown population/selection, RecyclerView, and LiveData observers.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Inflate layout and attach early so BaseDrawerActivity can find drawer views.
        binding = ActivityFuelLogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Now let BaseDrawerActivity do its own setup (menu, toolbar, etc.).
        super.onCreate(savedInstanceState);

        // ViewModels
        viewModel = new ViewModelProvider(this).get(FuelLogViewModel.class);
        vehicleViewModel = new ViewModelProvider(this).get(VehicleViewModel.class);
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        // Load vehicles for this user and configure the dropdown when data arrives.
        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);
        vehicleViewModel.loadUserVehicles(userId);
        vehicleViewModel.getUserVehicles().observe(this, vehicles -> {
            if (vehicles == null || vehicles.isEmpty()) {
                Toast.makeText(this, "No vehicles found for this account.", Toast.LENGTH_SHORT).show();
                return;
            }

            // (a) Update helper's cache and wire the dropdown adapter.
            CarSelectorHelper.loadVehicleData(this, vehicles);
            AutoCompleteTextView dropdown = binding.toolbarDropdown;
            //CarSelectorHelper.setupDropdown(this, dropdown);
            CarSelectorHelper.setupDropdown(this, dropdown, id -> {
                if (id != -1) sharedViewModel.selectCar(id); // emits to fragments
            });

            // (b) Initial selection → update shared model and list filter.
            Integer initialCarId = CarSelectorHelper.getSelectedOptionKey();
            if (initialCarId != null && initialCarId > 0) {
                sharedViewModel.selectCar(initialCarId);
                viewModel.setSelectedCarId(initialCarId);
            } else {
                Toast.makeText(this, "Select a car to load entries", Toast.LENGTH_SHORT).show();
            }

            // (c) Subsequent user selection changes.
            dropdown.setOnItemClickListener((parent, view, position, id) -> {
                String name = (String) parent.getItemAtPosition(position);
                CarSelectorHelper.setSelectedOption(this, name);
                Integer newCarId = CarSelectorHelper.getSelectedOptionKey();
                if (newCarId == null || newCarId <= 0) {
                    Toast.makeText(this, "Invalid car selection", Toast.LENGTH_SHORT).show();
                    return;
                }
                sharedViewModel.selectCar(newCarId);
                viewModel.setSelectedCarId(newCarId);
            });
        });

        // RecyclerView setup
        binding.logDisplayRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        FuelLogAdapter adapter = new FuelLogAdapter(new FuelLogAdapter.Callbacks() {
            @Override
            public void onDeleteClicked(long id) {
                new AlertDialog.Builder(FuelLogActivity.this)
                        .setMessage("Delete this entry?")
                        .setPositiveButton("Delete", (d, w) -> viewModel.deleteById(id))
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onEditClicked(long id) {
                startActivity(AddFuelEntryActivity.editIntentFactory(
                        FuelLogActivity.this,
                        CarSelectorHelper.getSelectedOptionKey(),
                        (int) id
                ));
            }
        });
        binding.logDisplayRecyclerView.setAdapter(adapter);

        // Keep the list in sync with the backing data.
        viewModel.entries.observe(this, adapter::submitList);

        // Add horizontal divider in between items
        MaterialDividerItemDecoration divider =
                new MaterialDividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        divider.setLastItemDecorated(false); // no divider after the last item
        divider.setDividerThicknessResource(this,R.dimen.list_divider_thickness);
        binding.logDisplayRecyclerView.addItemDecoration(divider);
    }


    /**
     * Ensure the toolbar dropdown text reflects the persisted selection when returning to the UI.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // 1) Update in-memory selectedOption from prefs
        CarSelectorHelper.syncFromPrefs(this);

        // 2) Update the dropdown text to match
        CarSelectorHelper.updateDropdownText(binding.toolbarDropdown);

        // 3) Emit to fragments if changed (or on first resume)
        int savedId = CarSelectorHelper.getSavedSelectedId(this);
        if (savedId != -1) {
            Integer current = sharedViewModel.getSelectedCarId().getValue();
            if (current == null || !current.equals(savedId)) {
                sharedViewModel.selectCar(savedId);
            }
        }
    }

    // --------------------------------------------------------------------------------------------
    // Intent factory
    // --------------------------------------------------------------------------------------------

    /**
     * Build an {@link Intent} that opens the Fuel Log screen.
     *
     * @param context Caller context
     * @return Intent ready for {@link Context#startActivity(Intent)}
     */
    public static Intent fuelLogIntentFactory(Context context) {
        return new Intent(context, FuelLogActivity.class);
    }

    // --------------------------------------------------------------------------------------------
    // Drawer wiring for BaseDrawerActivity
    // --------------------------------------------------------------------------------------------

    /**
     * Provides the {@link DrawerLayout} instance used by the base class to wire the drawer.
     */
    @Override
    protected DrawerLayout getDrawerLayout() {
        return binding.drawerLayout;
    }

    /**
     * Provides the {@link NavigationView} instance used by the base class to populate the menu.
     */
    @Override
    protected NavigationView getNavigationView() {
        return binding.navView;
    }

    /**
     * Provides the top {@link Toolbar} so the base class can set it up as the SupportActionBar.
     */
    @Override
    protected Toolbar getToolbar() {
        return binding.toolbar;
    }
}
