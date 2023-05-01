package org.techtown.myproject.community_search

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v : View? = inflater.inflate(R.layout.fragment_search_deal, container, false)

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

        return v
    }

    private fun setData(v : View) {

    }

    private fun getCommunityData(date : String, startDate : String, endDate : String, category : String, searchText : String) {

    }

    fun isBetweenDates(dateString: String, startDate: Date, endDate: Date): Boolean { // dateString이 기간 사이에 있는지 확인하기 위한 함수
        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        return date?.after(startDate) == true && date.before(endDate)
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