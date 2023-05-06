package org.techtown.myproject.my

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView
import org.techtown.myproject.R
import org.techtown.myproject.utils.UserInfo

class BlockReVAdapter(val blockList : ArrayList<UserInfo>):
    RecyclerView.Adapter<BlockReVAdapter.BlockViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return blockList.count()
    }

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        holder.nickNameArea!!.text = blockList[position].nickName
        holder.emailArea!!.text = blockList[position].email

        val imageFile = blockList[position].profileImage
        if(imageFile != "") {
            val storageReference = Firebase.storage.reference.child(imageFile)
            val imageView = holder.view?.findViewById<ImageView>(R.id.imageView)
            storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                if (task.isSuccessful) {
                    Glide.with(holder.view!!).load(task.result).into(imageView!!)
                } else {
                    holder.view!!.findViewById<ImageView>(R.id.imageView).isVisible = false
                }
            })
        } else {
            holder.view!!.findViewById<ImageView>(R.id.imageView).isVisible = false
        }

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.block_list_item, parent, false)
        return BlockViewHolder(view)
    }

    inner class BlockViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val imageView = view?.findViewById<CircleImageView>(R.id.imageView)
        val emailArea = view?.findViewById<TextView>(R.id.emailArea)
        val nickNameArea = view?.findViewById<TextView>(R.id.nickNameArea)
    }
}
