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
// Camila: added Vehicle so I can build the admin Vehicle Review (soft delete / restore)
import com.stanissudo.jycs_crafters.database.entities.Vehicle;

// NOTE: project uses LocalDataTypeConverter (not LocalDateTypeConverter)
import com.stanissudo.jycs_crafters.database.typeConverters.LocalDataTypeConverter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@TypeConverters(LocalDataTypeConverter.class)
// Camila: bumped DB version because I added the Vehicle table + isActive flag
@Database(entities = { FuelEntry.class, User.class, Vehicle.class }, version = 2, exportSchema = false)
public abstract class FuelTrackAppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "FuelTrackDatabase";
    public static final String FUEL_LOG_TABLE = "FuelEntryTable"; // entity uses this
    // Camila: align with @Entity(tableName = "user_table") in User.java
    public static final String USER_TABLE = "user_table";

    private static volatile FuelTrackAppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static FuelTrackAppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FuelTrackAppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    FuelTrackAppDatabase.class,
                                    DATABASE_NAME
                            )
                            // Camila: keep migration (dev), but fixed table names to match entities
                            .addMigrations(MIGRATION_1_2)
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

                User admin = new User("admin", "admin", true);
                admin.setAdmin(true);
                dao.insert(admin);

                User testUser = new User("testuser", "testuser", false);
                dao.insert(testUser);
            });
        }
    };

    // Camila: fixed to create FuelEntryTable + user_table (match @Entity names), plus vehicles
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Recreate FuelEntry table
            database.execSQL("DROP TABLE IF EXISTS " + FUEL_LOG_TABLE);
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS " + FUEL_LOG_TABLE + " (" +
                            "LogID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "CarID INTEGER NOT NULL, " +
                            "logDate INTEGER NOT NULL, " +          // stored via TypeConverter
                            "Odometer INTEGER NOT NULL, " +
                            "Gallons REAL NOT NULL, " +
                            "PricePerGallon REAL NOT NULL, " +
                            "TotalCost REAL NOT NULL, " +
                            "Location TEXT)"
            );

            // Recreate User table (lowercase name to match @Entity)
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS " + USER_TABLE + " (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "username TEXT NOT NULL, " +
                            "password TEXT NOT NULL, " +
                            "isAdmin INTEGER NOT NULL DEFAULT 0, " +
                            "isActive INTEGER NOT NULL DEFAULT 1" +
                            ")"
            );

            // Vehicles table for admin review (soft delete via isActive)
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS vehicles (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "userId INTEGER NOT NULL, " +
                            "name TEXT, " +
                            "year INTEGER, " +
                            "make TEXT, " +
                            "model TEXT, " +
                            "isActive INTEGER NOT NULL DEFAULT 1, " +
                            "FOREIGN KEY(userId) REFERENCES " + USER_TABLE + "(id) ON DELETE CASCADE" +
                            ")"
            );
            // Optional index:
            // database.execSQL("CREATE INDEX IF NOT EXISTS index_vehicles_userId ON vehicles(userId)");
        }
    };

    public abstract FuelEntryDAO fuelEntryDAO();
    public abstract UserDAO userDAO();

    // Camila: added VehicleDAO so Admin screens can act on vehicles
    public abstract VehicleDAO vehicleDAO();
}
