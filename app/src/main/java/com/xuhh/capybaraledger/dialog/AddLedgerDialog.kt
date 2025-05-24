package com.xuhh.capybaraledger.dialog

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.xuhh.capybaraledger.databinding.DialogAddLedgerBinding
import com.xuhh.capybaraledger.viewmodel.BillViewModel

class AddLedgerDialog(
    context: Context,
    private val viewModel: BillViewModel,
    private val onComplete: () -> Unit
) : Dialog(context) {
    private lateinit var binding: DialogAddLedgerBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogAddLedgerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置对话框宽度为屏幕宽度的85%
        window?.apply {
            val params = attributes
            params.width = (context.resources.displayMetrics.widthPixels * 0.85).toInt()
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes = params
            setBackgroundDrawableResource(android.R.color.transparent)
        }

        setupViews()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupViews() {
        // 设置关闭按钮
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        // 设置完成按钮
        binding.btnConfirm.setOnClickListener {
            val name = binding.etLedgerName.text?.toString()?.trim()
            
            when {
                name.isNullOrEmpty() -> {
                    binding.etLedgerName.error = "请输入账本名称"
                    return@setOnClickListener
                }
                viewModel.isLedgerNameExists(name) -> {
                    binding.etLedgerName.error = "账本名称已存在"
                    return@setOnClickListener
                }
                else -> {
                    // 创建账本
                    viewModel.createLedger(name)
                    onComplete()
                    dismiss()
                }
            }
        }
    }
} 