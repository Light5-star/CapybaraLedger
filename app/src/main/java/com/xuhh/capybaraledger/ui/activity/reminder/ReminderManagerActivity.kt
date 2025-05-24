package com.xuhh.capybaraledger.ui.activity.reminder

import android.os.Bundle
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.databinding.ActivityReminderManageBinding
import com.xuhh.capybaraledger.ui.base.BaseActivity
import com.xuhh.capybaraledger.viewmodel.ReminderViewModel

class ReminderManagerActivity : BaseActivity<ActivityReminderManageBinding>() {
    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: ReminderPagerAdapter

    private val viewModel: ReminderViewModel by viewModels {
        ReminderViewModel.Factory(
            (application as App).reminderRepository,
            this
        )
    }

    override fun initBinding(): ActivityReminderManageBinding {
        return ActivityReminderManageBinding.inflate(layoutInflater)
    }

    override fun initView() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbar()
        setupViewPager()
    }

    private fun setupToolbar() {
        mBinding.btnBack.setOnClickListener {
            if (mBinding.viewPager.currentItem == 1) {
                navigateToList()
            } else {
                finish()
            }
        }

        mBinding.btnAdd.setOnClickListener {
            if (mBinding.viewPager.currentItem == 0) {
                // 在列表页面，点击进入添加页面
                navigateToAddReminder()
            } else {
                // 在添加页面，点击执行保存
                val fragment = pagerAdapter.getFragment(1) as? ReminderAddFragment
                if (fragment?.saveReminder() == true) {
                    navigateToList()
                }
            }
        }
    }

    private fun setupViewPager() {
        pagerAdapter = ReminderPagerAdapter(this)
        mBinding.viewPager.apply {
            adapter = pagerAdapter
            isUserInputEnabled = false  // 禁止滑动切换
        }
    }

    fun navigateToEdit(reminderId: Long) {
        mBinding.viewPager.currentItem = 1
        mBinding.tvTitle.text = "编辑提醒"
        mBinding.btnAdd.text = "保存"
        
        // 使用 viewPager 的 id
        pagerAdapter.getFragment(1)?.let { fragment ->
            if (fragment is ReminderAddFragment) {
                fragment.loadReminder(reminderId)
            }
        }
    }

    fun navigateToAddReminder() {
        mBinding.viewPager.currentItem = 1
        mBinding.tvTitle.text = "添加提醒"
        mBinding.btnAdd.text = "保存"
    }

    fun navigateToList() {
        mBinding.viewPager.currentItem = 0
        mBinding.tvTitle.text = "记账提醒"
        mBinding.btnAdd.text = "添加"  // 修改按钮文字为"添加"
    }

    override fun onBackPressed() {
        if (mBinding.viewPager.currentItem == 1) {
            navigateToList()
        } else {
            super.onBackPressed()
        }
    }
} 