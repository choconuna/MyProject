package org.techtown.myproject.note_search

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.note.DungReVAdapter
import org.techtown.myproject.utils.DogDungModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DungSearchReVAdapter(val dogDungList : ArrayList<DogDungModel>):
    RecyclerView.Adapter<DungSearchReVAdapter.DungSearchViewHolder>() {

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

    override fun onBindViewHolder(holder: DungSearchViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        val currentYear = Calendar.getInstance().get(Calendar.YEAR) // 현재 연도 가져오기

        val dateSp = dogDungList[position].date.split(".")

        if(dateSp[1].length == 1 && dateSp[2].length == 1) {
            val inputFormat = SimpleDateFormat("yyyy.M.d", Locale.getDefault())
            val date = inputFormat.parse(dogDungList[position].date)
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
            val date = inputFormat.parse(dogDungList[position].date)
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
            val date = inputFormat.parse(dogDungList[position].date)
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
            val date = inputFormat.parse(dogDungList[position].date)
            val outputFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault())
            val outputDateStr = outputFormat.format(date!!)

            var outputDateSp = outputDateStr.split(" ")
            var outputDate = outputDateSp[0].split(".")

            if(currentYear == dateSp[0].toInt())
                holder!!.dateArea!!.text = outputDate[1] + "." + outputDate[2] + " " + outputDateSp[1]
            else
                holder!!.dateArea!!.text = outputDateSp[0]
        }

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DungSearchViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.dung_search_list_item, parent, false)
        return DungSearchViewHolder(view)
    }

    inner class DungSearchViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val dateArea = view?.findViewById<TextView>(R.id.dateArea)
        val dungColor = view?.findViewById<LinearLayout>(R.id.dungColor)
        val dungTypeArea = view?.findViewById<TextView>(R.id.dungTypeArea)
        val dungCntArea = view?.findViewById<TextView>(R.id.dungCntArea)
    }
}