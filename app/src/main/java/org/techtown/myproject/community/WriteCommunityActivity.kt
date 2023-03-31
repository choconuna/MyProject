package org.techtown.myproject.community

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.utils.FBRef
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class WriteCommunityActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var uid : String

    private var isImageUpload = false // 이미지가 첨부되었는지 안 되었는지 확인하는 변수

    private val TAG = WriteCommunityActivity::class.java.simpleName

    lateinit var writeBtn : Button
    lateinit var imageArea : ImageView

    lateinit var defaultImage : Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_community)

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()

        val currentDataTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentDataTime)

        writeBtn = findViewById(R.id.writeBtn)
        writeBtn.setOnClickListener {
            val title = findViewById<TextView>(R.id.titleArea).text.toString()
            val content = findViewById<TextView>(R.id.contentArea).text.toString()

            Log.d(TAG, title)
            Log.d(TAG, content)

            val key = FBRef.communityRef.push().key.toString() // 키 값을 먼저 받아옴 -> 게시물에 해당하는 이미지의 이름을 키 값으로 설정하기 위함

            FBRef.communityRef.child(key).setValue(CommunityModel(title, content, uid, dateFormat)) // 게시물 정보 데이터베이스에 저장

            Toast.makeText(this, "게시글 입력 완료", Toast.LENGTH_LONG).show()

            if(isImageUpload) { // 이미지 첨부되었을 시 이미지를 storage로 업로드
                val imageView : ImageView = imageArea
                val changedImage : BitmapDrawable = imageView.drawable as BitmapDrawable
                val bitmap : Bitmap = changedImage.bitmap

                if(bitmap != defaultImage) { // 기본 이미지와 비교 -> 이미지 업로드 누르고 이미지 선택 안 하고 나올 시 검은 화면이 나오는 것을 방지하기 위함
                    imageUpload(key)
                }
            }

            finish()
        }

        imageArea = findViewById(R.id.imageArea)
        imageArea.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)

            isImageUpload = true // 이미지를 첨부하였다는 의미

            val imageView : ImageView = imageArea
            defaultImage = (imageView.drawable as BitmapDrawable).bitmap
        }
    }

    private fun imageUpload(key : String) { // 이미지를 storage에 업로드하는 함수
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child("community/$key.png")

        imageArea.isDrawingCacheEnabled = true
        imageArea.buildDrawingCache()
        val bitmap = (imageArea.drawable as BitmapDrawable).bitmap
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
            imageArea.setImageURI(data?.data) // imageArea에 갤러리에서 선택한 이미지 넣기
        }
    }
}