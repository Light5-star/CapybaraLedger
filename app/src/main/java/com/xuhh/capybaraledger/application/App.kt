package com.xuhh.capybaraledger.application

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import com.xuhh.capybaraledger.data.database.AppDatabase
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.data.model.ThemeType
import com.xuhh.capybaraledger.data.repository.BillRepository
import com.xuhh.capybaraledger.data.repository.LedgerRepository
import com.xuhh.capybaraledger.data.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {
    companion object {
        lateinit var context: Context
            private set
    }

    // 数据库实例（单例）
    val database by lazy { AppDatabase.getInstance(this) }

    // 全局共享的 Repository
    val ledgerRepository by lazy { LedgerRepository(database.ledgerDao()) }
    val billRepository by lazy { BillRepository(database.billDao()) }
    val reminderRepository by lazy { ReminderRepository(database.reminderDao()) }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        applyTheme()
        checkDefaultLedger()
    }

    private fun checkDefaultLedger() {
        Log.d("App", "Checking default ledger...")
        // 在协程中执行数据库操作
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val defaultLedger = ledgerRepository.getDefaultLedger()
                if (defaultLedger == null) {
                    Log.d("App", "No default ledger found, creating one...")
                    // 创建默认账本
                    val newDefaultLedger = Ledger(
                        name = "默认账本",
                        description = "系统默认账本",
                        icon = 0,
                        color = 0,
                        isDefault = true,
                        sortOrder = 0
                    )
                    val ledgerId = ledgerRepository.createLedger(newDefaultLedger)
                    Log.d("App", "Created default ledger with ID: $ledgerId")
                } else {
                    Log.d("App", "Default ledger exists with ID: ${defaultLedger.id}")
                }
            } catch (e: Exception) {
                Log.e("App", "Error checking default ledger", e)
            }
        }
    }

    private fun applyTheme() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val themeName = prefs.getString("app_theme", ThemeType.DEFAULT.name)
        val theme = ThemeType.valueOf(themeName ?: ThemeType.DEFAULT.name)
        
        // 设置夜间模式
        if (theme == ThemeType.DARK) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}