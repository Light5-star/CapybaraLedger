package com.xuhh.capybaraledger.ui.fragment.detail

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.xuhh.capybaraledger.adapter.CalendarAdapter
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.data.dao.BillWithCategory
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.databinding.FragmentDetailsCalendarBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.viewmodel.BillViewModel
import com.xuhh.capybaraledger.viewmodel.ViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class CalendarModeFragment : BaseFragment<FragmentDetailsCalendarBinding>() {
    private val mViewModel: BillViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.ledgerRepository, app.billRepository)
    }
    private lateinit var calendarAdapter: CalendarAdapter
    private var isViewCreated = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewCreated = true
        setupCalendar()
        setupObservers()
        loadCalendarData()
    }

    private fun setupObservers() {
        // 观察账本变化
        lifecycleScope.launch {
            mViewModel.currentLedger.collect { ledger ->
                if (ledger != null && isViewCreated && isResumed) {
                    loadCalendarData()
                }
            }
        }

        // 观察月份变化
        lifecycleScope.launch {
            mViewModel.currentCalendar.collect { calendar ->
                if (isViewCreated && isResumed) {
                    loadCalendarData()
                }
            }
        }
    }

    private fun setupCalendar() {
        calendarAdapter = CalendarAdapter()
        mBinding.rvCalendar.apply {
            layoutManager = GridLayoutManager(context, 7)
            adapter = calendarAdapter
        }
    }

    fun loadCalendarData() {
        if (!isAdded || isDetached) return

        lifecycleScope.launch {
            try {
                val ledgerId = mViewModel.currentLedger.value?.id ?: return@launch
                val (startTime, endTime) = mViewModel.getCurrentMonthRange()
                
                val bills = mViewModel.getBillsWithCategoryByTimeRange(
                    ledgerId, startTime, endTime
                )

                // 处理数据和更新UI
                withContext(Dispatchers.Main) {
                    updateCalendarView(bills)
                }
            } catch (e: Exception) {
                Log.e("CalendarMode", "Error loading data", e)
            }
        }
    }

    private fun processCalendarData(bills: List<BillWithCategory>, calendar: Calendar): List<CalendarDay> {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        
        // 获取当月第一天是星期几
        val firstDayOfMonth = Calendar.getInstance().apply {
            clear()
            set(year, month, 1)
        }
        val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = firstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 按日期分组统计数据
        val dailyData = bills.groupBy { billWithCategory ->
            Calendar.getInstance().apply {
                timeInMillis = billWithCategory.bill.date
            }.get(Calendar.DAY_OF_MONTH)
        }.mapValues { (_, dayBills) ->
            val expense = dayBills.filter { it.bill.type == Bill.TYPE_EXPENSE }.sumOf { it.bill.amount }
            val income = dayBills.filter { it.bill.type == Bill.TYPE_INCOME }.sumOf { it.bill.amount }
            val balance = income - expense
            Triple(expense, income, balance)
        }

        val calendarDays = mutableListOf<CalendarDay>()
        
        // 添加空白天数
        repeat(firstDayOfWeek) {
            calendarDays.add(CalendarDay())
        }
        
        // 添加当月天数
        for (day in 1..daysInMonth) {
            val (expense, income, balance) = dailyData[day] ?: Triple(0.0, 0.0, 0.0)
            calendarDays.add(CalendarDay(day, expense, income, balance))
        }

        return calendarDays
    }

    private fun updateCalendarView(bills: List<BillWithCategory>) {
        val calendar = mViewModel.currentCalendar.value ?: return
        val calendarData = processCalendarData(bills, calendar)
        calendarAdapter.submitList(calendarData)
    }

    override fun initBinding(): FragmentDetailsCalendarBinding {
        return FragmentDetailsCalendarBinding.inflate(layoutInflater)
    }

    override fun onResume() {
        super.onResume()
        if (isViewCreated) {
            loadCalendarData()
        }
    }
}



