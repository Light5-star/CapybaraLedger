package com.xuhh.capybaraledger.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 用户模型
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["userId"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,          // 用户ID（自定义，不可重复）
    val nickname: String? = userId,        // 昵称
    val avatar: String? = null,  // 头像URL
    val gender: Int = GENDER_UNKNOWN,         // 性别：0未知，1男，2女
    val birthday: Long? = null,  // 生日时间戳
    val createdAt: Long = System.currentTimeMillis(),  // 创建时间
    val updatedAt: Long = System.currentTimeMillis()   // 更新时间
) {
    companion object {
        const val GENDER_UNKNOWN = 0
        const val GENDER_MALE = 1
        const val GENDER_FEMALE = 2
    }
} 