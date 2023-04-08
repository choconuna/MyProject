package org.techtown.myproject.statistics

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogCheckUpInputModel
import kotlin.math.roundToInt

class CheckUpInputStatisticsReVAdapter(val dogCheckUpInputStatisticsList : ArrayList<DogCheckUpInputModel>):
    RecyclerView.Adapter<CheckUpInputStatisticsReVAdapter.CheckUpInputStaticsViewHolder>() {

    override fun getItemCount(): Int {
        return dogCheckUpInputStatisticsList.count()
    }

    override fun onBindViewHolder(holder: CheckUpInputStaticsViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        holder.dateArea!!.text = dogCheckUpInputStatisticsList[position].date
        holder.maxArea!!.text = dogCheckUpInputStatisticsList[position].max
        holder.minArea!!.text = dogCheckUpInputStatisticsList[position].min
        holder.resultArea!!.text = dogCheckUpInputStatisticsList[position].result

        if(position != dogCheckUpInputStatisticsList.count() - 1) {
            holder.difference!!.visibility = VISIBLE
            var differenceValue = dogCheckUpInputStatisticsList[position].result.toFloat() - dogCheckUpInputStatisticsList[position+1].result.toFloat()
            val roundOff = (differenceValue * 100.0).roundToInt() / 100.0
            when {
                differenceValue == 0.toFloat() -> {
                    holder.difference!!.setTextColor(Color.parseColor("#000000"))
                    holder.difference!!.text = "-"
                }
                differenceValue > 0.toFloat() -> {
                    holder.difference!!.setTextColor(Color.parseColor("#DC143C"))
                    holder.difference!!.text = "+$roundOff"
                }
                differenceValue < 0.toFloat() -> {
                    holder.difference!!.setTextColor(Color.parseColor("#1E90FF"))
                    holder.difference!!.text = roundOff.toString()
                }
            }
        } else if(position == dogCheckUpInputStatisticsList.count() - 1) {
            holder.difference!!.visibility = INVISIBLE
        }

        if(dogCheckUpInputStatisticsList[position].result.toFloat() < dogCheckUpInputStatisticsList[position].min.toFloat()) {
            holder.stateImg!!.setImageResource(R.drawable.arrow_down)
            holder.state!!.text = "Low"
            holder.state!!.setTextColor(Color.parseColor("#1E90FF"))

            holder.resultArea!!.setTextColor(Color.parseColor("#1E90FF"))
        } else if(dogCheckUpInputStatisticsList[position].result.toFloat() > dogCheckUpInputStatisticsList[position].max.toFloat()) {
            holder.stateImg!!.setImageResource(R.drawable.arrow_up)
            holder.state!!.text = "High"
            holder.state!!.setTextColor(Color.parseColor("#DC143C"))

            holder.resultArea!!.setTextColor(Color.parseColor("#DC143C"))
        } else if(dogCheckUpInputStatisticsList[position].result.toFloat() >= dogCheckUpInputStatisticsList[position].min.toFloat() && dogCheckUpInputStatisticsList[position].result.toFloat() <= dogCheckUpInputStatisticsList[position].max.toFloat()) {
            holder.stateImg!!.setImageResource(R.drawable.minus)
            holder.state!!.text = "Normal"
            holder.state!!.setTextColor(Color.parseColor("#000000"))

            holder.resultArea!!.setTextColor(Color.parseColor("#000000"))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckUpInputStaticsViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.check_up_input_statistics_list_item, parent, false)
        return CheckUpInputStaticsViewHolder(view)
    }

    inner class CheckUpInputStaticsViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val dateArea = view?.findViewById<TextView>(R.id.dateArea)
        val difference = view?.findViewById<TextView>(R.id.difference)
        val stateImg = view?.findViewById<ImageView>(R.id.stateImg)
        val resultArea = view?.findViewById<TextView>(R.id.resultArea)
        val minArea = view?.findViewById<TextView>(R.id.minArea)
        val maxArea = view?.findViewById<TextView>(R.id.maxArea)
        val state = view?.findViewById<TextView>(R.id.state)
    }
}
