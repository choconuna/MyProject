package org.techtown.myproject.deal

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import org.json.JSONObject
import org.techtown.myproject.R
import org.techtown.myproject.chat.ChatInActivity
import org.techtown.myproject.chat.MessageRVAdapter
import org.techtown.myproject.utils.*
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class DealChatInActivity : AppCompatActivity() {

    private val TAG = ChatInActivity::class.java.simpleName

    private val FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send"
    private val SERVER_KEY = "AAAA3urRte0:APA91bElmJzeARg8yvRN-8-ABV4xZLaHdD2p6wmFzNjTLofP65CWNmvxT0TZ8cfxOLio4XSlJkLgrcLBfL44xUcLYHeTQFHoFTa0qP5G5kf84WvvvjPuHOZ2H7QY_y1Yc23P4gn8CNWk"

    private lateinit var chatConnectionId : String
    private lateinit var sharedPreferences: SharedPreferences

    lateinit var myUid : String
    lateinit var yourUid : String
    lateinit var dealId : String

    lateinit var yourProfile : CircleImageView
    private lateinit var yourNickNameArea : TextView

    lateinit var messageContentListView : ListView
    private val messageDataList = mutableListOf<DealMessageModel>()
    private lateinit var messageRVAdapter : DealMessageReVAdapter

    private lateinit var itemImageArea : ImageView
    private lateinit var stateSpinner: Spinner
    private lateinit var state : String
    private lateinit var priceArea : TextView
    private lateinit var titleArea : TextView

    var imageList : ArrayList<Uri> = ArrayList()
    private lateinit var plusImageBtn : Button
    private var count = 0 // 첨부한 사진 수

    lateinit var contentArea : EditText
    private lateinit var sendBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deal_chat_in)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        yourUid = intent.getStringExtra("yourUid").toString() // 채팅할 상대의 uid
        chatConnectionId = intent.getStringExtra("chatConnectionId").toString()
        dealId = intent.getStringExtra("dealId").toString()

        setData()

        plusImageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            activityResult.launch(intent)
        }

        stateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val originState = FBRef.dealRef.child(dealId).child("state").get().addOnSuccessListener {
                    if(it.value.toString() != parent.getItemAtPosition(position).toString()) {
                        state = parent.getItemAtPosition(position).toString()
                        changeDealState(state)
                        if(state.last() == '중')
                            Toast.makeText(applicationContext, "거래 상태 정보를 " + state + "으로 변경했습니다!", Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(applicationContext, "거래 상태 정보를 " + state + "로 변경했습니다!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        sendBtn.setOnClickListener {
            val content = contentArea.text.toString().trim()

            val any = if (content.isNotEmpty()) {
                val currentDataTime = Calendar.getInstance().time
                val dateFormat = SimpleDateFormat("yyyy.MM.dd.E HH:mm", Locale.KOREA).format(currentDataTime)

                val key = FBRef.dealMessageRef.child(dealId).child(chatConnectionId).push().key.toString() // 키 값을 먼저 받아옴
                FBRef.dealMessageRef.child(dealId).child(chatConnectionId).child(key).setValue(DealMessageModel(dealId, chatConnectionId, key, myUid, "letter", 0.toString(), content, dateFormat.toString(), "false"))

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

        itemImageArea = findViewById(R.id.itemImageArea)
        stateSpinner = findViewById(R.id.stateSpinner)
        priceArea = findViewById(R.id.priceArea)
        titleArea = findViewById(R.id.titleArea)

        messageContentListView = findViewById(R.id.messageContentListView)
        messageRVAdapter = DealMessageReVAdapter(messageDataList)
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

        val imgCnt = FBRef.dealRef.child(dealId).child("imgCnt").get().addOnSuccessListener {
            if(it.value.toString().toInt() >= 1) {

                val storageReference = Firebase.storage.reference.child("dealImage/$dealId/$dealId"+"0.png") // 커뮤니티에 첨부된 사진을 DB의 storage로부터 가져옴 -> 첨부된 사진이 여러 장이라면, 제일 첫 번째 사진을 대표 사진으로 띄움

                storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                    if(task.isSuccessful) {
                        Glide.with(this).load(task.result).into(itemImageArea) // 게시글에 첫 번째 이미지를 표시함
                    } else {
                        itemImageArea.visibility = View.GONE
                    }
                })
            }
        }

        val itemTitle = FBRef.dealRef.child(dealId).child("title").get().addOnSuccessListener {
            titleArea.text = it.value.toString()
        }

        val itemPrice = FBRef.dealRef.child(dealId).child("price").get().addOnSuccessListener {
            val decimalFormat = DecimalFormat("#,###")
            priceArea.text = decimalFormat.format(it.value.toString().replace(",","").toDouble()) + "원"
        }

        val itemState = FBRef.dealRef.child(dealId).child("state").get().addOnSuccessListener {
            for (i in 0 until stateSpinner.count) {
                if (stateSpinner.getItemAtPosition(i).toString() == it.value.toString()) {
                    stateSpinner.setSelection(i)
                    break
                }
            }
        }
    }

    private fun getMessages() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {

                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DealChatConnection::class.java)

                        // 이미 글 작성자와 채팅 이력이 존재한다면
                        if((item!!.userId1 == myUid || item!!.userId2 == yourUid) || (item!!.userId1 == yourUid && item!!.userId2 == myUid)) {
                            val postListener = object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    try {

                                        messageDataList.clear()

                                        for(dataModel in dataSnapshot.children) {
                                            val item = dataModel.getValue(DealMessageModel::class.java)

                                            if(item!!.sendUid != myUid) { // 메시지를 보낸 사람과 내가 다른 사람일 경우
                                                Log.d("isShown", item!!.sendUid + " " + myUid)
                                                Log.d("isShown", item!!.sendUid + " " + myUid + " " + item!!.content)
                                                val updateData = HashMap<String, Any>()
                                                updateData["shown"] = "true"

                                                FirebaseDatabase.getInstance().getReference("dealMessage").child(dealId).child(item!!.chatConnectionId).child(item!!.messageId).updateChildren(updateData)
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
                            FBRef.dealMessageRef.child(item!!.dealId).child(item!!.chatConnectionId).addValueEventListener(postListener)
                        }
                    }

                } catch(e : Exception) { }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Geting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dealChatConnectionRef.child(dealId).addValueEventListener(postListener)
    }

    private fun sendPostToFCM(chatConnectionId: String, nickName : String, message: String) { // FCM을 이용해서 상대방에게 채팅 전송 알림을 보내는 함수

        Log.d("sendPostToFCM", "$chatConnectionId $nickName $message")

        FBRef.dealChatConnectionRef.child(dealId)
            .child(chatConnectionId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val chatConnection = dataSnapshot.getValue(DealChatConnection::class.java)
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
                                            "key=$SERVER_KEY"
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

    private fun changeDealState(state : String) { // 거래 상품의 거래 상태를 바꿈
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 거래글 삭제 후 그 키 값에 해당하는 게시글이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                    val dataModel = dataSnapshot.getValue(DealModel::class.java)

                    FBRef.dealRef.child(dataModel!!.dealId).setValue(DealModel(dataModel!!.dealId, dataModel!!.sellerId, dataModel!!.location, dataModel!!.category, dataModel!!.price, dataModel!!.title, dataModel!!.content, dataModel!!.imgCnt, dataModel!!.method, state, dataModel!!.date)) // 게시물 정보 데이터베이스에 저장
                } catch(e : Exception) { }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dealRef.child(dealId).addValueEventListener(postListener)
    }

    private fun imageUpload(chatConnectionId: String, key : String) { // 이미지를 storage에 업로드하는 함수
        val storage = Firebase.storage
        val storageRef = storage.reference

        for(cnt in 0 until count) {
            val mountainsRef = storageRef.child("dealMessageImage/$chatConnectionId/$key/$key$cnt.png")

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

                val key = FBRef.dealMessageRef.child(dealId).child(chatConnectionId).push().key.toString() // 키 값을 먼저 받아옴
                FBRef.dealMessageRef.child(dealId).child(chatConnectionId).child(key).setValue(DealMessageModel(dealId, chatConnectionId, key, myUid, "picture", count.toString(), "", dateFormat.toString(), "false"))

                imageUpload(chatConnectionId, key) // 이미지를 firebase storage에 업로드

                val userName = FBRef.userRef.child(myUid).child("nickName").get().addOnSuccessListener {
                    sendPostToFCM(chatConnectionId, it.value.toString(), "사진을 보냈습니다.")
                }
            }
        }
    }
}