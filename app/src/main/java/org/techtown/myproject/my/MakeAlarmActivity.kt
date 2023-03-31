package org.techtown.myproject.my

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.techtown.myproject.R
import java.util.*


class MakeAlarmActivity : AppCompatActivity() {

    private val TAG = MakeAlarmActivity::class.java.simpleName
    private lateinit var hourArea : TextView
    private lateinit var minuteArea : TextView
    private lateinit var alarmManager : AlarmManager
    private lateinit var calendar: Calendar
    private lateinit var mainIntent : Intent
    private lateinit var pIntent : PendingIntent
    private lateinit var mContext : Context
    lateinit var category : String
    private lateinit var listener: OnTimeSetListener
    private lateinit var dialog: TimePickerDialog
    private lateinit var sun : CheckBox
    private lateinit var mon : CheckBox
    private lateinit var tue : CheckBox
    private lateinit var wed : CheckBox
    private lateinit var thu : CheckBox
    private lateinit var fri : CheckBox
    private lateinit var sat : CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_alarm)

        hourArea = findViewById(R.id.hour)
        minuteArea = findViewById(R.id.minute)
        setNowTime()

        val timeSet = findViewById<LinearLayout>(R.id.timeLayout)
        timeSet.setOnClickListener {
            showTime()
        }

        category = "식사"
        val categoryGroup = findViewById<RadioGroup>(R.id.category)
        categoryGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.eat -> category = "식사"
                R.id.medicine -> category = "복용"
                R.id.hospital -> category = "병원"
                R.id.walk -> category = "산책"
                R.id.etc -> category = "기타"
            }
            Toast.makeText(this, category, Toast.LENGTH_SHORT).show()
        }

        getDay()

        val saveBtn = findViewById<Button>(R.id.saveBtn)
        saveBtn.setOnClickListener {
            setAlarm()
            Toast.makeText(this, "알람 설정 성공", Toast.LENGTH_SHORT).show()
        }

        val backBtn = findViewById<ImageView>(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun getDay() {
        sun = findViewById(R.id.sun)
        mon = findViewById(R.id.mon)
        tue = findViewById(R.id.tue)
        wed = findViewById(R.id.wed)
        thu = findViewById(R.id.thu)
        fri = findViewById(R.id.fri)
        sat = findViewById(R.id.sat)
    }

    private fun setNowTime() {
        val cal = Calendar.getInstance()
        val mHour = cal.get(Calendar.HOUR_OF_DAY)
        val mMin = cal.get(Calendar.MINUTE)

        if(mHour < 10)
            hourArea.text = "0$mHour"
        else
            hourArea.text = mHour.toString()

        if(mMin < 10)
            minuteArea.text = "0$mMin"
        else
            minuteArea.text = mMin.toString()
    }

    private fun showTime() {
        listener =
            OnTimeSetListener { _, hourOfDay, minute ->
                if(hourOfDay < 10)
                    hourArea.text = "0$hourOfDay"
                else
                    hourArea.text = hourOfDay.toString()
                if(minute < 10)
                    minuteArea.text = "0$minute"
                else
                    minuteArea.text = minute.toString()
            }

        dialog = TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, listener, hourArea.text.toString().toInt(), minuteArea.text.toString().toInt(), true)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun setAlarm() {
        mContext = this
        alarmManager = mContext?.getSystemService(ALARM_SERVICE) as AlarmManager
        calendar = Calendar.getInstance()
        mainIntent = Intent(mContext, AlarmReceiver::class.java)

        val week = arrayOf(sun.isSelected, mon.isSelected, tue.isSelected, wed.isSelected, thu.isSelected, fri.isSelected, sat.isSelected)

        val hour = hourArea.text.toString().toInt()
        val minute = minuteArea.text.toString().toInt()

        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        var selectTime = calendar.timeInMillis
        val currentTime = System.currentTimeMillis()

        if(currentTime > selectTime) {
            selectTime += 24 * 60 * 60 * 1000
        }

        Log.d(TAG,"설정한 시간 : "+ calendar.time);

        mainIntent.putExtra("weekday", week)

        pIntent = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.getBroadcast(mContext, 0, mainIntent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        else
            PendingIntent.getBroadcast(mContext, 0, mainIntent, PendingIntent.FLAG_MUTABLE)
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, selectTime, AlarmManager.INTERVAL_DAY, pIntent)
    }
}
