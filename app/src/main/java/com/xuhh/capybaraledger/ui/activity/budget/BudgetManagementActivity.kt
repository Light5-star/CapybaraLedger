package com.xuhh.capybaraledger.ui.activity.budget

import android.os.Bundle
import com.xuhh.capybaraledger.databinding.ActivityBudgetManagementBinding
import com.xuhh.capybaraledger.ui.base.BaseActivity

class BudgetManagementActivity : BaseActivity<ActivityBudgetManagementBinding>() {
    
    override fun initBinding(): ActivityBudgetManagementBinding {
        return ActivityBudgetManagementBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        mBinding.btnBack.setOnClickListener {
            finish()
        }
    }
} 