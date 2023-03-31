package org.techtown.myproject.community

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.utils.FBRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R

class CommunityEditActivity : AppCompatActivity() {

    private val TAG = CommunityEditActivity::class.java.simpleName

    lateinit var titleArea : TextView
    lateinit var contentArea : TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var dateFormat : String

    private lateinit var key : String

    private lateinit var writerUid : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_edit)

        titleArea = findViewById(R.id.titleArea)
        contentArea = findViewById(R.id.contentArea)

        key = intent.getStringExtra("key").toString() // 게시글의 id
        getCommunityData(key)
        getImageData(key)

        /* auth = FirebaseAuth.getInstance()
        val currentDataTime = Calendar.getInstance().time
        dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentDataTime) */

        val editBtn = findViewById<Button>(R.id.editBtn)
        editBtn.setOnClickListener {
            editCommunityData(key)
        }
    }

    private fun getCommunityData(key : String) { // 게시물 내용을 화면에 적용

        val postListener = object : ValueEventListener { // 파이어베이스 안의 값이 변화할 시 작동됨
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dataModel = dataSnapshot.getValue(CommunityModel::class.java)

                titleArea.text = dataModel!!.title
                contentArea.text = dataModel!!.content
                writerUid = dataModel!!.uid
                dateFormat = dataModel!!.time
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.communityRef.child(key).addValueEventListener(postListener)
    }

    private fun getImageData(key : String) { // 이미지 불러와서 게시글에 띄우는 함수

        val storageReference = Firebase.storage.reference.child("community/$key.png")

        val imageView = findViewById<ImageView>(R.id.imageArea)

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if(task.isSuccessful) {
                Glide.with(this).load(task.result).into(imageView)
            } else {

            }
        })
    }

    private fun editCommunityData(key : String) { // 수정한 게시글 적용하는 함수
        FBRef.communityRef.child(key).setValue(CommunityModel(titleArea.text.toString(), contentArea.text.toString(), writerUid, dateFormat)) // 게시물 정보 데이터베이스에 저장
        Toast.makeText(this, "게시글 수정 완료", Toast.LENGTH_LONG).show()
        finish() // 창 닫기
    }
}