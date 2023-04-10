package org.techtown.myproject.receipt

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.ReceiptModel
import java.text.DecimalFormat

    class ReceiptReVAdapter(val receiptList : ArrayList<Receipt>):
    RecyclerView.Adapter<ReceiptReVAdapter.ReceiptViewHolder>() {

    private lateinit var receiptDetailListView : RecyclerView
    private val receiptDetailDataList = ArrayList<ReceiptModel>() // 가계부 목록 리스트
    lateinit var receiptDetailRVAdapter : ReceiptDetailReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

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

        receiptDetailRVAdapter = ReceiptDetailReVAdapter(receiptDetailDataList)
        receiptDetailListView = holder!!.receiptDetailReVAdapter!!
        receiptDetailListView.setItemViewCacheSize(20)
        receiptDetailListView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(holder!!.view!!.context, LinearLayoutManager.VERTICAL, false)
        receiptDetailListView.layoutManager = layoutManager
        receiptDetailListView.adapter = receiptDetailRVAdapter

        val selectedDate = receiptList[holder.adapterPosition].selectedDate
        val nowDate = receiptList[holder.adapterPosition].date
        val nowDay = receiptList[holder.adapterPosition].day
        val splitDate = selectedDate.split(".")

        holder.dateArea!!.text = nowDay
        holder.dayArea!!.text = nowDate + "요일"

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    receiptDetailDataList.clear() // 똑같은 데이터 복사 출력되는 것 막기 위한 초기화

                    for(dataModel in dataSnapshot.children) {
                        // dataModel.key
                        val item = dataModel.getValue(ReceiptModel::class.java)
                        val date = item!!.date
                        val dateSp = date.split(".")
                        val nowDate = dateSp[2]

                        if(nowDate.toInt() == nowDay.toInt() && dateSp[0].toInt() == splitDate[0].toInt() && dateSp[1].toInt() == splitDate[1].toInt()) {
                            Log.d("nowDate", nowDate)
                            val receipt = Receipt(date, nowDate, dateSp[3])
                            receiptDetailDataList.add(item!!)
                        }
                    }

                    receiptDetailRVAdapter.notifyDataSetChanged() // 동기화
                    Log.d("receiptDetailDataList", receiptDetailDataList.toString())
                } catch (e: Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        FBRef.receiptRef.child(myUid).addValueEventListener(postListener)

//        holder.category!!.text = receiptList[position].category
//        holder.contentArea!!.text = receiptList[position].content
//
//        val decimalFormat = DecimalFormat("#,###")
//        holder.priceArea!!.text = decimalFormat.format(receiptList[position].price.replace(",","").toDouble()) + "원"

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
//        val category = view?.findViewById<TextView>(R.id.category)
//        val contentArea = view?.findViewById<TextView>(R.id.contentArea)
//        val priceArea = view?.findViewById<TextView>(R.id.priceArea)
        val receiptDetailReVAdapter = view?.findViewById<RecyclerView>(R.id.receiptDetailRecyclerView)
    }
}