package com.xuhh.capybaraledger.ui.fragment.statistics

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class StatisticsPagerAdapter(
    private val fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> StatisticsTrendFragment()
            1 -> StatisticsRankFragment()
            2 -> StatisticsAnalysisFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }

    fun getCurrentFragment(position: Int): Fragment? {
        return fragmentManager.findFragmentByTag("f$position")
    }
} 