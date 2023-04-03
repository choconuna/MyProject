package org.techtown.myproject.note

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogMedicineModel

class MedicineReVAdapter(val dogMeidicineList : ArrayList<DogMedicineModel>):
    RecyclerView.Adapter<MedicineReVAdapter.MedicineViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return dogMeidicineList.count()
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        holder.timeArea!!.text = dogMeidicineList[position].time
        holder.medicineNameArea!!.text = dogMeidicineList[position].medicineName

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.medicine_list_item, parent, false)
        return MedicineViewHolder(view)
    }

    inner class MedicineViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val timeArea = view?.findViewById<TextView>(R.id.timeArea)
        val medicineNameArea = view?.findViewById<TextView>(R.id.medicineNameArea)
    }
}