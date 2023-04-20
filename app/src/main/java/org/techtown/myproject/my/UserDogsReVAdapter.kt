package org.techtown.myproject.my

import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.EditText
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
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UserDogsReVAdapter(val dogList : ArrayList<DogModel>):
    RecyclerView.Adapter<UserDogsReVAdapter.UserDogsViewHolder>() {

    override fun getItemCount(): Int {
        return dogList.count()
    }

    override fun onBindViewHolder(holder: UserDogsViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        val imageFile = dogList[position].dogProfileFile
        val storageReference = Firebase.storage.reference.child(imageFile)
        val imageView = holder.view?.findViewById<ImageView>(R.id.dogImage)
        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful) {
                Glide.with(holder.view!!).load(task.result).into(imageView!!)
            } else {
                holder!!.imageView!!.visibility = GONE
            }
        })

        holder!!.dogName!!.text = dogList[position].dogName

        val str = dogList[position].dogProfileFile
        val split = str.split("/", ".")
        Log.d("split", split.toString())


        val dogSex = dogList[position].dogSex
        if (dogSex == "수컷") // 수컷, 암컷에 따라 글 색상을 다르게 표시
            holder!!.dogSexArea!!.setTextColor(Color.parseColor("#6495ED"))
        else
            holder!!.dogSexArea!!.setTextColor(Color.parseColor("#FA8072"))
        holder!!.dogSexArea!!.text = dogSex

        holder!!.dogSpecies!!.text = dogList[position].dogSpecies

        val dogBirth = dogList[position].dogBirthDate

        val sb = StringBuffer() // 입력된 생년월일이 20100409라면 2010.04.09로 변환하여 화면에 출력하기 위해 StringBuffer() 사용
        sb.append(dogList[position].dogBirthDate)
        sb.insert(4, ".")
        sb.insert(7, ".")
        holder!!.dogBirthDate!!.text = sb

        val formats_year = SimpleDateFormat("yyyy")
        val formats_month = SimpleDateFormat("MM")
        val timeYear: Int = formats_year.format(Calendar.getInstance().time).toInt()
        val birthYear: Int = dogBirth.substring(0, 4).toInt() // 생년월일의 연도를 가져옴
        val timeMonth: Int = formats_month.format(Calendar.getInstance().time).toInt()
        val birthMonth: Int = dogBirth.substring(4, 6).toInt() // 생년월일의 월을 가져옴
        var dogAge = if (birthMonth <= timeMonth) {
            (timeYear - birthYear).toString() + "살 " + (timeMonth - birthMonth).toString() + "개월"
        } else {
            (timeYear - birthYear - 1).toString() + "살 " + (12 + timeMonth - birthMonth).toString() + "개월"
        }
        holder!!.dogAgeArea!!.text = dogAge

        holder!!.dogWeight!!.text = dogList[position].dogWeight + "kg"

        val isNeutralization = dogList[position].neutralization
        holder!!.neutralization!!.text = dogList[position].neutralization
        if(isNeutralization == "YES")
            holder!!.neutralization!!.setTextColor(Color.parseColor("#87CEFA"))
        else if(isNeutralization == "NO")
            holder!!.neutralization!!.setTextColor(Color.parseColor("#F08080"))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserDogsViewHolder {
        val view =
            LayoutInflater.from(parent!!.context).inflate(R.layout.user_dog_list_item, parent, false)
        return UserDogsViewHolder(view)
    }

    inner class UserDogsViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val imageView = view?.findViewById<ImageView>(R.id.dogImage)
        val dogName = view?.findViewById<TextView>(R.id.dogName)
        val dogSexArea = view?.findViewById<TextView>(R.id.dogSex)
        val dogSpecies = view?.findViewById<TextView>(R.id.dogSpecies)
        val dogAgeArea = view?.findViewById<TextView>(R.id.dogAgeArea)
        val dogBirthDate = view?.findViewById<TextView>(R.id.dogBirthDate)
        val dogWeight = view?.findViewById<TextView>(R.id.dogWeight)
        val neutralization = view?.findViewById<TextView>(R.id.neutralization)
    }
}
