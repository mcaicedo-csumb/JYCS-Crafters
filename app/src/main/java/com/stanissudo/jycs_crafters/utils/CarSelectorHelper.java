package com.stanissudo.jycs_crafters.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.stanissudo.jycs_crafters.R;
import com.stanissudo.jycs_crafters.database.entities.Vehicle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * * @author Stan Permiakov
 * * created: 8/12/2025
 * * @project JYCS-Crafters
 * * Utility class for managing the vehicle selection dropdown in the app.
 * * <p>
 * * This class holds a static map of all available vehicles (ID → Name) and
 * * provides methods to load them from the database, display them in an
 * * {@link AutoCompleteTextView}, and persist the last-selected vehicle
 * * using {@link SharedPreferences}.
 * * </p>
 * *
 * * <p>Workflow:</p>
 * * <ol>
 * *     <li>Call {@link #loadVehicleData(Context, List)} after login or when vehicle list is fetched.</li>
 * *     <li>Call {@link #setupDropdown(Activity, AutoCompleteTextView, OnVehicleSelected)}
 * *         to initialize the dropdown UI and selection handling.</li>
 * *     <li>Use {@link #getSelectedOptionKey()} or {@link #getSavedSelectedId(Context)}
 * *         to retrieve the currently selected vehicle's ID.</li>
 * * </ol>
 * *
 * * <p>The class maintains a static {@code selectedOption} string for the
 * * currently selected vehicle name and keeps it in sync with preferences.</p>
 * *
 */
public class CarSelectorHelper {

    /**
     * Static ordered map of vehicle IDs to their display names.
     * Maintains insertion order.
     */
    private static final Map<Integer, String> fullOptions = new LinkedHashMap<>();
    /**
     * The name of the currently selected vehicle.
     * Defaults to "No Vehicle" until set.
     */
    private static String selectedOption = "No Vehicle"; // Default value

    /**
     * Called when a vehicle is selected from the dropdown.
     * vehicleId the unique ID of the selected vehicle
     */
    public interface OnVehicleSelected {
        void onSelected(int vehicleId);
    }


    /**
     * Populates the static {@link #fullOptions} map with the provided list of vehicles.
     * <p>
     * This method should be called once after the list of vehicles is loaded
     * from the database. It also restores the last selected vehicle from
     * {@link SharedPreferences} if available, or defaults to the first
     * vehicle in the list.
     * </p>
     *
     * @param context  the {@link Context} used to access preferences
     * @param vehicles the list of {@link Vehicle} objects to populate the dropdown
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

    /**
     * Sets up the dropdown UI component with the currently loaded vehicle options.
     * <p>
     * The dropdown will initially display the last selected vehicle (or the default)
     * and show all other vehicles when clicked. When the user selects a vehicle,
     * the choice is saved to {@link SharedPreferences} and the provided
     * {@link OnVehicleSelected} callback is triggered with the selected ID.
     * </p>
     *
     * @param activity          the current {@link Activity} context
     * @param dropdown          the {@link AutoCompleteTextView} to set up
     * @param onVehicleSelected callback to invoke when a selection is made; can be {@code null}
     */
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

        // This listener saves the selection when the user chooses a car.
        dropdown.setOnItemClickListener((parent, view, position, id) -> {
            String newSelected = parent.getItemAtPosition(position).toString();

            // This line saves the choice persistently
            setSelectedOption(activity, newSelected);

            dropdown.setText(newSelected, false);

            // resolve ID here (from your fullOptions map) and emit it
            Integer selectedId = getSelectedOptionKey();
            if (selectedId != -1 && onVehicleSelected != null) {
                onVehicleSelected.onSelected(selectedId);
            }
        });
    }

    /**
     * Updates the dropdown text to the currently stored {@link #selectedOption}.
     * This is useful if the selection changes programmatically.
     *
     * @param dropdown the dropdown view to update
     */
    public static void updateDropdownText(AutoCompleteTextView dropdown) {
        dropdown.setText(selectedOption, false);
    }

    /**
     * Retrieves the ID of the currently selected vehicle option.
     *
     * @return the vehicle ID if found, or {@code -1} if no match
     */
    public static Integer getSelectedOptionKey() {
        for (Map.Entry<Integer, String> entry : fullOptions.entrySet()) {
            if (entry.getValue().equals(selectedOption)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    /**
     * Sets the selected vehicle option by its display name and saves the choice
     * persistently in {@link SharedPreferences}.
     *
     * @param context the context to access preferences
     * @param option  the display name of the selected vehicle
     */
    public static void setSelectedOption(Context context, String option) {
        selectedOption = option;
        Integer key = getSelectedOptionKey(); // Get the ID of the selected car

        if (key != -1) {
            // Save the car's ID to SharedPreferences so it persists
            SharedPreferences prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
            prefs.edit().putInt("lastSelectedVehicleId", key).apply();
        }
    }

    /**
     * Sets the selected vehicle option by its ID and saves it persistently.
     *
     * @param context the context to access preferences
     * @param key     the vehicle ID
     */
    public static void setSelectedOptionById(Context context, int key) {
        // Update in-memory label too
        String name = fullOptions.get(key);
        if (name != null) selectedOption = name;

        SharedPreferences prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        prefs.edit().putInt("lastSelectedVehicleId", key).apply();
    }

    /**
     * Reads the saved selected vehicle ID from {@link SharedPreferences}.
     *
     * @param context the context to access preferences
     * @return the saved vehicle ID, or {@code -1} if none saved
     */
    public static int getSavedSelectedId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        return prefs.getInt("lastSelectedVehicleId", -1);
    }

    /**
     * Synchronizes the in-memory {@link #selectedOption} string with the
     * value saved in {@link SharedPreferences}, if it exists in {@link #fullOptions}.
     *
     * @param context the context to access preferences
     */
    public static void syncFromPrefs(Context context) {
        int id = getSavedSelectedId(context);
        String name = fullOptions.get(id);
        if (name != null) selectedOption = name;
    }
}
