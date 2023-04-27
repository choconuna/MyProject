package org.techtown.myproject.deal

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.utils.DealModel
import org.techtown.myproject.utils.FBRef
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class DealReVAdapter(val dealList : ArrayList<DealModel>):
    RecyclerView.Adapter<DealReVAdapter.DealViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return dealList.count()
    }

    override fun onBindViewHolder(holder: DealViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        val itemLayoutView = holder!!.view?.findViewById<LinearLayout>(R.id.itemView)
        if(dealList[position].sellerId == myUid) { // 내가 쓴 글과 남이 쓴 글을 구분하기 위해 내가 쓴 글 배경색을 다른 색으로 적용
            itemLayoutView?.setBackgroundColor(Color.parseColor("#FAEBD7"))
        } else {
            itemLayoutView?.setBackgroundColor(Color.parseColor("#FFFFFF"))
        }

        val sellerUid = dealList[position].sellerId // 게시글 작성자
        val userName = FBRef.userRef.child(sellerUid).child("nickName").get().addOnSuccessListener {
            holder!!.nickNameArea!!.text = it.value.toString() // 게시글에 작성자의 아이디 표시
        }

        val profileFile = FBRef.userRef.child(sellerUid).child("profileImage").get().addOnSuccessListener {
            val storageReference = Firebase.storage.reference.child(it.value.toString()) // 유저의 profile 사진을 DB의 storage로부터 가져옴

            storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                if(task.isSuccessful) {
                    Glide.with(holder!!.view!!.context).load(task.result).into(holder!!.profileImageArea!!) // 유저의 profile 사진을 게시자 이름의 왼편에 표시함
                } else {
                    holder!!.profileImageArea!!.isVisible = false
                }
            })
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss") // 문자열 형식 지정
        val dateTime = LocalDateTime.parse(dealList[position].date, formatter) // 문자열을 LocalDateTime 객체로 변환
        val date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()) // LocalDateTime 객체를 Date 객체로 변환

        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault())
        val now = Date()
        val diff = now.time - date.time // 차이(ms)
        val diffMinutes = diff / (60 * 1000) // 분으로 변환

        when {
            diffMinutes < 1 -> holder!!.timeArea!!.text = "방금 전"
            diffMinutes < 60 -> holder!!.timeArea!!.text = "${diffMinutes}분 전"
            diffMinutes < 24 * 60 -> holder!!.timeArea!!.text = "${diffMinutes / 60}시간 전"
            else -> {
                val calendar = Calendar.getInstance() // Calendar 객체 생성
                calendar.time = now // Calendar 객체에 Date 설정
                val dealCalendar = Calendar.getInstance()
                dealCalendar.time = date

                if(calendar.get(Calendar.YEAR) == dealCalendar.get(Calendar.YEAR))
                    holder!!.timeArea!!.text = SimpleDateFormat("MM월 dd일", Locale.getDefault()).format(date)
                else
                    holder!!.timeArea!!.text = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(date)
            }
        }

        holder!!.categoryArea!!.text = dealList[position].category
        holder!!.titleArea!!.text = dealList[position].title
        holder!!.stateArea!!.text = dealList[position].state

        val decimalFormat = DecimalFormat("#,###")
        holder.priceArea!!.text = decimalFormat.format(dealList[position].price.replace(",","").toDouble()) + "원"

        when(dealList[position].method) {
            "둘 다" -> holder!!.methodArea!!.text = "직거래 & 택배 거래"
            else -> holder!!.methodArea!!.text = dealList[position].method
        }

        holder!!.locationArea!!.text = dealList[position].location

        when(dealList[position].state) {
            "예약 중" -> holder!!.stateArea!!.visibility = VISIBLE
            else -> holder!!.stateArea!!.visibility = GONE
        }

        val dealId = dealList[position].dealId
        val count = dealList[position].imgCnt
        if(count.toInt() >= 1) {

            val storageReference = Firebase.storage.reference.child("dealImage/$dealId/$dealId"+"0.png") // 커뮤니티에 첨부된 사진을 DB의 storage로부터 가져옴 -> 첨부된 사진이 여러 장이라면, 제일 첫 번째 사진을 대표 사진으로 띄움

            storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                if(task.isSuccessful) {
                    Glide.with(holder!!.view!!.context).load(task.result).into(holder!!.dealImage!!) // 게시글에 첫 번째 이미지를 표시함
                } else {
                    holder!!.dealImage!!.isVisible = false
                }
            })
        }

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DealViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.deal_list_item, parent, false)
        return DealViewHolder(view)
    }

    inner class DealViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val profileImageArea = view?.findViewById<ImageView>(R.id.profileImageArea)
        val nickNameArea = view?.findViewById<TextView>(R.id.nickNameArea)
        val timeArea = view?.findViewById<TextView>(R.id.timeArea)
        val categoryArea = view?.findViewById<TextView>(R.id.categoryArea)
        val titleArea = view?.findViewById<TextView>(R.id.titleArea)
        val stateArea = view?.findViewById<TextView>(R.id.stateArea)
        val priceArea = view?.findViewById<TextView>(R.id.priceArea)
        val methodArea = view?.findViewById<TextView>(R.id.methodArea)
        val locationArea = view?.findViewById<TextView>(R.id.locationArea)
        val dealImage = view?.findViewById<ImageView>(R.id.dealImage)
    }
}
