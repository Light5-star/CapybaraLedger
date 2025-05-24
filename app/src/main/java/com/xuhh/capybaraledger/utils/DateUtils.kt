package com.xuhh.capybaraledger.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// DateUtils.kt
object DateUtils {
    fun getDayStartEndTimestamps(date: String): Pair<Long, Long> {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateObj = format.parse(date) ?: Date()
        val calendar = Calendar.getInstance().apply {
            time = dateObj
        }

        // 获取当天00:00:00
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val start = calendar.timeInMillis

        // 获取当天23:59:59
        calendar.add(Calendar.DATE, 1)
        val end = calendar.timeInMillis - 1

        return Pair(start, end)
    }
}