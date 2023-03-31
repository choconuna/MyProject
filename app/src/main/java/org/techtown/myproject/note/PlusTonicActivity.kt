package org.techtown.myproject.note

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogTonicModel
import org.techtown.myproject.utils.FBRef
import java.io.ByteArrayOutputStream

class PlusTonicActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var nowDate : String

    private lateinit var tonicImage : ImageView
    private var tonicImageFile : String = ""
    private lateinit var defaultImage : Bitmap
    var isImageUpload : Boolean = false

    lateinit var timeSlot : String

    lateinit var tonicPartSpinner : Spinner
    lateinit var tonicPart : String
    lateinit var tonicNameArea : EditText
    lateinit var tonicWeightArea : EditText
    lateinit var tonicUnitSpinner : Spinner
    lateinit var tonicUnit : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plus_tonic)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        nowDate = intent.getStringExtra("date").toString() // 선택된 날짜

        findViewById<TextView>(R.id.today).text = nowDate

        tonicImage = findViewById(R.id.tonicImage)

        timeSlot = "아침"
        val timeGroup = findViewById<RadioGroup>(R.id.time)
        timeGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.breakfast -> timeSlot = "아침"
                R.id.lunch -> timeSlot = "점심"
                R.id.dinner -> timeSlot = "저녁"
            }
        }

        tonicPartSpinner = findViewById(R.id.partSpinner)
        tonicPart = tonicPartSpinner.getItemAtPosition(0).toString()
        tonicPartSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                tonicPart = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        tonicNameArea = findViewById(R.id.tonicNameArea)
        tonicWeightArea = findViewById(R.id.tonicWeightArea)

        tonicUnitSpinner = findViewById(R.id.tonicUnitSpinner)
        tonicUnit = tonicUnitSpinner.getItemAtPosition(0).toString()
        tonicUnitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                tonicUnit = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val plusBtn = findViewById<Button>(R.id.plusBtn)
        plusBtn.setOnClickListener {
            if(tonicNameArea.text.toString() == "") {
                Toast.makeText(this, "영양제 이름을 입력하세요!", Toast.LENGTH_LONG).show()
                tonicNameArea.setSelection(0)
            }
            else if(tonicWeightArea.text.toString() == "") {
                Toast.makeText(this, "영양제 양을 입력하세요!", Toast.LENGTH_LONG).show()
                tonicWeightArea.setSelection(0)
            } else {
                plusTonicNote(nowDate, timeSlot, tonicPart, tonicNameArea.text.toString(), tonicWeightArea.text.toString(), tonicUnit)
                Toast.makeText(this, "영양제 추가 완료!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }

        tonicImage.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)

            isImageUpload = true // 이미지를 첨부하였다는 의미
            val imageView : ImageView = tonicImage
            defaultImage = (imageView.drawable as BitmapDrawable).bitmap
        }
    }

    private fun imageUpload(key : String) { // 이미지를 storage에 업로드하는 함수
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child("dogTonicImage/$userId/$dogId/$key.png")

        tonicImageFile = "dogTonicImage/$userId/$dogId/$key.png"

        tonicImage.isDrawingCacheEnabled = true
        tonicImage.buildDrawingCache()
        val bitmap = (tonicImage.drawable as BitmapDrawable).bitmap
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
            tonicImage.setImageURI(data?.data) // mealImage에 갤러리에서 선택한 이미지 넣기
        }
    }

    private fun plusTonicNote(date : String, timeSlot : String, tonicPart : String, tonicName : String, tonicWeight : String, tonicUnit : String) {
        val key = FBRef.tonicRef.child(userId).child(dogId).push().key.toString() // 키 값을 먼저 받아옴 -> 영양제 사진의 이름을 키 값으로 설정하기 위함

        if(isImageUpload) { // 이미지 첨부되었을 시 이미지를 storage로 업로드
            val imageView : ImageView = tonicImage
            val changedImage : BitmapDrawable = imageView.drawable as BitmapDrawable
            val bitmap : Bitmap = changedImage.bitmap

            if(bitmap != defaultImage) { // 기본 이미지와 비교 -> 이미지 업로드 누르고 이미지 선택 안 하고 나올 시 검은 화면이 나오는 것을 방지하기 위함
                imageUpload(key)
            }
        }

        FBRef.tonicRef.child(userId).child(dogId).child(key).setValue(DogTonicModel(key, dogId, date, tonicImageFile, timeSlot, tonicPart, tonicName, tonicWeight, tonicUnit)) // 반려견 영양제 정보 데이터베이스에 저장
    }
}