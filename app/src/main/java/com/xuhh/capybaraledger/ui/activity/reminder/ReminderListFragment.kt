package com.xuhh.capybaraledger.ui.activity.reminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.widget.TextView
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.databinding.FragmentReminderListBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.viewmodel.ReminderViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.xuhh.capybaraledger.utils.NotificationHelper
import com.xuhh.capybaraledger.data.model.ReminderNotifyType
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.xuhh.capybaraledger.data.model.Reminder

class ReminderListFragment : BaseFragment<FragmentReminderListBinding>() {
    private val viewModel: ReminderViewModel by activityViewModels { 
        ReminderViewModel.Factory(
            (requireActivity().application as App).reminderRepository,
            requireContext()
        )
    }
    
    private val reminderAdapter = ReminderListAdapter()

    override fun initBinding(): FragmentReminderListBinding {
        return FragmentReminderListBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeReminders()
    }

    private fun setupRecyclerView() {
        mBinding.recyclerView.apply {
            adapter = reminderAdapter
            layoutManager = LinearLayoutManager(context)
            
            // 添加左滑删除
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val reminder = reminderAdapter.currentList[position]
                    
                    // 显示确认对话框
                    showDeleteConfirmDialog(reminder, position)
                }
            }).attachToRecyclerView(this)
        }

        reminderAdapter.onSwitchChanged = { reminder, isEnabled ->
            viewModel.updateReminderEnabled(reminder.id, isEnabled)
        }

        reminderAdapter.onItemClick = { reminder ->
            (activity as? ReminderManagerActivity)?.navigateToEdit(reminder.id)
        }
    }

    private fun observeReminders() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reminders.collectLatest { reminders ->
                reminderAdapter.submitList(reminders)
                mBinding.tvEmpty.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun showDeleteConfirmDialog(reminder: Reminder, position: Int) {
        val dialog = BottomSheetDialog(requireContext(), R.style.TransparentBottomSheetStyle)
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_confirm, null)
        dialog.setContentView(dialogView)

        // 设置背景透明
        dialog.window?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.apply {
            setBackgroundResource(android.R.color.transparent)
        }

        // 设置标题和内容
        dialogView.findViewById<TextView>(R.id.tv_title).text = "删除提醒"
        dialogView.findViewById<TextView>(R.id.tv_content).text = "确定要删除 ${reminder.name} 吗？"

        // 取消按钮
        dialogView.findViewById<TextView>(R.id.btn_cancel).setOnClickListener {
            // 恢复item位置
            reminderAdapter.notifyItemChanged(position)
            dialog.dismiss()
        }

        // 确认按钮
        dialogView.findViewById<TextView>(R.id.btn_confirm).setOnClickListener {
            viewModel.deleteReminder(reminder)
            dialog.dismiss()
            // 不需要手动移除item，因为数据变化会自动更新列表
        }

        // 点击外部取消时的处理
        dialog.setOnCancelListener {
            // 恢复item位置
            reminderAdapter.notifyItemChanged(position)
        }

        dialog.show()
    }
} 