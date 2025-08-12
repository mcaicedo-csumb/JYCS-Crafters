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

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(User user);

    @Query("DELETE FROM " + FuelTrackAppDatabase.USER_TABLE)
    void deleteAll();

    @Query("DELETE FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username")
    void deleteByUsername(String username);

    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET password = :newPassword WHERE username = :username")
    void updatePassword(String username, String newPassword);

    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " ORDER BY username ASC")
    LiveData<List<User>> getAllUsers();

    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username LIMIT 1")
    LiveData<User> getUserByUsername(String username);

    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE id = :id LIMIT 1")
    LiveData<User> getUserById(int id);

    @Query("SELECT COUNT(*) FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username LIMIT 1")
    int exists(String username);

    @Query("SELECT password FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username LIMIT 1")
    String getPasswordForUsername(String username);

    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET isActive = 0 WHERE username = :u")
    int deactivateByUsername(String u);

    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET isActive = 1 WHERE username = :u")
    int reactivateByUsername(String u);

    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE isActive = 1 ORDER BY username ASC")
    LiveData<List<User>> getActiveUsers();

    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE isActive = 0 ORDER BY username ASC")
    LiveData<List<User>> getInactiveUsers();

    // CAMILA: single-value check used by LoginActivity to block inactive users
    @Query("SELECT isActive FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :u LIMIT 1")
    Integer isActiveFor(String u); // returns 1 (active), 0 (inactive), or null if user missing
}
