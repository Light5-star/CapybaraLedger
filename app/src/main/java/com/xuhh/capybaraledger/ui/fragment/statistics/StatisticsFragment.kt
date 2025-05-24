package com.xuhh.capybaraledger.ui.fragment.statistics

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.databinding.FragmentStatisticsBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.ui.view.ledgerselect.LedgerSelectorDialog
import com.xuhh.capybaraledger.viewmodel.BillViewModel
import com.xuhh.capybaraledger.viewmodel.StatisticsViewModel
import com.xuhh.capybaraledger.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatisticsFragment : BaseFragment<FragmentStatisticsBinding>() {
    private var currentMode = Mode.TREND
    private val mViewModel: BillViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.ledgerRepository, app.billRepository)
    }
    private val statisticsViewModel: StatisticsViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initBinding(): FragmentStatisticsBinding {
        return FragmentStatisticsBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        setupViewPager()
        setupLedgerSelector()
        setupMonthSelector()
        setupModeSwitch()
        setupObservers()
        updateModeUI()
    }

    private fun setupViews() {
        updateModeUI()
        setupLedgerSelector()
        setupModeSwitch()
        setupViewPager()
        setupMonthSelector()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            mViewModel.currentLedger.collect { ledger ->
                ledger?.let {
                    mBinding.tvLedgerName.text = it.name
                    loadStatisticsData()
                }
            }
        }
    }

    private fun setupViewPager() {
        mBinding.viewPager.apply {
            isUserInputEnabled = false
            adapter = StatisticsPagerAdapter(childFragmentManager, lifecycle)
        }

        mBinding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentMode = when (position) {
                    0 -> Mode.TREND
                    1 -> Mode.RANK
                    2 -> Mode.ANALYSIS
                    else -> Mode.TREND
                }
                updateModeUI()
            }
        })
    }

    private fun setupLedgerSelector() {
        mBinding.layoutLedger.setOnClickListener {
            LedgerSelectorDialog(
                requireContext(),
                mViewModel
            ) { ledger ->
                loadStatisticsData()
            }.show()
        }
    }

    private fun loadStatisticsData() {
        val adapter = mBinding.viewPager.adapter as? StatisticsPagerAdapter
        val currentFragment = adapter?.getCurrentFragment(mBinding.viewPager.currentItem)
        when (currentFragment) {
            is StatisticsTrendFragment -> currentFragment.loadData()
            is StatisticsRankFragment -> currentFragment.loadData()
            is StatisticsAnalysisFragment -> currentFragment.loadData()
        }
    }

    private fun updateModeUI() {
        // 更新趋势模式按钮样式
        mBinding.btnTrendMode.apply {
            setTextColor(
                if (currentMode == Mode.TREND) {
                    ContextCompat.getColor(requireContext(), R.color.accent)
                } else {
                    ContextCompat.getColor(requireContext(), R.color.text_primary)
                }
            )
            setBackgroundResource(
                if (currentMode == Mode.TREND) {
                    R.drawable.bg_mode_switch_item_selected
                } else {
                    R.drawable.bg_mode_switch_item
                }
            )
        }

        // 更新排行模式按钮样式
        mBinding.btnRankMode.apply {
            setTextColor(
                if (currentMode == Mode.RANK) {
                    ContextCompat.getColor(requireContext(), R.color.accent)
                } else {
                    ContextCompat.getColor(requireContext(), R.color.text_primary)
                }
            )
            setBackgroundResource(
                if (currentMode == Mode.RANK) {
                    R.drawable.bg_mode_switch_item_selected
                } else {
                    R.drawable.bg_mode_switch_item
                }
            )
        }

        // 更新分析模式按钮样式
        mBinding.btnAnalysisMode.apply {
            setTextColor(
                if (currentMode == Mode.ANALYSIS) {
                    ContextCompat.getColor(requireContext(), R.color.accent)
                } else {
                    ContextCompat.getColor(requireContext(), R.color.text_primary)
                }
            )
            setBackgroundResource(
                if (currentMode == Mode.ANALYSIS) {
                    R.drawable.bg_mode_switch_item_selected
                } else {
                    R.drawable.bg_mode_switch_item
                }
            )
        }
    }

    private fun setupModeSwitch() {
        mBinding.btnTrendMode.setOnClickListener {
            if (currentMode != Mode.TREND) {
                currentMode = Mode.TREND
                updateModeUI()
                mBinding.viewPager.currentItem = 0
            }
        }

        mBinding.btnRankMode.setOnClickListener {
            if (currentMode != Mode.RANK) {
                currentMode = Mode.RANK
                updateModeUI()
                mBinding.viewPager.currentItem = 1
            }
        }

        mBinding.btnAnalysisMode.setOnClickListener {
            if (currentMode != Mode.ANALYSIS) {
                currentMode = Mode.ANALYSIS
                updateModeUI()
                mBinding.viewPager.currentItem = 2
            }
        }
    }

    private fun setupMonthSelector() {
        // 观察月份变化
        viewLifecycleOwner.lifecycleScope.launch {
            mViewModel.currentCalendar.collect { calendar ->
                updateMonthDisplay(calendar)
                loadStatisticsData()
            }
        }

        mBinding.btnPrevMonth.setOnClickListener {
            mViewModel.backMonth()
        }

        mBinding.btnNextMonth.setOnClickListener {
            mViewModel.nextMonth()
        }
    }

    private fun updateMonthDisplay(calendar: Calendar) {
        val sdf = SimpleDateFormat("yyyy年MM月", Locale.getDefault())
        mBinding.tvMonth.text = sdf.format(calendar.time)
    }

    private enum class Mode {
        TREND, RANK, ANALYSIS
    }
}