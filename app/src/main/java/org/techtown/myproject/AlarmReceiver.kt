package org.techtown.myproject

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Notification 생성 및 등록
        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java) as NotificationManager
        createNotificationChannel(notificationManager)

        val contentTitle = "제목"
        val contentText = "내용"

        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_appicon_round)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel Name",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel Description"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 500, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "channel_id"
        const val notificationId = 0
    }
}