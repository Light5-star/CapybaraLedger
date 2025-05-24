package com.xuhh.capybaraledger.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.dao.BillWithCategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DateSectionAdapter(
    private val onBillClick: (BillWithCategory) -> Unit
) : ListAdapter<DateSection, DateSectionAdapter.ViewHolder>(DateSectionDiffCallback) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tv_date)
        val tvBalance: TextView = view.findViewById(R.id.tv_balance)
        val rvBills: RecyclerView = view.findViewById(R.id.rv_bills)
        private val billAdapter = BillAdapter(onBillClick)

        init {
            rvBills.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = billAdapter
            }
        }

        fun bind(dateSection: DateSection) {
            // 设置日期
            tvDate.text = formatDate(dateSection.date)

            // 设置结余
            tvBalance.text = String.format(
                if (dateSection.balance >= 0) "+%.2f" else "%.2f",
                dateSection.balance
            )
            tvBalance.setTextColor(
                if (dateSection.balance >= 0) {
                    itemView.context.getColor(android.R.color.holo_green_dark)
                } else {
                    itemView.context.getColor(android.R.color.holo_red_dark)
                }
            )

            // 设置账单列表
            billAdapter.submitList(dateSection.bills.sortedByDescending { it.bill.time })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_date_section, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun formatDate(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }

        return when {
            calendar.get(Calendar.YEAR) != today.get(Calendar.YEAR) -> {
                // 跨年显示年月日
                SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(date)
            }
            calendar.get(Calendar.MONTH) != today.get(Calendar.MONTH) -> {
                // 跨月显示月日
                SimpleDateFormat("MM月dd日", Locale.getDefault()).format(date)
            }
            calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) -> {
                "今日"
            }
            calendar.get(Calendar.DAY_OF_MONTH) == yesterday.get(Calendar.DAY_OF_MONTH) -> {
                "昨日"
            }
            else -> {
                // 同月显示日
                SimpleDateFormat("dd日", Locale.getDefault()).format(date)
            }
        }
    }

    object DateSectionDiffCallback : DiffUtil.ItemCallback<DateSection>() {
        override fun areItemsTheSame(oldItem: DateSection, newItem: DateSection): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: DateSection, newItem: DateSection): Boolean {
            return oldItem == newItem
        }
    }
}

data class DateSection(
    val date: Date,
    val bills: List<BillWithCategory>,
    val balance: Double
) 