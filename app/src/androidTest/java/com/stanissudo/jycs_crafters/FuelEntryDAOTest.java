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

import com.stanissudo.jycs_crafters.database.FuelEntryDAO;
import com.stanissudo.jycs_crafters.database.FuelTrackAppDatabase;
import com.stanissudo.jycs_crafters.database.entities.FuelEntry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class FuelEntryDAOTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private FuelTrackAppDatabase db;
    private FuelEntryDAO dao;

    @Before
    public void setUp() {
        Context ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, FuelTrackAppDatabase.class)
                .allowMainThreadQueries() // OK for tests
                .build();
        dao = db.fuelEntryDAO();
    }

    @After
    public void tearDown() {
        db.close();
    }

    /** Helper to synchronously get a LiveData value on the main thread. */
    private static <T> T getOrAwaitValue(LiveData<T> liveData) {
        final Object[] data = new Object[1];
        CountDownLatch latch = new CountDownLatch(1);
        Observer<T> obs = new Observer<T>() {
            @Override public void onChanged(T t) {
                data[0] = t;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> liveData.observeForever(obs));
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

    private static FuelEntry entry(int carId, LocalDateTime when, int odo, double gal, double price, double total) {
        FuelEntry fe = new FuelEntry();
        fe.setCarID(carId);
        fe.setLogDate(when);
        fe.setOdometer(odo);
        fe.setGallons(gal);
        fe.setPricePerGallon(price);
        fe.setTotalCost(total);
        return fe;
    }

    @Test
    public void insertRecord() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 2, 3, 4, 5);

        dao.insertRecord(entry(99, now, 99999, 12.345, 6.47, 79.87));

        List<FuelEntry> all = getOrAwaitValue(dao.getEntriesForCar(99));
        assertNotNull(all);
        assertEquals(1, all.size());

        FuelEntry found = all.get(0);
        assertTrue(found.getLogID() > 0);
        assertEquals(99, (int) found.getCarID());
        assertEquals(99999, (int) found.getOdometer());
        assertEquals(12.345, found.getGallons(), 1e-6);
        assertEquals(6.47,   found.getPricePerGallon(), 1e-6);
        assertEquals(79.87,  found.getTotalCost(), 1e-6);
        assertEquals(now,    found.getLogDate());
    }

    @Test
    public void updateRecord() {
        // Insert original
        LocalDateTime t1 = LocalDateTime.of(2025, 1, 2, 3, 4, 5);
        dao.insertRecord(entry(99, t1, 10000, 10.0, 5.00, 50.00));

        // Read it back to get the generated id
        FuelEntry before = getOrAwaitValue(dao.getEntriesForCar(99)).get(0);
        int id = (int) before.getLogID();

        // Modify fields and update
        before.setOdometer(10123);
        before.setGallons(11.111);
        before.setPricePerGallon(5.25);
        before.setTotalCost(58.33);
        before.setLogDate(t1.plusDays(1));

        dao.updateRecord(before);

        // Verify persisted values
        FuelEntry after = getOrAwaitValue(dao.getRecordById(id));
        assertNotNull(after);
        assertEquals(id, after.getLogID());
        assertEquals(10123, (int) after.getOdometer());
        assertEquals(11.111, after.getGallons(), 1e-6);
        assertEquals(5.25,   after.getPricePerGallon(), 1e-6);
        assertEquals(58.33,  after.getTotalCost(), 1e-6);
        assertEquals(t1.plusDays(1), after.getLogDate());
    }

    @Test
    public void deleteRecordById() {
        // Insert two records
        LocalDateTime t1 = LocalDateTime.of(2025, 1, 2, 3, 4);
        LocalDateTime t2 = LocalDateTime.of(2025, 1, 3, 3, 4);
        dao.insertRecord(entry(99, t1, 10000, 10.0, 5.00, 50.0));
        dao.insertRecord(entry(99, t2, 10100, 9.0,  5.10, 45.9));

        List<FuelEntry> all = getOrAwaitValue(dao.getEntriesForCar(99));
        assertEquals(2, all.size());

        int deleteId = (int) all.get(0).getLogID(); // newest first (ORDER BY logDate DESC)

        dao.deleteRecordById(deleteId);

        List<FuelEntry> after = getOrAwaitValue(dao.getEntriesForCar(99));
        assertEquals(1, after.size());
        assertNotEquals(deleteId, after.get(0).getLogID());
    }
}
