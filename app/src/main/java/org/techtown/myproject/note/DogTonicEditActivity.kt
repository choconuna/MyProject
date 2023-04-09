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
import android.webkit.WebView
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
import org.techtown.myproject.utils.DogSnackModel
import org.techtown.myproject.utils.DogTonicModel
import org.techtown.myproject.utils.FBRef
import java.io.ByteArrayOutputStream

class DogTonicEditActivity : AppCompatActivity() {

    private val TAG = DogTonicEditActivity::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var nowDate : String
    lateinit var dogTonicId : String

    private lateinit var tonicImage : ImageView
    private var tonicImageFile : String = ""
    private lateinit var defaultImage : Bitmap
    var isImageUpload : Boolean = false

    lateinit var timeSlotGroup : RadioGroup
    lateinit var timeSlot : String

    lateinit var tonicPartSpinner : Spinner
    lateinit var tonicPart : String
    lateinit var tonicNameArea : EditText
    lateinit var tonicName : String
    lateinit var tonicWeightArea : EditText
    lateinit var tonicWeight : String
    lateinit var tonicUnitSpinner : Spinner
    lateinit var tonicUnit : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_tonic_edit)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        dogTonicId = intent.getStringExtra("id").toString() // dogTonic id
        nowDate = intent.getStringExtra("date").toString() // 선택된 날짜

        tonicImage = findViewById(R.id.tonicImage)
        timeSlotGroup = findViewById(R.id.time)
        tonicPartSpinner = findViewById(R.id.partSpinner)
        tonicNameArea = findViewById(R.id.tonicNameArea)
        tonicWeightArea = findViewById(R.id.tonicWeightArea)
        tonicUnitSpinner = findViewById(R.id.tonicUnitSpinner)

        getData()

        tonicImage.setOnClickListener {
            showDialog()
        }

        timeSlotGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.breakfast -> timeSlot = "아침"
                R.id.lunch -> timeSlot = "점심"
                R.id.dinner -> timeSlot = "저녁"
            }
        }

        tonicPartSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                tonicPart = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        tonicUnitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                tonicUnit = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val editBtn = findViewById<Button>(R.id.editBtn)
        editBtn.setOnClickListener {

            tonicName = tonicNameArea.text.toString().trim()
            tonicWeight = tonicWeightArea.text.toString().trim()

            when {
                tonicName == "" -> {
                    Toast.makeText(this, "영양제 이름을 입력하세요!", Toast.LENGTH_SHORT).show()
                    tonicNameArea.setSelection(0)
                }
                tonicWeight == "" -> {
                    Toast.makeText(this, "영양제의 양을 입력하세요!", Toast.LENGTH_SHORT).show()
                    tonicWeightArea.setSelection(0)
                }
                else -> {
                    saveDogTonic(timeSlot, tonicPart, tonicName, tonicWeight, tonicUnit)
                    Toast.makeText(this, "영양제 정보가 수정되었습니다!", Toast.LENGTH_SHORT).show()
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
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val post = dataSnapshot.getValue(DogTonicModel::class.java)
                timeSlot = post!!.timeSlot

                tonicNameArea.setText(post!!.tonicName)
                tonicWeightArea.setText(post!!.tonicWeight)
                tonicPart = post!!.tonicPart
                tonicUnit = post!!.tonicUnit

                tonicImageFile = post!!.tonicImageFile // 가져올 영양제 사진

                val profileFile =
                    FBRef.tonicRef.child(userId).child(dogId).child(dogTonicId)
                        .child("tonicImageFile").get().addOnSuccessListener {
                            if(it.value != "") {
                                val storageReference =
                                    Firebase.storage.reference.child(it.value.toString()) // 영양제 사진을 DB의 storage로부터 가져옴

                                storageReference.downloadUrl.addOnCompleteListener(
                                    OnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Glide.with(applicationContext).load(task.result)
                                                .into(tonicImage) // 영양제 사진을 표시함
                                        } else {
                                            findViewById<ImageView>(R.id.tonicImage).isVisible =
                                                false
                                        }
                                    })
                            }
                        }

                when (timeSlot) {
                    "아침" -> timeSlotGroup.check(findViewById<RadioButton>(R.id.breakfast).id)
                    "점심" -> timeSlotGroup.check(findViewById<RadioButton>(R.id.lunch).id)
                    "저녁" -> timeSlotGroup.check(findViewById<RadioButton>(R.id.dinner).id)
                }

                for (i in 0 until tonicPartSpinner.count) {
                    if (tonicPartSpinner.getItemAtPosition(i).toString() == tonicPart) {
                        tonicPartSpinner.setSelection(i)
                        break
                    }
                }

                for (i in 0 until tonicUnitSpinner.count) {
                    if (tonicUnitSpinner.getItemAtPosition(i).toString() == tonicUnit) {
                        tonicUnitSpinner.setSelection(i)
                        break
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.tonicRef.child(userId).child(dogId).child(dogTonicId).addValueEventListener(postListener)
    }

    private fun imageUpload(key : String) { // 이미지를 storage에 업로드하는 함수
        Log.d("IMAGEUPLOAD", "imageUpload() is called")
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child("dogTonicImage/$userId/$dogId/$key.png")
        if (tonicImageFile == "dogTonicImage/$userId/$dogId/$key.png") { // 영양제 이미지가 존재했다면, storage에 저장되어있던 이미지 삭제
            storageRef.child("dogTonicImage/$userId/$dogId/$key.png").delete()
        }

        tonicImageFile = "dogTonicImage/$userId/$dogId/$key.png" // 새로운 영양제 사진을 해당 이름으로 설정

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

    private fun showDialog() { // 영양제 이미지 설정을 위한 다이얼로그 띄우기
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.eat_image_dialog, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)

        val alertDialog = mBuilder.show()
        val galleryBtn = alertDialog.findViewById<Button>(R.id.galleryBtn)
        galleryBtn?.setOnClickListener { // 갤러리에서 가져오기 버튼 클릭 시
            Log.d(TAG, "gallery Button Clicked")

            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)

            isImageUpload = true

            val imageView : ImageView = tonicImage
            defaultImage = (imageView.drawable as BitmapDrawable).bitmap

            alertDialog.dismiss()
        }

        val deleteBtn = alertDialog.findViewById<Button>(R.id.deleteBtn)
        deleteBtn?.setOnClickListener {  // 이미지 삭제 버튼 클릭 시
            Log.d(TAG, "basic Button Clicked")

            isImageUpload = false

            val storage = Firebase.storage
            val storageRef = storage.reference
            val mountainsRef = storageRef.child("dogTonicImage/$userId/$dogId/$dogTonicId.png")
            if(tonicImageFile == "dogTonicImage/$userId/$dogId/$dogTonicId.png") {    // 영양제 이미지가 존재했다면, storage에 저장되어있던 이미지 삭제
                storageRef.child("dogTonicImage/$userId/$dogId/$dogTonicId.png").delete()
            }

            tonicImageFile = ""
            tonicImage.setImageResource(R.drawable.image_plus)

            alertDialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK && requestCode == 100) {
            Log.d("IMAGEURL", "Image URL: " + data?.data.toString())
            tonicImage.setImageURI(data?.data) // 영양제 이미지에 갤러리에서 선택한 이미지 넣기
            imageUpload(dogTonicId)
        }
    }

    private fun saveDogTonic(timeSlot : String, tonicPart : String, tonicName : String, tonicWeight : String, tonicUnit : String) {
        FBRef.tonicRef.child(userId).child(dogId).child(dogTonicId).setValue(DogTonicModel(dogTonicId, dogId, nowDate, tonicImageFile, timeSlot, tonicPart, tonicName, tonicWeight, tonicUnit)) // 반려견 영양제 정보 데이터베이스에 저장
    }
}