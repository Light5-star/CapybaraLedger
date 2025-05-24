package com.xuhh.capybaraledger.ui.fragment.detail

import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuhh.capybaraledger.adapter.DateSection
import com.xuhh.capybaraledger.adapter.DateSectionAdapter
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.data.dao.BillWithCategory
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.databinding.FragmentDetailsListBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.viewmodel.BillViewModel
import com.xuhh.capybaraledger.viewmodel.ViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class FlowModeFragment : BaseFragment<FragmentDetailsListBinding>() {
    private val mViewModel: BillViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.ledgerRepository, app.billRepository)
    }
    private lateinit var dateSectionAdapter: DateSectionAdapter
    private var isViewCreated = false

    override fun initView() {
        super.initView()
        setupRecyclerView()
        setupObservers()
        isViewCreated = true
        loadBillData()
    }

    private fun setupObservers() {
        // 观察账本变化
        lifecycleScope.launch {
            mViewModel.currentLedger.collect { ledger ->
                if (ledger != null && isViewCreated && isResumed) {
                    loadBillData()
                }
            }
        }

        // 观察月份变化
        lifecycleScope.launch {
            mViewModel.currentCalendar.collect { calendar ->
                if (isViewCreated && isResumed) {
                    loadBillData()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isViewCreated) {
            loadBillData()
        }
    }

    private fun setupRecyclerView() {
        dateSectionAdapter = DateSectionAdapter { billWithCategory ->
            // 处理账单点击事件
            Log.d("FlowModeFragment", "Bill clicked - ID: ${billWithCategory.bill.id}, Amount: ${billWithCategory.bill.amount}")
            val mainActivity = requireActivity() as com.xuhh.capybaraledger.MainActivity
            mainActivity.startBillEdit(
                ledgerId = billWithCategory.bill.ledgerId,
                selectedDate = billWithCategory.bill.date,
                billId = billWithCategory.bill.id
            )
        }
        mBinding.rvBills.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = dateSectionAdapter
        }
    }

    fun loadBillData() {
        if (!isAdded || isDetached) return

        lifecycleScope.launch {
            try {
                val ledgerId = mViewModel.currentLedger.value?.id ?: return@launch
                val (startTime, endTime) = mViewModel.getCurrentMonthRange()
                
                val billsWithCategory = mViewModel.getBillsWithCategoryByTimeRange(
                    ledgerId, startTime, endTime
                )
                
                // 处理数据和更新UI
                withContext(Dispatchers.Main) {
                    updateBillList(billsWithCategory)
                    updateEmptyView(billsWithCategory.isEmpty())
                }
            } catch (e: Exception) {
                Log.e("FlowMode", "Error loading data", e)
                withContext(Dispatchers.Main) {
                    updateEmptyView(true)
                }
            }
        }
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        if (isEmpty) {
            mBinding.rvBills.visibility = View.GONE
            mBinding.emptyView.visibility = View.VISIBLE
        } else {
            mBinding.rvBills.visibility = View.VISIBLE
            mBinding.emptyView.visibility = View.GONE
        }
    }

    private fun updateBillList(billsWithCategory: List<BillWithCategory>) {
        // 按日期分组处理数据
        val dateSections = billsWithCategory
            .groupBy { billWithCategory ->
                Calendar.getInstance().apply {
                    timeInMillis = billWithCategory.bill.date
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
            }
            .map { (date, billsInDay) ->
                val dayExpense = billsInDay
                    .filter { it.bill.type == Bill.TYPE_EXPENSE }
                    .sumOf { it.bill.amount }
                val dayIncome = billsInDay
                    .filter { it.bill.type == Bill.TYPE_INCOME }
                    .sumOf { it.bill.amount }
                val balance = dayIncome - dayExpense

                DateSection(
                    date = date,
                    bills = billsInDay,
                    balance = balance
                )
            }
            .sortedByDescending { it.date }

        dateSectionAdapter.submitList(dateSections)
    }

    override fun initBinding(): FragmentDetailsListBinding {
        return FragmentDetailsListBinding.inflate(layoutInflater)
    }
} 