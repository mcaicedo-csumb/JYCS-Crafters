package com.stanissudo.jycs_crafters;

import static com.stanissudo.jycs_crafters.AddFuelEntryActivity.addFuelEntryIntentFactory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

// NEW imports for logout:
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public abstract class BaseDrawerActivity extends AppCompatActivity {

    protected abstract DrawerLayout getDrawerLayout();

    protected abstract NavigationView getNavigationView();

    protected abstract Toolbar getToolbar();


    // Shared logout for ALL drawer screens
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
                //if (!(this instanceof AddFuelEntryActivity)) {
                Intent intent = AddFuelEntryActivity.addFuelEntryIntentFactory(getApplicationContext(), -1);
                startActivity(intent);
                // }
            } else if (id == R.id.nav_fuel_log) {
                if (!(this instanceof FuelLogActivity)) {
                    Intent intent = FuelLogActivity.fuelLogIntentFactory(getApplicationContext(), -1);
                    startActivity(intent);
                }
            } else if (id == R.id.nav_garage) {
                //if (!(this instanceof GarageActivity)) {
                //Intent intent = GarageActivity.garageIntentFactory(getApplicationContext(), -1);
                Intent intent = VehicleActivity.vehicleIntentFactory(getApplicationContext(), -1);
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
