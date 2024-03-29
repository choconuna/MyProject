package org.techtown.myproject.comment

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.ReCommentModel

class ReCommentReVAdapter(val reCommentList : MutableList<ReCommentModel>) :
    RecyclerView.Adapter<ReCommentReVAdapter.ReCommentViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int) // 클릭 이벤트
        fun onEditClick(v: View, position: Int) // 수정 이벤트
        fun onDeleteClick(v: View?, position: Int) //삭제 이벤트
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener: OnItemClickListener

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReCommentReVAdapter.ReCommentViewHolder {
        val view =
            LayoutInflater.from(parent!!.context).inflate(R.layout.recomment_list_item, parent, false)
        return ReCommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReCommentReVAdapter.ReCommentViewHolder, position: Int) {
        Log.d("reCommentList", reCommentList[position].toString())

        val writerUid = reCommentList[position].uid // 댓글 작성자 uid
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        val communityId = reCommentList[position].communityId // 댓글이 게시된 게시글의 id
        val ref = FBRef.userRef

        val itemLayoutView = holder.view?.findViewById<LinearLayout>(R.id.itemView)
        if (writerUid == myUid) { // 댓글 작성자가 나일 경우
            itemLayoutView?.setBackgroundColor(Color.parseColor("#FAEBD7")) // 다른 댓글과 내 댓글 색 구분
            holder!!.reCommentSet!!.isVisible = true // 댓글 수정/삭제 버튼 보이게
            holder!!.reCommentSet!!.setOnClickListener {
                val mDialogView = LayoutInflater.from(holder!!.view!!.context)
                    .inflate(R.layout.recomment_custom_dialog, null)
                val mBuilder = AlertDialog.Builder(holder!!.view!!.context).setView(mDialogView)

                val alertDialog = mBuilder.show()
                val editBtn = alertDialog.findViewById<Button>(R.id.editBtn)
                editBtn?.setOnClickListener { // 수정 버튼 클릭 시
                    val intent = Intent(holder!!.view!!.context, CommentEditActivity::class.java)
                    intent.putExtra("communityId", reCommentList[position].communityId)
                    intent.putExtra("commentId", reCommentList[position].commentId)
                    intent.putExtra("reCommentId", reCommentList[position].reCommentId)
                    holder!!.view!!.context.startActivity(intent) // 수정 페이지로 이동
                    alertDialog.dismiss()
                }

                val rmBtn = alertDialog.findViewById<Button>(R.id.rmBtn)
                rmBtn?.setOnClickListener {  // 삭제 버튼 클릭 시
                    FBRef.reCommentRef.child(communityId).child(reCommentList[position].commentId).child(reCommentList[position].reCommentId)
                        .removeValue() // 댓글 삭제
                    Toast.makeText(holder!!.view!!.context, "댓글이 삭제되었습니다!", Toast.LENGTH_SHORT)
                        .show()
                    alertDialog.dismiss()
                }
            }
        } else {
            itemLayoutView?.setBackgroundColor(Color.parseColor("#FFFFFF"))
        }

        val profileFile =
            FBRef.userRef.child(writerUid).child("profileImage").get().addOnSuccessListener {
                val storageReference =
                    Firebase.storage.reference.child(it.value.toString()) // 유저의 profile 사진을 DB의 storage로부터 가져옴

                storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Glide.with(holder.view!!).load(task.result)
                            .into(holder.profileImageArea!!) // 작성자의 profile 사진을 게시자 이름의 왼편에 표시함
                    } else {
                        // holder.view?.findViewById<ImageView>(R.id.profileImageArea)?.isVisible = false
                    }
                })
            }

        val userName = FBRef.userRef.child(writerUid).child("nickName").get().addOnSuccessListener {
            holder!!.nickNameArea!!.text = it.value.toString() // 댓글 작성자의 닉네임 표시
        }

        holder!!.contentArea!!.text = reCommentList[position].reCommentContent

        holder!!.timeArea!!.text = reCommentList[position].reCommentTime
    }

    override fun getItemCount(): Int {
        return reCommentList.count()
    }

    inner class ReCommentViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val profileImageArea = view?.findViewById<ImageView>(R.id.profileImageArea)
        val nickNameArea = view?.findViewById<TextView>(R.id.nickNameArea)
        val contentArea = view?.findViewById<TextView>(R.id.contentArea)
        val timeArea = view?.findViewById<TextView>(R.id.timeArea)
        val reCommentSet = view?.findViewById<ImageView>(R.id.reCommentSet)
    }
}
