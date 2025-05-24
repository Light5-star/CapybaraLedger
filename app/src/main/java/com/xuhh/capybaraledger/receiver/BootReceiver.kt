package com.xuhh.capybaraledger.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.utils.AlarmHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val appContext = context.applicationContext as App
            
            // 在协程中执行数据库操作
            CoroutineScope(Dispatchers.IO).launch {
                // 获取所有启用的提醒
                val reminders = appContext.reminderRepository.getAllEnabledReminders()
                
                // 重新设置所有提醒的闹钟
                reminders.forEach { reminder ->
                    AlarmHelper.scheduleReminder(appContext, reminder)
                }
            }
        }
    }
} 