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

class ReceiptItemReVAdapter(val receiptList : ArrayList<Receipt>):
    RecyclerView.Adapter<ReceiptItemReVAdapter.ReceiptItemViewHolder>() {

    private lateinit var receiptDetailListView : RecyclerView
    private val receiptDetailDataList = ArrayList<ReceiptModel>() // 가계부 목록 리스트
    lateinit var receiptDetailRVAdapter : ReceiptDetailReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    override fun getItemCount(): Int {
        return receiptList.count()
    }

    override fun onBindViewHolder(holder: ReceiptItemViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        receiptDetailRVAdapter = ReceiptDetailReVAdapter(receiptDetailDataList)
        receiptDetailListView = holder!!.receiptDetailReVAdapter!!
        receiptDetailListView.setItemViewCacheSize(20)
        receiptDetailListView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(holder!!.view!!.context, LinearLayoutManager.VERTICAL, false)
        receiptDetailListView.layoutManager = layoutManager
        receiptDetailListView.adapter = receiptDetailRVAdapter

        val selectedDate = receiptList[position].selectedDate
        val splitDate = selectedDate.split(".")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    receiptDetailDataList.clear() // 똑같은 데이터 복사 출력되는 것 막기 위한 초기화

                    for(dataModel in dataSnapshot.children) {
                        // dataModel.key
                        val item = dataModel.getValue(ReceiptModel::class.java)
                        val date = item!!.date
                        val dateSp = date.split(".")

                        if(dateSp[0].toInt() == splitDate[0].toInt() && dateSp[1].toInt() == splitDate[1].toInt() && dateSp[2].toInt() == splitDate[2].toInt()) {
                            Log.d("receiptItem", item!!.date + " " + item!!.category + " " + item!!.content + " " + item!!.price)
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptItemViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.receipt_list, parent, false)
        return ReceiptItemViewHolder(view)
    }

    inner class ReceiptItemViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val receiptDetailReVAdapter = view?.findViewById<RecyclerView>(R.id.receiptDetailRecyclerView)
    }
}