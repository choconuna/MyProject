package org.techtown.myproject.community_search

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.SearchData
import org.techtown.myproject.SearchViewModel
import org.techtown.myproject.community.CommunityListVAdapter
import org.techtown.myproject.community.CommunityModel
import org.techtown.myproject.community.PagerFragmentStateAdapter
import org.techtown.myproject.deal.*
import org.techtown.myproject.utils.DealModel
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.ReceiptModel
import java.text.SimpleDateFormat
import java.util.*

class SearchDealFragment : Fragment() {

    private val TAG = SearchDealFragment::class.java.simpleName

    private lateinit var viewModel: SearchViewModel

    private var category: String? = null
    private var date: String? = null
    private var startDate : String? = null
    private var endDate : String? = null
    private var searchText: String? = null

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var pullLocationName : String

    private lateinit var selectCategory : TextView

    private lateinit var adminArea : String
    private lateinit var subLocality : String
    private lateinit var thoroughfare : String

    private lateinit var myUid : String

    private lateinit var locationArea : TextView
    private var locationList : MutableMap<String, Int> = mutableMapOf()

    lateinit var dealRecyclerView: RecyclerView

    private var dealMap : MutableMap<DealModel, Long> = mutableMapOf()
    private val dealList = ArrayList<DealModel>() // 거래 목록 리스트
    lateinit var dealRVAdapter : DealReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    private var checkedLocation : String = "" // 카테고리에서 선택된 지역
    private var checkedCategory : MutableList<String> = mutableListOf() // 카테고리에서 선택된 판매 용품 종류 리스트

    private lateinit var categoryRecyclerView : RecyclerView
    private lateinit var categoryRVAdapter : CategoryReVAdapter
    private val categoryList = ArrayList<String>() // 카테고리 목록 리스트
    lateinit var cLayoutManager : RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v : View? = inflater.inflate(R.layout.fragment_search_deal, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        sharedPreferences = v!!.context.getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        pullLocationName = sharedPreferences.getString(myUid + "SearchLocation", "").toString() // 사용자의 위치 정보를 받아옴
        Log.d("getLocation", pullLocationName)
        adminArea = pullLocationName.split(" ")[0]
        subLocality = pullLocationName.split(" ")[1]
        thoroughfare = pullLocationName.split(" ")[2]

        viewModel = ViewModelProvider(requireActivity())[SearchViewModel::class.java]

        setData(v!!)

        viewModel.searchData.observe(viewLifecycleOwner) { searchData ->
            date = searchData!!.date
            startDate = searchData!!.startDate
            endDate = searchData!!.endDate
            category = searchData!!.category
            searchText = searchData!!.searchText

            getDealData(date!!, startDate!!, endDate!!, category!!, searchText!!, checkedLocation, checkedCategory) // 검색 결과에 맞는 커뮤니티 데이터 가져오기
        }

        selectCategory.setOnClickListener {
            showDialog(v!!)
        }

        locationArea.setOnClickListener {
            val intent = Intent(v!!.context, SearchLocalActivity::class.java)
            v!!.context.startActivity(intent)
        }

        dealRVAdapter.setItemClickListener(object: DealReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val intent = Intent(v!!.context, DealInActivity::class.java)
                intent.putExtra("dealId", dealList[position].dealId)
                intent.putExtra("sellerId", dealList[position].sellerId)
                v!!.context.startActivity(intent)
            }
        })

        return v
    }

    override fun onResume() {
        super.onResume()

        pullLocationName = sharedPreferences.getString(myUid + "SearchLocation", "").toString() // 사용자의 위치 정보를 받아옴
        Log.d("getLocation", pullLocationName)
        adminArea = pullLocationName.split(" ")[0]
        subLocality = pullLocationName.split(" ")[1]
        thoroughfare = pullLocationName.split(" ")[2]

        locationArea.text = "$adminArea $subLocality $thoroughfare"
    }

    private fun setData(v : View) {
        selectCategory = v.findViewById(R.id.selectCategory)
        locationArea = v.findViewById(R.id.locationArea)

        dealRecyclerView = v.findViewById(R.id.dealRecyclerView)
        dealRVAdapter = DealReVAdapter(dealList)
        dealRecyclerView.setItemViewCacheSize(20)
        dealRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        dealRecyclerView.layoutManager = layoutManager
        dealRecyclerView.adapter = dealRVAdapter

        categoryRecyclerView = v.findViewById(R.id.categoryRecyclerView)
        categoryRVAdapter = CategoryReVAdapter(categoryList)
        categoryRecyclerView.setItemViewCacheSize(20)
        categoryRecyclerView.setHasFixedSize(true)
        cLayoutManager = LinearLayoutManager(v.context, LinearLayoutManager.HORIZONTAL, false)
        categoryRecyclerView.layoutManager = cLayoutManager
        categoryRecyclerView.adapter = categoryRVAdapter

        categoryList.clear()
        categoryList.add("전체 지역")
        categoryList.add("전체")
        categoryRVAdapter.notifyDataSetChanged()

        locationList.clear()

        locationList["모두"] = 0
        locationList[adminArea] = 0
        locationList["$adminArea $subLocality"] = 0
        locationList["$adminArea $subLocality $thoroughfare"] = 0

        locationArea.text = "$adminArea $subLocality $thoroughfare"
    }

    private fun getDealData(date : String, startDate : String, endDate : String, category : String, searchText : String, checkedLocation : String, checkedCategory : MutableList<String>) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    dealList.clear()
                    dealMap.clear()

                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DealModel::class.java)

                        if (item!!.state != "거래 완료") { // 거래가 완료된 데이터 제외

                            when (category) {
                                "제목+본문" -> {
                                    if(item!!.title.contains(searchText) || item!!.content.contains(searchText)) {
                                        if(date == "전체") { // 날짜가 전체일 경우
                                            if(checkedLocation == "" && checkedCategory.isEmpty()) { // 선택된 지역과 선택된 카테고리가 없을 경우 -> 처음으로 화면에 진입했을 경우

                                                val date = item!!.date
                                                val sp = date.split(" ")
                                                val dateSp = sp[0].split(".")
                                                val timeSp = sp[1].split(":")

                                                var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                dealMap[item!!] = dayNum.toLong()
                                            } else { // 선택된 지역과 선택된 카테고리가 존재할 경우
                                                if(checkedLocation == "전체 지역") { // 전체 지역이 선택됐을 경우
                                                    if(checkedCategory.contains("전체")) {
                                                        val date = item!!.date
                                                        val sp = date.split(" ")
                                                        val dateSp = sp[0].split(".")
                                                        val timeSp = sp[1].split(":")

                                                        var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                        dealMap[item!!] = dayNum.toLong()
                                                    } else {
                                                        for(i in 0 until checkedCategory.size) {
                                                            if (item!!.category == checkedCategory[i]) {
                                                                val date = item!!.date
                                                                val sp = date.split(" ")
                                                                val dateSp = sp[0].split(".")
                                                                val timeSp = sp[1].split(":")

                                                                var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                dealMap[item!!] = dayNum.toLong()
                                                            }
                                                        }
                                                    }
                                                } else if(checkedLocation != "전체 지역") {
                                                    if(checkedLocation.last() == '동') {
                                                        if(checkedCategory.contains("전체")) {
                                                            if(item!!.location.contains(checkedLocation.substring(0, 2))) {
                                                                val date = item!!.date
                                                                val sp = date.split(" ")
                                                                val dateSp = sp[0].split(".")
                                                                val timeSp = sp[1].split(":")

                                                                var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                dealMap[item!!] = dayNum.toLong()
                                                            }
                                                        } else {
                                                            if(item!!.location.contains(checkedLocation.substring(0, 2))) {
                                                                for(i in 0 until checkedCategory.size) {
                                                                    if (item!!.category == checkedCategory[i]) {
                                                                        val date = item!!.date
                                                                        val sp = date.split(" ")
                                                                        val dateSp = sp[0].split(".")
                                                                        val timeSp = sp[1].split(":")

                                                                        var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                        dealMap[item!!] = dayNum.toLong()
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        if(checkedCategory.contains("전체")) {
                                                            if(item!!.location.contains(checkedLocation)) {
                                                                val date = item!!.date
                                                                val sp = date.split(" ")
                                                                val dateSp = sp[0].split(".")
                                                                val timeSp = sp[1].split(":")

                                                                var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                dealMap[item!!] = dayNum.toLong()
                                                            }
                                                        } else {
                                                            if(item!!.location.contains(checkedLocation)) {
                                                                for(i in 0 until checkedCategory.size) {
                                                                    if (item!!.category == checkedCategory[i]) {
                                                                        val date = item!!.date
                                                                        val sp = date.split(" ")
                                                                        val dateSp = sp[0].split(".")
                                                                        val timeSp = sp[1].split(":")

                                                                        var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                        dealMap[item!!] = dayNum.toLong()
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else { // 날짜가 일정 기간일 경우
                                            if(checkedLocation == "" && checkedCategory.isEmpty()) { // 선택된 지역과 선택된 카테고리가 없을 경우 -> 처음으로 화면에 진입했을 경우

                                                var nowDateSp = item!!.date.split(" ")

                                                // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                val nowDateFm = nowDateSp[0]

                                                if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                    var dateSp = nowDateSp[0].split(".")
                                                    var timeSp = nowDateSp[1].split(":")
                                                    var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                    dealMap[item] = nowDate.toLong()
                                                }
                                            } else { // 선택된 지역과 선택된 카테고리가 존재할 경우
                                                if(checkedLocation == "전체 지역") { // 전체 지역이 선택됐을 경우
                                                    if(checkedCategory.contains("전체")) {
                                                        var nowDateSp = item!!.date.split(" ")

                                                        // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                        val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                        val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                        val nowDateFm = nowDateSp[0]

                                                        if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                            var dateSp = nowDateSp[0].split(".")
                                                            var timeSp = nowDateSp[1].split(":")
                                                            var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                            dealMap[item] = nowDate.toLong()
                                                        }
                                                    } else {
                                                        for(i in 0 until checkedCategory.size) {
                                                            if (item!!.category == checkedCategory[i]) {
                                                                var nowDateSp = item!!.date.split(" ")

                                                                // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                                val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                                val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                                val nowDateFm = nowDateSp[0]

                                                                if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                                    var dateSp = nowDateSp[0].split(".")
                                                                    var timeSp = nowDateSp[1].split(":")
                                                                    var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                    dealMap[item] = nowDate.toLong()
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else if(checkedLocation != "전체 지역") {
                                                    if(checkedLocation.last() == '동') {
                                                        if(checkedCategory.contains("전체")) {
                                                            if(item!!.location.contains(checkedLocation.substring(0, 2))) {
                                                                var nowDateSp = item!!.date.split(" ")

                                                                // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                                val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                                val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                                val nowDateFm = nowDateSp[0]

                                                                if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                                    var dateSp = nowDateSp[0].split(".")
                                                                    var timeSp = nowDateSp[1].split(":")
                                                                    var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                    dealMap[item] = nowDate.toLong()
                                                                }
                                                            }
                                                        } else {
                                                            if(item!!.location.contains(checkedLocation.substring(0, 2))) {
                                                                for(i in 0 until checkedCategory.size) {
                                                                    if (item!!.category == checkedCategory[i]) {
                                                                        var nowDateSp = item!!.date.split(" ")

                                                                        // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                                        val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                                        val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                                        val nowDateFm = nowDateSp[0]

                                                                        if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                                            var dateSp = nowDateSp[0].split(".")
                                                                            var timeSp = nowDateSp[1].split(":")
                                                                            var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                            dealMap[item] = nowDate.toLong()
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        if(checkedCategory.contains("전체")) {
                                                            if(item!!.location.contains(checkedLocation)) {
                                                                var nowDateSp = item!!.date.split(" ")

                                                                // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                                val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                                val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                                val nowDateFm = nowDateSp[0]

                                                                if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                                    var dateSp = nowDateSp[0].split(".")
                                                                    var timeSp = nowDateSp[1].split(":")
                                                                    var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                    dealMap[item] = nowDate.toLong()
                                                                }
                                                            }
                                                        } else {
                                                            if(item!!.location.contains(checkedLocation)) {
                                                                for(i in 0 until checkedCategory.size) {
                                                                    if (item!!.category == checkedCategory[i]) {
                                                                        var nowDateSp = item!!.date.split(" ")

                                                                        // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                                        val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                                        val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                                        val nowDateFm = nowDateSp[0]

                                                                        if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                                            var dateSp = nowDateSp[0].split(".")
                                                                            var timeSp = nowDateSp[1].split(":")
                                                                            var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                            dealMap[item] = nowDate.toLong()
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                "제목" -> {
                                    if(item!!.title.contains(searchText)) {
                                        if(date == "전체") { // 날짜가 전체일 경우
                                            if(checkedLocation == "" && checkedCategory.isEmpty()) { // 선택된 지역과 선택된 카테고리가 없을 경우 -> 처음으로 화면에 진입했을 경우

                                                val date = item!!.date
                                                val sp = date.split(" ")
                                                val dateSp = sp[0].split(".")
                                                val timeSp = sp[1].split(":")

                                                var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                dealMap[item!!] = dayNum.toLong()
                                            } else { // 선택된 지역과 선택된 카테고리가 존재할 경우
                                                if(checkedLocation == "전체 지역") { // 전체 지역이 선택됐을 경우
                                                    if(checkedCategory.contains("전체")) {
                                                        val date = item!!.date
                                                        val sp = date.split(" ")
                                                        val dateSp = sp[0].split(".")
                                                        val timeSp = sp[1].split(":")

                                                        var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                        dealMap[item!!] = dayNum.toLong()
                                                    } else {
                                                        for(i in 0 until checkedCategory.size) {
                                                            if (item!!.category == checkedCategory[i]) {
                                                                val date = item!!.date
                                                                val sp = date.split(" ")
                                                                val dateSp = sp[0].split(".")
                                                                val timeSp = sp[1].split(":")

                                                                var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                dealMap[item!!] = dayNum.toLong()
                                                            }
                                                        }
                                                    }
                                                } else if(checkedLocation != "전체 지역") {
                                                    if(checkedLocation.last() == '동') {
                                                        if(checkedCategory.contains("전체")) {
                                                            if(item!!.location.contains(checkedLocation.substring(0, 2))) {
                                                                val date = item!!.date
                                                                val sp = date.split(" ")
                                                                val dateSp = sp[0].split(".")
                                                                val timeSp = sp[1].split(":")

                                                                var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                dealMap[item!!] = dayNum.toLong()
                                                            }
                                                        } else {
                                                            if(item!!.location.contains(checkedLocation.substring(0, 2))) {
                                                                for(i in 0 until checkedCategory.size) {
                                                                    if (item!!.category == checkedCategory[i]) {
                                                                        val date = item!!.date
                                                                        val sp = date.split(" ")
                                                                        val dateSp = sp[0].split(".")
                                                                        val timeSp = sp[1].split(":")

                                                                        var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                        dealMap[item!!] = dayNum.toLong()
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        if(checkedCategory.contains("전체")) {
                                                            if(item!!.location.contains(checkedLocation)) {
                                                                val date = item!!.date
                                                                val sp = date.split(" ")
                                                                val dateSp = sp[0].split(".")
                                                                val timeSp = sp[1].split(":")

                                                                var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                dealMap[item!!] = dayNum.toLong()
                                                            }
                                                        } else {
                                                            if(item!!.location.contains(checkedLocation)) {
                                                                for(i in 0 until checkedCategory.size) {
                                                                    if (item!!.category == checkedCategory[i]) {
                                                                        val date = item!!.date
                                                                        val sp = date.split(" ")
                                                                        val dateSp = sp[0].split(".")
                                                                        val timeSp = sp[1].split(":")

                                                                        var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                        dealMap[item!!] = dayNum.toLong()
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else { // 날짜가 일정 기간일 경우
                                            if(checkedLocation == "" && checkedCategory.isEmpty()) { // 선택된 지역과 선택된 카테고리가 없을 경우 -> 처음으로 화면에 진입했을 경우

                                                var nowDateSp = item!!.date.split(" ")

                                                // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                val nowDateFm = nowDateSp[0]

                                                if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                    var dateSp = nowDateSp[0].split(".")
                                                    var timeSp = nowDateSp[1].split(":")
                                                    var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                    dealMap[item] = nowDate.toLong()
                                                }
                                            } else { // 선택된 지역과 선택된 카테고리가 존재할 경우
                                                if(checkedLocation == "전체 지역") { // 전체 지역이 선택됐을 경우
                                                    if(checkedCategory.contains("전체")) {
                                                        var nowDateSp = item!!.date.split(" ")

                                                        // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                        val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                        val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                        val nowDateFm = nowDateSp[0]

                                                        if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                            var dateSp = nowDateSp[0].split(".")
                                                            var timeSp = nowDateSp[1].split(":")
                                                            var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                            dealMap[item] = nowDate.toLong()
                                                        }
                                                    } else {
                                                        for(i in 0 until checkedCategory.size) {
                                                            if (item!!.category == checkedCategory[i]) {
                                                                var nowDateSp = item!!.date.split(" ")

                                                                // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                                val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                                val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                                val nowDateFm = nowDateSp[0]

                                                                if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                                    var dateSp = nowDateSp[0].split(".")
                                                                    var timeSp = nowDateSp[1].split(":")
                                                                    var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                    dealMap[item] = nowDate.toLong()
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else if(checkedLocation != "전체 지역") {
                                                    if(checkedLocation.last() == '동') {
                                                        if(checkedCategory.contains("전체")) {
                                                            if(item!!.location.contains(checkedLocation.substring(0, 2))) {
                                                                var nowDateSp = item!!.date.split(" ")

                                                                // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                                val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                                val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                                val nowDateFm = nowDateSp[0]

                                                                if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                                    var dateSp = nowDateSp[0].split(".")
                                                                    var timeSp = nowDateSp[1].split(":")
                                                                    var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                    dealMap[item] = nowDate.toLong()
                                                                }
                                                            }
                                                        } else {
                                                            if(item!!.location.contains(checkedLocation.substring(0, 2))) {
                                                                for(i in 0 until checkedCategory.size) {
                                                                    if (item!!.category == checkedCategory[i]) {
                                                                        var nowDateSp = item!!.date.split(" ")

                                                                        // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                                        val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                                        val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                                        val nowDateFm = nowDateSp[0]

                                                                        if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                                            var dateSp = nowDateSp[0].split(".")
                                                                            var timeSp = nowDateSp[1].split(":")
                                                                            var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                            dealMap[item] = nowDate.toLong()
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        if(checkedCategory.contains("전체")) {
                                                            if(item!!.location.contains(checkedLocation)) {
                                                                var nowDateSp = item!!.date.split(" ")

                                                                // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                                val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                                val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                                val nowDateFm = nowDateSp[0]

                                                                if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                                    var dateSp = nowDateSp[0].split(".")
                                                                    var timeSp = nowDateSp[1].split(":")
                                                                    var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                    dealMap[item] = nowDate.toLong()
                                                                }
                                                            }
                                                        } else {
                                                            if(item!!.location.contains(checkedLocation)) {
                                                                for(i in 0 until checkedCategory.size) {
                                                                    if (item!!.category == checkedCategory[i]) {
                                                                        var nowDateSp = item!!.date.split(" ")

                                                                        // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                                        val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                                        val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                                        val nowDateFm = nowDateSp[0]

                                                                        if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                                            var dateSp = nowDateSp[0].split(".")
                                                                            var timeSp = nowDateSp[1].split(":")
                                                                            var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                            dealMap[item] = nowDate.toLong()
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                "작성자" -> {
                                    val userNameRef = FBRef.userRef.child(item!!.sellerId).child("nickName")
                                    userNameRef.get().addOnSuccessListener {
                                        val nickName = it.value as? String ?: ""
                                        Log.d("nickName", "$nickName $searchText")
                                        if(nickName.contains(searchText)) {
                                            if(date == "전체") { // 날짜가 전체일 경우
                                                if(checkedLocation == "" && checkedCategory.isEmpty()) { // 선택된 지역과 선택된 카테고리가 없을 경우 -> 처음으로 화면에 진입했을 경우

                                                    val date = item!!.date
                                                    val sp = date.split(" ")
                                                    val dateSp = sp[0].split(".")
                                                    val timeSp = sp[1].split(":")

                                                    var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                    dealMap[item!!] = dayNum.toLong()

                                                    dealList.clear()
                                                    val sortedDealMap = sortMapByKey(dealMap)
                                                    for((key, value) in sortedDealMap.entries) {
                                                        dealList.add(key)
                                                    }

                                                    dealRVAdapter.notifyDataSetChanged()
                                                } else { // 선택된 지역과 선택된 카테고리가 존재할 경우
                                                    if(checkedLocation == "전체 지역") { // 전체 지역이 선택됐을 경우
                                                        if(checkedCategory.contains("전체")) {
                                                            val date = item!!.date
                                                            val sp = date.split(" ")
                                                            val dateSp = sp[0].split(".")
                                                            val timeSp = sp[1].split(":")

                                                            var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                            dealMap[item!!] = dayNum.toLong()

                                                            dealList.clear()
                                                            val sortedDealMap = sortMapByKey(dealMap)
                                                            for((key, value) in sortedDealMap.entries) {
                                                                dealList.add(key)
                                                            }

                                                            dealRVAdapter.notifyDataSetChanged()
                                                        } else {
                                                            for(i in 0 until checkedCategory.size) {
                                                                if (item!!.category == checkedCategory[i]) {
                                                                    val date = item!!.date
                                                                    val sp = date.split(" ")
                                                                    val dateSp = sp[0].split(".")
                                                                    val timeSp = sp[1].split(":")

                                                                    var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                    dealMap[item!!] = dayNum.toLong()

                                                                    dealList.clear()
                                                                    val sortedDealMap = sortMapByKey(dealMap)
                                                                    for((key, value) in sortedDealMap.entries) {
                                                                        dealList.add(key)
                                                                    }

                                                                    dealRVAdapter.notifyDataSetChanged()
                                                                }
                                                            }
                                                        }
                                                    } else if(checkedLocation != "전체 지역") {
                                                        if(checkedLocation.last() == '동') {
                                                            if(checkedCategory.contains("전체")) {
                                                                if(item!!.location.contains(checkedLocation.substring(0, 2))) {
                                                                    val date = item!!.date
                                                                    val sp = date.split(" ")
                                                                    val dateSp = sp[0].split(".")
                                                                    val timeSp = sp[1].split(":")

                                                                    var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                    dealMap[item!!] = dayNum.toLong()

                                                                    dealList.clear()
                                                                    val sortedDealMap = sortMapByKey(dealMap)
                                                                    for((key, value) in sortedDealMap.entries) {
                                                                        dealList.add(key)
                                                                    }

                                                                    dealRVAdapter.notifyDataSetChanged()
                                                                }
                                                            } else {
                                                                if(item!!.location.contains(checkedLocation.substring(0, 2))) {
                                                                    for(i in 0 until checkedCategory.size) {
                                                                        if (item!!.category == checkedCategory[i]) {
                                                                            val date = item!!.date
                                                                            val sp = date.split(" ")
                                                                            val dateSp = sp[0].split(".")
                                                                            val timeSp = sp[1].split(":")

                                                                            var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                            dealMap[item!!] = dayNum.toLong()

                                                                            dealList.clear()
                                                                            val sortedDealMap = sortMapByKey(dealMap)
                                                                            for((key, value) in sortedDealMap.entries) {
                                                                                dealList.add(key)
                                                                            }

                                                                            dealRVAdapter.notifyDataSetChanged()
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            if(checkedCategory.contains("전체")) {
                                                                if(item!!.location.contains(checkedLocation)) {
                                                                    val date = item!!.date
                                                                    val sp = date.split(" ")
                                                                    val dateSp = sp[0].split(".")
                                                                    val timeSp = sp[1].split(":")

                                                                    var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                    dealMap[item!!] = dayNum.toLong()

                                                                    dealList.clear()
                                                                    val sortedDealMap = sortMapByKey(dealMap)
                                                                    for((key, value) in sortedDealMap.entries) {
                                                                        dealList.add(key)
                                                                    }

                                                                    dealRVAdapter.notifyDataSetChanged()
                                                                }
                                                            } else {
                                                                if(item!!.location.contains(checkedLocation)) {
                                                                    for(i in 0 until checkedCategory.size) {
                                                                        if (item!!.category == checkedCategory[i]) {
                                                                            val date = item!!.date
                                                                            val sp = date.split(" ")
                                                                            val dateSp = sp[0].split(".")
                                                                            val timeSp = sp[1].split(":")

                                                                            var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                            dealMap[item!!] = dayNum.toLong()

                                                                            dealList.clear()
                                                                            val sortedDealMap = sortMapByKey(dealMap)
                                                                            for((key, value) in sortedDealMap.entries) {
                                                                                dealList.add(key)
                                                                            }

                                                                            dealRVAdapter.notifyDataSetChanged()
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            } else { // 날짜가 일정 기간일 경우
                                                if(checkedLocation == "" && checkedCategory.isEmpty()) { // 선택된 지역과 선택된 카테고리가 없을 경우 -> 처음으로 화면에 진입했을 경우

                                                    var nowDateSp = item!!.date.split(" ")

                                                    // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                    val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                    val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                    val nowDateFm = nowDateSp[0]

                                                    if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                        var dateSp = nowDateSp[0].split(".")
                                                        var timeSp = nowDateSp[1].split(":")
                                                        var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                        dealMap[item] = nowDate.toLong()

                                                        dealList.clear()
                                                        val sortedDealMap = sortMapByKey(dealMap)
                                                        for((key, value) in sortedDealMap.entries) {
                                                            dealList.add(key)
                                                        }

                                                        dealRVAdapter.notifyDataSetChanged()
                                                    }
                                                } else { // 선택된 지역과 선택된 카테고리가 존재할 경우
                                                    if(checkedLocation == "전체 지역") { // 전체 지역이 선택됐을 경우
                                                        if(checkedCategory.contains("전체")) {
                                                            var nowDateSp = item!!.date.split(" ")

                                                            // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                            val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                            val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                            val nowDateFm = nowDateSp[0]

                                                            if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                                var dateSp = nowDateSp[0].split(".")
                                                                var timeSp = nowDateSp[1].split(":")
                                                                var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                dealMap[item] = nowDate.toLong()

                                                                dealList.clear()
                                                                val sortedDealMap = sortMapByKey(dealMap)
                                                                for((key, value) in sortedDealMap.entries) {
                                                                    dealList.add(key)
                                                                }

                                                                dealRVAdapter.notifyDataSetChanged()
                                                            }
                                                        } else {
                                                            for(i in 0 until checkedCategory.size) {
                                                                if (item!!.category == checkedCategory[i]) {
                                                                    var nowDateSp = item!!.date.split(" ")

                                                                    // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                                    val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                                    val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                                    val nowDateFm = nowDateSp[0]

                                                                    if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                                        var dateSp = nowDateSp[0].split(".")
                                                                        var timeSp = nowDateSp[1].split(":")
                                                                        var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                        dealMap[item] = nowDate.toLong()

                                                                        dealList.clear()
                                                                        val sortedDealMap = sortMapByKey(dealMap)
                                                                        for((key, value) in sortedDealMap.entries) {
                                                                            dealList.add(key)
                                                                        }

                                                                        dealRVAdapter.notifyDataSetChanged()
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    } else if(checkedLocation != "전체 지역") {
                                                        if(checkedLocation.last() == '동') {
                                                            if(checkedCategory.contains("전체")) {
                                                                if(item!!.location.contains(checkedLocation.substring(0, 2))) {
                                                                    var nowDateSp = item!!.date.split(" ")

                                                                    // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                                    val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                                    val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                                    val nowDateFm = nowDateSp[0]

                                                                    if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                                        var dateSp = nowDateSp[0].split(".")
                                                                        var timeSp = nowDateSp[1].split(":")
                                                                        var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                        dealMap[item] = nowDate.toLong()

                                                                        dealList.clear()
                                                                        val sortedDealMap = sortMapByKey(dealMap)
                                                                        for((key, value) in sortedDealMap.entries) {
                                                                            dealList.add(key)
                                                                        }

                                                                        dealRVAdapter.notifyDataSetChanged()
                                                                    }
                                                                }
                                                            } else {
                                                                if(item!!.location.contains(checkedLocation.substring(0, 2))) {
                                                                    for(i in 0 until checkedCategory.size) {
                                                                        if (item!!.category == checkedCategory[i]) {
                                                                            var nowDateSp = item!!.date.split(" ")

                                                                            // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                                            val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                                            val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                                            val nowDateFm = nowDateSp[0]

                                                                            if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                                                var dateSp = nowDateSp[0].split(".")
                                                                                var timeSp = nowDateSp[1].split(":")
                                                                                var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                                dealMap[item] = nowDate.toLong()

                                                                                dealList.clear()
                                                                                val sortedDealMap = sortMapByKey(dealMap)
                                                                                for((key, value) in sortedDealMap.entries) {
                                                                                    dealList.add(key)
                                                                                }

                                                                                dealRVAdapter.notifyDataSetChanged()
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            if(checkedCategory.contains("전체")) {
                                                                if(item!!.location.contains(checkedLocation)) {
                                                                    var nowDateSp = item!!.date.split(" ")

                                                                    // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                                    val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                                    val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                                    val nowDateFm = nowDateSp[0]

                                                                    if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                                        var dateSp = nowDateSp[0].split(".")
                                                                        var timeSp = nowDateSp[1].split(":")
                                                                        var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                        dealMap[item] = nowDate.toLong()

                                                                        dealList.clear()
                                                                        val sortedDealMap = sortMapByKey(dealMap)
                                                                        for((key, value) in sortedDealMap.entries) {
                                                                            dealList.add(key)
                                                                        }

                                                                        dealRVAdapter.notifyDataSetChanged()
                                                                    }
                                                                }
                                                            } else {
                                                                if(item!!.location.contains(checkedLocation)) {
                                                                    for(i in 0 until checkedCategory.size) {
                                                                        if (item!!.category == checkedCategory[i]) {
                                                                            var nowDateSp = item!!.date.split(" ")

                                                                            // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                                                            val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                                                            val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                                                            val nowDateFm = nowDateSp[0]

                                                                            if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                                                var dateSp = nowDateSp[0].split(".")
                                                                                var timeSp = nowDateSp[1].split(":")
                                                                                var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                                                dealMap[item] = nowDate.toLong()

                                                                                dealList.clear()
                                                                                val sortedDealMap = sortMapByKey(dealMap)
                                                                                for((key, value) in sortedDealMap.entries) {
                                                                                    dealList.add(key)
                                                                                }

                                                                                dealRVAdapter.notifyDataSetChanged()
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val sortedDealMap = sortMapByKey(dealMap)
                    for((key, value) in sortedDealMap.entries) {
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

    fun isBetweenDates(dateString: String, startDate: Date, endDate: Date): Boolean {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        return (date?.after(startDate) == true || date?.equals(startDate) == true) && (date?.before(endDate) == true || date?.equals(endDate) == true)
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

    fun showDialog(v : View) {
        val dialogView = LayoutInflater.from(v!!.context).inflate(R.layout.category_dialog, null)
        val dialog = Dialog(v!!.context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)

        val height = (resources.displayMetrics.heightPixels * 0.5).toInt()
        dialog.window?.apply {
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, height)
            setGravity(Gravity.BOTTOM)
            setBackgroundDrawable(ColorDrawable(Color.WHITE))
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            setWindowAnimations(android.R.style.Animation_InputMethod)
        }

        dialog.show()

        val location1 = dialogView?.findViewById<RadioButton>(R.id.location1)!!
        val location2 = dialogView?.findViewById<RadioButton>(R.id.location2)!!
        val location3 = dialogView?.findViewById<RadioButton>(R.id.location3)!!
        val location4 = dialogView?.findViewById<RadioButton>(R.id.location4)!!

        location2.text = adminArea
        location3.text = subLocality
        location4.text = thoroughfare

        when {
            categoryList.contains(location1.text.toString()) -> {
                location1.isChecked = true
                location1.setTextColor(Color.WHITE)
                location2.setTextColor(Color.parseColor("#c08457"))
                location3.setTextColor(Color.parseColor("#c08457"))
                location4.setTextColor(Color.parseColor("#c08457"))
            }
            categoryList.contains(location2.text.toString()) -> {
                location2.isChecked = true
                location2.setTextColor(Color.WHITE)
                location1.setTextColor(Color.parseColor("#c08457"))
                location3.setTextColor(Color.parseColor("#c08457"))
                location4.setTextColor(Color.parseColor("#c08457"))
            }
            categoryList.contains(location3.text.toString()) -> {
                location3.isChecked = true
                location3.setTextColor(Color.WHITE)
                location1.setTextColor(Color.parseColor("#c08457"))
                location2.setTextColor(Color.parseColor("#c08457"))
                location4.setTextColor(Color.parseColor("#c08457"))
            }
            categoryList.contains(location4.text.toString()) -> {
                location4.isChecked = true
                location4.setTextColor(Color.WHITE)
                location1.setTextColor(Color.parseColor("#c08457"))
                location2.setTextColor(Color.parseColor("#c08457"))
                location3.setTextColor(Color.parseColor("#c08457"))
            }
        }

        var radioGroup = dialogView?.findViewById<RadioGroup>(R.id.radioGroup)!!
        val selectedRadioButton = dialogView?.findViewById<RadioButton>(radioGroup.checkedRadioButtonId)
        checkedLocation = selectedRadioButton.text.toString()
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val checkedRadioButton = dialogView?.findViewById<RadioButton>(checkedId)
            checkedRadioButton.setTextColor(Color.WHITE)
            checkedLocation = checkedRadioButton.text.toString()

            for (i in 0 until group.childCount) {
                val radioButton = group.getChildAt(i) as RadioButton
                if (radioButton.id != checkedId) {
                    radioButton.setTextColor(Color.parseColor("#c08457"))
                }
            }
        }

        Log.d("categoryList", checkedCategory.toString())

        val categoryBox1 = dialogView?.findViewById<CheckBox>(R.id.categoryBox1)
        if(categoryList.contains(categoryBox1.text.toString())) {
            categoryBox1.isChecked = true
        }
        val categoryBox2 = dialogView?.findViewById<CheckBox>(R.id.categoryBox2)
        if(categoryList.contains(categoryBox2.text.toString())) {
            categoryBox2.isChecked = true
        }
        val categoryBox3 = dialogView?.findViewById<CheckBox>(R.id.categoryBox3)
        if(categoryList.contains(categoryBox3.text.toString())) {
            categoryBox3.isChecked = true
        }
        val categoryBox4 = dialogView?.findViewById<CheckBox>(R.id.categoryBox4)
        if(categoryList.contains(categoryBox4.text.toString())) {
            categoryBox4.isChecked = true
        }
        val categoryBox5 = dialogView?.findViewById<CheckBox>(R.id.categoryBox5)
        if(categoryList.contains(categoryBox5.text.toString())) {
            categoryBox5.isChecked = true
        }
        val categoryBox6 = dialogView?.findViewById<CheckBox>(R.id.categoryBox6)
        if(categoryList.contains(categoryBox6.text.toString())) {
            categoryBox6.isChecked = true
        }

        checkedCategory.clear()
        if(categoryList.contains(categoryBox1.text.toString())) {
            categoryBox1.isChecked = true
            checkedCategory.add(categoryBox1.text.toString())
        }
        categoryBox1?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!checkedCategory.contains(categoryBox1.text.toString())) {
                    checkedCategory.add(categoryBox1.text.toString())
                }
            } else {
                checkedCategory.remove(categoryBox1.text.toString())
            }
        }

        if(categoryList.contains(categoryBox2.text.toString())) {
            categoryBox2.isChecked = true
            checkedCategory.add(categoryBox2.text.toString())
        }
        categoryBox2?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!checkedCategory.contains(categoryBox2.text.toString())) {
                    checkedCategory.add(categoryBox2.text.toString())
                }
            } else {
                checkedCategory.remove(categoryBox2.text.toString())
            }
        }

        if(categoryList.contains(categoryBox3.text.toString())) {
            categoryBox3.isChecked = true
            checkedCategory.add(categoryBox3.text.toString())
        }
        categoryBox3?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!checkedCategory.contains(categoryBox3.text.toString())) {
                    checkedCategory.add(categoryBox3.text.toString())
                }
            } else {
                checkedCategory.remove(categoryBox3.text.toString())
            }
        }

        if(categoryList.contains(categoryBox4.text.toString())) {
            categoryBox4.isChecked = true
            checkedCategory.add(categoryBox4.text.toString())
        }
        categoryBox4?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!checkedCategory.contains(categoryBox4.text.toString())) {
                    checkedCategory.add(categoryBox4.text.toString())
                }
            } else {
                checkedCategory.remove(categoryBox4.text.toString())
            }
        }

        if(categoryList.contains(categoryBox5.text.toString())) {
            categoryBox5.isChecked = true
            checkedCategory.add(categoryBox5.text.toString())
        }
        categoryBox5?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!checkedCategory.contains(categoryBox5.text.toString())) {
                    checkedCategory.add(categoryBox5.text.toString())
                }
            } else {
                checkedCategory.remove(categoryBox5.text.toString())
            }
        }

        if(categoryList.contains(categoryBox6.text.toString())) {
            categoryBox6.isChecked = true
            checkedCategory.add(categoryBox6.text.toString())
        }
        categoryBox6?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!checkedCategory.contains(categoryBox6.text.toString())) {
                    checkedCategory.add(categoryBox6.text.toString())
                }
            } else {
                checkedCategory.remove(categoryBox6.text.toString())
            }
        }

        val checkBtn = dialogView?.findViewById<ImageView>(R.id.checkBtn)
        checkBtn.setOnClickListener {
            if(checkedCategory.size == 0) {
                Toast.makeText(v!!.context, "카테고리를 하나 이상 선택하세요!", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("checkedCategory", "$checkedLocation $checkedCategory")

                categoryList.clear()

                categoryList.add(checkedLocation)
                for(i in 0 until checkedCategory.size)
                    categoryList.add(checkedCategory[i])

                categoryRVAdapter.notifyDataSetChanged()

                getDealData(date!!, startDate!!, endDate!!, category!!, searchText!!, checkedLocation, checkedCategory) // 검색 결과에 맞는 커뮤니티 데이터 가져오기

                dialog.dismiss()
            }
        }

        val backBtn = dialogView?.findViewById<ImageView>(R.id.backBtn)
        backBtn!!.setOnClickListener {
            dialog.dismiss()
        }
    }
}