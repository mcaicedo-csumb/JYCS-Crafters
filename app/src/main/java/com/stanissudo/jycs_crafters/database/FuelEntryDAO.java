package com.stanissudo.jycs_crafters.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.stanissudo.jycs_crafters.database.entities.FuelEntry;

import java.time.LocalDateTime;
import java.util.List;

import com.stanissudo.jycs_crafters.database.pojos.CarCostStats;
import com.stanissudo.jycs_crafters.database.pojos.CarDistanceStats;

@Dao
public interface FuelEntryDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecord(FuelEntry fuelEntry);
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateRecord(FuelEntry fuelEntry);

    @Query("SELECT * FROM " + FuelTrackAppDatabase.FUEL_LOG_TABLE + " WHERE CarID = :carId ORDER BY logDate DESC")
    LiveData<List<FuelEntry>> getEntriesForCar(int carId);

//    @Query("SELECT * FROM " + FuelTrackAppDatabase.FUEL_LOG_TABLE + " WHERE  LogID = :logId ORDER BY logDate DESC")
//    LiveData<List<FuelEntry>> getRecordsById(int logId);

    @Query("SELECT * FROM " + FuelTrackAppDatabase.FUEL_LOG_TABLE + " WHERE LogID = :id LIMIT 1")
    LiveData<FuelEntry> getRecordById(int id);

    @Query("SELECT odometer FROM " + FuelTrackAppDatabase.FUEL_LOG_TABLE + " WHERE LogID != :logId AND CarID = :carId AND logDate < :logDate ORDER BY logDate DESC LIMIT 1")
    Integer getPreviousOdometer(long logId, int carId, LocalDateTime logDate);
    @Query("SELECT odometer FROM " + FuelTrackAppDatabase.FUEL_LOG_TABLE + " WHERE LogID != :logId AND CarID = :carId AND logDate > :logDate ORDER BY logDate LIMIT 1")
    Integer getNextOdometer(long logId, int carId, LocalDateTime logDate);

    @Query("SELECT COUNT(*) AS fillUpsCount, " +
            "SUM(TotalCost) as totalCost, " +
            "AVG(PricePerGallon) as avgPricePerGallon, " +
            "AVG(TotalCost) as avgCostPerFillUp " +
            "FROM " + FuelTrackAppDatabase.FUEL_LOG_TABLE + " " +
            "WHERE CarID = :carId")
    LiveData<CarCostStats> getCostStatsForVehicle(int carId);

    @Query("SELECT MAX(Odometer) AS lastOdometer, " +
            "(MAX(Odometer) - MIN(Odometer)) as totalDistance, " +
            "(MAX(Odometer) - MIN(Odometer)) / (COUNT(*) - 1) as avgDistancePerFillUp " +
            "FROM " + FuelTrackAppDatabase.FUEL_LOG_TABLE + " " +
            "WHERE CarID = :carId")
    LiveData<CarDistanceStats> getDistanceStatsForVehicle(int carId);

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