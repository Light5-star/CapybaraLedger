package com.xuhh.capybaraledger.ui.view.unicode

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.xuhh.capybaraledger.R

/**
 * iconfont的使用类
 * 可以直接设置text
 */
class UnicodeTextView(
    context: Context,
    attrs: AttributeSet? = null
): AppCompatTextView(context, attrs) {

    companion object {
        private var iconFont: android.graphics.Typeface? = null
    }

    init {
        // 加载字体并进行错误处理
        if (iconFont == null) {
            iconFont = ResourcesCompat.getFont(context, R.font.iconfont)
        }
        iconFont?.let {
            typeface = it
        } ?: run {
            // 处理字体加载失败的情况，例如使用默认字体
            typeface = android.graphics.Typeface.DEFAULT
        }
    }

    // 可以在这里添加自定义属性处理逻辑
    init {
        context.obtainStyledAttributes(attrs, R.styleable.UnicodeTextView).apply {
            recycle()
        }
    }
}