package com.xuhh.capybaraledger.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Window
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.databinding.DialogAboutBinding

class AboutDialog(context: Context) : Dialog(context) {
    private lateinit var binding: DialogAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置窗口背景为透明
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        // 设置弹窗宽度
        window?.setLayout(
            context.resources.displayMetrics.widthPixels * 9 / 10,
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )

        // 设置点击事件
        binding.btnClose.setOnClickListener { dismiss() }
        
        // 了解更多点击事件
        binding.tvLearnMore.setOnClickListener {
            openUrl("http://cpledger.xuhh.site/")
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