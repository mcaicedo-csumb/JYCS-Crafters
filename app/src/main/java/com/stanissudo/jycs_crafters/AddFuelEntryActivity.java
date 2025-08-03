package com.stanissudo.jycs_crafters;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.google.android.material.navigation.NavigationView;
import com.stanissudo.jycs_crafters.databinding.ActivityAddFuelEntryBinding;

public class AddFuelEntryActivity extends BaseDrawerActivity {

    private ActivityAddFuelEntryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddFuelEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        Calendar calendar = Calendar.getInstance();
        // Format date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);

        binding.editTextDateFuelEntry.setText(dateFormat.format(calendar.getTime()));
        binding.editTextTimeFuelEntry.setText(timeFormat.format(calendar.getTime()));

        binding.editTextDateFuelEntry.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, y, m, d) -> {
                        // Months are 0-based in Calendar
                        calendar.set(Calendar.YEAR, y);
                        calendar.set(Calendar.MONTH, m);
                        calendar.set(Calendar.DAY_OF_MONTH, d);
                        binding.editTextDateFuelEntry.setText(dateFormat.format(calendar.getTime()));
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        binding.editTextTimeFuelEntry.setOnClickListener(v -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, h, m) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, h);
                        calendar.set(Calendar.MINUTE, m);
                        binding.editTextTimeFuelEntry.setText(timeFormat.format(calendar.getTime()));
                    },
                    hour, minute, true // true = 24-hour
            );
            timePickerDialog.show();
        });
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
