package org.techtown.myproject.note

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogPeeModel

class PeeReVAdapter(val dogPeeList : ArrayList<DogPeeModel>):
    RecyclerView.Adapter<PeeReVAdapter.PeeViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return dogPeeList.count()
    }

    override fun onBindViewHolder(holder: PeeViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        when (dogPeeList[position].peeType) {
            "transparent" -> {
                holder.peeTypeArea!!.text = "투명한 무색 소변"
                holder.peeColor!!.setBackgroundColor(Color.parseColor("#000000"))
            }
            "lightYellow" -> {
                holder.peeTypeArea!!.text = "투명한 노란색 소변"
                holder.peeColor!!.setBackgroundColor(Color.parseColor("#FFFFE0"))
            }
            "darkYellow" -> {
                holder.peeTypeArea!!.text = "주황색과 어두운 노란색 소변"
                holder.peeColor!!.setBackgroundColor(Color.parseColor("#FFE4B5"))
            }
            "red" -> {
                holder.peeTypeArea!!.text = "붉은색 소변"
                holder.peeColor!!.setBackgroundColor(Color.parseColor("#FA8072"))
            }
            "brown" -> {
                holder.peeTypeArea!!.text = "갈색 소변"
                holder.peeColor!!.setBackgroundColor(Color.parseColor("#A0522D"))
            }
        }

        holder.peeCntArea!!.text = dogPeeList[position].peeCount

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeeViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.pee_list_item, parent, false)
        return PeeViewHolder(view)
    }

    inner class PeeViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val peeColor = view?.findViewById<LinearLayout>(R.id.peeColor)
        val peeTypeArea = view?.findViewById<TextView>(R.id.peeTypeArea)
        val peeCntArea = view?.findViewById<TextView>(R.id.peeCntArea)
    }
}
