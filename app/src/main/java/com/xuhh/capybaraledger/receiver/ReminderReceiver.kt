package com.xuhh.capybaraledger.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.xuhh.capybaraledger.data.model.ReminderNotifyType
import com.xuhh.capybaraledger.utils.NotificationHelper
import com.xuhh.capybaraledger.utils.AlarmHelper
import com.xuhh.capybaraledger.data.database.AppDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "ReminderReceiver"
        private const val WAKE_LOCK_TIMEOUT = 10000L // 10秒
    }

    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or
            PowerManager.ACQUIRE_CAUSES_WAKEUP or
            PowerManager.ON_AFTER_RELEASE,
            "CapybaraLedger:ReminderWakeLock"
        ).apply {
            acquire(30000) // 30秒
        }

        try {
            val reminderId = intent.getLongExtra("reminder_id", -1)
            val reminderName = intent.getStringExtra("reminder_name") ?: "记账提醒"
            val notifyType = intent.getStringExtra("notify_type")?.let {
                ReminderNotifyType.valueOf(it)
            } ?: ReminderNotifyType.NOTIFICATION

            // 先振动，确保即使通知失败也能提醒
            if (notifyType == ReminderNotifyType.VIBRATE || 
                notifyType == ReminderNotifyType.RING_VIBRATE) {
                vibrate(context)
            }

            // 显示通知
            NotificationHelper.showReminder(
                context = context,
                title = reminderName,
                notifyType = notifyType,
                id = reminderId.toInt()
            )

            // 重新调度下一次提醒
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val db = AppDatabase.getInstance(context)
                    val reminder = db.reminderDao().getReminder(reminderId)
                    if (reminder != null && reminder.isEnabled) {
                        AlarmHelper.scheduleReminder(context, reminder)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error rescheduling reminder", e)
                }
            }
        } finally {
            wakeLock.release()
        }
    }

    private fun vibrate(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = 
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // 创建一个振动效果：500ms振动，200ms暂停，重复3次
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 500, 200, 500, 200, 500),
                        -1 // 不重复
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), -1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating", e)
        }
    }
} 