package org.techtown.myproject.statistics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R

class TonicStatisticsReVAdapter(val dogTonicNameList : ArrayList<String>):
    RecyclerView.Adapter<TonicStatisticsReVAdapter.TonicNameViewHolder>() {

    override fun getItemCount(): Int {
        return dogTonicNameList.count()
    }

    override fun onBindViewHolder(holder: TonicNameViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        holder.tonicNameArea!!.text = dogTonicNameList[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TonicNameViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.tonic_statistics_list_item, parent, false)
        return TonicNameViewHolder(view)
    }

    inner class TonicNameViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val tonicNameArea = view?.findViewById<TextView>(R.id.tonicNameArea)
    }
}