package org.techtown.myproject.statistics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R

class SnackStatisticsReVAdapter(val dogSnackNameList : ArrayList<String>):
    RecyclerView.Adapter<SnackStatisticsReVAdapter.SnackNameViewHolder>() {

    override fun getItemCount(): Int {
        return dogSnackNameList.count()
    }

    override fun onBindViewHolder(holder: SnackNameViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        holder.snackNameArea!!.text = dogSnackNameList[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnackNameViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.snack_statistics_list_item, parent, false)
        return SnackNameViewHolder(view)
    }

    inner class SnackNameViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val snackNameArea = view?.findViewById<TextView>(R.id.snackNameArea)
    }
}