package org.techtown.myproject.my

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.deal.DealInActivity
import org.techtown.myproject.utils.DealModel
import org.techtown.myproject.utils.FBRef
import java.util.*

class DealCompleteFragment : Fragment() {

    private val TAG = DealCompleteFragment::class.java.simpleName

    private lateinit var myUid : String

    lateinit var dealRecyclerView: RecyclerView

    private var dealMap : MutableMap<DealModel, Long> = mutableMapOf()
    private val dealList = ArrayList<DealModel>() // 거래 목록 리스트
    lateinit var dealRVAdapter : MyDealReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v : View? = inflater.inflate(R.layout.fragment_deal_complete, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        setData(v!!)

        getData()

        dealRVAdapter.setItemClickListener(object: MyDealReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val intent = Intent(v!!.context, DealInActivity::class.java)
                intent.putExtra("dealId", dealList[position].dealId)
                intent.putExtra("sellerId", dealList[position].sellerId)
                v!!.context.startActivity(intent)
            }
        })


        return v
    }

    private fun setData(v : View) {

        dealRecyclerView = v.findViewById(R.id.dealRecyclerView)
        dealRVAdapter = MyDealReVAdapter(dealList)
        dealRecyclerView.setItemViewCacheSize(20)
        dealRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        dealRecyclerView.layoutManager = layoutManager
        dealRecyclerView.adapter = dealRVAdapter
    }

    private fun getData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    dealList.clear()
                    dealMap.clear()

                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DealModel::class.java)

                        if (item!!.state == "거래 완료" && item!!.sellerId == myUid) { // 내가 판매 중인 데이터만

                            val date = item!!.date
                            val sp = date.split(" ")
                            val dateSp = sp[0].split(".")
                            val timeSp = sp[1].split(":")

                            var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                            dealMap[item!!] = dayNum.toLong()
                        }
                    }

                    val sortedDealMap = sortMapByKey(dealMap)
                    for((key, value) in dealMap.entries) {
                        dealList.add(key)
                    }

                    dealRVAdapter.notifyDataSetChanged()

                } catch (e: Exception) {
                    Log.d(TAG, "거래 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dealRef.addValueEventListener(postListener)
    }

    private fun sortMapByKey(map: MutableMap<DealModel, Long>): LinkedHashMap<DealModel, Long> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.value }

        val result = LinkedHashMap<DealModel, Long>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }
}