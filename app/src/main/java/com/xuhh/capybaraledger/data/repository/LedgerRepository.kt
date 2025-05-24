package com.xuhh.capybaraledger.data.repository

import com.xuhh.capybaraledger.data.dao.LedgerDao
import com.xuhh.capybaraledger.data.model.Ledger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LedgerRepository(private val ledgerDao: LedgerDao) {

    // 创建账本
    suspend fun createLedger(ledger: Ledger): Long = withContext(Dispatchers.IO) {
        ledgerDao.insert(ledger)
    }

    // 更新账本信息
    suspend fun updateLedger(ledger: Ledger) = withContext(Dispatchers.IO) {
        ledgerDao.update(ledger)
    }

    // 根据ID获取账本
    suspend fun getLedgerById(id: Long): Ledger? = withContext(Dispatchers.IO) {
        ledgerDao.getLedgerById(id)
    }

    // 获取默认账本
    suspend fun getDefaultLedger(): Ledger? = withContext(Dispatchers.IO) {
        ledgerDao.getDefaultLedger()
    }

    // 获取所有账本的Flow
    fun getAllLedgersFlow(): Flow<List<Ledger>> {
        return ledgerDao.getAllLedgersFlow()
    }

    fun getAllLedgers(): List<Ledger> {
        return ledgerDao.getAllLedgers()
    }

    // 设置默认账本
    suspend fun setDefaultLedger(id: Long) = withContext(Dispatchers.IO) {
        ledgerDao.safeSetDefaultLedger(id)
    }

    // 删除账本
    suspend fun deleteLedger(id: Long) = withContext(Dispatchers.IO) {
        ledgerDao.deleteLedger(id)
    }
} 