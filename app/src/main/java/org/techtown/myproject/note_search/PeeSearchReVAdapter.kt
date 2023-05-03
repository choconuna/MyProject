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
import org.techtown.myproject.utils.DogPeeModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PeeSearchReVAdapter(val dogPeeList : ArrayList<DogPeeModel>):
    RecyclerView.Adapter<PeeSearchReVAdapter.PeeSearchViewHolder>() {

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

    override fun onBindViewHolder(holder: PeeSearchViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        val currentYear = Calendar.getInstance().get(Calendar.YEAR) // 현재 연도 가져오기

        val dateSp = dogPeeList[position].date.split(".")

        if(dateSp[1].length == 1 && dateSp[2].length == 1) {
            val inputFormat = SimpleDateFormat("yyyy.M.d", Locale.getDefault())
            val date = inputFormat.parse(dogPeeList[position].date)
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
            val date = inputFormat.parse(dogPeeList[position].date)
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
            val date = inputFormat.parse(dogPeeList[position].date)
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
            val date = inputFormat.parse(dogPeeList[position].date)
            val outputFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault())
            val outputDateStr = outputFormat.format(date!!)

            var outputDateSp = outputDateStr.split(" ")
            var outputDate = outputDateSp[0].split(".")

            if(currentYear == dateSp[0].toInt())
                holder!!.dateArea!!.text = outputDate[1] + "." + outputDate[2] + " " + outputDateSp[1]
            else
                holder!!.dateArea!!.text = outputDateSp[0]
        }

        when (dogPeeList[position].peeType) {
            "transparent" -> {
                holder.peeTypeArea!!.text = "투명한 무색 소변"
                holder.peeColor!!.setBackgroundColor(Color.parseColor("#FFFFFF"))
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeeSearchViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.pee_list_item, parent, false)
        return PeeSearchViewHolder(view)
    }

    inner class PeeSearchViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val dateArea = view?.findViewById<TextView>(R.id.dateArea)
        val peeColor = view?.findViewById<LinearLayout>(R.id.peeColor)
        val peeTypeArea = view?.findViewById<TextView>(R.id.peeTypeArea)
        val peeCntArea = view?.findViewById<TextView>(R.id.peeCntArea)
    }
}
