package com.xuhh.capybaraledger

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.databinding.ActivityMainBinding
import com.xuhh.capybaraledger.ui.activity.bill_edit_activity.BillEditActivity
import com.xuhh.capybaraledger.ui.base.BaseActivity
import com.xuhh.capybaraledger.ui.view.unicode.UnicodeTextView
import com.xuhh.capybaraledger.utils.ThemeHelper
import com.xuhh.capybaraledger.viewmodel.BillViewModel
import com.xuhh.capybaraledger.viewmodel.ViewModelFactory
import java.util.Calendar

class MainActivity : BaseActivity<ActivityMainBinding>() {
    // 定义请求码常量
    companion object {
        const val REQUEST_BILL_DETAIL = 1001
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val billEditLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 无论结果如何，都刷新数据，确保界面显示最新状态
        refreshAllData()

        // 检查是否有特殊错误码，如404（账单不存在）
        val errorCode = result.data?.getIntExtra("error_code", 0) ?: 0

        if (errorCode == 404) {
            // 处理账单不存在的情况
            Log.d("MainActivity", "账单编辑页面返回，账单不存在，错误码：404")
            showToast("该账单不存在或已被删除")
        } else {
            // 正常情况处理
            Log.d("MainActivity", "账单编辑页面返回，刷新数据，结果码：${result.resultCode}")
        }
    }

    val sharedBillViewModel: BillViewModel by viewModels {
        ViewModelFactory(
            (application as App).ledgerRepository,
            (application as App).billRepository
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun refreshAllData() {
        // 刷新所有数据
        Log.d("MainActivity", "刷新所有数据")

        // 强制刷新当前账本的账单数据
        try {
            // 刷新首页数据
            sharedBillViewModel.loadBillsForCurrentLedger()

            // 刷新明细页面数据
            sharedBillViewModel.currentCalendar.value?.let {
                // 触发一次日期变化，强制刷新明细页
                sharedBillViewModel.setCurrentMonth(it.get(Calendar.YEAR), it.get(Calendar.MONTH))
            }

            Log.d("MainActivity", "数据刷新请求已发送")
        } catch (e: Exception) {
            Log.e("MainActivity", "刷新数据时出错", e)
            showToast("刷新数据失败，请重试")
        }
    }

    override fun initBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initView() {
        settingBottomNav()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun settingBottomNav() {
        val bottomNavigationView = mBinding?.navView
        // 为每个菜单项设置自定义图标
        if (bottomNavigationView != null) {
            bottomNavigationView.menu.apply {
                findItem(R.id.navigation_home).icon = createFontIcon(R.string.icon_home)
                findItem(R.id.navigation_details).icon = createFontIcon(R.string.icon_mingxi)
                findItem(R.id.navigation_add).icon = createFontIcon(R.string.icon_chuangjian_tianjia_piliang_tianjia)
                findItem(R.id.navigation_statistics).icon = createFontIcon(R.string.icon_tubiao)
                findItem(R.id.navigation_profile).icon = createFontIcon(R.string.icon_wode)
            }
        }

        val navView: BottomNavigationView = mBinding?.navView!!
        // 获取 NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        // 获取 NavController
        val navController = navHostFragment.navController

        // 设置底部导航与导航控制器的关联
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                    true
                }
                R.id.navigation_details -> {
                    navController.navigate(R.id.navigation_details)
                    true
                }
                R.id.navigation_add -> {
                    // 打开记账界面
                    startBillEdit()
                    false  // 返回false表示不切换选中状态
                }
                R.id.navigation_statistics -> {
                    navController.navigate(R.id.navigation_statistics)
                    true
                }
                R.id.navigation_profile -> {
                    navController.navigate(R.id.navigation_profile)
                    true
                }
                else -> false
            }
        }

        // 设置默认选中的页面
        navController.navigate(R.id.navigation_home)
    }

    private fun createFontIcon(stringRes: Int): BitmapDrawable {
        val iconView = UnicodeTextView(this)
        iconView.text = getString(stringRes)
        iconView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        iconView.setTextColor(ContextCompat.getColor(this, R.color.text_primary))

        // 测量并布局 View
        val widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        iconView.measure(widthSpec, heightSpec)
        iconView.layout(0, 0, iconView.measuredWidth, iconView.measuredHeight)

        // 创建 Bitmap并绘制
        val bitmap = Bitmap.createBitmap(iconView.measuredWidth, iconView.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        iconView.draw(canvas)

        return BitmapDrawable(resources, bitmap)
    }

    // 修改启动记账页面的方法
    @RequiresApi(Build.VERSION_CODES.O)
    fun startBillEdit(ledgerId: Long = -1L, selectedDate: Long = -1L, billId: Long = -1L) {
        try {
            Log.d("MainActivity", "Starting bill edit with billId: $billId")
            val intent = Intent(this, BillEditActivity::class.java).apply {
                putExtra("ledger_id", ledgerId)
                putExtra("selected_date", selectedDate)
                putExtra("bill_id", billId)
                // 添加标志以确保每次创建新的实例
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            Log.d("MainActivity", "Intent extras - bill_id: ${intent.getLongExtra("bill_id", -1L)}")
            billEditLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e("MainActivity", "启动账单编辑页面失败: ${e.message}")
            showToast("无法打开账单详情，请刷新后重试")
            refreshAllData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        // ... 其他代码
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 刷新所有的数据
        Log.d(TAG, "刷新所有数据")
        refreshAllData()

        if (requestCode == REQUEST_BILL_DETAIL) {
            // 账单编辑页面返回
            val errorCode = data?.getIntExtra("error_code", 0) ?: 0

            if (errorCode == 404) {
                // 处理账单不存在的情况
                Log.d(TAG, "账单编辑页面返回，账单不存在，错误码：404")
            } else {
                // 正常返回的情况
                Log.d(TAG, "账单编辑页面返回，刷新数据，结果码：$resultCode")
            }
        }
    }
}