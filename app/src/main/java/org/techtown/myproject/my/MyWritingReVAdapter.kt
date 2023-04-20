package org.techtown.myproject.my

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.community.CommunityModel
import org.techtown.myproject.utils.FBRef

class MyWritingReVAdapter(val communityList : MutableList<CommunityModel>) :
    RecyclerView.Adapter<MyWritingReVAdapter.MyWritingViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyWritingReVAdapter.MyWritingViewHolder {
        val view =
            LayoutInflater.from(parent!!.context).inflate(R.layout.my_writing_list_item, parent, false)
        return MyWritingViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyWritingReVAdapter.MyWritingViewHolder, position: Int) {
        Log.d("communityList", communityList[position].toString())

        val writerUid = communityList[position].uid // 댓글 작성자 uid
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        val communityId = communityList[position].communityId // 댓글이 게시된 게시글의 id

        val ref = FBRef.userRef

        val count = communityList[position].count
        if(count.toInt() >= 1) {
            val imageView = holder!!.view?.findViewById<ImageView>(R.id.communityImage)

            val storageReference = Firebase.storage.reference.child("communityImage/$communityId/$communityId"+"0.png") // 커뮤니티에 첨부된 사진을 DB의 storage로부터 가져옴 -> 첨부된 사진이 여러 장이라면, 제일 첫 번째 사진을 대표 사진으로 띄움

            storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                if(task.isSuccessful) {
                    Glide.with(holder!!.view!!.context).load(task.result).into(imageView!!) // 커뮤니티 대표 사진을 게시자 이름의 왼편에 표시함
                } else {
                    holder!!.view?.findViewById<ImageView>(R.id.communityImage)!!.isVisible = false
                }
            })
        }

        holder!!.categoryArea!!.text = communityList[position].category

        holder!!.titleArea!!.text = communityList[position].title

        holder!!.contentArea!!.text = communityList[position].content

        holder!!.timeArea!!.text = communityList[position].time

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }


    override fun getItemCount(): Int {
        return communityList.count()
    }

    inner class MyWritingViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val categoryArea = view?.findViewById<TextView>(R.id.categoryArea)
        val titleArea = view?.findViewById<TextView>(R.id.titleArea)
        val contentArea = view?.findViewById<TextView>(R.id.contentArea)
        val timeArea = view?.findViewById<TextView>(R.id.timeArea)
    }
}
