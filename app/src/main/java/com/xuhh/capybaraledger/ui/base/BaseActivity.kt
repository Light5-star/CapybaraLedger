package com.xuhh.capybaraledger.ui.base

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.utils.StatusBarHelper
import com.xuhh.capybaraledger.utils.ThemeHelper

/**
 * 基本的Activity
 * 完成一些绑定初始化操作，简化使用代码
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    protected lateinit var mBinding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = initBinding()
        setContentView(mBinding.root)
        
        // 设置状态栏颜色为primary
        StatusBarHelper.setStatusBarColorToPrimary(this)
        
        // 设置 WindowInsets
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
        
        initView()
    }

    abstract fun initBinding(): VB
    open fun initView(){}

    protected fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}