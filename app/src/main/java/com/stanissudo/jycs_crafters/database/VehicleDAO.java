package com.stanissudo.jycs_crafters.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.stanissudo.jycs_crafters.database.entities.Vehicle;

import java.util.List;

/** Camila: admin DAO for vehicles (lists + soft-delete/restore) */
@Dao
public interface VehicleDAO {

    // Camila: seed/insert helper for tests or setup
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Vehicle vehicle);

    // Camila: admin lists
    @Query("SELECT * FROM vehicles WHERE isActive = 1 ORDER BY id DESC")
    LiveData<List<Vehicle>> getActiveVehicles();

    @Query("SELECT * FROM vehicles WHERE isActive = 0 ORDER BY id DESC")
    LiveData<List<Vehicle>> getInactiveVehicles();

    // Camila: soft delete / restore actions
    @Query("UPDATE vehicles SET isActive = 0 WHERE id = :vehicleId")
    void softDelete(int vehicleId);

    @Query("UPDATE vehicles SET isActive = 1 WHERE id = :vehicleId")
    void restore(int vehicleId);

    // Camila: hard delete (admin-only)
    @Delete
    void delete(Vehicle vehicle);
}
