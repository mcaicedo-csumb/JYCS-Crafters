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

    private final FuelEntryDAO fuelEntryDAO;
    private final UserDAO userDAO;

    private final LiveData<List<FuelEntry>> allLogs;

    private FuelTrackAppRepository(Application application) {
        FuelTrackAppDatabase db = FuelTrackAppDatabase.getDatabase(application);
        this.fuelEntryDAO = db.fuelEntryDAO();
        this.userDAO = db.userDAO();
        this.allLogs = fuelEntryDAO.getAllRecords();
    }

    public static FuelTrackAppRepository getRepository(Application application) {
        if (repository != null) return repository;

        Future<FuelTrackAppRepository> future = FuelTrackAppDatabase.databaseWriteExecutor.submit(
                () -> new FuelTrackAppRepository(application)
        );

        try {
            repository = future.get();
            return repository;
        } catch (InterruptedException | ExecutionException e) {
            Log.d(MainActivity.TAG, "Problem getting FuelTrackAppRepository, thread error.");
        }

        return null;
    }

    // =================== FuelEntry Methods ===================
    public LiveData<List<FuelEntry>> getAllLogs() {
        return fuelEntryDAO.getAllRecords();
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
