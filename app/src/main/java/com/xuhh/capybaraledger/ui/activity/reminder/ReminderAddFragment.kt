package com.xuhh.capybaraledger.ui.activity.reminder

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.data.model.Reminder
import com.xuhh.capybaraledger.data.model.ReminderNotifyType
import com.xuhh.capybaraledger.data.model.ReminderRepeatType
import com.xuhh.capybaraledger.databinding.FragmentReminderAddBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.viewmodel.ReminderViewModel
import kotlinx.coroutines.launch

class ReminderAddFragment : BaseFragment<FragmentReminderAddBinding>() {
    private val viewModel: ReminderViewModel by activityViewModels { 
        ReminderViewModel.Factory(
            (requireActivity().application as App).reminderRepository,
            requireContext()
        )
    }
    
    private var reminderName: String = ""
    private var currentRepeatType = ReminderRepeatType.ONCE
    private var currentNotifyType = ReminderNotifyType.NOTIFICATION
    private var customDays: List<Int>? = null
    private var isOddWeek: Boolean = true
    private var editingReminder: Reminder? = null

    override fun initBinding(): FragmentReminderAddBinding {
        return FragmentReminderAddBinding.inflate(layoutInflater)
    }

    override fun initView() {
        setupViews()
        
        // 从参数中获取 reminderId 并加载数据
        arguments?.getLong("reminder_id", -1L)?.let { reminderId ->
            if (reminderId != -1L) {
                loadReminder(reminderId)
            }
        }
    }

    private fun setupViews() {
        // 重复选择点击事件
        mBinding.llRepeat.setOnClickListener {
            showRepeatTypeDialog()
        }

        // 提醒方式点击事件
        mBinding.llNotify.root.setOnClickListener {
            showNotifyTypeDialog()
        }

        // 提醒名称点击事件
        mBinding.llName.setOnClickListener {
            showNameDialog()
        }

        // 初始化默认显示
        mBinding.tvNameValue.text = reminderName.ifEmpty { "请输入提醒名称" }
        updateRepeatTypeText()
        updateNotifyTypeText()
    }

    private fun showRepeatTypeDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_repeat_type, null)
        dialog.setContentView(dialogView)

        // 创建临时变量存储选择状态
        var tempRepeatType = currentRepeatType
        var tempCustomDays = customDays?.toList()
        var tempIsOddWeek = isOddWeek

        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.rg_repeat_type)
        val weekdaySelector = dialogView.findViewById<ViewGroup>(R.id.weekday_selector)
        val alternateRestSelector = dialogView.findViewById<ViewGroup>(R.id.alternate_rest_selector)
        val weekdayViews = listOf(
            weekdaySelector.findViewById<TextView>(R.id.tv_sunday),
            weekdaySelector.findViewById<TextView>(R.id.tv_monday),
            weekdaySelector.findViewById<TextView>(R.id.tv_tuesday),
            weekdaySelector.findViewById<TextView>(R.id.tv_wednesday),
            weekdaySelector.findViewById<TextView>(R.id.tv_thursday),
            weekdaySelector.findViewById<TextView>(R.id.tv_friday),
            weekdaySelector.findViewById<TextView>(R.id.tv_saturday)
        )

        // 设置单双休选择器
        val oddWeekView = alternateRestSelector.findViewById<TextView>(R.id.tv_odd_week)
        val evenWeekView = alternateRestSelector.findViewById<TextView>(R.id.tv_even_week)

        // 设置当前选中状态
        when (tempRepeatType) {
            ReminderRepeatType.CUSTOM -> {
                radioGroup.check(R.id.rb_custom)
                weekdaySelector.visibility = View.VISIBLE
                alternateRestSelector.visibility = View.GONE
                tempCustomDays?.forEach { day ->
                    weekdayViews[day % 7].isSelected = true
                }
            }
            ReminderRepeatType.ALTERNATE_REST -> {
                radioGroup.check(R.id.rb_alternate_rest)
                weekdaySelector.visibility = View.GONE
                alternateRestSelector.visibility = View.VISIBLE
                oddWeekView.isSelected = tempIsOddWeek
                evenWeekView.isSelected = !tempIsOddWeek
            }
            else -> {
                radioGroup.check(when (tempRepeatType) {
                    ReminderRepeatType.ONCE -> R.id.rb_once
                    ReminderRepeatType.DAILY -> R.id.rb_daily
                    ReminderRepeatType.DOUBLE_REST -> R.id.rb_double_rest
                    ReminderRepeatType.SINGLE_REST -> R.id.rb_single_rest
                    else -> R.id.rb_once
                })
                weekdaySelector.visibility = View.GONE
                alternateRestSelector.visibility = View.GONE
            }
        }

        // 设置单双休选择点击事件
        oddWeekView.setOnClickListener {
            tempIsOddWeek = true
            oddWeekView.isSelected = true
            evenWeekView.isSelected = false
        }

        evenWeekView.setOnClickListener {
            tempIsOddWeek = false
            oddWeekView.isSelected = false
            evenWeekView.isSelected = true
        }

        // 设置星期选择点击事件
        weekdayViews.forEachIndexed { index, view ->
            view.setOnClickListener {
                view.isSelected = !view.isSelected
                tempCustomDays = weekdayViews.mapIndexedNotNull { i, v ->
                    if (v.isSelected) (i + 1) % 7 else null
                }
            }
        }

        // 设置底部按钮点击事件
        dialogView.findViewById<TextView>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.btn_confirm).setOnClickListener {
            // 确认时才更新实际的值
            currentRepeatType = tempRepeatType
            customDays = tempCustomDays
            isOddWeek = tempIsOddWeek
            updateRepeatTypeText()
            dialog.dismiss()
        }

        // 选择监听
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_custom -> {
                    weekdaySelector.visibility = View.VISIBLE
                    alternateRestSelector.visibility = View.GONE
                    if (tempCustomDays == null) {
                        tempCustomDays = listOf(1)
                        weekdayViews[1].isSelected = true
                    }
                    tempRepeatType = ReminderRepeatType.CUSTOM
                }
                R.id.rb_alternate_rest -> {
                    weekdaySelector.visibility = View.GONE
                    alternateRestSelector.visibility = View.VISIBLE
                    tempRepeatType = ReminderRepeatType.ALTERNATE_REST
                    oddWeekView.isSelected = tempIsOddWeek
                    evenWeekView.isSelected = !tempIsOddWeek
                }
                else -> {
                    weekdaySelector.visibility = View.GONE
                    alternateRestSelector.visibility = View.GONE
                    tempRepeatType = when (checkedId) {
                        R.id.rb_once -> ReminderRepeatType.ONCE
                        R.id.rb_daily -> ReminderRepeatType.DAILY
                        R.id.rb_double_rest -> ReminderRepeatType.DOUBLE_REST
                        R.id.rb_single_rest -> ReminderRepeatType.SINGLE_REST
                        else -> ReminderRepeatType.ONCE
                    }
                }
            }
        }

        dialog.show()
    }

    private fun updateRepeatTypeText() {
        val text = when (currentRepeatType) {
            ReminderRepeatType.ONCE -> "仅一次"
            ReminderRepeatType.DAILY -> "每天"
            ReminderRepeatType.DOUBLE_REST -> "双休制"
            ReminderRepeatType.SINGLE_REST -> "单休制"
            ReminderRepeatType.ALTERNATE_REST -> {
                if (isOddWeek) "单双休(本周为单周)" else "单双休(本周为双周)"
            }
            ReminderRepeatType.CUSTOM -> {
                val days = customDays ?: return
                // 检查是否选择了所有天数
                if (days.size == 7) {
                    "每天"
                } else {
                    val dayNames = days.map { 
                        when (it) {
                            1 -> "周一"
                            2 -> "周二"
                            3 -> "周三"
                            4 -> "周四"
                            5 -> "周五"
                            6 -> "周六"
                            7, 0 -> "周日"  // 处理 0 和 7 都表示周日的情况
                            else -> ""
                        }
                    }
                    "每${dayNames.joinToString("、")}"
                }
            }
        }
        mBinding.tvRepeatValue.text = text
    }

    private fun showNotifyTypeDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_notify_type, null)
        dialog.setContentView(dialogView)

        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.rg_notify_type)
        
        // 设置当前选中状态
        when (currentNotifyType) {
            ReminderNotifyType.NOTIFICATION -> radioGroup.check(R.id.rb_notification)
            ReminderNotifyType.RING -> radioGroup.check(R.id.rb_ring)
            ReminderNotifyType.VIBRATE -> radioGroup.check(R.id.rb_vibrate)
            ReminderNotifyType.RING_VIBRATE -> radioGroup.check(R.id.rb_ring_vibrate)
        }

        // 选择监听
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            currentNotifyType = when (checkedId) {
                R.id.rb_notification -> ReminderNotifyType.NOTIFICATION
                R.id.rb_ring -> ReminderNotifyType.RING
                R.id.rb_vibrate -> ReminderNotifyType.VIBRATE
                R.id.rb_ring_vibrate -> ReminderNotifyType.RING_VIBRATE
                else -> ReminderNotifyType.NOTIFICATION
            }
            updateNotifyTypeText()  // 立即更新显示
        }

        // 确认按钮
        dialogView.findViewById<TextView>(R.id.btn_confirm).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateNotifyTypeText() {
        val text = when (currentNotifyType) {
            ReminderNotifyType.NOTIFICATION -> "通知"
            ReminderNotifyType.RING -> "响铃"
            ReminderNotifyType.VIBRATE -> "振动"
            ReminderNotifyType.RING_VIBRATE -> "响铃和振动"
        }
        mBinding.llNotify.tvNotifyValue.text = text
    }

    private fun showNameDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_reminder_name, null)
        dialog.setContentView(dialogView)

        val etName = dialogView.findViewById<EditText>(R.id.et_name)
        etName.setText(reminderName)
        etName.setSelection(reminderName.length)

        dialogView.findViewById<TextView>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.btn_confirm).setOnClickListener {
            val name = etName.text.toString().trim()
        if (name.isBlank()) {
                Toast.makeText(requireContext(), "请输入提醒名称", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            reminderName = name
            mBinding.tvNameValue.text = name
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadReminderData(reminder: Reminder) {
        // 加载提醒数据到界面
        reminderName = reminder.name
        currentRepeatType = reminder.repeatType
        currentNotifyType = reminder.notifyType
        customDays = reminder.customDays
        isOddWeek = reminder.isOddWeek ?: true

        // 更新UI
        mBinding.apply {
            tvNameValue.text = reminderName
            
            // 设置时间
            val (hours, minutes) = reminder.time.split(":").map { it.toInt() }
            timePicker.hour = hours
            timePicker.minute = minutes
            
            // 更新其他显示
            updateRepeatTypeText()
            updateNotifyTypeText()
        }
    }

    fun saveReminder(): Boolean {
        if (reminderName.isBlank()) {
            Toast.makeText(requireContext(), "请输入提醒名称", Toast.LENGTH_SHORT).show()
            return false
        }

        val hour = mBinding.timePicker.hour
        val minute = mBinding.timePicker.minute
        val time = String.format("%02d:%02d", hour, minute)

        if (editingReminder != null) {
            // 编辑模式：使用现有的 ID 更新提醒
            viewModel.updateReminder(
                editingReminder!!.copy(
                    name = reminderName,
                    time = time,
                    repeatType = currentRepeatType,
                    customDays = if (currentRepeatType == ReminderRepeatType.CUSTOM) customDays else null,
                    isOddWeek = if (currentRepeatType == ReminderRepeatType.ALTERNATE_REST) isOddWeek else null,
                    notifyType = currentNotifyType
                )
            )
        } else {
            // 添加模式：创建新提醒
            viewModel.createReminder(
                name = reminderName,
                time = time,
                repeatType = currentRepeatType,
                customDays = if (currentRepeatType == ReminderRepeatType.CUSTOM) customDays else null,
                isOddWeek = if (currentRepeatType == ReminderRepeatType.ALTERNATE_REST) isOddWeek else null,
                notifyType = currentNotifyType
            )
        }

        // 返回列表页面
        (activity as? ReminderManagerActivity)?.navigateToList()
        return true
    }

    fun loadReminder(reminderId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val reminder = viewModel.getReminder(reminderId)
                if (reminder != null) {
                    editingReminder = reminder
                    loadReminderData(reminder)
                } else {
                    Toast.makeText(requireContext(), "找不到该提醒", Toast.LENGTH_SHORT).show()
                    (activity as? ReminderManagerActivity)?.navigateToList()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "加载提醒失败", Toast.LENGTH_SHORT).show()
                (activity as? ReminderManagerActivity)?.navigateToList()
            }
        }
    }

    companion object {
        fun newInstance(reminderId: Long? = null): ReminderAddFragment {
            return ReminderAddFragment().apply {
                arguments = Bundle().apply {
                    reminderId?.let { putLong("reminder_id", it) }
                }
            }
        }
    }
} 