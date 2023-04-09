package org.techtown.myproject.note

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogMealModel
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef
import java.io.ByteArrayOutputStream
import java.util.*

class PlusMealActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var nowDate : String

    private lateinit var mealImage : ImageView
    private var mealImageFile : String = ""
    private lateinit var defaultImage : Bitmap
    var isImageUpload : Boolean = false

    private lateinit var hourArea : TextView
    private lateinit var minuteArea : TextView
    private lateinit var listener: TimePickerDialog.OnTimeSetListener
    private lateinit var dialog: TimePickerDialog

    lateinit var timeSlot : String

    lateinit var spinner : Spinner
    lateinit var mealType : String

    lateinit var mealNameArea : EditText
    lateinit var mealWeightArea : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plus_meal)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        nowDate = intent.getStringExtra("date").toString() // 선택된 날짜

        findViewById<TextView>(R.id.today).text = nowDate

        mealImage = findViewById(R.id.mealImage)

        timeSlot = "아침"
        val timeGroup = findViewById<RadioGroup>(R.id.time)
        timeGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.breakfast -> timeSlot = "아침"
                R.id.lunch -> timeSlot = "점심"
                R.id.dinner -> timeSlot = "저녁"
            }
        }

        hourArea = findViewById(R.id.hour)
        minuteArea = findViewById(R.id.minute)
        setNowTime()

        val timeSet = findViewById<LinearLayout>(R.id.timeLayout)
        timeSet.setOnClickListener {
            showTime()
        }

        spinner = findViewById(R.id.spinner)
        mealType = spinner.getItemAtPosition(0).toString()
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                mealType = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        mealNameArea = findViewById(R.id.mealNameArea)
        mealWeightArea = findViewById(R.id.mealWeightArea)

        val plusBtn = findViewById<Button>(R.id.plusBtn)
        plusBtn.setOnClickListener {
            if(mealNameArea.text.toString().trim() == "") {
                Toast.makeText(this, "사료 이름을 입력하세요!", Toast.LENGTH_LONG).show()
                mealNameArea.setSelection(0)
            }
            else if(mealWeightArea.text.toString().trim() == "") {
                Toast.makeText(this, "사료 양을 입력하세요!", Toast.LENGTH_LONG).show()
                mealWeightArea.setSelection(0)
            } else {
                plusMealNote(nowDate, timeSlot,hourArea.text.toString().trim() + ":" + minuteArea.text.toString().trim(), mealType, mealNameArea.text.toString().trim(), mealWeightArea.text.toString().trim())
                Toast.makeText(this, "사료 추가 완료!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }

        mealImage.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)

            isImageUpload = true // 이미지를 첨부하였다는 의미
            val imageView : ImageView = mealImage
            defaultImage = (imageView.drawable as BitmapDrawable).bitmap
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

    private fun imageUpload(key : String) { // 이미지를 storage에 업로드하는 함수
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child("dogMealImage/$userId/$dogId/$key.png")

        mealImageFile = "dogMealImage/$userId/$dogId/$key.png"

        mealImage.isDrawingCacheEnabled = true
        mealImage.buildDrawingCache()
        val bitmap = (mealImage.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = mountainsRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK && requestCode == 100) {
            mealImage.setImageURI(data?.data) // mealImage에 갤러리에서 선택한 이미지 넣기
        }
    }

    private fun plusMealNote(date : String, timeSlot : String, time : String, mealType : String, mealName : String, mealWeight : String) {
        val key = FBRef.mealRef.child(userId).child(dogId).push().key.toString() // 키 값을 먼저 받아옴 -> 사료 사진의 이름을 키 값으로 설정하기 위함

        if(isImageUpload) { // 이미지 첨부되었을 시 이미지를 storage로 업로드
            val imageView : ImageView = mealImage
            val changedImage : BitmapDrawable = imageView.drawable as BitmapDrawable
            val bitmap : Bitmap = changedImage.bitmap

            if(bitmap != defaultImage) { // 기본 이미지와 비교 -> 이미지 업로드 누르고 이미지 선택 안 하고 나올 시 검은 화면이 나오는 것을 방지하기 위함
                imageUpload(key)
            }
        }

        FBRef.mealRef.child(userId).child(dogId).child(key).setValue(DogMealModel(key, dogId, date, mealImageFile, timeSlot, time, mealType, mealName, mealWeight)) // 반려견 사료 정보 데이터베이스에 저장
    }
}