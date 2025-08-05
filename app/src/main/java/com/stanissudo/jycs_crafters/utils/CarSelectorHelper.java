package com.stanissudo.jycs_crafters.utils;

import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.stanissudo.jycs_crafters.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CarSelectorHelper {

    private static final List<String> fullOptions = new ArrayList<>(Arrays.asList("BMW", "Mercedes", "Porsche"));
    private static String selectedOption = fullOptions.get(0); // default value

    public static void setupDropdown(Activity activity, AutoCompleteTextView dropdown) {

        dropdown.setText(selectedOption, false);
        final boolean[] isDropdownOpen = {false}; // Track open/close state

        dropdown.setOnClickListener(v -> {
            String selected = dropdown.getText().toString();

            // Filter out the selected item
            List<String> filteredOptions = new ArrayList<>();
            for (String option : fullOptions) {
                if (!option.equals(selected)) {
                    filteredOptions.add(option);
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

    public static void setSelectedOption(String option) {
        selectedOption = option;
    }
}
