package com.stanissudo.jycs_crafters.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.stanissudo.jycs_crafters.database.entities.FuelEntry;
import com.stanissudo.jycs_crafters.database.entities.User;
import com.stanissudo.jycs_crafters.database.typeConverters.LocalDataTypeConverter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@TypeConverters(LocalDataTypeConverter.class)
@Database(entities = {FuelEntry.class, User.class}, version = 2, exportSchema = false)
public abstract class FuelTrackAppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "FuelTrackDatabase";

    public static final String FUEL_LOG_TABLE = "FuelEntryTable";
    public static final String USER_TABLE = "UserTable";

    private static volatile FuelTrackAppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static FuelTrackAppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FuelTrackAppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    FuelTrackAppDatabase.class, DATABASE_NAME)
                            .addCallback(addDefaultValues)
                            .fallbackToDestructiveMigration()
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

    public abstract FuelEntryDAO fuelEntryDAO();
    public abstract UserDAO userDAO();
}
