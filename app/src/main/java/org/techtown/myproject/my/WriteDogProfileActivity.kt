package org.techtown.myproject.my

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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef
import java.io.ByteArrayOutputStream

class WriteDogProfileActivity : AppCompatActivity() {

    private val TAG = WriteDogProfileActivity::class.java.simpleName

    lateinit var mFirebaseAuth: FirebaseAuth
    lateinit var userId : String
    lateinit var dogDB : DatabaseReference

    private lateinit var dogProfileImage : ImageView
    private var dogProfileFile : String = "basic_user.png"
    private lateinit var defaultImage : Bitmap
    var isImageUpload : Boolean = false

    var dogNameCheckClicked : Boolean = false
    var dogNameChecked : Boolean = false
    lateinit var dogNameArea : EditText
    lateinit var dogName : String

    lateinit var dogBirthArea : EditText
    lateinit var dogBirthDate : String

    lateinit var dogSex : String

    lateinit var spinner : Spinner
    lateinit var species : String

    lateinit var dogWeightArea : EditText
    lateinit var dogWeight : String

    lateinit var neutralization : String

    private lateinit var sharedPreferences: SharedPreferences // 대표 반려견 id를 저장하기 위함
    lateinit var editor : SharedPreferences.Editor

    lateinit var saveBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_dog_profile)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        dogNameArea = findViewById(R.id.dogNameArea)
        val dogNameCheckBtn = findViewById<Button>(R.id.dogNameCheckBtn)
        dogNameCheckBtn.setOnClickListener {
            dogNameCheckClicked = true
            dogName = dogNameArea.text.toString().trim()
            checkDogName(dogName)
        }

        dogBirthArea = findViewById(R.id.dogBirth)
        dogWeightArea = findViewById(R.id.dogWeightArea)
        dogProfileImage = findViewById(R.id.dogImage)

        dogSex = "수컷"
        val sexGroup = findViewById<RadioGroup>(R.id.sexGroup)
        sexGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.male -> dogSex = "수컷"
                R.id.female -> dogSex = "암컷"
            }
        }

        spinner = findViewById(R.id.spinner)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                species = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        neutralization = "YES"
        val neutralizationGroup = findViewById<RadioGroup>(R.id.neutralization)
        neutralizationGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.yes -> neutralization = "YES"
                R.id.no -> neutralization = "NO"
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }

        saveBtn = findViewById(R.id.saveBtn)
        saveBtn.setOnClickListener {
            dogName = dogNameArea.text.toString().trim()
            dogBirthDate = dogBirthArea.text.toString().trim()
            dogWeight = dogWeightArea.text.toString().trim()

            when {
                dogName == "" -> {
                    Toast.makeText(this, "반려견 이름을 입력하세요!", Toast.LENGTH_LONG).show()
                    dogNameArea.setSelection(0)
                }
                !dogNameCheckClicked -> {
                    Toast.makeText(this, "반려견 이름이 중복되는지 확인하세요!", Toast.LENGTH_LONG).show()
                    dogNameArea.setSelection(0)
                }
                !dogNameChecked -> {
                    Toast.makeText(this, "반려견 이름이 중복됩니다. 다시 입력하세요!", Toast.LENGTH_LONG).show()
                    dogNameArea.setText("")
                    dogNameArea.setSelection(0)
                    dogNameCheckClicked = false
                }
                dogBirthDate == "" -> {
                    Toast.makeText(this, "반려견 생일을 입력하세요!", Toast.LENGTH_LONG).show()
                    dogBirthArea.setSelection(0)
                }
                dogWeight == "" -> {
                    Toast.makeText(this, "반려견 몸무게를 입력하세요!", Toast.LENGTH_LONG).show()
                    dogWeightArea.setSelection(0)
                }
                else -> {
                    saveDogProfile(userId, dogName, dogBirthDate, dogSex, species, dogWeight, neutralization)
                    Toast.makeText(this, "반려견 추가 등록되었습니다!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        dogProfileImage.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)

            isImageUpload = true // 이미지를 첨부하였다는 의미
            val imageView : ImageView = dogProfileImage
            defaultImage = (imageView.drawable as BitmapDrawable).bitmap
        }
    }

    private fun checkDogName(dogName : String) { // 반려견 이름이 중복되었는지 확인
        FBRef.dogRef.child(userId).orderByChild("dogName").equalTo(dogName).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dogNameChecked = if (!dataSnapshot.exists()) {
                    Toast.makeText(this@WriteDogProfileActivity, "이름이 중복되지 않습니다!", Toast.LENGTH_SHORT).show()
                    true
                } else {
                    Toast.makeText(this@WriteDogProfileActivity, "이름이 중복됩니다!", Toast.LENGTH_SHORT).show()
                    false
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@WriteDogProfileActivity, databaseError.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun imageUpload(key : String) { // 이미지를 storage에 업로드하는 함수
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child("dogProfileImage/$userId/$key.png")

        dogProfileFile = "dogProfileImage/$userId/$key.png"

        dogProfileImage.isDrawingCacheEnabled = true
        dogProfileImage.buildDrawingCache()
        val bitmap = (dogProfileImage.drawable as BitmapDrawable).bitmap
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
            dogProfileImage.setImageURI(data?.data) // dogProfileImage에 갤러리에서 선택한 이미지 넣기
        }
    }

    private fun saveDogProfile(uid : String, dogName : String, dogBirthDate : String, dogSex : String, dogSpecies : String, dogWeight : String, neutralization : String) {
        val key = FBRef.dogRef.push().key.toString() // 키 값을 먼저 받아옴 -> 반려견 프로필 사진의 이름을 키 값으로 설정하기 위함

        if(isImageUpload) { // 이미지 첨부되었을 시 이미지를 storage로 업로드
            val imageView : ImageView = dogProfileImage
            val changedImage : BitmapDrawable = imageView.drawable as BitmapDrawable
            val bitmap : Bitmap = changedImage.bitmap

            if(bitmap != defaultImage) { // 기본 이미지와 비교 -> 이미지 업로드 누르고 이미지 선택 안 하고 나올 시 검은 화면이 나오는 것을 방지하기 위함
                imageUpload(key)
            }
        }

        FBRef.dogRef.child(uid).child(key).setValue(DogModel(dogProfileFile, dogName, dogBirthDate, dogSex, dogSpecies, dogWeight, neutralization)) // 반려견 정보 데이터베이스에 저장
    }
}