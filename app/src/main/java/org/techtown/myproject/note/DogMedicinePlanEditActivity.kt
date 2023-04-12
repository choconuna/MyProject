package org.techtown.myproject.note

import android.app.Activity
import android.app.TimePickerDialog
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogMedicinePlanModel
import org.techtown.myproject.utils.FBRef

class DogMedicinePlanEditActivity : AppCompatActivity() {

    private val TAG = DogMealEditActivity::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var dogMedicinePlanId : String

    private lateinit var startDateName : TextView
    private lateinit var startYearArea : EditText
    private lateinit var startMonthArea : EditText
    private lateinit var startDayArea : EditText

    private lateinit var endDateArea : LinearLayout
    private lateinit var endYearArea : EditText
    private lateinit var endMonthArea : EditText
    private lateinit var endDayArea : EditText

    lateinit var time : String
    private lateinit var hourArea : TextView
    private lateinit var minuteArea : TextView
    private lateinit var listener: TimePickerDialog.OnTimeSetListener
    private lateinit var dialog: TimePickerDialog

    lateinit var repeat : String
    lateinit var repeatGroup : RadioGroup

    lateinit var medicineNameArea : EditText
    lateinit var medicineName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_medicine_plan_edit)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        dogMedicinePlanId = intent.getStringExtra("id").toString() // dogMedicinePlan id

        startDateName = findViewById(R.id.startDateName)
        startYearArea = findViewById(R.id.startYearArea)
        startMonthArea = findViewById(R.id.startMonthArea)
        startDayArea = findViewById(R.id.startDayArea)

        endDateArea = findViewById(R.id.endDateArea)
        endYearArea = findViewById(R.id.endYearArea)
        endMonthArea = findViewById(R.id.endMonthArea)
        endDayArea = findViewById(R.id.endDayArea)

        repeatGroup = findViewById(R.id.repeatGroup)
        hourArea = findViewById(R.id.hour)
        minuteArea = findViewById(R.id.minute)
        medicineNameArea = findViewById(R.id.medicineNameArea)

        getData()

        val timeSet = findViewById<LinearLayout>(R.id.timeLayout)
        timeSet.setOnClickListener {
            showTime()
        }

        repeatGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.oneDay ->  {
                    repeat = "하루"
                    startDateName.text = "날짜"
                    endDateArea.visibility = GONE
                }
                R.id.everyDay -> {
                    repeat = "매일"
                    startDateName.text = "시작 날짜"
                    endDateArea.visibility = VISIBLE
                }
            }
        }

        val editBtn = findViewById<Button>(R.id.editBtn)
        editBtn.setOnClickListener {

            medicineName = medicineNameArea.text.toString().trim()

            val sm = startMonthArea.text.toString().toInt()
            val sd = startDayArea.text.toString().toInt()

            if(medicineName == "") {
                Toast.makeText(this, "투약할 약의 이름을 입력하세요!", Toast.LENGTH_LONG).show()
                medicineNameArea.setSelection(0)
            }
            else if(startYearArea.text.toString() == "" || startMonthArea.text.toString() == "" || startDayArea.text.toString() == "" || sm < 1 || sm > 12 || sd < 1 || sd > 31) {
                if(((sm == 1 || sm == 3 || sm == 5 || sm == 7 || sm == 8 || sm == 10 || sm == 12) && sd > 31) || ((sm == 2 || sm == 4 || sm == 6 || sm == 9 || sm == 11) && sd > 30)) {
                    if (repeat == "하루")
                        Toast.makeText(this, "날짜를 정확하게 입력하세요!", Toast.LENGTH_LONG).show()
                    else if (repeat == "매일")
                        Toast.makeText(this, "시작 날짜를 정확하게 입력하세요!", Toast.LENGTH_LONG).show()
                    startYearArea.setSelection(0)
                }
                else {
                    if (repeat == "하루")
                        Toast.makeText(this, "날짜를 정확하게 입력하세요!", Toast.LENGTH_LONG).show()
                    else if (repeat == "매일")
                        Toast.makeText(this, "시작 날짜를 정확하게 입력하세요!", Toast.LENGTH_LONG).show()
                    startYearArea.setSelection(0)
                }
            } else if(repeat == "매일" && (endYearArea.text.toString() == "" || endMonthArea.text.toString() == "" || endDayArea.text.toString() == "" || endMonthArea.text.toString().toInt() < 1 || endMonthArea.text.toString().toInt() > 12 || endDayArea.text.toString().toInt() < 1 || endDayArea.text.toString().toInt() > 31)) {
                val em = endMonthArea.text.toString().toInt()
                val ed = endDayArea.text.toString().toInt()
                if(((em == 1 || em == 3 || em == 5 || em == 7 || em == 8 || em == 10 || em == 12) && ed > 31) || ((em == 2 || em == 4 || em == 6 || em == 9 || em == 11) && ed > 30)) {
                    Toast.makeText(this, "종료 날짜를 정확하게 입력하세요!", Toast.LENGTH_LONG).show()
                    endYearArea.setSelection(0)
                } else {
                    Toast.makeText(this, "종료 날짜를 정확하게 입력하세요!", Toast.LENGTH_LONG).show()
                    endYearArea.setSelection(0)
                }
            }  else {
                val startMonth = startMonthArea.text.toString().trim()
                val startDay = startDayArea.text.toString().trim()
                var startDate = ""

                if(startMonth.length == 1 && startDay.length == 1)
                    startDate = startYearArea.text.toString().trim() + "." + "0$startMonth" + "." + "0$startDay"
                else if(startMonth.length == 1 && startDay.length == 2)
                    startDate = startYearArea.text.toString().trim() + "." + "0$startMonth" + "." + startDay
                else if(startMonth.length == 2 && startDay.length == 1)
                    startDate = startYearArea.text.toString().trim() + "." + startMonth + "." + "0$startDay"
                else if(startMonth.length == 2 && startDay.length == 2)
                    startDate = startYearArea.text.toString().trim() + "." + startMonth + "." + startDay

                var endDate = ""
                if(repeat == "하루") {
                    if (startMonth.length == 1 && startDay.length == 1)
                        endDate = startYearArea.text.toString().trim() + "." + "0$startMonth" + "." + "0$startDay"
                    else if (startMonth.length == 1 && startDay.length == 2)
                        endDate = startYearArea.text.toString().trim() + "." + "0$startMonth" + "." + startDay
                    else if (startMonth.length == 2 && startDay.length == 1)
                        endDate = startYearArea.text.toString().trim() + "." + startMonth + "." + "0$startDay"
                    else if (startMonth.length == 2 && startDay.length == 2)
                        endDate = startYearArea.text.toString().trim() + "." + startMonth + "." + startDay
                }

                else if(repeat == "매일") {
                    val endMonth = endMonthArea.text.toString().trim()
                    val endDay = endDayArea.text.toString().trim()

                    if (endMonth.length == 1 && endDay.length == 1)
                        endDate = endYearArea.text.toString().trim() + "." + "0$endMonth" + "." + "0$endDay"
                    else if (endMonth.length == 1 && endDay.length == 2)
                        endDate = endYearArea.text.toString().trim() + "." + "0$endMonth" + "." + endDay
                    else if (endMonth.length == 2 && endDay.length == 1)
                        endDate = endYearArea.text.toString().trim() + "." + endMonth + "." + "0$endDay"
                    else if (endMonth.length == 2 && endDay.length == 2)
                        endDate = endYearArea.text.toString().trim() + "." + endMonth + "." + endDay
                }

                editMedicinePlanNote(startDate, endDate, hourArea.text.toString() + ":" + minuteArea.text.toString(), repeat, medicineNameArea.text.toString().trim())
                Toast.makeText(this, "투약 일정 수정 완료!", Toast.LENGTH_SHORT).show()
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
                val post = dataSnapshot.getValue(DogMedicinePlanModel::class.java)
                repeat = post!!.repeat

                medicineNameArea.setText(post!!.medicineName)

                when (repeat) {
                    "하루" ->  {
                        repeatGroup.check(findViewById<RadioButton>(R.id.oneDay).id)
                        startDateName.text = "날짜"
                        endDateArea.visibility = View.GONE
                    }
                    "매일" -> {
                        repeatGroup.check(findViewById<RadioButton>(R.id.everyDay).id)
                        startDateName.text = "시작 날짜"
                        endDateArea.visibility = View.VISIBLE

                        val endDate = post!!.endDate
                        val endDateSplit = endDate.split(".")
                        endYearArea.setText(endDateSplit[0])
                        endMonthArea.setText(endDateSplit[1])
                        endDayArea.setText(endDateSplit[2])
                    }
                }

                val startDate = post!!.startDate
                val startDateSplit = startDate.split(".")
                startYearArea.setText(startDateSplit[0])
                startMonthArea.setText(startDateSplit[1])
                startDayArea.setText(startDateSplit[2])

                time = post!!.time
                var timeToken = time.split(':')
                hourArea.text = timeToken[0]
                minuteArea.text = timeToken[1]
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.medicinePlanRef.child(userId).child(dogId).child(dogMedicinePlanId).addValueEventListener(postListener)
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

    private fun editMedicinePlanNote(startDate : String, endDate : String, time : String, repeat : String, medicineName : String) {
        FBRef.medicinePlanRef.child(userId).child(dogId).child(dogMedicinePlanId).setValue(DogMedicinePlanModel(dogMedicinePlanId, dogId, startDate, endDate, time, repeat, medicineName)) // 반려견 투약 일정 정보 데이터베이스에 저장
    }
}