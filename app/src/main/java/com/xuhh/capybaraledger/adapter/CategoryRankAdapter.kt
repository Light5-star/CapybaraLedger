package com.xuhh.capybaraledger.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.ui.view.unicode.UnicodeTextView

data class CategoryRankItem(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val iconResId: String
)

class CategoryRankAdapter : ListAdapter<CategoryRankItem, CategoryRankAdapter.ViewHolder>(CategoryRankDiffCallback) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRank: TextView = view.findViewById(R.id.tv_rank)
        val tvIcon: UnicodeTextView = view.findViewById(R.id.tv_icon)
        val tvCategory: TextView = view.findViewById(R.id.tv_category)
        val tvAmount: TextView = view.findViewById(R.id.tv_amount)
        val tvPercentage: TextView = view.findViewById(R.id.tv_percentage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_rank, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.tvRank.text = (position + 1).toString()
        holder.tvIcon.setText(item.iconResId)
        holder.tvCategory.text = item.category
        holder.tvAmount.text = String.format("%.2f", item.amount)
        holder.tvPercentage.text = String.format("%.1f%%", item.percentage * 100)
    }

    object CategoryRankDiffCallback : DiffUtil.ItemCallback<CategoryRankItem>() {
        override fun areItemsTheSame(oldItem: CategoryRankItem, newItem: CategoryRankItem): Boolean {
            return oldItem.category == newItem.category
        }

        override fun areContentsTheSame(oldItem: CategoryRankItem, newItem: CategoryRankItem): Boolean {
            return oldItem == newItem
        }
    }
} 