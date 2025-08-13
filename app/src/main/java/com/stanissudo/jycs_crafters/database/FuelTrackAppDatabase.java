package com.stanissudo.jycs_crafters.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
// (kept)
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.stanissudo.jycs_crafters.database.entities.FuelEntry;
import com.stanissudo.jycs_crafters.database.entities.User;
import com.stanissudo.jycs_crafters.database.entities.Vehicle;
import com.stanissudo.jycs_crafters.database.typeConverters.LocalDateTypeConverter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@TypeConverters({LocalDateTypeConverter.class})
// CAMILA: bump version 2 -> 3 to add User.displayName
@Database(entities = {FuelEntry.class, User.class, Vehicle.class}, version = 3, exportSchema = false)
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
                            // CAMILA: register both migrations so users can upgrade 1->2 (isActive) and 2->3 (displayName)
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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
                // CAMILA: set default display name for new DBs
                admin.setDisplayName("Administrator");
                dao.insert(admin);

                User testUser = new User("testuser", "testuser");
                // CAMILA: set default display name for new DBs
                testUser.setDisplayName("Test User");
                dao.insert(testUser);
            });
        }
    };

    // CAMILA: existing non-destructive migration to add isActive (kept)
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // CAMILA: add 'isActive' with default true to existing users
            database.execSQL("ALTER TABLE " + USER_TABLE + " ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1");
        }
    };

    // CAMILA: new migration to add displayName and backfill from username
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // CAMILA: add new nullable column to avoid constraint issues on older devices
            db.execSQL("ALTER TABLE " + USER_TABLE + " ADD COLUMN displayName TEXT");
            // CAMILA: backfill displayName with username for all existing rows
            db.execSQL("UPDATE " + USER_TABLE + " SET displayName = username WHERE displayName IS NULL");
        }
    };

    public abstract FuelEntryDAO fuelEntryDAO();
    public abstract UserDAO userDAO();
    public abstract VehicleDAO vehicleDAO();

    //TODO: Add your DAO instances here
}
