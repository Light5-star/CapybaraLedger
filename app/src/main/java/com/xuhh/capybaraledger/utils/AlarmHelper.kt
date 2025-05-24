package com.xuhh.capybaraledger.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.xuhh.capybaraledger.data.model.Reminder
import com.xuhh.capybaraledger.data.model.ReminderRepeatType
import com.xuhh.capybaraledger.receiver.ReminderReceiver
import java.util.Calendar

object AlarmHelper {
    private const val ACTION_REMINDER = "com.xuhh.capybaraledger.action.REMINDER"

    fun scheduleReminder(context: Context, reminder: Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("reminder_name", reminder.name)
            putExtra("notify_type", reminder.notifyType.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = calculateNextTriggerTime(reminder)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                    pendingIntent
                )
            }
        } else {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                pendingIntent
            )
        }
    }

    private fun calculateNextTriggerTime(reminder: Reminder): Long {
        val (hours, minutes) = reminder.time.split(":").map { it.toInt() }
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 如果设置的时间已经过去，根据重复类型计算下一次触发时间
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            when (reminder.repeatType) {
                ReminderRepeatType.ONCE -> {
                    // 单次提醒，如果时间已过，设置为明天同一时间
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }
                ReminderRepeatType.DAILY -> {
                    // 每天提醒，设置为明天同一时间
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }
                ReminderRepeatType.CUSTOM -> {
                    // 自定义重复，找到下一个重复日
                    val today = calendar.get(Calendar.DAY_OF_WEEK)
                    val customDays = reminder.customDays ?: return calendar.timeInMillis
                    
                    // 转换自定义天数为 Calendar.DAY_OF_WEEK 格式
                    val mappedDays = customDays.map { day ->
                        when (day) {
                            7 -> Calendar.SUNDAY
                            else -> day + 1
                        }
                    }.sorted()

                    // 找到下一个重复日
                    val nextDay = mappedDays.find { it > today }
                        ?: mappedDays.first() // 如果没有更大的日期，取第一天
                    
                    // 计算需要添加的天数
                    val daysToAdd = if (nextDay > today) {
                        nextDay - today
                    } else {
                        7 - today + nextDay
                    }
                    
                    calendar.add(Calendar.DAY_OF_MONTH, daysToAdd)
                }
                ReminderRepeatType.DOUBLE_REST -> {
                    // 双休制，周一到周五
                    var daysToAdd = 1
                    while (true) {
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                        if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                            break
                        }
                    }
                }
                ReminderRepeatType.SINGLE_REST -> {
                    // 单休制，周一到周六
                    var daysToAdd = 1
                    while (true) {
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                        if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                            break
                        }
                    }
                }
                ReminderRepeatType.ALTERNATE_REST -> {
                    // 单双休，根据 isOddWeek 判断
                    val isOddWeek = reminder.isOddWeek ?: true
                    val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
                    val isCurrentOddWeek = currentWeek % 2 == 1
                    
                    var daysToAdd = 1
                    while (true) {
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                        val newWeek = calendar.get(Calendar.WEEK_OF_YEAR)
                        val isNewOddWeek = newWeek % 2 == 1
                        
                        // 检查是否是工作日
                        if (dayOfWeek != Calendar.SUNDAY && // 周日必休
                            (dayOfWeek != Calendar.SATURDAY || // 周六根据单双周判断
                             isNewOddWeek == isOddWeek)) {
                            break
                        }
                    }
                }
            }
        }

        return calendar.timeInMillis
    }

    fun cancelReminder(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra("reminder_id", reminderId)
            data = createUniqueUri(reminderId)
            addCategory(reminderId.toString())  // 添加相同的分类标识
        }
        
        val requestCode = generateUniqueRequestCode(reminderId)
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    private fun generateUniqueRequestCode(reminderId: Long): Int {
        // 使用更复杂的哈希算法
        return (reminderId.toString() + System.currentTimeMillis()).hashCode()
    }

    private fun createUniqueUri(reminderId: Long): android.net.Uri {
        return android.net.Uri.parse("reminder://${reminderId}/${System.currentTimeMillis()}")
    }
} 