package com.xuhh.capybaraledger.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import com.xuhh.capybaraledger.application.App

object ColorUtils {
    /**
     * 获取颜色资源
     */
    @ColorInt
    fun getColor(@ColorRes colorResId: Int): Int {
        return App.context.getColor(colorResId)
    }

    /**
     * 调整颜色的透明度
     */
    fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).toInt()
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }
} 