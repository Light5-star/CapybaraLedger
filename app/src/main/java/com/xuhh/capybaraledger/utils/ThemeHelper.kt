package com.xuhh.capybaraledger.utils

import android.app.Activity
import androidx.preference.PreferenceManager
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.model.ThemeType

object ThemeHelper {
    fun applyTheme(activity: Activity) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val themeName = prefs.getString("app_theme", ThemeType.DEFAULT.name)
        val theme = ThemeType.valueOf(themeName ?: ThemeType.DEFAULT.name)
        
        val themeResId = when (theme) {
            ThemeType.DEFAULT -> R.style.Theme_CapybaraLedger_Default
            ThemeType.PINK -> R.style.Theme_CapybaraLedger_Pink
            ThemeType.BLUE -> R.style.Theme_CapybaraLedger_Blue
            ThemeType.DARK -> R.style.Theme_CapybaraLedger_Dark
        }
        
        activity.setTheme(themeResId)
    }
} 