package com.stanissudo.jycs_crafters.database.typeConverters;

import androidx.room.TypeConverter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class LocalDataTypeConverter {
    @TypeConverter
    public  long convertDateToLong(LocalDateTime date){
        ZonedDateTime zdt = ZonedDateTime.of(date, ZoneId.systemDefault());
        return zdt.toInstant().toEpochMilli();

    }
    @TypeConverter
    public LocalDateTime convertLongToDate(Long epochMill){
        Instant instant = Instant.ofEpochMilli(epochMill);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}