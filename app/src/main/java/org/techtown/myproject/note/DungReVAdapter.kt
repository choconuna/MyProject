package org.techtown.myproject.note

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogDungModel

class DungReVAdapter(val dogDungList : ArrayList<DogDungModel>):
    RecyclerView.Adapter<DungReVAdapter.DungViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return dogDungList.count()
    }

    override fun onBindViewHolder(holder: DungViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        when (dogDungList[position].dungType) {
            "regular" -> {
                holder.dungTypeArea!!.text = "보통 변"
                holder.dungColor!!.setBackgroundColor(Color.parseColor("#CD853F"))
            }
            "watery" -> {
                holder.dungTypeArea!!.text = "묽은 변"
                holder.dungColor!!.setBackgroundColor(Color.parseColor("#DEB887"))
            }
            "diarrhea" -> {
                holder.dungTypeArea!!.text = "설사"
                holder.dungColor!!.setBackgroundColor(Color.parseColor("#CD853F"))
            }
            "hard" -> {
                holder.dungTypeArea!!.text = "짙고 딱딱한 변"
                holder.dungColor!!.setBackgroundColor(Color.parseColor("#A0522D"))
            }
            "red" -> {
                holder.dungTypeArea!!.text = "붉은색 변"
                holder.dungColor!!.setBackgroundColor(Color.parseColor("#8B0000"))
            }
            "black" -> {
                holder.dungTypeArea!!.text = "검은색 변"
                holder.dungColor!!.setBackgroundColor(Color.parseColor("#696969"))
            }
            "white" -> {
                holder.dungTypeArea!!.text = "하얀색 점이 있는 변"
                holder.dungColor!!.setBackgroundColor(Color.parseColor("#CD853F"))
            }
        }

        holder.dungCntArea!!.text = dogDungList[position].dungCount

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DungViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.dung_list_item, parent, false)
        return DungViewHolder(view)
    }

    inner class DungViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val dungColor = view?.findViewById<LinearLayout>(R.id.dungColor)
        val dungTypeArea = view?.findViewById<TextView>(R.id.dungTypeArea)
        val dungCntArea = view?.findViewById<TextView>(R.id.dungCntArea)
    }
}