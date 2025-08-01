package com.stanissudo.jycs_crafters.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.stanissudo.jycs_crafters.database.entities.FuelEntry;
import com.stanissudo.jycs_crafters.database.typeConverters.LocalDataTypeConverter;

@TypeConverters(LocalDataTypeConverter.class)
@Database(entities = {FuelEntry.class}, version = 1, exportSchema = false)
public abstract class FuelEntryDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "FuelTrackDatabase"; //Needs to move to master table
    public static final String FUEL_LOG_TABLE = "FuelEntryTable";
}
