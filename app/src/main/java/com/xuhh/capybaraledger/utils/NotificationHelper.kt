package com.xuhh.capybaraledger.utils

import android.Manifest
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.xuhh.capybaraledger.MainActivity
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.model.ReminderNotifyType

object NotificationHelper {
    private const val CHANNEL_ID = "reminder_channel"
    private const val CHANNEL_NAME = "记账提醒"
    private var notificationId = 0  // 改为变量，用于生成唯一ID

    // 添加振动模式常量
    private val VIBRATE_PATTERN_FOREGROUND = longArrayOf(0, 500) // 单次振动500ms
    private val VIBRATE_PATTERN_BACKGROUND = longArrayOf(0, 500, 200, 500, 200, 500) // 三次振动

    // 添加提醒文本列表
    private val reminderTitles = listOf(
        "卡皮提醒你",
        "记账小助手",
        "该记账啦",
        "今日份的记账提醒",
        "别忘记记账哦"
    )

    private val reminderContents = listOf(
        "今天花了多少呢？快来记一下吧！",
        "记录一下今天的收支，让生活更有规划～",
        "水豚在等你来记录今天的账单呢",
        "养成记账习惯，从现在开始！",
        "不要让今天的账单溜走了，快来记录一下吧",
        "点击进入，轻松记录今天的收支",
        "今天也要好好记账哦，财务自由从记账开始"
    )

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT  // 降低重要性级别
            ).apply {
                description = "记账提醒通知"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = 
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showReminder(context: Context, title: String, notifyType: ReminderNotifyType, id: Int) {
        createNotificationChannel(context)

        // 创建普通 Intent，但不立即打开
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.capybara)
            .setContentTitle(title)
            .setContentText("该记账啦！")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)  // 改为提醒类别
            .setAutoCancel(true)  // 点击后自动消失
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(false)  // 改为非持续通知
            .setDefaults(getNotificationDefaults(notifyType))
            // 添加声音和振动
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))  // 使用通知音而不是闹钟音
            .setVibrate(longArrayOf(0, 500, 200, 500))  // 减少振动强度
            .build()

        // 移除强制性标志
        notification.flags = notification.flags or 
            Notification.FLAG_AUTO_CANCEL  // 点击后自动消失

        val notificationManager = 
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, notification)
    }

    private fun getNotificationDefaults(notifyType: ReminderNotifyType): Int {
        return when (notifyType) {
            ReminderNotifyType.NOTIFICATION -> NotificationCompat.DEFAULT_ALL
            ReminderNotifyType.RING -> NotificationCompat.DEFAULT_SOUND
            ReminderNotifyType.VIBRATE -> NotificationCompat.DEFAULT_VIBRATE
            ReminderNotifyType.RING_VIBRATE -> NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE
        }
    }

    private fun vibrate(context: Context, pattern: LongArray) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    private fun isAppForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName

        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }

    // 生成唯一的通知ID
    private fun generateNotificationId(): Int {
        return notificationId++
    }

    // 清除所有通知
    fun clearAllNotifications(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
} 