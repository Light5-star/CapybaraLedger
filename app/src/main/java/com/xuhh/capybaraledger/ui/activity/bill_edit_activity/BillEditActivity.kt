package com.xuhh.capybaraledger.ui.activity.bill_edit_activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.adapter.BillAdapter
import com.xuhh.capybaraledger.data.dao.BillWithCategory
import com.xuhh.capybaraledger.data.database.AppDatabase
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.data.model.Category
import com.xuhh.capybaraledger.data.model.Categories
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.databinding.ActivityBillEditBinding
import com.xuhh.capybaraledger.ui.base.BaseActivity
import com.xuhh.capybaraledger.ui.view.billtypeselect.CategoryDialog
import com.xuhh.capybaraledger.ui.view.ledgerselect.LedgerSelectorDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.ViewModelProvider
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.viewmodel.BillViewModel
import com.xuhh.capybaraledger.viewmodel.ViewModelFactory
import android.view.View
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

class BillEditActivity : BaseActivity<ActivityBillEditBinding>() {
    private lateinit var billAdapter: BillAdapter
    private val TAG = "BillEditActivity"
    private lateinit var database: AppDatabase
    private lateinit var viewModel: BillViewModel
    private var currentLedger: Ledger? = null
    private var currentCategory: Category? = null
    private var isExpense = true // true为支出，false为收入
    private var billId: Long = -1L // 账单ID，-1表示新建
    private var existingBill: Bill? = null // 已有账单数据

    override fun initBinding(): ActivityBillEditBinding {
        return ActivityBillEditBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        val app = application as App
        val factory = ViewModelFactory(app.ledgerRepository, app.billRepository)
        viewModel = ViewModelProvider(this, factory)[BillViewModel::class.java]

        database = AppDatabase.getInstance(this)
        
        // 获取传入的账单ID
        val extras = intent.extras
        Log.d(TAG, "Intent extras: ${extras?.keySet()?.joinToString { "$it: ${extras.get(it)}" }}")
        billId = intent.getLongExtra("bill_id", -1L)
        Log.d(TAG, "Received bill id: $billId")
        
        // 确保billId是有效的账单ID
        if (billId > 0) {
            lifecycleScope.launch(Dispatchers.IO) {
                val bill = database.billDao().getBillWithLogging(billId)
                if (bill == null) {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "账单ID: $billId 不存在")
                        showToast("找不到该账单数据，可能已被删除")
                        
                        // 设置错误码404并返回
                        val intent = Intent()
                        intent.putExtra("error_code", 404)
                        setResult(RESULT_CANCELED, intent)
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "找到账单数据，准备设置UI - 账单ID: ${bill.bill.id}, 金额: ${bill.bill.amount}, 分类ID: ${bill.bill.categoryId}, 账本ID: ${bill.bill.ledgerId}")
                        setupUIComponents()
                        setupEditMode()
                        loadBillData(billId)
                    }
                }
            }
        } else {
            // 新建模式
            Log.d(TAG, "新建账单模式")
            setupUIComponents()
            setupCreateMode()
            loadDefaultLedger()
            setupCategorySpinner() // 设置默认分类
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)  // 更新当前Intent
        // 处理新的Intent
        intent?.let {
            val extras = it.extras
            Log.d(TAG, "New intent received: ${extras?.keySet()?.joinToString { "$it: ${extras.get(it)}" }}")
            val newBillId = it.getLongExtra("bill_id", -1L)
            Log.d(TAG, "New bill id received: $newBillId")
            
            // 只有当新的billId与当前的不同时才重新加载
            if (newBillId != billId && newBillId > 0) {
                billId = newBillId
                // 重新设置UI状态
                setupUIComponents()
                setupEditMode()
                loadBillData(billId)
            }
        }
    }
    
    private fun setupUIComponents() {
        setupToolbar()
        setupLedgerSelector()
        setupTypeSelector()
        setupAmountInput()
        setupDatePicker()
        setupTimePicker()
        setUpCategorySelector()
        
        // 设置按钮点击事件
        mBinding.btnSave.setOnClickListener {
            saveBill()
        }
        
        mBinding.btnBack.setOnClickListener {
            finish()
        }
        
        mBinding.btnDelete.setOnClickListener {
            showDeleteConfirmDialog()
        }
        
        mBinding.btnUpdate.setOnClickListener {
            updateBill()
        }
    }
    
    private fun setupEditMode() {
        mBinding.tvTitle.text = "编辑账单"
        mBinding.btnSave.visibility = View.GONE
        mBinding.bottomActionBar.visibility = View.VISIBLE
    }
    
    private fun setupCreateMode() {
        mBinding.tvTitle.text = getString(R.string.bill_add)
        mBinding.btnSave.visibility = View.VISIBLE
        mBinding.bottomActionBar.visibility = View.GONE
    }
    
    private fun loadBillData(billId: Long) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "开始加载账单数据，ID: $billId")
                
                val billWithCategory = withContext(Dispatchers.IO) {
                    // 先尝试直接获取bill
                    val bill = database.billDao().getBillWithLogging(billId)
                    Log.d(TAG, "数据库查询结果 - 账单: ${bill != null}, ID: ${bill?.bill?.id}, 金额: ${bill?.bill?.amount}")
                    
                    if (bill != null) {
                        // 找到账单了，继续获取分类信息
                        val category = database.categoryDao().getCategoryById(bill.bill.categoryId)
                        Log.d(TAG, "获取分类结果: ${category != null}, 分类ID: ${bill.bill.categoryId}, 分类名称: ${category?.name}")
                        
                        // 验证账本是否存在
                        val ledger = database.ledgerDao().getLedgerById(bill.bill.ledgerId)
                        Log.d(TAG, "获取账本结果: ${ledger != null}, 账本ID: ${bill.bill.ledgerId}, 账本名称: ${ledger?.name}")
                        
                        if (ledger == null) {
                            Log.e(TAG, "账本ID ${bill.bill.ledgerId} 不存在")
                            return@withContext null
                        }
                        
                        // 如果找到分类，使用找到的分类
                        if (category != null) {
                            return@withContext bill
                        }
                        
                        // 如果分类不存在，使用默认分类
                        val defaultCategory = if (bill.bill.type == Bill.TYPE_EXPENSE) 
                            Categories.EXPENSE_CATEGORIES.last() else Categories.INCOME_CATEGORIES.last()
                        Log.d(TAG, "使用默认分类: ${defaultCategory.name}, ID: ${defaultCategory.id}")
                        return@withContext BillWithCategory(bill.bill, defaultCategory)
                    } else {
                        Log.e(TAG, "账单ID: $billId 不存在")
                        null
                    }
                }
                
                if (billWithCategory != null) {
                    Log.d(TAG, "成功加载账单数据，填充UI - 账单ID: ${billWithCategory.bill.id}, 金额: ${billWithCategory.bill.amount}, 分类ID: ${billWithCategory.bill.categoryId}, 账本ID: ${billWithCategory.bill.ledgerId}")
                    fillBillData(billWithCategory)
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "找不到ID为 $billId 的账单")
                        showToast("找不到该账单数据，可能已被删除")
                        
                        // 返回错误标识码，区分不同错误原因
                        setResult(RESULT_CANCELED, intent.putExtra("error_code", 404))
                        finish()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载账单数据失败: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showToast("加载账单数据失败: ${e.message}")
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
        }
    }
    
    private fun fillBillData(billWithCategory: BillWithCategory) {
        // 保存账单数据
        existingBill = billWithCategory.bill
        
        // 设置收支类型
        isExpense = billWithCategory.bill.type == Bill.TYPE_EXPENSE
        mBinding.rgType.check(if (isExpense) R.id.rb_expense else R.id.rb_income)
        
        // 设置金额
        mBinding.etAmount.setText(billWithCategory.bill.amount.toString())
        
        // 设置分类
        currentCategory = billWithCategory.category
        mBinding.tvCategory.text = currentCategory?.name
        
        // 设置备注和付款对象
        mBinding.etNote.setText(billWithCategory.bill.note ?: "")
        mBinding.etPayee.setText(billWithCategory.bill.payee ?: "")
        
        // 设置日期和时间
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        mBinding.tvDate.text = dateFormat.format(Date(billWithCategory.bill.date))
        mBinding.tvTime.text = timeFormat.format(Date(billWithCategory.bill.time))
        
        // 加载账本信息
        lifecycleScope.launch {
            try {
                val ledger = withContext(Dispatchers.IO) {
                    database.ledgerDao().getLedgerById(billWithCategory.bill.ledgerId)
                }
                if (ledger != null) {
                    currentLedger = ledger
                    mBinding.tvLedger.text = ledger.name
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading ledger data", e)
            }
        }
    }

    private fun showDeleteConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("删除确认")
            .setMessage("确定要删除这条账单记录吗？此操作不可恢复。")
            .setPositiveButton("删除") { _, _ ->
                deleteBill()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun deleteBill() {
        if (billId == -1L) return
        
        lifecycleScope.launch {
            try {
                Log.d(TAG, "开始删除账单，ID: $billId")
                
                val result = withContext(Dispatchers.IO) {
                    // 使用事务方法删除账单
                    val success = database.billDao().deleteBillWithTransaction(billId)
                    if (success) {
                        Log.d(TAG, "账单删除成功，ID: $billId")
                    } else {
                        Log.e(TAG, "账单删除失败，ID: $billId 可能不存在或删除失败")
                    }
                    success
                }
                
                withContext(Dispatchers.Main) {
                    if (result) {
                        showToast("账单已删除")
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        showToast("删除操作未成功完成")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "删除账单时发生错误: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showToast("删除失败: ${e.message}")
                }
            }
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateBill() {
        // 验证输入
        val amount = mBinding.etAmount.text.toString().toDoubleOrNull()
        if (amount == null || amount <= 0) {
            showToast("请输入有效金额")
            return
        }

        if (currentCategory == null) {
            showToast("请选择分类")
            return
        }

        if (currentLedger == null) {
            showToast("请选择账本")
            return
        }

        // 获取日期和时间
        val dateStr = mBinding.tvDate.text.toString()
        val timeStr = mBinding.tvTime.text.toString()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        val date = dateFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
        val time = timeFormat.parse(timeStr)?.time ?: System.currentTimeMillis()

        Log.d(TAG, "开始更新账单 - ID: $billId, 金额: $amount, 分类ID: ${currentCategory!!.id}, 账本ID: ${currentLedger!!.id}")

        // 创建账单对象
        val bill = Bill(
            id = billId,
            amount = amount,
            type = if (isExpense) Bill.TYPE_EXPENSE else Bill.TYPE_INCOME,
            categoryId = currentCategory!!.id,
            ledgerId = currentLedger!!.id,
            date = date,
            time = time,
            note = mBinding.etNote.text.toString(),
            payee = mBinding.etPayee.text.toString()
        )

        // 更新数据库
        lifecycleScope.launch {
            try {
                Log.d(TAG, "开始执行数据库更新操作")
                
                // 先验证账单是否存在
                val existingBill = withContext(Dispatchers.IO) {
                    database.billDao().getBillById(billId)
                }
                
                if (existingBill == null) {
                    Log.e(TAG, "更新失败：账单ID $billId 不存在")
                    showToast("找不到该账单数据，可能已被删除")
                    setResult(RESULT_CANCELED)
                    finish()
                    return@launch
                }
                
                // 验证分类是否存在
                val category = withContext(Dispatchers.IO) {
                    database.categoryDao().getCategoryById(bill.categoryId)
                }
                
                if (category == null) {
                    Log.e(TAG, "更新失败：分类ID ${bill.categoryId} 不存在")
                    showToast("所选分类不存在，请重新选择")
                    return@launch
                }
                
                // 验证账本是否存在
                val ledger = withContext(Dispatchers.IO) {
                    database.ledgerDao().getLedgerById(bill.ledgerId)
                }
                
                if (ledger == null) {
                    Log.e(TAG, "更新失败：账本ID ${bill.ledgerId} 不存在")
                    showToast("所选账本不存在，请重新选择")
                    return@launch
                }
                
                // 执行更新操作
                val result = withContext(Dispatchers.IO) {
                    database.billDao().updateBillWithTransaction(bill)
                }
                
                Log.d(TAG, "数据库更新操作完成，结果: $result")
                
                if (result) {
                    // 通知数据已更新
                    setResult(RESULT_OK)
                    
                    // 刷新数据
                    withContext(Dispatchers.Main) {
                        // 强制刷新数据
                        viewModel.loadBillsForCurrentLedger()
                        showToast("更新成功")
                    }
                    
                    // 关闭当前页面
                    finish()
                } else {
                    Log.e(TAG, "更新账单失败 - 数据库操作返回false")
                    showToast("更新账单失败，请重试")
                }
            } catch (e: Exception) {
                Log.e(TAG, "更新账单失败: ${e.message}", e)
                showToast("更新账单失败: ${e.message}")
            }
        }
    }

    private fun setUpCategorySelector() {
        mBinding.tvCategory.setOnClickListener {
            // 根据收支类型获取对应分类
            val categories = if (isExpense) {
                Categories.EXPENSE_CATEGORIES
            } else {
                Categories.INCOME_CATEGORIES
            }

            // 创建分类选择弹窗
            CategoryDialog(
                context = this,
                categories = categories,
                selectedCategory = currentCategory,
                onCategorySelected = { category ->
                    if (category != null) {
                        currentCategory = category
                        mBinding.tvCategory.text = category.name
                        Log.d(TAG, "选择分类 - ID: ${category.id}, 名称: ${category.name}")
                    } else {
                        Log.e(TAG, "选择分类失败 - 分类为空")
                    }
                }
            ).show()
        }
    }

    private fun setupLedgerSelector() {
        mBinding.tvLedger.setOnClickListener {
            LedgerSelectorDialog(
                this,
                viewModel
            ) { ledger ->
                currentLedger = ledger
                mBinding.tvLedger.text = ledger.name
            }.show()
        }
    }

    private fun setupTypeSelector() {
        mBinding.rgType.setOnCheckedChangeListener { _, checkedId ->
            isExpense = checkedId == R.id.rb_expense
            // 保存当前分类ID
            val currentCategoryId = currentCategory?.id
            setupCategorySpinner()
            // 如果之前有分类，尝试找到相同ID的分类
            if (currentCategoryId != null) {
                val categories = if (isExpense) {
                    Categories.EXPENSE_CATEGORIES
                } else {
                    Categories.INCOME_CATEGORIES
                }
                currentCategory = categories.find { it.id == currentCategoryId }
                if (currentCategory != null) {
                    mBinding.tvCategory.text = currentCategory?.name
                }
            }
        }
    }

    private fun setupCategorySpinner() {
        val categories = if (isExpense) {
            Categories.EXPENSE_CATEGORIES
        } else {
            Categories.INCOME_CATEGORIES
        }
        
        // 更新UI显示分类
        if (categories.isNotEmpty()) {
            // 如果当前没有分类，才设置默认分类
            if (currentCategory == null) {
                currentCategory = categories[0]
                mBinding.tvCategory.text = currentCategory?.name
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
        }
    }

    private fun loadDefaultLedger() {
        lifecycleScope.launch {
            try {
                val defaultLedger = withContext(Dispatchers.IO) {
                    val ledgers = database.ledgerDao().getAllLedgers()
                    ledgers.find { it.isDefault } ?: ledgers.firstOrNull()
                }
                
                defaultLedger?.let {
                    currentLedger = it
                    mBinding.tvLedger.text = it.name
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading default ledger", e)
            }
        }
    }

    private fun setupAmountInput() {
        mBinding.etAmount.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && mBinding.etAmount.text.toString().isBlank()) {
                // 失去焦点且内容为空时，显示提示文字
                mBinding.etAmount.hint = getString(R.string.bill_amount_hint)
            } else if (hasFocus) {
                // 获得焦点时，隐藏提示文字
                mBinding.etAmount.hint = ""
            }
        }

        mBinding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // 可以在这里添加金额格式化的逻辑
            }
        })
    }
    
    //日期选择器
    private fun setupDatePicker() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = Date()
        mBinding.tvDate.text = dateFormat.format(currentDate)
        mBinding.tvDate.setOnClickListener {
            // 获取当前日期（用于初始化选择器）
            val calendar = Calendar.getInstance()
            val initialYear = calendar.get(Calendar.YEAR)
            val initialMonth = calendar.get(Calendar.MONTH)
            val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

            // 创建日期选择对话框
            DatePickerDialog(
                this, // Context
                { _, year, month, dayOfMonth -> // 日期选择回调
                    // 构造选中的日期
                    val selectedDate = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }.time

                    // 更新界面显示
                    mBinding.tvDate.text = dateFormat.format(selectedDate)
                },
                initialYear,   // 默认显示年份
                initialMonth,  // 默认显示月份（0-11）
                initialDay     // 默认显示日期
            ).apply {
                // 设置对话框标题
                setTitle("请选择日期")
                // 显示对话框
                show()
            }
        }
    }

    //时间选择器
    private fun setupTimePicker() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = Date()
        mBinding.tvTime.text = timeFormat.format(currentTime)

        mBinding.tvTime.setOnClickListener {
            // 获取当前时间（用于初始化选择器）
            val calendar = Calendar.getInstance()
            val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
            val initialMinute = calendar.get(Calendar.MINUTE)

            // 创建时间选择对话框
            TimePickerDialog(
                this, // Context
                { _, hourOfDay, minute -> // 时间选择回调
                    // 构造选中的时间
                    val selectedTime = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hourOfDay)
                        set(Calendar.MINUTE, minute)
                    }.time

                    // 更新界面显示
                    mBinding.tvTime.text = timeFormat.format(selectedTime)
                },
                initialHour,  // 默认显示小时
                initialMinute, // 默认显示分钟
                true          // 是否使用24小时制
            ).apply {
                // 设置对话框标题
                setTitle("请选择时间")
                // 显示对话框
                show()
            }
        }
    }
    
    // 获取当前日期的时间戳，只保留年月日部分
    private fun getCurrentDate(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun saveBill() {
        // 1. 验证输入
        val amountStr = mBinding.etAmount.text.toString()
        if (amountStr.isBlank()) {
            showToast("请输入金额")
            return
        }

        val amount = try {
            amountStr.toDouble()
        } catch (e: Exception) {
            showToast("金额格式不正确")
            return
        }

        if (amount <= 0) {
            showToast("金额必须大于0")
            return
        }

        if (currentLedger == null) {
            showToast("请选择账本")
            return
        }

        if (currentCategory == null) {
            showToast("请选择分类")
            return
        }

        val note = mBinding.etNote.text.toString()
        val payee = mBinding.etPayee.text.toString()

        lifecycleScope.launch {
            try {
                // 2. 获取日期和时间
                val dateStr = mBinding.tvDate.text.toString()
                val timeStr = mBinding.tvTime.text.toString()

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                val date = dateFormat.parse(dateStr)?.time
                val time = timeFormat.parse(timeStr)?.time

                if (date == null || time == null) {
                    showToast("日期或时间格式错误")
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    // 首先检查分类是否存在
                    var categoryToUse = database.categoryDao().getCategoryByName(currentCategory!!.name)
                    
                    if (categoryToUse == null) {
                        // 如果分类不存在，先插入分类
                        val categoryId = database.categoryDao().insert(currentCategory!!)
                        
                        // 重新获取插入后的分类
                        categoryToUse = database.categoryDao().getCategoryByName(currentCategory!!.name)
                        
                        if (categoryToUse == null) {
                            throw Exception("分类插入失败")
                        }
                    }

                    // 检查账本是否存在
                    val existingLedger = database.ledgerDao().getLedgerById(currentLedger!!.id)

                    if (existingLedger == null) {
                        throw Exception("账本不存在")
                    }

                    // 创建账单对象
                    val bill = Bill(
                        ledgerId = currentLedger!!.id,
                        categoryId = categoryToUse.id,
                        amount = amount,
                        type = if (isExpense) Bill.TYPE_EXPENSE else Bill.TYPE_INCOME,
                        date = date,
                        time = time,
                        note = if (note.isBlank()) null else note,
                        payee = if (payee.isBlank()) null else payee
                    )

                    // 插入账单
                    val billId = database.billDao().insert(bill)
                    Log.d(TAG, "Bill inserted with ID: $billId")

                    // 验证账单是否真的保存了
                    val savedBill = database.billDao().getBillById(billId)
                    Log.d(TAG, "Saved bill verification: ${savedBill?.id}, ${savedBill?.categoryId}, ${savedBill?.ledgerId}")
                }

                // 显示成功提示并返回
                withContext(Dispatchers.Main) {
                    showToast("保存成功")
                    setResult(RESULT_OK)
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving bill", e)
                withContext(Dispatchers.Main) {
                    showToast("保存失败：${e.message}")
                }
            }
        }
    }
}