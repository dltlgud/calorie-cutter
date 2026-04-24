package com.example.main.noticeboard

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class CommunityPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> ScheduleBoardFragment()
        1 -> GeneralBoardFragment()
        else -> FollowFeedFragment()
    }
}

