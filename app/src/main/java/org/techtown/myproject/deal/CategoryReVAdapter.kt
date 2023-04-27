package org.techtown.myproject.deal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.techtown.myproject.R

class CategoryReVAdapter(val localList : ArrayList<String>) : RecyclerView.Adapter<CategoryReVAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return localList.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder!!.textView.text = localList[position]

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.category_list_item, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val textView = itemView.findViewById<TextView>(R.id.textView)
    }
}