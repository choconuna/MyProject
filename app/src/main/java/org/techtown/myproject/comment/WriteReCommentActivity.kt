package org.techtown.myproject.comment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject
import org.techtown.myproject.BuildConfig
import org.techtown.myproject.R
import org.techtown.myproject.community.CommunityModel
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.FCMToken
import org.techtown.myproject.utils.ReCommentModel
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class WriteReCommentActivity : AppCompatActivity() {

    private val TAG = CommentEditActivity::class.java.simpleName

    private val FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send"
    private val SERVER_KEY = BuildConfig.FCM_SERVER_KEY

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

                val writerUid = FBRef.commentRef.child(communityId).child(commentId).child("uid").get().addOnSuccessListener {
                    val commentUid = it.value.toString()
                    if(it.value.toString() != userId) {
                        val userName = FBRef.userRef.child(userId).child("nickName").get().addOnSuccessListener {
                            sendPostToFCM(commentUid, it.value.toString())
                        }
                    }
                }

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

    private fun sendPostToFCM(key: String, nickName : String) { // FCM을 이용해서 댓글 작성 알림을 보내는 함수

        FBRef.tokenRef.child(key)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    for (dataModel in dataSnapshot.children) {

                        val item = dataModel.getValue(FCMToken::class.java)
                        Log.d("yourToken", item!!.toString())
                        Log.d("yourToken", item!!.fcmTokenId)
                        Log.d("yourToken", item!!.userId)
                        Log.d("yourToken", item!!.token)

                        val yourToken = item!!.token

                        Thread {
                            try {
                                val root = JSONObject()
                                val notification = JSONObject()
                                notification.put("body", "$nickName" + "님이 대댓글을 달았습니다.")
                                notification.put("title", "멍노트")
                                root.put("notification", notification)
                                root.put("to", yourToken)
                                val url = URL(FCM_MESSAGE_URL)
                                val conn: HttpURLConnection =
                                    url.openConnection() as HttpURLConnection
                                conn.requestMethod = "POST"
                                conn.doOutput = true
                                conn.doInput = true
                                conn.addRequestProperty(
                                    "Authorization",
                                    "key=$SERVER_KEY"
                                )
                                conn.setRequestProperty(
                                    "Accept",
                                    "application/json"
                                )
                                conn.setRequestProperty(
                                    "Content-type",
                                    "application/json"
                                )
                                val os: OutputStream = conn.outputStream
                                os.write(
                                    root.toString().toByteArray(charset("utf-8"))
                                )
                                os.flush()
                                conn.responseCode
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                        }.start()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("yourToken", "Failed to read value.", error.toException())
                }
            })
    }
}