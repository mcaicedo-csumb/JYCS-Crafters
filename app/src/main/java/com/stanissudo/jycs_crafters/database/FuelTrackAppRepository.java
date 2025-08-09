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
import com.stanissudo.jycs_crafters.database.entities.User;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FuelTrackAppRepository {
    private static FuelTrackAppRepository repository;
    // TODO: Add your DAO variable here
    private final FuelEntryDAO fuelEntryDAO;
    private final UserDAO userDAO;
    private LiveData<List<FuelEntry>> allLogs;

    private FuelTrackAppRepository(Application application) {
        FuelTrackAppDatabase db = FuelTrackAppDatabase.getDatabase(application);
        this.fuelEntryDAO = db.fuelEntryDAO();
        this.userDAO = db.userDAO();
        //TODO: Assign your DAO Variable here
        //this.allLogs = this.fuelEntryDAO.getAllRecords();
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
    // =================== FuelEntry Methods ===================
    public void insertFuelEntry(FuelEntry fuelEntry) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> fuelEntryDAO.insert(fuelEntry));
    }

    public LiveData<List<FuelEntry>> getEntriesForCar(int carId) {
        return fuelEntryDAO.getEntriesForCar(carId);
    }

    // =================== User Methods ===================
    public LiveData<User> getUserByUsername(String username) {
        return userDAO.getUserByUsername(username);
    }

    public LiveData<User> getUserById(int id) {
        return userDAO.getUserById(id);
    }

    public void insertUser(User user) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> userDAO.insert(user));
    }

    public void deleteAllUsers() {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(userDAO::deleteAll);
    }

    public void deleteUserByUsername(String username) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> userDAO.deleteByUsername(username));
    }

    public void updatePassword(String username, String newPassword) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> userDAO.updatePassword(username, newPassword));
    }

    public LiveData<List<User>> getAllUsers() {
        return userDAO.getAllUsers();
    }

}
