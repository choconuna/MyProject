package org.techtown.myproject.community

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.chat.ChatInActivity
import org.techtown.myproject.comment.CommentModel
import org.techtown.myproject.comment.CommentReVAdapter
import org.techtown.myproject.utils.ChatConnection
import org.techtown.myproject.utils.FBRef
import java.text.SimpleDateFormat
import java.util.*


class SpecificCommunityInActivity : AppCompatActivity() {

    private val TAG = SpecificCommunityInActivity::class.java.simpleName

    lateinit var categoryArea : TextView
    lateinit var category : String
    lateinit var titleArea : TextView
    lateinit var timeArea : TextView
    lateinit var contentArea : TextView
    lateinit var communitySetIcon : ImageView
    lateinit var commentArea : EditText
    private lateinit var key : String

    private lateinit var writerArea : LinearLayout

    lateinit var imageListView : RecyclerView
    private val imageDataList = ArrayList<String>() // 게시글의 사진을 넣는 리스트
    lateinit var communityImageVAdapter : CommunityImageAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    lateinit var commentListView : RecyclerView
    private val commentDataList = ArrayList<CommentModel>() // 각 게시물 내의 댓글을 넣는 리스트
    lateinit var commentRVAdapter : CommentReVAdapter
    lateinit var cLayoutManager : RecyclerView.LayoutManager

    private lateinit var myUid : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specific_community_in)

        categoryArea = findViewById(R.id.categoryArea)
        titleArea = findViewById(R.id.titleArea)
        contentArea = findViewById(R.id.contentArea)
        timeArea = findViewById(R.id.timeArea)

        writerArea = findViewById(R.id.writerArea)

        commentArea = findViewById(R.id.commentArea)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        communityImageVAdapter = CommunityImageAdapter(imageDataList)
        imageListView = findViewById(R.id.imageListView)
        imageListView.setWillNotDraw(false)
        imageListView.setItemViewCacheSize(20)
        imageListView.isDrawingCacheEnabled = true
        imageListView.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        imageListView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        imageListView.layoutManager = layoutManager
        imageListView.adapter = communityImageVAdapter

        communityImageVAdapter.setItemClickListener(object: CommunityImageAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                val intent = Intent(applicationContext, CommunityImageDetailActivity::class.java)
                intent.putExtra("image", imageDataList[position]) // 사진 링크 넘기기
                startActivity(intent)
            }
        })

        communitySetIcon = findViewById(R.id.communitySet)
        communitySetIcon.setOnClickListener {
            showDialog()
        }

        category = intent.getStringExtra("category").toString() // 게시글의 카테고리 받아옴
        key = intent.getStringExtra("key").toString() // 게시글의 key 값을 받아옴
        getCommunityData(key)

        writerArea.setOnClickListener { // 작성자의 프로필 사진이나 닉네임 부분 클릭 시
            val postListener = object : ValueEventListener { // 파이어베이스 안의 값이 변화할 시 작동됨
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try { // 게시글 삭제 후 그 키 값에 해당하는 게시글이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                        val dataModel = dataSnapshot.getValue(CommunityModel::class.java)

                        val writerUid = dataModel!!.uid // 작성자 key id
                        var userName = ""

                        FBRef.userRef.child(writerUid).child("nickName").get()
                            .addOnSuccessListener {
                                userName = it.value.toString() // 게시글에 작성자의 닉네임을 가져옴

                                if (myUid != writerUid) { // 작성자의 uid가 현재 사용자의 uid와 같지 않을 경우
                                    val mDialogView = LayoutInflater.from(this@SpecificCommunityInActivity).inflate(R.layout.chatting_dialog, null)
                                    val mBuilder = AlertDialog.Builder(this@SpecificCommunityInActivity).setView(mDialogView)

                                    val alertDialog = mBuilder.show()

                                    Log.d("getChat", "$writerUid $userName")

                                    val userNameArea = alertDialog.findViewById<TextView>(R.id.userNameArea)
                                    userNameArea!!.text = userName

                                    val yesBtn = alertDialog.findViewById<Button>(R.id.yesBtn)
                                    yesBtn?.setOnClickListener { // 예 버튼 클릭 시
                                        Log.d(TAG, "yes Button Clicked")

                                        val postListener = object : ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                try {
                                                    var isExist = false
                                                    for(dataModel in dataSnapshot.children) {
                                                        Log.d(TAG, dataModel.toString())
                                                        val item = dataModel.getValue(ChatConnection::class.java)

                                                        Log.d("writerUid", "$writerUid $myUid")

                                                        // 이미 글 작성자와 채팅 이력이 존재한다면
                                                        if((item!!.userId1 == myUid && item!!.userId2 == writerUid) || (item!!.userId1 == writerUid && item!!.userId2 == myUid)) {
                                                            isExist = true
                                                            Log.d("writerUid", "isExist")
                                                            val intent = Intent(applicationContext, ChatInActivity::class.java) // 글 작성자와의 기존 채팅방으로 이동
                                                            intent.putExtra("chatConnectionId", item!!.chatConnectionId)
                                                            intent.putExtra("yourUid", writerUid)
                                                            startActivity(intent)
                                                        }
                                                    }
                                                    if(!isExist) {

                                                        Log.d("writerUid", "isNotExist")

                                                        val key = FBRef.chatConnectionRef.push().key.toString() // 키 값을 먼저 받아옴
                                                        FBRef.chatConnectionRef.child(key).setValue(ChatConnection(key, myUid, writerUid)) // 채팅 커넥션 데이터 생성

                                                        val intent = Intent(applicationContext, ChatInActivity::class.java)
                                                        intent.putExtra("chatConnectionId", key)
                                                        intent.putExtra("yourUid", writerUid)
                                                        startActivity(intent)
                                                    }
                                                } catch(e : Exception) { }
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {
                                                // Getting Post failed, log a message
                                                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                                            }
                                        }
                                        FBRef.chatConnectionRef.addValueEventListener(postListener)

                                        alertDialog.dismiss()
                                    }

                                    val noBtn = alertDialog.findViewById<Button>(R.id.noBtn)
                                    noBtn?.setOnClickListener {  // 아니오 버튼 클릭 시
                                        Log.d(TAG, "no Button Clicked")

                                        alertDialog.dismiss() // 다이얼로그 창 닫기
                                    }
                                }
                            }
                    } catch (e : Exception) {
                        Log.d(TAG, "게시글 삭제 완료")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                }
            }
            FBRef.communityRef.child(category).child(key).addValueEventListener(postListener)
        }

        // getImageData(key)

        findViewById<ImageView>(R.id.sendBtn).setOnClickListener { // 댓글 입력 버튼 클릭 시
            insertComment(key)

            val mInputMethodManager: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            mInputMethodManager.hideSoftInputFromWindow(commentArea.windowToken, 0)
        }

        commentRVAdapter = CommentReVAdapter(commentDataList)
        commentListView = findViewById(R.id.commentListView)
        commentListView.setWillNotDraw(false)
        commentListView.setItemViewCacheSize(20)
        commentListView.isDrawingCacheEnabled = true
        commentListView.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        commentListView.setHasFixedSize(true)
        cLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        commentListView.layoutManager = cLayoutManager
        commentListView.adapter = commentRVAdapter // 어댑터 연결

        getCommentData(key)

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun showDialog() { // 게시글 수정/삭제를 위한 다이얼로그 띄우기
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)

        val alertDialog = mBuilder.show()
        val editBtn = alertDialog.findViewById<Button>(R.id.editBtn)
        editBtn?.setOnClickListener { // 수정 버튼 클릭 시
            Log.d(TAG, "edit Button Clicked")

            val intent = Intent(this, SpecificCommunityEditActivity::class.java)
            intent.putExtra("category", category)
            intent.putExtra("key", key)
            startActivity(intent) // 수정 페이지로 이동

            alertDialog.dismiss()
        }

        val rmBtn = alertDialog.findViewById<Button>(R.id.rmBtn)
        rmBtn?.setOnClickListener {  // 삭제 버튼 클릭 시
            Log.d(TAG, "remove Button Clicked")
            alertDialog.dismiss()
            deleteCommentData(key)
            deleteCommunityImage(key)
            FBRef.communityRef.child(category).child(key).removeValue() // 파이어베이스에서 해당 게시물의 키 값에 해당되는 데이터 삭제
            finish() // 삭제 완료 후 게시글 창 닫기
        }
    }

    private fun deleteCommunityImage(key : String) {
        val postListener = object : ValueEventListener { // 파이어베이스 안의 값이 변화할 시 작동됨
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 사진 삭제 후 그 키 값에 해당하는 사진이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                    val dataModel = dataSnapshot.getValue(CommunityModel::class.java)

                    if (dataModel!!.count.toInt() >= 1) {
                        for (index in 0 until dataModel.count.toInt()) {
                            Firebase.storage.reference.child("communityImage/$key/$key$index.png")
                                .delete().addOnSuccessListener { // 사진 삭제
                                }.addOnFailureListener {
                                }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "게시글 삭제 완료")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.communityRef.child(category).child(key).addValueEventListener(postListener)
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
                try { // 게시글 삭제 후 그 키 값에 해당하는 게시글이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                    val dataModel = dataSnapshot.getValue(CommunityModel::class.java)

                    categoryArea.text = dataModel!!.category
                    titleArea.text = dataModel!!.title
                    contentArea.text = dataModel!!.content
                    timeArea.text = dataModel!!.time

                    val writerUid = dataModel.uid // 작성자 key id

                    Log.d("writerUid", writerUid)

                    val profileFile = FBRef.userRef.child(writerUid).child("profileImage").get().addOnSuccessListener {
                        val storageReference = Firebase.storage.reference.child(it.value.toString()) // 유저의 profile 사진을 DB의 storage로부터 가져옴

                        val imageView = findViewById<ImageView>(R.id.profileImageArea)

                        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                            if(task.isSuccessful) {
                                Glide.with(applicationContext).load(task.result).thumbnail(Glide.with(applicationContext).load(task.result)).into(imageView) // 유저의 profile 사진을 작성자 이름의 왼편에 표시함
                            } else {
                                findViewById<ImageView>(R.id.profileImageArea).isVisible = false
                            }
                        })
                    }

                    val userName = FBRef.userRef.child(writerUid).child("nickName").get().addOnSuccessListener {
                        findViewById<TextView>(R.id.nickNameArea)!!.text = it.value.toString() // 게시글에 작성자의 아이디 표시
                    }

                    imageDataList.clear()

                    if(dataModel.count.toInt() >= 1) {
                        for(index in 0 until dataModel.count.toInt()) {
                            imageDataList.add("communityImage/$key/$key$index.png")
                        }
                        Log.d("imageDataList", imageDataList.toString())
                    }
                    communityImageVAdapter.notifyDataSetChanged()

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
        FBRef.communityRef.child(category).child(key).addValueEventListener(postListener)
    }

    private fun getCommentData(key : String) { // 파이어베이스로부터 댓글 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                commentDataList.clear()

                for(dataModel in dataSnapshot.children) {
                    val item = dataModel.getValue(CommentModel::class.java)
                    commentDataList.add(item!!)
                }

                Log.d("commentDataList", commentDataList.toString())
                commentRVAdapter.notifyDataSetChanged() // 데이터 동기화
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
        val commentKey = FBRef.commentRef.child(key).push().key.toString() // 댓글의 키 값을 먼저 받아옴
        FBRef.commentRef.child(key).child(commentKey).setValue(CommentModel(myUid, key, commentKey, commentArea.text.toString(), dateFormat, (0).toString()))
        commentArea.setText("")
    }
}