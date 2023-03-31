package org.techtown.myproject.community

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.my.DogProfileInActivity
import org.techtown.myproject.my.DogReVAdapter
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class WriteSpecificCommunityActivity : AppCompatActivity() {

    private val TAG = WriteSpecificCommunityActivity::class.java.simpleName

    lateinit var galleryAdapter: GalleryAdapter
    var imageList : ArrayList<Uri> = ArrayList()
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager : RecyclerView.LayoutManager

    lateinit var category : String // 작성된 글의 카테고리

    private lateinit var auth: FirebaseAuth
    private lateinit var uid : String

    private lateinit var imageButton : ImageView
    private lateinit var imageCnt : TextView
    private var count = 0 // 첨부한 사진 수

    lateinit var writeBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_specific_community)

        category = intent.getStringExtra("category").toString()
        findViewById<TextView>(R.id.textView).text = category + "글 작성하기"

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()

        imageCnt = findViewById(R.id.imageCnt)

        galleryAdapter = GalleryAdapter(imageList, this) // 어댑터 초기화
        recyclerView = findViewById(R.id.imageRecyclerView)
        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = galleryAdapter

        // 사진 클릭 시 사진 삭제하기
        galleryAdapter.setItemClickListener(object: GalleryAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                Log.d("removeImage", "이미지 삭제")
                imageList.remove(imageList[position]) // imageList에서 해당 사진 삭제
                count -= 1 // 첨부한 사진의 수를 1 줄임
                imageCnt.text = count.toString() // 첨부한 사진의 수를 반영
                Log.d("imageList", imageList.toString())
                galleryAdapter.notifyDataSetChanged()
            }
        })

        imageButton = findViewById(R.id.imageBtn)
        imageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            activityResult.launch(intent)
        }

        val currentDataTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentDataTime)

        writeBtn = findViewById(R.id.writeBtn)
        writeBtn.setOnClickListener {
            val title = findViewById<TextView>(R.id.titleArea).text.toString()
            val content = findViewById<TextView>(R.id.contentArea).text.toString()

            Log.d(TAG, title)
            Log.d(TAG, content)

            val key = FBRef.communityRef.child(category).push().key.toString() // 게시물의 키 값을 먼저 받아옴 -> 게시물에 해당하는 이미지의 이름을 게시물의 키 값으로 설정하기 위함

            FBRef.communityRef.child(category).child(key).setValue(CommunityModel(uid, key, category, title, content, count.toString(), dateFormat)) // 게시물 정보 데이터베이스에 저장

            Toast.makeText(this, "게시글 입력 완료", Toast.LENGTH_LONG).show()

            if(count >= 1) { // 이미지 첨부되었을 시 이미지를 storage로 업로드
                imageUpload(key)
            }
            finish()
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun imageUpload(key : String) { // 이미지를 storage에 업로드하는 함수
        val storage = Firebase.storage
        val storageRef = storage.reference

        for(cnt in 0 until count) {
            val mountainsRef = storageRef.child("communityImage/$key/$key$cnt.png")

            var uploadTask = mountainsRef.putFile(imageList[cnt])
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
            }.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            }
        }
    }

    private val activityResult : ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {

        if(it.resultCode == RESULT_OK) { // 결과 코드 OK, 아니면 null
            if(it.data!!.clipData != null) { // 멀티 이미지일 경우
                count += it.data!!.clipData!!.itemCount // 선택한 이미지 개수

                for(index in 0 until it.data!!.clipData!!.itemCount) {
                    val imageUri = it.data!!.clipData!!.getItemAt(index).uri // 이미지 담기
                    imageList.add(imageUri) // 이미지 추가
                }
            } else { // 싱글 이미지일 경우
                count += it.data!!.clipData!!.itemCount
                val imageUri = it.data!!.data
                imageList.add(imageUri!!)
            }
            galleryAdapter.notifyDataSetChanged() // 동기화
            imageCnt.text = count.toString()
            Log.d("imageList", imageList.toString())
        }
    }

    /* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK && requestCode == 100) {
            imageArea.setImageURI(data?.data) // imageArea에 갤러리에서 선택한 이미지 넣기
        }
    } */
}