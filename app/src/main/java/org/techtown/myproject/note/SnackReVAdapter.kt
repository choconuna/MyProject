package org.techtown.myproject.note

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
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogMealModel
import org.techtown.myproject.utils.DogSnackModel

class SnackReVAdapter(val dogSnackList : ArrayList<DogSnackModel>):
    RecyclerView.Adapter<SnackReVAdapter.SnackViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return dogSnackList.count()
    }

    override fun onBindViewHolder(holder: SnackViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        holder.timeSlotArea!!.text = dogSnackList[position].timeSlot
        holder.snackTypeArea!!.text = dogSnackList[position].snackType
        holder.snackNameArea!!.text = dogSnackList[position].snackName
        holder.snackWeightArea!!.text = dogSnackList[position].snackWeight
        holder.snackUnitArea!!.text = dogSnackList[position].snackUnit

        val imageFile = dogSnackList[position].snackImageFile
        if(imageFile != "") {
            val storageReference = Firebase.storage.reference.child(imageFile)
            val imageView = holder.view?.findViewById<ImageView>(R.id.snackImage)
            storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                if (task.isSuccessful) {
                    Glide.with(holder.view!!).load(task.result).into(imageView!!)
                } else {
                    holder.view!!.findViewById<ImageView>(R.id.snackImage).isVisible = false
                }
            })
        } else {
            holder.view!!.findViewById<ImageView>(R.id.snackImage).isVisible = false
        }

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnackViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.snack_list_item, parent, false)
        return SnackViewHolder(view)
    }

    inner class SnackViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val timeSlotArea = view?.findViewById<TextView>(R.id.timeSlotArea)
        val snackTypeArea = view?.findViewById<TextView>(R.id.snackTypeArea)
        val snackNameArea = view?.findViewById<TextView>(R.id.snackNameArea)
        val snackWeightArea = view?.findViewById<TextView>(R.id.snackWeightArea)
        val snackUnitArea = view?.findViewById<TextView>(R.id.snackUnitArea)
    }
}