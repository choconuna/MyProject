package org.techtown.myproject.note

import android.app.Activity
import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogMedicineModel
import org.techtown.myproject.utils.FBRef
import java.util.*

class PlusMedicineActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var nowDate : String

    private lateinit var medicineNameArea : EditText

    private lateinit var yearArea : EditText
    private lateinit var monthArea : EditText
    private lateinit var dayArea : EditText

    private lateinit var hourArea : TextView
    private lateinit var minuteArea : TextView
    private lateinit var listener: TimePickerDialog.OnTimeSetListener
    private lateinit var dialog: TimePickerDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plus_medicine)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id

        medicineNameArea = findViewById(R.id.medicineNameArea)

        yearArea = findViewById(R.id.yearArea)
        monthArea = findViewById(R.id.monthArea)
        dayArea = findViewById(R.id.dayArea)

        hourArea = findViewById(R.id.hour)
        minuteArea = findViewById(R.id.minute)
        setNowTime()

        val timeSet = findViewById<LinearLayout>(R.id.timeLayout)
        timeSet.setOnClickListener {
            showTime()
        }

        val plusBtn = findViewById<Button>(R.id.plusBtn)
        plusBtn.setOnClickListener {

            val month = monthArea.text.toString().toInt()
            val day = dayArea.text.toString().toInt()

            if(medicineNameArea.text.toString() == "") {
                Toast.makeText(this, "투약한 약의 이름을 입력하세요!", Toast.LENGTH_LONG).show()
                medicineNameArea.setSelection(0)
            }
            else if(yearArea.text.toString() == "" || monthArea.text.toString() == "" || dayArea.text.toString() == "" || month < 1 || month > 12 || day < 1 || day > 31) {
                if(((month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) && day > 31) || ((month == 2 || month == 4 || month == 6 || month == 9 || month == 11) && day > 30)) {
                    Toast.makeText(this, "날짜를 정확하게 입력하세요!", Toast.LENGTH_LONG).show()
                    yearArea.setSelection(0)
                }  else {
                    Toast.makeText(this, "날짜를 정확하게 입력하세요!", Toast.LENGTH_LONG).show()
                    yearArea.setSelection(0)
                }
            }  else {
                val date = yearArea.text.toString() + "." + monthArea.text.toString() + "." + dayArea.text.toString()

                plusMedicineNote(date, hourArea.text.toString() + ":" + minuteArea.text.toString(), medicineNameArea.text.toString())
                Toast.makeText(this, "투약 기록 추가 완료!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
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
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                if (hourOfDay < 10)
                    hourArea.text = "0$hourOfDay"
                else
                    hourArea.text = hourOfDay.toString()
                if (minute < 10)
                    minuteArea.text = "0$minute"
                else
                    minuteArea.text = minute.toString()
            }

        dialog = TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, listener, hourArea.text.toString().toInt(), minuteArea.text.toString().toInt(), true)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun plusMedicineNote(date : String, time : String, medicineName : String) {
        val key = FBRef.medicineRef.child(userId).child(dogId).push().key.toString() // 키 값을 먼저 받아옴

        FBRef.medicineRef.child(userId).child(dogId).child(key).setValue(DogMedicineModel(key, dogId, date, time, medicineName)) // 반려견 투약 기록 정보 데이터베이스에 저장
    }
}