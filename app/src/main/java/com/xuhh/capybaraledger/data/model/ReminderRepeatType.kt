package com.xuhh.capybaraledger.data.model

enum class ReminderRepeatType {
    ONCE,           // 仅一次
    DAILY,          // 每天
    DOUBLE_REST,    // 双休制 (周六周日休息)
    SINGLE_REST,    // 单休制 (周日休息)
    ALTERNATE_REST, // 单双休 (根据单双周决定是否周六休息)
    CUSTOM          // 自定义 (自定义每周哪几天提醒)
} 