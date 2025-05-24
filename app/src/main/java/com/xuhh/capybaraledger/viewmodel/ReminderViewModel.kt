package com.xuhh.capybaraledger.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.xuhh.capybaraledger.data.model.Reminder
import com.xuhh.capybaraledger.data.model.ReminderNotifyType
import com.xuhh.capybaraledger.data.model.ReminderRepeatType
import com.xuhh.capybaraledger.data.repository.ReminderRepository
import com.xuhh.capybaraledger.utils.AlarmHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val repository: ReminderRepository,
    private val context: Context
) : ViewModel() {

    // 所有提醒列表
    val reminders: StateFlow<List<Reminder>> = repository.getAllReminders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 创建提醒
    fun createReminder(
        name: String,
        time: String,
        repeatType: ReminderRepeatType,
        customDays: List<Int>? = null,
        isOddWeek: Boolean? = null,
        notifyType: ReminderNotifyType
    ) {
        viewModelScope.launch {
            val reminder = Reminder(
                id = 0,  // 让数据库自动生成ID
                name = name,
                time = time,
                repeatType = repeatType,
                customDays = customDays,
                isOddWeek = isOddWeek,
                notifyType = notifyType,
                isEnabled = true
            )
            val newId = repository.insertReminder(reminder)
            // 设置闹钟
            AlarmHelper.scheduleReminder(context, reminder.copy(id = newId))
        }
    }

    // 更新提醒
    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
            // 更新闹钟
            AlarmHelper.scheduleReminder(context, reminder)
        }
    }

    // 删除提醒
    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            // 取消闹钟
            AlarmHelper.cancelReminder(context, reminder.id)
        }
    }

    // 更新提醒启用状态
    fun updateReminderEnabled(id: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            // 先取消现有的闹钟
            if (!isEnabled) {
                AlarmHelper.cancelReminder(context, id)
            }
            
            // 更新数据库
            repository.updateReminderEnabled(id, isEnabled)
            
            // 如果是启用，获取完整的提醒信息后再设置闹钟
            if (isEnabled) {
                repository.getReminder(id)?.let { reminder ->
                    AlarmHelper.scheduleReminder(context, reminder)
                }
            }
        }
    }

    // 获取重复类型的显示文本
    fun getRepeatTypeText(reminder: Reminder): String {
        return when (reminder.repeatType) {
            ReminderRepeatType.ONCE -> "仅一次"
            ReminderRepeatType.DAILY -> "每天"
            ReminderRepeatType.DOUBLE_REST -> "双休制"
            ReminderRepeatType.SINGLE_REST -> "单休制"
            ReminderRepeatType.ALTERNATE_REST -> {
                if (reminder.isOddWeek == true) "单双休(本周为单周)" else "单双休(本周为双周)"
            }
            ReminderRepeatType.CUSTOM -> {
                val days = reminder.customDays ?: return "自定义"
                val dayNames = days.map { 
                    when (it) {
                        1 -> "周一"
                        2 -> "周二"
                        3 -> "周三"
                        4 -> "周四"
                        5 -> "周五"
                        6 -> "周六"
                        7 -> "周日"
                        else -> ""
                    }
                }
                "每${dayNames.joinToString("、")}"
            }
        }
    }

    // 获取提醒方式的显示文本
    fun getNotifyTypeText(notifyType: ReminderNotifyType): String {
        return when (notifyType) {
            ReminderNotifyType.NOTIFICATION -> "通知"
            ReminderNotifyType.RING -> "响铃"
            ReminderNotifyType.VIBRATE -> "振动"
            ReminderNotifyType.RING_VIBRATE -> "响铃和振动"
        }
    }

    suspend fun getReminder(id: Long): Reminder? {
        return repository.getReminder(id)
    }

    // ViewModel Factory
    class Factory(private val repository: ReminderRepository, private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ReminderViewModel(repository, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 