package org.techtown.myproject

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MyNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // 알림을 받았을 때 처리하는 코드
        val medicineName = intent?.getStringExtra("medicineName")
        showNotification(context, medicineName)
    }

    private fun showNotification(context: Context?, medicineName: String?) {
        // 알림을 보여주는 코드
        // NotificationCompat.Builder를 사용하여 알림 생성
        val builder = NotificationCompat.Builder(context!!, "default")
            .setSmallIcon(R.drawable.ic_notification_small_icon_medicine)
            .setColor(Color.parseColor("#c08457")) // 알림 아이콘 색
            .setContentTitle("투약 알림")
            .setContentText("$medicineName 를 투약할 시간입니다.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // NotificationManagerCompat을 사용하여 알림 표시
        with(NotificationManagerCompat.from(context)) {
            notify(0, builder.build())
        }
    }
}
