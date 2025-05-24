package com.xuhh.capybaraledger.ui.fragment.profile

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.databinding.FragmentProfileBinding
import com.xuhh.capybaraledger.dialog.AboutDialog
import com.xuhh.capybaraledger.dialog.FeedbackDialog
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.ui.activity.ledger.LedgerManageActivity
import com.xuhh.capybaraledger.ui.activity.reminder.ReminderManagerActivity
import com.xuhh.capybaraledger.ui.activity.theme.ThemeSettingsActivity
import com.xuhh.capybaraledger.ui.activity.budget.BudgetManagementActivity
import com.xuhh.capybaraledger.utils.BarUtils
import com.xuhh.capybaraledger.utils.ColorUtils
import kotlinx.coroutines.launch

class ProfileFragment: BaseFragment<FragmentProfileBinding>() {
    override fun initBinding(): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        
        // 加载记账统计数据
        loadAccountingStats()
        
        // 设置关于我们的点击事件
        mBinding.llAbout.setOnClickListener {
            AboutDialog(requireContext()).show()
        }
        
        // 设置反馈的点击事件
        mBinding.llFeedback.setOnClickListener {
            FeedbackDialog(requireContext()).show()
        }

        mBinding.llLedger.setOnClickListener {
            startActivity(Intent(requireContext(), LedgerManageActivity::class.java))
        }

        mBinding.llReminder.setOnClickListener {
            startActivity(Intent(requireContext(), ReminderManagerActivity::class.java))
        }

        mBinding.llTheme.setOnClickListener {
            startActivity(Intent(requireContext(), ThemeSettingsActivity::class.java))
        }

        mBinding.llDonate.setOnClickListener {
            showDonateDialog()
        }

        mBinding.llBudget.setOnClickListener {
            startActivity(Intent(requireContext(), BudgetManagementActivity::class.java))
        }
    }
    
    // 加载记账统计数据
    private fun loadAccountingStats() {
        val app = requireActivity().application as App
        val billRepository = app.billRepository
        
        lifecycleScope.launch {
            try {
                // 获取记账天数
                val totalDays = billRepository.getAccountingDaysCount()
                mBinding.tvTotalDays.text = totalDays.toString()
                
                // 获取连续记账天数
                val streakDays = billRepository.getConsecutiveAccountingDays()
                mBinding.tvStreakDays.text = streakDays.toString()
            } catch (e: Exception) {
                // 错误处理
                mBinding.tvTotalDays.text = "0"
                mBinding.tvStreakDays.text = "0"
            }
        }
    }

    private fun showDonateDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_donate)
        
        // 设置窗口背景为透明
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        // 设置宽度为屏幕的85%
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        // 设置支付宝点击事件
        dialog.findViewById<View>(R.id.ll_alipay).setOnClickListener {
            try {
                // 使用您的收款码
                val qrCode = "https://qr.alipay.com/fkx13741qojunpob0eyr283"
                val encodedQrCode = java.net.URLEncoder.encode(qrCode, "utf-8")
                // 构建支付宝 App 的 URL Schema
                val alipayUrl = "alipayqr://platformapi/startapp?saId=10000007&qrcode=$encodedQrCode"
                
                // 尝试打开支付宝 App
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(alipayUrl)))
            } catch (e: Exception) {
                // 如果失败，使用浏览器打开
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://qr.alipay.com/fkx13741qojunpob0eyr283")))
            }
            dialog.dismiss()
        }

        // 设置微信点击事件
        dialog.findViewById<View>(R.id.ll_wechat).setOnClickListener {
            dialog.dismiss()
        }

        // 设置关闭按钮
        dialog.findViewById<View>(R.id.btn_close).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 设置状态栏颜色
        BarUtils.setStatusBarColor(requireActivity(), ColorUtils.getColor(R.color.primary))
        // 设置状态栏图标为亮色
        BarUtils.setStatusBarLightMode(requireActivity(), false)
    }
    
    override fun onResume() {
        super.onResume()
        // 每次恢复时更新统计数据，以反映最新变化
        loadAccountingStats()
    }
}