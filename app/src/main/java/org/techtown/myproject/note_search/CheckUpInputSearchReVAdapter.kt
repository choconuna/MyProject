package org.techtown.myproject.note_search

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.note.CheckUpInputReVAdapter
import org.techtown.myproject.utils.DogCheckUpInputModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CheckUpInputSearchReVAdapter(val dogCheckUpInputList : ArrayList<DogCheckUpInputModel>):
    RecyclerView.Adapter<CheckUpInputSearchReVAdapter.CheckUpSearchInputViewHolder>() {

    override fun getItemCount(): Int {
        return dogCheckUpInputList.count()
    }

    override fun onBindViewHolder(holder: CheckUpSearchInputViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        val currentYear = Calendar.getInstance().get(Calendar.YEAR) // 현재 연도 가져오기

        val dateSp = dogCheckUpInputList[position].date.split(".")

        if(dateSp[1].length == 1 && dateSp[2].length == 1) {
            val inputFormat = SimpleDateFormat("yyyy.M.d", Locale.getDefault())
            val date = inputFormat.parse(dogCheckUpInputList[position].date)
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
            val date = inputFormat.parse(dogCheckUpInputList[position].date)
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
            val date = inputFormat.parse(dogCheckUpInputList[position].date)
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
            val date = inputFormat.parse(dogCheckUpInputList[position].date)
            val outputFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault())
            val outputDateStr = outputFormat.format(date!!)

            var outputDateSp = outputDateStr.split(" ")
            var outputDate = outputDateSp[0].split(".")

            if(currentYear == dateSp[0].toInt())
                holder!!.dateArea!!.text = outputDate[1] + "." + outputDate[2] + " " + outputDateSp[1]
            else
                holder!!.dateArea!!.text = outputDateSp[0]
        }

        holder.nameArea!!.text = dogCheckUpInputList[position].name
        holder.maxArea!!.text = dogCheckUpInputList[position].max
        holder.minArea!!.text = dogCheckUpInputList[position].min
        holder.resultArea!!.text = dogCheckUpInputList[position].result
        holder.partArea!!.text = dogCheckUpInputList[position].part

        if(dogCheckUpInputList[position].result.toFloat() < dogCheckUpInputList[position].min.toFloat()) {
            holder.stateImg!!.setImageResource(R.drawable.ic_round_arrow_downward_24)
            holder.state!!.text = "Low"
            holder.state!!.setTextColor(Color.parseColor("#1E90FF"))

            holder.resultArea!!.setTextColor(Color.parseColor("#1E90FF"))
        } else if(dogCheckUpInputList[position].result.toFloat() > dogCheckUpInputList[position].max.toFloat()) {
            holder.stateImg!!.setImageResource(R.drawable.ic_round_arrow_upward_24)
            holder.state!!.text = "High"
            holder.state!!.setTextColor(Color.parseColor("#DC143C"))

            holder.resultArea!!.setTextColor(Color.parseColor("#DC143C"))
        } else if(dogCheckUpInputList[position].result.toFloat() >= dogCheckUpInputList[position].min.toFloat() && dogCheckUpInputList[position].result.toFloat() <= dogCheckUpInputList[position].max.toFloat()) {
            holder.stateImg!!.setImageResource(R.drawable.ic_round_horizontal_rule_24)
            holder.state!!.text = "Normal"
            holder.state!!.setTextColor(Color.parseColor("#000000"))

            holder.resultArea!!.setTextColor(Color.parseColor("#000000"))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckUpSearchInputViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.check_up_input_search_list_item, parent, false)
        return CheckUpSearchInputViewHolder(view)
    }

    inner class CheckUpSearchInputViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val dateArea = view?.findViewById<TextView>(R.id.dateArea)
        val nameArea = view?.findViewById<TextView>(R.id.nameArea)
        val stateImg = view?.findViewById<ImageView>(R.id.stateImg)
        val resultArea = view?.findViewById<TextView>(R.id.resultArea)
        val minArea = view?.findViewById<TextView>(R.id.minArea)
        val maxArea = view?.findViewById<TextView>(R.id.maxArea)
        val state = view?.findViewById<TextView>(R.id.state)
        val partArea = view?.findViewById<TextView>(R.id.partArea)
    }
}
