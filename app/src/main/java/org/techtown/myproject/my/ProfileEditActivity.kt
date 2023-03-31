package org.techtown.myproject.my

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import org.techtown.myproject.utils.UserInfo
import org.techtown.myproject.utils.FBRef
import java.io.ByteArrayOutputStream

class ProfileEditActivity : AppCompatActivity() {

    private val TAG = ProfileEditActivity::class.java.simpleName

    private lateinit var nameArea : EditText
    private lateinit var nickNameArea : EditText
    private lateinit var profileImage : ImageView

    private lateinit var defaultImage : Bitmap

    lateinit var originName : String
    lateinit var nickName : String
    var nickNameCheckClicked : Boolean = false
    var nickNameChecked : Boolean = false

    private lateinit var email : String
    private lateinit var pw : String

    lateinit var profileFile : String
    var isImageUpload = false

    lateinit var mDatabaseReference: DatabaseReference
    lateinit var mAuth : FirebaseAuth
    lateinit var uid : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        val backBtn = findViewById<ImageView>(R.id.backBtn)
        backBtn.setOnClickListener { // 뒤로 가기 버튼 클릭 시 액티비티 종료
            finish()
        }

        profileImage = findViewById(R.id.imageView)
        nameArea = findViewById(R.id.userName)
        nickNameArea = findViewById(R.id.nickNameArea)

        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser?.uid.toString()

        mDatabaseReference = FBRef.userRef.child(uid)

        getProfileData(uid) // 기존 사용자의 profile 데이터 가져오기

        profileImage.setOnClickListener {
            showDialog()
        }

        val nickNameCheckBtn = findViewById<Button>(R.id.nickNameCheckBtn)
        nickNameCheckBtn.setOnClickListener {
            nickNameCheckClicked = true
            nickName = nickNameArea.text.toString().trim()
            checkNickName(nickName)
        }

        findViewById<Button>(R.id.editBtn).setOnClickListener {
            editProfile(uid) // 사용자의 profile 내용 수정
        }
    }

    private fun getProfileData(key : String) { // 프로필 데이터 가져옴
        val postListener = object : ValueEventListener { // 파이어베이스 안의 값이 변화할 시 작동됨
            @RequiresApi(Build.VERSION_CODES.S)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dataModel = dataSnapshot.getValue(UserInfo::class.java)

                profileFile = dataModel!!.profileImage
                email = dataModel!!.email
                pw = dataModel!!.password

                val profileFile = mDatabaseReference.child("profileImage").get().addOnSuccessListener {
                    val storageReference = Firebase.storage.reference.child(it.value.toString()) // 유저의 profile 사진을 DB의 storage로부터 가져옴

                    storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                        if(task.isSuccessful) {
                            Glide.with(applicationContext).load(task.result).into(profileImage) // 유저의 profile 사진을 게시자 이름의 왼편에 표시함
                            profileImage.setAllowClickWhenDisabled(true)
                        } else {
                        }
                    })
                }

                val userName = mDatabaseReference.child("userName").get().addOnSuccessListener {
                    nameArea.setText(it.value.toString()) // 작성자의 이름 가져오기
                }

                val nickName = mDatabaseReference.child("nickName").get().addOnSuccessListener {
                    nickNameArea.setText(it.value.toString()) // 닉네임 가져오기
                    originName = it.value.toString()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        mDatabaseReference.addValueEventListener(postListener)
    }

    private fun checkNickName(nickname : String) { // 닉네임이 중복되었는지 확인
        FBRef.userRef.orderByChild("nickName").equalTo(nickname).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                nickNameChecked = if (!dataSnapshot.exists()) {
                    Toast.makeText(applicationContext, "닉네임이 중복되지 않습니다!", Toast.LENGTH_SHORT).show()
                    true
                } else {
                    Toast.makeText(applicationContext, "닉네임이 중복됩니다!", Toast.LENGTH_SHORT).show()
                    false
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(applicationContext, databaseError.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun imageUpload(key : String) { // 이미지를 storage에 업로드하는 함수
        Log.d("IMAGEUPLOAD", "imageUpload() is called")
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child("profileImage/$key.png")
        if(profileFile == "profileImage/$key.png") { // 사용자의 profile 이미지가 기본 이미지가 아니었다면, storage에 저장되어있던 이미지 삭제
            storageRef.child("profileImage/$key.png").delete()
        }

        profileFile = "profileImage/$key.png" // profileImage 아래의 사용자 uid 값 이름으로 되어있는 png 파일을 사용자의 profile file 이름으로 설정

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
            val mountainsRef = storageRef.child("profileImage/$uid.png")
            if(profileFile == "profileImage/$uid.png") {    // 사용자의 profile 이미지가 기본 이미지가 아니었다면, storage에 저장되어있던 이미지 삭제
                storageRef.child("profileImage/$uid.png").delete()
            }

            profileFile = "basic_user.png"
            profileImage.setImageResource(R.drawable.blankuser)

            alertDialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK && requestCode == 100) {
            profileImage.setImageURI(data?.data) // profileImage에 갤러리에서 선택한 이미지 넣기
            imageUpload(uid)
        }
    }

    private fun editProfile(key : String) {
        Log.d(TAG, originName + " " + nickNameArea.text.toString().trim())
        if(originName == nickNameArea.text.toString().trim() || (nickNameCheckClicked && nickNameChecked)) {
            FBRef.userRef.child(key).setValue(UserInfo(profileFile, nameArea.text.toString().trim(), nickNameArea.text.toString().trim(), email, pw)) // 게시물 정보 데이터베이스에 저장
            Toast.makeText(this, "프로필 수정 완료", Toast.LENGTH_SHORT).show()
            finish() // 창 닫기
        } else if(!nickNameCheckClicked) {
            Toast.makeText(this, "닉네임이 중복되는지 확인하세요!", Toast.LENGTH_LONG).show()
            nickNameArea.setSelection(0)
        } else if(!nickNameChecked) {
            Toast.makeText(this, "닉네임이 중복됩니다. 다시 입력하세요!", Toast.LENGTH_LONG).show()
            nickNameArea.setText("")
            nickNameArea.setSelection(0)
            nickNameCheckClicked = false
        }
    }
}