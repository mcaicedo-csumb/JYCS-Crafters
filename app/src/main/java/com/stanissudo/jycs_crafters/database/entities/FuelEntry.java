package com.stanissudo.jycs_crafters.database.entities;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.stanissudo.jycs_crafters.database.FuelTrackAppDatabase;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity(tableName = FuelTrackAppDatabase.FUEL_LOG_TABLE)
public class FuelEntry {
    @PrimaryKey(autoGenerate = true)
    private int LogID;
    private int CarID;
    @NonNull
    private LocalDateTime logDate;
    private int Odometer;
    private double Gallons;
    private double PricePerGallon;
    private double TotalCost;
    private String Location;

    public FuelEntry() {}
    public FuelEntry(int carID, int odometer, double gallons, double pricePerGallon) {
        CarID = carID;
        logDate = LocalDateTime.now();
        Odometer = odometer;
        Gallons = gallons;
        TotalCost = gallons * pricePerGallon;
        PricePerGallon = pricePerGallon;
    }

    public FuelEntry(int carID, int odometer, double gallons, double pricePerGallon, String location) {
        CarID = carID;
        logDate = LocalDateTime.now();
        Odometer = odometer;
        Gallons = gallons;
        PricePerGallon = pricePerGallon;
        TotalCost = gallons * pricePerGallon;
        Location = location;
    }

    public FuelEntry(int carID, @NonNull LocalDateTime logDate, int odometer, double gallons, double pricePerGallon, String location) {
        CarID = carID;
        this.logDate = logDate;
        Odometer = odometer;
        Gallons = gallons;
        PricePerGallon = pricePerGallon;
        TotalCost = gallons * pricePerGallon;
        Location = location;
    }

    public FuelEntry(int carID, double pricePerGallon, double gallons, int odometer, @NonNull LocalDateTime logDate) {
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
        return LogID == fuelEntry.LogID && CarID == fuelEntry.CarID && Odometer == fuelEntry.Odometer && Double.compare(Gallons, fuelEntry.Gallons) == 0 && Double.compare(PricePerGallon, fuelEntry.PricePerGallon) == 0 && Double.compare(TotalCost, fuelEntry.TotalCost) == 0 && Objects.equals(logDate, fuelEntry.logDate) && Objects.equals(Location, fuelEntry.Location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(LogID, CarID, logDate, Odometer, Gallons, PricePerGallon, TotalCost, Location);
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
                ", TotalCost=" + TotalCost +
                ", Location='" + Location + '\'' +
                '}';
    }

    public int getLogID() {
        return LogID;
    }

    public void setLogID(int logID) {
        LogID = logID;
    }

    public int getCarID() {
        return CarID;
    }

    public void setCarID(int carID) {
        CarID = carID;
    }

    public LocalDateTime getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDateTime logDate) {
        this.logDate = logDate;
    }

    public int getOdometer() {
        return Odometer;
    }

    public void setOdometer(int odometer) {
        Odometer = odometer;
    }

    public double getGallons() {
        return Gallons;
    }

    public void setGallons(double gallons) {
        Gallons = gallons;
    }

    public double getPricePerGallon() {
        return PricePerGallon;
    }

    public void setPricePerGallon(double pricePerGallon) {
        PricePerGallon = pricePerGallon;
    }

    public double getTotalCost() {
        return TotalCost;
    }

    public void setTotalCost(double totalCost) {
        TotalCost = totalCost;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }
}
