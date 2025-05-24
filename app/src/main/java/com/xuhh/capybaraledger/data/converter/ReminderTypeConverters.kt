package com.xuhh.capybaraledger.data.converter

import androidx.room.TypeConverter
import com.xuhh.capybaraledger.data.model.ReminderNotifyType
import com.xuhh.capybaraledger.data.model.ReminderRepeatType

class ReminderTypeConverters {
    @TypeConverter
    fun fromRepeatType(value: ReminderRepeatType): String {
        return value.name
    }

    @TypeConverter
    fun toRepeatType(value: String): ReminderRepeatType {
        return ReminderRepeatType.valueOf(value)
    }

    @TypeConverter
    fun fromNotifyType(value: ReminderNotifyType): String {
        return value.name
    }

    @TypeConverter
    fun toNotifyType(value: String): ReminderNotifyType {
        return ReminderNotifyType.valueOf(value)
    }

    @TypeConverter
    fun fromCustomDays(value: String): List<Int> {
        return if (value.isEmpty()) {
            emptyList()
        } else {
            value.split(",").map { it.toInt() }
        }
    }

    @TypeConverter
    fun toCustomDays(value: List<Int>): String {
        return value.joinToString(",")
    }
} 