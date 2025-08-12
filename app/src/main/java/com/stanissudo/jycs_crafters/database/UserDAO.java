package com.stanissudo.jycs_crafters.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.stanissudo.jycs_crafters.database.entities.User;

import java.util.List;

@Dao
public interface UserDAO {

    // ===== Basic CRUD / lookups =====
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Query("DELETE FROM " + FuelTrackAppDatabase.USER_TABLE)
    void deleteAll();

    @Query("DELETE FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username")
    void deleteByUsername(String username);

    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " ORDER BY username ASC")
    LiveData<List<User>> getAllUsers();

    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username LIMIT 1")
    LiveData<User> getUserByUsername(String username);

    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE id = :id LIMIT 1")
    LiveData<User> getUserById(int id);

    @Query("SELECT COUNT(*) FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :u")
    int exists(String u);

    // Change password by username (legacy)
    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET password = :newPassword WHERE username = :username")
    void updatePassword(String username, String newPassword);

    // ===== Admin: activate/deactivate by username =====
    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET isActive = 0 WHERE username = :u")
    int deactivateByUsername(String u);

    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET isActive = 1 WHERE username = :u")
    int reactivateByUsername(String u);

    // Lists for admin UI
    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE isActive = 1 ORDER BY username ASC")
    LiveData<List<User>> getActiveUsers();

    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE isActive = 0 ORDER BY username ASC")
    LiveData<List<User>> getInactiveUsers();

    // ===== Settings spec additions =====

    // CAMILA: update editable display name by userId (Settings screen)
    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET displayName = :name WHERE id = :userId")
    void updateDisplayName(int userId, String name);

    // CAMILA: update password by userId (expects hashed value; used by Settings + login upgrade)
    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET password = :hashedPassword WHERE id = :userId")
    void updatePasswordById(int userId, String hashedPassword);

    // CAMILA: soft delete (deactivate) the current account by id (used by Settings “Deactivate My Account”)
    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET isActive = 0 WHERE id = :userId")
    void softDeleteUser(int userId);

    @Query("SELECT password FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username LIMIT 1")
    String getPasswordForUsername(String username);
}
