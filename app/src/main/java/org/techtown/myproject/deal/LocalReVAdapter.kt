package org.techtown.myproject.deal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.techtown.myproject.R

class LocalReVAdapter(val localList : ArrayList<String>) : RecyclerView.Adapter<LocalReVAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return localList.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder!!.textView.text = localList[position]

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.local_item_list, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val textView = itemView.findViewById<TextView>(R.id.textView)
    }
}