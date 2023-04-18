package org.techtown.myproject.walk

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.WalkDogModel
import org.techtown.myproject.utils.WalkModel
import java.math.RoundingMode
import java.text.DecimalFormat

class SpecificWalkReVAdapter(val walkList : ArrayList<WalkDogModel>):
    RecyclerView.Adapter<SpecificWalkReVAdapter.SpecificWalkViewHolder>() {

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

    override fun onBindViewHolder(holder: SpecificWalkViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecificWalkViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.specific_dog_walk_list_item, parent, false)
        return SpecificWalkViewHolder(view)
    }

    inner class SpecificWalkViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val dateArea = view?.findViewById<TextView>(R.id.dateArea)
        val startTimeArea = view?.findViewById<TextView>(R.id.startTimeArea)
        val endTimeArea = view?.findViewById<TextView>(R.id.endTimeArea)
        val distanceArea = view?.findViewById<TextView>(R.id.distanceArea)
        val timeArea = view?.findViewById<TextView>(R.id.timeArea)
    }
}