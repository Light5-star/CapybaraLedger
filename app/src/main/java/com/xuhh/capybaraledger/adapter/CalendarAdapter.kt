package com.xuhh.capybaraledger.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.ui.fragment.detail.CalendarDay

class CalendarAdapter : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {
    private var calendarDays = listOf<CalendarDay>()

    fun submitList(days: List<CalendarDay>) {
        calendarDays = days
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(calendarDays[position])
    }

    override fun getItemCount() = calendarDays.size

    class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        private val tvExpense: TextView = itemView.findViewById(R.id.tvExpense)
        private val tvIncome: TextView = itemView.findViewById(R.id.tvIncome)
        private val tvBalance: TextView = itemView.findViewById(R.id.tvBalance)

        fun bind(day: CalendarDay) {
            if (day.day > 0) {
                tvDay.text = day.day.toString()
                tvDay.visibility = View.VISIBLE

                if (day.expense > 0) {
                    tvExpense.text = "%.0f".format(day.expense)
                    tvExpense.visibility = View.VISIBLE
                } else {
                    tvExpense.visibility = View.INVISIBLE
                }

                if (day.income > 0) {
                    tvIncome.text = "%.0f".format(day.income)
                    tvIncome.visibility = View.VISIBLE
                } else {
                    tvIncome.visibility = View.INVISIBLE
                }

                if (day.balance != 0.0) {
                    tvBalance.text = "%.0f".format(day.balance)
                    tvBalance.visibility = View.VISIBLE
                } else {
                    tvBalance.visibility = View.INVISIBLE
                }
            } else {
                tvDay.visibility = View.INVISIBLE
                tvExpense.visibility = View.INVISIBLE
                tvIncome.visibility = View.INVISIBLE
                tvBalance.visibility = View.INVISIBLE
            }
        }
    }
}