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

    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE id = :id LIMIT 1")
    LiveData<User> getUserById(int id);

    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username LIMIT 1")
    LiveData<User> getUserByUsername(String username);

    // Synchronous fetch for background-thread checks
    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username LIMIT 1")
    User getUserByUsernameNow(String username);

    // ✅ NEW: fast existence check (returns 1 if exists, 0 if not)
    @Query("SELECT EXISTS(SELECT 1 FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username)")
    int exists(String username);

    @Query("DELETE FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username")
    void deleteByUsername(String username);

    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET password = :newPassword WHERE username = :username")
    void updatePassword(String username, String newPassword);

    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " ORDER BY username ASC")
    LiveData<List<User>> getAllUsers();


    @Query("SELECT password FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username LIMIT 1")
    String getPasswordForUsername(String username);


}
