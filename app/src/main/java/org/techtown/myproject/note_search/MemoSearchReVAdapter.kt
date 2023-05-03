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
import org.techtown.myproject.note.MemoReVAdapter
import org.techtown.myproject.utils.DogMemoModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MemoSearchReVAdapter(val dogMemoList : ArrayList<DogMemoModel>):
    RecyclerView.Adapter<MemoSearchReVAdapter.MemoSearchViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return dogMemoList.count()
    }

    override fun onBindViewHolder(holder: MemoSearchViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        val currentYear = Calendar.getInstance().get(Calendar.YEAR) // 현재 연도 가져오기

        val dateSp = dogMemoList[position].date.split(".")

        if(dateSp[1].length == 1 && dateSp[2].length == 1) {
            val inputFormat = SimpleDateFormat("yyyy.M.d", Locale.getDefault())
            val date = inputFormat.parse(dogMemoList[position].date)
            val outputFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault())
            val outputDateStr = outputFormat.format(date!!)

            var outputDateSp = outputDateStr.split(" ")
            var outputDate = outputDateSp[0].split(".")

            if(currentYear == dateSp[0].toInt())
                holder!!.memoDate!!.text = outputDate[1] + "." + outputDate[2] + " " + outputDateSp[1]
            else
                holder!!.memoDate!!.text = outputDateSp[0]

        } else if(dateSp[1].length == 1 && dateSp[2].length == 2) {
            val inputFormat = SimpleDateFormat("yyyy.M.dd", Locale.getDefault())
            val date = inputFormat.parse(dogMemoList[position].date)
            val outputFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault())
            val outputDateStr = outputFormat.format(date!!)

            var outputDateSp = outputDateStr.split(" ")
            var outputDate = outputDateSp[0].split(".")

            if(currentYear == dateSp[0].toInt())
                holder!!.memoDate!!.text = outputDate[1] + "." + outputDate[2] + " " + outputDateSp[1]
            else
                holder!!.memoDate!!.text = outputDateSp[0]

        } else if(dateSp[1].length == 2 && dateSp[2].length == 1) {
            val inputFormat = SimpleDateFormat("yyyy.MM.d", Locale.getDefault())
            val date = inputFormat.parse(dogMemoList[position].date)
            val outputFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault())
            val outputDateStr = outputFormat.format(date!!)

            var outputDateSp = outputDateStr.split(" ")
            var outputDate = outputDateSp[0].split(".")

            if(currentYear == dateSp[0].toInt())
                holder!!.memoDate!!.text = outputDate[1] + "." + outputDate[2] + " " + outputDateSp[1]
            else
                holder!!.memoDate!!.text = outputDateSp[0]

        } else if(dateSp[1].length == 2 && dateSp[2].length == 2) {
            val inputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            val date = inputFormat.parse(dogMemoList[position].date)
            val outputFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault())
            val outputDateStr = outputFormat.format(date!!)

            var outputDateSp = outputDateStr.split(" ")
            var outputDate = outputDateSp[0].split(".")

            if(currentYear == dateSp[0].toInt())
                holder!!.memoDate!!.text = outputDate[1] + "." + outputDate[2] + " " + outputDateSp[1]
            else
                holder!!.memoDate!!.text = outputDateSp[0]
        }

        holder.memoContent!!.text = dogMemoList[position].content
        holder.memoTitle!!.text = dogMemoList[position].title
        holder.memoTime!!.text = dogMemoList[position].time

        val dogId = dogMemoList[position].dogId
        val dogMemoId = dogMemoList[position].dogMemoId
        val count = dogMemoList[position].count
        if(count.toInt() >= 1) {

            val storageReference = Firebase.storage.reference.child("memoImage/$myUid/$dogId/$dogMemoId/$dogMemoId"+"0.png") // 메모에 첨부된 사진을 DB의 storage로부터 가져옴 -> 첨부된 사진이 여러 장이라면, 제일 첫 번째 사진을 대표 사진으로 띄움

            storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                if(task.isSuccessful) {
                    Glide.with(holder.view!!).load(task.result).into(holder.imageView!!) // 메모의 사진을 표시함
                } else {
                    holder.imageView!!.isVisible = false
                }
            })
        }

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoSearchViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.memo_search_list_item, parent, false)
        return MemoSearchViewHolder(view)
    }

    inner class MemoSearchViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val memoDate = view?.findViewById<TextView>(R.id.dateArea)
        val memoTime = view?.findViewById<TextView>(R.id.timeArea)
        val memoTitle = view?.findViewById<TextView>(R.id.titleArea)
        val memoContent = view?.findViewById<TextView>(R.id.contentArea)
        val imageView = view?.findViewById<ImageView>(R.id.memoImage)
    }
}