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

    // Basic CRUD
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(User user);

    @Query("DELETE FROM " + FuelTrackAppDatabase.USER_TABLE)
    void deleteAll();

    @Query("DELETE FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username")
    void deleteByUsername(String username);

    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET password = :newPassword WHERE username = :username")
    void updatePassword(String username, String newPassword);

    @Query("SELECT COUNT(*) FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username")
    int exists(String username);

    // Lookups
    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username LIMIT 1")
    LiveData<User> getUserByUsername(String username);

    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE id = :id LIMIT 1")
    LiveData<User> getUserById(int id);

    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " ORDER BY username ASC")
    LiveData<List<User>> getAllUsers();

    // CAMILA: non-LiveData list for background maintenance sweep
    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE)
    List<User> getAllUsersList();

    // CAMILA: fetch stored password (hash or legacy plaintext) for comparison
    @Query("SELECT password FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username LIMIT 1")
    String getPasswordForUsername(String username);

    // CAMILA: update password by id (used by sweep + settings)
    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET password = :passwordHash WHERE id = :userId")
    void updatePasswordById(int userId, String passwordHash);

    // CAMILA: display name updates
    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET displayName = :displayName WHERE id = :userId")
    void updateDisplayName(int userId, String displayName);

    // CAMILA: soft-delete / reactivate by id (settings / admin tools)
    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET isActive = 0 WHERE id = :userId")
    void softDeleteUserById(int userId);

    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET isActive = 1 WHERE id = :userId")
    void reactivateUserById(int userId);

    // CAMILA: deactivate/reactivate by username (admin screen convenience)
    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET isActive = 0 WHERE username = :username")
    void deactivateByUsername(String username);

    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET isActive = 1 WHERE username = :username")
    void reactivateByUsername(String username);

    // CAMILA: activity helpers
    @Query("SELECT isActive FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :u LIMIT 1")
    Integer isActiveFor(String u);

    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE isActive = 1 ORDER BY username ASC")
    LiveData<List<User>> getActiveUsers();

    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE isActive = 0 ORDER BY username ASC")
    LiveData<List<User>> getInactiveUsers();
}
