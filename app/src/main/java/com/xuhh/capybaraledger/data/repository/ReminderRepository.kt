package com.xuhh.capybaraledger.data.repository

import com.xuhh.capybaraledger.data.dao.ReminderDao
import com.xuhh.capybaraledger.data.model.Reminder
import kotlinx.coroutines.flow.Flow

class ReminderRepository(
    private val reminderDao: ReminderDao
) {
    // 获取所有提醒，按时间排序
    fun getAllReminders(): Flow<List<Reminder>> {
        return reminderDao.getAllReminders()
    }

    // 获取单个提醒
    suspend fun getReminder(id: Long): Reminder? {
        return reminderDao.getReminder(id)
    }

    // 添加提醒
    suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(reminder)
    }

    // 更新提醒
    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    // 删除提醒
    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
    }

    // 更新提醒的启用状态
    suspend fun updateReminderEnabled(id: Long, isEnabled: Boolean) {
        reminderDao.updateReminderEnabled(id, isEnabled)
    }

    suspend fun getAllEnabledReminders(): List<Reminder> {
        return reminderDao.getAllEnabledReminders()
    }
} 