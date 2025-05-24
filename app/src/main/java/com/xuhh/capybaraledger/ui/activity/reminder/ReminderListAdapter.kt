package com.xuhh.capybaraledger.ui.activity.reminder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xuhh.capybaraledger.data.model.Reminder
import com.xuhh.capybaraledger.data.model.ReminderRepeatType
import com.xuhh.capybaraledger.databinding.ItemReminderBinding

class ReminderListAdapter : ListAdapter<Reminder, ReminderListAdapter.ViewHolder>(
    ReminderDiffCallback()
) {

    var onSwitchChanged: ((Reminder, Boolean) -> Unit)? = null
    var onItemClick: ((Reminder) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReminderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemReminderBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(getItem(position))
                }
            }
        }

        fun bind(reminder: Reminder) {
            binding.tvTime.text = reminder.time
            binding.tvRepeat.text = when (reminder.repeatType) {
                ReminderRepeatType.ONCE -> "仅一次"
                ReminderRepeatType.DAILY -> "每天"
                ReminderRepeatType.DOUBLE_REST -> "双休制"
                ReminderRepeatType.SINGLE_REST -> "单休制"
                ReminderRepeatType.ALTERNATE_REST -> {
                    if (reminder.isOddWeek == true) "单双休(本周为单周)" else "单双休(本周为双周)"
                }
                ReminderRepeatType.CUSTOM -> {
                    val days = reminder.customDays
                    if (days?.size == 7) {
                        "每天"
                    } else {
                        val dayNames = days?.map { 
                            when (it) {
                                1 -> "周一"
                                2 -> "周二"
                                3 -> "周三"
                                4 -> "周四"
                                5 -> "周五"
                                6 -> "周六"
                                7, 0 -> "周日"
                                else -> ""
                            }
                        }
                        "每${dayNames?.joinToString("、")}"
                    }
                }
            }
            binding.switchEnable.isChecked = reminder.isEnabled
            binding.switchEnable.setOnCheckedChangeListener { _, isChecked ->
                onSwitchChanged?.invoke(reminder, isChecked)
            }
        }
    }

    private class ReminderDiffCallback : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem == newItem
        }
    }
} 