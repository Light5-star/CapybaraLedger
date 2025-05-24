package com.xuhh.capybaraledger.utils

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.xuhh.capybaraledger.R

/**
 * 状态栏工具类
 */
object StatusBarHelper {
    
    /**
     * 设置状态栏为主题的primary颜色
     */
    fun setStatusBarColorToPrimary(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 设置状态栏颜色为主题的primary颜色
            activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.primary)
            
            // 状态栏图标颜色(深色背景需要浅色图标)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 如果是浅色背景，设置状态栏图标为深色
                activity.window.decorView.systemUiVisibility = 0
            }
        }
    }
} 