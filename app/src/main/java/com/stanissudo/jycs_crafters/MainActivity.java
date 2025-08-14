package com.stanissudo.jycs_crafters;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.AutoCompleteTextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.databinding.ActivityMainBinding;
import com.stanissudo.jycs_crafters.utils.CarSelectorHelper;
import com.stanissudo.jycs_crafters.viewHolders.SharedViewModel;
import com.stanissudo.jycs_crafters.viewHolders.StatsPagerAdapter;
import com.stanissudo.jycs_crafters.viewHolders.GarageViewModel;

/**
 * @author Stan Permiakov, Ysabelle Kim
 * created: 7/31/2025
 * @version 1.0.0
 * Explanation: <p>MainActivity shows the user a summarized view of a selected vehicle.
 * There are two fragments that show the vehicle's fuel costs and distance.</p>
 * @project JYCS-Crafters
 * @name MainActivity.java
 */
public class MainActivity extends BaseDrawerActivity {

    private ActivityMainBinding binding;
    public static final String TAG = "FuelTrackApp_Log";
    private static final String MAIN_ACTIVITY_USER_ID = "com.stanissudo.jycs_crafters.MAIN_ACTIVITY_USER_ID";
    private SharedViewModel sharedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- 1. Initialize ViewModels ---
        GarageViewModel garageViewModel = new ViewModelProvider(this).get(GarageViewModel.class);
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        // --- 2. Standard Setup (Repository, SharedPreferences, etc.) ---
        FuelTrackAppRepository repository = FuelTrackAppRepository.getRepository(getApplication());
        SharedPreferences sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User");
        int userId = sharedPreferences.getInt("userId", -1);

        // --- 3. Setup UI Components (ViewPager, Tabs, Navigation) ---
        ViewPager2 viewPager = binding.viewPager;
        TabLayout tabLayout = binding.tabLayout;
        StatsPagerAdapter pagerAdapter = new StatsPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Cost" : "Distance");
        }).attach();

        // Setup for Navigation Drawer Header
        NavigationView navView = binding.navView;
        View headerView = navView.getHeaderView(0);
        TextView usernameText = headerView.findViewById(R.id.nav_header_username);
        usernameText.setText(username);

        // --- 4. Load Data and Link to UI ---
        // Observe the list of vehicles from the database
        garageViewModel.loadUserVehicles(userId);
        garageViewModel.getUserVehicles().observe(this, vehicles -> {
            if (vehicles != null && !vehicles.isEmpty()) {
                // a. Load vehicle data into the helper, which sets the default selection
                CarSelectorHelper.loadVehicleData(this, vehicles);

                // b. Setup the dropdown UI with the loaded data
                AutoCompleteTextView carSelectorDropdown = binding.toolbarDropdown;
                //CarSelectorHelper.setupDropdown(this, carSelectorDropdown);
                CarSelectorHelper.setupDropdown(this, carSelectorDropdown, id -> {
                    if (id != -1) sharedViewModel.selectCar(id); // emits to fragments
                });

                // c. **CRITICAL FIX:** Get the initial car ID and push it to the SharedViewModel.
                //    This immediately informs all listening fragments which car to display.
                Integer initialCarId = CarSelectorHelper.getSelectedOptionKey();
                if (initialCarId != -1) {
                    sharedViewModel.selectCar(initialCarId);
                }
            }
        });

        // --- 5. Set Listeners for User Interactions ---
        // Listener for future dropdown selections
        binding.toolbarDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String newSelected = parent.getItemAtPosition(position).toString();
            CarSelectorHelper.setSelectedOption(this, newSelected);
            binding.toolbarDropdown.setText(newSelected, false);

            // Get the new car ID and notify the SharedViewModel of the change
            Integer selectedId = CarSelectorHelper.getSelectedOptionKey();
            if (selectedId != -1) {
                sharedViewModel.selectCar(selectedId);
            }
        });

        // Listener for Navigation Drawer item clicks
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                logout();
                return true;
            }
            // Handle other navigation items if you have them
            return false;
        });

        // Listener for the hardware back button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
            }
        });
    }

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

    /**
     * intentFactory for MainActivity
     *
     * @param context context
     * @param userID  int
     * @return intent to change activity
     */
    static Intent mainActivityIntentFactory(Context context, int userID) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(MAIN_ACTIVITY_USER_ID, userID);
        return intent;
    }
}
