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
import com.stanissudo.jycs_crafters.fragments.HomeFragment;
import com.stanissudo.jycs_crafters.fragments.SettingsFragment;
import com.stanissudo.jycs_crafters.utils.CarSelectorHelper;
import com.stanissudo.jycs_crafters.viewHolders.StatsPagerAdapter;
import com.stanissudo.jycs_crafters.viewHolders.VehicleViewModel;

public class MainActivity extends BaseDrawerActivity {

    private ActivityMainBinding binding;
    private FuelTrackAppRepository repository;
    private SharedPreferences sharedPreferences;
    public static final String TAG = "FuelTrackApp_Log";
    private static final String MAIN_ACTIVITY_USER_ID = "com.stanissudo.jycs_crafters.MAIN_ACTIVITY_USER_ID";

    private VehicleViewModel vehicleViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = FuelTrackAppRepository.getRepository(getApplication());

        // Get saved username from SharedPreferences
        sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User");
        int userId = sharedPreferences.getInt("userId", -1);

        // 1. Get the ViewModel
        vehicleViewModel = new ViewModelProvider(this).get(VehicleViewModel.class);


        // 3. Tell the ViewModel to load the vehicles for this user
        vehicleViewModel.loadUserVehicles(userId);

        // 4. Observe the data. When it arrives from the database, populate the helper.
        vehicleViewModel.getUserVehicles().observe(this, vehicles -> {
            if (vehicles != null) {
                // 1. First, load the data into the helper.
                CarSelectorHelper.loadVehicleData(this, vehicles);

                // 2. NOW that the helper has data, set up the dropdown.
                AutoCompleteTextView carSelectorDropdown = binding.toolbarDropdown;
                CarSelectorHelper.setupDropdown(this, carSelectorDropdown);
            }
        });

        // Display username in drawer header
        NavigationView navView = binding.navView;
        View headerView = navView.getHeaderView(0);
        TextView usernameText = headerView.findViewById(R.id.nav_header_username);
        usernameText.setText(username);


        // Back button handling
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

        // --- SETUP TABS AND VIEWPAGER for Stats fragments---

        // 1. Get references to the new views
        ViewPager2 viewPager = binding.viewPager;
        TabLayout tabLayout = binding.tabLayout;

        // 2. Create and set the adapter
        StatsPagerAdapter pagerAdapter = new StatsPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // 3. Link the TabLayout and the ViewPager
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 1) {
                        tab.setText("Distance");
                    } else {
                        tab.setText("Cost");
                    }
                }
        ).attach();

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

    /**
     * intentFactory for MainActivity
     * @param context context
     * @param userID int
     * @return intent to change activity
     */
    static Intent mainActivityIntentFactory(Context context, int userID) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MAIN_ACTIVITY_USER_ID, userID);
        return intent;
    }
}
