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
import com.stanissudo.jycs_crafters.database.pojos.CarCostStats;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
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

    public LiveData<CarCostStats> getCostStatsForVehicle(int vehicleId) {
        return fuelEntryDAO.getCostStatsForVehicle(vehicleId);
    }

    // ====== Add near your other callbacks ======
    public interface ExistsCallback {
        void onResult(boolean exists);
    }

    public interface ResultCallback {
        void onResult(boolean ok, String message);
    }

    // ✅ Check if a username exists (async)
    public void userExistsAsync(String username, ExistsCallback cb) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            boolean exists = userDAO.exists(username) == 1;
            main.post(() -> cb.onResult(exists));
        });
    }

    // ✅ Change password, but only if the username exists
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

    // ✅ Delete user safely: prevent deleting the currently-logged-in admin; validate existence first
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

    // Add a user ONLY if the username doesn't already exist
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
            if (!stored.equals(currentPassword)) { // if you later hash, update this compare
                main.post(() -> cb.onResult(false, "Current password is incorrect."));
                return;
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                main.post(() -> cb.onResult(false, "New password cannot be empty."));
                return;
            }
            userDAO.updatePassword(currentUsername, newPassword);
            main.post(() -> cb.onResult(true, "Password changed."));
        });
    }

    // CAMILA: update display name by userId (Settings screen)
    public void updateDisplayName(int userId, String name) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> userDAO.updateDisplayName(userId, name));
    }

    // CAMILA: update password by userId with hashed value (Settings + login upgrade)
    public void updatePasswordById(int userId, String hashedPassword) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> userDAO.updatePasswordById(userId, hashedPassword));
    }

    // CAMILA: soft delete (deactivate) current account by id (Settings -> Deactivate)
    public void softDeleteUserById(int userId) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> userDAO.softDeleteUser(userId));
    }

    // CAMILA: admin - reactivate a user by username
    public void reactivateUser(String username, ResultCallback cb) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            boolean exists = userDAO.exists(username) == 1;
            if (!exists) {
                main.post(() -> cb.onResult(false, "Username does not exist."));
                return;
            }
            int rows = userDAO.reactivateByUsername(username);
            main.post(() -> cb.onResult(rows > 0, rows > 0 ? "User reactivated." : "Nothing changed."));
        });
    }

    // CAMILA: admin - deactivate user by username, prevent deactivating the currently logged-in admin
    public void deactivateUserSafely(String usernameToDeactivate, String currentUsername, boolean currentIsAdmin, ResultCallback cb) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            if (currentIsAdmin && usernameToDeactivate.equals(currentUsername)) {
                main.post(() -> cb.onResult(false, "You can't deactivate the account you're logged in with."));
                return;
            }
            boolean exists = userDAO.exists(usernameToDeactivate) == 1;
            if (!exists) {
                main.post(() -> cb.onResult(false, "Username does not exist."));
                return;
            }
            int rows = userDAO.deactivateByUsername(usernameToDeactivate);
            main.post(() -> cb.onResult(rows > 0, rows > 0 ? "User deactivated." : "Nothing changed."));
        });
    }
}
