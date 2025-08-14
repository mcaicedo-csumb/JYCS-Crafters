package com.stanissudo.jycs_crafters.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.stanissudo.jycs_crafters.R;
import com.stanissudo.jycs_crafters.database.entities.Vehicle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CarSelectorHelper {

    private static final Map<Integer, String> fullOptions = new LinkedHashMap<>();
    private static String selectedOption = "No Vehicle"; // Default value
    public interface OnVehicleSelected { void onSelected(int vehicleId); }


    /**
     * NEW METHOD: This populates the static map with real vehicle data.
     * Call this from your MainActivity or after login.
     * @param vehicles The list of vehicles from the database.
     */
    public static void loadVehicleData(Context context, List<Vehicle> vehicles) {
        fullOptions.clear(); // Clear old data first

        if (vehicles != null && !vehicles.isEmpty()) {
            for (Vehicle vehicle : vehicles) {
                fullOptions.put(vehicle.getVehicleID(), vehicle.getName());
            }

            // Restore the last saved vehicle ID from device storage
            SharedPreferences prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
            int lastSelectedId = prefs.getInt("lastSelectedVehicleId", -1);
            String lastSelectedName = fullOptions.get(lastSelectedId); // Find car name by its ID

            if (lastSelectedName != null) {
                // If a valid last-selected car was found, use it
                selectedOption = lastSelectedName;
            } else {
                // Otherwise, default to the first car in the list
                selectedOption = vehicles.get(0).getName();
            }

        } else {
            selectedOption = "No Vehicle"; // Handle case where user has no vehicles
        }
    }

    public static void setupDropdown(Activity activity, AutoCompleteTextView dropdown, OnVehicleSelected onVehicleSelected) {

        dropdown.setText(selectedOption, false);

        dropdown.setOnClickListener(v -> {
            String selected = dropdown.getText().toString();

            List<String> filteredOptions = new ArrayList<>();
            for (Map.Entry<Integer, String> entry : fullOptions.entrySet()) {
                if (!entry.getValue().equals(selected)) {
                    filteredOptions.add(entry.getValue());
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    activity,
                    R.layout.car_selector_dropdown,
                    filteredOptions
            );
            dropdown.setAdapter(adapter);
            dropdown.showDropDown();
        });

        // VVV THIS IS THE PART YOU MUST UNCOMMENT VVV
        // This listener saves the selection when the user chooses a car.
        dropdown.setOnItemClickListener((parent, view, position, id) -> {
            String newSelected = parent.getItemAtPosition(position).toString();

            // This line saves the choice persistently
            setSelectedOption(activity, newSelected);

            dropdown.setText(newSelected, false);

            // 🔑 resolve ID here (from your fullOptions map) and emit it
            Integer selectedId = getSelectedOptionKey();
            if (selectedId != -1 && onVehicleSelected != null) {
                onVehicleSelected.onSelected(selectedId);
            }
        });
    }
    public static void updateDropdownText(AutoCompleteTextView dropdown) {
        dropdown.setText(selectedOption, false);
    }
    public static String getSelectedOption() {
        return selectedOption;
    }

    public static Integer getSelectedOptionKey() {
        for (Map.Entry<Integer, String> entry : fullOptions.entrySet()) {
            if (entry.getValue().equals(selectedOption)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public static void setSelectedOption(Context context, String option) {
        selectedOption = option;
        Integer key = getSelectedOptionKey(); // Get the ID of the selected car

        if (key != -1) {
            // Save the car's ID to SharedPreferences so it persists
            SharedPreferences prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
            prefs.edit().putInt("lastSelectedVehicleId", key).apply();
        }
    }

    public static void setSelectedOptionById(Context context, int key) {

        if (key != -1) {
            // Save the car's ID to SharedPreferences so it persists
            SharedPreferences prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
            prefs.edit().putInt("lastSelectedVehicleId", key).apply();
        }
    }

}
