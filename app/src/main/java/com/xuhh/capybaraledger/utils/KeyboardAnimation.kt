package com.xuhh.capybaraledger.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat

/**
 * 键盘动画工具
 */
object KeyboardAnimation {
    //设置键盘跟随动画
    fun setupKeyboardAnimation(view: View, dy:Int = 100){
        ViewCompat.setWindowInsetsAnimationCallback(
            view,
            object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP){
                //记录移动的距离
                var distance:Int = ViewUtils.dp2px(view.context, dy)

                override fun onStart(
                    animation: WindowInsetsAnimationCompat,
                    bounds: WindowInsetsAnimationCompat.BoundsCompat
                ): WindowInsetsAnimationCompat.BoundsCompat {

                    //确定是上移还是下移
                    val insets = ViewCompat.getRootWindowInsets(view)
                    insets?.let {
                        if (insets.isVisible(WindowInsetsCompat.Type.ime())){
                            //已经显示 即将隐藏
                            distance = -distance
                        }else{
                            //还没出现 即将出现
                            distance = 0
                        }
                    }
                    return super.onStart(animation, bounds)
                }

                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    val animation = runningAnimations.find {
                        it.typeMask and WindowInsetsCompat.Type.ime() != 0
                    }
                    //如果有键盘动画就继续
                    if (animation == null) return insets

                    //跟随移动实际上就是将控件的y坐标在当前的基础上加或者减
                    view.translationY = distance * animation.interpolatedFraction

                    return insets
                }

            }
        )
    }
}