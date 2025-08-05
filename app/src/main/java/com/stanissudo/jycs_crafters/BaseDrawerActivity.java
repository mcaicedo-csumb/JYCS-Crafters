package com.stanissudo.jycs_crafters;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public abstract class BaseDrawerActivity extends AppCompatActivity {

    protected abstract DrawerLayout getDrawerLayout();

    protected abstract NavigationView getNavigationView();

    protected abstract Toolbar getToolbar();

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

            if (id == R.id.nav_home) {
                if (!(this instanceof MainActivity)) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
            } else if (id == R.id.nav_fuel_entry) {
                if (!(this instanceof AddFuelEntryActivity)) {
                    Intent intent = new Intent(this, AddFuelEntryActivity.class);
                    startActivity(intent);
                }
            } else if (id == R.id.nav_garage) {
                if (!(this instanceof AddFuelEntryActivity)) {
                    Intent intent = new Intent(this, VehicleActivity.class);
                    startActivity(intent);
                }
            } else if (id == R.id.nav_settings && this instanceof MainActivity) {
                ((MainActivity) this).showSettingsFragment();
            }

            getDrawerLayout().closeDrawer(GravityCompat.START);
            return true;
        });
    }
}
