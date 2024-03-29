package org.techtown.myproject.chat

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import org.techtown.myproject.BuildConfig
import org.techtown.myproject.R
import org.techtown.myproject.utils.*
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class ChatInActivity : AppCompatActivity() {

    private val TAG = ChatInActivity::class.java.simpleName

    private val FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send"

    private lateinit var chatConnectionId : String
    private lateinit var sharedPreferences: SharedPreferences

    lateinit var myUid : String
    lateinit var yourUid : String

    lateinit var yourProfile : CircleImageView
    private lateinit var yourNickNameArea : TextView

    private lateinit var profileArea : LinearLayout

    lateinit var messageContentListView : ListView
    private val messageDataList = mutableListOf<MessageModel>()
    private lateinit var messageRVAdapter : MessageRVAdapter

    var imageList : ArrayList<Uri> = ArrayList()
    private lateinit var plusImageBtn : Button
    private var count = 0 // 첨부한 사진 수

    lateinit var contentArea : EditText
    private lateinit var sendBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_in)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        yourUid = intent.getStringExtra("yourUid").toString() // 채팅할 상대의 uid
        chatConnectionId = intent.getStringExtra("chatConnectionId").toString()

        setData()

        profileArea.setOnClickListener {

            FBRef.userRef.child(yourUid).child("nickName").get().addOnSuccessListener {
                var userName = it.value.toString() // 게시글에 작성자의 닉네임을 가져옴

                val mDialogView = LayoutInflater.from(this@ChatInActivity).inflate(R.layout.block_dialog, null)
                val mBuilder = AlertDialog.Builder(this@ChatInActivity).setView(mDialogView)

                val alertDialog = mBuilder.show()

                Log.d("getChatProfile", "$yourUid $userName")

                val userNameArea = alertDialog.findViewById<TextView>(R.id.userNameArea)
                userNameArea!!.text = userName

                val yesBtn = alertDialog.findViewById<Button>(R.id.yesBtn)
                yesBtn?.setOnClickListener { // 예 버튼 클릭 시
                    Log.d(TAG, "yes Button Clicked")

                    val key = FBRef.blockRef.child(myUid).push().key.toString() // 키 값을 먼저 받아옴
                    FBRef.blockRef.child(myUid).child(key).setValue(BlockModel(key, myUid, yourUid)) // 차단 데이터 생성

                    alertDialog.dismiss() // 다이얼로그 창 닫기
                    finish() // 창 닫기
                }

                val noBtn = alertDialog.findViewById<Button>(R.id.noBtn)
                noBtn?.setOnClickListener {  // 아니오 버튼 클릭 시
                    Log.d(TAG, "no Button Clicked")

                    alertDialog.dismiss() // 다이얼로그 창 닫기
                }
            }
        }

        plusImageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            activityResult.launch(intent)
        }

        sendBtn.setOnClickListener {
            val content = contentArea.text.toString().trim()

            val any = if (content.isNotEmpty()) {
                val currentDataTime = Calendar.getInstance().time
                val dateFormat = SimpleDateFormat("yyyy.MM.dd.E HH:mm", Locale.KOREA).format(currentDataTime)

                val key = FBRef.messageRef.child(chatConnectionId).push().key.toString() // 키 값을 먼저 받아옴
                FBRef.messageRef.child(chatConnectionId).child(key).setValue(MessageModel(chatConnectionId, key, myUid, "letter", 0.toString(), content, dateFormat.toString(), "false"))
                FBRef.chatConnectionRef.child(chatConnectionId).child("lastTime").setValue(System.currentTimeMillis().toString())

                contentArea.setText("")
                val mInputMethodManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                mInputMethodManager.hideSoftInputFromWindow(contentArea.windowToken, 0)

                val userName = FBRef.userRef.child(myUid).child("nickName").get().addOnSuccessListener {
                    sendPostToFCM(chatConnectionId, it.value.toString(), content)
                }

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

        profileArea = findViewById(R.id.profileArea)

        messageContentListView = findViewById(R.id.messageContentListView)
        messageRVAdapter = MessageRVAdapter(messageDataList)
        messageContentListView.adapter = messageRVAdapter

        plusImageBtn = findViewById(R.id.plusImageBtn)
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

    private fun sendPostToFCM(chatConnectionId: String, nickName : String, message: String) { // FCM을 이용해서 상대방에게 채팅 전송 알림을 보내는 함수

        Log.d("sendPostToFCM", "$chatConnectionId $nickName $message")

        FBRef.chatConnectionRef
            .child(chatConnectionId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val chatConnection = dataSnapshot.getValue(ChatConnection::class.java)
                    var yourUid = ""
                    if (chatConnection!!.userId1 != myUid)
                        yourUid = chatConnection!!.userId1
                    else if (chatConnection!!.userId2 != myUid)
                        yourUid = chatConnection!!.userId2

                    Log.d("yourToken", yourUid)

                    FBRef.tokenRef.child(yourUid).addValueEventListener(object : ValueEventListener {
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
                                        notification.put("body", "$nickName: $message")
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
                                            "key=${BuildConfig.FCM_SERVER_KEY}"
                                        )
                                        conn.setRequestProperty("Accept", "application/json")
                                        conn.setRequestProperty(
                                            "Content-type",
                                            "application/json"
                                        )
                                        val os: OutputStream = conn.outputStream
                                        os.write(root.toString().toByteArray(charset("utf-8")))
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

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun getMessages() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {

                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(ChatConnection::class.java)

                        // 이미 글 작성자와 채팅 이력이 존재한다면
                        if((item!!.userId1 == myUid && item!!.userId2 == yourUid) || (item!!.userId1 == yourUid && item!!.userId2 == myUid)) {
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


                                            Log.d("getMessage", item!!.toString())
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

    private fun imageUpload(chatConnectionId: String, key : String) { // 이미지를 storage에 업로드하는 함수
        val storage = Firebase.storage
        val storageRef = storage.reference

        for(cnt in 0 until count) {
            val mountainsRef = storageRef.child("messageImage/$chatConnectionId/$key/$key$cnt.png")

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
            Log.d("imageList", imageList.toString())

            if(count > 0) {

                val currentDataTime = Calendar.getInstance().time
                val dateFormat = SimpleDateFormat("yyyy.MM.dd.E HH:mm", Locale.KOREA).format(currentDataTime)

                val key = FBRef.messageRef.child(chatConnectionId).push().key.toString() // 키 값을 먼저 받아옴
                FBRef.messageRef.child(chatConnectionId).child(key).setValue(MessageModel(chatConnectionId, key, myUid, "picture", count.toString(), "", dateFormat.toString(), "false"))

                imageUpload(chatConnectionId, key) // 이미지를 firebase storage에 업로드

                val userName = FBRef.userRef.child(myUid).child("nickName").get().addOnSuccessListener {
                    sendPostToFCM(chatConnectionId, it.value.toString(), "사진을 보냈습니다.")
                }
            }
        }
    }
}