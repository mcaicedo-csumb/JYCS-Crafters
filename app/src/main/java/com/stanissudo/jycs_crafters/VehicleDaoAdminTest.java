package com.stanissudo.jycs_crafters;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.stanissudo.jycs_crafters.database.FuelTrackAppDatabase;
import com.stanissudo.jycs_crafters.database.UserDAO;
import com.stanissudo.jycs_crafters.database.VehicleDAO;
import com.stanissudo.jycs_crafters.database.entities.User;
import com.stanissudo.jycs_crafters.database.entities.Vehicle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class VehicleDaoAdminTest {

    private FuelTrackAppDatabase db;
    private VehicleDAO vehicleDAO;
    private UserDAO userDAO;

    @Before
    public void setup() {
        Context ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, FuelTrackAppDatabase.class)
                .allowMainThreadQueries() // tests only
                .build();
        vehicleDAO = db.vehicleDAO();
        userDAO = db.userDAO();
    }

    @After
    public void tearDown() { db.close(); }

    @Test
    public void admin_softDelete_and_restore_vehicle() throws Exception {
        // Arrange: owner user
        User owner = new User("owner1", "pw", false);
        userDAO.insert(owner);
        User ownerDb = userDAO.loginNow("owner1", "pw");
        assertNotNull(ownerDb);

        // Insert a vehicle
        Vehicle car = new Vehicle();
        car.setUserId(ownerDb.getId());
        car.setName("2016 Honda Civic");
        car.setActive(true);
        vehicleDAO.insert(car);

        // Sanity: it appears in active list
        List<Vehicle> active1 = TestUtils.getOrAwait(vehicleDAO.getActiveVehicles());
        Vehicle found = null;
        for (Vehicle v : active1) {
            if ("2016 Honda Civic".equals(v.getName())) { found = v; break; }
        }
        assertNotNull(found);

        // Act: soft delete
        vehicleDAO.softDelete(found.getId());
        List<Vehicle> inactive = TestUtils.getOrAwait(vehicleDAO.getInactiveVehicles());
        boolean inInactive = false;
        for (Vehicle v : inactive) {
            if (v.getId() == found.getId()) { inInactive = true; break; }
        }
        assertTrue(inInactive);

        // Act: restore
        vehicleDAO.restore(found.getId());
        List<Vehicle> active2 = TestUtils.getOrAwait(vehicleDAO.getActiveVehicles());
        boolean backActive = false;
        for (Vehicle v : active2) {
            if (v.getId() == found.getId()) { backActive = true; break; }
        }
        assertTrue(backActive);
    }
}
