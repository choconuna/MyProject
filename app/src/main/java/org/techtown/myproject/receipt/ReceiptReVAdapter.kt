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

    private lateinit var receiptItemListView : RecyclerView
    private val receiptItemDataList = ArrayList<Receipt>() // 가계부 목록 리스트
    lateinit var receiptItemRVAdapter : ReceiptItemReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    override fun getItemCount(): Int {
        return receiptList.count()
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        receiptItemRVAdapter = ReceiptItemReVAdapter(receiptItemDataList)
        receiptItemListView = holder!!.receiptDetailReVAdapter!!
        receiptItemListView.setItemViewCacheSize(20)
        receiptItemListView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(holder!!.view!!.context, LinearLayoutManager.VERTICAL, false)
        receiptItemListView.layoutManager = layoutManager
        receiptItemListView.adapter = receiptItemRVAdapter

        val selectedDate = receiptList[position].selectedDate
        val nowDate = receiptList[position].date
        val nowDay = receiptList[position].day
        val splitDate = selectedDate.split(".")

        holder.dateArea!!.text = nowDay
        holder.dayArea!!.text = nowDate + "요일"

        receiptItemDataList.clear()
        receiptItemDataList.add(receiptList[position])
        receiptItemRVAdapter.notifyDataSetChanged()

        val postListener = object : ValueEventListener { // 해당 일자의 총 지출액 표시
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    var dayPrice = 0

                    for(dataModel in dataSnapshot.children) {
                        // dataModel.key
                        val item = dataModel.getValue(ReceiptModel::class.java)
                        val date = item!!.date
                        val dateSp = date.split(".")

                        if(dateSp[0].toInt() == splitDate[0].toInt() && dateSp[1].toInt() == splitDate[1].toInt() && dateSp[2].toInt() == splitDate[2].toInt()) {
                            dayPrice += item!!.price.toInt()
                        }
                    }

                    val decimalFormat = DecimalFormat("#,###")
                    holder.dayPriceArea!!.text = decimalFormat.format(dayPrice.toString().replace(",","").toDouble()) + "원"
                } catch (e: Exception) {
                    Log.d("receiptError", e.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("receiptError", databaseError.toString())
            }
        }
        FBRef.receiptRef.child(myUid).addValueEventListener(postListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.receipt_list_item, parent, false)
        return ReceiptViewHolder(view)
    }

    inner class ReceiptViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val dateArea = view?.findViewById<TextView>(R.id.dateArea)
        val dayArea = view?.findViewById<TextView>(R.id.dayArea)
        val dayPriceArea = view?.findViewById<TextView>(R.id.dayPriceArea)
        val receiptDetailReVAdapter = view?.findViewById<RecyclerView>(R.id.receiptDetailRecyclerView)
    }
}