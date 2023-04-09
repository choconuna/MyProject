package org.techtown.myproject.receipt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.utils.ReceiptModel
import java.text.DecimalFormat

class ReceiptReVAdapter(val receiptList : ArrayList<ReceiptModel>):
    RecyclerView.Adapter<ReceiptReVAdapter.ReceiptViewHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return receiptList.count()
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        val nowDate = receiptList[position].date
        val splitDate = nowDate.split(".")

        holder.dateArea!!.text = splitDate[2]
        holder.dayArea!!.text = splitDate[3] + "요일"
        holder.category!!.text = receiptList[position].category
        holder.contentArea!!.text = receiptList[position].content

        val decimalFormat = DecimalFormat("#,###")
        holder.priceArea!!.text = decimalFormat.format(receiptList[position].price.replace(",","").toDouble()) + "원"

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.receipt_list_item, parent, false)
        return ReceiptViewHolder(view)
    }

    inner class ReceiptViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val dateArea = view?.findViewById<TextView>(R.id.dateArea)
        val dayArea = view?.findViewById<TextView>(R.id.dayArea)
        val category = view?.findViewById<TextView>(R.id.category)
        val contentArea = view?.findViewById<TextView>(R.id.contentArea)
        val priceArea = view?.findViewById<TextView>(R.id.priceArea)
    }
}