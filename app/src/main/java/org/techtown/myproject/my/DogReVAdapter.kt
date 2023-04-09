package org.techtown.myproject.my

import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DogReVAdapter(val dogList : ArrayList<DogModel>):
    RecyclerView.Adapter<DogReVAdapter.DogViewHolder>() {

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

        holder!!.dogName!!.text = dogList[position].dogName

        val str = dogList[position].dogProfileFile
        val split = str.split("/", ".")
        Log.d("split", split.toString())

        val sharedPreferences : SharedPreferences  = holder!!.view!!.context.getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        val isMainDogCheck = sharedPreferences.getString(myUid, "").toString()
        if(isMainDogCheck.isNotEmpty()) { // 대표 반려견 표시
            Log.d("SharedPreferences", sharedPreferences.getString(myUid, "").toString())
            val mainDog = FBRef.dogRef.child(myUid).child(isMainDogCheck).child("dogName").get().addOnSuccessListener {
                Log.d("SharedPreferences", it.value.toString())
                if(it.value.toString() == dogList[position].dogName) {
                    holder!!.isMainDog!!.setImageResource(R.drawable.red_heart)
                } else {
                    holder!!.isMainDog!!.setImageResource(R.drawable.black_heart)
                }
            }
        }

        val dogSex = dogList[position].dogSex
        if(dogSex == "수컷") // 수컷, 암컷에 따라 글 색상을 다르게 표시
            holder!!.dogSexArea!!.setTextColor(Color.parseColor("#6495ED"))
        else
            holder!!.dogSexArea!!.setTextColor(Color.parseColor("#FA8072"))
        holder!!.dogSexArea!!.text = dogSex

        holder!!.dogSpecies!!.text = dogList[position].dogSpecies

        val dogBirth = dogList[position].dogBirthDate
        val formats_year = SimpleDateFormat("yyyy")
        val formats_month = SimpleDateFormat("MM")
        val timeYear: Int = formats_year.format(Calendar.getInstance().time).toInt()
        val birthYear: Int = dogBirth.substring(0, 4).toInt() // 생년월일의 연도를 가져옴
        val timeMonth : Int = formats_month.format(Calendar.getInstance().time).toInt()
        val birthMonth : Int = dogBirth.substring(4, 6).toInt() // 생년월일의 월을 가져옴
        var dogAge = if(birthMonth <= timeMonth) {
            (timeYear - birthYear).toString() + "살 " + (timeMonth - birthMonth).toString() + "개월"
        } else {
            (timeYear - birthYear - 1).toString() + "살 " + (12 + timeMonth - birthMonth).toString() + "개월"
        }
        holder!!.dogBirthDate!!.text = dogAge

        holder!!.dogWeight!!.text = dogList[position].dogWeight + "kg"

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.dog_list_item, parent, false)
        return DogViewHolder(view)
    }

    inner class DogViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val isMainDog = view?.findViewById<ImageView>(R.id.isMainDog)
        val imageView = view?.findViewById<ImageView>(R.id.dogImage)
        val dogName = view?.findViewById<TextView>(R.id.dogName)
        val dogSexArea = view?.findViewById<TextView>(R.id.dogSex)
        val dogSpecies = view?.findViewById<TextView>(R.id.dogSpecies)
        val dogBirthDate = view?.findViewById<TextView>(R.id.dogAge)
        val dogWeight = view?.findViewById<TextView>(R.id.dogWeight)
    }
}