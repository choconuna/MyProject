package org.techtown.myproject.my

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef
import java.text.SimpleDateFormat
import java.util.*

class DogListVAdapter(val dogList : MutableList<DogModel>) : BaseAdapter() {
    override fun getCount(): Int {
        return dogList.size
    }

    override fun getItem(position: Int): Any {
        return dogList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView

        view = LayoutInflater.from(parent?.context).inflate(R.layout.dog_list_item_two, parent, false)

        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        val imageFile = dogList[position].dogProfileFile
        val storageReference = Firebase.storage.reference.child(imageFile)
        val imageView = view?.findViewById<ImageView>(R.id.dogImage)
        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if(task.isSuccessful) {
                Glide.with(view).load(task.result).into(imageView!!)
            } else {
                // view?.findViewById<ImageView>(R.id.communityImage)!!.isVisible = false
            }
        })

        val dogName = view?.findViewById<TextView>(R.id.dogName)
        dogName!!.text = dogList[position].dogName

        val sharedPreferences : SharedPreferences = view!!.context.getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        val isMainDogCheck = sharedPreferences.getString(myUid, "").toString()
        val isMainDog = view?.findViewById<ImageView>(R.id.isMainDog)
        if(isMainDogCheck.isNotEmpty()) { // 대표 반려견 표시
            Log.d("SharedPreferences", sharedPreferences.getString(myUid, "").toString())
            val mainDog = FBRef.dogRef.child(myUid).child(isMainDogCheck).child("dogName").get().addOnSuccessListener {
                Log.d("SharedPreferences", it.value.toString())
                if(it.value.toString() == dogList[position].dogName) {
                    isMainDog!!.setImageResource(R.drawable.red_heart)
                } else {
                    isMainDog!!.setImageResource(R.drawable.black_heart)
                }
            }
        }

        val dogSexArea = view?.findViewById<TextView>(R.id.dogSex)
        val dogSex = dogList[position].dogSex
        if(dogSex == "수컷") // 수컷, 암컷에 따라 글 색상을 다르게 표시
            dogSexArea!!.setTextColor(Color.parseColor("#6495ED"))
        else
            dogSexArea!!.setTextColor(Color.parseColor("#FA8072"))
        dogSexArea.text = dogSex

        val dogSpecies = view?.findViewById<TextView>(R.id.dogSpecies)
        dogSpecies!!.text = dogList[position].dogSpecies

        val dogBirthDate = view?.findViewById<TextView>(R.id.dogBirthDate)
        val dogBirth = dogList[position].dogBirthDate
        val sb = StringBuffer() // 입력된 생년월일이 20100409라면 2010.04.09로 변환하여 화면에 출력하기 위해 StringBuffer() 사용
        sb.append(dogBirth)
        sb.insert(4, ".")
        sb.insert(7, ".")
        dogBirthDate!!.text = sb

        return view!!
    }
}