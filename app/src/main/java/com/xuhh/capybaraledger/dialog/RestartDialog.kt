package com.xuhh.capybaraledger.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.xuhh.capybaraledger.databinding.DialogRestartBinding

class RestartDialog(
    context: Context,
    private val onConfirm: () -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogRestartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogRestartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置对话框宽度和位置
        window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent) // 设置窗口背景透明
            setLayout(
                (context.resources.displayMetrics.widthPixels * 0.85).toInt(), // 设置宽度为屏幕的85%
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.CENTER) // 居中显示
        }

        // 设置点击事件
        binding.btnConfirm.setOnClickListener {
            dismiss()
            onConfirm()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }
} 