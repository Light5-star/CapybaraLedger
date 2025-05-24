package com.xuhh.capybaraledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 提醒名称
    val name: String,
    
    // 提醒时间 (24小时制，格式: HH:mm)
    val time: String,
    
    // 重复类型
    val repeatType: ReminderRepeatType,
    
    // 自定义重复的星期几 (仅当repeatType为CUSTOM时有效)
    // 使用List<Int>存储，数字1-7代表周一到周日
    val customDays: List<Int>? = null,
    
    // 单双休时是否本周为单周 (仅当repeatType为ALTERNATE_REST时有效)
    val isOddWeek: Boolean? = null,
    
    // 提醒方式
    val notifyType: ReminderNotifyType,
    
    // 是否启用
    val isEnabled: Boolean = true,
    
    // 创建时间
    val createTime: Long = System.currentTimeMillis()
) 