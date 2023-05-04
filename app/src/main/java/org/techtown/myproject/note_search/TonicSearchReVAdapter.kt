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
import org.techtown.myproject.note.TonicReVAdapter
import org.techtown.myproject.utils.DogTonicModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TonicSearchReVAdapter (val dogTonicList : ArrayList<DogTonicModel>):
    RecyclerView.Adapter<TonicSearchReVAdapter.TonicSearchViewHolder>() {

    override fun getItemCount(): Int {
        return dogTonicList.count()
    }

    override fun onBindViewHolder(holder: TonicSearchViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        val currentYear = Calendar.getInstance().get(Calendar.YEAR) // 현재 연도 가져오기

        val dateSp = dogTonicList[position].date.split(".")

        if(dateSp[1].length == 1 && dateSp[2].length == 1) {
            val inputFormat = SimpleDateFormat("yyyy.M.d", Locale.getDefault())
            val date = inputFormat.parse(dogTonicList[position].date)
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
            val date = inputFormat.parse(dogTonicList[position].date)
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
            val date = inputFormat.parse(dogTonicList[position].date)
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
            val date = inputFormat.parse(dogTonicList[position].date)
            val outputFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault())
            val outputDateStr = outputFormat.format(date!!)

            var outputDateSp = outputDateStr.split(" ")
            var outputDate = outputDateSp[0].split(".")

            if(currentYear == dateSp[0].toInt())
                holder!!.dateArea!!.text = outputDate[1] + "." + outputDate[2] + " " + outputDateSp[1]
            else
                holder!!.dateArea!!.text = outputDateSp[0]
        }

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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TonicSearchViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.tonic_search_list_item, parent, false)
        return TonicSearchViewHolder(view)
    }

    inner class TonicSearchViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val dateArea = view?.findViewById<TextView>(R.id.dateArea)
        val timeSlotArea = view?.findViewById<TextView>(R.id.timeSlotArea)
        val tonicPartArea = view?.findViewById<TextView>(R.id.tonicPartArea)
        val tonicNameArea = view?.findViewById<TextView>(R.id.tonicNameArea)
        val tonicWeightArea = view?.findViewById<TextView>(R.id.tonicWeightArea)
        val tonicUnitArea = view?.findViewById<TextView>(R.id.tonicUnitArea)
    }
}