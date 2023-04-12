package org.techtown.myproject.note

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogHeartModel

class HeartReVAdapter(val dogHeartList : ArrayList<DogHeartModel>):
    RecyclerView.Adapter<HeartReVAdapter.HeartViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return dogHeartList.count()
    }

    override fun onBindViewHolder(holder: HeartViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        holder.heartCntArea!!.text = dogHeartList[position].heartCount
        if(dogHeartList[position].heartCount.toInt() > 30)
            holder.state!!.setImageResource(R.drawable.arrow_up)
        else
            holder.state!!.setImageResource(R.drawable.minus)

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeartViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.heart_list_item, parent, false)
        return HeartViewHolder(view)
    }

    inner class HeartViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val heartCntArea = view?.findViewById<TextView>(R.id.heartCntArea)
        val state = view?.findViewById<ImageView>(R.id.state)
    }
}