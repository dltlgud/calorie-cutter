// 파일 경로: app/src/main/java/com/example/main/notification/ReminderWorker.kt
package com.example.main.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.main.R
import com.example.main.auth.LoginActivity

class ReminderWorker(
    private val ctx: Context,
    params: WorkerParameters
) : Worker(ctx, params) {

    companion object {
        private const val CHANNEL_ID = "reminder_channel"
        private const val CHANNEL_NAME = "운동 리마인더"
        private const val NOTIFICATION_ID = 1001
    }

    override fun doWork(): Result {
        createNotificationChannel()
        sendNotification()
        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "48시간 동안 앱 미사용 시 보내는 운동 리마인더"
            }
            val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
            mgr.createNotificationChannel(channel)
        }
    }

    private fun sendNotification() {
        // 1) resources/values/quotes.xml 에 정의된 string-array 불러오기
        val messages = ctx.resources.getStringArray(R.array.quotes)
        val randomMessage = messages.random()

        // 2) 로그인 화면으로 이동할 PendingIntent
        val intent = Intent(ctx, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            ctx,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE else 0
        )

        // 3) Notification 생성
        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)      // 알맞은 아이콘 리소스 준비!
            .setContentTitle("운동을 잊지 마셨나요?")
            .setContentText(randomMessage)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // 4) 시스템에 알림 요청 (Android 13+ 권한 체크)
        val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mgr.notify(NOTIFICATION_ID, notification)
        }
        // 권한이 없으면 안전하게 무시합니다
    }
}
