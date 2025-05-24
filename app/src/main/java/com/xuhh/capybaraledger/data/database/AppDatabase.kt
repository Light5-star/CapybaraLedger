package com.xuhh.capybaraledger.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.xuhh.capybaraledger.data.converter.ReminderTypeConverters
import com.xuhh.capybaraledger.data.dao.BillDao
import com.xuhh.capybaraledger.data.dao.CategoryDao
import com.xuhh.capybaraledger.data.dao.LedgerDao
import com.xuhh.capybaraledger.data.dao.ReminderDao
import com.xuhh.capybaraledger.data.dao.UserDao
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.data.model.Categories
import com.xuhh.capybaraledger.data.model.Category
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.data.model.Reminder
import com.xuhh.capybaraledger.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Bill::class, Ledger::class, User::class, Category::class, Reminder::class],
    version = 2
)
@TypeConverters(ReminderTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun billDao(): BillDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        private const val TAG = "AppDatabase"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Log.d(TAG, "Creating new database instance")
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "BillDatabase.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d(TAG, "Database created, initializing data...")
                
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val database = INSTANCE ?: return@launch

                        // 只创建默认分类
                        Log.d(TAG, "Creating default categories...")
                        Categories.getAllCategories().forEach { category ->
                            database.categoryDao().insert(category)
                        }
                        Log.d(TAG, "Database initialization completed")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error initializing database", e)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                Log.d(TAG, "Database opened")
            }
        }
    }
}