package org.techtown.myproject.note

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogCheckUpInputModel

class CheckUpInputReVAdapter(val dogCheckUpInputList : ArrayList<DogCheckUpInputModel>):
    RecyclerView.Adapter<CheckUpInputReVAdapter.CheckUpInputViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return dogCheckUpInputList.count()
    }

    override fun onBindViewHolder(holder: CheckUpInputViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        holder.nameArea!!.text = dogCheckUpInputList[position].name
        holder.maxArea!!.text = dogCheckUpInputList[position].max
        holder.minArea!!.text = dogCheckUpInputList[position].min
        holder.resultArea!!.text = dogCheckUpInputList[position].result

        if(dogCheckUpInputList[position].result.toFloat() < dogCheckUpInputList[position].min.toFloat()) {
            holder.stateImg!!.setImageResource(R.drawable.arrow_down)
            holder.state!!.text = "Low"
            holder.state!!.setTextColor(Color.parseColor("#1E90FF"))

            holder.resultArea!!.setTextColor(Color.parseColor("#1E90FF"))
        } else if(dogCheckUpInputList[position].result.toFloat() > dogCheckUpInputList[position].max.toFloat()) {
            holder.stateImg!!.setImageResource(R.drawable.arrow_up)
            holder.state!!.text = "High"
            holder.state!!.setTextColor(Color.parseColor("#DC143C"))

            holder.resultArea!!.setTextColor(Color.parseColor("#DC143C"))
        } else if(dogCheckUpInputList[position].result.toFloat() >= dogCheckUpInputList[position].min.toFloat() && dogCheckUpInputList[position].result.toFloat() <= dogCheckUpInputList[position].max.toFloat()) {
            holder.stateImg!!.setImageResource(R.drawable.minus)
            holder.state!!.text = "Normal"
            holder.state!!.setTextColor(Color.parseColor("#000000"))

            holder.resultArea!!.setTextColor(Color.parseColor("#000000"))
        }

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckUpInputViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.check_up_input_list_item, parent, false)
        return CheckUpInputViewHolder(view)
    }

    inner class CheckUpInputViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val nameArea = view?.findViewById<TextView>(R.id.nameArea)
        val stateImg = view?.findViewById<ImageView>(R.id.stateImg)
        val resultArea = view?.findViewById<TextView>(R.id.resultArea)
        val minArea = view?.findViewById<TextView>(R.id.minArea)
        val maxArea = view?.findViewById<TextView>(R.id.maxArea)
        val state = view?.findViewById<TextView>(R.id.state)
    }
}
