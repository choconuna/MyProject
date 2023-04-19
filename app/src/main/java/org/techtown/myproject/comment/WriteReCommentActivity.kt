package org.techtown.myproject.comment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.ReCommentModel
import java.text.SimpleDateFormat
import java.util.*

class WriteReCommentActivity : AppCompatActivity() {

    private val TAG = CommentEditActivity::class.java.simpleName

    private lateinit var communityId : String
    private lateinit var commentId : String

    private lateinit var reCommentArea : EditText

    private lateinit var userId : String
    private lateinit var reCommentTime : String
    private lateinit var count : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_re_comment)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        reCommentArea = findViewById(R.id.reCommentArea)

        communityId = intent.getStringExtra("communityId").toString() // 커뮤니티 id
        commentId = intent.getStringExtra("commentId").toString() // 대댓글을 달 댓글 id


        val saveBtn = findViewById<TextView>(R.id.saveBtn)
        saveBtn.setOnClickListener {
            if(reCommentArea.text.toString().trim().isNotEmpty()) {

                val currentDataTime = Calendar.getInstance().time
                val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentDataTime)
                val reCommentKey = FBRef.reCommentRef.child(communityId).child(commentId).push().key.toString() // 대댓글의 키 값을 먼저 받아옴
                FBRef.reCommentRef.child(communityId).child(commentId).child(reCommentKey).setValue(ReCommentModel(userId, communityId, commentId, reCommentKey, reCommentArea.text.toString(), dateFormat))

                finish()
            } else {
                Toast.makeText(this, "대댓글 내용을 작성하세요!", Toast.LENGTH_SHORT).show()
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }
}