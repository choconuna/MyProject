package org.techtown.myproject.community_search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.SearchData
import org.techtown.myproject.SearchViewModel
import org.techtown.myproject.community.CommunityListVAdapter
import org.techtown.myproject.community.CommunityModel
import org.techtown.myproject.community.PagerFragmentStateAdapter
import org.techtown.myproject.community.SpecificCommunityInActivity
import org.techtown.myproject.utils.FBRef
import java.text.SimpleDateFormat
import java.util.*

class SearchInformationFragment : Fragment() {

    private val TAG = SearchInformationFragment::class.java.simpleName

    private lateinit var viewModel: SearchViewModel

    private var category: String? = null
    private var date: String? = null
    private var startDate : String? = null
    private var endDate : String? = null
    private var searchText: String? = null

    lateinit var communityListView : ListView

    private val communityDataList = mutableListOf<CommunityModel>() // 각 게시물을 넣는 리스트
    private var communityMap : MutableMap<CommunityModel, Long> = mutableMapOf()
    private var sortedMap : MutableMap<CommunityModel, Long> = mutableMapOf()

    lateinit var communityRVAdapter : CommunityListVAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v : View? = inflater.inflate(R.layout.fragment_search_information, container, false)

        viewModel = ViewModelProvider(requireActivity())[SearchViewModel::class.java]

        setData(v!!)

        viewModel.searchData.observe(viewLifecycleOwner) { searchData ->
            date = searchData!!.date
            startDate = searchData!!.startDate
            endDate = searchData!!.endDate
            category = searchData!!.category
            searchText = searchData!!.searchText

            getCommunityData(date!!, startDate!!, endDate!!, category!!, searchText!!) // 검색 결과에 맞는 커뮤니티 데이터 가져오기
        }

        // 게시물 클릭 시 그 게시물 보이기
        communityListView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(context, SpecificCommunityInActivity::class.java)
            intent.putExtra("category", "정보") // 게시물의 카테고리 넘기기
            intent.putExtra("key", communityDataList[position].communityId) // 게시물의 key 값 넘기기
            startActivity(intent)
        }

        return v
    }

    private fun setData(v : View) {
        communityRVAdapter = CommunityListVAdapter(communityDataList)
        communityListView = v.findViewById(R.id.communityListView)
        communityListView.adapter = communityRVAdapter
    }

    private fun getCommunityData(date : String, startDate : String, endDate : String, category : String, searchText : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {

                    communityDataList.clear()
                    communityMap.clear()
                    sortedMap.clear()

                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(CommunityModel::class.java)

                        when (category) {
                            "제목+본문" -> {
                                if(item!!.title.contains(searchText) || item!!.content.contains(searchText)) {
                                    if(date == "전체") { // 날짜가 전체일 경우
                                        var nowDateSp = item!!.time.split(" ")
                                        var dateSp = nowDateSp[0].split(".")
                                        var timeSp = nowDateSp[1].split(":")
                                        var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                        communityMap[item] = nowDate.toLong()
                                    } else { // 날짜가 일정 기간일 경우
                                        var nowDateSp = item!!.time.split(" ")

                                        // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                        val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                        val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                        val nowDateFm = nowDateSp[0]

                                        if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                            var dateSp = nowDateSp[0].split(".")
                                            var timeSp = nowDateSp[1].split(":")
                                            var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                            communityMap[item] = nowDate.toLong()
                                        }
                                    }
                                }
                            }
                            "제목" -> {
                                if(item!!.title.contains(searchText)) {
                                    if(date == "전체") { // 날짜가 전체일 경우
                                        var nowDateSp = item!!.time.split(" ")
                                        var dateSp = nowDateSp[0].split(".")
                                        var timeSp = nowDateSp[1].split(":")
                                        var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                        communityMap[item] = nowDate.toLong()
                                    } else { // 날짜가 일정 기간일 경우
                                        var nowDateSp = item!!.time.split(" ")

                                        // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                        val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                        val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                        val nowDateFm = nowDateSp[0]

                                        if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                            var dateSp = nowDateSp[0].split(".")
                                            var timeSp = nowDateSp[1].split(":")
                                            var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                            communityMap[item] = nowDate.toLong()
                                        }
                                    }
                                }
                            }
                            "작성자" -> {
                                val userNameRef = FBRef.userRef.child(item!!.uid).child("nickName")
                                userNameRef.get().addOnSuccessListener {
                                    val nickName = it.value as? String ?: ""
                                    Log.d("nickName", "$nickName $searchText")
                                    if(nickName.contains(searchText)) {
                                        if(date == "전체") { // 날짜가 전체일 경우
                                            var nowDateSp = item!!.time.split(" ")
                                            var dateSp = nowDateSp[0].split(".")
                                            var timeSp = nowDateSp[1].split(":")
                                            var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                            communityMap[item] = nowDate.toLong()

                                            Log.d("nickName", "실행")
                                            communityDataList.clear()
                                            sortedMap = sortMapByKey(communityMap)
                                            for((key, value) in sortedMap.entries) {
                                                communityDataList.add(key)
                                                Log.d("sortedMap", key.toString())
                                            }

                                            communityRVAdapter.notifyDataSetChanged()

                                        } else { // 날짜가 일정 기간일 경우
                                            var nowDateSp = item!!.time.split(" ")

                                            // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                            val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)
                                            val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDate)
                                            val nowDateFm = nowDateSp[0]

                                            if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                var dateSp = nowDateSp[0].split(".")
                                                var timeSp = nowDateSp[1].split(":")
                                                var nowDate = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                communityMap[item] = nowDate.toLong()

                                                Log.d("nickName", "실행")
                                                communityDataList.clear()
                                                sortedMap = sortMapByKey(communityMap)
                                                for((key, value) in sortedMap.entries) {
                                                    communityDataList.add(key)
                                                    Log.d("sortedMap", key.toString())
                                                }

                                                communityRVAdapter.notifyDataSetChanged()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    sortedMap = sortMapByKey(communityMap)
                    for((key, value) in sortedMap.entries) {
                        communityDataList.add(key)
                        Log.d("sortedMap", key.toString())
                    }

                    communityRVAdapter.notifyDataSetChanged()

                } catch(e : Exception) { }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.communityRef.child("정보").addValueEventListener(postListener)
    }

    fun isBetweenDates(dateString: String, startDate: Date, endDate: Date): Boolean {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        return (date?.after(startDate) == true || date?.equals(startDate) == true) && (date?.before(endDate) || date?.equals(endDate))
    }

    private fun sortMapByKey(map: MutableMap<CommunityModel, Long>): LinkedHashMap<CommunityModel, Long> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.value }

        val result = LinkedHashMap<CommunityModel, Long>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }
}