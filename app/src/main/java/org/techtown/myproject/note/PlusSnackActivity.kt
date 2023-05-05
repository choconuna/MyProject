package org.techtown.myproject.note

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogMealModel
import org.techtown.myproject.utils.DogSnackModel
import org.techtown.myproject.utils.FBRef
import java.io.ByteArrayOutputStream
import java.util.*

class PlusSnackActivity : AppCompatActivity() {

    private val TAG = PlusSnackActivity::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var nowDate : String

    private lateinit var snackImage : ImageView
    private var snackImageFile : String = ""
    private lateinit var defaultImage : Bitmap
    var isImageUpload : Boolean = false

    lateinit var timeSlot : String

    lateinit var snackNameSpinner : Spinner

    lateinit var snackSpinner : Spinner
    lateinit var snackType : String
    lateinit var snackNameArea : EditText
    lateinit var snackWeightArea : EditText
    lateinit var snackUnitSpinner : Spinner
    lateinit var snackUnit : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plus_snack)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        nowDate = intent.getStringExtra("date").toString() // 선택된 날짜

        findViewById<TextView>(R.id.today).text = nowDate

        snackNameSpinner = findViewById(R.id.snackNameSpinner)
        showSnackNameSpinner()

        snackNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (selectedItem == "직접 입력") {
                    snackNameArea.setText("")
                    snackNameArea.setSelection(0)
                    snackNameArea.isEnabled = true
                } else {
                    snackNameArea.setText(selectedItem)
                    snackNameArea.isEnabled = false
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        snackImage = findViewById(R.id.snackImage)

        timeSlot = "아침"
        val timeGroup = findViewById<RadioGroup>(R.id.time)
        timeGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.breakfast -> timeSlot = "아침"
                R.id.lunch -> timeSlot = "점심"
                R.id.dinner -> timeSlot = "저녁"
            }
        }

        snackSpinner = findViewById(R.id.snackSpinner)
        snackType = snackSpinner.getItemAtPosition(0).toString()
        snackSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                snackType = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        snackNameArea = findViewById(R.id.snackNameArea)
        snackWeightArea = findViewById(R.id.snackWeightArea)

        snackUnitSpinner = findViewById(R.id.snackUnitSpinner)
        snackUnit = snackUnitSpinner.getItemAtPosition(0).toString()
        snackUnitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                snackUnit = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val plusBtn = findViewById<Button>(R.id.plusBtn)
        plusBtn.setOnClickListener {
            if(snackNameArea.text.toString().trim() == "") {
                Toast.makeText(this, "간식 이름을 입력하세요!", Toast.LENGTH_LONG).show()
                snackNameArea.setSelection(0)
            }
            else if(snackWeightArea.text.toString().trim() == "") {
                Toast.makeText(this, "간식 양을 입력하세요!", Toast.LENGTH_LONG).show()
                snackWeightArea.setSelection(0)
            } else {
                plusSnackNote(nowDate, timeSlot, snackType, snackNameArea.text.toString().trim(), snackWeightArea.text.toString().trim(), snackUnit)
                Toast.makeText(this, "간식 추가 완료!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }

        snackImage.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)

            isImageUpload = true // 이미지를 첨부하였다는 의미
            val imageView : ImageView = snackImage
            defaultImage = (imageView.drawable as BitmapDrawable).bitmap
        }
    }

    private fun showSnackNameSpinner() {
        val snackSet = mutableSetOf<String>()
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    snackSet.clear()
                    snackSet.add("직접 입력")

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogSnackModel::class.java)
                        item?.let { snackSet.add(it.snackName) }
                    }
                    Log.d("snackSet", snackSet.toString())

                    if(snackSet.size == 1)
                        snackNameSpinner.visibility = View.GONE
                    else
                        snackNameSpinner.visibility = View.VISIBLE

                    val adapter = ArrayAdapter<String>(applicationContext, R.layout.custom_spinner, snackSet.toList())
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    snackNameSpinner.adapter = adapter

                } catch (e: Exception) {
                    Log.d(TAG, "사료 기록 삭제 완료")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.snackRef.child(userId).child(dogId).addValueEventListener(postListener)
    }

    private fun imageUpload(key : String) { // 이미지를 storage에 업로드하는 함수
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child("dogSnackImage/$userId/$dogId/$key.png")

        snackImageFile = "dogSnackImage/$userId/$dogId/$key.png"

        snackImage.isDrawingCacheEnabled = true
        snackImage.buildDrawingCache()
        val bitmap = (snackImage.drawable as BitmapDrawable).bitmap
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
            snackImage.setImageURI(data?.data) // mealImage에 갤러리에서 선택한 이미지 넣기
        }
    }

    private fun plusSnackNote(date : String, timeSlot : String, snackType : String, snackName : String, snackWeight : String, snackUnit : String) {
        val key = FBRef.snackRef.child(userId).child(dogId).push().key.toString() // 키 값을 먼저 받아옴 -> 간식 사진의 이름을 키 값으로 설정하기 위함

        if(isImageUpload) { // 이미지 첨부되었을 시 이미지를 storage로 업로드
            val imageView : ImageView = snackImage
            val changedImage : BitmapDrawable = imageView.drawable as BitmapDrawable
            val bitmap : Bitmap = changedImage.bitmap

            if(bitmap != defaultImage) { // 기본 이미지와 비교 -> 이미지 업로드 누르고 이미지 선택 안 하고 나올 시 검은 화면이 나오는 것을 방지하기 위함
                imageUpload(key)
            }
        }

        FBRef.snackRef.child(userId).child(dogId).child(key).setValue(DogSnackModel(key, dogId, date, snackImageFile, timeSlot, snackType, snackName, snackWeight, snackUnit)) // 반려견 간식 정보 데이터베이스에 저장
    }
}