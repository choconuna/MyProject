package org.techtown.myproject.community

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.utils.FBRef
import java.text.DateFormat
import java.util.ArrayList

class SpecificCommunityEditActivity : AppCompatActivity() {

    private val TAG = SpecificCommunityEditActivity::class.java.simpleName

    lateinit var category : String // 작성된 글의 카테고리

    private lateinit var auth: FirebaseAuth
    private lateinit var writerUid : String // 작성자의 uid

    private lateinit var key : String // 게시글의 key

    lateinit var galleryAdapter: GalleryAdapter
    var imageList : ArrayList<Uri> = ArrayList()
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager : RecyclerView.LayoutManager

    private lateinit var titleArea : EditText
    private lateinit var contentArea : EditText
    private lateinit var time : String

    private lateinit var imageButton : ImageView
    private lateinit var imageCnt : TextView
    private var originCount = 0 // 기존 첨부 사진 수
    private var count = 0 // 첨부한 사진 수

    lateinit var editBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specific_community_edit)

        category = intent.getStringExtra("category").toString() // 게시글의 카테고리
        findViewById<TextView>(R.id.textView).text = category + "글 수정하기"

        titleArea = findViewById(R.id.titleArea)
        contentArea = findViewById(R.id.contentArea)
        imageCnt = findViewById(R.id.imageCnt)

        auth = FirebaseAuth.getInstance()
        writerUid = auth.currentUser?.uid.toString() // 작성자의 uid
        key = intent.getStringExtra("key").toString() // 게시글의 id
        getCommunityData(key)

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

        val editBtn = findViewById<Button>(R.id.editBtn)
        editBtn.setOnClickListener {
            val title = titleArea.text.toString()
            val content = contentArea.text.toString()

            FBRef.communityRef.child(category).child(key).setValue(CommunityModel(writerUid, key, category, title, content, (originCount+count).toString(), time)) // 게시물 정보 데이터베이스에 저장

            Toast.makeText(this, "게시글 수정 완료!", Toast.LENGTH_LONG).show()

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
            var num = originCount + cnt // 기존 사진 수 + 현재 사진 인덱스 -> 기존 사진 뒤에 이어붙임
            val mountainsRef = storageRef.child("communityImage/$key/$key$num.png")

            var uploadTask = mountainsRef.putFile(imageList[cnt])
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
            }.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            }
        }
    }

    private fun getCommunityData(key : String) {
        val postListener = object : ValueEventListener { // 파이어베이스 안의 값이 변화할 시 작동됨
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dataModel = dataSnapshot.getValue(CommunityModel::class.java)

                titleArea.setText(dataModel!!.title)
                contentArea.setText(dataModel!!.content)
                originCount = dataModel!!.count.toInt()
                writerUid = dataModel!!.uid
                time = dataModel!!.time
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.communityRef.child(category).child(key).addValueEventListener(postListener)
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