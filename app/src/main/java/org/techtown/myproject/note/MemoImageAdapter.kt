package org.techtown.myproject.note

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.community.CommunityImageAdapter

class MemoImageAdapter(val imageList : MutableList<String>) :
    RecyclerView.Adapter<MemoImageAdapter.MemoImageViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoImageAdapter.MemoImageViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.memo_image_list_item, parent, false)
        return MemoImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemoImageAdapter.MemoImageViewHolder, position: Int) {
        Log.d("storageImage", imageList[position])

        holder.progressBar!!.isVisible = true

        Firebase.storage.reference.child(imageList[position]).downloadUrl.addOnCompleteListener(
            OnCompleteListener { task ->
            if(task.isSuccessful) {
                Glide.with(holder.view!!).load(task.result).apply(RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)).into(holder.gallerView!!) // 사진을 게시
                holder.progressBar!!.isVisible = false
            } else {
                holder.view?.findViewById<ImageView>(R.id.galleryView)!!.isVisible = false
                holder.progressBar!!.isVisible = false
            }
        })

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun getItemCount(): Int {
        return imageList.count()
    }

    inner class MemoImageViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val progressBar = view?.findViewById<ProgressBar>(R.id.progressBar)
        val gallerView = view?.findViewById<ImageView>(R.id.galleryView)
    }
}