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
import org.techtown.myproject.utils.DogVomitModel

class VomitReVAdapter(val dogVomitList : ArrayList<DogVomitModel>):
    RecyclerView.Adapter<VomitReVAdapter.VomitViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener: OnItemClickListener

    override fun getItemCount(): Int {
        return dogVomitList.count()
    }

    override fun onBindViewHolder(holder: VomitViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        when (dogVomitList[position].vomitType) {
            "transparent" -> {
                holder.vomitTypeArea!!.text = "투명한 무색 구토"
                holder.vomitColor!!.setBackgroundColor(Color.parseColor("#FFFFFF"))
            }
            "bubble" -> {
                holder.vomitTypeArea!!.text = "흰색 거품이 섞인 구토"
                holder.vomitColor!!.setBackgroundColor(Color.parseColor("#FFFAFA"))
            }
            "food" -> {
                holder.vomitTypeArea!!.text = "음식이 섞인 구토"
                holder.vomitColor!!.setBackgroundColor(Color.parseColor("#CD853F"))
            }
            "yellow" -> {
                holder.vomitTypeArea!!.text = "노란색 구토"
                holder.vomitColor!!.setBackgroundColor(Color.parseColor("#FFE4B5"))
            }
            "leaf" -> {
                holder.vomitTypeArea!!.text = "잎사귀가 섞인 초록색 구토"
                holder.vomitColor!!.setBackgroundColor(Color.parseColor("#9ACD32"))
            }
            "pink" -> {
                holder.vomitTypeArea!!.text = "분홍색 구토"
                holder.vomitColor!!.setBackgroundColor(Color.parseColor("#FFC0CB"))
            }
            "brown" -> {
                holder.vomitTypeArea!!.text = "짙은 갈색 구토"
                holder.vomitColor!!.setBackgroundColor(Color.parseColor("#8B4513"))
            }
            "green" -> {
                holder.vomitTypeArea!!.text = "녹색 구토"
                holder.vomitColor!!.setBackgroundColor(Color.parseColor("#228B22"))
            }
            "substance" -> {
                holder.vomitTypeArea!!.text = "이물질이 섞인 구토"
                holder.vomitColor!!.setBackgroundColor(Color.parseColor("#708090"))
            }
            "red" -> {
                holder.vomitTypeArea!!.text = "붉은색 구토"
                holder.vomitColor!!.setBackgroundColor(Color.parseColor("#B22222"))
            }
        }

        holder.vomitCntArea!!.text = dogVomitList[position].vomitCount

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VomitViewHolder {
        val view =
            LayoutInflater.from(parent!!.context).inflate(R.layout.vomit_list_item, parent, false)
        return VomitViewHolder(view)
    }

    inner class VomitViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val vomitColor = view?.findViewById<LinearLayout>(R.id.vomitColor)
        val vomitTypeArea = view?.findViewById<TextView>(R.id.vomitTypeArea)
        val vomitCntArea = view?.findViewById<TextView>(R.id.vomitCntArea)
    }
}
