package com.stanissudo.jycs_crafters.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.stanissudo.jycs_crafters.database.entities.User;
import com.stanissudo.jycs_crafters.database.entities.Vehicle;

import java.util.List;

/** Camila: tiny repo for admin tools so Activities don't touch DAOs directly */
public class FuelTrackAdminRepository {

    private final UserDAO userDAO;
    private final VehicleDAO vehicleDAO;

    public FuelTrackAdminRepository(Application app) {
        FuelTrackAppDatabase db = FuelTrackAppDatabase.getDatabase(app);
        this.userDAO = db.userDAO();
        this.vehicleDAO = db.vehicleDAO();
    }

    // --- Users ---
    public LiveData<List<User>> getAllUsers() { return userDAO.getAllUsers(); }

    public void setUserActive(int userId, boolean active) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> userDAO.setActive(userId, active));
    }

    // --- Vehicles ---
    public LiveData<List<Vehicle>> getActiveVehicles() { return vehicleDAO.getActiveVehicles(); }

    public LiveData<List<Vehicle>> getInactiveVehicles() { return vehicleDAO.getInactiveVehicles(); }

    public void softDeleteVehicle(int id) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> vehicleDAO.softDelete(id));
    }

    public void restoreVehicle(int id) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> vehicleDAO.restore(id));
    }

    public void deleteVehicle(Vehicle v) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> vehicleDAO.delete(v));
    }
}
