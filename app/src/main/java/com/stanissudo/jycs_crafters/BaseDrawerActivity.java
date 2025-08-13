package com.stanissudo.jycs_crafters;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.stanissudo.jycs_crafters.utils.CarSelectorHelper;

/**
 * *  @author Stan Permiakov
 * *  created: 8/12/2025
 * *  @project JYCS-Crafters
 * *
 * Base Activity that wires a Material navigation drawer and shared behaviors
 * (toolbar setup, drawer toggle, and a common logout flow). Subclasses must
 * provide the {@link DrawerLayout}, {@link NavigationView}, and {@link Toolbar}
 * instances from their inflated layouts.
 * <p>
 * <b>Key responsibilities</b>
 * <ul>
 *   <li>Install the toolbar as the SupportActionBar and connect it to the drawer toggle.</li>
 *   <li>Provide a shared, app-wide <em>Logout</em> action that signs out from Firebase and Google,
 *       clears local login state, and navigates to {@link LoginActivity}.</li>
 *   <li>Handle common navigation destinations from the drawer (Home, Fuel Entry, Fuel Log, Garage, Settings).</li>
 * </ul>
 * <p>
 * <b>Subclass contract</b>
 * <ul>
 *   <li>Inflate your view hierarchy (containing the drawer, nav view, and toolbar) before
 *       {@code super.onPostCreate(...)} is invoked so that these accessors return non-null views.</li>
 *   <li>Return the same instances consistently from {@link #getDrawerLayout()},
 *       {@link #getNavigationView()}, and {@link #getToolbar()}.</li>
 * </ul>
 */
public abstract class BaseDrawerActivity extends AppCompatActivity {

    /**
     * @return The {@link DrawerLayout} that hosts the navigation drawer for this screen.
     */
    protected abstract DrawerLayout getDrawerLayout();

    /**
     * @return The {@link NavigationView} used to display drawer menu items.
     */
    protected abstract NavigationView getNavigationView();

    /**
     * @return The top {@link Toolbar} for this screen (installed as the SupportActionBar).
     */
    protected abstract Toolbar getToolbar();


    /**
     * Shared logout routine for all drawer-based screens.
     * <p>
     * Performs:
     * <ol>
     *   <li>Firebase sign-out (covers email/password and Google-linked sessions).</li>
     *   <li>Google Sign-In sign-out using a default {@link GoogleSignInOptions} client.</li>
     *   <li>Clears local login state in {@code SharedPreferences}.</li>
     *   <li>Starts {@link LoginActivity} with flags to clear the task/back stack and finishes the current Activity.</li>
     * </ol>
     */
    protected void logout() {
        // Firebase sign out (covers email/pw + Google-linked Firebase session)
        FirebaseAuth.getInstance().signOut();

        // Google sign out
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(
                this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        );

        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);

        googleSignInClient.signOut().addOnCompleteListener(task -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isLoggedIn", false);
            editor.remove("username");
            editor.remove("isAdmin");
            editor.apply();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Lifecycle hook executed after {@link #onCreate(Bundle)}. Installs the toolbar as the
     * SupportActionBar, wires the drawer toggle, and attaches a menu item selection listener that
     * handles common navigation (including a global <em>Logout</em> action).
     * <p>
     * <b>Note:</b> Subclasses should ensure their view hierarchy (containing the drawer,
     * navigation view, and toolbar) is already inflated before this method runs.
     *
     * @param savedInstanceState State bundle from the framework (may be {@code null}).
     */
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                this,
                getDrawerLayout(),
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        getDrawerLayout().addDrawerListener(toggle);
        toggle.syncState();

        getNavigationView().setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_logout) {
                // works from ANY activity now
                logout();
                return true;
            }

            if (id == R.id.nav_home) {
                if (!(this instanceof MainActivity)) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
            } else if (id == R.id.nav_fuel_entry) {
                Intent intent = AddFuelEntryActivity.addFuelIntentFactory(getApplicationContext(), CarSelectorHelper.getSelectedOptionKey());
                startActivity(intent);
                // }
            } else if (id == R.id.nav_fuel_log) {
                if (!(this instanceof FuelLogActivity)) {
                    Intent intent = FuelLogActivity.fuelLogIntentFactory(getApplicationContext());
                    startActivity(intent);
                }
            } else if (id == R.id.nav_garage) {
                //if (!(this instanceof GarageActivity)) {
                Intent intent = GarageActivity.garageIntentFactory(getApplicationContext(), -1);
                //Intent intent = AddVehicleActivity.vehicleIntentFactory(getApplicationContext(), -1);
                startActivity(intent);
                // }
            }
            // ✅ ADDED: open Settings from the drawer
            else if (id == R.id.nav_settings) {
                startActivity(SettingsActivity.intentFactory(this)); // or: new Intent(this, SettingsActivity.class)
            }

            getDrawerLayout().closeDrawer(GravityCompat.START);
            return true;
        });
    }
}
