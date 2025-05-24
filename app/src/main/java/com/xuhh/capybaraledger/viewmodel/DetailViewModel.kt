package com.xuhh.capybaraledger.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class DetailViewModel : ViewModel() {
    private val _calendar = MutableLiveData<Calendar>().apply {
        value = Calendar.getInstance()
    }
    val calendar: LiveData<Calendar> = _calendar

    // 获取当前日期字符串
    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(_calendar.value?.time ?: Date())
    }

    init {
        // 初始化时设置为当月1号0点
        val initialCalendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        _calendar.value = initialCalendar
    }

    fun nextMonth() {
        val currentCalendar = _calendar.value ?: return
        Log.d("DetailViewModel", "nextMonth: before=${currentCalendar.get(Calendar.MONTH)}")
        
        val newCalendar = currentCalendar.clone() as Calendar
        newCalendar.add(Calendar.MONTH, 1)
        Log.d("DetailViewModel", "nextMonth: after=${newCalendar.get(Calendar.MONTH)}")
        _calendar.value = newCalendar
    }

    fun backMonth() {
        val currentCalendar = _calendar.value ?: return
        Log.d("DetailViewModel", "backMonth: before=${currentCalendar.get(Calendar.MONTH)}")
        
        val newCalendar = currentCalendar.clone() as Calendar
        newCalendar.add(Calendar.MONTH, -1)
        Log.d("DetailViewModel", "backMonth: after=${newCalendar.get(Calendar.MONTH)}")
        _calendar.value = newCalendar
    }

    fun getCurrentMonthRange(): Pair<Long, Long> {
        val currentCalendar = _calendar.value?.clone() as? Calendar ?: return Pair(0L, 0L)
        val year = currentCalendar.get(Calendar.YEAR)
        val month = currentCalendar.get(Calendar.MONTH)
        Log.d("DetailViewModel", "Getting range for year=$year, month=$month")

        // 设置为月初
        val startCalendar = Calendar.getInstance().apply {
            clear()  // 清除所有字段
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = startCalendar.timeInMillis

        // 设置为月末
        val endCalendar = Calendar.getInstance().apply {
            clear()  // 清除所有字段
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }
        val endTime = endCalendar.timeInMillis

        Log.d("DetailViewModel", "Time range for month $month: ${startCalendar.time} to ${endCalendar.time}")
        return Pair(startTime, endTime)
    }
}
