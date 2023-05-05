package org.techtown.myproject

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.random.Random.Default.nextInt

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 알림의 내용을 설정합니다.
        val medicineName = intent.getStringExtra("medicineName")

        // Notification 객체를 생성합니다.
        val notificationBuilder = NotificationCompat.Builder(context, "MedicinePlan")
            .setSmallIcon(R.drawable.ic_notification_small_icon_medicine)
            .setColor(Color.parseColor("#c08457"))
            .setContentTitle("약 복용 알림")
            .setContentText(medicineName + "을(를) 복용해주세요.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // NotificationManagerCompat을 이용하여 Notification을 보냅니다.
        val notificationId = System.currentTimeMillis().toInt()
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, notificationBuilder.build())
        }
    }
}
