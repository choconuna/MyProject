package org.techtown.myproject.note

import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogSnackModel
import org.techtown.myproject.utils.DogWaterModel
import org.techtown.myproject.utils.FBRef

class DogWaterEditActivity : AppCompatActivity() {

    private val TAG = DogWaterEditActivity::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var nowDate : String
    lateinit var dogWaterId :String

    lateinit var minWeight : TextView
    lateinit var maxWeight : TextView

    lateinit var timeSlotGroup : RadioGroup
    lateinit var timeSlot : String

    lateinit var waterWeightArea : EditText
    lateinit var waterWeight : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_water_edit)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        nowDate = intent.getStringExtra("date").toString() // 선택된 날짜
        dogWaterId = intent.getStringExtra("id").toString() // dogSnack id

        timeSlotGroup = findViewById(R.id.time)
        minWeight = findViewById(R.id.minWeight)
        maxWeight = findViewById(R.id.maxWeight)
        waterWeightArea = findViewById(R.id.waterWeightArea)

        getData()

        timeSlotGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.breakfast -> timeSlot = "아침"
                R.id.lunch -> timeSlot = "점심"
                R.id.dinner -> timeSlot = "저녁"
            }
        }

        val editBtn = findViewById<Button>(R.id.editBtn)
        editBtn.setOnClickListener {

            waterWeight = waterWeightArea.text.toString().trim()

            when {
                waterWeight == "" -> {
                    Toast.makeText(this, "음수량을 입력하세요!", Toast.LENGTH_SHORT).show()
                    waterWeightArea.setSelection(0)
                }
                else -> {
                    saveDogWater(timeSlot, waterWeight)
                    Toast.makeText(this, "음수량 정보가 수정되었습니다!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun getData() {
        val dogWeight = FBRef.dogRef.child(userId).child(dogId).child("dogWeight").get().addOnSuccessListener {
            val w = it.value.toString().toInt()
            minWeight.text = (w*50).toString()
            maxWeight.text = (w*60).toString()
        }

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val post = dataSnapshot.getValue(DogWaterModel::class.java)
                timeSlot = post!!.timeSlot

                waterWeightArea.setText(post!!.waterWeight)

                when (timeSlot) {
                    "아침" -> timeSlotGroup.check(findViewById<RadioButton>(R.id.breakfast).id)
                    "점심" -> timeSlotGroup.check(findViewById<RadioButton>(R.id.lunch).id)
                    "저녁" -> timeSlotGroup.check(findViewById<RadioButton>(R.id.dinner).id)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.waterRef.child(userId).child(dogId).child(dogWaterId).addValueEventListener(postListener)
    }

    private fun saveDogWater(timeSlot : String, waterWeight : String) {
        FBRef.waterRef.child(userId).child(dogId).child(dogWaterId).setValue(DogWaterModel(dogWaterId, dogId, nowDate, timeSlot, waterWeight))  // 반려견 물 정보 데이터베이스에 저장
    }
}