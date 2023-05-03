package org.techtown.myproject.note_search

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
import org.techtown.myproject.note.CheckUpPictureReVAdapter
import org.techtown.myproject.utils.DogCheckUpPictureModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CheckUpPictureSearchReVAdapter(val dogCheckUpPictureList : ArrayList<DogCheckUpPictureModel>):
    RecyclerView.Adapter<CheckUpPictureSearchReVAdapter.CheckUpPictureSearchViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener: OnItemClickListener

    override fun getItemCount(): Int {
        return dogCheckUpPictureList.count()
    }

    override fun onBindViewHolder(holder: CheckUpPictureSearchViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid//

        val currentYear = Calendar.getInstance().get(Calendar.YEAR) // 현재 연도 가져오기

        val dateSp = dogCheckUpPictureList[position].date.split(".")

        if(dateSp[1].length == 1 && dateSp[2].length == 1) {
            val inputFormat = SimpleDateFormat("yyyy.M.d", Locale.getDefault())
            val date = inputFormat.parse(dogCheckUpPictureList[position].date)
            val outputFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault())
            val outputDateStr = outputFormat.format(date!!)

            var outputDateSp = outputDateStr.split(" ")
            var outputDate = outputDateSp[0].split(".")

            if(currentYear == dateSp[0].toInt())
                holder!!.dateArea!!.text = outputDate[1] + "." + outputDate[2] + " " + outputDateSp[1]
            else
                holder!!.dateArea!!.text = outputDateSp[0]

        } else if(dateSp[1].length == 1 && dateSp[2].length == 2) {
            val inputFormat = SimpleDateFormat("yyyy.M.dd", Locale.getDefault())
            val date = inputFormat.parse(dogCheckUpPictureList[position].date)
            val outputFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault())
            val outputDateStr = outputFormat.format(date!!)

            var outputDateSp = outputDateStr.split(" ")
            var outputDate = outputDateSp[0].split(".")

            if(currentYear == dateSp[0].toInt())
                holder!!.dateArea!!.text = outputDate[1] + "." + outputDate[2] + " " + outputDateSp[1]
            else
                holder!!.dateArea!!.text = outputDateSp[0]

        } else if(dateSp[1].length == 2 && dateSp[2].length == 1) {
            val inputFormat = SimpleDateFormat("yyyy.MM.d", Locale.getDefault())
            val date = inputFormat.parse(dogCheckUpPictureList[position].date)
            val outputFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault())
            val outputDateStr = outputFormat.format(date!!)

            var outputDateSp = outputDateStr.split(" ")
            var outputDate = outputDateSp[0].split(".")

            if(currentYear == dateSp[0].toInt())
                holder!!.dateArea!!.text = outputDate[1] + "." + outputDate[2] + " " + outputDateSp[1]
            else
                holder!!.dateArea!!.text = outputDateSp[0]

        } else if(dateSp[1].length == 2 && dateSp[2].length == 2) {
            val inputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            val date = inputFormat.parse(dogCheckUpPictureList[position].date)
            val outputFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault())
            val outputDateStr = outputFormat.format(date!!)

            var outputDateSp = outputDateStr.split(" ")
            var outputDate = outputDateSp[0].split(".")

            if(currentYear == dateSp[0].toInt())
                holder!!.dateArea!!.text = outputDate[1] + "." + outputDate[2] + " " + outputDateSp[1]
            else
                holder!!.dateArea!!.text = outputDateSp[0]
        }

        holder.checkUpCategoryArea!!.text = dogCheckUpPictureList[position].checkUpCategory
        holder.hospitalArea!!.text = dogCheckUpPictureList[position].hospitalName
        holder.contentArea!!.text = dogCheckUpPictureList[position].content

        val dogId = dogCheckUpPictureList[position].dogId
        val dogCheckUpPictureId = dogCheckUpPictureList[position].dogCheckUpPictureId
        val count = dogCheckUpPictureList[position].count
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckUpPictureSearchViewHolder {
        val view = LayoutInflater.from(parent!!.context)
            .inflate(R.layout.check_up_picture_search_list_item, parent, false)
        return CheckUpPictureSearchViewHolder(view)
    }

    inner class CheckUpPictureSearchViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val dateArea = view?.findViewById<TextView>(R.id.dateArea)
        val checkUpCategoryArea = view?.findViewById<TextView>(R.id.checkUpCategoryArea)
        val hospitalArea = view?.findViewById<TextView>(R.id.hospitalArea)
        val contentArea = view?.findViewById<TextView>(R.id.contentArea)
        val imageView = view?.findViewById<ImageView>(R.id.imageView)
    }
}
