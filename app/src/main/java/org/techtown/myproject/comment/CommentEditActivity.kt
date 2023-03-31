package org.techtown.myproject.comment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.community.CommunityModel
import org.techtown.myproject.utils.FBRef

class CommentEditActivity : AppCompatActivity() {

    private val TAG = CommentEditActivity::class.java.simpleName

    private lateinit var communityId : String
    private lateinit var commentId : String
    private lateinit var contentArea : EditText

    private lateinit var userId : String
    private lateinit var commentTime : String
    private lateinit var count : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment_edit)

        contentArea = findViewById(R.id.contentArea)

        communityId = intent.getStringExtra("communityId").toString() // 커뮤니티 id
        commentId = intent.getStringExtra("commentId").toString() // 댓글 id

        getCommentContent(communityId, commentId)

        val editBtn = findViewById<TextView>(R.id.editBtn)
        editBtn.setOnClickListener {
            editComment(communityId, commentId)
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun getCommentContent(communityId : String, commentId : String) {
        val postListener = object : ValueEventListener { // 파이어베이스 안의 값이 변화할 시 작동됨
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dataModel = dataSnapshot.getValue(CommentModel::class.java)

                userId = dataModel!!.uid
                commentTime = dataModel!!.commentTime
                count = dataModel!!.count

                contentArea.setText(dataModel!!.commentContent)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.commentRef.child(communityId).child(commentId).addValueEventListener(postListener)
    }

    private fun editComment(communityId : String, commentId : String) {
        FBRef.commentRef.child(communityId).child(commentId).setValue(CommentModel(userId, communityId, commentId, contentArea.text.toString(), commentTime, count)) // 댓글 데이터 데이터베이스에 저장
        Toast.makeText(this, "댓글이 수정되었습니다!", Toast.LENGTH_LONG).show()
        finish() // 창 닫기
    }
}