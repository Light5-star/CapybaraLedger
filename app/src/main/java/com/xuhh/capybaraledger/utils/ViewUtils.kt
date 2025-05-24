package com.xuhh.capybaraledger.utils

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.xuhh.capybaraledger.R

/**
 * Viewd的工具类
 * 提供dp和px的转化
 * 提供简易的toast
 * 提供简易的跳转
 * 提供容器的查询
 */
object ViewUtils {
    //dp2px
    fun dp2px(context: Context, dp:Int): Int{
        return (context.resources.displayMetrics.density*dp).toInt()
    }

    fun toast(context: Context,message:String){
        Toast.makeText(context,message,Toast.LENGTH_LONG).show()
    }

    //带动画的切换
    fun navigate(
        view: View,
        @IdRes id: Int,
        @AnimRes inAnim: Int = R.anim.right_in_anim,
        @AnimRes outAnim: Int = R.anim.left_out_anim,
        @AnimRes popinAnim: Int = R.anim.left_in_anim,
        @AnimRes popoutAnim: Int = R.anim.right_out_anim,
    ){
        val navController = view.findNavController()
        val navOption = NavOptions.Builder()
            .setEnterAnim(inAnim)
            .setExitAnim(outAnim)
            .setPopEnterAnim(popinAnim)
            .setPopExitAnim(popoutAnim)
            .build()

        navController.navigate(id,null,navOption)
    }

    /**
     * 通过一个View找到父容器里面的某一个类型的View
     * 从DecorView开始一层一层往下找 找到FragmentContainerView
     */
    fun findContainerView(parent:ViewGroup):FragmentContainerView?{
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child is FragmentContainerView) {
                return child
            }
            if (child is ViewGroup) {
                val found = findContainerView(child)
                if (found != null) {
                    return found
                }
            }
        }
        return null
    }
}
















