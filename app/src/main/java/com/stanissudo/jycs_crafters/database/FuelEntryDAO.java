package com.stanissudo.jycs_crafters.database;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.room.Query;

import com.stanissudo.jycs_crafters.database.entities.FuelEntry;

import java.time.LocalDateTime;
import java.util.List;

import com.stanissudo.jycs_crafters.database.pojos.CarCostStats;

@Dao
public interface FuelEntryDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FuelEntry fuelEntry);

    @Query("SELECT * FROM " + FuelTrackAppDatabase.FUEL_LOG_TABLE + " WHERE CarID = :carId ORDER BY logDate DESC")
    LiveData<List<FuelEntry>> getEntriesForCar(int carId);

    @Query("SELECT * FROM " + FuelTrackAppDatabase.FUEL_LOG_TABLE + " WHERE  LogID = :logId ORDER BY logDate DESC")
    LiveData<List<FuelEntry>> getRecordsById(int logId);

    @Query("SELECT odometer FROM " + FuelTrackAppDatabase.FUEL_LOG_TABLE + " WHERE  CarID = :carId AND logDate < :logDate ORDER BY logDate DESC LIMIT 1")
    Integer getPreviousOdometer(int carId, LocalDateTime logDate);
    @Query("SELECT odometer FROM " + FuelTrackAppDatabase.FUEL_LOG_TABLE + " WHERE  CarID = :carId AND logDate > :logDate ORDER BY logDate LIMIT 1")
    Integer getNextOdometer(int carId, LocalDateTime logDate);

    @Query("SELECT " +
            "SUM(TotalCost) as totalCost, " +
            "AVG(PricePerGallon) as avgPricePerGallon, " +
            "AVG(TotalCost) as avgCostPerFillUp " +
            "FROM " + FuelTrackAppDatabase.FUEL_LOG_TABLE + " " +
            "WHERE CarID = :carId")
    LiveData<CarCostStats> getCostStatsForVehicle(int carId);

    @Query("DELETE FROM " + FuelTrackAppDatabase.FUEL_LOG_TABLE + " WHERE  LogID = :recordId")
void deleteRecordById(long recordId);
}

//@PrimaryKey(autoGenerate = true)
//private int LogID;
//@NonNull
//private Integer CarID = -1;
//@NonNull
//private LocalDateTime logDate = LocalDateTime.now();;
//private Integer Odometer;
//private Double Gallons;
//private Double PricePerGallon;
//private Double TotalCost;
//private String Location;