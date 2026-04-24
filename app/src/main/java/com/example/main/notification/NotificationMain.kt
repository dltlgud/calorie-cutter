package com.example.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.main.notification.ReminderWorker
import java.util.concurrent.TimeUnit

class NotificationMain : AppCompatActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // 레이아웃 이름 그대로 쓰셔도 되고 필요시 변경

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        scheduleInactivityReminder()
    }

    private fun scheduleInactivityReminder() {
        val work = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(48, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this)
            .enqueueUniqueWork(
                "inactivity_reminder",
                ExistingWorkPolicy.REPLACE,
                work
            )
    }
}
