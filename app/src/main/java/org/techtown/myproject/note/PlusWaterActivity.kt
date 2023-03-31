package org.techtown.myproject.note

import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogTonicModel
import org.techtown.myproject.utils.DogWaterModel
import org.techtown.myproject.utils.FBRef
import org.w3c.dom.Text

class PlusWaterActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var nowDate : String

    lateinit var minWeight : TextView
    lateinit var maxWeight : TextView

    lateinit var timeSlot : String

    lateinit var waterWeightArea : EditText

    var totalWaterWeight : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plus_water)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        nowDate = intent.getStringExtra("date").toString() // 선택된 날짜

        findViewById<TextView>(R.id.today).text = nowDate

        minWeight = findViewById(R.id.minWeight)
        maxWeight = findViewById(R.id.maxWeight)
        getProperWaterWeight()

        waterWeightArea = findViewById(R.id.waterWeightArea)

        timeSlot = "아침"
        val timeGroup = findViewById<RadioGroup>(R.id.time)
        timeGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.breakfast -> timeSlot = "아침"
                R.id.lunch -> timeSlot = "점심"
                R.id.dinner -> timeSlot = "저녁"
            }
        }

        val plusBtn = findViewById<Button>(R.id.plusBtn)
        plusBtn.setOnClickListener {
            if(waterWeightArea.text.toString() == "") {
                Toast.makeText(this, "물 양을 입력하세요!", Toast.LENGTH_LONG).show()
                waterWeightArea.setSelection(0)
            } else {
                plusWaterNote(nowDate, timeSlot, waterWeightArea.text.toString())
                Toast.makeText(this, "물 추가 완료!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun getProperWaterWeight() { // 권장 음수량 구하기
        val dogWeight = FBRef.dogRef.child(userId).child(dogId).child("dogWeight").get().addOnSuccessListener {
            val w = it.value.toString().toInt()
            minWeight.text = (w*50).toString()
            maxWeight.text = (w*60).toString()
        }
    }

    private fun plusWaterNote(date : String, timeSlot : String, waterWeight : String) {
        val key = FBRef.waterRef.child(userId).child(dogId).push().key.toString() // 키 값을 먼저 받아옴

        FBRef.waterRef.child(userId).child(dogId).child(key).setValue(DogWaterModel(key, dogId, date, timeSlot, waterWeight)) // 반려견 물 정보 데이터베이스에 저장
    }
}