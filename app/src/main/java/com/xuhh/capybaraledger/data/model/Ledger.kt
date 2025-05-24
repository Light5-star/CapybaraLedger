package com.xuhh.capybaraledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 账本模型
 */
@Entity(tableName = "ledger")
data class Ledger(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,   //id
    val name: String,   //账本名称
    val description: String? = null,    //账本描述
    val icon: Int = 0,   //账本图标
    val color: Int = 0, //账本颜色
    val isDefault: Boolean = false, //是否默认账本
    val sortOrder: Int = 0,  //排序编号
    var currency: Boolean = false
) 