/**
 * Author: Jose
 * Date: 2025-08-13
 *
 * Unit tests for verifying CRUD (Create, Read, Update, Delete) operations in the UserDAO.
 * These tests use an in-memory Room database to avoid affecting the real app data.
 *
 * Purpose:
 * - Validate that insert, update, and delete operations work as expected.
 * - Confirm that DAO queries return correct results.
 *
 * Notes:
 * - Uses AndroidJUnit4 runner for instrumentation testing.
 * - `allowMainThreadQueries()` is used here only for testing convenience.
 */

package com.stanissudo.jycs_crafters;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.stanissudo.jycs_crafters.database.FuelTrackAppDatabase;
import com.stanissudo.jycs_crafters.database.UserDAO;
import com.stanissudo.jycs_crafters.database.entities.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UserDaoCrudTest {

    private FuelTrackAppDatabase db;
    private UserDAO dao;

    /**
     * Sets up an in-memory database before each test and obtains the DAO instance.
     */
    @Before
    public void setUp() {
        Context ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, FuelTrackAppDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.userDAO();
    }

    /**
     * Closes the in-memory database after each test.
     */
    @After
    public void tearDown() {
        db.close();
    }

    /**
     * Tests that inserting a user correctly stores the record and
     * allows retrieval via `exists()` and `getPasswordForUsername()`.
     */
    @Test
    public void insert_insertsRow() {
        User u = new User("alice", "pw");
        dao.insert(u);

        // exists(...) returns COUNT(*)
        assertEquals(1, dao.exists("alice"));
        assertEquals("pw", dao.getPasswordForUsername("alice"));
    }

    /**
     * Tests that updating a user's password via `updatePassword()` modifies
     * the stored value correctly.
     */
    @Test
    public void update_updatesPassword() {
        dao.insert(new User("bob", "oldpw"));
        dao.updatePassword("bob", "newpw");

        assertEquals("newpw", dao.getPasswordForUsername("bob"));
    }

    /**
     * Tests that deleting a user by username removes their record from the database
     * and that subsequent lookups return no results.
     */
    @Test
    public void delete_deletesByUsername() {
        dao.insert(new User("charlie", "secret"));
        assertEquals(1, dao.exists("charlie"));

        dao.deleteByUsername("charlie");
        assertEquals(0, dao.exists("charlie"));
        assertNull(dao.getPasswordForUsername("charlie"));
    }
}
