package org.techtown.myproject.community

import android.graphics.Color
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

class CommunityListVAdapter(val communityList : MutableList<CommunityModel>) : BaseAdapter() {
    override fun getCount(): Int {
        return communityList.size
    }

    override fun getItem(position: Int): Any {
        return communityList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView

        view = LayoutInflater.from(parent?.context).inflate(R.layout.community_list_item, parent, false)

        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        val itemLayoutView = view?.findViewById<LinearLayout>(R.id.itemView)
        if(communityList[position].uid == myUid) { // 내가 쓴 글과 남이 쓴 글을 구분하기 위해 내가 쓴 글 배경색을 다른 색으로 적용
            itemLayoutView?.setBackgroundColor(Color.parseColor("#FAEBD7"))
        } else {
            itemLayoutView?.setBackgroundColor(Color.parseColor("#FFFFFF"))
        }

        val writerUid = communityList[position].uid // 게시글 작성자
        val ref = FBRef.userRef
        val userName = FBRef.userRef.child(writerUid).child("nickName").get().addOnSuccessListener {
            view?.findViewById<TextView>(R.id.nickNameArea)!!.text = it.value.toString() // 게시글에 작성자의 아이디 표시
        }

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

        val communityId = communityList[position].communityId
        val count = communityList[position].count
        if(count.toInt() >= 1) {
            val imageView = view?.findViewById<ImageView>(R.id.communityImage)

            val storageReference = Firebase.storage.reference.child("communityImage/$communityId/$communityId"+"0.png") // 커뮤니티에 첨부된 사진을 DB의 storage로부터 가져옴 -> 첨부된 사진이 여러 장이라면, 제일 첫 번째 사진을 대표 사진으로 띄움

            storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                if(task.isSuccessful) {
                    Glide.with(view.context).load(task.result).into(imageView!!) // 유저의 profile 사진을 게시자 이름의 왼편에 표시함
                } else {
                    view?.findViewById<ImageView>(R.id.communityImage)!!.isVisible = false
                }
            })
        }

        val category = view?.findViewById<TextView>(R.id.categoryArea)
        category!!.text = communityList[position].category

        val title = view?.findViewById<TextView>(R.id.titleArea)
        title!!.text = communityList[position].title

        val content = view?.findViewById<TextView>(R.id.contentArea)
        content!!.text = communityList[position].content

        val time = view?.findViewById<TextView>(R.id.timeArea)
        time!!.text = communityList[position].time

        return view!!
    }
}