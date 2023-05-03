package org.techtown.myproject.note_search

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
import org.techtown.myproject.utils.DogSnackModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SnackSearchReVAdapter(val dogSnackList : ArrayList<DogSnackModel>):
    RecyclerView.Adapter<SnackSearchReVAdapter.SnackSearchViewHolder>() {

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

    override fun onBindViewHolder(holder: SnackSearchViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        val currentYear = Calendar.getInstance().get(Calendar.YEAR) // 현재 연도 가져오기

        val dateSp = dogSnackList[position].date.split(".")

        if(dateSp[1].length == 1 && dateSp[2].length == 1) {
            val inputFormat = SimpleDateFormat("yyyy.M.d", Locale.getDefault())
            val date = inputFormat.parse(dogSnackList[position].date)
            val outputFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault())
            val outputDateStr = outputFormat.format(date!!)

            var outputDateSp = outputDateStr.split(" ")
            var outputDate = outputDateSp[0].split(".")

            if(currentYear == dateSp[0].toInt())
                holder!!.dateArea!!.text = outputDate[1] + "." + outputDate[2] + " " + outputDateSp[1]
            else
                holder!!.dateArea!!.text = outputDateSp[0]

        } else if(dateSp[1].length == 1 && dateSp[2].length == 2) {
            val inputFormat = SimpleDateFormat("yyyy.M.dd", Locale.getDefault())
            val date = inputFormat.parse(dogSnackList[position].date)
            val outputFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault())
            val outputDateStr = outputFormat.format(date!!)

            var outputDateSp = outputDateStr.split(" ")
            var outputDate = outputDateSp[0].split(".")

            if(currentYear == dateSp[0].toInt())
                holder!!.dateArea!!.text = outputDate[1] + "." + outputDate[2] + " " + outputDateSp[1]
            else
                holder!!.dateArea!!.text = outputDateSp[0]

        } else if(dateSp[1].length == 2 && dateSp[2].length == 1) {
            val inputFormat = SimpleDateFormat("yyyy.MM.d", Locale.getDefault())
            val date = inputFormat.parse(dogSnackList[position].date)
            val outputFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault())
            val outputDateStr = outputFormat.format(date!!)

            var outputDateSp = outputDateStr.split(" ")
            var outputDate = outputDateSp[0].split(".")

            if(currentYear == dateSp[0].toInt())
                holder!!.dateArea!!.text = outputDate[1] + "." + outputDate[2] + " " + outputDateSp[1]
            else
                holder!!.dateArea!!.text = outputDateSp[0]

        } else if(dateSp[1].length == 2 && dateSp[2].length == 2) {
            val inputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            val date = inputFormat.parse(dogSnackList[position].date)
            val outputFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault())
            val outputDateStr = outputFormat.format(date!!)

            var outputDateSp = outputDateStr.split(" ")
            var outputDate = outputDateSp[0].split(".")

            if(currentYear == dateSp[0].toInt())
                holder!!.dateArea!!.text = outputDate[1] + "." + outputDate[2] + " " + outputDateSp[1]
            else
                holder!!.dateArea!!.text = outputDateSp[0]
        }


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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnackSearchViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.snack_search_list_item, parent, false)
        return SnackSearchViewHolder(view)
    }

    inner class SnackSearchViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val dateArea = view?.findViewById<TextView>(R.id.dateArea)
        val timeSlotArea = view?.findViewById<TextView>(R.id.timeSlotArea)
        val snackTypeArea = view?.findViewById<TextView>(R.id.snackTypeArea)
        val snackNameArea = view?.findViewById<TextView>(R.id.snackNameArea)
        val snackWeightArea = view?.findViewById<TextView>(R.id.snackWeightArea)
        val snackUnitArea = view?.findViewById<TextView>(R.id.snackUnitArea)
    }
}