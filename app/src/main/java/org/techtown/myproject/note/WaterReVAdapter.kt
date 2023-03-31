package org.techtown.myproject.note

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogWaterModel

class WaterReVAdapter(val dogWaterList : ArrayList<DogWaterModel>):
    RecyclerView.Adapter<WaterReVAdapter.WaterViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return dogWaterList.count()
    }

    override fun onBindViewHolder(holder: WaterViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        holder.timeSlotArea!!.text = dogWaterList[position].timeSlot
        holder.waterWeightArea!!.text = dogWaterList[position].waterWeight

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaterViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.water_list_item, parent, false)
        return WaterViewHolder(view)
    }

    inner class WaterViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val timeSlotArea = view?.findViewById<TextView>(R.id.timeSlotArea)
        val waterWeightArea = view?.findViewById<TextView>(R.id.waterWeightArea)
    }
}