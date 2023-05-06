package org.techtown.myproject.deal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
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
import org.techtown.myproject.community.CommunityModel
import org.techtown.myproject.note.ImageDetailActivity
import org.techtown.myproject.utils.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class DealInActivity : AppCompatActivity() {

    private val TAG = DealInActivity::class.java.simpleName

    private lateinit var myUid : String
    private lateinit var dealId : String // 거래 데이터 id
    private lateinit var sellerId : String // 판매자 id

    private lateinit var sellerProfile : ImageView
    private lateinit var sellerNickNameArea : TextView
    private lateinit var communitySet : ImageView // 현재 사용자가 판매자일 경우 거래 게시글 수정/삭제를 위한 아이콘

    private lateinit var textView : LinearLayout // 판매자의 프로필 영역

    private lateinit var state : String
    private lateinit var stateSpinner : Spinner // 현재 사용자가 판매자일 경우 상품 판매 상태를 표시하기 위한 스피너

    private lateinit var imageRecyclerView : RecyclerView
    private val imageDataList = ArrayList<String>() // 게시글의 사진을 넣는 리스트
    lateinit var dealImageVAdapter : DealImageReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    private lateinit var titleArea : TextView
    private lateinit var locationArea : TextView
    private lateinit var categoryArea : TextView
    private lateinit var dateArea : TextView
    private lateinit var contentArea : TextView

    private lateinit var priceArea : TextView
    private lateinit var methodArea : TextView
    private lateinit var sellerChatBtn : Button // 현재 사용자가 판매자일 경우 거래 채팅 목록으로 넘어가기 위한 Button
    private lateinit var customerChatBtn : Button // 현재 사용자가 판매자가 아닐 경우, 채팅하기 페이지로 넘어가기 위한 Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deal_in)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        dealId = intent.getStringExtra("dealId").toString()
        sellerId = intent.getStringExtra("sellerId").toString()

        setData()

        if(myUid != sellerId)
            updateVisitors()

        getDealData()

        communitySet.setOnClickListener {
            showDialog()
        }

        textView.setOnClickListener {
            val postListener = object : ValueEventListener { // 파이어베이스 안의 값이 변화할 시 작동됨
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try { // 게시글 삭제 후 그 키 값에 해당하는 게시글이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                        val dataModel = dataSnapshot.getValue(DealModel::class.java)

                        val writerUid = dataModel!!.sellerId // 작성자 key id
                        var userName = ""

                        FBRef.userRef.child(writerUid).child("nickName").get().addOnSuccessListener {
                            userName = it.value.toString() // 게시글에 작성자의 닉네임을 가져옴

                            if (myUid != writerUid) { // 작성자의 uid가 현재 사용자의 uid와 같지 않을 경우
                                val mDialogView = LayoutInflater.from(this@DealInActivity).inflate(R.layout.block_dialog, null)
                                val mBuilder = AlertDialog.Builder(this@DealInActivity).setView(mDialogView)

                                val alertDialog = mBuilder.show()

                                Log.d("getDeal", "$writerUid $userName")

                                val userNameArea = alertDialog.findViewById<TextView>(R.id.userNameArea)
                                userNameArea!!.text = userName

                                val yesBtn = alertDialog.findViewById<Button>(R.id.yesBtn)
                                yesBtn?.setOnClickListener { // 예 버튼 클릭 시
                                    Log.d(TAG, "yes Button Clicked")

                                    val key = FBRef.blockRef.child(myUid).push().key.toString() // 키 값을 먼저 받아옴
                                    FBRef.blockRef.child(myUid).child(key).setValue(BlockModel(key, myUid, writerUid)) // 차단 데이터 생성

                                    FBRef.userRef.child(writerUid).child("nickName").get().addOnSuccessListener {
                                        Toast.makeText(applicationContext, it.value.toString() + "님이 차단되었습니다.", Toast.LENGTH_SHORT).show()

                                        alertDialog.dismiss() // 다이얼로그 창 닫기
                                        finish() // 창 닫기
                                    }
                                }

                                val noBtn = alertDialog.findViewById<Button>(R.id.noBtn)
                                noBtn?.setOnClickListener {  // 아니오 버튼 클릭 시
                                    Log.d(TAG, "no Button Clicked")

                                    alertDialog.dismiss() // 다이얼로그 창 닫기
                                }
                            }
                        }
                    } catch (e : Exception) {
                        Log.d(TAG, "거래 삭제 완료")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                }
            }
            FBRef.dealRef.child(dealId).addValueEventListener(postListener)
        }

        stateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val originState = FBRef.dealRef.child(dealId).child("state").get().addOnSuccessListener {
                    if(parent.getItemAtPosition(position).toString() == "거래 완료" && it.value.toString() != "거래 완료") {
                        state = parent.getItemAtPosition(position).toString()

                        for (i in 0 until stateSpinner.count) {
                            if (stateSpinner.getItemAtPosition(i).toString() == it.value.toString()) {
                                stateSpinner.setSelection(i)
                                break
                            }
                        }

                        changeDealState(state)
                    } else if(parent.getItemAtPosition(position).toString() == "거래 완료" && it.value.toString() == "거래 완료") {

                        for (i in 0 until stateSpinner.count) {
                            if (stateSpinner.getItemAtPosition(i).toString() == it.value.toString()) {
                                stateSpinner.setSelection(i)
                                break
                            }
                        }

                    } else if(it.value.toString() != parent.getItemAtPosition(position).toString()) {
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

        dealImageVAdapter.setItemClickListener(object: DealImageReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val intent = Intent(applicationContext, ImageDetailActivity::class.java)
                Log.d("imageDataList", imageDataList[position])
                intent.putExtra("image", imageDataList[position]) // 사진 링크 넘기기
                startActivity(intent)
            }
        })

        sellerChatBtn.setOnClickListener {

            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try { // 거래글 삭제 후 그 키 값에 해당하는 게시글이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                        if (dataSnapshot.exists()) {
                            val intent = Intent(applicationContext, SpecificChatActivity::class.java)
                            intent.putExtra("dealId", dealId)
                            startActivity(intent)
                        } else {
                            Toast.makeText(applicationContext, "채팅 기록이 없습니다!", Toast.LENGTH_SHORT).show()
                        }
                    } catch(e : Exception) {
                        Toast.makeText(applicationContext, "채팅 기록이 없습니다!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                }
            }
            FBRef.dealChatConnectionRef.child(dealId).addValueEventListener(postListener)
        }


        customerChatBtn.setOnClickListener {
            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try { // 거래글 삭제 후 그 키 값에 해당하는 게시글이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성

                        var isExist = false

                        for(dataModel in dataSnapshot.children) {
                            val item = dataModel.getValue(DealChatConnection::class.java)

                            if(item!!.userId1 == myUid || item!!.userId2 == myUid) {
                                isExist = true
                                val intent = Intent(applicationContext, DealChatInActivity::class.java) // 해당 채팅방으로 이동
                                intent.putExtra("dealId", dealId)
                                intent.putExtra("chatConnectionId", item!!.chatConnectionId)

                                val your1 = FBRef.dealChatConnectionRef.child(item!!.dealId).child(item!!.chatConnectionId).child("userId1").get().addOnSuccessListener {
                                    if(it.value.toString() != myUid) {
                                        intent.putExtra("yourUid", it.value.toString())
                                        startActivity(intent)
                                    }
                                }
                                val your2 = FBRef.dealChatConnectionRef.child(item!!.dealId).child(item!!.chatConnectionId).child("userId2").get().addOnSuccessListener {
                                    if(it.value.toString() != myUid) {
                                        intent.putExtra("yourUid", it.value.toString())
                                        startActivity(intent)
                                    }
                                }
                            }
                        }

                        if(!isExist) {
                            val key = FBRef.dealChatConnectionRef.child(dealId).push().key.toString() // 키 값을 먼저 받아옴
                            FBRef.dealChatConnectionRef.child(dealId).child(key).setValue(DealChatConnection(dealId, key, myUid, sellerId, System.currentTimeMillis().toString())) // 채팅 커넥션 데이터 생성

                            val intent = Intent(applicationContext, DealChatInActivity::class.java)
                            intent.putExtra("dealId", dealId)
                            intent.putExtra("chatConnectionId", key)
                            intent.putExtra("yourUid", sellerId)
                            startActivity(intent)
                        }

                    } catch(e : Exception) {
                        Toast.makeText(applicationContext, "채팅 기록이 없습니다!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                    Toast.makeText(applicationContext, "채팅 기록이 없습니다!", Toast.LENGTH_SHORT).show()
                }
            }
            FBRef.dealChatConnectionRef.child(dealId).addValueEventListener(postListener)
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setData() {
        sellerProfile = findViewById(R.id.selllerProfile)
        sellerNickNameArea = findViewById(R.id.sellerNickNameArea)
        communitySet = findViewById(R.id.communitySet)

        textView = findViewById(R.id.textView)

        stateSpinner = findViewById(R.id.stateSpinner)

        imageRecyclerView = findViewById(R.id.imageRecyclerView)
        dealImageVAdapter = DealImageReVAdapter(imageDataList)
        imageRecyclerView.setItemViewCacheSize(20)
        imageRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imageRecyclerView.layoutManager = layoutManager
        imageRecyclerView.adapter = dealImageVAdapter

        titleArea = findViewById(R.id.titleArea)
        locationArea = findViewById(R.id.locationArea)
        categoryArea = findViewById(R.id.categoryArea)
        dateArea = findViewById(R.id.dateArea)
        contentArea = findViewById(R.id.contentArea)

        priceArea = findViewById(R.id.priceArea)
        methodArea = findViewById(R.id.methodArea)
        sellerChatBtn = findViewById(R.id.sellerChatBtn)
        customerChatBtn = findViewById(R.id.customerChatBtn)

        val profileFile = FBRef.userRef.child(sellerId).child("profileImage").get().addOnSuccessListener {
            val storageReference = Firebase.storage.reference.child(it.value.toString()) // 유저의 profile 사진을 DB의 storage로부터 가져옴

            storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                if(task.isSuccessful) {
                    Glide.with(applicationContext).load(task.result).thumbnail(Glide.with(applicationContext).load(task.result)).into(sellerProfile)
                } else {
                    sellerProfile.isVisible = false
                }
            })
        }

        val sellerNickName = FBRef.userRef.child(sellerId).child("nickName").get().addOnSuccessListener {
            sellerNickNameArea!!.text = it.value.toString() // 게시글에 판매자의 닉네임 표시
        }

        if(myUid == sellerId) {
            communitySet.visibility = VISIBLE
            stateSpinner.visibility = VISIBLE
            sellerChatBtn.visibility = VISIBLE
            customerChatBtn.visibility = GONE
        } else {
            communitySet.visibility = GONE
            stateSpinner.visibility = GONE
            sellerChatBtn.visibility = GONE
            customerChatBtn.visibility = VISIBLE
        }
    }

    private fun updateVisitors() {
        val dealRef = FBRef.dealRef.child(dealId)
        dealRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val visitors = dataSnapshot.child("visitors").value.toString().toInt()
                    val newVisitors = visitors + 1
                    val childUpdates = HashMap<String, Any>()
                    childUpdates["visitors"] = newVisitors.toString()
                    dealRef.updateChildren(childUpdates)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Failed to update visitors count.", databaseError.toException())
            }
        })
    }

    private fun getDealData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 거래글 삭제 후 그 키 값에 해당하는 게시글이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                    val dataModel = dataSnapshot.getValue(DealModel::class.java)
                    titleArea.text = dataModel!!.title
                    locationArea.text = dataModel!!.location
                    categoryArea.text = dataModel!!.category

                    when(dataModel!!.method) {
                        "둘 다" -> methodArea.text = "직거래 & 택배 거래"
                        else -> methodArea.text = dataModel!!.method
                    }

                    contentArea.text = dataModel!!.content

                    val decimalFormat = DecimalFormat("#,###")
                    priceArea.text = decimalFormat.format(dataModel!!.price.replace(",","").toDouble()) + "원"

                    if(myUid == dataModel!!.sellerId) {
                        state = dataModel!!.state
                        for (i in 0 until stateSpinner.count) {
                            if (stateSpinner.getItemAtPosition(i).toString() == state) {
                                stateSpinner.setSelection(i)
                                break
                            }
                        }
                    }

                    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss") // 문자열 형식 지정
                    val dateTime = LocalDateTime.parse(dataModel!!.date, formatter) // 문자열을 LocalDateTime 객체로 변환
                    val date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()) // LocalDateTime 객체를 Date 객체로 변환

                    val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault())
                    val now = Date()
                    val diff = now.time - date.time // 차이(ms)
                    val diffMinutes = diff / (60 * 1000) // 분으로 변환

                    when {
                        diffMinutes < 1 -> dateArea.text = "방금 전"
                        diffMinutes < 60 -> dateArea.text = "${diffMinutes}분 전"
                        diffMinutes < 24 * 60 -> dateArea.text = "${diffMinutes / 60}시간 전"
                        else -> {
                            val calendar = Calendar.getInstance() // Calendar 객체 생성
                            calendar.time = now // Calendar 객체에 Date 설정
                            val dealCalendar = Calendar.getInstance()
                            dealCalendar.time = date

                            if(calendar.get(Calendar.YEAR) == dealCalendar.get(Calendar.YEAR))
                                dateArea.text = SimpleDateFormat("MM월 dd일", Locale.getDefault()).format(date)
                            else
                                dateArea.text = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(date)
                        }
                    }

                    imageDataList.clear()

                    if(dataModel!!.imgCnt.toInt() >= 1) {
                        for(index in 0 until dataModel!!.imgCnt.toInt()) {
                            imageDataList.add("dealImage/$dealId/$dealId$index.png")
                        }
                        Log.d("imageDataList", imageDataList.toString())
                    }
                    dealImageVAdapter.notifyDataSetChanged()

                } catch(e : Exception) { }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dealRef.child(dealId).addValueEventListener(postListener)
    }

    private fun changeDealState(state : String) { // 거래 상품의 거래 상태를 바꿈
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 거래글 삭제 후 그 키 값에 해당하는 게시글이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                    val dataModel = dataSnapshot.getValue(DealModel::class.java)

                    if(state == "거래 완료") {
                        val intent = Intent(applicationContext, ChoiceBuyerActivity::class.java)
                        intent.putExtra("dealId", dealId)
                        startActivity(intent)
                    } else {
                        FBRef.dealRef.child(dataModel!!.dealId).setValue(DealModel(dataModel!!.dealId, dataModel!!.sellerId, dataModel!!.location, dataModel!!.category, dataModel!!.price, dataModel!!.title, dataModel!!.content, dataModel!!.imgCnt, dataModel!!.method, state, dataModel!!.date, "", "", dataModel!!.visitors)) // 게시물 정보 데이터베이스에 저장
                    }

                } catch(e : Exception) { }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dealRef.child(dealId).addValueEventListener(postListener)
    }

    private fun showDialog() { // 게시글 수정/삭제를 위한 다이얼로그 띄우기
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.deal_dialog, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)

        val alertDialog = mBuilder.show()
        val editBtn = alertDialog.findViewById<Button>(R.id.editBtn)
        editBtn?.setOnClickListener { // 수정 버튼 클릭 시
            Log.d(TAG, "edit Button Clicked")

            val intent = Intent(this, DealEditActivity::class.java)
            intent.putExtra("dealId", dealId)
            startActivity(intent) // 수정 페이지로 이동

            alertDialog.dismiss()
        }

        val rmBtn = alertDialog.findViewById<Button>(R.id.rmBtn)
        rmBtn?.setOnClickListener {  // 삭제 버튼 클릭 시
            Log.d(TAG, "remove Button Clicked")
            alertDialog.dismiss()

            deleteDealImage() // 거래 이미지 삭제
            deleteDealChatImage() // 거래 채팅 이미지 삭제
            FBRef.dealMessageRef.child(dealId).removeValue() // 거래 메시지 삭제
            FBRef.dealChatConnectionRef.child(dealId).removeValue() // 거래 채팅 커넥션 삭제
            FBRef.dealRef.child(dealId).removeValue() // 거래 데이터 삭제

            Toast.makeText(this, "거래 글이 삭제되었습니다!", Toast.LENGTH_SHORT).show()
            finish() // 삭제 완료 후 게시글 창 닫기
        }
    }

    private fun deleteDealImage() { // 거래 이미지를 삭제하는 함수
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val item = dataSnapshot.getValue(DealModel::class.java)

                    if (item!!.imgCnt.toInt() >= 1) {
                        for (index in 0 until item!!.imgCnt.toInt()) {
                            Firebase.storage.reference.child("dealImage/$dealId/$dealId$index.png")
                                .delete().addOnSuccessListener { // 사진 삭제
                                }.addOnFailureListener {
                                }
                        }
                    }

                } catch(e : Exception) { }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Geting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dealRef.child(dealId).addValueEventListener(postListener)
    }

    private fun deleteDealChatImage() { // 거래 채팅 이미지를 삭제하는 함수

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {

                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DealChatConnection::class.java)

                        val postListener = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                try {

                                    for(dataModel in dataSnapshot.children) {
                                        val item2 = dataModel.getValue(DealMessageModel::class.java)
                                        var chatConnectionId = item!!.chatConnectionId
                                        var messageId = item2!!.messageId
                                        Log.d("deleteImage", "$chatConnectionId $messageId")

                                        if (item2!!.picNum.toInt() >= 1) {
                                            for (index in 0 until item2!!.picNum.toInt()) {
                                                Firebase.storage.reference.child("dealMessageImage/$chatConnectionId/$messageId/$messageId$index.png")
                                                    .delete().addOnSuccessListener { // 사진 삭제
                                                    }.addOnFailureListener {
                                                        Log.d("deleteImage", it.toString())
                                                    }
                                            }
                                        }
                                    }
                                } catch(e : Exception) { }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Geting Post failed, log a message
                                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                            }
                        }
                        FBRef.dealMessageRef.child(item!!.dealId).child(item!!.chatConnectionId).addValueEventListener(postListener)
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
}