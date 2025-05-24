package com.xuhh.capybaraledger.ui.activity.theme

import android.content.Intent
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.xuhh.capybaraledger.data.model.ThemeType
import com.xuhh.capybaraledger.databinding.ActivityThemeSettingsBinding
import com.xuhh.capybaraledger.dialog.RestartDialog
import com.xuhh.capybaraledger.ui.base.BaseActivity

class ThemeSettingsActivity : BaseActivity<ActivityThemeSettingsBinding>() {
    private lateinit var prefs: SharedPreferences

    override fun initBinding(): ActivityThemeSettingsBinding {
        return ActivityThemeSettingsBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        
        // 设置返回按钮
        mBinding.btnBack.setOnClickListener {
            finish()
        }

        // 设置当前主题
        val currentTheme = getCurrentTheme()
        when (currentTheme) {
            ThemeType.DEFAULT -> mBinding.rbThemeDefault.isChecked = true
            ThemeType.DARK -> mBinding.rbThemeDark.isChecked = true
            ThemeType.PINK -> mBinding.rbThemePink.isChecked = true
            ThemeType.BLUE -> mBinding.rbThemeBlue.isChecked = true
        }

        // 设置主题选择监听
        mBinding.rgThemes.setOnCheckedChangeListener { _, checkedId ->
            val newTheme = when (checkedId) {
                mBinding.rbThemeDefault.id -> ThemeType.DEFAULT
                mBinding.rbThemeDark.id -> ThemeType.DARK
                mBinding.rbThemePink.id -> ThemeType.PINK
                mBinding.rbThemeBlue.id -> ThemeType.BLUE
                else -> ThemeType.DEFAULT
            }
            saveTheme(newTheme)
            showRestartDialog()
        }
    }

    private fun getCurrentTheme(): ThemeType {
        val themeName = prefs.getString("app_theme", ThemeType.DEFAULT.name)
        return ThemeType.valueOf(themeName ?: ThemeType.DEFAULT.name)
    }

    private fun saveTheme(theme: ThemeType) {
        prefs.edit().putString("app_theme", theme.name).apply()
    }

    private fun showRestartDialog() {
        RestartDialog(this) {
            // 重启应用
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            android.os.Process.killProcess(android.os.Process.myPid())
        }.show()
    }
} 