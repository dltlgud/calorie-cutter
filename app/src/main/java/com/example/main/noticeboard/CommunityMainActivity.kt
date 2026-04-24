package com.example.main.noticeboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.example.main.R
import com.example.main.auth.LoginActivity

class CommunityMainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        FirebaseFirestore.getInstance().firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        setContentView(R.layout.activity_community_main)

        bottomNav = findViewById(R.id.bottom_nav)
        fab = findViewById(R.id.fab_add)
        bottomNav.setItemActiveIndicatorEnabled(false)

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frame, HomeFragment())
            .commit()

        // 홈 탭일 때만 FAB 표시
        fab.visibility = View.VISIBLE

        fab.setOnClickListener {
            val dateStr = HomeFragment.currentSelectedDate.toString()
            val sheet = DayDetailBottomSheet.newInstance(dateStr)
            sheet.onDismissed = {
                val frag = supportFragmentManager.findFragmentById(R.id.main_frame)
                (frag as? HomeFragment)?.loadWeeklyStats()
            }
            sheet.show(supportFragmentManager, "FabDayDetail")
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_home -> {
                    fab.show()
                    HomeFragment()
                }
                R.id.nav_library -> {
                    fab.hide()
                    LibraryFragment()
                }
                R.id.nav_community -> {
                    fab.hide()
                    CommunityFragment()
                }
                R.id.nav_mypage -> {
                    fab.hide()
                    MyPageFragment()
                }
                else -> null
            }

            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_frame, it)
                    .commit()
                true
            } ?: false
        }
    }

    override fun onBackPressed() {
        if (bottomNav.selectedItemId != R.id.nav_home) {
            bottomNav.selectedItemId = R.id.nav_home
        } else {
            super.onBackPressed()
        }
    }
}
