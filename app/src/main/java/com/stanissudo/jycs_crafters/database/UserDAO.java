/**
 * Author: Jose Caicedo
 * Date: 2025-08-13
 *
 * This interface defines the Data Access Object (DAO) for managing User entities in the Room database.
 * It provides CRUD (Create, Read, Update, Delete) operations, as well as additional helper queries
 * for user management, such as password updates, account activation/deactivation, and lookups.
 *
 * The methods are annotated with Room persistence library annotations to execute SQL queries.
 * LiveData is used for reactive UI updates, while some methods return plain lists for background operations.
 */

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

    /**
     * Inserts a new user into the database.
     * If a user with the same primary key already exists, the insert will be ignored.
     *
     * @param user The User object to insert.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(User user);

    /**
     * Deletes all users from the database.
     */
    @Query("DELETE FROM " + FuelTrackAppDatabase.USER_TABLE)
    void deleteAll();

    /**
     * Deletes a specific user by their username.
     *
     * @param username The username of the user to delete.
     */
    @Query("DELETE FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username")
    void deleteByUsername(String username);

    /**
     * Updates the password for a given username.
     *
     * @param username    The username whose password will be updated.
     * @param newPassword The new password (or password hash).
     */
    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET password = :newPassword WHERE username = :username")
    void updatePassword(String username, String newPassword);

    /**
     * Checks if a user with the given username exists in the database.
     *
     * @param username The username to check.
     * @return The count of matching users (0 if not found, >0 if exists).
     */
    @Query("SELECT COUNT(*) FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username")
    int exists(String username);

    /**
     * Retrieves a user by their username as LiveData.
     * This allows UI to observe and update automatically when the user data changes.
     *
     * @param username The username to search for.
     * @return LiveData containing the User object, or null if not found.
     */
    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username LIMIT 1")
    LiveData<User> getUserByUsername(String username);

    /**
     * Retrieves a user by their ID as LiveData.
     *
     * @param id The user ID.
     * @return LiveData containing the User object.
     */
    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE id = :id LIMIT 1")
    LiveData<User> getUserById(int id);

    /**
     * Retrieves all users in ascending order by username.
     *
     * @return LiveData list of all users.
     */
    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " ORDER BY username ASC")
    LiveData<List<User>> getAllUsers();

    /**
     * Camila: Retrieves all users as a standard List (non-LiveData),
     * useful for background maintenance tasks.
     * @return A list of all users.
     */
    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE)
    List<User> getAllUsersList();

    /**
     * Maria: Fetches the stored password (hash or legacy plaintext) for the given username.
     * Used for password verification during login.
     * @param username The username to search for.
     * @return The stored password string.
     */
    @Query("SELECT password FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :username LIMIT 1")
    String getPasswordForUsername(String username);

    /**
     * Camila: Updates the password for a user identified by ID.
     * This is used by system sweeps and settings.
     * @param userId       The ID of the user.
     * @param passwordHash The new password hash.
     */
    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET password = :passwordHash WHERE id = :userId")
    void updatePasswordById(int userId, String passwordHash);

    /**
     * Maria: Updates the display name of a user by ID.
     *
     * @param userId      The ID of the user.
     * @param displayName The new display name.
     */
    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET displayName = :displayName WHERE id = :userId")
    void updateDisplayName(int userId, String displayName);

    /**
     * Maria: Soft-deletes a user by setting their "isActive" status to false (0).
     *
     * @param userId The ID of the user to deactivate.
     */
    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET isActive = 0 WHERE id = :userId")
    void softDeleteUserById(int userId);

    /**
     * Reactivates a previously soft-deleted user by ID.
     *
     * @param userId The ID of the user to reactivate.
     */
    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET isActive = 1 WHERE id = :userId")
    void reactivateUserById(int userId);

    /**
     * Maria: Deactivates a user by their username (used for admin screen convenience).
     *
     * @param username The username of the user to deactivate.
     */
    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET isActive = 0 WHERE username = :username")
    void deactivateByUsername(String username);

    /**
     * Reactivates a user by their username.
     *
     * @param username The username of the user to reactivate.
     */
    @Query("UPDATE " + FuelTrackAppDatabase.USER_TABLE + " SET isActive = 1 WHERE username = :username")
    void reactivateByUsername(String username);

    /**
     * Maria: Retrieves the active status of a user by their username.
     *
     * @param u The username.
     * @return 1 if active, 0 if inactive, or null if not found.
     */
    @Query("SELECT isActive FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE username = :u LIMIT 1")
    Integer isActiveFor(String u);

    /**
     * Retrieves a list of all active users ordered by username.
     *
     * @return LiveData list of active users.
     */
    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE isActive = 1 ORDER BY username ASC")
    LiveData<List<User>> getActiveUsers();

    /**
     * Retrieves a list of all inactive users ordered by username.
     *
     * @return LiveData list of inactive users.
     */
    @Query("SELECT * FROM " + FuelTrackAppDatabase.USER_TABLE + " WHERE isActive = 0 ORDER BY username ASC")
    LiveData<List<User>> getInactiveUsers();
}
