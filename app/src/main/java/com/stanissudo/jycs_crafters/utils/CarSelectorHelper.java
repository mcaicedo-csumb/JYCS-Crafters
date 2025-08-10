package com.stanissudo.jycs_crafters.utils;

import android.app.Activity;
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

//    private static final Map<Integer, String> fullOptions = new LinkedHashMap<>();
//
//    static {
//        fullOptions.put(1, "BMW");
//        fullOptions.put(2, "Mercedes");
//        fullOptions.put(3, "Porsche");
//    }
//    private static String selectedOption = fullOptions.entrySet().iterator().next().getValue();

    private static final Map<Integer, String> fullOptions = new LinkedHashMap<>();
    private static String selectedOption = "No Vehicle"; // Default value

    /**
     * NEW METHOD: This populates the static map with real vehicle data.
     * Call this from your MainActivity or after login.
     * @param vehicles The list of vehicles from the database.
     */
    public static void loadVehicleData(List<Vehicle> vehicles) {
        fullOptions.clear(); // Clear old data
        if (vehicles != null && !vehicles.isEmpty()) {
            for (Vehicle vehicle : vehicles) {
                // Populate the map with the real ID and Name
                fullOptions.put(vehicle.getVehicleID(), vehicle.getName());
            }
            // Set the default selected option to the first car
            selectedOption = vehicles.get(0).getName();
        } else {
            // Handle case where user has no vehicles
            selectedOption = "No Vehicle";
        }
    }

    public static void setupDropdown(Activity activity, AutoCompleteTextView dropdown) {

        dropdown.setText(selectedOption, false);
        final boolean[] isDropdownOpen = {false}; // Track open/close state

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

            // Toggle dropdown manually
            if (isDropdownOpen[0]) {
                dropdown.dismissDropDown();
            } else {
                dropdown.showDropDown();
            }

            isDropdownOpen[0] = !isDropdownOpen[0];
        });

        // Reset dropdown state when user selects an item
        dropdown.setOnItemClickListener((parent, view, position, id) -> {
            String newSelected = parent.getItemAtPosition(position).toString();
            setSelectedOption(newSelected);
            dropdown.setText(newSelected, false);
            isDropdownOpen[0] = false;
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

    public static void setSelectedOption(String option) {
        selectedOption = option;
    }
}
