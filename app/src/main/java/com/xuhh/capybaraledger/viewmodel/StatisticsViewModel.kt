package com.xuhh.capybaraledger.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Calendar

class StatisticsViewModel : ViewModel() {
    private val _calendar = MutableLiveData<Calendar>()
    val calendar: LiveData<Calendar> = _calendar

    private val _currentLedgerId = MutableLiveData<Long>(1L)
    val currentLedgerId: LiveData<Long> = _currentLedgerId

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
        Log.d("StatisticsViewModel", "nextMonth: before=${currentCalendar.get(Calendar.MONTH)}")
        
        val newCalendar = currentCalendar.clone() as Calendar
        newCalendar.add(Calendar.MONTH, 1)
        Log.d("StatisticsViewModel", "nextMonth: after=${newCalendar.get(Calendar.MONTH)}")
        _calendar.value = newCalendar
    }

    fun backMonth() {
        val currentCalendar = _calendar.value ?: return
        Log.d("StatisticsViewModel", "backMonth: before=${currentCalendar.get(Calendar.MONTH)}")
        
        val newCalendar = currentCalendar.clone() as Calendar
        newCalendar.add(Calendar.MONTH, -1)
        Log.d("StatisticsViewModel", "backMonth: after=${newCalendar.get(Calendar.MONTH)}")
        _calendar.value = newCalendar
    }

    fun getCurrentMonthRange(): Pair<Long, Long> {
        val currentCalendar = _calendar.value?.clone() as? Calendar ?: return Pair(0L, 0L)
        val year = currentCalendar.get(Calendar.YEAR)
        val month = currentCalendar.get(Calendar.MONTH)
        Log.d("StatisticsViewModel", "Getting range for year=$year, month=$month")

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

        Log.d("StatisticsViewModel", "Time range for month $month: ${startCalendar.time} to ${endCalendar.time}")
        return Pair(startTime, endTime)
    }

    fun setLedgerId(ledgerId: Long) {
        _currentLedgerId.value = ledgerId
    }
}
