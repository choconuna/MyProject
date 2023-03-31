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
import org.techtown.myproject.utils.DogTonicModel

class TonicReVAdapter (val dogTonicList : ArrayList<DogTonicModel>):
    RecyclerView.Adapter<TonicReVAdapter.TonicViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return dogTonicList.count()
    }

    override fun onBindViewHolder(holder: TonicViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        holder.timeSlotArea!!.text = dogTonicList[position].timeSlot
        holder.tonicPartArea!!.text = dogTonicList[position].tonicPart
        holder.tonicNameArea!!.text = dogTonicList[position].tonicName
        holder.tonicWeightArea!!.text = dogTonicList[position].tonicWeight
        holder.tonicUnitArea!!.text = dogTonicList[position].tonicUnit

        val imageFile = dogTonicList[position].tonicImageFile
        if(imageFile != "") {
            val storageReference = Firebase.storage.reference.child(imageFile)
            val imageView = holder.view?.findViewById<ImageView>(R.id.tonicImage)
            storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                if (task.isSuccessful) {
                    Glide.with(holder.view!!).load(task.result).into(imageView!!)
                } else {
                    holder.view!!.findViewById<ImageView>(R.id.tonicImage).isVisible = false
                }
            })
        } else {
            holder.view!!.findViewById<ImageView>(R.id.tonicImage).isVisible = false
        }

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TonicViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.tonic_list_item, parent, false)
        return TonicViewHolder(view)
    }

    inner class TonicViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val timeSlotArea = view?.findViewById<TextView>(R.id.timeSlotArea)
        val tonicPartArea = view?.findViewById<TextView>(R.id.tonicPartArea)
        val tonicNameArea = view?.findViewById<TextView>(R.id.tonicNameArea)
        val tonicWeightArea = view?.findViewById<TextView>(R.id.tonicWeightArea)
        val tonicUnitArea = view?.findViewById<TextView>(R.id.tonicUnitArea)
    }
}