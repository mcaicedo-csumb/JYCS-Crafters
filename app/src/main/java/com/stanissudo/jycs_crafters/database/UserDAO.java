// Author: Jose Caicedo
// Camila: added admin queries + active toggle for User Management
// Date: 08/05/2025

package com.stanissudo.jycs_crafters.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.stanissudo.jycs_crafters.database.entities.User;

import java.util.List;

@Dao
public interface UserDAO {

    // Camila: standard insert (void is fine for Room; we look up later)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Delete
    void delete(User user);

    // Camila: used by DB callback to clear demo data
    @Query("DELETE FROM " + FuelTrackAppDatabase.USER_TABLE)
    void deleteAll();

    // Camila: admin list (User Management)
    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " ORDER BY username ASC")
    LiveData<List<User>> getAllUsers();

    // Camila: direct login fetch; also handy in tests
    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE +
            " WHERE username = :username AND password = :password LIMIT 1")
    User loginNow(String username, String password);

    // Camila: single user by id (LiveData) for screens that need to observe row changes
    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE id = :id LIMIT 1")
    LiveData<User> getUserById(int id);

    // Camila: fetch one user by username (sync) for repo helper
    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username LIMIT 1")
    User getUserByUsernameNow(String username);

    // Camila: admin toggle
    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET isActive = :active WHERE id = :userId")
    void setUserActive(int userId, boolean active);

    // Camila: small helpers used by repository; safe no-ops if username not found
    @Query("DELETE FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username")
    void deleteByUsername(String username);

    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET password = :newPassword WHERE username = :username")
    void updatePassword(String username, String newPassword);
}
