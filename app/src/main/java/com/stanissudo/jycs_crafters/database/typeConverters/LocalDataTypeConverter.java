/**
 * Utility class for converting between {@link LocalDateTime} and {@code long} epoch time
 * for use with Room database storage.
 * <p>
 * Room does not support complex types like {@code LocalDateTime} natively,
 * so this converter enables storing date-time values as epoch milliseconds.
 * </p>
 *
 * @author Stan Permiakov
 * @version 1.0
 * @since 2025-07-31
 */

package com.stanissudo.jycs_crafters.database.typeConverters;

import androidx.room.TypeConverter;

import java.time.Instant;
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