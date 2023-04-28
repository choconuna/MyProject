package org.techtown.myproject.chat

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

class MessageImageAdapter(val imageList : MutableList<String>) :
    RecyclerView.Adapter<MessageImageAdapter.MessageImageViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageImageAdapter.MessageImageViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.message_image_list_item, parent, false)
        return MessageImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageImageAdapter.MessageImageViewHolder, position: Int) {
        Log.d("storageImage", imageList[position])
        Firebase.storage.reference.child(imageList[position]).downloadUrl.addOnCompleteListener(
            OnCompleteListener { task ->
            if(task.isSuccessful) {
                Glide.with(holder.view!!).load(task.result).into(holder.galleryView!!) // 사진을 게시
            } else {
                holder.view?.findViewById<ImageView>(R.id.galleryView)!!.isVisible = false
            }
        })

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onViewRecycled(holder: MessageImageViewHolder) { // glide가 해당 activity가 종료되어도 실행되어 오류가 발생. 그 오류를 해결하기 위한 코드
        Glide.with(holder.view!!).clear(holder.galleryView!!)
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int {
        return imageList.count()
    }

    inner class MessageImageViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val galleryView = view?.findViewById<ImageView>(R.id.galleryView)
    }
}