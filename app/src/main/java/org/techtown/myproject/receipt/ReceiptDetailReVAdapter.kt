package org.techtown.myproject.receipt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        holder.category!!.text = receiptDetailList[position].category
        holder.contentArea!!.text = receiptDetailList[position].content

        val decimalFormat = DecimalFormat("#,###")
        holder.priceArea!!.text = decimalFormat.format(receiptDetailList[position].price.replace(",","").toDouble()) + "원"

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptDetailViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.receipt_detail_list_item, parent, false)
        return ReceiptDetailViewHolder(view)
    }

    inner class ReceiptDetailViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val category = view?.findViewById<TextView>(R.id.category)
        val contentArea = view?.findViewById<TextView>(R.id.contentArea)
        val priceArea = view?.findViewById<TextView>(R.id.priceArea)
    }
}