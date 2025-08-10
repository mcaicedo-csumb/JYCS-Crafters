package com.stanissudo.jycs_crafters.viewHolders;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.database.entities.Vehicle;

import java.util.List;
public class VehicleViewModel extends AndroidViewModel {

    private final FuelTrackAppRepository repository;
    private LiveData<List<Vehicle>> userVehicles;

    // To hold the currently selected vehicle across the app if needed
    private final MutableLiveData<Vehicle> selectedVehicle = new MutableLiveData<>();

    public VehicleViewModel(@NonNull Application application) {
        super(application);
        repository = FuelTrackAppRepository.getRepository(application);
        // Initialize with an empty list to avoid nulls
        userVehicles = new MutableLiveData<>();
    }

    /**
     * Loads the vehicles for a specific user. Call this when the user logs in
     * or when the activity is created.
     * @param userId The ID of the current user.
     */
    public void loadUserVehicles(int userId) {
        userVehicles = repository.getVehiclesForUser(userId);
    }

    /**
     * Returns the LiveData list of vehicles. Activities can observe this.
     */
    public LiveData<List<Vehicle>> getUserVehicles() {
        return userVehicles;
    }

    // --- Optional: Methods to manage the selected state ---

    public void selectVehicle(Vehicle vehicle) {
        selectedVehicle.setValue(vehicle);
    }

    public LiveData<Vehicle> getSelectedVehicle() {
        return selectedVehicle;
    }
}
