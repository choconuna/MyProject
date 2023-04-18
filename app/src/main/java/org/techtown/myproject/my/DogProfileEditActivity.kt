package org.techtown.myproject.my

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef
import java.io.ByteArrayOutputStream

class DogProfileEditActivity : AppCompatActivity() {

    private val TAG = DogProfileEditActivity::class.java.simpleName

    lateinit var userId : String
    private lateinit var key : String
    lateinit var mDatabaseReference: DatabaseReference

    private lateinit var profileImage : ImageView
    private lateinit var dogProfileFile : String
    private var isBasicProfile : Boolean = false
    private lateinit var defaultImage : Bitmap
    var isImageUpload = false

    lateinit var dogNameArea : EditText
    lateinit var dogName : String
    lateinit var originName : String
    var dogNameCheckClicked : Boolean = false
    var dogNameChecked : Boolean = false

    lateinit var dogBirthArea : EditText
    lateinit var dogBirthDate : String

    lateinit var dogSexGroup : RadioGroup
    lateinit var dogSex : String

    lateinit var spinner : Spinner

    lateinit var dogSpecies : String

    lateinit var dogWeightArea : EditText
    lateinit var dogWeight : String

    lateinit var neutralizationGroup : RadioGroup
    lateinit var neutralization : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_profile_edit)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        key = intent.getStringExtra("key").toString() // 사용자의 반려견의 id

        mDatabaseReference = FBRef.dogRef.child(userId).child(key)

        profileImage = findViewById(R.id.dogImage)
        dogNameArea = findViewById(R.id.dogNameArea)
        dogBirthArea = findViewById(R.id.dogBirth)
        dogSexGroup = findViewById(R.id.sexGroup)
        spinner = findViewById(R.id.spinner)
        dogWeightArea = findViewById(R.id.dogWeightArea)
        neutralizationGroup = findViewById(R.id.neutralization)

        getDogData()

        profileImage.setOnClickListener {
            showDialog()
        }

        val dogNameCheckBtn = findViewById<Button>(R.id.dogNameCheckBtn)
        dogNameCheckBtn.setOnClickListener {
            dogNameCheckClicked = true
            dogName = dogNameArea.text.toString().trim()
            checkDogName(dogName)
        }

        val editBtn = findViewById<Button>(R.id.editBtn)
        editBtn.setOnClickListener {
            dogSexGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.male -> dogSex = "수컷"
                    R.id.female -> dogSex = "암컷"
                }
            }

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    dogSpecies = parent.getItemAtPosition(position).toString()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            neutralizationGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.yes -> neutralization = "YES"
                    R.id.no -> neutralization = "NO"
                }
            }

            Log.d("check", dogSex + dogSpecies + neutralization)

            dogName = dogNameArea.text.toString().trim()
            dogBirthDate = dogBirthArea.text.toString().trim()
            dogWeight = dogWeightArea.text.toString().trim()

            when {
                dogName == "" -> {
                    Toast.makeText(this, "반려견 이름을 입력하세요!", Toast.LENGTH_LONG).show()
                    dogNameArea.setSelection(0)
                }
                dogBirthDate == "" -> {
                    Toast.makeText(this, "반려견 생일을 입력하세요!", Toast.LENGTH_LONG).show()
                    dogBirthArea.setSelection(0)
                }
                dogWeight == "" -> {
                    Toast.makeText(this, "반려견 몸무게를 입력하세요!", Toast.LENGTH_LONG).show()
                    dogWeightArea.setSelection(0)
                }
                originName == dogNameArea.text.toString().trim() || (dogNameCheckClicked && dogNameChecked) -> {
                    editDogProfile(key) // 사용자 반려견의 profile 내용 수정
                    Toast.makeText(this, "반려견 프로필이 수정되었습니다!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                !dogNameCheckClicked -> {
                    if(originName != dogName) {
                        Toast.makeText(this, "반려견 이름이 중복되는지 확인하세요!", Toast.LENGTH_LONG).show()
                        dogNameArea.setSelection(0)
                    }
                }
                !dogNameChecked -> {
                    if(originName != dogName) {
                        Toast.makeText(this, "반려견 이름이 중복됩니다. 다시 입력하세요!", Toast.LENGTH_LONG).show()
                        dogNameArea.setText("")
                        dogNameArea.setSelection(0)
                        dogNameCheckClicked = false
                    }
                }
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun checkDogName(dogName : String) { // 반려견 이름이 중복되었는지 확인
        FBRef.dogRef.child(userId).orderByChild("dogName").equalTo(dogName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dogNameChecked = if (!dataSnapshot.exists()) {
                    Toast.makeText(applicationContext, "반려견 이름이 중복되지 않습니다!", Toast.LENGTH_SHORT).show()
                    true
                } else {
                    Toast.makeText(applicationContext, "반려견 이름이 중복됩니다!", Toast.LENGTH_SHORT).show()
                    false
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(applicationContext, databaseError.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getDogData() { // 기존에 저장된 데이터를 가져옴
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val post = dataSnapshot.getValue(DogModel::class.java)
                dogName = post!!.dogName
                dogNameArea.setText(dogName)
                originName = post!!.dogName

                dogBirthDate = post!!.dogBirthDate
                dogBirthArea.setText(dogBirthDate)

                dogSex = post!!.dogSex
                dogSpecies = post!!.dogSpecies

                dogWeight = post!!.dogWeight
                dogWeightArea.setText(dogWeight)

                neutralization = post!!.neutralization

                dogProfileFile = post!!.dogProfileFile // 가져올 유저의 profile 사진

                val profileFile =
                    mDatabaseReference.child("dogProfileFile").get().addOnSuccessListener {
                        val storageReference =
                            Firebase.storage.reference.child(it.value.toString()) // 반려견의 profile 사진을 DB의 storage로부터 가져옴

                        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Glide.with(applicationContext).load(task.result)
                                    .into(profileImage) // 반려견의 profile 사진을 표시함
                            } else {
                                // findViewById<ImageView>(R.id.profileImageArea).isVisible = false
                            }
                        })
                    }

                for(i in 0 until spinner.count) {
                    if(spinner.getItemAtPosition(i).toString() == dogSpecies)  {
                        spinner.setSelection(i)
                        break
                    }
                }

                if(dogSex == "수컷")
                    dogSexGroup.check(findViewById<RadioButton>(R.id.male).id)
                else
                    dogSexGroup.check(findViewById<RadioButton>(R.id.female).id)

                if(neutralization == "YES")
                    neutralizationGroup.check(findViewById<RadioButton>(R.id.yes).id)
                else
                    neutralizationGroup.check(findViewById<RadioButton>(R.id.no).id)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        mDatabaseReference.addValueEventListener(postListener)
    }

    private fun imageUpload(key : String) { // 이미지를 storage에 업로드하는 함수
        Log.d("IMAGEUPLOAD", "imageUpload() is called")
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child("dogProfileImage/$userId/$key.png")
        if(dogProfileFile == "dogProfileImage/$userId/$key.png") { // 반려견의 profile 이미지가 기본 이미지가 아니었다면, storage에 저장되어있던 이미지 삭제
            storageRef.child("dogProfileImage/$userId/$key.png").delete()
        }

        dogProfileFile = "dogProfileImage/$userId/$key.png" // dogProfileImage 아래의 반려견의 uid 값 이름으로 되어있는 png 파일을 반려견의 profile file 이름으로 설정

        profileImage.isDrawingCacheEnabled = true
        profileImage.buildDrawingCache()
        val bitmap = (profileImage.drawable as BitmapDrawable).bitmap
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

    private fun showDialog() { // 프로필 이미지 설정을 위한 다이얼로그 띄우기
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.profile_image_dialog, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)

        val alertDialog = mBuilder.show()
        val galleryBtn = alertDialog.findViewById<Button>(R.id.galleryBtn)
        galleryBtn?.setOnClickListener { // 갤러리에서 가져오기 버튼 클릭 시
            Log.d(TAG, "gallery Button Clicked")

            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)

            isImageUpload = true

            val imageView : ImageView = profileImage
            defaultImage = (imageView.drawable as BitmapDrawable).bitmap

            alertDialog.dismiss()
        }

        val basicBtn = alertDialog.findViewById<Button>(R.id.basicBtn)
        basicBtn?.setOnClickListener {  // 기본 이미지로 설정 버튼 클릭 시
            Log.d(TAG, "basic Button Clicked")

            isImageUpload = false

            val storage = Firebase.storage
            val storageRef = storage.reference
            val mountainsRef = storageRef.child("dogProfileImage/$userId/$key.png")
            if(dogProfileFile == "dogProfileImage/$userId/$key.png") {    // 반려견의 profile 이미지가 기본 이미지가 아니었다면, storage에 저장되어있던 이미지 삭제
                storageRef.child("dogProfileImage/$userId/$key.png").delete()
            }

            dogProfileFile = "basic_user.png"
            profileImage.setImageResource(R.drawable.blankuser)

            alertDialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK && requestCode == 100) {
            Log.d("IMAGEURL", "Image URL: " + data?.data.toString())
            profileImage.setImageURI(data?.data) // profileImage에 갤러리에서 선택한 이미지 넣기
            imageUpload(key)
        }
    }

    private fun editDogProfile(key : String) {
        FBRef.dogRef.child(userId).child(key).setValue(DogModel(key, dogProfileFile, dogName, dogBirthDate, dogSex, dogSpecies, dogWeight, neutralization)) // 반려견 정보 데이터베이스에 저장
    }
}