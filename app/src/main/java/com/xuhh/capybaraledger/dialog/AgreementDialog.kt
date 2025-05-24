package com.xuhh.capybaraledger.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.xuhh.capybaraledger.databinding.DialogAgreementBinding

class AgreementDialog(
    context: Context,
    private val onAgree: () -> Unit,
    private val onDisagree: () -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogAgreementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogAgreementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置窗口背景为透明
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        // 设置弹窗宽度为屏幕的85%
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.85).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        // 设置不可取消
        setCancelable(false)
        
        // 设置点击事件
        binding.btnConfirm.setOnClickListener {
            onAgree()
            dismiss()
        }
        
        binding.btnCancel.setOnClickListener {
            onDisagree()
            dismiss()
        }
        
        binding.tvAgreement.setOnClickListener {
            openUrl("http://cpledger.xuhh.site/policy/agreement.html")
        }
        
        binding.tvPrivacy.setOnClickListener {
            openUrl("http://cpledger.xuhh.site/policy/privacy.html")
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
} 