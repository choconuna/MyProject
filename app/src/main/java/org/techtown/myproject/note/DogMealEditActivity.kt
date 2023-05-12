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
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogMealModel
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef
import java.io.ByteArrayOutputStream

class DogMealEditActivity : AppCompatActivity() {

    private val TAG = DogMealEditActivity::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var dogMealId : String
    lateinit var nowDate : String

    private lateinit var mealImage : ImageView
    private var mealImageFile : String = ""
    private lateinit var defaultImage : Bitmap
    var isImageUpload : Boolean = false

    lateinit var time : String
    private lateinit var hourArea : TextView
    private lateinit var minuteArea : TextView
    private lateinit var listener: TimePickerDialog.OnTimeSetListener
    private lateinit var dialog: TimePickerDialog

    lateinit var timeSlot : String
    lateinit var timeSlotGroup : RadioGroup

    lateinit var spinner : Spinner
    lateinit var mealType : String

    lateinit var mealNameSpinner : Spinner

    lateinit var mealNameArea : EditText
    lateinit var mealName : String
    lateinit var mealWeightArea : EditText
    lateinit var mealWeight : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_meal_edit)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        dogMealId = intent.getStringExtra("id").toString() // dogMeal id
        nowDate = intent.getStringExtra("date").toString() // 선택된 날짜

        mealNameSpinner = findViewById(R.id.mealNameSpinner)
        showMealNameSpinner()

        FBRef.mealRef.child(userId).child(dogId).child(dogMealId).child("mealName").get().addOnSuccessListener {
            for (i in 0 until mealNameSpinner.count) {
                if (mealNameSpinner.getItemAtPosition(i).toString() == it.value.toString()) {
                    mealNameSpinner.setSelection(i)
                    break
                }
            }
        }

        mealNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (selectedItem == "직접 입력") {
                    mealNameArea.setText("")
                    mealNameArea.setSelection(0)
                    mealNameArea.isEnabled = true
                } else {
                    mealNameArea.setText(selectedItem)
                    mealNameArea.isEnabled = false
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        mealImage = findViewById(R.id.mealImage)
        timeSlotGroup = findViewById(R.id.time)
        hourArea = findViewById(R.id.hour)
        minuteArea = findViewById(R.id.minute)
        spinner = findViewById(R.id.spinner)
        mealNameArea = findViewById(R.id.mealNameArea)
        mealWeightArea = findViewById(R.id.mealWeightArea)

        getData()

        mealImage.setOnClickListener {
            showDialog()
        }

        val timeSet = findViewById<LinearLayout>(R.id.timeLayout)
        timeSet.setOnClickListener {
            showTime()
        }

        timeSlotGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.breakfast -> timeSlot = "아침"
                R.id.lunch -> timeSlot = "점심"
                R.id.dinner -> timeSlot = "저녁"
            }
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                mealType = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val editBtn = findViewById<Button>(R.id.editBtn)
        editBtn.setOnClickListener {

            mealName = mealNameArea.text.toString().trim()
            mealWeight = mealWeightArea.text.toString().trim()

            when {
                mealName == "" -> {
                    Toast.makeText(this, "사료 이름을 입력하세요!", Toast.LENGTH_SHORT).show()
                    mealNameArea.setSelection(0)
                }
                mealWeight == "" -> {
                    Toast.makeText(this, "사료의 양을 입력하세요!", Toast.LENGTH_SHORT).show()
                    mealWeightArea.setSelection(0)
                }
                else -> {
                    saveDogMeal(timeSlot, hourArea.text.toString() + ":" + minuteArea.text.toString(), mealType, mealName, mealWeight)
                    Toast.makeText(this, "사료 정보가 수정되었습니다!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun showMealNameSpinner() {
        val mealSet = mutableSetOf<String>()
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    mealSet.clear()
                    mealSet.add("직접 입력")

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogMealModel::class.java)
                        item?.let { mealSet.add(it.mealName) }
                    }
                    Log.d("mealSet", mealSet.toString())

                    if(mealSet.size == 1)
                        mealNameSpinner.visibility = View.GONE
                    else
                        mealNameSpinner.visibility = View.VISIBLE

                    val adapter = ArrayAdapter<String>(applicationContext, R.layout.custom_spinner, mealSet.toList())
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    mealNameSpinner.adapter = adapter

                } catch (e: Exception) {
                    Log.d(TAG, "사료 기록 삭제 완료")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.mealRef.child(userId).child(dogId).addValueEventListener(postListener)
    }

    private fun getData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val post = dataSnapshot.getValue(DogMealModel::class.java)
                timeSlot = post!!.timeSlot

                time = post!!.time
                var timeToken = time.split(':')
                hourArea.text = timeToken[0]
                minuteArea.text = timeToken[1]

                mealNameArea.setText(post!!.mealName)
                mealWeightArea.setText(post!!.mealWeight)
                mealType = post!!.mealType

                mealImageFile = post!!.mealImageFile // 가져올 사료 사진

                val profileFile =
                    FBRef.mealRef.child(userId).child(dogId).child(dogMealId).child("mealImageFile").get().addOnSuccessListener {
                        if(it.value != "") {
                            val storageReference =
                                Firebase.storage.reference.child(it.value.toString()) // 사료 사진을 DB의 storage로부터 가져옴

                            storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Glide.with(applicationContext).load(task.result)
                                        .into(mealImage) // 사료 사진을 표시함
                                } else {
                                    findViewById<ImageView>(R.id.mealImage).isVisible = false
                                }
                            })
                        }
                    }

                for(i in 0 until spinner.count) {
                    if(spinner.getItemAtPosition(i).toString() == mealType)  {
                        spinner.setSelection(i)
                        break
                    }
                }

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
        FBRef.mealRef.child(userId).child(dogId).child(dogMealId).addValueEventListener(postListener)
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
        Log.d("IMAGEUPLOAD", "imageUpload() is called")
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child("dogMealImage/$userId/$dogId/$key.png")
        if (mealImageFile == "dogMealImage/$userId/$dogId/$key.png") { // 사료 이미지가 기본 이미지가 아니었다면, storage에 저장되어있던 이미지 삭제
            storageRef.child("dogMealImage/$userId/$dogId/$key.png").delete()
        }

        mealImageFile = "dogMealImage/$userId/$dogId/$key.png" // 새로운 사료의 사진을 해당 이름으로 설정

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

    private fun showDialog() { // 사료 이미지 설정을 위한 다이얼로그 띄우기
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.eat_image_dialog, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)

        val alertDialog = mBuilder.show()
        val galleryBtn = alertDialog.findViewById<Button>(R.id.galleryBtn)
        galleryBtn?.setOnClickListener { // 갤러리에서 가져오기 버튼 클릭 시
            Log.d(TAG, "gallery Button Clicked")

            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)

            isImageUpload = true

            val imageView : ImageView = mealImage
            defaultImage = (imageView.drawable as BitmapDrawable).bitmap

            alertDialog.dismiss()
        }

        val deleteBtn = alertDialog.findViewById<Button>(R.id.deleteBtn)
        deleteBtn?.setOnClickListener {  // 이미지 삭제 버튼 클릭 시
            Log.d(TAG, "basic Button Clicked")

            isImageUpload = false

            val storage = Firebase.storage
            val storageRef = storage.reference
            val mountainsRef = storageRef.child("dogMealImage/$userId/$dogId/$dogMealId.png")
            if(mealImageFile == "dogMealImage/$userId/$dogId/$dogMealId.png") {    // 사료 이미지가 존재했다면, storage에 저장되어있던 이미지 삭제
                storageRef.child("dogMealImage/$userId/$dogId/$dogMealId.png").delete()
            }

            mealImageFile = ""
            mealImage.setImageResource(R.drawable.image_plus)

            alertDialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK && requestCode == 100) {
            Log.d("IMAGEURL", "Image URL: " + data?.data.toString())
            mealImage.setImageURI(data?.data) // 사료 이미지에 갤러리에서 선택한 이미지 넣기
            imageUpload(dogMealId)
        }
    }

    private fun saveDogMeal(timeSlot : String, time : String, mealType : String, mealName : String, mealWeight : String) {
        FBRef.mealRef.child(userId).child(dogId).child(dogMealId).setValue(DogMealModel(dogMealId, dogId, nowDate, mealImageFile, timeSlot, time, mealType, mealName, mealWeight)) // 반려견 사료 정보 데이터베이스에 저장
    }
}