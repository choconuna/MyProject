package org.techtown.myproject.deal

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.community.CommunityImageAdapter

class DealImageReVAdapter(val imageList : MutableList<String>) :
    RecyclerView.Adapter<DealImageReVAdapter.DealImageViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DealImageReVAdapter.DealImageViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.deal_image_list_item, parent, false)
        return DealImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: DealImageReVAdapter.DealImageViewHolder, position: Int) {
        Log.d("storageImage", imageList[position])

        Firebase.storage.reference.child(imageList[position]).downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if(task.isSuccessful) {
                Glide.with(holder.view!!).load(task.result).into(holder.gallerView!!) // 사진을 게시
            } else {
                holder.view?.findViewById<ImageView>(R.id.galleryView)!!.isVisible = false
            }
        })

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun getItemCount(): Int {
        return imageList.count()
    }

    inner class DealImageViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val gallerView = view?.findViewById<ImageView>(R.id.galleryView)
    }
}