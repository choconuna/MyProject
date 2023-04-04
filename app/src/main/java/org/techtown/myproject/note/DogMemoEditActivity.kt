package org.techtown.myproject.note

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.community.CommunityModel
import org.techtown.myproject.community.GalleryAdapter
import org.techtown.myproject.utils.DogMealModel
import org.techtown.myproject.utils.DogMemoModel
import org.techtown.myproject.utils.FBRef
import java.text.SimpleDateFormat
import java.util.*

class DogMemoEditActivity : AppCompatActivity() {

    private val TAG = DogMemoEditActivity::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var dogMemoId : String
    lateinit var date : String

    lateinit var galleryAdapter: GalleryAdapter
    var imageList : ArrayList<Uri> = ArrayList()
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager : RecyclerView.LayoutManager

    private lateinit var imageButton : ImageView
    private lateinit var imageCnt : TextView
    private var count = 0 // 첨부한 사진 수

    lateinit var writeBtn : Button

    lateinit var time : String

    lateinit var titleArea : EditText
    lateinit var title : String
    lateinit var contentArea : EditText
    lateinit var content : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_memo_edit)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        dogMemoId = intent.getStringExtra("id").toString() // dogMemo id
        date = intent.getStringExtra("date").toString() // 선택된 날짜

        galleryAdapter = GalleryAdapter(imageList, this) // 어댑터 초기화
        recyclerView = findViewById(R.id.imageRecyclerView)
        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = galleryAdapter

        imageCnt = findViewById(R.id.imageCnt)
        titleArea = findViewById(R.id.titleArea)
        contentArea = findViewById(R.id.contentArea)

        getData()

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
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.KOREA).format(currentDataTime)

        val editBtn = findViewById<Button>(R.id.editBtn)
        editBtn.setOnClickListener {
            val title = titleArea.text.toString()
            val content = contentArea.text.toString()

            FBRef.memoRef.child(userId).child(dogId).child(dogMemoId).setValue(DogMemoModel(dogMemoId, dogId, date, dateFormat, title, content, count.toString()))

            Toast.makeText(this, "메모 수정 완료!", Toast.LENGTH_LONG).show()

            /* if(count >= 1) { // 이미지 첨부되었을 시 이미지를 storage로 업로드
                imageUpload(key)
            } */

            finish()
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun getData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val post = dataSnapshot.getValue(DogMemoModel::class.java)
                    title = post!!.title
                    titleArea.setText(title)

                    content = post!!.content
                    contentArea.setText(content)

                    date = post!!.date
                    time = post!!.time

                    imageCnt.text = post!!.count
                    count = post!!.count.toInt()

                    /* if(post.count.toInt() >= 1) {
                        for(index in 0 until post.count.toInt()) {
                            val storageReference =
                                Firebase.storage.reference.child("memoImage/$userId/$dogId/$dogMemoId/$dogMemoId$index.png") // 메모 사진을 DB의 storage로부터 가져옴

                            storageReference.downloadUrl.addOnSuccessListener {
                                Log.d("imageList", it.toString())
                                imageList.add(it.toString().toUri())
                            }
                        }
                        Log.d("imageList", imageList.toString())
                    }
                    galleryAdapter.notifyDataSetChanged() */

                } catch (e: Exception) {
                    Log.d(TAG, "메모 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.memoRef.child(userId).child(dogId).child(dogMemoId).addValueEventListener(postListener)
    }

    private fun imageUpload(key : String) { // 이미지를 storage에 업로드하는 함수
        val storage = Firebase.storage
        val storageRef = storage.reference

        for(cnt in 0 until count) {
            val mountainsRef = storageRef.child("memoImage/$userId/$dogId/$key/$key$cnt.png")

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
}