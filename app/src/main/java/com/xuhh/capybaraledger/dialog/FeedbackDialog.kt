package com.xuhh.capybaraledger.dialog

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import com.xuhh.capybaraledger.databinding.DialogFeedbackBinding

class FeedbackDialog(context: Context) : Dialog(context) {
    private lateinit var binding: DialogFeedbackBinding
    
    companion object {
        private const val QQ_GROUP = "962285162"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置窗口背景为透明
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        // 设置弹窗宽度
        window?.setLayout(
            context.resources.displayMetrics.widthPixels * 9 / 10,
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // 关闭按钮
        binding.btnClose.setOnClickListener { dismiss() }

        // QQ群
        binding.llQqGroup.setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("mqqapi://card/show_pslcard?src_type=internal&version=1&uin=$QQ_GROUP&card_type=group&source=qrcode")
            }
            
            try {
                context.startActivity(intent)
                dismiss()
            } catch (e: Exception) {
                try {
                    // 如果无法直接跳转QQ群，复制群号到剪贴板
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("QQ群号", QQ_GROUP)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "群号已复制到剪贴板", Toast.LENGTH_SHORT).show()
                    
                    // 尝试打开QQ主界面
                    context.startActivity(Intent().apply {
                        action = Intent.ACTION_VIEW
                        data = Uri.parse("mqqapi://card/show_pslcard?src_type=internal&version=1&uin=$QQ_GROUP")
                        setPackage("com.tencent.mobileqq")
                    })
                    dismiss()
                } catch (e: Exception) {
                    Toast.makeText(context, "请安装QQ", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 邮箱
        binding.llEmail.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:xhhcode@qq.com")
                    putExtra(Intent.EXTRA_SUBJECT, "卡皮记账 - 用户反馈")
                }
                context.startActivity(intent)
                dismiss()
            } catch (e: Exception) {
                Toast.makeText(context, "未找到邮箱应用", Toast.LENGTH_SHORT).show()
            }
        }

        // 公众号
        binding.llWechat.setOnClickListener {
            try {
                // 复制公众号名称到剪贴板
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("公众号", "Light5star")
                clipboard.setPrimaryClip(clip)
                
                // 尝试打开微信
                val intent = Intent().apply {
                    action = Intent.ACTION_MAIN
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setClassName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Toast.makeText(context, "公众号名称已复制，请在微信中搜索：Light5star", Toast.LENGTH_LONG).show()
                dismiss()
            } catch (e: Exception) {
                Toast.makeText(context, "请安装微信", Toast.LENGTH_SHORT).show()
            }
        }
    }
} 