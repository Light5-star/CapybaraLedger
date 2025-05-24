package com.xuhh.capybaraledger.data.model

import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.xuhh.capybaraledger.R

/**
 * 类别模型
 * 类别的数据
 */
@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)]
)
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: Int,
    @StringRes val iconResId: Int
) {
    companion object {
        const val TYPE_EXPENSE = 0
        const val TYPE_INCOME = 1
    }
}

object Categories {
    // 支出类别
    val EXPENSE_CATEGORIES = listOf(
        Category(name = "餐饮", type = 0,iconResId= R.string.icon_canyin),
        Category(name = "零食", type = 0,iconResId= R.string.icon_lingshi),
        Category(name = "水果", type = 0,iconResId= R.string.icon_shuiguo),
        Category(name = "蔬菜", type = 0,iconResId= R.string.icon_shucai),
        Category(name = "饮品", type = 0,iconResId= R.string.icon_drink),
        Category(name = "日用品", type = 0,iconResId= R.string.icon_riyongpin),
        Category(name = "交通", type = 0,iconResId= R.string.icon_jiaotong_xinxi),
        Category(name = "住房", type = 0,iconResId= R.string.icon_zhufang),
        Category(name = "通讯", type = 0,iconResId= R.string.icon_dianhua),
        Category(name =  "购物", type = 0,iconResId= R.string.icon_gouwuche),
        Category(name =  "数码", type = 0,iconResId= R.string.icon_shuma_jiadian_leimu),
        Category(name =  "医疗", type = 0,iconResId= R.string.icon_yiliao),
        Category(name =  "教育", type = 0,iconResId= R.string.icon_jiaoyu),
        Category(name =  "办公", type = 0,iconResId= R.string.icon_bangong),
        Category(name =  "社交", type = 0,iconResId= R.string.icon_jiating_guanxi),
        Category(name =  "娱乐", type = 0,iconResId= R.string.icon_fanyule),
        Category(name =  "运动", type = 0,iconResId= R.string.icon_yundong),
        Category(name =  "美容", type = 0,iconResId= R.string.icon_meirong),
        Category(name =  "宠物", type = 0,iconResId= R.string.icon_chongwu),
        Category(name =  "游戏", type = 0,iconResId= R.string.icon_youxi),
        Category(name =  "汽车", type = 0,iconResId= R.string.icon_qiche_001),
        Category(name =  "快递", type = 0,iconResId= R.string.icon_kuaidi),
        Category(name =  "彩票", type = 0,iconResId= R.string.icon_caipiao),
        Category(name =  "捐赠", type = 0,iconResId= R.string.icon_aixin_juanzeng),
        Category(name =  "礼物", type = 0,iconResId= R.string.icon_gift),
        Category(name =  "其他", type = 0,iconResId= R.string.icon_category)
    )

    // 收入类别
    val INCOME_CATEGORIES = listOf(
        Category(name =  "工资", type = 1,iconResId= R.string.icon_gongzi_jianyi),
        Category(name =  "奖金", type = 1,iconResId= R.string.icon_jiangjin_guize),
        Category(name =  "红包", type = 1,iconResId= R.string.icon_hongbao),
        Category(name =  "理财", type = 1,iconResId= R.string.icon_licai),
        Category(name =  "分红", type = 1,iconResId= R.string.icon_gudong_fenhong),
        Category(name =  "其他", type = 1,iconResId= R.string.icon_category)
    )

    // 获取所有类别
    fun getAllCategories() = EXPENSE_CATEGORIES + INCOME_CATEGORIES

    // 根据类型获取类别
    fun getCategoriesByType(type: Int) = 
        if (type == 0) EXPENSE_CATEGORIES else INCOME_CATEGORIES

    // 根据ID获取类别
    fun getCategoryById(id: Long) = getAllCategories().find { it.id == id }

} 