package com.xuhh.capybaraledger.data.repository

import com.xuhh.capybaraledger.data.dao.UserDao
import com.xuhh.capybaraledger.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDao) {

    // 创建用户
    suspend fun createUser(user: User): Long = withContext(Dispatchers.IO) {
        userDao.insert(user)
    }

    // 更新用户信息
    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        userDao.update(user)
    }

    // 根据ID获取用户
    suspend fun getUserById(id: Long): User? = withContext(Dispatchers.IO) {
        userDao.getUserById(id)
    }

    // 根据用户ID获取用户
    suspend fun getUserByUserId(userId: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserByUserId(userId)
    }

    // 获取所有用户的Flow
    fun getAllUsersFlow(): Flow<List<User>> {
        return userDao.getAllUsersFlow()
    }

    // 检查用户ID是否已存在
    suspend fun isUserIdExists(userId: String): Boolean = withContext(Dispatchers.IO) {
        userDao.isUserIdExists(userId) > 0
    }
} 