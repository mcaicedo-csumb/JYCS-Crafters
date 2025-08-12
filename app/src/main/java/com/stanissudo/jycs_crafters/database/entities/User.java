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

    // CAMILA: add soft-status flag to support deactivate/reactivate (default active)
    private boolean isActive = true;

    public User(@NonNull String username, @NonNull String password) {
        this.username = username;
        this.password = password;
        this.isAdmin = false;
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

    // CAMILA: expose soft-status flag for admin management
    public boolean isActive() {
        return isActive;
    }

    // CAMILA: allow toggling active/inactive instead of hard-deleting
    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        // CAMILA: include isActive in equality to reflect soft-status in comparisons
        return id == user.id &&
                isAdmin == user.isAdmin &&
                isActive == user.isActive &&
                username.equals(user.username) &&
                password.equals(user.password);
    }

    @Override
    public int hashCode() {
        // CAMILA: include isActive so hash matches equals contract
        return Objects.hash(id, username, password, isAdmin, isActive);
    }
}
