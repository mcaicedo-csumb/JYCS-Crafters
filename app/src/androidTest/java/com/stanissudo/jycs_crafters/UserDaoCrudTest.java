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

    @Before
    public void setUp() {
        Context ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, FuelTrackAppDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.userDAO();
    }

    @After
    public void tearDown() { db.close(); }

    @Test
    public void insert_insertsRow() {
        User u = new User("alice", "pw");
        dao.insert(u);

        // exists(...) returns COUNT(*)
        assertEquals(1, dao.exists("alice"));
        assertEquals("pw", dao.getPasswordForUsername("alice"));
    }

    @Test
    public void update_updatesPassword() {
        dao.insert(new User("bob", "oldpw"));
        dao.updatePassword("bob", "newpw");

        assertEquals("newpw", dao.getPasswordForUsername("bob"));
    }

    @Test
    public void delete_deletesByUsername() {
        dao.insert(new User("charlie", "secret"));
        assertEquals(1, dao.exists("charlie"));

        dao.deleteByUsername("charlie");
        assertEquals(0, dao.exists("charlie"));
        assertNull(dao.getPasswordForUsername("charlie"));
    }
}
