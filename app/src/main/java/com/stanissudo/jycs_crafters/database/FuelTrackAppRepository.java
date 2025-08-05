/**
 * @author Stan Permiakov
 * @version 1.0
 * @since 2025-08-02
 */
package com.stanissudo.jycs_crafters.database;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.stanissudo.jycs_crafters.MainActivity;
import com.stanissudo.jycs_crafters.database.entities.FuelEntry;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FuelTrackAppRepository {
    private static FuelTrackAppRepository repository;
    private final FuelEntryDAO fuelEntryDAO;
    private LiveData<List<FuelEntry>> allLogs;

    private FuelTrackAppRepository(Application application) {
        FuelTrackAppDatabase db = FuelTrackAppDatabase.getDatabase(application);
        this.fuelEntryDAO = db.fuelEntryDAO();
        this.allLogs = this.fuelEntryDAO.getAllRecords();
    }

    /**
     * Returns a singleton instance of FuelTrackAppRepository.
     *
     * @param application The Application context used to initialize the database.
     * @return A singleton GymLogRepository instance.
     */
    public static FuelTrackAppRepository getRepository(Application application) {
        if (repository != null) {
            return repository;
        }
        Future<FuelTrackAppRepository> future = FuelTrackAppDatabase.databaseWriteExecutor.submit(
                new Callable<FuelTrackAppRepository>() {
                    @Override
                    public FuelTrackAppRepository call() throws Exception {
                        return new FuelTrackAppRepository(application);
                    }
                }
        );
        try {
            return future.get();

        } catch (InterruptedException | ExecutionException e) {
            Log.d(MainActivity.TAG, "Problem getting GymRepository, thread error.");
        }
        return null;
    }

  //TODO: Insert your DB methods here
    public LiveData<List<FuelEntry>> getAllLogs() {

        return fuelEntryDAO.getAllRecords();
    }
}
