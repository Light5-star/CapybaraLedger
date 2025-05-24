package com.xuhh.capybaraledger.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.dao.BillWithCategory
import com.xuhh.capybaraledger.ui.view.unicode.UnicodeTextView

class BillAdapter(
    private val onBillClick: (BillWithCategory) -> Unit
) : ListAdapter<BillWithCategory, BillAdapter.ViewHolder>(BillDiffCallback) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcon: UnicodeTextView = view.findViewById(R.id.tv_icon)
        val tvCategory: TextView = view.findViewById(R.id.tv_category)
        val tvNote: TextView = view.findViewById(R.id.tv_note)
        val tvAmount: TextView = view.findViewById(R.id.tv_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bill_home, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val billWithCategory = getItem(position)

        // 设置分类图标
        holder.tvIcon.setText(billWithCategory.category.iconResId)
        holder.tvCategory.text = billWithCategory.category.name

        // 设置分类名称和备注
        if (billWithCategory.bill.note.isNullOrEmpty()) {
            holder.tvNote.visibility = View.GONE
        } else {
            holder.tvNote.apply {
                visibility = View.VISIBLE
                text = billWithCategory.bill.note
            }
        }

        // 设置金额
        holder.tvAmount.text = String.format(
            if (billWithCategory.bill.type == 0) "-%.2f" else "+%.2f",
            billWithCategory.bill.amount
        )

        // 设置点击事件
        holder.itemView.setOnClickListener { onBillClick(billWithCategory) }

        // 根据收支类型设置不同颜色
        holder.tvAmount.setTextColor(
            if (billWithCategory.bill.type == 0) {
                holder.itemView.context.getColor(android.R.color.holo_red_dark)
            } else {
                holder.itemView.context.getColor(android.R.color.holo_green_dark)
            }
        )
    }

    object BillDiffCallback : DiffUtil.ItemCallback<BillWithCategory>() {
        override fun areItemsTheSame(
            oldItem: BillWithCategory,
            newItem: BillWithCategory
        ): Boolean {
            return oldItem.bill.id == newItem.bill.id
        }

        override fun areContentsTheSame(
            oldItem: BillWithCategory,
            newItem: BillWithCategory
        ): Boolean {
            return oldItem == newItem
        }

    }

    fun submitSortedList(list: List<BillWithCategory>) {
        // 过滤掉可能存在的无效账单数据
        val filteredList = list.filter { billWithCategory -> 
            val isValid = billWithCategory.bill.id > 0 && 
                         billWithCategory.category.id > 0 &&
                         billWithCategory.bill.amount > 0 &&
                         billWithCategory.bill.date > 0 &&
                         billWithCategory.bill.time > 0
            
            if (!isValid) {
                Log.w("BillAdapter", "过滤掉无效账单数据 - ID: ${billWithCategory.bill.id}, 金额: ${billWithCategory.bill.amount}, 分类ID: ${billWithCategory.category.id}")
            }
            isValid
        }
        
        // 按时间倒序排序，优先按日期排序，日期相同则按具体时间排序
        val sortedList = filteredList.sortedWith(
            compareByDescending<BillWithCategory> { it.bill.date }
            .thenByDescending { it.bill.time }
            .thenByDescending { it.bill.id }  // 添加ID作为最后的排序依据，确保相同时间的账单顺序稳定
        )
        
        Log.d("BillAdapter", "提交账单列表 - 原始数量: ${list.size}, 过滤后数量: ${filteredList.size}, 排序后数量: ${sortedList.size}")
        
        // 使用submitList而不是直接设置列表，以触发DiffUtil的更新
        super.submitList(sortedList.toList())  // 创建新的列表实例，确保触发更新
    }
}
