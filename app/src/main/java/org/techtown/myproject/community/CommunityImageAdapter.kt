package org.techtown.myproject.community

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.my.DogReVAdapter

class CommunityImageAdapter(val imageList : MutableList<String>) :
    RecyclerView.Adapter<CommunityImageAdapter.CoummunityImageViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityImageAdapter.CoummunityImageViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.community_image_list_item, parent, false)
        return CoummunityImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommunityImageAdapter.CoummunityImageViewHolder, position: Int) {
        Log.d("storageImage", imageList[position])
        Firebase.storage.reference.child(imageList[position]).downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if(task.isSuccessful) {
                Glide.with(holder.view!!).load(task.result).into(holder.gallerView!!) // 사진을 게시
            } else {
                holder.view?.findViewById<ImageView>(R.id.galleryView)!!.isVisible = false
            }
        })
    }

    override fun getItemCount(): Int {
        return imageList.count()
    }

    inner class CoummunityImageViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val gallerView = view?.findViewById<ImageView>(R.id.galleryView)
    }
}