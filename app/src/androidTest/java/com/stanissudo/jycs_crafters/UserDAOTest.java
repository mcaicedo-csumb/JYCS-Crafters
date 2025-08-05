package com.stanissudo.jycs_crafters;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.stanissudo.jycs_crafters.database.FuelTrackAppDatabase;
import com.stanissudo.jycs_crafters.database.UserDAO;
import com.stanissudo.jycs_crafters.database.entities.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UserDAOTest {

    private FuelTrackAppDatabase db;
    private UserDAO dao;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, FuelTrackAppDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.userDAO();
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void insertAndGetUser() {
        User user = new User("admin", "admin");
        user.setAdmin(true);
        dao.insert(user);

        User fetched = dao.getUserByUsernameNow("admin");  // must be sync method
        assertNotNull(fetched);
        assertEquals("admin", fetched.getUsername());
        assertTrue(fetched.isAdmin());
    }

    @Test
    public void testInvalidUserReturnsNull() {
        User fetched = dao.getUserByUsernameNow("nonexistent");
        assertNull(fetched);
    }
}
