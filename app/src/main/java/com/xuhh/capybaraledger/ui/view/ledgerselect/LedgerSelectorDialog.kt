package com.xuhh.capybaraledger.ui.view.ledgerselect

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.databinding.DialogLedgerSelectorBinding
import com.xuhh.capybaraledger.dialog.AddLedgerDialog
import com.xuhh.capybaraledger.viewmodel.BillViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LedgerSelectorDialog(
    context: Context,
    private val viewModel: BillViewModel,
    private val onLedgerSelected: (Ledger) -> Unit
) : Dialog(context) {
    private lateinit var binding: DialogLedgerSelectorBinding
    private lateinit var adapter: LedgerSelectorAdapter
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogLedgerSelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置对话框宽度为屏幕宽度的85%
        window?.apply {
            val params = attributes
            params.width = (context.resources.displayMetrics.widthPixels * 0.85).toInt()
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes = params
            setBackgroundDrawableResource(android.R.color.transparent)
        }

        // 设置关闭按钮点击事件
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        setupRecyclerView()
        setupAddButton()
        observeViewModel()
    }

    private fun setupAddButton() {
        binding.btnAddLedger.setOnClickListener {
            // 先关闭当前对话框
            dismiss()
            // 打开添加账本对话框
            AddLedgerDialog(context, viewModel) {
                // 完成后的回调
            }.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupRecyclerView() {
        adapter = LedgerSelectorAdapter { ledger ->
            // 更新当前账本
            coroutineScope.launch {
                viewModel.updateCurrentLedger(ledger)
                onLedgerSelected(ledger)
                dismiss()
            }
        }

        binding.rvLedgers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@LedgerSelectorDialog.adapter
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeViewModel() {
        coroutineScope.launch {
            // 观察当前账本
            viewModel.currentLedger.collect { ledger ->
                ledger?.let {
                    adapter.setCurrentLedger(it.id)
                }
            }
        }

        coroutineScope.launch {
            // 观察账本列表
            viewModel.ledgers.collect { ledgers ->
                adapter.submitList(ledgers)
            }
        }
    }

    override fun dismiss() {
        coroutineScope.cancel()
        super.dismiss()
    }
} 