package org.techtown.myproject.note

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogMedicinePlanModel

class MedicinePlanReVAdapter(val dogMeidicinePlanList : ArrayList<DogMedicinePlanModel>):
    RecyclerView.Adapter<MedicinePlanReVAdapter.MedicinePlanViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return dogMeidicinePlanList.count()
    }

    override fun onBindViewHolder(holder: MedicinePlanViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        holder.repeatArea!!.text = dogMeidicinePlanList[position].repeat
        holder.timeArea!!.text = dogMeidicinePlanList[position].time
        holder.medicineNameArea!!.text = dogMeidicinePlanList[position].medicineName

        if(dogMeidicinePlanList[position].repeat == "매일") { // 투약 일정이 반복될 경우, 반복 기간이 보이도록 설정
            holder.dateArea!!.visibility = VISIBLE // 반복 기간이 보이도록 설정
            holder.startDateArea!!.text = dogMeidicinePlanList[position].startDate
            holder.endDateArea!!.text = dogMeidicinePlanList[position].endDate
        }

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicinePlanViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.medicine_plan_list_item, parent, false)
        return MedicinePlanViewHolder(view)
    }

    inner class MedicinePlanViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val repeatArea = view?.findViewById<TextView>(R.id.repeatArea)
        val timeArea = view?.findViewById<TextView>(R.id.timeArea)
        val dateArea = view?.findViewById<LinearLayout>(R.id.dateArea)
        val startDateArea = view?.findViewById<TextView>(R.id.startDateArea)
        val endDateArea = view?.findViewById<TextView>(R.id.endDateArea)
        val medicineNameArea = view?.findViewById<TextView>(R.id.medicineNameArea)
        val checkImage = view?.findViewById<ImageView>(R.id.checkImage)
    }
}