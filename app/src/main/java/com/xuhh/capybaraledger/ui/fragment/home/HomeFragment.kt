package com.xuhh.capybaraledger.ui.fragment.home

import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.adapter.BillAdapter
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.databinding.FragmentHomeBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.ui.view.ledgerselect.LedgerSelectorDialog
import com.xuhh.capybaraledger.viewmodel.BillViewModel
import com.xuhh.capybaraledger.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class HomeFragment: BaseFragment<FragmentHomeBinding>() {
    private val mViewModel: BillViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.ledgerRepository, app.billRepository)
    }
    private lateinit var billAdapter: BillAdapter
    private var isViewCreated = false

    override fun initBinding(): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initView() {
        super.initView()
        setupViews()
        observeViewModel()
        isViewCreated = true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        if (isViewCreated) {
            mViewModel.loadBillsForCurrentLedger()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupViews() {
        setDate()
        setQuote()
        setupLedgerSelector()
        setupRecyclerView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // 观察当前账本
            mViewModel.currentLedger.collect { ledger ->
                ledger?.let {
                    mBinding.tvLedgerName.text = it.name
                    // 账本变更时强制刷新账单数据
                    mViewModel.loadBillsForCurrentLedger()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 观察账单列表
            mViewModel.bills.collect { bills ->
                billAdapter.submitSortedList(bills)
                mBinding.tvEmpty.visibility = if (bills.isEmpty()) View.VISIBLE else View.GONE
                Log.d("HomeFragment", "收到新的账单数据列表，条数: ${bills.size}")
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 观察余额
            mViewModel.balance.collect { balance ->
                mBinding.tvBalance.text = "今日结余：${String.format("%.2f", balance)}"
            }
        }
        
        // 注册生命周期事件监听，确保页面恢复时刷新数据
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                mViewModel.loadBillsForCurrentLedger()
                Log.d("HomeFragment", "页面恢复，刷新账单数据")
            }
        })
    }

    private fun setupLedgerSelector() {
        mBinding.layoutLedger.setOnClickListener {
            LedgerSelectorDialog(
                requireContext(),
                mViewModel
            ) { _ -> 
                // 账本选择的处理已经在 Dialog 中完成
            }.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupRecyclerView() {
        billAdapter = BillAdapter { billWithCategory ->
            try {
                // 先验证账单和分类数据是否有效
                if (billWithCategory.bill.id > 0 && billWithCategory.category.id > 0) {
                    Log.d("HomeFragment", """
                        点击账单:
                        账单ID: ${billWithCategory.bill.id}
                        分类ID: ${billWithCategory.category.id}
                    """.trimIndent())
                    
                    // 处理账单点击事件
                    val mainActivity = requireActivity() as com.xuhh.capybaraledger.MainActivity
                    mainActivity.startBillEdit(
                        ledgerId = billWithCategory.bill.ledgerId,
                        selectedDate = billWithCategory.bill.date,
                        billId = billWithCategory.bill.id  // 确保使用账单ID而不是分类ID
                    )
                } else {
                    // 账单数据无效，提示用户并刷新
                    showToast("账单数据已失效，正在刷新...")
                    mViewModel.loadBillsForCurrentLedger()
                }
            } catch (e: Exception) {
                // 处理可能的异常
                Log.e("HomeFragment", "处理账单点击事件失败", e)
                showToast("无法打开账单详情，请重试")
                mViewModel.loadBillsForCurrentLedger()
            }
        }

        mBinding.rvBills.apply {
            adapter = billAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    //设置每日一句
    private fun setQuote() {
        val quotes = resources.getStringArray(R.array.daily_quotes)
        val randomQuote = quotes[Random().nextInt(quotes.size)]
        mBinding.tvQuote.text = randomQuote
    }

    //设置日期
    private fun setDate() {
        val dateFormat = SimpleDateFormat("MM月dd日 E", Locale.CHINESE)
        mBinding.tvDate.text = dateFormat.format(Date())
    }
}