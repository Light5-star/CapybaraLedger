package com.xuhh.capybaraledger.ui.base

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.xuhh.capybaraledger.utils.KeyboardAnimation
import kotlinx.coroutines.cancel

/**
 * 基本的Fragment
 * 完成一些绑定和键盘等初始化操作，简化使用代码
 */
abstract class BaseFragment<T: ViewBinding>: Fragment() {
    private lateinit var _binding:T
    val mBinding:T
        get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = initBinding()
        return _binding.root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initKeyboardListener()
        initEeyboardEvent()
    }
    @RequiresApi(Build.VERSION_CODES.R)
    private fun initEeyboardEvent(){
        mBinding.root.setOnClickListener {
            val insets = ViewCompat.getRootWindowInsets(mBinding.root)
            insets?.also{
                if (insets.isVisible(WindowInsetsCompat.Type.ime())){
                    //隐藏键盘
                   mBinding.root.windowInsetsController?.hide(WindowInsetsCompat.Type.ime())
                }
            }
        }
    }
    private fun initKeyboardListener(){
        val view = initKeyboardAnimationView()
        if (view != null) {
            ViewCompat.setOnApplyWindowInsetsListener(view
            ) { v, insets -> //键盘动画
                KeyboardAnimation.setupKeyboardAnimation(view, 100)
                insets
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.cancel()
    }

    open fun initKeyboardAnimationView():View?{
        return null
    }
    abstract fun initBinding(): T
    open fun initView() {}


}