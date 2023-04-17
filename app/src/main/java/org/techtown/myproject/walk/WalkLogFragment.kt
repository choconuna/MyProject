package org.techtown.myproject.walk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.my.DogListVAdapter
import org.techtown.myproject.my.DogReVAdapter
import org.techtown.myproject.note.*
import org.techtown.myproject.receipt.Receipt
import org.techtown.myproject.receipt.ReceiptReVAdapter
import org.techtown.myproject.receipt.ReceiptRecordFragment
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.ReceiptModel
import org.techtown.myproject.utils.WalkModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*


class WalkLogFragment : Fragment() {

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

    private lateinit var walkListView : RecyclerView
    private val walkDataList = ArrayList<WalkModel>() // 산책 목록 리스트
    lateinit var walkRVAdapter : WalkReVAdapter
    lateinit var wLayoutManager : RecyclerView.LayoutManager

    private var walkMap : MutableMap<Int, WalkModel> = mutableMapOf()
    private var sortedMap : MutableMap<Int, WalkModel> = mutableMapOf()

    lateinit var walkDogRecyclerView: RecyclerView
    private val walkDogReDataList = ArrayList<DogModel>() // 각 반려견의 프로필을 넣는 리스트
    private val dogKeyList = mutableListOf<String>() // 각 반려견의 키값을 넣는 리스트
    lateinit var dogListReVAdapter: DogListReVAdapter
    lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(R.layout.fragment_walk_log, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        setData(v!!)

        getFBDogData()

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
        }

        walkRVAdapter.setItemClickListener(object: WalkReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                showDialog(v, walkDataList[position].walkId)
            }
        })

        return v
    }

    private fun setData(v : View) {

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

        walkRVAdapter = WalkReVAdapter(walkDataList)
        walkListView = v!!.findViewById(R.id.walkRecyclerView)
        walkListView.setHasFixedSize(true)
        wLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        walkListView.layoutManager = wLayoutManager
        walkListView.adapter = walkRVAdapter

        dogListReVAdapter = DogListReVAdapter(walkDogReDataList)
        walkDogRecyclerView = v!!.findViewById(R.id.dogRecyclerView)
        walkDogRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        walkDogRecyclerView.layoutManager = layoutManager
        walkDogRecyclerView.adapter = dogListReVAdapter

        showDate(myUid, year, month)
    }

    private fun showDate(userId : String, year : String, month : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    walkDataList.clear() // 똑같은 데이터 복사 출력되는 것 막기 위한 초기화
                    walkMap.clear()
                    sortedMap.clear()

                    for(dataModel in dataSnapshot.children) {
                        Log.d(TAG, dataModel.toString())
                        // dataModel.key
                        val item = dataModel.getValue(WalkModel::class.java)
                        val date = item!!.date
                        val dateSp = date.split(".")
                        val nowYear = dateSp[0]
                        val nowMonth = dateSp[1]
                        val nowDate = dateSp[2]

                        val startTime = item!!.startTime
                        val startTimeSp = startTime.split(":")

                        val dateTime = (dateSp[2] + startTimeSp[0] + startTimeSp[1])
                        Log.d("dateTime", dateTime)

                        if(year.toInt() == nowYear.toInt() && month.toInt() == nowMonth.toInt()) {
                            walkMap[dateTime.toInt()] = item!!
                            sortedMap = sortMapByKey(walkMap)
                        }
                    }

                    for((key, value) in sortedMap.entries) {
                        walkDataList.add(value)
                        Log.d("sortedMap", value.toString())
                    }
                    walkRVAdapter.notifyDataSetChanged() // 동기화

                    Log.d("walkDataList", walkDataList.toString())
                } catch (e: Exception) {
                    Log.d(TAG, "산책 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.walkRef.child(userId).addValueEventListener(postListener)
    }

    private fun getFBDogData() { // 파이어베이스로부터 반려견 프로필 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                dogKeyList.clear()
                walkDogReDataList.clear()

                for(dataModel in dataSnapshot.children) {
                    Log.d(TAG, dataModel.toString())
                    val item = dataModel.getValue(DogModel::class.java)
                    walkDogReDataList.add(item!!)
                    dogKeyList.add(dataModel.key!!)
                    Log.d("key", dogKeyList.toString())
                }

                dogListReVAdapter.notifyDataSetChanged()
                // dogRVAdapter.notifyDataSetChanged() // 동기화

                Log.d(TAG, walkDogReDataList.toString())
                // Log.d(TAG, dogDataList.toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dogRef.child(myUid).addValueEventListener(postListener)
    }

    private fun showDialog(v : View, id : String) { // 산책 기록 수정/삭제를 위한 다이얼로그 띄우기
        val mDialogView = LayoutInflater.from(v.context).inflate(R.layout.walk_dialog, null)
        val mBuilder = AlertDialog.Builder(v.context).setView(mDialogView)

        val alertDialog = mBuilder.show()
        val yesBtn = alertDialog.findViewById<Button>(R.id.yesBtn)
        yesBtn?.setOnClickListener { // 예 버튼 클릭 시
            Log.d(TAG, "yes Button Clicked")

            FBRef.walkDogRef.child(myUid).child(id).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제
            FBRef.walkRef.child(myUid).child(id).removeValue()

            alertDialog.dismiss()
        }

        val noBtn = alertDialog.findViewById<Button>(R.id.noBtn)
        noBtn?.setOnClickListener {  // 아니오 버튼 클릭 시
            Log.d(TAG, "no Button Clicked")
            alertDialog.dismiss()
        }
    }


    private fun sortMapByKey(map: MutableMap<Int, WalkModel>): LinkedHashMap<Int, WalkModel> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.key }

        val result = LinkedHashMap<Int, WalkModel>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }
}