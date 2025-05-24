package com.xuhh.capybaraledger.ui.activity.ledger

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.databinding.ActivityLedgerManageBinding
import com.xuhh.capybaraledger.databinding.DialogDeleteConfirmBinding
import com.xuhh.capybaraledger.dialog.AddLedgerDialog
import com.xuhh.capybaraledger.ui.base.BaseActivity
import com.xuhh.capybaraledger.viewmodel.BillViewModel
import com.xuhh.capybaraledger.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import android.app.AlertDialog
import android.widget.TextView
import com.xuhh.capybaraledger.R

class LedgerManageActivity : BaseActivity<ActivityLedgerManageBinding>() {
    private val mViewModel: BillViewModel by viewModels {
        val app = application as App
        ViewModelFactory(app.ledgerRepository, app.billRepository)
    }
    private lateinit var adapter: LedgerManageAdapter

    override fun initBinding(): ActivityLedgerManageBinding {
        return ActivityLedgerManageBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initView() {
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupToolbar() {
        mBinding.btnBack.setOnClickListener {
            finish()
        }

        mBinding.btnAdd.setOnClickListener {
            AddLedgerDialog(this, mViewModel) {
                // 添加完成后会自动更新列表
            }.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupRecyclerView() {
        adapter = LedgerManageAdapter(
            onSetDefault = { ledger ->
                mViewModel.setDefaultLedger(ledger.id)
            },
            onDelete = { ledger ->
                showDeleteConfirmDialog(ledger)
            }
        )

        mBinding.rvLedgers.apply {
            layoutManager = LinearLayoutManager(this@LedgerManageActivity)
            adapter = this@LedgerManageActivity.adapter
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeViewModel() {
        lifecycleScope.launch {
            mViewModel.ledgers.collect { ledgers ->
                adapter.submitList(ledgers)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDeleteConfirmDialog(ledger: Ledger) {
        val dialog = AlertDialog.Builder(this).create()
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_confirm, null)
        dialog.setView(dialogView)

        // 设置标题和内容
        dialogView.findViewById<TextView>(R.id.tv_title).text = "删除账本"
        dialogView.findViewById<TextView>(R.id.tv_content).text = "确定要删除「${ledger.name}」吗？\n删除后数据将无法恢复"

        // 取消按钮
        dialogView.findViewById<TextView>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        // 确认按钮
        dialogView.findViewById<TextView>(R.id.btn_confirm).setOnClickListener {
            mViewModel.deleteLedger(ledger.id)
            dialog.dismiss()
        }

        dialog.show()
    }
} 