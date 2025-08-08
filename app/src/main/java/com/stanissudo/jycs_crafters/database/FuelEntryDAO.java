package com.stanissudo.jycs_crafters.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.stanissudo.jycs_crafters.database.entities.FuelEntry;

import java.util.List;

@Dao
public interface FuelEntryDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FuelEntry fuelEntry);

    @Query("SELECT * FROM " + FuelTrackAppDatabase.FUEL_LOG_TABLE + " WHERE CarID = :carId ORDER BY logDate DESC")
    LiveData<List<FuelEntry>> getEntriesForCar(int carId);

    @Query("SELECT * FROM " + FuelTrackAppDatabase.FUEL_LOG_TABLE + " WHERE  LogID = :logId ORDER BY logDate DESC")
    LiveData<List<FuelEntry>> getRecordsById(int logId);
}
