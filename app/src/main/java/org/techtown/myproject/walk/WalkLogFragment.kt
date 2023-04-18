package org.techtown.myproject.walk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.selects.select
import org.techtown.myproject.R
import org.techtown.myproject.my.DogListVAdapter
import org.techtown.myproject.my.DogReVAdapter
import org.techtown.myproject.note.*
import org.techtown.myproject.receipt.Receipt
import org.techtown.myproject.receipt.ReceiptReVAdapter
import org.techtown.myproject.receipt.ReceiptRecordFragment
import org.techtown.myproject.utils.*
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*


class WalkLogFragment : Fragment() {

    private val TAG = WalkLogFragment::class.java.simpleName

    private var selectedDogId : String = "all"

    private lateinit var allImage : TextView

    private lateinit var startDateArea : TextView
    private lateinit var between : TextView
    private lateinit var endDateArea : TextView

    private lateinit var selectedMenu : String

    private var endDate = LocalDate.now() // 현재 날짜
    private lateinit var endYear : String
    private lateinit var endMonth : String
    private lateinit var endDay : String

    private lateinit var startDate : LocalDate
    private lateinit var startYear : String
    private lateinit var startMonth : String
    private lateinit var startDay : String

    private lateinit var spinner : Spinner

    private lateinit var backDate : Button
    private lateinit var nextDate : Button

    private lateinit var myUid : String
    private lateinit var nowDate : String

    private lateinit var dogNameArea :TextView
    private lateinit var alphaArea : TextView
    private lateinit var totalNumArea : TextView
    private lateinit var totalTimeArea : TextView
    private lateinit var totalDistanceArea : TextView

    private lateinit var walkListView : RecyclerView
    private val walkDataList = ArrayList<WalkModel>() // 산책 목록 리스트
    lateinit var walkRVAdapter : WalkReVAdapter
    lateinit var wLayoutManager : RecyclerView.LayoutManager

    private var walkMap : MutableMap<Long, WalkModel> = mutableMapOf()
    private var sortedMap : MutableMap<Long, WalkModel> = mutableMapOf()

    lateinit var walkDogRecyclerView: RecyclerView
    private val walkDogReDataList = ArrayList<DogModel>() // 각 반려견의 프로필을 넣는 리스트
    private val dogKeyList = mutableListOf<String>() // 각 반려견의 키값을 넣는 리스트
    lateinit var dogListReVAdapter: DogListReVAdapter
    lateinit var layoutManager: RecyclerView.LayoutManager

    private var dogWalkMap : MutableMap<Long, WalkDogModel> = mutableMapOf()
    private var dogSortedMap : MutableMap<Long, WalkDogModel> = mutableMapOf()

    private lateinit var dogWalkListView : RecyclerView
    private val dogWalkDataList = ArrayList<WalkDogModel>() // 반려견 산책 목록 리스트
    lateinit var dogWalkRVAdapter : SpecificWalkReVAdapter
    lateinit var dLayoutManager : RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(R.layout.fragment_walk_log, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        setData(v!!)

        getFBDogData()

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                selectedMenu = parent.getItemAtPosition(position).toString()
                setDate(v, selectedMenu)

                when(selectedMenu) {
                    "주별" -> {
                        setDate(v!!, selectedMenu)

                        if (selectedDogId == "all") {
                            showWeekData(myUid, startYear, startMonth, startDay, endYear, endMonth, endDay)
                            Log.d("partDate", "$startYear.$startMonth.$startDay $endYear.$endMonth.$endDay")
                            walkListView.visibility = VISIBLE
                            dogWalkListView.visibility = GONE
                        } else {
                            showWeekSpecificData(myUid, startYear, startMonth, startDay, endYear, endMonth, endDay, selectedDogId.trim())
                            Log.d("partDate", "$startYear.$startMonth.$startDay $endYear.$endMonth.$endDay")
                            walkListView.visibility = GONE
                            dogWalkListView.visibility = VISIBLE
                        }
                    }
                    "월별" -> {
                        setDate(v!!, selectedMenu)
                        if (selectedDogId == "all") {
                            showMonthData(myUid, endYear, endMonth)
                            Log.d("partDate", "$endYear.$endMonth")
                            walkListView.visibility = VISIBLE
                            dogWalkListView.visibility = GONE
                        } else {
                            showMonthSpecificData(myUid, endYear,  endMonth, selectedDogId.trim())
                            Log.d("partDate", "$endYear.$endMonth")
                            walkListView.visibility = GONE
                            dogWalkListView.visibility = VISIBLE
                        }
                    }
                    "연별" -> {
                        setDate(v!!, selectedMenu)

                        if (selectedDogId == "all") {
                            showYearData(myUid, endYear)
                            Log.d("partDate", "$endYear")
                            walkListView.visibility = VISIBLE
                            dogWalkListView.visibility = GONE
                        } else {
                            showYearSpecificData(myUid, endYear, selectedDogId.trim())
                            Log.d("partDate", "$endYear")
                            walkListView.visibility = GONE
                            dogWalkListView.visibility = VISIBLE
                        }
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        allImage.setOnClickListener {
            selectedDogId = "all"
            dogNameArea.visibility = GONE
            alphaArea.visibility = GONE

            when(selectedMenu) {
                "주별" -> {
                    setDate(v!!, selectedMenu)

                    showWeekData(myUid, startYear, startMonth, startDay, endYear, endMonth, endDay)
                    Log.d("partDate", "$startYear.$startMonth.$startDay $endYear.$endMonth.$endDay")
                    walkListView.visibility = VISIBLE
                    dogWalkListView.visibility = GONE
                }
                "월별" -> {
                    setDate(v!!, selectedMenu)

                    showMonthData(myUid, endYear, endMonth)
                    Log.d("partDate", "$endYear.$endMonth")
                    walkListView.visibility = VISIBLE
                    dogWalkListView.visibility = GONE
                }
                "연별" -> {
                    setDate(v!!, selectedMenu)

                    showYearData(myUid, endYear)
                    Log.d("partDate", "$endYear")
                    walkListView.visibility = VISIBLE
                    dogWalkListView.visibility = GONE
                }
            }

            Toast.makeText(v!!.context, "모든 산책 데이터를 불러옵니다.", Toast.LENGTH_SHORT).show()
        }

        backDate.setOnClickListener {
            when(selectedMenu) {
                "주별" -> {
                    endDate = endDate.minusWeeks(1) // 기준 날짜를 한주 전으로 설정
                    setDate(v!!, selectedMenu)

                    if (selectedDogId == "all") {
                        showWeekData(myUid, startYear, startMonth, startDay, endYear, endMonth, endDay)
                        Log.d("partDate", "$startYear.$startMonth.$startDay $endYear.$endMonth.$endDay")
                        walkListView.visibility = VISIBLE
                        dogWalkListView.visibility = GONE
                    } else {
                        showWeekSpecificData(myUid, startYear, startMonth, startDay, endYear, endMonth, endDay, selectedDogId.trim())
                        Log.d("partDate", "$startYear.$startMonth.$startDay $endYear.$endMonth.$endDay")
                        walkListView.visibility = GONE
                        dogWalkListView.visibility = VISIBLE
                    }
                }
                "월별" -> {
                    endDate = endDate.minusMonths(1) // 기준 날짜를 한달 전으로 설정
                    setDate(v!!, selectedMenu)
                    if (selectedDogId == "all") {
                        showMonthData(myUid, endYear, endMonth)
                        Log.d("partDate", "$endYear.$endMonth")
                        walkListView.visibility = VISIBLE
                        dogWalkListView.visibility = GONE
                    } else {
                        showMonthSpecificData(myUid, endYear,  endMonth, selectedDogId.trim())
                        Log.d("partDate", "$endYear.$endMonth")
                        walkListView.visibility = GONE
                        dogWalkListView.visibility = VISIBLE
                    }
                }
                "연별" -> {
                    endDate = endDate.minusYears(1) // 기준 날짜를 1년 전으로 설정
                    setDate(v!!, selectedMenu)

                    if (selectedDogId == "all") {
                        showYearData(myUid, endYear)
                        Log.d("partDate", "$endYear")
                        walkListView.visibility = VISIBLE
                        dogWalkListView.visibility = GONE
                    } else {
                        showYearSpecificData(myUid, endYear, selectedDogId.trim())
                        Log.d("partDate", "$endYear")
                        walkListView.visibility = GONE
                        dogWalkListView.visibility = VISIBLE
                    }
                }
            }
        }

        nextDate.setOnClickListener {
            when(selectedMenu) {
                "주별" -> {
                    endDate = endDate.plusWeeks(1) // 기준 날짜를 한주 후로 설정
                    setDate(v!!, selectedMenu)

                    if (selectedDogId == "all") {
                        showWeekData(myUid, startYear, startMonth, startDay, endYear, endMonth, endDay)
                        Log.d("partDate", "$startYear.$startMonth.$startDay $endYear.$endMonth.$endDay")
                        walkListView.visibility = VISIBLE
                        dogWalkListView.visibility = GONE
                    } else {
                        showWeekSpecificData(myUid, startYear, startMonth, startDay, endYear, endMonth, endDay, selectedDogId)
                        Log.d("partDate", "$startYear.$startMonth.$startDay $endYear.$endMonth.$endDay")
                        walkListView.visibility = GONE
                        dogWalkListView.visibility = VISIBLE
                    }
                }
                "월별" -> {
                    endDate = endDate.plusMonths(1) // 기준 날짜를 한달 후로 설정
                    setDate(v!!, selectedMenu)

                    if (selectedDogId == "all") {
                        showMonthData(myUid, endYear, endMonth)
                        Log.d("partDate", "$endYear.$endMonth")
                        walkListView.visibility = VISIBLE
                        dogWalkListView.visibility = GONE
                    } else {
                        showMonthSpecificData(myUid, endYear,  endMonth, selectedDogId)
                        Log.d("partDate", "$endYear.$endMonth")
                        walkListView.visibility = GONE
                        dogWalkListView.visibility = VISIBLE
                    }
                }
                "연별" -> {
                    endDate = endDate.plusYears(1) // 기준 날짜를 1년 후로 설정
                    setDate(v!!, selectedMenu)

                    if (selectedDogId == "all") {
                        showYearData(myUid, endYear)
                        Log.d("partDate", "$endYear.$endMonth")
                        walkListView.visibility = VISIBLE
                        dogWalkListView.visibility = GONE
                    } else {
                        showYearSpecificData(myUid, endYear, selectedDogId)
                        Log.d("partDate", "$endYear")
                        walkListView.visibility = GONE
                        dogWalkListView.visibility = VISIBLE
                    }
                }
            }
        }

        dogListReVAdapter.setItemClickListener(object: DogListReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                selectedDogId = walkDogReDataList[position].dogId.trim()
                dogNameArea.text = walkDogReDataList[position].dogName.trim()
                alphaArea.visibility = VISIBLE
                dogNameArea.visibility = VISIBLE
                Toast.makeText(v!!.context, "특정 산책 데이터를 불러옵니다.", Toast.LENGTH_SHORT).show()

                when(selectedMenu) {
                    "주별" -> {
                        setDate(v!!, selectedMenu)

                        showWeekSpecificData(myUid, startYear, startMonth, startDay, endYear, endMonth, endDay, selectedDogId)
                        Log.d("partDate", "$startYear.$startMonth.$startDay $endYear.$endMonth.$endDay")
                        walkListView.visibility = GONE
                        dogWalkListView.visibility = VISIBLE
                    }
                    "월별" -> {
                        setDate(v!!, selectedMenu)

                        showMonthSpecificData(myUid, endYear,  endMonth, selectedDogId)
                        Log.d("partDate", "$endYear.$endMonth")
                        walkListView.visibility = GONE
                        dogWalkListView.visibility = VISIBLE
                    }
                    "연별" -> {
                        setDate(v!!, selectedMenu)

                        showYearSpecificData(myUid, endYear, selectedDogId)
                        Log.d("partDate", "$endYear")
                        walkListView.visibility = GONE
                        dogWalkListView.visibility = VISIBLE
                    }
                }
            }
        })

        walkRVAdapter.setItemClickListener(object: WalkReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                showDialog(v, walkDataList[position].walkId.trim())
            }
        })

        dogWalkRVAdapter.setItemClickListener(object: SpecificWalkReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                showDogDialog(v, dogWalkDataList[position].dogId.trim(), dogWalkDataList[position].walkId.trim(), dogWalkDataList[position].dogWalkId.trim())
            }
        })

        return v
    }

    private fun setData(v : View) {

        allImage = v!!.findViewById(R.id.allImage)

        spinner = v!!.findViewById(R.id.spinner)

        dogNameArea = v!!.findViewById(R.id.dogNameArea)
        alphaArea = v!!.findViewById(R.id.alphaArea)
        totalNumArea = v!!.findViewById(R.id.totalNumArea)
        totalDistanceArea = v!!.findViewById(R.id.totalDistanceArea)
        totalTimeArea = v!!.findViewById(R.id.totalTimeArea)

        backDate = v!!.findViewById(R.id.backDate)
        nextDate = v!!.findViewById(R.id.nextDate)
        startDateArea = v!!.findViewById(R.id.startDateArea)
        endDateArea = v!!.findViewById(R.id.endDateArea)
        between = v!!.findViewById(R.id.between )

        walkRVAdapter = WalkReVAdapter(walkDataList)
        walkListView = v!!.findViewById(R.id.walkRecyclerView)
        walkListView.setHasFixedSize(true)
        wLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        walkListView.layoutManager = wLayoutManager
        walkListView.adapter = walkRVAdapter

        dogWalkRVAdapter = SpecificWalkReVAdapter(dogWalkDataList)
        dogWalkListView = v!!.findViewById(R.id.dogWalkRecyclerView)
        dogWalkListView.setHasFixedSize(true)
        dLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        dogWalkListView.layoutManager = dLayoutManager
        dogWalkListView.adapter = dogWalkRVAdapter

        dogListReVAdapter = DogListReVAdapter(walkDogReDataList)
        walkDogRecyclerView = v!!.findViewById(R.id.dogRecyclerView)
        walkDogRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        walkDogRecyclerView.layoutManager = layoutManager
        walkDogRecyclerView.adapter = dogListReVAdapter

        setDate(v!!, "주별")
        showWeekData(myUid, startYear, startMonth, startDay, endYear, endMonth, endDay)
        Log.d("partDate", "$startYear.$startMonth.$startDay $endYear.$endMonth.$endDay")
        walkListView.visibility = VISIBLE
        dogWalkListView.visibility = GONE
    }

    private fun setDate(v : View, selectedMenu : String) {

        when(selectedMenu) {
            "주별" -> {
                week(endDate.toString())

                endDateArea.visibility = VISIBLE

                val endDateSplit = endDateArea.text.toString().split(".")
                endYear = endDateSplit[0]
                endMonth = endDateSplit[1]
                endDay = endDateSplit[2]

                between.visibility = VISIBLE

                startDateArea.visibility = VISIBLE

                val startDateSplit = startDateArea.text.toString().split(".")
                startYear = startDateSplit[0]
                startMonth = startDateSplit[1]
                startDay = startDateSplit[2]

            }
            "월별" -> {
                val format = "yyyy년 MM월"
                val formatt = "yyyy.MM"
                val sdf = DateTimeFormatter.ofPattern(format)
                val sfd = DateTimeFormatter.ofPattern(formatt)
                endDateArea.text = endDate.format(sdf)
                endDateArea.visibility = VISIBLE

                val endDateSplit = endDate.format(sfd).toString().split(".")
                endYear = endDateSplit[0]
                endMonth = endDateSplit[1]

                between.visibility = GONE
                startDateArea.visibility = GONE
            }
            "연별" -> {
                val format = "yyyy년"
                val sdf = DateTimeFormatter.ofPattern(format)
                endDateArea.text = endDate.format(sdf)
                endDateArea.visibility = VISIBLE

                val endDateSplit = endDateArea.text.toString().split("년")
                endYear = endDateSplit[0]

                between.visibility = GONE
                startDateArea.visibility = GONE
            }
        }
    }

    private fun showWeekData(userId : String, startYear : String, startMonth : String, startDay : String, endYear : String, endMonth : String, endDay : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    walkDataList.clear() // 똑같은 데이터 복사 출력되는 것 막기 위한 초기화
                    walkMap.clear()
                    sortedMap.clear()

                    var totalNum = 0
                    var totalDistance = 0.0
                    var totalHour = 0
                    var totalMinute = 0
                    var totalSecond = 0

                    for(dataModel in dataSnapshot.children) {
                        Log.d(TAG, dataModel.toString())
                        // dataModel.key
                        val item = dataModel.getValue(WalkModel::class.java)
                        val date = item!!.date
                        val dateSp = date.split(".")
                        val nowYear = dateSp[0]
                        val nowMonth = dateSp[1]
                        val nowDay = dateSp[2]

                        val startTime = item!!.startTime
                        val startTimeSp = startTime.split(":")

                        val dateTime = (dateSp[0] + dateSp[1] + dateSp[2] + startTimeSp[0] + startTimeSp[1])
                        Log.d("dateTime", dateTime)

                        val timeSp = item!!.time.split(":")

                        if(startYear.toInt() == endYear.toInt() && startYear.toInt() == nowYear.toInt()) { // 일주일의 시작과 끝이 같은 연도일 경우
                            if(startMonth.toInt() == endMonth.toInt() && startMonth.toInt() == nowMonth.toInt()) { // 일주일의 시작과 끝이 같은 달일 경우
                                if(nowDay.toInt() >= startDay.toInt() && nowDay.toInt() <= endDay.toInt()) { // 일주일 사이에 기록된 산책 날짜 데이터 추가
                                    walkMap[dateTime.toLong()] = item!!
                                    totalNum += 1
                                    totalDistance += item!!.distance.toFloat()
                                    totalHour += timeSp[0].toInt()
                                    totalMinute += timeSp[1].toInt()
                                    totalSecond += timeSp[2].toInt()
                                }
                            } else if(startMonth.toInt() < endMonth.toInt()) { // 일주일의 시작과 끝이 다른 달일 경우
                                if(startMonth.toInt() == nowMonth.toInt()) { // 일주일의 시작인 달과 같은 달일 경우
                                    if(nowDay.toInt() >= startDay.toInt()) {
                                        walkMap[dateTime.toLong()] = item!!
                                        totalNum += 1
                                        totalDistance += item!!.distance.toFloat()
                                        totalHour += timeSp[0].toInt()
                                        totalMinute += timeSp[1].toInt()
                                        totalSecond += timeSp[2].toInt()
                                    }
                                } else if(nowMonth.toInt() == endMonth.toInt()) { // 일주일의 끝인 달과 같은 달일 경우
                                    if(nowDay.toInt() <= endDay.toInt()) {
                                        walkMap[dateTime.toLong()] = item!!
                                        totalNum += 1
                                        totalDistance += item!!.distance.toFloat()
                                        totalHour += timeSp[0].toInt()
                                        totalMinute += timeSp[1].toInt()
                                        totalSecond += timeSp[2].toInt()
                                    }
                                }
                            }
                        } else if(startYear.toInt() < endYear.toInt()) { // 일주일의 시작과 끝이 다른 연도일 경우
                            if(startMonth.toInt() == nowMonth.toInt()) { // 일주일의 시작인 달과 같은 달일 경우
                                if(nowDay.toInt() >= startDay.toInt()) {
                                    walkMap[dateTime.toLong()] = item!!
                                    totalNum += 1
                                    totalDistance += item!!.distance.toFloat()
                                    totalHour += timeSp[0].toInt()
                                    totalMinute += timeSp[1].toInt()
                                    totalSecond += timeSp[2].toInt()
                                }
                            } else if(nowMonth.toInt() == endMonth.toInt()) { // 일주일의 끝인 달과 같은 달일 경우
                                if(nowDay.toInt() <= endDay.toInt()) {
                                    walkMap[dateTime.toLong()] = item!!
                                    totalNum += 1
                                    totalDistance += item!!.distance.toFloat()
                                    totalHour += timeSp[0].toInt()
                                    totalMinute += timeSp[1].toInt()
                                    totalSecond += timeSp[2].toInt()
                                }
                            }
                        }
                    }

                    totalNumArea.text = totalNum.toString()

                    val df = DecimalFormat("#.##")
                    df.roundingMode = RoundingMode.DOWN
                    val roundoff = df.format(totalDistance)
                    totalDistanceArea.text = roundoff + "km"

                    var totalTime = ""
                    var hourStr = ""
                    var minuteStr = ""
                    var secondStr =""

                    if(totalSecond > 59) {
                        totalMinute += 1
                        totalSecond -= 60
                    }
                    if(totalMinute > 59) {
                        totalMinute -= 60
                        totalHour += 1
                    }

                    hourStr = if(totalHour < 10)
                        "0$totalHour"
                    else
                        totalHour.toString()

                    minuteStr = if(totalMinute < 10)
                        "0$totalMinute"
                    else
                        totalMinute.toString()

                    secondStr = if(totalSecond < 10)
                        "0$totalSecond"
                    else
                        totalSecond.toString()

                    totalTimeArea.text = "$hourStr:$minuteStr:$secondStr"

                    sortedMap = sortMapByKey(walkMap)

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

    private fun showWeekSpecificData(userId : String, startYear : String, startMonth : String, startDay : String, endYear : String, endMonth : String, endDay : String, dogId : String) {

        Log.d("getDate", "$userId$dogId")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    dogWalkDataList.clear() // 똑같은 데이터 복사 출력되는 것 막기 위한 초기화
                    dogWalkMap.clear()
                    dogSortedMap.clear()

                    var totalNum = 0
                    var totalDistance = 0.0
                    var totalHour = 0
                    var totalMinute = 0
                    var totalSecond = 0

                    for(dataModel in dataSnapshot.children) {
                        Log.d(TAG, dataModel.toString())
                        // dataModel.key
                        val item = dataModel.getValue(WalkDogModel::class.java)
                        val date = item!!.date
                        val dateSp = date.split(".")
                        val nowYear = dateSp[0]
                        val nowMonth = dateSp[1]
                        val nowDay = dateSp[2]

                        val startTime = item!!.startTime
                        val startTimeSp = startTime.split(":")

                        val dateTime = (dateSp[0] + dateSp[1] + dateSp[2] + startTimeSp[0] + startTimeSp[1])
                        Log.d("dateTime", dateTime)

                        val timeSp = item!!.time.split(":")

                        if(nowYear.toInt() == startYear.toInt() && nowYear.toInt() == endYear.toInt()) { // 일주일 전이 같은 연도일 경우
                            if(nowMonth.toInt() == startMonth.toInt() && nowMonth.toInt() == endMonth.toInt()) { // 일주일 전이 같은 달일 경우
                                if(nowDay.toInt() in startDay.toInt()..endDay.toInt()) {
                                    dogWalkMap[dateTime.toLong()] = item!!
                                    totalNum += 1
                                    totalDistance += item!!.distance.toFloat()
                                    totalHour += timeSp[0].toInt()
                                    totalMinute += timeSp[1].toInt()
                                    totalSecond += timeSp[2].toInt()
                                }
                            } else if(nowMonth.toInt() < endMonth.toInt() && nowMonth.toInt() == startMonth.toInt()) { // 일주일 전이 전달일 경우
                                if(nowDay.toInt() >= startDay.toInt()) {
                                    dogWalkMap[dateTime.toLong()] = item!!
                                    totalNum += 1
                                    totalDistance += item!!.distance.toFloat()
                                    totalHour += timeSp[0].toInt()
                                    totalMinute += timeSp[1].toInt()
                                    totalSecond += timeSp[2].toInt()
                                }
                            } else if(nowMonth.toInt() == endMonth.toInt() && nowMonth.toInt() > startMonth.toInt()) { // 일주일 전이 이번달일 경우
                                if(nowDay.toInt() <= endDay.toInt()) {
                                    dogWalkMap[dateTime.toLong()] = item!!
                                    totalNum += 1
                                    totalDistance += item!!.distance.toFloat()
                                    totalHour += timeSp[0].toInt()
                                    totalMinute += timeSp[1].toInt()
                                    totalSecond += timeSp[2].toInt()
                                }
                            }
                        } else if((nowYear.toInt() < endYear.toInt()) && nowYear.toInt() == endYear.toInt()) { // 일주일 전이 전년도일 경우
                            if(nowMonth.toInt() == startMonth.toInt() && nowDay.toInt() >= startDay.toInt()) {
                                dogWalkMap[dateTime.toLong()] = item!!
                                totalNum += 1
                                totalDistance += item!!.distance.toFloat()
                                totalHour += timeSp[0].toInt()
                                totalMinute += timeSp[1].toInt()
                                totalSecond += timeSp[2].toInt()
                            }
                        }
                    }

                    totalNumArea.text = totalNum.toString()

                    val df = DecimalFormat("#.##")
                    df.roundingMode = RoundingMode.DOWN
                    val roundoff = df.format(totalDistance)
                    totalDistanceArea.text = roundoff + "km"

                    var totalTime = ""
                    var hourStr = ""
                    var minuteStr = ""
                    var secondStr =""

                    if(totalSecond > 59) {
                        totalMinute += 1
                        totalSecond -= 60
                    }
                    if(totalMinute > 59) {
                        totalMinute -= 60
                        totalHour += 1
                    }

                    hourStr = if(totalHour < 10)
                        "0$totalHour"
                    else
                        totalHour.toString()

                    minuteStr = if(totalMinute < 10)
                        "0$totalMinute"
                    else
                        totalMinute.toString()

                    secondStr = if(totalSecond < 10)
                        "0$totalSecond"
                    else
                        totalSecond.toString()

                    totalTimeArea.text = "$hourStr:$minuteStr:$secondStr"

                    dogSortedMap = sortMapByKey2(dogWalkMap)

                    for((key, value) in dogSortedMap.entries) {
                        dogWalkDataList.add(value)
                        Log.d("dogSortedMap", value.toString())
                    }
                    dogWalkRVAdapter.notifyDataSetChanged() // 동기화

                    Log.d("walkDataList", dogWalkDataList.toString())
                } catch (e: Exception) {
                    Log.d(TAG, "산책 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.walkDogRef.child(userId).child(dogId).addValueEventListener(postListener)
    }

    private fun showMonthData(userId : String, year : String, month : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    walkDataList.clear() // 똑같은 데이터 복사 출력되는 것 막기 위한 초기화
                    walkMap.clear()
                    sortedMap.clear()

                    var totalNum = 0
                    var totalDistance = 0.0
                    var totalHour = 0
                    var totalMinute = 0
                    var totalSecond = 0

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

                        val timeSp = item!!.time.split(":")

                        if(year.toInt() == nowYear.toInt() && month.toInt() == nowMonth.toInt()) {
                            walkMap[dateTime.toLong()] = item!!
                            sortedMap = sortMapByKey(walkMap)

                            totalNum += 1
                            totalDistance += item!!.distance.toFloat()
                            totalHour += timeSp[0].toInt()
                            totalMinute += timeSp[1].toInt()
                            totalSecond += timeSp[2].toInt()
                        }
                    }

                    totalNumArea.text = totalNum.toString()

                    val df = DecimalFormat("#.##")
                    df.roundingMode = RoundingMode.DOWN
                    val roundoff = df.format(totalDistance)
                    totalDistanceArea.text = roundoff + "km"

                    var totalTime = ""
                    var hourStr = ""
                    var minuteStr = ""
                    var secondStr =""

                    if(totalSecond > 59) {
                        totalMinute += 1
                        totalSecond -= 60
                    }
                    if(totalMinute > 59) {
                        totalMinute -= 60
                        totalHour += 1
                    }

                    hourStr = if(totalHour < 10)
                        "0$totalHour"
                    else
                        totalHour.toString()

                    minuteStr = if(totalMinute < 10)
                        "0$totalMinute"
                    else
                        totalMinute.toString()

                    secondStr = if(totalSecond < 10)
                        "0$totalSecond"
                    else
                        totalSecond.toString()

                    totalTimeArea.text = "$hourStr:$minuteStr:$secondStr"

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

    private fun showMonthSpecificData(userId : String, year : String, month : String, dogId : String) {

        Log.d("getDate", "$userId$dogId")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    dogWalkDataList.clear() // 똑같은 데이터 복사 출력되는 것 막기 위한 초기화
                    dogWalkMap.clear()
                    dogSortedMap.clear()

                    var totalNum = 0
                    var totalDistance = 0.0
                    var totalHour = 0
                    var totalMinute = 0
                    var totalSecond = 0

                    for(dataModel in dataSnapshot.children) {
                        Log.d("showSpecificDate", "played")
                        Log.d("showSpecificDate", dataModel.toString())
                        Log.d(TAG, dataModel.toString())
                        // dataModel.key
                        val item = dataModel.getValue(WalkDogModel::class.java)
                        val date = item!!.date
                        val dateSp = date.split(".")
                        val nowYear = dateSp[0]
                        val nowMonth = dateSp[1]
                        val nowDate = dateSp[2]

                        val startTime = item!!.startTime
                        val startTimeSp = startTime.split(":")

                        val dateTime = (dateSp[2] + startTimeSp[0] + startTimeSp[1])
                        Log.d("dateTime", dateTime)

                        val timeSp = item!!.time.split(":")

                        if(year.toInt() == nowYear.toInt() && month.toInt() == nowMonth.toInt()) {
                            dogWalkMap[dateTime.toLong()] = item!!
                            dogSortedMap = sortMapByKey2(dogWalkMap)

                            totalNum += 1
                            totalDistance += item!!.distance.toFloat()
                            totalHour += timeSp[0].toInt()
                            totalMinute += timeSp[1].toInt()
                            totalSecond += timeSp[2].toInt()
                        }
                    }

                    totalNumArea.text = totalNum.toString()

                    val df = DecimalFormat("#.##")
                    df.roundingMode = RoundingMode.DOWN
                    val roundoff = df.format(totalDistance)
                    totalDistanceArea.text = roundoff + "km"

                    var totalTime = ""
                    var hourStr = ""
                    var minuteStr = ""
                    var secondStr =""

                    if(totalSecond > 59) {
                        totalMinute += 1
                        totalSecond -= 60
                    }
                    if(totalMinute > 59) {
                        totalMinute -= 60
                        totalHour += 1
                    }

                    hourStr = if(totalHour < 10)
                        "0$totalHour"
                    else
                        totalHour.toString()

                    minuteStr = if(totalMinute < 10)
                        "0$totalMinute"
                    else
                        totalMinute.toString()

                    secondStr = if(totalSecond < 10)
                        "0$totalSecond"
                    else
                        totalSecond.toString()

                    totalTimeArea.text = "$hourStr:$minuteStr:$secondStr"

                    for((key, value) in dogSortedMap.entries) {
                        dogWalkDataList.add(value)
                        Log.d("dogSortedMap", value.toString())
                    }
                    dogWalkRVAdapter.notifyDataSetChanged() // 동기화

                    Log.d("walkDataList", dogWalkDataList.toString())
                } catch (e: Exception) {
                    Log.d(TAG, "산책 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.walkDogRef.child(userId).child(dogId).addValueEventListener(postListener)
    }

    private fun showYearData(userId : String, year : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    walkDataList.clear() // 똑같은 데이터 복사 출력되는 것 막기 위한 초기화
                    walkMap.clear()
                    sortedMap.clear()

                    var totalNum = 0
                    var totalDistance = 0.0
                    var totalHour = 0
                    var totalMinute = 0
                    var totalSecond = 0

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

                        val dateTime = (dateSp[0] + dateSp[1] + dateSp[2] + startTimeSp[0] + startTimeSp[1])
                        Log.d("dateTime", dateTime)

                        val timeSp = item!!.time.split(":")

                        if(year.toInt() == nowYear.toInt()) {
                            walkMap[dateTime.toLong()] = item!!
                            sortedMap = sortMapByKey(walkMap)

                            totalNum += 1
                            totalDistance += item!!.distance.toFloat()
                            totalHour += timeSp[0].toInt()
                            totalMinute += timeSp[1].toInt()
                            totalSecond += timeSp[2].toInt()
                        }
                    }

                    totalNumArea.text = totalNum.toString()

                    val df = DecimalFormat("#.##")
                    df.roundingMode = RoundingMode.DOWN
                    val roundoff = df.format(totalDistance)
                    totalDistanceArea.text = roundoff + "km"

                    var totalTime = ""
                    var hourStr = ""
                    var minuteStr = ""
                    var secondStr =""

                    if(totalSecond > 59) {
                        totalMinute += 1
                        totalSecond -= 60
                    }
                    if(totalMinute > 59) {
                        totalMinute -= 60
                        totalHour += 1
                    }

                    hourStr = if(totalHour < 10)
                        "0$totalHour"
                    else
                        totalHour.toString()

                    minuteStr = if(totalMinute < 10)
                        "0$totalMinute"
                    else
                        totalMinute.toString()

                    secondStr = if(totalSecond < 10)
                        "0$totalSecond"
                    else
                        totalSecond.toString()

                    totalTimeArea.text = "$hourStr:$minuteStr:$secondStr"

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

    private fun showYearSpecificData(userId : String, year : String, dogId : String) {

        Log.d("getDate", "$userId$dogId")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    dogWalkDataList.clear() // 똑같은 데이터 복사 출력되는 것 막기 위한 초기화
                    dogWalkMap.clear()
                    dogSortedMap.clear()

                    var totalNum = 0
                    var totalDistance = 0.0
                    var totalHour = 0
                    var totalMinute = 0
                    var totalSecond = 0

                    for(dataModel in dataSnapshot.children) {
                        Log.d("showSpecificDate", "played")
                        Log.d("showSpecificDate", dataModel.toString())
                        Log.d(TAG, dataModel.toString())
                        // dataModel.key
                        val item = dataModel.getValue(WalkDogModel::class.java)
                        val date = item!!.date
                        val dateSp = date.split(".")
                        val nowYear = dateSp[0]
                        val nowMonth = dateSp[1]
                        val nowDate = dateSp[2]

                        val startTime = item!!.startTime
                        val startTimeSp = startTime.split(":")

                        val dateTime = (dateSp[0] + dateSp[1] + dateSp[2] + startTimeSp[0] + startTimeSp[1])
                        Log.d("dateTime", dateTime)

                        val timeSp = item!!.time.split(":")

                        if(year.toInt() == nowYear.toInt()) {
                            dogWalkMap[dateTime.toLong()] = item!!
                            dogSortedMap = sortMapByKey2(dogWalkMap)

                            totalNum += 1
                            totalDistance += item!!.distance.toFloat()
                            totalHour += timeSp[0].toInt()
                            totalMinute += timeSp[1].toInt()
                            totalSecond += timeSp[2].toInt()
                        }
                    }

                    totalNumArea.text = totalNum.toString()

                    val df = DecimalFormat("#.##")
                    df.roundingMode = RoundingMode.DOWN
                    val roundoff = df.format(totalDistance)
                    totalDistanceArea.text = roundoff + "km"

                    var totalTime = ""
                    var hourStr = ""
                    var minuteStr = ""
                    var secondStr =""

                    if(totalSecond > 59) {
                        totalMinute += 1
                        totalSecond -= 60
                    }
                    if(totalMinute > 59) {
                        totalMinute -= 60
                        totalHour += 1
                    }

                    hourStr = if(totalHour < 10)
                        "0$totalHour"
                    else
                        totalHour.toString()

                    minuteStr = if(totalMinute < 10)
                        "0$totalMinute"
                    else
                        totalMinute.toString()

                    secondStr = if(totalSecond < 10)
                        "0$totalSecond"
                    else
                        totalSecond.toString()

                    totalTimeArea.text = "$hourStr:$minuteStr:$secondStr"

                    for((key, value) in dogSortedMap.entries) {
                        dogWalkDataList.add(value)
                        Log.d("dogSortedMap", value.toString())
                    }
                    dogWalkRVAdapter.notifyDataSetChanged() // 동기화

                    Log.d("walkDataList", dogWalkDataList.toString())
                } catch (e: Exception) {
                    Log.d(TAG, "산책 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.walkDogRef.child(userId).child(dogId).addValueEventListener(postListener)
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

            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    for(dataModel in dataSnapshot.children) {
                        Log.d(TAG, dataModel.toString())
                        val item = dataModel.getValue(DogModel::class.java)
                        val postListener = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                try {

                                    for(dataModel in dataSnapshot.children) {
                                        Log.d("showSpecificDate", "played")
                                        Log.d("showSpecificDate", dataModel.toString())
                                        Log.d(TAG, dataModel.toString())
                                        // dataModel.key
                                        val item = dataModel.getValue(WalkDogModel::class.java)
                                        if(item!!.walkId == id) {
                                            FBRef.walkDogRef.child(myUid).child(item!!.dogId).child(item!!.dogWalkId).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.d(TAG, "산책 기록 삭제 완료")
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Getting Post failed, log a message
                                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                            }
                        }
                        FBRef.walkDogRef.child(myUid).child(item!!.dogId).addValueEventListener(postListener)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                }
            }
            FBRef.dogRef.child(myUid).addValueEventListener(postListener)

            FBRef.walkRef.child(myUid).child(id).removeValue()

            alertDialog.dismiss()
        }

        val noBtn = alertDialog.findViewById<Button>(R.id.noBtn)
        noBtn?.setOnClickListener {  // 아니오 버튼 클릭 시
            Log.d(TAG, "no Button Clicked")

            alertDialog.dismiss()
        }
    }

    private fun showDogDialog(v : View, dogId : String, walkId : String, dogWalkId : String) { // 산책 기록 수정/삭제를 위한 다이얼로그 띄우기
        val mDialogView = LayoutInflater.from(v.context).inflate(R.layout.walk_dialog, null)
        val mBuilder = AlertDialog.Builder(v.context).setView(mDialogView)

        Log.d(TAG, "$dogId $dogWalkId $walkId")

        val alertDialog = mBuilder.show()
        val yesBtn = alertDialog.findViewById<Button>(R.id.yesBtn)
        yesBtn?.setOnClickListener { // 예 버튼 클릭 시
            Log.d(TAG, "yes Button Clicked")

            FBRef.walkDogRef.child(myUid).child(dogId).child(dogWalkId).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제

            var dogNum : String = ""
            var date : String = ""
            var startTime : String = ""
            var endTime : String = ""
            var time : String = ""
            var distance : String = ""

            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {
                        val item = dataSnapshot.getValue(WalkModel::class.java)

                        if(item!!.dogNum.toInt() == 1)
                            FBRef.walkRef.child(myUid).child(walkId).removeValue()
                        else if (item!!.dogNum.toInt() > 1) {
                            dogNum = (item!!.dogNum.toInt() - 1).toString()
                            date = item!!.date
                            startTime = item!!.startTime
                            endTime = item!!.endTime
                            time = item!!.time
                            distance = item!!.distance

                            FBRef.walkRef.child(myUid).child(walkId).setValue(WalkModel(myUid, walkId, dogNum, date, startTime, endTime, time, distance))
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "산책 기록 삭제 완료")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                }
            }
            FBRef.walkRef.child(myUid).child(walkId).addValueEventListener(postListener)

            alertDialog.dismiss()
        }

        val noBtn = alertDialog.findViewById<Button>(R.id.noBtn)
        noBtn?.setOnClickListener {  // 아니오 버튼 클릭 시
            Log.d(TAG, "no Button Clicked")
            alertDialog.dismiss()
        }
    }


    private fun sortMapByKey(map: MutableMap<Long, WalkModel>): LinkedHashMap<Long, WalkModel> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.key }

        val result = LinkedHashMap<Long, WalkModel>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }

    private fun sortMapByKey2(map: MutableMap<Long, WalkDogModel>): LinkedHashMap<Long, WalkDogModel> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.key }

        val result = LinkedHashMap<Long, WalkDogModel>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }

    fun week(eventDate: String) { // 선택된 날짜가 포함된 1주일간의 날짜 범위를 구하는 함수
        val dateArray = eventDate.split("-").toTypedArray()

        val cal = Calendar.getInstance()
        cal[dateArray[0].toInt(), dateArray[1].toInt() - 1] = dateArray[2].toInt()

        cal.firstDayOfWeek = Calendar.SUNDAY // 일주일의 첫날을 일요일로 지정

        val dayOfWeek = cal[Calendar.DAY_OF_WEEK] - cal.firstDayOfWeek // 시작일과 특정 날짜의 차이를 구함

        cal.add(Calendar.DAY_OF_MONTH, -dayOfWeek) // 해당 주차의 첫째날 지정

        val sf = SimpleDateFormat("yyyy.MM.dd")

        val startDt = sf.format(cal.time) // 해당 주차의 첫째 날짜

        cal.add(Calendar.DAY_OF_MONTH, 6)  // 해당 주차의 마지막 날짜 지정

        val endDt = sf.format(cal.time) // 해당 주차의 마지막 날짜

        startDateArea.text = startDt.toString()
        endDateArea.text = endDt.toString()

        Log.d("getWeekDate", "특정 날짜 = [$eventDate] >> 시작 날짜 = [$startDt], 종료 날짜 = [$endDt]")
    }
}