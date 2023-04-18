package org.techtown.myproject.walk

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.receipt.ReceiptDetailReVAdapter
import org.techtown.myproject.utils.*
import java.math.RoundingMode
import java.text.DecimalFormat

class WalkReVAdapter(val walkList : ArrayList<WalkModel>):
    RecyclerView.Adapter<WalkReVAdapter.WalkViewHolder>() {

    lateinit var dogRecyclerView: RecyclerView
    private val dogReDataList = java.util.ArrayList<String>() // 각 반려견의 프로필을 넣는 리스트
    lateinit var dogListReVAdapter: WithDogReVAdapter
    lateinit var layoutManager: RecyclerView.LayoutManager

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return walkList.count()
    }

    override fun onBindViewHolder(holder: WalkViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        dogListReVAdapter = WithDogReVAdapter(dogReDataList)
        dogRecyclerView = holder!!.view!!.findViewById(R.id.dogImageRecyclerView)
        dogRecyclerView.setItemViewCacheSize(20)
        dogRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(holder!!.view!!.context, LinearLayoutManager.HORIZONTAL, false)
        dogRecyclerView.layoutManager = layoutManager
        dogRecyclerView.adapter = dogListReVAdapter

        val walkId = walkList[position].walkId

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for(dataModel in dataSnapshot.children) {
                    val item = dataModel.getValue(DogModel::class.java)
                    val postListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            try {

                                dogReDataList.clear()

                                for(dataModel in dataSnapshot.children) {
                                    // dataModel.key
                                    val item = dataModel.getValue(WalkDogModel::class.java)
                                    if(item!!.walkId == walkId) {
                                        dogReDataList.add(item!!.dogId) // 해당 산책 기록에서 함께한 반려견의 id를 리스트에 포함 -> 반려견 프로필 사진을 띄우기 위함
                                    }
                                }

                                dogListReVAdapter.notifyDataSetChanged() // 동기화
                            } catch (e: Exception) {
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    }
                    FBRef.walkDogRef.child(myUid).child(item!!.dogId).addValueEventListener(postListener)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        FBRef.dogRef.child(myUid).addValueEventListener(postListener)


        holder!!.dateArea!!.text = walkList[position].date

        var startTimeSp = walkList[position].startTime.split(":")
        holder!!.startTimeArea!!.text = startTimeSp[0] + "시 " + startTimeSp[1] + "분"

        var endTimeSp = walkList[position].endTime.split(":")
        holder!!.endTimeArea!!.text = endTimeSp[0] + "시 " + endTimeSp[1] + "분"

        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.DOWN
        val roundoff = df.format(walkList[position].distance.toFloat())
        holder!!.distanceArea!!.text = roundoff

        var timeSp = walkList[position].time.split(":")
        var hour = timeSp[0]
        var minute = timeSp[1]
        var second = timeSp[2]
        if(hour.toInt() == 0) { // 산책 시간이 1시간 미만일 경우
            if(second.toInt() == 0) {
                holder!!.timeArea!!.text = minute.toInt().toString() + "분"
            } else if(second.toInt() != 0) {
                holder!!.timeArea!!.text = minute.toInt().toString() + "분 " + second.toInt().toString() + "초"
            }
        } else if(hour.toInt() != 0) { // 산책 시간이 1시간 이상일 경우
            if(minute.toInt() == 0 && second.toInt() == 0) {
                holder!!.timeArea!!.text = hour.toInt().toString() + "시간"
            } else if(minute.toInt() == 0 && second.toInt() != 0) {
                holder!!.timeArea!!.text = hour.toInt().toString() + "시간 " + second.toInt().toString() + "초"
            } else if(minute.toInt() != 0 && second.toInt() == 0) {
                holder!!.timeArea!!.text = hour.toInt().toString() + "시간 " + minute.toInt().toString() + "분"
            } else if(minute.toInt() != 0 && second.toInt() != 0) {
                holder!!.timeArea!!.text = hour.toInt().toString() + "시간 " + minute.toInt().toString() + "분 " + second.toInt().toString() + "초"
            }
        }

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalkViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.walk_list_item, parent, false)
        return WalkViewHolder(view)
    }

    inner class WalkViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val dateArea = view?.findViewById<TextView>(R.id.dateArea)
        val startTimeArea = view?.findViewById<TextView>(R.id.startTimeArea)
        val endTimeArea = view?.findViewById<TextView>(R.id.endTimeArea)
        val distanceArea = view?.findViewById<TextView>(R.id.distanceArea)
        val timeArea = view?.findViewById<TextView>(R.id.timeArea)
    }
}