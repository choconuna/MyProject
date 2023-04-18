package org.techtown.myproject.walk

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogModel

class DogListReVAdapter(val dogList : ArrayList<DogModel>):
    RecyclerView.Adapter<DogListReVAdapter.DogViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return dogList.count()
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        val imageFile = dogList[position].dogProfileFile
        val storageReference = Firebase.storage.reference.child(imageFile)
        val imageView = holder.view?.findViewById<ImageView>(R.id.dogImage)
        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if(task.isSuccessful) {
                Glide.with(holder.view!!).load(task.result).into(imageView!!)
            } else {
                // view?.findViewById<ImageView>(R.id.communityImage)!!.isVisible = false
            }
        })

        val str = dogList[position].dogProfileFile
        val split = str.split("/", ".")
        Log.d("split", split.toString())


        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.dog_pic_list_item, parent, false)
        return DogViewHolder(view)
    }

    inner class DogViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val imageView = view?.findViewById<ImageView>(R.id.dogImage)
    }
}