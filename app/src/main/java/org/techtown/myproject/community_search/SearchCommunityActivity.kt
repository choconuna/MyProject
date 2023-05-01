package org.techtown.myproject.community_search

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.SearchViewModel
import org.techtown.myproject.community.*
import org.techtown.myproject.deal.DealFragment
import org.techtown.myproject.receipt.ReceiptRecordEditActivity
import org.techtown.myproject.receipt.ReceiptSearchReVAdapter
import org.techtown.myproject.utils.ReceiptModel
import java.text.SimpleDateFormat
import java.util.*

class SearchCommunityActivity : AppCompatActivity() {

    private lateinit var myUid : String

    private lateinit var searchViewModel: SearchViewModel

    private lateinit var dateSpinner : Spinner
    private lateinit var date : String

    private lateinit var dateShowArea : LinearLayout
    private lateinit var showStartDate : LinearLayout // 클릭 시 달력 나옴 -> 날짜 선택하도록
    private lateinit var startDateArea : TextView // 선택된 시작 날짜
    private lateinit var showEndDate : LinearLayout // 클릭 시 달력 나옴 -> 날짜 선택하도록
    private lateinit var endDateArea : TextView // 선택된 종료 날짜

    private lateinit var backBtn : ImageView
    private lateinit var categorySpinner : Spinner
    private lateinit var category : String
    private lateinit var searchArea: EditText
    private lateinit var searchBtn : ImageView

    lateinit var tab_main : TabLayout
    lateinit var viewPager : ViewPager2

    private val searchInformationFragment by lazy { SearchInformationFragment() }
    private val searchReviewFragment by lazy { SearchReviewFragment() }
    private val searchFreeFragment by lazy { SearchFreeFragment() }
    private val searchQuestionFragment by lazy { SearchQuestionFragment() }
    private val searchDealFragment by lazy { SearchDealFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_community)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        searchViewModel = ViewModelProvider(this)[SearchViewModel::class.java]

        setTab()

        setData()

        dateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, id: Long) {
                date = adapterView.getItemAtPosition(position).toString()

                if(date == "기간") {
                    dateShowArea.visibility = View.VISIBLE
                } else {
                    dateShowArea.visibility = View.GONE
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }

        showStartDate.setOnClickListener {
            val datepickercalendar = Calendar.getInstance()
            val year = datepickercalendar.get(Calendar.YEAR)
            val month = datepickercalendar.get(Calendar.MONTH)
            val day = datepickercalendar.get(Calendar.DAY_OF_MONTH)

            // 이전에 선택된 날짜 가져오기
            val selectedDate = startDateArea.text.toString().split('.')
            val selectedStartYear = selectedDate[0].toIntOrNull()
            val selectedStartMonth = selectedDate[1].toIntOrNull()?.minus(1) // 월은 0부터 시작하기 때문에 1을 빼줌
            val selectedStartDay = selectedDate[2].toIntOrNull()

            if(!this.isFinishing) {
                val dpd = DatePickerDialog(
                    this@SearchCommunityActivity, R.style.MySpinnerDatePickerStyle,
                    { _, year, monthOfYear, dayOfMonth ->

                        val month = monthOfYear + 1 // 월이 0부터 시작하여 1을 더해줌
                        val calendar = Calendar.getInstance() //선택한 날짜의 요일을 구하기 위한 calendar
                        calendar.set(year, monthOfYear, dayOfMonth) //선택한 날짜 세팅

                        if(month.toString().length == 1 && dayOfMonth.toString().length == 1) {
                            startDateArea.text = "$year.0$month.0$dayOfMonth"
                        } else if(dayOfMonth.toString().length == 1) {
                            startDateArea.text = "$year.$month.0$dayOfMonth"
                        } else if(month.toString().length == 1) {
                            startDateArea.text = "$year.0$month.$dayOfMonth"
                        } else {
                            startDateArea.text = "$year.$month.$dayOfMonth"
                        }
                    }, selectedStartYear!!, selectedStartMonth!!, selectedStartDay!!
                )
                dpd.show()
            }
        }

        showEndDate.setOnClickListener {
            val datepickercalendar = Calendar.getInstance()
            val year = datepickercalendar.get(Calendar.YEAR)
            val month = datepickercalendar.get(Calendar.MONTH)
            val day = datepickercalendar.get(Calendar.DAY_OF_MONTH)

            // 이전에 선택된 날짜 가져오기
            val selectedDate = endDateArea.text.toString().split('.')
            val selectedEndYear = selectedDate[0].toIntOrNull()
            val selectedEndMonth = selectedDate[1].toIntOrNull()?.minus(1) // 월은 0부터 시작하기 때문에 1을 빼줌
            val selectedEndDay = selectedDate[2].toIntOrNull()

            if(!this.isFinishing) {
                val dpd = DatePickerDialog(
                    this@SearchCommunityActivity, R.style.MySpinnerDatePickerStyle,
                    { _, year, monthOfYear, dayOfMonth ->

                        val month = monthOfYear + 1 // 월이 0부터 시작하여 1을 더해줌
                        val calendar = Calendar.getInstance() //선택한 날짜의 요일을 구하기 위한 calendar
                        calendar.set(year, monthOfYear, dayOfMonth) //선택한 날짜 세팅

                        if(month.toString().length == 1 && dayOfMonth.toString().length == 1) {
                            endDateArea.text = "$year.0$month.0$dayOfMonth"
                        } else if(dayOfMonth.toString().length == 1) {
                            endDateArea.text = "$year.$month.0$dayOfMonth"
                        } else if(month.toString().length == 1) {
                            endDateArea.text = "$year.0$month.$dayOfMonth"
                        } else {
                            endDateArea.text = "$year.$month.$dayOfMonth"
                        }
                    }, selectedEndYear!!, selectedEndMonth!!, selectedEndDay!!
                )
                dpd.show()
            }
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, id: Long) {
                category = adapterView.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }

        searchBtn.setOnClickListener {

            val searchText = searchArea.text.toString().trim()

            searchViewModel.setValues(date, startDateArea.text.toString(), endDateArea.text.toString(), category, searchText)

            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(searchArea.windowToken, 0)
        }

        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setTab() {
        viewPager = findViewById(R.id.viewpager)
        tab_main = findViewById(R.id.tabs)

        // adapter
        val pagerAdapter = PagerFragmentStateAdapter(this)

        // 5개의 fragment add
        pagerAdapter.addFragment(searchInformationFragment)
        pagerAdapter.addFragment(searchReviewFragment)
        pagerAdapter.addFragment(searchFreeFragment)
        pagerAdapter.addFragment(searchQuestionFragment)
        pagerAdapter.addFragment(searchDealFragment)

        viewPager.adapter = pagerAdapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.e("ViewPagerFragment", "Page ${position + 1}")
            }
        })

        // tablayout attach
        val tabTitles = listOf<String>("정보", "후기", "자유", "질문", "거래")
        TabLayoutMediator(tab_main, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }


    private fun setData() {

        dateSpinner = findViewById(R.id.dateSpinner)
        date = dateSpinner.getItemAtPosition(0).toString()
        dateShowArea = findViewById(R.id.dateShowArea)

        showStartDate = findViewById(R.id.showStartDate)
        startDateArea = findViewById(R.id.startDateArea)
        showEndDate = findViewById(R.id.showEndDate)
        endDateArea = findViewById(R.id.endDateArea)

        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)
        startDateArea.text = formattedDate // 시작 날짜를 일단 현재 날짜로 설정
        endDateArea.text = formattedDate // 종료 날짜를 일단 현재 날짜로 설정

        backBtn = findViewById(R.id.backBtn)
        searchBtn = findViewById(R.id.searchBtn)
        categorySpinner = findViewById(R.id.categorySpinner)
        category = categorySpinner.getItemAtPosition(0).toString()
        searchArea = findViewById(R.id.searchArea)
    }
}