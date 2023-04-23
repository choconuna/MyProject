package org.techtown.myproject.chat

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView
import org.techtown.myproject.R
import org.techtown.myproject.utils.ChatConnection
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.MessageModel
import java.text.SimpleDateFormat
import java.util.*

class ChatInActivity : AppCompatActivity() {

    private val TAG = ChatInActivity::class.java.simpleName

    private lateinit var chatConnectionId : String

    lateinit var myUid : String
    lateinit var yourUid : String

    lateinit var yourProfile : CircleImageView
    private lateinit var yourNickNameArea : TextView

    lateinit var messageContentListView : ListView
    private val messageDataList = mutableListOf<MessageModel>()
    private lateinit var messageRVAdapter : MessageRVAdapter

    lateinit var contentArea : EditText
    private lateinit var sendBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_in)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        yourUid = intent.getStringExtra("yourUid").toString() // 채팅할 상대의 uid
        chatConnectionId = intent.getStringExtra("chatConnectionId").toString()

        setData()

        sendBtn.setOnClickListener {
            val content = contentArea.text.toString().trim()

            Toast.makeText(this, content, Toast.LENGTH_SHORT).show()

            val any = if (content.isNotEmpty()) {
                val currentDataTime = Calendar.getInstance().time
                val dateFormat =
                    SimpleDateFormat("yyyy.MM.dd.E HH:mm", Locale.KOREA).format(currentDataTime)

                val key = FBRef.messageRef.child(chatConnectionId).push().key.toString() // 키 값을 먼저 받아옴
                FBRef.messageRef.child(chatConnectionId).child(key).setValue(MessageModel(chatConnectionId, key, myUid, content, dateFormat.toString(), "false"))

                contentArea.setText("")

                val mInputMethodManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                mInputMethodManager.hideSoftInputFromWindow(contentArea.windowToken, 0)

            } else {
                Toast.makeText(this, "메시지 내용을 입력하세요!", Toast.LENGTH_SHORT).show()
            }
        }

        getMessages()

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setData() {
        yourProfile = findViewById(R.id.yourProfile)
        yourNickNameArea = findViewById(R.id.yourNickNameArea)

        messageContentListView = findViewById(R.id.messageContentListView)
        messageRVAdapter = MessageRVAdapter(messageDataList)
        messageContentListView.adapter = messageRVAdapter

        contentArea = findViewById(R.id.contentArea)
        sendBtn = findViewById(R.id.sendBtn)

        val profileFile = FBRef.userRef.child(yourUid).child("profileImage").get().addOnSuccessListener {
            val storageReference = Firebase.storage.reference.child(it.value.toString()) // 유저의 profile 사진을 DB의 storage로부터 가져옴

            storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                if(task.isSuccessful) {
                    Glide.with(applicationContext).load(task.result).thumbnail(Glide.with(applicationContext).load(task.result)).into(yourProfile) // 유저의 profile 사진을 작성자 이름의 왼편에 표시함
                } else {
                    yourProfile.isVisible = false
                }
            })
        }

        val userName = FBRef.userRef.child(yourUid).child("nickName").get().addOnSuccessListener {
            yourNickNameArea!!.text = it.value.toString() // 게시글에 작성자의 닉네임 표시
        }
    }

    private fun getMessages() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {

                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(ChatConnection::class.java)

                        // 이미 글 작성자와 채팅 이력이 존재한다면
                        if((item!!.userId1 == myUid || item!!.userId2 == yourUid) || (item!!.userId1 == yourUid && item!!.userId2 == myUid)) {
                            val postListener = object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    try {

                                        messageDataList.clear()

                                        for(dataModel in dataSnapshot.children) {
                                            val item = dataModel.getValue(MessageModel::class.java)

                                            if(item!!.sendUid != myUid) { // 메시지를 보낸 사람과 내가 다른 사람일 경우
                                                Log.d("isShown", item!!.sendUid + " " + myUid)
                                                Log.d("isShown", item!!.sendUid + " " + myUid + " " + item!!.content)
                                                val updateData = HashMap<String, Any>()
                                                updateData["shown"] = "true"

                                                FirebaseDatabase.getInstance().getReference("message").child(item!!.chatConnectionId).child(item!!.messageId).updateChildren(updateData)
                                                    .addOnSuccessListener {}
                                                    .addOnFailureListener {}
                                            }

                                            messageDataList.add(item!!)
                                        }

                                        for(i in 0 until messageDataList.size)
                                            Log.d("messageList", messageDataList[i].sendUid + " " + messageDataList[i].content)
                                        messageRVAdapter.notifyDataSetChanged()

                                        messageContentListView.setSelection(messageDataList.size - 1) //스크롤 위치를 listView의 마지막으로 이동

                                    } catch(e : Exception) { }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Geting Post failed, log a message
                                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                                }
                            }
                            FBRef.messageRef.child(item!!.chatConnectionId).addValueEventListener(postListener)
                        }
                    }

                } catch(e : Exception) { }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Geting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.chatConnectionRef.addValueEventListener(postListener)
    }
}