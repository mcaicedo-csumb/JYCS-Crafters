package com.stanissudo.jycs_crafters.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Camila: Vehicle entity for admin review + soft delete/restore (isActive) */
@Entity(
        tableName = "vehicles",
        foreignKeys = @ForeignKey(
                entity = com.stanissudo.jycs_crafters.database.entities.User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = { @Index("userId") }
)
public class Vehicle {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "userId")
    private int userId;

    // Camila: simple display name for the admin list (e.g., "2016 Honda Civic")
    @ColumnInfo(name = "name")
    private String name;

    // Optional details I may show later
    @ColumnInfo(name = "year")
    private Integer year;

    @ColumnInfo(name = "make")
    private String make;

    @ColumnInfo(name = "model")
    private String model;

    // Camila: soft-delete flag used by VehicleReviewActivity
    @ColumnInfo(name = "isActive", defaultValue = "1")
    private boolean isActive = true;

    // --- Getters/Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return "Vehicle{id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", year=" + year +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
