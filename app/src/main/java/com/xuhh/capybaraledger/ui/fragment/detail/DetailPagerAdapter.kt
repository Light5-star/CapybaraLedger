package com.xuhh.capybaraledger.ui.fragment.detail

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class DetailPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FlowModeFragment()
            1 -> CalendarModeFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }

    fun getFragment(position: Int): Fragment? {
        return when (position) {
            0 -> FlowModeFragment()
            1 -> CalendarModeFragment()
            else -> null
        }
    }
} 