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
// CAMILA: stats – POJO used by CostStatsViewModel
import com.stanissudo.jycs_crafters.database.pojos.CarCostStats;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FuelTrackAppRepository {

    private static FuelTrackAppRepository repository;

    private final FuelEntryDAO fuelEntryDAO;
    private final UserDAO userDAO;
    private final VehicleDAO vehicleDAO;

    private final Handler main = new Handler(Looper.getMainLooper());

    public interface ResultCallback { void onResult(boolean ok, String message); }
    public interface ExistsCallback { void onResult(boolean exists); }
    // CAMILA: simple boolean callback for active checks
    public interface BoolCallback { void onResult(boolean value); }

    public interface OdometerCheckCallback {
        void onResult(boolean ok, Integer prev, Integer next);
    }

    private FuelTrackAppRepository(Application application) {
        FuelTrackAppDatabase db = FuelTrackAppDatabase.getDatabase(application);
        this.fuelEntryDAO = db.fuelEntryDAO();
        this.userDAO = db.userDAO();
        this.vehicleDAO = db.vehicleDAO();
    }

    public static FuelTrackAppRepository getRepository(Application application) {
        if (repository != null) return repository;
        Future<FuelTrackAppRepository> future =
                FuelTrackAppDatabase.databaseWriteExecutor.submit(
                        (Callable<FuelTrackAppRepository>) () -> new FuelTrackAppRepository(application)
                );
        try {
            repository = future.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.d(MainActivity.TAG, "Problem getting FuelTrackAppRepository, thread error.");
        }
        return repository;
    }

    // =================== FuelEntry Methods ===================

    public void insertFuelEntry(FuelEntry fuelEntry) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> fuelEntryDAO.insert(fuelEntry));
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

    public void checkOdometerAsync(int carId, LocalDateTime when, int value, OdometerCheckCallback cb) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            Integer prev = fuelEntryDAO.getPreviousOdometer(carId, when);
            Integer next = fuelEntryDAO.getNextOdometer(carId, when);
            boolean ok = (prev == null || value > prev) && (next == null || value < next);
            main.post(() -> cb.onResult(ok, prev, next));
        });
    }

    public LiveData<List<FuelEntry>> getAllLogsByUserId(int loggedInUserId) {
        return fuelEntryDAO.getRecordsById(loggedInUserId);
    }

    // CAMILA: stats – DAO returns a single CarCostStats
    public LiveData<CarCostStats> getCostStatsForVehicle(int vehicleId) {
        return fuelEntryDAO.getCostStatsForVehicle(vehicleId);
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

    public void userExistsAsync(String username, ExistsCallback cb) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            boolean exists = userDAO.exists(username) == 1;
            main.post(() -> cb.onResult(exists));
        });
    }

    public void addUserSafely(String username, String password, boolean isAdmin, ResultCallback cb) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            boolean exists = userDAO.exists(username) == 1;
            if (exists) {
                main.post(() -> cb.onResult(false, "Username already exists."));
                return;
            }
            User u = new User(username, password);
            u.setAdmin(isAdmin);
            userDAO.insert(u);
            main.post(() -> cb.onResult(true, "User added."));
        });
    }

    public void changePasswordIfUserExists(String username, String newPassword, ResultCallback cb) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            boolean exists = userDAO.exists(username) == 1;
            if (!exists) {
                main.post(() -> cb.onResult(false, "Username does not exist."));
                return;
            }
            userDAO.updatePassword(username, newPassword);
            main.post(() -> cb.onResult(true, "Password changed."));
        });
    }

    public void changePasswordWithCurrentCheck(String currentUsername,
                                               String currentPassword,
                                               String newPassword,
                                               ResultCallback cb) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            String stored = userDAO.getPasswordForUsername(currentUsername);
            if (stored == null) {
                main.post(() -> cb.onResult(false, "Session error: user not found."));
                return;
            }
            if (!stored.equals(currentPassword)) {
                main.post(() -> cb.onResult(false, "Current password is incorrect."));
                return;
            }
            userDAO.updatePassword(currentUsername, newPassword);
            main.post(() -> cb.onResult(true, "Password changed."));
        });
    }

    public void deleteUserSafely(String usernameToDelete, String currentUsername, boolean currentIsAdmin, ResultCallback cb) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            if (currentIsAdmin && usernameToDelete.equals(currentUsername)) {
                main.post(() -> cb.onResult(false, "You can't delete the account you're logged in with."));
                return;
            }
            boolean exists = userDAO.exists(usernameToDelete) == 1;
            if (!exists) {
                main.post(() -> cb.onResult(false, "Username does not exist."));
                return;
            }
            userDAO.deleteByUsername(usernameToDelete);
            main.post(() -> cb.onResult(true, "User removed."));
        });
    }

    public LiveData<List<User>> getActiveUsers() { return userDAO.getActiveUsers(); }
    public LiveData<List<User>> getInactiveUsers() { return userDAO.getInactiveUsers(); }

    public void deactivateUserSafely(String targetUsername, String currentUsername, boolean currentIsAdmin, ResultCallback cb) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            if (currentIsAdmin && targetUsername.equals(currentUsername)) {
                main.post(() -> cb.onResult(false, "You can’t deactivate the account you’re using."));
                return;
            }
            if (userDAO.exists(targetUsername) != 1) {
                main.post(() -> cb.onResult(false, "Username does not exist."));
                return;
            }
            userDAO.deactivateByUsername(targetUsername);
            main.post(() -> cb.onResult(true, "User deactivated."));
        });
    }

    public void reactivateUser(String targetUsername, ResultCallback cb) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            userDAO.reactivateByUsername(targetUsername);
            main.post(() -> cb.onResult(true, "User reactivated."));
        });
    }

    // CAMILA: async helper for LoginActivity – returns true only if the user row exists AND isActive == 1
    public void isUserActiveAsync(String username, BoolCallback cb) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            Integer flag = userDAO.isActiveFor(username);
            boolean active = (flag != null && flag == 1);
            main.post(() -> cb.onResult(active));
        });
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
}
