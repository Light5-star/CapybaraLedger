package com.xuhh.capybaraledger.ui.view.ledgerselect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.databinding.ItemLedgerSelectorBinding

class LedgerSelectorAdapter(
    private val onLedgerSelected: (Ledger) -> Unit
) : ListAdapter<Ledger, LedgerSelectorAdapter.ViewHolder>(LedgerDiffCallback()) {

    private var currentLedgerId: Long = 1L  // 添加当前选中的账本ID

    fun setCurrentLedger(ledgerId: Long) {
        currentLedgerId = ledgerId
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemLedgerSelectorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ledger: Ledger) {
            binding.tvName.text = ledger.name
            // 根据是否是当前账本显示"已选择"标记
            binding.tvSelected.visibility = if (ledger.id == currentLedgerId) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                onLedgerSelected(ledger)
                currentLedgerId = ledger.id
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLedgerSelectorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ledger = getItem(position)
        holder.bind(ledger)
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