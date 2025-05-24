package com.xuhh.capybaraledger.ui.activity.ledger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.databinding.ItemLedgerManageBinding

class LedgerManageAdapter(
    private val onSetDefault: (Ledger) -> Unit,
    private val onDelete: (Ledger) -> Unit
) : ListAdapter<Ledger, LedgerManageAdapter.ViewHolder>(LedgerDiffCallback()) {

    inner class ViewHolder(
        private val binding: ItemLedgerManageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(ledger: Ledger) {
            binding.tvName.text = ledger.name
            
            // 如果是默认账本，显示"默认"标签，隐藏"设为默认"按钮，禁用删除按钮
            if (ledger.isDefault) {
                binding.tvDefaultTag.visibility = View.VISIBLE
                binding.btnSetDefault.visibility = View.GONE
                binding.btnDelete.visibility = View.GONE  // 隐藏删除按钮
            } else {
                binding.tvDefaultTag.visibility = View.GONE
                binding.btnSetDefault.visibility = View.VISIBLE
                binding.btnDelete.visibility = View.VISIBLE  // 显示删除按钮
            }
            
            binding.btnSetDefault.setOnClickListener {
                onSetDefault(ledger)
            }
            
            binding.btnDelete.setOnClickListener {
                onDelete(ledger)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemLedgerManageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class LedgerDiffCallback : DiffUtil.ItemCallback<Ledger>() {
        override fun areItemsTheSame(oldItem: Ledger, newItem: Ledger): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Ledger, newItem: Ledger): Boolean {
            return oldItem == newItem
        }
    }
} 