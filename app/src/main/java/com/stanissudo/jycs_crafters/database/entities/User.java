package com.stanissudo.jycs_crafters.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import com.stanissudo.jycs_crafters.database.FuelTrackAppDatabase;

import java.util.Objects;

/**
 * Represents a user account stored in the Room database.
 */
@Entity(tableName = FuelTrackAppDatabase.USER_TABLE)
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String username;

    @NonNull
    private String password;

    private boolean isAdmin = false;

    // display name editable in Settings
    private String displayName;

    // (active/deactivated)
    private boolean isActive = true;

    public User(@NonNull String username, @NonNull String password) {
        this.username = username;
        this.password = password;
        this.isAdmin = false;

        // default display name mirrors username
        this.displayName = username;

        // new accounts are active by default
        this.isActive = true;
    }

    // Getters and setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }


    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id == user.id &&
                isAdmin == user.isAdmin &&
                // CAMILA: isActive in equality
                isActive == user.isActive &&
                username.equals(user.username) &&
                password.equals(user.password) &&
                // CAMILA: displayName in equality
                Objects.equals(displayName, user.displayName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, username, password, isAdmin, isActive, displayName);
    }
}
