package org.techtown.myproject.receipt

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.utils.ReceiptModel
import java.text.DecimalFormat

class ReceiptDetailReVAdapter(val receiptDetailList : ArrayList<ReceiptModel>):
    RecyclerView.Adapter<ReceiptDetailReVAdapter.ReceiptDetailViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return receiptDetailList.count()
    }

    override fun onBindViewHolder(holder: ReceiptDetailViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        when(receiptDetailList[position].category) {
            "식비" -> holder.categoryImage!!.setImageResource(R.drawable.pet_food)
            "병원" -> holder.categoryImage!!.setImageResource(R.drawable.pet_hospital)
            "용품" -> holder.categoryImage!!.setImageResource(R.drawable.pet_goods)
            "교육" -> holder.categoryImage!!.setImageResource(R.drawable.pet_school)
            "미용" -> holder.categoryImage!!.setImageResource(R.drawable.pet_beauty)
            "교통" -> holder.categoryImage!!.setImageResource(R.drawable.pet_car)
            "여행" -> holder.categoryImage!!.setImageResource(R.drawable.pet_travel)
        }

        holder.category!!.text = receiptDetailList[position].category
        holder.contentArea!!.text = receiptDetailList[position].content

        holder.payMethodArea!!.text = receiptDetailList[position].payMethod
        holder.placeArea!!.text = receiptDetailList[position].place

        if(receiptDetailList[position].payMethod == "카드")
            holder.monthPlanArea!!.text = receiptDetailList[position].payMonthRole
        else if(receiptDetailList[position].payMethod == "현금")
            holder.monthPlanArea!!.visibility = GONE

        if(receiptDetailList[position].payMethod == "카드" && receiptDetailList[position].payMonthRole == "할부") {
            holder!!.divideMonth!!.visibility = VISIBLE
            holder!!.divideMonth!!.text = receiptDetailList[position].nowPayMonth + "/" + receiptDetailList[position].payMonth
        } else {
            holder!!.divideMonth!!.visibility = GONE
        }

        val decimalFormat = DecimalFormat("#,###")
        holder.priceArea!!.text = decimalFormat.format(receiptDetailList[position].price.replace(",","").toDouble()) + "원"

        holder.itemView.setOnClickListener {
            val intent = Intent(holder!!.view!!.context, ReceiptRecordEditActivity::class.java)
            intent.putExtra("key", receiptDetailList[position].receiptId)
            holder!!.view!!.context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptDetailViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.receipt_detail_list_item, parent, false)
        return ReceiptDetailViewHolder(view)
    }

    inner class ReceiptDetailViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val categoryImage = view?.findViewById<ImageView>(R.id.categoryImage)
        val category = view?.findViewById<TextView>(R.id.category)
        val payMethodArea = view?.findViewById<TextView>(R.id.payMethodArea)
        val placeArea = view?.findViewById<TextView>(R.id.placeArea)
        val contentArea = view?.findViewById<TextView>(R.id.contentArea)
        val divideMonth = view?.findViewById<TextView>(R.id.divideMonth)
        val monthPlanArea = view?.findViewById<TextView>(R.id.monthPlanArea)
        val priceArea = view?.findViewById<TextView>(R.id.priceArea)
    }
}