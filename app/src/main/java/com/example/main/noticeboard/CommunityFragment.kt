package com.example.main.noticeboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.main.R

class CommunityFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var writeButton: FloatingActionButton

    private val writePostLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            when (tabLayout.selectedTabPosition) {
                0 -> (childFragmentManager.findFragmentByTag("f0") as? ScheduleBoardFragment)?.loadPosts()
                1 -> (childFragmentManager.findFragmentByTag("f1") as? GeneralBoardFragment)?.loadPosts()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_community, container, false)

        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)
        writeButton = view.findViewById(R.id.writeButton)

        val adapter = CommunityPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "일정 공유"
                1 -> "자유 게시판"
                else -> "팔로우"
            }
        }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                writeButton.visibility = if (position == 2) View.GONE else View.VISIBLE
            }
        })

        writeButton.setOnClickListener {
            val position = tabLayout.selectedTabPosition
            if (position == 2) return@setOnClickListener
            val category = if (position == 0) "schedule" else "free"
            val intent = Intent(requireContext(), WritePostActivity::class.java)
            intent.putExtra("category", category)
            writePostLauncher.launch(intent)
        }

        return view
    }
}
