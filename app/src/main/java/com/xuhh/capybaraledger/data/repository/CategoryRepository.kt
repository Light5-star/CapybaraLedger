package com.xuhh.capybaraledger.data.repository

import com.xuhh.capybaraledger.data.dao.CategoryDao
import com.xuhh.capybaraledger.data.model.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CategoryRepository (
    private val categoryDao: CategoryDao
) {
    // 插入分类（处理唯一性冲突）
    suspend fun insertCategory(category: Category): Boolean = withContext(Dispatchers.IO) {
        try {
            categoryDao.insert(category).toInt() != -1
        } catch (e: Exception) {
            false  // 唯一性冲突或其他异常
        }
    }

    // 获取所有分类（Flow 实时更新）
    suspend fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    // 根据类型获取分类（支出/收入）
    fun getCategoriesByType(type: Int): Flow<List<Category>> =
        categoryDao.getCategoriesByType(type)

    // 根据 ID 获取分类（可选）
    suspend fun getCategoryById(id: Long): Category? = withContext(Dispatchers.IO) {
        categoryDao.getCategoryById(id)
    }

    // 检查名称是否已存在（用于 UI 实时验证）
    suspend fun isCategoryNameExists(name: String): Boolean = withContext(Dispatchers.IO) {
        categoryDao.getCategoryByName(name) != null
    }
}