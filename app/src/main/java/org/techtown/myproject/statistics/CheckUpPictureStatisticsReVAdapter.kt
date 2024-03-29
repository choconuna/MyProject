package org.techtown.myproject.statistics

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
import org.techtown.myproject.utils.DogCheckUpPictureModel

class CheckUpPictureStatisticsReVAdapter(val dogCheckUpPictureStatisticsList : ArrayList<DogCheckUpPictureModel>):
    RecyclerView.Adapter<CheckUpPictureStatisticsReVAdapter.CheckUpPictureStaticsViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener: OnItemClickListener

    override fun getItemCount(): Int {
        return dogCheckUpPictureStatisticsList.count()
    }

    override fun onBindViewHolder(holder: CheckUpPictureStaticsViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        holder.date!!.text = dogCheckUpPictureStatisticsList[position].date
        holder.hospitalArea!!.text = dogCheckUpPictureStatisticsList[position].hospitalName
        holder.contentArea!!.text = dogCheckUpPictureStatisticsList[position].content

        val dogId = dogCheckUpPictureStatisticsList[position].dogId
        val dogCheckUpPictureId = dogCheckUpPictureStatisticsList[position].dogCheckUpPictureId
        val count = dogCheckUpPictureStatisticsList[position].count
        if (count.toInt() >= 1) {

            val storageReference =
                Firebase.storage.reference.child("checkUpImage/$myUid/$dogId/$dogCheckUpPictureId/$dogCheckUpPictureId" + "0.png") // 검사 기록에 첨부된 사진을 DB의 storage로부터 가져옴 -> 첨부된 사진이 여러 장이라면, 제일 첫 번째 사진을 대표 사진으로 띄움

            storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                if (task.isSuccessful) {
                    Glide.with(holder.view!!).load(task.result)
                        .into(holder.imageView!!) // 메모의 사진을 표시함
                } else {
                    holder.imageView!!.isVisible = false
                }
            })
        }

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckUpPictureStaticsViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.check_up_picture_statistics_list_item, parent, false)
        return CheckUpPictureStaticsViewHolder(view)
    }

    inner class CheckUpPictureStaticsViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val date = view?.findViewById<TextView>(R.id.dateArea)
        val hospitalArea = view?.findViewById<TextView>(R.id.hospitalArea)
        val contentArea = view?.findViewById<TextView>(R.id.contentArea)
        val imageView = view?.findViewById<ImageView>(R.id.imageView)
    }
}
