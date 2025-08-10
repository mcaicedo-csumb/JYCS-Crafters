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

    /**
     * NEW METHOD: This populates the static map with real vehicle data.
     * Call this from your MainActivity or after login.
     * @param vehicles The list of vehicles from the database.
     */
    public static void loadVehicleData(Context context, List<Vehicle> vehicles) {
        fullOptions.clear(); // Clear old data
        if (vehicles != null && !vehicles.isEmpty()) {
            for (Vehicle vehicle : vehicles) {
                // Populate the map with the real ID and Name
                fullOptions.put(vehicle.getVehicleID(), vehicle.getName());
            }

            // Restore the last saved vehicle ID
            SharedPreferences prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
            int lastSelectedId = prefs.getInt("lastSelectedVehicleId", -1);
            String lastSelectedName = fullOptions.get(lastSelectedId);

            if (lastSelectedName != null) {
                selectedOption = lastSelectedName; // Restore saved selection
            } else {
                selectedOption = vehicles.get(0).getName(); // Default to first car
            }

        } else {
            selectedOption = "No Vehicle";
        }
    }

    public static void setupDropdown(Activity activity, AutoCompleteTextView dropdown) {

        dropdown.setText(selectedOption, false);
        //final boolean[] isDropdownOpen = {false}; // Track open/close state

        dropdown.setOnClickListener(v -> {
            String selected = dropdown.getText().toString();


            List<String> filteredOptions = new ArrayList<>();
            for (Map.Entry<Integer, String> entry : fullOptions.entrySet()) {
                if (!entry.getValue().equals(selected)) {
                    filteredOptions.add(entry.getValue());
                }
            }

            // Update adapter with filtered list
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    activity,
                    R.layout.car_selector_dropdown,
                    filteredOptions
            );
            dropdown.setAdapter(adapter);
            dropdown.showDropDown();

//            // Toggle dropdown manually
//            if (isDropdownOpen[0]) {
//                dropdown.dismissDropDown();
//            } else {
//                dropdown.showDropDown();
//            }
//
//            isDropdownOpen[0] = !isDropdownOpen[0];
//        });
//
//        // Reset dropdown state when user selects an item
//        dropdown.setOnItemClickListener((parent, view, position, id) -> {
//            String newSelected = parent.getItemAtPosition(position).toString();
//            setSelectedOption(activity, newSelected);
//            dropdown.setText(newSelected, false);
//            isDropdownOpen[0] = false;
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
        Integer key = getSelectedOptionKey();
        if (key != -1) {
            SharedPreferences prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
            prefs.edit().putInt("lastSelectedVehicleId", key).apply();
        }
    }
}
