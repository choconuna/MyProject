package org.techtown.myproject.comment

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.community.SpecificCommunityEditActivity
import org.techtown.myproject.receipt.ReceiptDetailReVAdapter
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.ReCommentModel
import org.techtown.myproject.utils.ReceiptModel

class CommentReVAdapter(val commentList : MutableList<CommentModel>) :
    RecyclerView.Adapter<CommentReVAdapter.CommentViewHolder>() {

    private lateinit var reCommentRecyclerView : RecyclerView
    private val reCommentDataList = ArrayList<ReCommentModel>() // 대댓글 목록 리스트
    lateinit var reCommentRVAdapter : ReCommentReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    interface OnItemClickListener {
        fun onClick(v: View, position: Int) // 클릭 이벤트
        fun onEditClick(v : View, position : Int) // 수정 이벤트
        fun onDeleteClick(v: View?, position: Int) //삭제 이벤트
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentReVAdapter.CommentViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.comment_list_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentReVAdapter.CommentViewHolder, position: Int) {
        Log.d("commentList", commentList[position].toString())

        reCommentRVAdapter = ReCommentReVAdapter(reCommentDataList)
        reCommentRecyclerView = holder!!.reCommentRecyclerView!!
        reCommentRecyclerView.setItemViewCacheSize(20)
        reCommentRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(holder!!.view!!.context, LinearLayoutManager.VERTICAL, false)
        reCommentRecyclerView.layoutManager = layoutManager
        reCommentRecyclerView.adapter = reCommentRVAdapter

        val writerUid = commentList[position].uid // 댓글 작성자 uid
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        val communityId = commentList[position].communityId // 댓글이 게시된 게시글의 id
        val commentId = commentList[position].commentId
        val ref = FBRef.userRef

        val itemLayoutView = holder.view?.findViewById<LinearLayout>(R.id.itemView)
        if(writerUid == myUid) { // 댓글 작성자가 나일 경우
            itemLayoutView?.setBackgroundColor(Color.parseColor("#FAEBD7")) // 다른 댓글과 내 댓글 색 구분
            holder!!.commentSet!!.isVisible = true // 댓글 수정/삭제 버튼 보이게
            holder!!.commentSet!!.setOnClickListener { View ->
                val mDialogView = LayoutInflater.from(holder!!.view!!.context).inflate(R.layout.comment_custom_dialog, null)
                val mBuilder = AlertDialog.Builder(holder!!.view!!.context).setView(mDialogView)

                val alertDialog = mBuilder.show()
                val editBtn = alertDialog.findViewById<Button>(R.id.editBtn)
                editBtn?.setOnClickListener { // 수정 버튼 클릭 시
                    val intent = Intent(holder!!.view!!.context, CommentEditActivity::class.java)
                    intent.putExtra("communityId", commentList[position].communityId)
                    intent.putExtra("commentId", commentList[position].commentId)
                    holder!!.view!!.context.startActivity(intent) // 수정 페이지로 이동

                    alertDialog.dismiss()
                }

                val rmBtn = alertDialog.findViewById<Button>(R.id.rmBtn)
                rmBtn?.setOnClickListener {  // 삭제 버튼 클릭 시

                    val postListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            try {

                                for(dataModel in dataSnapshot.children) {
                                    val item = dataModel.getValue(ReCommentModel::class.java)
                                    FBRef.reCommentRef.child(communityId).child(commentId).child(item!!.reCommentId).removeValue() // 대댓글 삭제
                                }

                            } catch(e : Exception) {
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    }
                    FBRef.reCommentRef.child(communityId).child(commentList[position].commentId).addValueEventListener(postListener)

                    FBRef.commentRef.child(communityId).child(commentList[position].commentId).removeValue() // 댓글 삭제
                    Toast.makeText(holder!!.view!!.context, "댓글이 삭제되었습니다!", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                }
            }
        } else {
            itemLayoutView?.setBackgroundColor(Color.parseColor("#FFFFFF"))
        }

        val profileFile = FBRef.userRef.child(writerUid).child("profileImage").get().addOnSuccessListener {
            val storageReference = Firebase.storage.reference.child(it.value.toString()) // 유저의 profile 사진을 DB의 storage로부터 가져옴

            storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                if(task.isSuccessful && holder.view != null) {
                    Glide.with(holder.view!!).load(task.result).into(holder.profileImageArea!!) // 작성자의 profile 사진을 게시자 이름의 왼편에 표시함
                } else {
                    // holder.view?.findViewById<ImageView>(R.id.profileImageArea)?.isVisible = false
                }
            })
        }

        val userName = FBRef.userRef.child(writerUid).child("nickName").get().addOnSuccessListener {
            holder!!.nickNameArea!!.text = it.value.toString() // 댓글 작성자의 닉네임 표시
        }

        holder!!.contentArea!!.text = commentList[position].commentContent

        holder!!.timeArea!!.text = commentList[position].commentTime

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {

                    reCommentDataList.clear()

                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(ReCommentModel::class.java)
                        reCommentDataList.add(item!!)
                    }

                    Log.d("reCommentDataList", reCommentDataList.toString())
                    reCommentRVAdapter.notifyDataSetChanged() // 데이터 동기화
                } catch(e : Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        FBRef.reCommentRef.child(communityId).child(commentId).addValueEventListener(postListener)

        holder!!.reCommentBtn!!.setOnClickListener {
            val intent = Intent(holder!!.view!!.context, WriteReCommentActivity::class.java)
            intent.putExtra("communityId", commentList[position].communityId)
            intent.putExtra("commentId", commentList[position].commentId)
            holder!!.view!!.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return commentList.count()
    }

    inner class CommentViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val profileImageArea = view?.findViewById<ImageView>(R.id.profileImageArea)
        val nickNameArea = view?.findViewById<TextView>(R.id.nickNameArea)
        val contentArea = view?.findViewById<TextView>(R.id.contentArea)
        val timeArea = view?.findViewById<TextView>(R.id.timeArea)
        val commentSet = view?.findViewById<ImageView>(R.id.commentSet)
        val reCommentBtn = view?.findViewById<TextView>(R.id.reCommentBtn)
        val reCommentRecyclerView = view?.findViewById<RecyclerView>(R.id.reCommentRecyclerView)
    }
}