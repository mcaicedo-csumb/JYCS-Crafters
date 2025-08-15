package com.stanissudo.jycs_crafters.database.entities;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.stanissudo.jycs_crafters.database.FuelTrackAppDatabase;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * * @author Stan Permiakov
 * * created: 8/12/2025
 * * @project JYCS-Crafters
 */
@Entity(tableName = FuelTrackAppDatabase.FUEL_LOG_TABLE)
public class FuelEntry {
    @PrimaryKey(autoGenerate = true)
    private long LogID;
    @NonNull
    private Integer CarID = -1;
    @NonNull
    private LocalDateTime logDate = LocalDateTime.now();
    private Integer Odometer;
    private Double Gallons;
    private Double PricePerGallon;
    private Double TotalCost;

    public FuelEntry() {
    }

    public FuelEntry(int carID, int odometer, double gallons, double pricePerGallon) {
        CarID = carID;
        logDate = LocalDateTime.now();
        Odometer = odometer;
        Gallons = gallons;
        TotalCost = gallons * pricePerGallon;
        PricePerGallon = pricePerGallon;
    }

    public FuelEntry(int carID, int odometer, double pricePerGallon, double gallons, @NonNull LocalDateTime logDate) {
        CarID = carID;
        PricePerGallon = pricePerGallon;
        TotalCost = gallons * pricePerGallon;
        Gallons = gallons;
        Odometer = odometer;
        this.logDate = logDate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FuelEntry fuelEntry = (FuelEntry) o;
        return LogID == fuelEntry.LogID && CarID == fuelEntry.CarID && Odometer == fuelEntry.Odometer && Double.compare(Gallons, fuelEntry.Gallons) == 0 && Double.compare(PricePerGallon, fuelEntry.PricePerGallon) == 0 && Double.compare(TotalCost, fuelEntry.TotalCost) == 0 && Objects.equals(logDate, fuelEntry.logDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(LogID, CarID, logDate, Odometer, Gallons, PricePerGallon, TotalCost);
    }

    @NonNull
    @Override
    public String toString() {
        return "FuelEntry{" +
                "LogID=" + LogID +
                ", CarID=" + CarID +
                ", logDate=" + logDate +
                ", Odometer=" + Odometer +
                ", Gallons=" + Gallons +
                ", PricePerGallon=" + PricePerGallon +
                ", TotalCost=" + TotalCost + '\'' +
                '}';
    }

    public long getLogID() {
        return LogID;
    }

    public void setLogID(long logID) {
        LogID = logID;
    }

    @NonNull
    public Integer getCarID() {
        return CarID;
    }

    public void setCarID(@NonNull Integer carID) {
        CarID = carID;
    }

    @NonNull
    public LocalDateTime getLogDate() {
        return logDate;
    }

    public void setLogDate(@NonNull LocalDateTime logDate) {
        this.logDate = logDate;
    }

    public Integer getOdometer() {
        return Odometer;
    }

    public void setOdometer(Integer odometer) {
        Odometer = odometer;
    }

    public Double getGallons() {
        return Gallons;
    }

    public void setGallons(Double gallons) {
        Gallons = gallons;
    }

    public Double getPricePerGallon() {
        return PricePerGallon;
    }

    public void setPricePerGallon(Double pricePerGallon) {
        PricePerGallon = pricePerGallon;
    }

    public Double getTotalCost() {
        return TotalCost;
    }

    public void setTotalCost(Double totalCost) {
        TotalCost = totalCost;
    }

}
