// app/src/androidTest/java/com/stanissudo/jycs_crafters/VehicleDaoTest.java
package com.stanissudo.jycs_crafters;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.stanissudo.jycs_crafters.database.FuelTrackAppDatabase;
import com.stanissudo.jycs_crafters.database.VehicleDAO;
import com.stanissudo.jycs_crafters.database.entities.Vehicle;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/*Camila Caicedo
*8/15/2025
* Tests for VehicleDAO
 */

@RunWith(AndroidJUnit4.class)
public class VehicleDaoTest {

    // Makes LiveData execute instantly & treats calls as main-thread for tests
    @Rule public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private FuelTrackAppDatabase db;
    private VehicleDAO vehicleDAO;

    @Before
    public void setUp() {
        Context ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, FuelTrackAppDatabase.class)
                .allowMainThreadQueries() // OK in instrumented tests
                .build();
        vehicleDAO = db.vehicleDAO();
    }

    @After
    public void tearDown() {
        db.close();
    }

    // Helper to synchronously read LiveData on the *main* thread
    private static <T> T getOrAwaitValue(LiveData<T> liveData) {
        final Object[] data = new Object[1];
        CountDownLatch latch = new CountDownLatch(1);
        Observer<T> observer = new Observer<T>() {
            @Override public void onChanged(T o) {
                data[0] = o;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };
        // Ensure observeForever is called on the main thread
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            liveData.observeForever(observer);
        });
        try {
            if (!latch.await(2, TimeUnit.SECONDS)) {
                throw new AssertionError("LiveData value was never set.");
            }
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
        @SuppressWarnings("unchecked")
        T out = (T) data[0];
        return out;
    }

    private Vehicle makeVehicle(int userId, String name, String make, String model, int year) {
        Vehicle v = new Vehicle();
        v.setUserId(userId);
        v.setName(name);
        v.setMake(make);
        v.setModel(model);
        v.setYear(year);
        return v;
    }

    // 1) INSERT
    @Test
    public void insertVehicle_insertsAndCanBeReadBack() {
        vehicleDAO.insertVehicle(makeVehicle(1, "JUnit Insert", "Honda", "Civic", 2016));

        List<Vehicle> all = getOrAwaitValue(vehicleDAO.getAllVehicles());
        assertNotNull(all);

        Vehicle found = null;
        for (Vehicle it : all) {
            if ("JUnit Insert".equals(it.getName())
                    && "Honda".equals(it.getMake())
                    && "Civic".equals(it.getModel())
                    && it.getYear() == 2016) {
                found = it; break;
            }
        }
        assertNotNull(found);
        assertTrue(found.getVehicleID() > 0);
        assertEquals(1, found.getUserId().intValue());
    }

    // 2) UPDATE
    @Test
    public void updateVehicle_updatesFields() {
        vehicleDAO.insertVehicle(makeVehicle(2, "Old Name", "Ford", "Focus", 2014));

        List<Vehicle> beforeList = getOrAwaitValue(vehicleDAO.getAllVehicles());
        Vehicle before = null;
        for (Vehicle it : beforeList) {
            if ("Old Name".equals(it.getName())) { before = it; break; }
        }
        assertNotNull(before);
        int id = before.getVehicleID();

        vehicleDAO.updateVehicleName(id, "New Name");
        vehicleDAO.updateVehicleMake(id, "Toyota");
        vehicleDAO.updateVehicleModel(id, "Corolla");
        vehicleDAO.updateVehicleYear(id, 2018);

        List<Vehicle> afterList = getOrAwaitValue(vehicleDAO.getAllVehicles());
        Vehicle after = null;
        for (Vehicle it : afterList) {
            if (it.getVehicleID() == id) { after = it; break; }
        }
        assertNotNull(after);
        assertEquals("New Name", after.getName());
        assertEquals("Toyota", after.getMake());
        assertEquals("Corolla", after.getModel());
        assertEquals(2018, after.getYear());
    }

    // 3) DELETE
    @Test
    public void deleteVehicle_removesRow() {
        vehicleDAO.insertVehicle(makeVehicle(3, "To Delete", "Nissan", "Sentra", 2012));

        List<Vehicle> all = getOrAwaitValue(vehicleDAO.getAllVehicles());
        Vehicle inserted = null;
        for (Vehicle it : all) {
            if ("To Delete".equals(it.getName())) { inserted = it; break; }
        }
        assertNotNull(inserted);
        int id = inserted.getVehicleID();

        vehicleDAO.deleteVehicleById(id);

        List<Vehicle> after = getOrAwaitValue(vehicleDAO.getAllVehicles());
        boolean stillThere = false;
        for (Vehicle it : after) {
            if (it.getVehicleID() == id) { stillThere = true; break; }
        }
        assertFalse(stillThere);
    }
}
