package com.xuhh.capybaraledger.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xuhh.capybaraledger.data.dao.BillWithCategory
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.data.repository.BillRepository
import com.xuhh.capybaraledger.data.repository.LedgerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
class BillViewModel(
    private val ledgerRepository: LedgerRepository,
    private val billRepository: BillRepository
) : ViewModel() {
    private val _currentLedger = MutableStateFlow<Ledger?>(null)
    val currentLedger: StateFlow<Ledger?> = _currentLedger.asStateFlow()

    private val _bills = MutableStateFlow<List<BillWithCategory>>(emptyList())
    val bills: StateFlow<List<BillWithCategory>> = _bills.asStateFlow()

    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    private val _ledgers = MutableStateFlow<List<Ledger>>(emptyList())
    val ledgers: StateFlow<List<Ledger>> = _ledgers.asStateFlow()

    private val _currentCalendar = MutableStateFlow(Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    })
    val currentCalendar: StateFlow<Calendar> = _currentCalendar.asStateFlow()

    init {
        loadLedgers()
        loadDefaultLedger()
    }

    private fun loadLedgers() {
        viewModelScope.launch {
            ledgerRepository.getAllLedgersFlow().collect { ledgers ->
                _ledgers.value = ledgers
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadDefaultLedger() {
        viewModelScope.launch {
            val defaultLedger = ledgerRepository.getDefaultLedger()
            defaultLedger?.let { 
                updateCurrentLedger(it)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateCurrentLedger(ledger: Ledger) {
        viewModelScope.launch {
            _currentLedger.value = ledger
            loadBillsForCurrentLedger()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadBillsForCurrentLedger() {
        viewModelScope.launch {
            val ledgerId = _currentLedger.value?.id ?: return@launch
            
            try {
                Log.d("BillViewModel", "开始加载当前账本(ID:$ledgerId)的账单数据")
                // 获取今天的时间范围
                val (startTime, endTime) = getCurrentDayRange()
                
                // 加载账单
                val bills = billRepository.getBillsByDateRange(
                    ledgerId = ledgerId,
                    startDate = startTime,
                    endDate = endTime
                )
                
                Log.d("BillViewModel", "成功获取账单数据，共${bills.size}条")
                
                // 确保在主线程更新UI，并创建新的列表实例
                withContext(Dispatchers.Main) {
                    _bills.value = bills.toList()
                }
                
                // 计算余额
                calculateDailyBalance(ledgerId, startTime, endTime)
            } catch (e: Exception) {
                // 处理错误
                Log.e("BillViewModel", "加载账单数据失败: ${e.message}")
                withContext(Dispatchers.Main) {
                    _bills.value = emptyList()
                    _balance.value = 0.0
                }
            }
        }
    }

    private suspend fun calculateDailyBalance(ledgerId: Long, startDate: Long, endDate: Long) {
        val income = billRepository.getAmountByType(ledgerId, Bill.TYPE_INCOME, startDate, endDate)
        val expense = billRepository.getAmountByType(ledgerId, Bill.TYPE_EXPENSE, startDate, endDate)
        _balance.value = income - expense
    }

    private fun parseDateToTimestamp(date: String): Long {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.parse(date)?.time ?: 0L
    }

    fun createLedger(name: String) {
        viewModelScope.launch {
            val ledger = Ledger(name = name)
            ledgerRepository.createLedger(ledger)
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // 获取指定月份的账单
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getBillsByMonth(ledgerId: Long, startDate: String, endDate: String) =
        billRepository.getBillsByLedgerAndMonth(ledgerId, startDate, endDate)

    // 获取指定日期的账单
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getBillsByDate(date: String, ledgerId: Long?) =
        billRepository.getBillsByDate(date, ledgerId)

    // 获取指定时间范围内的账单数据
    suspend fun getBillsWithCategoryByTimeRange(
        ledgerId: Long,
        startTime: Long,
        endTime: Long
    ): List<BillWithCategory> {
        return billRepository.getBillsWithCategoryByTimeRange(ledgerId, startTime, endTime)
    }

    fun nextMonth() {
        val currentCalendar = _currentCalendar.value
        Log.d("BillViewModel", "nextMonth: before=${currentCalendar.get(Calendar.MONTH)}")
        
        val newCalendar = currentCalendar.clone() as Calendar
        newCalendar.add(Calendar.MONTH, 1)
        Log.d("BillViewModel", "nextMonth: after=${newCalendar.get(Calendar.MONTH)}")
        _currentCalendar.value = newCalendar
    }

    fun backMonth() {
        val currentCalendar = _currentCalendar.value
        Log.d("BillViewModel", "backMonth: before=${currentCalendar.get(Calendar.MONTH)}")
        
        val newCalendar = currentCalendar.clone() as Calendar
        newCalendar.add(Calendar.MONTH, -1)
        Log.d("BillViewModel", "backMonth: after=${newCalendar.get(Calendar.MONTH)}")
        _currentCalendar.value = newCalendar
    }

    fun setCurrentMonth(year: Int, month: Int) {
        Log.d("BillViewModel", "设置月份: year=$year, month=$month")
        val newCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        _currentCalendar.value = newCalendar
    }

    fun getCurrentMonthRange(): Pair<Long, Long> {
        val currentCalendar = _currentCalendar.value.clone() as Calendar
        val year = currentCalendar.get(Calendar.YEAR)
        val month = currentCalendar.get(Calendar.MONTH)
        Log.d("BillViewModel", "Getting range for year=$year, month=$month")

        // 设置为月初
        val startCalendar = Calendar.getInstance().apply {
            clear()  // 清除所有字段
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = startCalendar.timeInMillis

        // 设置为月末
        val endCalendar = Calendar.getInstance().apply {
            clear()  // 清除所有字段
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }
        val endTime = endCalendar.timeInMillis

        Log.d("BillViewModel", "Time range for month $month: ${startCalendar.time} to ${endCalendar.time}")
        return Pair(startTime, endTime)
    }

    // 新增方法：获取今天的开始和结束时间戳
    private fun getCurrentDayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        // 设置为今天的开始时间 (00:00:00.000)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        // 设置为今天的结束时间 (23:59:59.999)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endTime = calendar.timeInMillis

        Log.d("BillViewModel", "Today's range: ${Date(startTime)} to ${Date(endTime)}")
        return Pair(startTime, endTime)
    }

    fun setDefaultLedger(ledgerId: Long) {
        viewModelScope.launch {
            ledgerRepository.setDefaultLedger(ledgerId)
        }
    }

    fun deleteLedger(ledgerId: Long) {
        viewModelScope.launch {
            ledgerRepository.deleteLedger(ledgerId)
        }
    }

    fun isLedgerNameExists(name: String): Boolean {
        return _ledgers.value.any { it.name == name }
    }
}