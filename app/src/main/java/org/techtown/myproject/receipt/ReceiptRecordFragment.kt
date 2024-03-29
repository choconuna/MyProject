package org.techtown.myproject.receipt

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogCheckUpInputModel
import org.techtown.myproject.utils.DogCheckUpPictureModel
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.ReceiptModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class ReceiptRecordFragment : Fragment() {

    private val TAG = ReceiptRecordFragment::class.java.simpleName

    private lateinit var backMonth : Button
    private lateinit var nextMonth : Button
    private lateinit var dateArea : TextView

    private var selectedDate = LocalDate.now()
    private lateinit var year : String
    private lateinit var month : String
    private lateinit var day : String
    private lateinit var dayOfWeek : String

    private lateinit var myUid : String
    private lateinit var nowDate : String

    private lateinit var monthPriceArea : TextView

    private lateinit var writeBtn : ImageView

    private lateinit var receiptListView : RecyclerView
    private val receiptDataList = ArrayList<Receipt>() // 가계부 목록 리스트
    lateinit var receiptRVAdapter : ReceiptReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    private var receiptMap : MutableMap<Int, Receipt> = mutableMapOf()
    private var sortedMap : MutableMap<Int, Receipt> = mutableMapOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v : View? = inflater.inflate(R.layout.fragment_receipt_record, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        setData(v)

        backMonth.setOnClickListener {
            selectedDate = selectedDate.minusMonths(1)
            val format = "yyyy년 MM월"
            val sdf = DateTimeFormatter.ofPattern(format)
            dateArea.text = selectedDate.format(sdf)

            val datee = selectedDate.toString().split("-")
            year = datee[0]
            month = datee[1]
            day = datee[2]
            dayOfWeek = selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
            Log.d("selectedDate", "$year $month $day $dayOfWeek")

            showDate(myUid, year, month)
            showMonthPrice(myUid, year, month)
        }

        nextMonth.setOnClickListener {
            selectedDate = selectedDate.plusMonths(1)
            val format = "yyyy년 MM월"
            val sdf = DateTimeFormatter.ofPattern(format)
            dateArea.text = selectedDate.format(sdf)

            val datee = selectedDate.toString().split("-")
            year = datee[0]
            month = datee[1]
            day = datee[2]
            dayOfWeek = selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
            Log.d("selectedDate", "$year $month $day $dayOfWeek")

            showDate(myUid, year, month)
            showMonthPrice(myUid, year, month)
        }

        writeBtn.setOnClickListener {
            val intent = Intent(v!!.context, PlusReceiptRecordActivity::class.java)
            startActivity(intent)
        }

        return v
    }

    private fun setData(v : View?) {
        backMonth = v!!.findViewById(R.id.backMonth)
        nextMonth = v!!.findViewById(R.id.nextMonth)
        dateArea = v!!.findViewById(R.id.date)

        val cal: Calendar = Calendar.getInstance()
        val format = "yyyy년 MM월"
        val sdf = SimpleDateFormat(format)
        nowDate = sdf.format(cal.time)
        dateArea.text = nowDate

        val cal2 : Calendar = Calendar.getInstance()
        val formatt = "yyyy.MM"
        val sfd = SimpleDateFormat(formatt)
        val date2 = sfd.format(cal2.time).split(".")
        year = date2[0]
        month = date2[1]

        monthPriceArea = v!!.findViewById(R.id.monthPriceArea)
        writeBtn = v!!.findViewById(R.id.writeBtn)

        receiptRVAdapter = ReceiptReVAdapter(receiptDataList)
        receiptListView = v!!.findViewById(R.id.receiptRecyclerView)
        receiptListView.setItemViewCacheSize(20)
        receiptListView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        receiptListView.layoutManager = layoutManager
        receiptListView.adapter = receiptRVAdapter

        showDate(myUid, year, month)
        showMonthPrice(myUid, year, month)
    }

    private fun showDate(userId : String, year : String, month : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    receiptDataList.clear() // 똑같은 데이터 복사 출력되는 것 막기 위한 초기화
                    receiptMap.clear()
                    sortedMap.clear()

                    for(dataModel in dataSnapshot.children) {
                        Log.d(TAG, dataModel.toString())
                        // dataModel.key
                        val item = dataModel.getValue(ReceiptModel::class.java)
                        val date = item!!.date
                        val dateSp = date.split(".")
                        val nowYear = dateSp[0]
                        val nowMonth = dateSp[1]
                        val nowDate = dateSp[2]

                        if(year.toInt() == nowYear.toInt() && month.toInt() == nowMonth.toInt()) {
                            val receipt = Receipt(date, nowDate, dateSp[3])
                            receiptMap[nowDate.toInt()] = receipt
                            sortedMap = sortMapByKey(receiptMap)
//                            receiptDataList.add(receipt)
                        }
                    }

                    for((key, value) in sortedMap.entries) {
                        receiptDataList.add(value)
                        Log.d("sortedMap", value.toString())
                    }
                    receiptRVAdapter.notifyDataSetChanged() // 동기화

                    Log.d("receiptDataList", receiptDataList.toString())
                } catch (e: Exception) {
                    Log.d("receiptError", e.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.d("receiptError", databaseError.toString())
            }
        }
        FBRef.receiptRef.child(userId).addValueEventListener(postListener)
    }

    private fun showMonthPrice(userId : String, year : String, month : String) {
        val postListener = object : ValueEventListener { // 해당 일자의 총 지출액 표시
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    var dayPrice = 0

                    for(dataModel in dataSnapshot.children) {
                        // dataModel.key
                        val item = dataModel.getValue(ReceiptModel::class.java)
                        val date = item!!.date
                        val dateSp = date.split(".")

                        if(dateSp[0].toInt() == year.toInt() && dateSp[1].toInt() == month.toInt()) {
                            dayPrice += item!!.price.toInt()
                        }
                    }

                    val decimalFormat = DecimalFormat("#,###")
                    monthPriceArea!!.text = decimalFormat.format(dayPrice.toString().replace(",","").toDouble()) + "원"
                } catch (e: Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        FBRef.receiptRef.child(userId).addValueEventListener(postListener)
    }

    private fun sortMapByKey(map: MutableMap<Int, Receipt>): LinkedHashMap<Int, Receipt> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.key }

        val result = LinkedHashMap<Int, Receipt>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }
}