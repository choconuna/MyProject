package org.techtown.myproject.note

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogHeartModel
import org.techtown.myproject.utils.DogPeeModel
import org.techtown.myproject.utils.FBRef
import org.w3c.dom.Text
import java.util.concurrent.TimeUnit


class PlusHeartActivity : AppCompatActivity() {

    private val TAG = PlusHeartActivity::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var nowDate : String

    private var timeCountInMilliSeconds = 30000

    private lateinit var progressBarCircle : ProgressBar
    private lateinit var time : TextView
    private lateinit var countDownTimer : CountDownTimer

    private var firstClicked = true
    private lateinit var heartCnt : TextView
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plus_heart)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        nowDate = intent.getStringExtra("date").toString() // 선택된 날짜

        progressBarCircle = findViewById(R.id.progressBarCircle)
        time = findViewById(R.id.textViewTime)
        heartCnt = findViewById(R.id.heartCnt)

        start()

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun start() {
        setProgressBarValues()
        firstClicked = true
        time.text = "00:00:30"
        count = 0
        heartCnt.text = count.toString()

        progressBarCircle.setOnClickListener {
            if(firstClicked) {
                startCountDownTimer()
                firstClicked = false
            }
            if(time.text.toString() != "00:00:00") {
                count += 1
            }
            heartCnt.text = count.toString()
        }

    }

    private fun showDialog() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.heart_dialog, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)

        val alertDialog = mBuilder.show()

        alertDialog.findViewById<TextView>(R.id.heartCnt)!!.text = (count * 2).toString()

        val saveBtn = alertDialog.findViewById<Button>(R.id.saveBtn)
        saveBtn?.setOnClickListener { // 아니오 버튼 클릭 시
            Log.d(TAG, "save Button Clicked")

            val key = FBRef.heartRef.child(userId).child(dogId).push().key.toString() // 키 값을 먼저 받아옴

            FBRef.heartRef.child(userId).child(dogId).child(key).setValue(DogHeartModel(key, dogId, nowDate, (count * 2).toString())) // 반려견 호흡수 정보 데이터베이스에 저장

            alertDialog.dismiss()

            finish()
        }

        val returnBtn = alertDialog.findViewById<Button>(R.id.returnBtn)
        returnBtn?.setOnClickListener {  // 예 버튼 클릭 시
            Log.d(TAG, "return Button Clicked")

            alertDialog.dismiss()

            start()
        }
    }

    // 카운트다운 시작 기능
    private fun startCountDownTimer() {
        countDownTimer = object : CountDownTimer(timeCountInMilliSeconds.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                time.text = hmsTimeFormatter(millisUntilFinished)
                progressBarCircle.progress = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                showDialog()
            }
        }.start()
        countDownTimer.start()
    }


    // 원형 프로그레스 바에 값 세팅
    private fun setProgressBarValues() {
        progressBarCircle.max = timeCountInMilliSeconds / 1000
        progressBarCircle.progress = timeCountInMilliSeconds / 1000
    }


     // 밀리언 초를 시간으로 포멧해주는 기능
     // @param milliSeconds
     // @return HH:mm:ss 시간 포멧
    @SuppressLint("DefaultLocale")
    private fun hmsTimeFormatter(milliSeconds: Long): String? {
        return java.lang.String.format(
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(milliSeconds),
            TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(
                    milliSeconds
                )
            ),
            TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(
                    milliSeconds
                )
            )
        )
    }
}