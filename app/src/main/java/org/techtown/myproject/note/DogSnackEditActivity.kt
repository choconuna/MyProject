package org.techtown.myproject.note

import android.app.Activity
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
import org.techtown.myproject.utils.DogSnackModel
import org.techtown.myproject.utils.FBRef
import java.io.ByteArrayOutputStream

class DogSnackEditActivity : AppCompatActivity() {

    private val TAG = DogSnackEditActivity::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var nowDate : String
    lateinit var dogSnackId : String

    private lateinit var snackImage : ImageView
    private var snackImageFile : String = ""
    private lateinit var defaultImage : Bitmap
    var isImageUpload : Boolean = false

    lateinit var timeSlotGroup : RadioGroup
    lateinit var timeSlot : String

    lateinit var snackSpinner : Spinner
    lateinit var snackType : String
    lateinit var snackNameArea : EditText
    lateinit var snackName : String
    lateinit var snackWeightArea : EditText
    lateinit var snackWeight : String
    lateinit var snackUnitSpinner : Spinner
    lateinit var snackUnit : String

    lateinit var snackNameSpinner : Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_snack_edit)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        dogSnackId = intent.getStringExtra("id").toString() // dogSnack id
        nowDate = intent.getStringExtra("date").toString() // 선택된 날짜

        snackNameSpinner = findViewById(R.id.snackNameSpinner)
        showSnackNameSpinner()

        FBRef.snackRef.child(userId).child(dogId).child(dogSnackId).child("snackName").get().addOnSuccessListener {
            for (i in 0 until snackNameSpinner.count) {
                if (snackNameSpinner.getItemAtPosition(i).toString() == it.value.toString()) {
                    snackNameSpinner.setSelection(i)
                    break
                }
            }
        }

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
        timeSlotGroup = findViewById(R.id.time)
        snackSpinner = findViewById(R.id.snackSpinner)
        snackNameArea = findViewById(R.id.snackNameArea)
        snackWeightArea = findViewById(R.id.snackWeightArea)
        snackUnitSpinner = findViewById(R.id.snackUnitSpinner)

        getData()

        snackImage.setOnClickListener {
            showDialog()
        }

        timeSlotGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.breakfast -> timeSlot = "아침"
                R.id.lunch -> timeSlot = "점심"
                R.id.dinner -> timeSlot = "저녁"
            }
        }

        snackSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                snackType = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        snackUnitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                snackUnit = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val editBtn = findViewById<Button>(R.id.editBtn)
        editBtn.setOnClickListener {

            snackName = snackNameArea.text.toString().trim()
            snackWeight = snackWeightArea.text.toString().trim()

            when {
                snackName == "" -> {
                    Toast.makeText(this, "간식 이름을 입력하세요!", Toast.LENGTH_SHORT).show()
                    snackNameArea.setSelection(0)
                }
                snackWeight == "" -> {
                    Toast.makeText(this, "간식의 양을 입력하세요!", Toast.LENGTH_SHORT).show()
                    snackWeightArea.setSelection(0)
                }
                else -> {
                    saveDogSnack(timeSlot, snackType, snackName, snackWeight, snackUnit)
                    Toast.makeText(this, "간식 정보가 수정되었습니다!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
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
                    Log.d(TAG, "간식 기록 삭제 완료")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.snackRef.child(userId).child(dogId).addValueEventListener(postListener)
    }

    private fun getData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val post = dataSnapshot.getValue(DogSnackModel::class.java)
                timeSlot = post!!.timeSlot

                snackNameArea.setText(post!!.snackName)
                snackWeightArea.setText(post!!.snackWeight)
                snackType = post!!.snackType
                snackUnit = post!!.snackUnit

                snackImageFile = post!!.snackImageFile // 가져올 간식 사진

                val profileFile =
                    FBRef.snackRef.child(userId).child(dogId).child(dogSnackId)
                        .child("snackImageFile").get().addOnSuccessListener {
                            if(it.value != "") {
                                val storageReference =
                                    Firebase.storage.reference.child(it.value.toString()) // 사료 사진을 DB의 storage로부터 가져옴

                                storageReference.downloadUrl.addOnCompleteListener(
                                    OnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Glide.with(applicationContext).load(task.result)
                                                .into(snackImage) // 사료 사진을 표시함
                                        } else {
                                            // findViewById<ImageView>(R.id.mealImage).isVisible = false
                                        }
                                    })
                            }
                        }

                when (timeSlot) {
                    "아침" -> timeSlotGroup.check(findViewById<RadioButton>(R.id.breakfast).id)
                    "점심" -> timeSlotGroup.check(findViewById<RadioButton>(R.id.lunch).id)
                    "저녁" -> timeSlotGroup.check(findViewById<RadioButton>(R.id.dinner).id)
                }

                for (i in 0 until snackSpinner.count) {
                    if (snackSpinner.getItemAtPosition(i).toString() == snackType) {
                        snackSpinner.setSelection(i)
                        break
                    }
                }

                for (i in 0 until snackUnitSpinner.count) {
                    if (snackUnitSpinner.getItemAtPosition(i).toString() == snackUnit) {
                        snackUnitSpinner.setSelection(i)
                        break
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.snackRef.child(userId).child(dogId).child(dogSnackId).addValueEventListener(postListener)
    }

    private fun imageUpload(key : String) { // 이미지를 storage에 업로드하는 함수
        Log.d("IMAGEUPLOAD", "imageUpload() is called")
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child("dogSnackImage/$userId/$dogId/$key.png")
        if (snackImageFile == "dogSnackImage/$userId/$dogId/$key.png") { // 간식 이미지가 존재했다면, storage에 저장되어있던 이미지 삭제
            storageRef.child("dogSnackImage/$userId/$dogId/$key.png").delete()
        }

        snackImageFile = "dogSnackImage/$userId/$dogId/$key.png" // 새로운 간식의 사진을 해당 이름으로 설정

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

    private fun showDialog() { // 간식 이미지 설정을 위한 다이얼로그 띄우기
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.eat_image_dialog, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)

        val alertDialog = mBuilder.show()
        val galleryBtn = alertDialog.findViewById<Button>(R.id.galleryBtn)
        galleryBtn?.setOnClickListener { // 갤러리에서 가져오기 버튼 클릭 시
            Log.d(TAG, "gallery Button Clicked")

            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)

            isImageUpload = true

            val imageView : ImageView = snackImage
            defaultImage = (imageView.drawable as BitmapDrawable).bitmap

            alertDialog.dismiss()
        }

        val deleteBtn = alertDialog.findViewById<Button>(R.id.deleteBtn)
        deleteBtn?.setOnClickListener {  // 이미지 삭제 버튼 클릭 시
            Log.d(TAG, "basic Button Clicked")

            isImageUpload = false

            val storage = Firebase.storage
            val storageRef = storage.reference
            val mountainsRef = storageRef.child("dogSnackImage/$userId/$dogId/$dogSnackId.png")
            if(snackImageFile == "dogSnackImage/$userId/$dogId/$dogSnackId.png") {    // 간식 이미지가 존재했다면, storage에 저장되어있던 이미지 삭제
                storageRef.child("dogSnackImage/$userId/$dogId/$dogSnackId.png").delete()
            }

            snackImageFile = ""
            snackImage.setImageResource(R.drawable.image_plus)

            alertDialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK && requestCode == 100) {
            Log.d("IMAGEURL", "Image URL: " + data?.data.toString())
            snackImage.setImageURI(data?.data) // 간식 이미지에 갤러리에서 선택한 이미지 넣기
            imageUpload(dogSnackId)
        }
    }

    private fun saveDogSnack(timeSlot : String, snackType : String, snackName : String, snackWeight : String, snackUnit : String) {
        FBRef.snackRef.child(userId).child(dogId).child(dogSnackId).setValue(DogSnackModel(dogSnackId, dogId, nowDate, snackImageFile, timeSlot, snackType, snackName, snackWeight, snackUnit)) // 반려견 간식 정보 데이터베이스에 저장
    }
}