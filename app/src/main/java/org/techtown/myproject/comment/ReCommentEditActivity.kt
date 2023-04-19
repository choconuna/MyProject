package org.techtown.myproject.comment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.ReCommentModel

class ReCommentEditActivity : AppCompatActivity() {

    private val TAG = ReCommentEditActivity::class.java.simpleName

    private lateinit var communityId : String
    private lateinit var commentId : String
    private lateinit var reCommentId : String
    private lateinit var contentArea : EditText

    private lateinit var userId : String
    private lateinit var reCommentTime : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_re_comment_edit)

        contentArea = findViewById(R.id.contentArea)

        communityId = intent.getStringExtra("communityId").toString() // 커뮤니티 id
        commentId = intent.getStringExtra("commentId").toString() // 댓글 id
        reCommentId = intent.getStringExtra("reCommentId").toString() // 대댓글 id

        getReCommentContent(communityId, commentId, reCommentId)

        val editBtn = findViewById<TextView>(R.id.editBtn)
        editBtn.setOnClickListener {
            editReComment(communityId, commentId, reCommentId)
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun getReCommentContent(communityId : String, commentId : String, reCommentId : String) {
        val postListener = object : ValueEventListener { // 파이어베이스 안의 값이 변화할 시 작동됨
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val dataModel = dataSnapshot.getValue(ReCommentModel::class.java)

                    userId = dataModel!!.uid
                    reCommentTime = dataModel!!.reCommentTime

                    contentArea.setText(dataModel!!.reCommentContent)
                } catch(e : Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.reCommentRef.child(communityId).child(commentId).child(reCommentId).addValueEventListener(postListener)
    }

    private fun editReComment(communityId : String, commentId : String, reCommentId : String) {
        FBRef.reCommentRef.child(communityId).child(commentId).child(reCommentId).setValue(
            ReCommentModel(userId, communityId, commentId, reCommentId, contentArea.text.toString(), reCommentTime)
        ) // 댓글 데이터 데이터베이스에 저장
        Toast.makeText(this, "대댓글이 수정되었습니다!", Toast.LENGTH_LONG).show()
        finish() // 창 닫기
    }
}