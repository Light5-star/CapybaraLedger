package com.xuhh.capybaraledger.ui.view.billtypeselect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.model.Category

class CategoryAdapter(
    private val categories: List<Category>,
    private val selectedCategory: Category?,
    private val onClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcon: TextView = view.findViewById(R.id.tv_icon)
        val tvName: TextView = view.findViewById(R.id.tv_name)
        val root: View = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bill_type_selector, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]

        // 设置分类数据
        holder.tvIcon.setText(category.iconResId)
        holder.tvName.text = category.name

        // 设置选中状态
        val isSelected = category.id == selectedCategory?.id
        holder.root.background = if (isSelected) {
            ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_dialog_ledger_selector)
        } else {
            null
        }

        holder.itemView.setOnClickListener { onClick(category) }
    }

    override fun getItemCount() = categories.size
}