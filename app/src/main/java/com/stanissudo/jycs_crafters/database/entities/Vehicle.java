package com.stanissudo.jycs_crafters.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.stanissudo.jycs_crafters.database.FuelTrackAppDatabase;

import java.util.Objects;

/**
 * @author Ysabelle Kim
 * created: 8/9/2025 - 11:39 PM
 * @project JYCS-Crafters
 * file: Vehicle.java
 * @since 1.0.0
 * Explanation: Vehicle entity for FuelTrackAppDatabase
 */
@Entity(tableName = FuelTrackAppDatabase.VEHICLE_TABLE)
public class Vehicle {
    @PrimaryKey(autoGenerate = true)
    private int VehicleID;
    private String Name;
    private String Make;
    private String Model;
    private int Year;

    public Vehicle() {
    }

    public Vehicle(String name, String make, String model, int year) {
        Name = name;
        Make = make;
        Model = model;
        Year = year;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Vehicle)) return false;
        Vehicle vehicle = (Vehicle) o;
        return VehicleID == vehicle.VehicleID && Year == vehicle.Year && Objects.equals(Name, vehicle.Name) && Objects.equals(Make, vehicle.Make) && Objects.equals(Model, vehicle.Model);
    }

    @Override
    public int hashCode() {
        return Objects.hash(VehicleID, Name, Make, Model, Year);
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "VehicleID=" + VehicleID +
                ", Name='" + Name + '\'' +
                ", Make='" + Make + '\'' +
                ", Model='" + Model + '\'' +
                ", Year=" + Year +
                '}';
    }

    public int getVehicleID() {
        return VehicleID;
    }

    public void setVehicleID(int vehicleID) {
        VehicleID = vehicleID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getMake() {
        return Make;
    }

    public void setMake(String make) {
        Make = make;
    }

    public int getYear() {
        return Year;
    }

    public void setYear(int year) {
        Year = year;
    }

    public String getModel() {
        return Model;
    }

    public void setModel(String model) {
        Model = model;
    }
}
