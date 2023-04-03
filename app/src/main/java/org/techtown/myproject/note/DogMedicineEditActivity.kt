package org.techtown.myproject.note

import android.app.Activity
import android.app.TimePickerDialog
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogMedicineModel
import org.techtown.myproject.utils.DogMedicinePlanModel
import org.techtown.myproject.utils.FBRef

class DogMedicineEditActivity : AppCompatActivity() {

    private val TAG = DogMedicineEditActivity::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var dogMedicineId : String

    private lateinit var yearArea : EditText
    private lateinit var monthArea : EditText
    private lateinit var dayArea : EditText

    lateinit var time : String
    private lateinit var hourArea : TextView
    private lateinit var minuteArea : TextView
    private lateinit var listener: TimePickerDialog.OnTimeSetListener
    private lateinit var dialog: TimePickerDialog

    lateinit var medicineNameArea : EditText
    lateinit var medicineName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_medicine_edit)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        dogMedicineId = intent.getStringExtra("id").toString() // dogMedicine id

        yearArea = findViewById(R.id.yearArea)
        monthArea = findViewById(R.id.monthArea)
        dayArea = findViewById(R.id.dayArea)

        hourArea = findViewById(R.id.hour)
        minuteArea = findViewById(R.id.minute)
        medicineNameArea = findViewById(R.id.medicineNameArea)

        getData()

        val timeSet = findViewById<LinearLayout>(R.id.timeLayout)
        timeSet.setOnClickListener {
            showTime()
        }

        val editBtn = findViewById<Button>(R.id.editBtn)
        editBtn.setOnClickListener {

            medicineName = medicineNameArea.text.toString().trim()

            val month = monthArea.text.toString().toInt()
            val day = dayArea.text.toString().toInt()

            if(medicineName == "") {
                Toast.makeText(this, "투약할 약의 이름을 입력하세요!", Toast.LENGTH_LONG).show()
                medicineNameArea.setSelection(0)
            }
            if(medicineNameArea.text.toString() == "") {
                Toast.makeText(this, "투약한 약의 이름을 입력하세요!", Toast.LENGTH_LONG).show()
                medicineNameArea.setSelection(0)
            }
            else if(yearArea.text.toString() == "" || monthArea.text.toString() == "" || dayArea.text.toString() == "" || month < 1 || month > 12 || day < 1 || day > 31) {
                if(((month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) && day > 31) || ((month == 2 || month == 4 || month == 6 || month == 9 || month == 11) && day > 30)) {
                    Toast.makeText(this, "날짜를 정확하게 입력하세요!", Toast.LENGTH_LONG).show()
                    yearArea.setSelection(0)
                } else {
                    Toast.makeText(this, "날짜를 정확하게 입력하세요!", Toast.LENGTH_LONG).show()
                    yearArea.setSelection(0)
                }
            }  else {
                val date = yearArea.text.toString() + "." + monthArea.text.toString() + "." + dayArea.text.toString()

                editMedicineNote(date, hourArea.text.toString() + ":" + minuteArea.text.toString(), medicineNameArea.text.toString())
                Toast.makeText(this, "투약 기록 수정 완료!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun getData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val post = dataSnapshot.getValue(DogMedicineModel::class.java)

                    medicineNameArea.setText(post!!.medicineName)

                    val date = post!!.date
                    val dateSplit = date.split(".")
                    yearArea.setText(dateSplit[0])
                    monthArea.setText(dateSplit[1])
                    dayArea.setText(dateSplit[2])

                    time = post!!.time
                    var timeToken = time.split(':')
                    hourArea.text = timeToken[0]
                    minuteArea.text = timeToken[1]
                } catch (e: Exception) {
                    Log.d(TAG, " 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.medicineRef.child(userId).child(dogId).child(dogMedicineId).addValueEventListener(postListener)
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

    private fun editMedicineNote(date : String, time : String, medicineName : String) {
        FBRef.medicineRef.child(userId).child(dogId).child(dogMedicineId).setValue(DogMedicineModel(dogMedicineId, dogId, date, time, medicineName)) // 반려견 투약 기록 정보 데이터베이스에 저장
    }
}