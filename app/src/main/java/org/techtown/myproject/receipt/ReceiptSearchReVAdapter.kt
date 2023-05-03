package org.techtown.myproject.receipt

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.utils.ReceiptModel
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class ReceiptSearchReVAdapter(val receiptSearchList : ArrayList<ReceiptModel>):
    RecyclerView.Adapter<ReceiptSearchReVAdapter.ReceiptSearchDetailViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return receiptSearchList.count()
    }

    override fun onBindViewHolder(holder: ReceiptSearchDetailViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        val currentYear = Calendar.getInstance().get(Calendar.YEAR) // 현재 연도 가져오기

        val dateSp = receiptSearchList[position].date.split(".")
        if(currentYear == dateSp[0].toInt())
            holder!!.dateArea!!.text = dateSp[1] + "." + dateSp[2] + " (" + dateSp[3] + ")"
        else
            holder!!.dateArea!!.text = dateSp[0] + "." +  dateSp[1] + "." + dateSp[2]

        when(receiptSearchList[position].category) {
            "식비" -> holder.categoryImage!!.setImageResource(R.drawable.pet_food)
            "병원" -> holder.categoryImage!!.setImageResource(R.drawable.pet_hospital)
            "용품" -> holder.categoryImage!!.setImageResource(R.drawable.pet_goods)
            "교육" -> holder.categoryImage!!.setImageResource(R.drawable.pet_school)
            "미용" -> holder.categoryImage!!.setImageResource(R.drawable.pet_beauty)
            "교통" -> holder.categoryImage!!.setImageResource(R.drawable.pet_car)
            "여행" -> holder.categoryImage!!.setImageResource(R.drawable.pet_travel)
        }

        holder.category!!.text = receiptSearchList[position].category
        holder.contentArea!!.text = receiptSearchList[position].content

        holder.payMethodArea!!.text = receiptSearchList[position].payMethod
        holder.placeArea!!.text = receiptSearchList[position].place

        if(receiptSearchList[position].payMethod == "카드")
            holder.monthPlanArea!!.text = receiptSearchList[position].payMonthRole
        else if(receiptSearchList[position].payMethod == "현금")
            holder.monthPlanArea!!.visibility = View.GONE

        if(receiptSearchList[position].payMethod == "카드" && receiptSearchList[position].payMonthRole == "할부") {
            holder!!.divideMonth!!.visibility = View.VISIBLE
            holder!!.divideMonth!!.text = receiptSearchList[position].nowPayMonth + "/" + receiptSearchList[position].payMonth
        } else {
            holder!!.divideMonth!!.visibility = View.GONE
        }

        val decimalFormat = DecimalFormat("#,###")
        holder.priceArea!!.text = decimalFormat.format(receiptSearchList[position].price.replace(",","").toDouble()) + "원"

        holder.itemView.setOnClickListener {
            val intent = Intent(holder!!.view!!.context, ReceiptRecordEditActivity::class.java)
            intent.putExtra("key", receiptSearchList[position].receiptId)
            holder!!.view!!.context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptSearchDetailViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.receipt_search_list_item, parent, false)
        return ReceiptSearchDetailViewHolder(view)
    }

    inner class ReceiptSearchDetailViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val dateArea = view?.findViewById<TextView>(R.id.dateArea)
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