package org.techtown.myproject.community

import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.comment.CommentListVAdapter
import org.techtown.myproject.comment.CommentModel
import org.techtown.myproject.utils.FBRef
import java.text.SimpleDateFormat
import java.util.*


class CommunityInActivity : AppCompatActivity() {

    private val TAG = CommunityInActivity::class.java.simpleName

    lateinit var titleArea : TextView
    lateinit var timeArea : TextView
    lateinit var contentArea : TextView
    lateinit var communitySetIcon : ImageView
    lateinit var commentArea : EditText
    private lateinit var key : String

    lateinit var commentListView : ListView
    private val commentDataList = mutableListOf<CommentModel>() // 각 게시물 내의 댓글을 넣는 리스트
    lateinit var commentRVAdapter : CommentListVAdapter

    private lateinit var myUid : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_in)

        titleArea = findViewById(R.id.titleArea)
        contentArea = findViewById(R.id.contentArea)
        timeArea = findViewById(R.id.timeArea)

        commentArea = findViewById(R.id.commentArea)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        communitySetIcon = findViewById(R.id.communitySetIcon)
        communitySetIcon.setOnClickListener {
            showDialog()
        }

        key = intent.getStringExtra("key").toString()
        getCommunityData(key)
        getImageData(key)

        findViewById<ImageView>(R.id.sendBtn).setOnClickListener { // 댓글 입력 버튼 클릭 시
            insertComment(key)
        }

        commentRVAdapter = CommentListVAdapter(commentDataList)
        commentListView = findViewById(R.id.commentListView)
        commentListView.adapter = commentRVAdapter // 어댑터 연결

        getCommentData(key)
    }

    private fun showDialog() { // 게시글 수정/삭제를 위한 다이얼로그 띄우기
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)

        val alertDialog = mBuilder.show()
        val editBtn = alertDialog.findViewById<Button>(R.id.editBtn)
        editBtn?.setOnClickListener { // 수정 버튼 클릭 시
            Log.d(TAG, "edit Button Clicked")

            val intent = Intent(this, CommunityEditActivity::class.java)
            intent.putExtra("key", key)
            startActivity(intent) // 수정 페이지로 이동

            alertDialog.dismiss()
        }

        val rmBtn = alertDialog.findViewById<Button>(R.id.rmBtn)
        rmBtn?.setOnClickListener {  // 삭제 버튼 클릭 시
            Log.d(TAG, "remove Button Clicked")
            alertDialog.dismiss()
            deleteCommentData(key)
            Firebase.storage.reference.child("community/$key.png").delete().addOnSuccessListener { // 사진 삭제
            }.addOnFailureListener {
            }
            FBRef.communityRef.child(key).removeValue() // 파이어베이스에서 해당 게시물의 키 값에 해당되는 데이터 삭제
            finish() // 삭제 완료 후 게시글 창 닫기
        }
    }

    private fun deleteCommentData(key : String) { // 게시글 내의 댓글을 삭제하는 함수
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(dataModel in dataSnapshot.children) {
                    Log.d(TAG, dataModel.toString())
                    // dataModel.key
                    val item = dataModel.getValue(CommentModel::class.java)
                    Log.d("communityId", dataModel.key+key+item!!.communityId)
                    if (key == item!!.communityId) {
                        FBRef.commentRef.child(key).child(dataModel.key!!).removeValue()
                    }
                }

                Log.d(TAG, "deleteCommentData")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.commentRef.addValueEventListener(postListener)
    }

    private fun getCommunityData(key : String) { // 게시물 내용을 화면에 적용

        val postListener = object : ValueEventListener { // 파이어베이스 안의 값이 변화할 시 작동됨
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 게시글 삭제 후 그 키 값에 해당하는 게시글이 삭제되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                    val dataModel = dataSnapshot.getValue(CommunityModel::class.java)

                    titleArea.text = dataModel!!.title
                    contentArea.text = dataModel!!.content
                    timeArea.text = dataModel!!.time

                    val writerUid = dataModel.uid

                    Log.d("writerUid", writerUid)
                    val ref = FBRef.userRef

                    val profileFile = FBRef.userRef.child(writerUid).child("profileImage").get().addOnSuccessListener {
                        val storageReference = Firebase.storage.reference.child(it.value.toString()) // 유저의 profile 사진을 DB의 storage로부터 가져옴

                        val imageView = findViewById<ImageView>(R.id.profileImageArea)

                        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                            if(task.isSuccessful) {
                                Glide.with(applicationContext).load(task.result).into(imageView) // 유저의 profile 사진을 게시자 이름의 왼편에 표시함
                            } else {
                                findViewById<ImageView>(R.id.profileImageArea).isVisible = false
                            }
                        })
                    }

                    val userName = FBRef.userRef.child(writerUid).child("userName").get().addOnSuccessListener {
                        findViewById<TextView>(R.id.nameArea)!!.text = it.value.toString() // 게시글에 작성자의 이름을 표시
                    }

                    if(myUid == writerUid) {
                        Log.d(TAG, "사용자가 쓴 글")
                        communitySetIcon.isVisible = true // 사용자가 쓴 글에서는 수정/삭제 메뉴가 보이도록
                    } else {
                        Log.d(TAG, "사용자가 쓰지 않은 글")
                    }
                } catch(e : Exception) {
                    Log.d(TAG, "게시글 삭제 완료")
                }
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
                findViewById<ImageView>(R.id.imageArea).isVisible = false
            }
        })
    }

    private fun getCommentData(key : String) { // 파이어베이스로부터 댓글 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 댓글 삭제 후 그 키 값에 해당하는 댓글이 다시 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성

                    commentDataList.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(CommentModel::class.java)
                        commentDataList.add(item!!)
                    }
                    commentRVAdapter.notifyDataSetChanged() // 데이터 동기화
                } catch (e: Exception) {
                    Log.d(TAG, "게시글 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.commentRef.child(key).addValueEventListener(postListener)
    }

    private fun insertComment(key : String) { // 댓글을 파이어베이스 DB에 삽입하는 함수
        val currentDataTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentDataTime)
        FBRef.commentRef.child(key).push().setValue(CommentModel(myUid, key, commentArea.text.toString(), dateFormat))
        commentArea.setText("")
    }
}