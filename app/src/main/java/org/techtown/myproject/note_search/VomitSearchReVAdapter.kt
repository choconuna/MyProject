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
import org.techtown.myproject.utils.DogVomitModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VomitSearchReVAdapter(val dogVomitList : ArrayList<DogVomitModel>):
    RecyclerView.Adapter<VomitSearchReVAdapter.VomitSearchViewHolder>() {

    override fun getItemCount(): Int {
        return dogVomitList.count()
    }

    override fun onBindViewHolder(holder: VomitSearchViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        val currentYear = Calendar.getInstance().get(Calendar.YEAR) // 현재 연도 가져오기

        val dateSp = dogVomitList[position].date.split(".")

        if(dateSp[1].length == 1 && dateSp[2].length == 1) {
            val inputFormat = SimpleDateFormat("yyyy.M.d", Locale.getDefault())
            val date = inputFormat.parse(dogVomitList[position].date)
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
            val date = inputFormat.parse(dogVomitList[position].date)
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
            val date = inputFormat.parse(dogVomitList[position].date)
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
            val date = inputFormat.parse(dogVomitList[position].date)
            val outputFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault())
            val outputDateStr = outputFormat.format(date!!)

            var outputDateSp = outputDateStr.split(" ")
            var outputDate = outputDateSp[0].split(".")

            if(currentYear == dateSp[0].toInt())
                holder!!.dateArea!!.text = outputDate[1] + "." + outputDate[2] + " " + outputDateSp[1]
            else
                holder!!.dateArea!!.text = outputDateSp[0]
        }

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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VomitSearchViewHolder {
        val view =
            LayoutInflater.from(parent!!.context).inflate(R.layout.vomit_search_list_item, parent, false)
        return VomitSearchViewHolder(view)
    }

    inner class VomitSearchViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val dateArea = view?.findViewById<TextView>(R.id.dateArea)
        val vomitColor = view?.findViewById<LinearLayout>(R.id.vomitColor)
        val vomitTypeArea = view?.findViewById<TextView>(R.id.vomitTypeArea)
        val vomitCntArea = view?.findViewById<TextView>(R.id.vomitCntArea)
    }
}
