package org.techtown.myproject.receipt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.utils.ReceiptPieModel
import java.text.DecimalFormat

class MonthReceiptStatisticsPieReVAdapter(val receiptPieModel : ArrayList<ReceiptPieModel>):
    RecyclerView.Adapter<MonthReceiptStatisticsPieReVAdapter.ReceiptStatisticsPieViewHolder>() {

    override fun getItemCount(): Int {
        return receiptPieModel.count()
    }

    override fun onBindViewHolder(holder: ReceiptStatisticsPieViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        when(receiptPieModel[position].category) {
            "식비" -> holder.categoryImage!!.setImageResource(R.drawable.pet_food)
            "병원" -> holder.categoryImage!!.setImageResource(R.drawable.pet_hospital)
            "용품" -> holder.categoryImage!!.setImageResource(R.drawable.pet_goods)
            "교육" -> holder.categoryImage!!.setImageResource(R.drawable.pet_school)
            "미용" -> holder.categoryImage!!.setImageResource(R.drawable.pet_beauty)
            "교통" -> holder.categoryImage!!.setImageResource(R.drawable.pet_car)
            "여행" -> holder.categoryImage!!.setImageResource(R.drawable.pet_travel)
        }

        holder.percentArea!!.text = receiptPieModel[position].percent + "%"
        holder.categoryArea!!.text = receiptPieModel[position].category

        val decimalFormat = DecimalFormat("#,###")
        holder.priceArea!!.text = decimalFormat.format(receiptPieModel[position].price.replace(",","").toDouble()) + "원"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptStatisticsPieViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.receipt_pie_list_item, parent, false)
        return ReceiptStatisticsPieViewHolder(view)
    }

    inner class ReceiptStatisticsPieViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val categoryImage = view?.findViewById<ImageView>(R.id.categoryImage)
        val percentArea = view?.findViewById<TextView>(R.id.percentArea)
        val categoryArea = view?.findViewById<TextView>(R.id.categoryArea)
        val priceArea = view?.findViewById<TextView>(R.id.priceArea)
    }
}