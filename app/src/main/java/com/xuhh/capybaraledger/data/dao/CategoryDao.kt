package com.xuhh.capybaraledger.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.xuhh.capybaraledger.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    // 插入分类
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category):Long

    // 查询所有分类
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    // 根据类型查询分类（支出或收入）
    @Query("SELECT * FROM categories WHERE type = :type")
    fun getCategoriesByType(type: Int): Flow<List<Category>>

    // 根据 ID 查询分类
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?

    // 根据精确名称查询分类（用于唯一性检查）
    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): Category

    // 批量插入分类
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Category>)
}