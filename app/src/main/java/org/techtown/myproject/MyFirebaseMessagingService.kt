package org.techtown.myproject

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService: FirebaseMessagingService() {

    private val TAG = "FirebaseService"

    // 토큰 생성
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d(TAG, "Refreshed token: $token")

        var uid = FirebaseAuth.getInstance().currentUser?.uid.toString()

        // 토큰 값 따로 저장
        val pref = this.getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(uid + "Token", token).apply()
        editor.commit()

        Log.i("로그", "토큰 저장 성공적")
    }

    // 메시지 수신
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // 채널 ID
        val channelId = "my_channel_01"
        // 채널 이름
        val channelName = "My Channel"
        // 채널에 대한 설명
        val channelDescription = "My Channel Description"
        // 중요도 설정
        val importance = NotificationManager.IMPORTANCE_HIGH

        // 알림 채널 생성
        val channel = NotificationChannel(channelId, channelName, importance)
        channel.description = channelDescription

        // 알림 매니저에 채널 등록
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        if (remoteMessage.notification != null) {
            val body = remoteMessage.notification!!.body
            Log.d(TAG, "Notification Body: $body")
            // 알림 생성
            val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.stat_notify_chat)
                .setColor(Color.parseColor("#c08457")) // 알림 아이콘 색
                .setContentTitle(remoteMessage.notification?.title) // 알림 제목
                .setContentText(remoteMessage.notification?.body) // 알림 내용
                .setAutoCancel(true) // 알림을 클릭하면 자동으로 삭제

            // 알림 표시
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(0, notificationBuilder.build())
        }
    }
}