package com.stanissudo.jycs_crafters.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.stanissudo.jycs_crafters.database.entities.FuelEntry;
import com.stanissudo.jycs_crafters.database.entities.User;
import com.stanissudo.jycs_crafters.database.entities.Vehicle;
import com.stanissudo.jycs_crafters.database.typeConverters.LocalDateTypeConverter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@TypeConverters({LocalDateTypeConverter.class})
// CAMILA: bump DB version to include users.isActive column (1 -> 2)
@Database(entities = {FuelEntry.class, User.class, Vehicle.class}, version = 2, exportSchema = false)
public abstract class FuelTrackAppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "FuelTrackDatabase";
    public static final String FUEL_LOG_TABLE = "FuelEntryTable";
    public static final String USER_TABLE = "UserTable";
    public static final String VEHICLE_TABLE = "VehicleTable";

    //TODO: Add more tables here
    private static volatile FuelTrackAppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static FuelTrackAppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FuelTrackAppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    FuelTrackAppDatabase.class, DATABASE_NAME)
                            // CAMILA: register migration that adds users.isActive without dropping tables
                            .addMigrations(MIGRATION_1_2) // Only for Development Environment
                            .addCallback(addDefaultValues)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback addDefaultValues = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                UserDAO dao = INSTANCE.userDAO();
                dao.deleteAll();

                User admin = new User("admin", "admin");
                admin.setAdmin(true);
                dao.insert(admin);

                User testUser = new User("testuser", "testuser");
                dao.insert(testUser);
            });
        }
    };

//    private static final RoomDatabase.Callback addDefaultValues = new RoomDatabase.Callback() {
//        @Override
//        public void onCreate(@org.jspecify.annotations.NonNull SupportSQLiteDatabase db) {
//            super.onCreate(db);
//            //TODO: Add logic here
//            //// Log.i(MainActivity.TAG, "DATABASE CREATED!");
//            //// databaseWriteExecutor.execute(() -> {
//            ////     UserDAO dao = INSTANCE.userDAO();
//            ////     dao.deleteAll();
//            ////     User admin = new User("admin", "admin");
//            ////     admin.setAdmin(true);
//            ////     dao.insert(admin);
//            ////
//            ////     User testUser = new User("testuser", "testuser");
//            ////     dao.insert(testUser);
//            // });
//        }
//    };

    // CAMILA: convert MIGRATION_1_2 to a non-destructive migration that adds the new 'isActive' column.
    //         We ALTER the table named in USER_TABLE ("UserTable"), which matches your @Entity tableName.
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // CAMILA: Add 'isActive' to users with a default of 1 (true) and NOT NULL
            database.execSQL("ALTER TABLE " + USER_TABLE + " ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1");

            // --- Original dev-only destructive migration (commented to avoid schema mismatch) ---
            // NOTE: These were dropping/creating tables with names that don't match your entity constants.
            // Keeping for reference but not executing to prevent runtime crashes.
            //
            // // Drop old tables
            // // database.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
            // // database.execSQL("DROP TABLE IF EXISTS " + VEHICLE_TABLE);
            // // database.execSQL("DROP TABLE IF EXISTS " + FUEL_LOG_TABLE);
            //
            // // Recreate (names below didn't match your constants and would break Room)
            // // database.execSQL(
            // //         "CREATE TABLE IF NOT EXISTS fuelLogTable (" +
            // //                 "LogID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            // //                 "CarID INTEGER NOT NULL, " +
            // //                 "logDate INTEGER NOT NULL, " +
            // //                 "Odometer INTEGER NOT NULL, " +
            // //                 "Gallons REAL NOT NULL, " +
            // //                 "PricePerGallon REAL NOT NULL, " +
            // //                 "TotalCost REAL NOT NULL, " +
            // //                 "Location TEXT)"
            // // );
            // // database.execSQL(
            // //         "CREATE TABLE IF NOT EXISTS User (" +
            // //                 "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            // //                 "username TEXT NOT NULL, " +
            // //                 "password TEXT NOT NULL, " +
            // //                 "isAdmin INTEGER NOT NULL DEFAULT 0" +
            // //                 ")"
            // // );
            // // database.execSQL(
            // //         "CREATE TABLE IF NOT EXISTS Vehicle (" +
            // //                 "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            // //                 "Name TEXT NOT NULL, " +
            // //                 "Make TEXT NOT NULL, " +
            // //                 "Model TEXT NOT NULL, " +
            // //                 "Year INTEGER NOT NULL" +
            // //                 ")"
            // // );
        }
    };

    public abstract FuelEntryDAO fuelEntryDAO();
    public abstract UserDAO userDAO();
    public abstract VehicleDAO vehicleDAO();

    //TODO: Add your DAO instances here
}
