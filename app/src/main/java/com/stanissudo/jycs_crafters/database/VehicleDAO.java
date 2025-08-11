package com.stanissudo.jycs_crafters.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.stanissudo.jycs_crafters.database.entities.Vehicle;

import java.util.List;

/**
 * @author Ysabelle Kim
 * created: 8/9/2025 - 11:56 PM
 * @project JYCS-Crafters
 * file: VehicleDAO.java
 * @since 1.0.0
 * Explanation: VehicleDAO
 */
@Dao
public interface VehicleDAO {
    // TODO: add rest of queries to VehicleDAO
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Vehicle... vehicle);
    @Delete
    void delete(Vehicle vehicle);
    @Query("DELETE FROM " + FuelTrackAppDatabase.VEHICLE_TABLE + " WHERE Name = :name")
    void deleteByVehicleName(String name);

    @Query("SELECT * FROM " + FuelTrackAppDatabase.VEHICLE_TABLE + " ORDER BY name ASC")
    LiveData<List<Vehicle>> getAllVehicles();

    @Query("SELECT * FROM " + FuelTrackAppDatabase.VEHICLE_TABLE + " WHERE UserId = :userId ORDER BY VehicleID")
    LiveData<List<Vehicle>> getVehiclesForUser(int userId);

    @Query("UPDATE " + FuelTrackAppDatabase.VEHICLE_TABLE + " SET Name = :newName WHERE VehicleID = :vehicleID")
    void updateVehicleName(int vehicleID, String newName);

    @Query("UPDATE " + FuelTrackAppDatabase.VEHICLE_TABLE + " SET Make = :newMake WHERE VehicleID = :vehicleID")
    void updateVehicleMake(int vehicleID, String newMake);

    @Query("UPDATE " + FuelTrackAppDatabase.VEHICLE_TABLE + " SET Model = :newModel WHERE VehicleID = :vehicleID")
    void updateVehicleModel(int vehicleID, String newModel);

    @Query("UPDATE " + FuelTrackAppDatabase.VEHICLE_TABLE + " SET Year = :newYear WHERE VehicleID = :vehicleID")
    void updateVehicleYear(int vehicleID, int newYear);
}
