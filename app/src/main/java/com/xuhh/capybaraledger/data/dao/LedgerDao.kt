package com.xuhh.capybaraledger.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.xuhh.capybaraledger.data.model.Ledger
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {
    @Insert
    suspend fun insert(ledger: Ledger): Long

    @Update
    suspend fun update(ledger: Ledger)

    @Query("SELECT * FROM ledger WHERE id = :id")
    suspend fun getLedgerById(id: Long): Ledger?

    @Query("SELECT * FROM ledger WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultLedger(): Ledger?

    @Query("SELECT * FROM ledger ORDER BY sortOrder ASC")
    fun getAllLedgersFlow(): Flow<List<Ledger>>

    @Query("SELECT * FROM ledger ORDER BY sortOrder ASC")
    fun getAllLedgers(): List<Ledger>

    @Query("UPDATE ledger SET isDefault = 0")
    suspend fun clearDefaultLedger()

    @Query("UPDATE ledger SET isDefault = 1 WHERE id = :id")
    suspend fun unsafeSetDefaultLedger(id: Long)

    @Query("DELETE FROM ledger WHERE id = :id")
    suspend fun deleteLedger(id: Long)

    @Transaction
    suspend fun safeSetDefaultLedger(id: Long) {
        clearDefaultLedger()
        unsafeSetDefaultLedger(id)
    }
} 