package org.techtown.myproject.community

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.techtown.myproject.R

class GalleryAdapter() : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    lateinit var imageList : ArrayList<Uri>
    lateinit var context : Context

    constructor(imageList : ArrayList<Uri>, context: Context) : this() {
        this.imageList = imageList
        this.context = context
    }

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryAdapter.ViewHolder {
        val inflater : LayoutInflater = LayoutInflater.from(parent.context)

        val view : View = inflater.inflate(R.layout.image_item_list, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryAdapter.ViewHolder, position: Int) {
        Glide.with(context).load(imageList[position]).into(holder.galleryView)

        holder.galleryView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    inner class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        val galleryView : ImageView = view.findViewById(R.id.galleryView)
    }
}