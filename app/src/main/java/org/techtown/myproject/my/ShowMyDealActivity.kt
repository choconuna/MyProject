package org.techtown.myproject.my

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.techtown.myproject.R
import org.techtown.myproject.community.PagerFragmentStateAdapter

class ShowMyDealActivity : AppCompatActivity() {

    lateinit var tab_main : TabLayout
    lateinit var viewPager : ViewPager2

    lateinit var backBtn : ImageView

    private val dealSellFragment by lazy { DealSellFragment() }
    private val dealReservationFragment by lazy { DealReservationFragment() }
    private val dealCompleteFragment by lazy { DealCompleteFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_my_deal)

        viewPager = findViewById(R.id.viewpager)
        tab_main = findViewById(R.id.tabs)

        backBtn = findViewById(R.id.back)
        backBtn.setOnClickListener { // 뒤로 가기 버튼 클릭 시 화면 나오기
            finish()
        }

        val pagerAdapter = PagerFragmentStateAdapter(this)
        // 3개의 fragment add
        pagerAdapter.addFragment(dealSellFragment)
        pagerAdapter.addFragment(dealReservationFragment)
        pagerAdapter.addFragment(dealCompleteFragment)

        // adapter
        viewPager.adapter = pagerAdapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int){
                super.onPageSelected(position)
                Log.e("ViewPagerFragment", "Page ${position+1}")
            }
        })
        // tablayout attach
        val tabTitles = listOf<String>("판매 중", "예약 중", "거래 완료")
        TabLayoutMediator(tab_main, viewPager){ tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
}