/**
 * @author Stan Permiakov
 * @version 1.0
 * @since 2025-08-02
 */
package com.stanissudo.jycs_crafters.database;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.stanissudo.jycs_crafters.MainActivity;
import com.stanissudo.jycs_crafters.database.entities.FuelEntry;
import com.stanissudo.jycs_crafters.database.entities.User;
import com.stanissudo.jycs_crafters.database.entities.Vehicle;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FuelTrackAppRepository {
    private static FuelTrackAppRepository repository;
    // TODO: Add your DAO variable here
    private final FuelEntryDAO fuelEntryDAO;
    private final UserDAO userDAO;
    private final VehicleDAO vehicleDAO;
    private LiveData<List<FuelEntry>> allLogs;
    private final Handler main = new Handler(Looper.getMainLooper());

    public interface OdometerCheckCallback {
        void onResult(boolean ok, @androidx.annotation.Nullable Integer prev,
                      @androidx.annotation.Nullable Integer next);
    }

    public void checkOdometerAsync(int carId, java.time.LocalDateTime when, int value,
                                   OdometerCheckCallback cb) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            Integer prev = fuelEntryDAO.getPreviousOdometer(carId, when);
            Integer next = fuelEntryDAO.getNextOdometer(carId, when);
            boolean ok = (prev == null || value > prev) && (next == null || value < next);
            main.post(() -> cb.onResult(ok, prev, next));
        });
    }

    private FuelTrackAppRepository(Application application) {
        FuelTrackAppDatabase db = FuelTrackAppDatabase.getDatabase(application);
        this.fuelEntryDAO = db.fuelEntryDAO();
        this.userDAO = db.userDAO();
        this.vehicleDAO = db.vehicleDAO();
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
            Log.d(MainActivity.TAG, "Problem getting FuelTrackAppRepository, thread error.");
        }
        return null;
    }

    //TODO: Insert your DB methods here
    // =================== FuelEntry Methods ===================
    public void insertFuelEntry(FuelEntry fuelEntry) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> fuelEntryDAO.insert(fuelEntry));
    }

    interface BoolCallback {
        void onResult(boolean ok);
    }

    public Integer getPreviousOdometer(int carId, LocalDateTime logDate) {
        return fuelEntryDAO.getPreviousOdometer(carId, logDate);
    }

    public Integer getNextOdometer(int carId, LocalDateTime logDate) {
        return fuelEntryDAO.getNextOdometer(carId, logDate);
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

    public LiveData<List<FuelEntry>> getAllLogsByUserId(int loggedInUserId) {
        return fuelEntryDAO.getRecordsById(loggedInUserId);
    }

    // =================== Vehicle Methods ===================
    public void insertVehicle(Vehicle vehicle) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> vehicleDAO.insert(vehicle));
    }

    public void deleteVehicleByVehicleName(String name) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> vehicleDAO.deleteByVehicleName(name));
    }

    public LiveData<List<Vehicle>> getAllVehicles() {
        return vehicleDAO.getAllVehicles();
    }

    public LiveData<List<Vehicle>> getVehiclesForUser(int userId) {
        return vehicleDAO.getVehiclesForUser(userId);
    }
    public void updateVehicleName(int vehicleID, String newName) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> vehicleDAO.updateVehicleName(vehicleID, newName));
    }
    public void updateVehicleMake(int vehicleID, String newMake) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> vehicleDAO.updateVehicleMake(vehicleID, newMake));
    }
    public void updateVehicleModel(int vehicleID, String newModel) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> vehicleDAO.updateVehicleModel(vehicleID, newModel));
    }
    public void updateVehicleYear(int vehicleID, int newYear) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> vehicleDAO.updateVehicleYear(vehicleID, newYear));
    }
}
