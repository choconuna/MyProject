package org.techtown.myproject.deal

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView
import org.techtown.myproject.R
import org.techtown.myproject.utils.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class DealChatReVAdapter(val dealChatList : ArrayList<DealChatConnection>):
    RecyclerView.Adapter<DealChatReVAdapter.DealChatViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return dealChatList.count()
    }

    override fun onBindViewHolder(holder: DealChatViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        var yourUid = ""

        if(dealChatList[position].userId1 == myUid)
            yourUid = dealChatList[position].userId2
        else if(dealChatList[position].userId2 == myUid)
            yourUid = dealChatList[position].userId1


        val profileFile = FBRef.userRef.child(yourUid).child("profileImage").get().addOnSuccessListener {
            val storageReference = Firebase.storage.reference.child(it.value.toString()) // 유저의 profile 사진을 DB의 storage로부터 가져옴

            storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                if(task.isSuccessful) {
                    Glide.with(holder!!.view!!.context).load(task.result).thumbnail(Glide.with(holder!!.view!!.context).load(task.result)).into(holder!!.yourProfile!!)
                } else {
                    holder!!.yourProfile!!.isVisible = false
                }
            })
        }

        val userName = FBRef.userRef.child(yourUid).child("nickName").get().addOnSuccessListener {
            holder!!.yourNickNameArea!!.text = it.value.toString() // 채팅 상대방의 닉네임 표시
        }

        val userLocationRef = FBRef.userLocationRef.child(yourUid) // uid를 기반으로 데이터베이스 참조를 만듦
        userLocationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (locationSnapshot in dataSnapshot.children) {
                    val item = locationSnapshot.getValue(UserLocationModel::class.java)
                    Log.d("yourLocation", item!!.location)
                    holder!!.yourLocationArea!!.text = item!!.location.split(" ")[2]
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        val dealId = dealChatList[position].dealId
        val imgCnt = FBRef.dealRef.child(dealId).child("imgCnt").get().addOnSuccessListener {
            if(it.value.toString().toInt() >= 1) {

                val storageReference = Firebase.storage.reference.child("dealImage/$dealId/$dealId"+"0.png") // 커뮤니티에 첨부된 사진을 DB의 storage로부터 가져옴 -> 첨부된 사진이 여러 장이라면, 제일 첫 번째 사진을 대표 사진으로 띄움

                storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                    if(task.isSuccessful) {
                        Glide.with(holder!!.view!!.context).load(task.result).into(holder!!.itemImageArea!!) // 게시글에 첫 번째 이미지를 표시함
                    } else {
                        holder!!.itemImageArea!!.visibility = GONE
                    }
                })
            }
        }

        var messageList : MutableList<DealMessageModel> = mutableListOf()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {

                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DealMessageModel::class.java)

                        messageList.add(item!!)
                    }

                    if(messageList[messageList.size - 1].type == "letter") // 맨 마지막 메시지 내용이 글일 경우
                        holder!!.contentArea!!.text = messageList[messageList.size - 1].content // 맨 마지막 메시지 내용을 띄움
                    else if(messageList[messageList.size - 1].type == "picture") // 맨 마지막 메시지 내용이 사진일 경우
                        holder!!.contentArea!!.text = "사진을 보냈습니다."

                    val currentDataTime = Calendar.getInstance().time
                    val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(currentDataTime)

                    val sendDateSp = messageList[messageList.size-1].sendDate.split(" ")[0].split(".")
                    val sendTimeSp = messageList[messageList.size-1].sendDate.split(" ")[1].split(":")
                    val sendDateFormat = sendDateSp[0] + "." + sendDateSp[1] + "." + sendDateSp[2]

                    val currentDate: LocalDate =
                        LocalDate.parse(dateFormat, DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                    val sendDate: LocalDate =
                        LocalDate.parse(sendDateFormat, DateTimeFormatter.ofPattern("yyyy.MM.dd"))

                    when {
                        currentDate.minusDays(1) == sendDate -> { // 마지막 메시지를 보낸 날짜가 어제인 경우
                            holder!!.timeArea!!.text = "어제"
                        }
                        currentDate == sendDate -> { // 마지막 메시지를 보낸 날짜가 오늘인 경우
                            holder!!.timeArea!!.text = sendTimeSp[0] + ":" + sendTimeSp[1] // 몇 시 몇 분인지 표시되도록 함
                        }
                        currentDate.year == sendDate.year -> { // 현재 날짜와 메시지를 보낸 날짜가 같은 연도일 경우
                            holder!!.timeArea!!.text = sendDateSp[1] + "월 " + sendDateSp[2] + "일" // 날짜가 표시되도록 함 (월, 일만)
                        }
                        else -> { // 현재 날짜와 메시지를 보낸 날짜가 다른 연도일 경우
                            holder!!.timeArea!!.text = sendDateSp[0] + "년 " + sendDateSp[1] + "월 " + sendDateSp[2] +"일" // 날짜가 표시되도록 함(연도와 월, 일 모두)
                        }
                    }

                } catch(e : Exception) { }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        FBRef.dealMessageRef.child(dealChatList[position].dealId).child(dealChatList[position].chatConnectionId).addValueEventListener(postListener)

        val shownPostListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {

                    var noShownCnt = 0

                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(MessageModel::class.java)

                        if(item!!.sendUid != myUid) { // 메시지를 보낸 사람이 내가 아닌 경우
                            if(item!!.shown == "false") // 내가 상대방이 보낸 메시지를 확인하지 못한 경우
                                noShownCnt++ // 보지 못한 메시지의 수를 나타내는 nonShwonCnt를 1 증가시킴
                        }
                    }

                    if(noShownCnt > 0) {
                        holder!!.noShownCntArea!!.text = noShownCnt.toString()
                        holder!!.noShownCntArea!!.visibility = VISIBLE
                    } else if(noShownCnt == 0) {
                        holder!!.noShownCntArea!!.visibility = GONE
                    }

                } catch(e : Exception) { }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        FBRef.dealMessageRef.child(dealChatList[position].dealId).child(dealChatList[position].chatConnectionId).addValueEventListener(shownPostListener)

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DealChatViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.deal_chat_list_item, parent, false)
        return DealChatViewHolder(view)
    }

    inner class DealChatViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val yourProfile = view?.findViewById<CircleImageView>(R.id.yourProfile)
        val yourNickNameArea = view?.findViewById<TextView>(R.id.yourNickNameArea)
        val yourLocationArea = view?.findViewById<TextView>(R.id.yourLocationArea)
        val contentArea = view?.findViewById<TextView>(R.id.contentArea)
        val timeArea = view?.findViewById<TextView>(R.id.timeArea)
        val noShownCntArea = view?.findViewById<TextView>(R.id.noShownCntArea)
        val itemImageArea = view?.findViewById<ImageView>(R.id.itemImageArea)
    }
}