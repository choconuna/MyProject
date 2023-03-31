package org.techtown.myproject.note

import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log
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
import org.techtown.myproject.my.DogReVAdapter
import org.techtown.myproject.utils.DogMealModel
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MealReVAdapter(val dogMealList : ArrayList<DogMealModel>):
    RecyclerView.Adapter<MealReVAdapter.MealViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return dogMealList.count()
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        holder.timeSlotArea!!.text = dogMealList[position].timeSlot
        holder.timeArea!!.text = dogMealList[position].time
        holder.mealTypeArea!!.text = dogMealList[position].mealType
        holder.mealNameArea!!.text = dogMealList[position].mealName
        holder.mealWeightArea!!.text = dogMealList[position].mealWeight

        val imageFile = dogMealList[position].mealImageFile
        if(imageFile != "") {
            val storageReference = Firebase.storage.reference.child(imageFile)
            val imageView = holder.view?.findViewById<ImageView>(R.id.mealImage)
            storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                if (task.isSuccessful) {
                    Glide.with(holder.view!!).load(task.result).into(imageView!!)
                } else {
                    holder.view!!.findViewById<ImageView>(R.id.mealImage).isVisible = false
                }
            })
        } else {
            holder.view!!.findViewById<ImageView>(R.id.mealImage).isVisible = false
        }

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.meal_list_item, parent, false)
        return MealViewHolder(view)
    }

    inner class MealViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val timeSlotArea = view?.findViewById<TextView>(R.id.timeSlotArea)
        val timeArea = view?.findViewById<TextView>(R.id.timeArea)
        val mealTypeArea = view?.findViewById<TextView>(R.id.mealTypeArea)
        val mealNameArea = view?.findViewById<TextView>(R.id.mealNameArea)
        val mealWeightArea = view?.findViewById<TextView>(R.id.mealWeightArea)
    }
}
