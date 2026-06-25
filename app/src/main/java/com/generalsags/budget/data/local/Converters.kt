package com.generalsags.budget.data.local

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    @TypeConverter
    fun fromDate(date: LocalDate): String {
        return date.format(dateFormatter)
    }

    @TypeConverter
    fun toDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString, dateFormatter)
    }

    @TypeConverter
    fun fromTime(time: LocalTime): String {
        return time.format(timeFormatter)
    }

    @TypeConverter
    fun toTime(timeString: String): LocalTime {
        return LocalTime.parse(timeString, timeFormatter)
    }
}
