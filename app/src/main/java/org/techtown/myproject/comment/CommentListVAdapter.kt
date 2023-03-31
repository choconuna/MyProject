package org.techtown.myproject.comment

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.utils.FBRef

class CommentListVAdapter(val commentList : MutableList<CommentModel>) : BaseAdapter() {
    override fun getCount(): Int {
        return commentList.size
    }

    override fun getItem(position: Int): Any {
        return commentList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView

        view = LayoutInflater.from(parent?.context).inflate(R.layout.comment_list_item, parent, false)

        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        val writerUid = commentList[position].uid // 댓글 작성자 uid
        val communityId = commentList[position].communityId // 댓글이 게시된 게시글의 id
        val ref = FBRef.userRef

        val profileFile = FBRef.userRef.child(writerUid).child("profileImage").get().addOnSuccessListener {
            val storageReference = Firebase.storage.reference.child(it.value.toString()) // 유저의 profile 사진을 DB의 storage로부터 가져옴

            val imageView = view?.findViewById<ImageView>(R.id.profileImageArea)

            storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                if(task.isSuccessful) {
                    Glide.with(view).load(task.result).into(imageView!!) // 유저의 profile 사진을 게시자 이름의 왼편에 표시함
                } else {
                    view?.findViewById<ImageView>(R.id.profileImageArea)!!.isVisible = false
                }
            })
        }

        val userName = FBRef.userRef.child(writerUid).child("userName").get().addOnSuccessListener {
            view?.findViewById<TextView>(R.id.nameArea)!!.text = it.value.toString() // 댓글 작성자의 이름을 표시
        }

        val title = view?.findViewById<TextView>(R.id.titleArea)
        title!!.text = commentList[position].commentContent

        val time = view?.findViewById<TextView>(R.id.timeArea)
        time!!.text = commentList[position].commentTime

        return view!!
    }

}