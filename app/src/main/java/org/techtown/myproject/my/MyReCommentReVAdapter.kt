package org.techtown.myproject.my

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.community.CommunityModel
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.ReCommentModel

class MyReCommentReVAdapter(val reCommentList : MutableList<ReCommentModel>) :
    RecyclerView.Adapter<MyReCommentReVAdapter.MyReCommentViewHolder>() {

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
    ): MyReCommentReVAdapter.MyReCommentViewHolder {
        val view =
            LayoutInflater.from(parent!!.context).inflate(R.layout.my_recomment_list_item, parent, false)
        return MyReCommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyReCommentReVAdapter.MyReCommentViewHolder, position: Int) {
        Log.d("reCommentList", reCommentList[position].toString())

        val writerUid = reCommentList[position].uid // 댓글 작성자 uid
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        val communityId = reCommentList[position].communityId // 댓글이 게시된 게시글의 id
        val ref = FBRef.userRef

        var categoryList : List<String> = listOf("정보", "후기", "자유", "질문", "거래")
        for(index in categoryList) {
            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {

                        for(dataModel in dataSnapshot.children) {
                            val item = dataModel.getValue(CommunityModel::class.java)
                            if (item!!.communityId == communityId) {
                                val userName = FBRef.userRef.child(item!!.uid).child("nickName").get().addOnSuccessListener {
                                    holder!!.writerNameArea!!.text = it.value.toString() // 게시글에 작성자의 아이디 표시
                                }
                            }
                        }
                    } catch(e : Exception) {
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            }
            FBRef.communityRef.child(index).addValueEventListener(postListener)
        }

        holder!!.contentArea!!.text = reCommentList[position].reCommentContent

        holder!!.timeArea!!.text = reCommentList[position].reCommentTime

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun getItemCount(): Int {
        return reCommentList.count()
    }

    inner class MyReCommentViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val writerNameArea = view?.findViewById<TextView>(R.id.writerNameArea)
        val contentArea = view?.findViewById<TextView>(R.id.contentArea)
        val timeArea = view?.findViewById<TextView>(R.id.timeArea)
    }
}
