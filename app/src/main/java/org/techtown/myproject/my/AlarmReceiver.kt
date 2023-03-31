package org.techtown.myproject.my

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import org.techtown.myproject.R
import java.util.*


class AlarmReceiver : BroadcastReceiver() {

    private val TAG = AlarmReceiver::class.java.simpleName

    private lateinit var context : Context

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        this.context = context!!

        val bIntent = Intent(context, MakeAlarmActivity::class.java)

        val week = intent?.getBooleanArrayExtra("weekday")

        val cal = Calendar.getInstance()
        Log.d(TAG, "요일: " + cal.get(Calendar.DAY_OF_WEEK))

        if(!week?.get(cal.get(Calendar.DAY_OF_WEEK))!!)
            return

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntentWithParentStack(bIntent)
        val bPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        val notificationBuilder = NotificationCompat.Builder(context, "alarm").setSmallIcon(R.mipmap.ic_launcher_appicon_round).setDefaults(
            Notification.DEFAULT_ALL).setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setAutoCancel(true)
            .setContentTitle("알람")
            .setContentText("울림")
            .setContentIntent(bPendingIntent)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        /* val serviceIntent = Intent(context, AlarmService::class.java)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            this.context.startForegroundService(serviceIntent)
        else
            this.context.startService(serviceIntent) */
    }
}