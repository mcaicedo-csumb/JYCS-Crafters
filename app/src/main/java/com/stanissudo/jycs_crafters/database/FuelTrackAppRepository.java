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
    private final FuelEntryDAO fuelEntryDAO;
    private final UserDAO userDAO;
    private final VehicleDAO vehicleDAO;
    private LiveData<List<FuelEntry>> allLogs;
    private final Handler main = new Handler(Looper.getMainLooper());

    public interface OdometerCheckCallback {
        void onResult(boolean ok, @androidx.annotation.Nullable Integer prev,
                      @androidx.annotation.Nullable Integer next);
    }

    public void checkOdometerAsync(long logId, int carId, java.time.LocalDateTime when, int value,

                                   OdometerCheckCallback cb) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            Integer prev = fuelEntryDAO.getPreviousOdometer(logId, carId, when);
            Integer next = fuelEntryDAO.getNextOdometer(logId, carId, when);
            boolean ok = (prev == null || value > prev) && (next == null || value < next);
            main.post(() -> cb.onResult(ok, prev, next));
        });
    }

    private FuelTrackAppRepository(Application application) {
        FuelTrackAppDatabase db = FuelTrackAppDatabase.getDatabase(application);
        this.fuelEntryDAO = db.fuelEntryDAO();
        this.userDAO = db.userDAO();
        this.vehicleDAO = db.vehicleDAO();

        // CAMILA: one-time sweep to hash any legacy plaintext passwords
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            List<User> users = userDAO.getAllUsersList();
            if (users == null) return;
            for (User u : users) {
                String pw = u.getPassword();
                if (pw != null && !pw.matches("(?i)^[0-9a-f]{64}$")) {
                    userDAO.updatePasswordById(u.getId(), sha256(pw));
                }
            }
        });
    }

    /**
     * Returns a singleton instance of FuelTrackAppRepository.
     */
    public static FuelTrackAppRepository getRepository(Application application) {
        if (repository != null) {
            return repository;
        }
        Future<FuelTrackAppRepository> future = FuelTrackAppDatabase.databaseWriteExecutor.submit(
                new Callable<FuelTrackAppRepository>() {
                    @Override
                    public FuelTrackAppRepository call() {
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

    // =================== FuelEntry Methods ===================
    public void insertFuelEntry(FuelEntry fuelEntry) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> fuelEntryDAO.insertRecord(fuelEntry));
    }
    public void updateFuelEntry(FuelEntry fuelEntry) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> fuelEntryDAO.updateRecord(fuelEntry));
    }


    interface BoolCallback {
        void onResult(boolean ok);
    }

    public Integer getPreviousOdometer(long logId, int carId, LocalDateTime logDate) {
        return fuelEntryDAO.getPreviousOdometer(logId, carId, logDate);

    }

    public Integer getNextOdometer(long logId, int carId, LocalDateTime logDate) {
        return fuelEntryDAO.getNextOdometer(logId, carId, logDate);
    }

    public LiveData<List<FuelEntry>> getEntriesForCar(int carId) {
        return fuelEntryDAO.getEntriesForCar(carId);
    }

    public void deleteRecordByID(long carId) {
        fuelEntryDAO.deleteRecordById(carId);
    }
    public LiveData<FuelEntry> getRecordById(int logId) {
        return fuelEntryDAO.getRecordById(logId);
    }

    // =================== User Methods ===================
    public LiveData<User> getUserByUsername(String username) {
        return userDAO.getUserByUsername(username);
    }

    public LiveData<User> getUserById(int id) {
        return userDAO.getUserById(id);
    }

    public void insertUser(User user) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            // CAMILA: store hashed
            String pw = user.getPassword();
            if (pw != null && !pw.matches("(?i)^[0-9a-f]{64}$")) {
                user.setPassword(sha256(pw));
            }
            userDAO.insert(user);
        });
    }

    public void deleteAllUsers() {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(userDAO::deleteAll);
    }

    public void deleteUserByUsername(String username) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> userDAO.deleteByUsername(username));
    }

    public void updatePassword(String username, String newPassword) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            // CAMILA: hash on write
            String hashed = (newPassword != null && newPassword.matches("(?i)^[0-9a-f]{64}$"))
                    ? newPassword : sha256(newPassword);
            userDAO.updatePassword(username, hashed);
        });
    }

    // CAMILA: password by id (settings / sweep)
    public void updatePasswordById(int userId, String newPasswordHash) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            String hashed = (newPasswordHash != null && newPasswordHash.matches("(?i)^[0-9a-f]{64}$"))
                    ? newPasswordHash : sha256(newPasswordHash);
            userDAO.updatePasswordById(userId, hashed);
        });
    }

    // CAMILA: display name
    public void updateDisplayName(int userId, String newDisplayName) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> userDAO.updateDisplayName(userId, newDisplayName));
    }

    // CAMILA: soft delete (deactivate) self by id
    public void softDeleteUserById(int userId) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> userDAO.softDeleteUserById(userId));
    }

    public LiveData<List<User>> getAllUsers() {
        return userDAO.getAllUsers();
    }

//    public LiveData<List<FuelEntry>> getAllLogsByUserId(int loggedInUserId) {
//        return fuelEntryDAO.getRecordsById(loggedInUserId);
//    }

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
    public void deleteVehicleById(long vehicleID) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> vehicleDAO.deleteVehicleById(vehicleID));
    };

    public LiveData<CarCostStats> getCostStatsForVehicle(int vehicleId) {
        return fuelEntryDAO.getCostStatsForVehicle(vehicleId);
    }

    // ====== Callbacks ======
    public interface ExistsCallback { void onResult(boolean exists); }
    public interface ResultCallback { void onResult(boolean ok, String message); }

    public void userExistsAsync(String username, ExistsCallback cb) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            boolean exists = userDAO.exists(username) == 1;
            main.post(() -> cb.onResult(exists));
        });
    }

    public void changePasswordIfUserExists(String username, String newPassword, ResultCallback cb) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            boolean exists = userDAO.exists(username) == 1;
            if (!exists) {
                main.post(() -> cb.onResult(false, "Username does not exist."));
                return;
            }
            String hashed = (newPassword != null && newPassword.matches("(?i)^[0-9a-f]{64}$"))
                    ? newPassword : sha256(newPassword);
            userDAO.updatePassword(username, hashed);
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

    public void addUserSafely(String username, String password, boolean isAdmin, ResultCallback cb) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            boolean exists = userDAO.exists(username) == 1;
            if (exists) {
                main.post(() -> cb.onResult(false, "Username already exists."));
                return;
            }
            User u = new User(username, password);
            u.setAdmin(isAdmin);
            String pw = u.getPassword();
            if (pw != null && !pw.matches("(?i)^[0-9a-f]{64}$")) {
                u.setPassword(sha256(pw));
            }
            userDAO.insert(u);
            main.post(() -> cb.onResult(true, "User added."));
        });
    }

    public void changePasswordWithCurrentCheck(String currentUsername,
                                               String currentPassword,
                                               String newPassword,
                                               ResultCallback cb) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            String stored = userDAO.getPasswordForUsername(currentUsername); // CAMILA
            if (stored == null) {
                main.post(() -> cb.onResult(false, "Session error: user not found."));
                return;
            }
            boolean storedIsHash = stored.matches("(?i)^[0-9a-f]{64}$");     // CAMILA
            String curHash = sha256(currentPassword);                        // CAMILA
            boolean ok = storedIsHash ? stored.equalsIgnoreCase(curHash) : stored.equals(currentPassword);
            if (!ok) {
                main.post(() -> cb.onResult(false, "Current password is incorrect."));
                return;
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                main.post(() -> cb.onResult(false, "New password cannot be empty."));
                return;
            }
            userDAO.updatePassword(currentUsername, sha256(newPassword));    // CAMILA
            main.post(() -> cb.onResult(true, "Password changed."));
        });
    }

    // CAMILA: admin-safe deactivate/reactivate by username
    public void deactivateUserSafely(String usernameToDeactivate,
                                     String currentUsername,
                                     boolean currentIsAdmin,
                                     ResultCallback cb) {
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
            userDAO.deactivateByUsername(usernameToDeactivate);
            main.post(() -> cb.onResult(true, "User deactivated."));
        });
    }

    public void reactivateUserSafely(String usernameToReactivate,
                                     String currentUsername,
                                     boolean currentIsAdmin,
                                     ResultCallback cb) {
        FuelTrackAppDatabase.databaseWriteExecutor.execute(() -> {
            boolean exists = userDAO.exists(usernameToReactivate) == 1;
            if (!exists) {
                main.post(() -> cb.onResult(false, "Username does not exist."));
                return;
            }
            userDAO.reactivateByUsername(usernameToReactivate);
            main.post(() -> cb.onResult(true, "User reactivated."));
        });
    }

    // CAMILA: SHA-256 helper
    private static String sha256(String s) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
