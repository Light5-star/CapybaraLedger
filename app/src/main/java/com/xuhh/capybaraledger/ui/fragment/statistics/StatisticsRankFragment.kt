package com.xuhh.capybaraledger.ui.fragment.statistics

import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.xuhh.capybaraledger.adapter.CategoryRankAdapter
import com.xuhh.capybaraledger.adapter.CategoryRankItem
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.data.model.Category
import com.xuhh.capybaraledger.databinding.FragmentStatisticsRankBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.viewmodel.BillViewModel
import com.xuhh.capybaraledger.viewmodel.StatisticsViewModel
import com.xuhh.capybaraledger.viewmodel.ViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.content.ContextCompat
import com.xuhh.capybaraledger.R
import android.view.View

class StatisticsRankFragment : BaseFragment<FragmentStatisticsRankBinding>() {
    private val statisticsViewModel: StatisticsViewModel by activityViewModels()
    private val mViewModel: BillViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.ledgerRepository, app.billRepository)
    }
    private var currentType = Bill.TYPE_EXPENSE
    private var isViewCreated = false
    private lateinit var categoryRankAdapter: CategoryRankAdapter

    override fun initBinding(): FragmentStatisticsRankBinding {
        return FragmentStatisticsRankBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initView() {
        super.initView()
        setupPieChart()
        setupTypeButtons()
        setupRecyclerView()
        setupObservers()
        isViewCreated = true
        loadData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupObservers() {
        // 观察账本变化
        lifecycleScope.launch {
            mViewModel.currentLedger.collect { ledger ->
                ledger?.let {
                    if (isViewCreated && isResumed) {
                        loadData()
                    }
                }
            }
        }

        // 观察日期变化
        statisticsViewModel.calendar.observe(viewLifecycleOwner) { calendar ->
            if (isViewCreated && isResumed) {
                loadData()
            }
        }
    }

    private fun setupPieChart() {
        mBinding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            transparentCircleRadius = 60f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            setUsePercentValues(true)
            legend.isEnabled = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupTypeButtons() {
        mBinding.btnExpense.setOnClickListener {
            if (currentType != Bill.TYPE_EXPENSE) {
                currentType = Bill.TYPE_EXPENSE
                updateTypeUI()
                loadData()
            }
        }

        mBinding.btnIncome.setOnClickListener {
            if (currentType != Bill.TYPE_INCOME) {
                currentType = Bill.TYPE_INCOME
                updateTypeUI()
                loadData()
            }
        }
    }

    private fun updateTypeUI() {
        // 支出按钮
        mBinding.btnExpense.apply {
            setTextColor(ContextCompat.getColor(requireContext(), 
                if (currentType == Bill.TYPE_EXPENSE) R.color.accent else R.color.text_primary))
            setBackgroundResource(
                if (currentType == Bill.TYPE_EXPENSE) 
                    R.drawable.bg_mode_switch_item_selected
                else 
                    R.drawable.bg_mode_switch_item)
        }

        // 收入按钮
        mBinding.btnIncome.apply {
            setTextColor(ContextCompat.getColor(requireContext(), 
                if (currentType == Bill.TYPE_INCOME) R.color.accent else R.color.text_primary))
            setBackgroundResource(
                if (currentType == Bill.TYPE_INCOME) 
                    R.drawable.bg_mode_switch_item_selected 
                else 
                    R.drawable.bg_mode_switch_item)
        }
    }

    private fun setupRecyclerView() {
        categoryRankAdapter = CategoryRankAdapter()
        mBinding.rvRank.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = categoryRankAdapter
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val (startTime, endTime) = mViewModel.getCurrentMonthRange()
                val ledgerId = mViewModel.currentLedger.value?.id ?: return@launch

                val billsWithCategory = mViewModel.getBillsWithCategoryByTimeRange(ledgerId, startTime, endTime)

                // 按类别分组统计
                val categoryData = billsWithCategory
                    .filter { it.bill.type == currentType }
                    .groupBy { it.category }
                    .mapValues { (_, bills) -> bills.sumOf { it.bill.amount } }
                    .toList()
                    .sortedByDescending { (_, amount) -> amount }

                // 更新UI
                withContext(Dispatchers.Main) {
                    updateRankList(categoryData)
                    updatePieChart(categoryData)
                    updateEmptyView(categoryData.isEmpty())
                }
            } catch (e: Exception) {
                Log.e("StatisticsRank", "Error loading data", e)
                withContext(Dispatchers.Main) {
                    updateEmptyView(true)
                }
            }
        }
    }

    private fun updatePieChart(categoryData: List<Pair<Category, Double>>) {
        val entries = categoryData.map { (category, amount) ->
            PieEntry(amount.toFloat(), category.name)
        }

        val colors = listOf(
            Color.rgb(255, 99, 71),
            Color.rgb(255, 165, 0),
            Color.rgb(255, 215, 0),
            Color.rgb(144, 238, 144),
            Color.rgb(135, 206, 235)
        )

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextSize = 14f
            valueTextColor = Color.BLACK
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        }

        mBinding.pieChart.apply {
            data = PieData(dataSet).apply {
                setValueFormatter(PercentFormatter(mBinding.pieChart))
            }
            centerText = if (currentType == Bill.TYPE_EXPENSE) "支出分布" else "收入分布"
            invalidate()
        }
    }

    private fun updateRankList(categoryData: List<Pair<Category, Double>>) {
        val rankItems = categoryData.map { (category, amount) ->
            CategoryRankItem(
                category = category.name,
                amount = amount,
                percentage = (amount / categoryData.sumOf { it.second }).toFloat(),
                iconResId = getString(category.iconResId)
            )
        }
        categoryRankAdapter.submitList(rankItems)
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        if (isEmpty) {
            mBinding.contentView.visibility = View.GONE
            mBinding.llEmptyView.visibility = View.VISIBLE
        } else {
            mBinding.contentView.visibility = View.VISIBLE
            mBinding.llEmptyView.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        if (isViewCreated) {
            loadData()
        }
    }
} 