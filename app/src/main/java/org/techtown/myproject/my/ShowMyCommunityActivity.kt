package org.techtown.myproject.my

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.techtown.myproject.R
import org.techtown.myproject.community.*

class ShowMyCommunityActivity : AppCompatActivity() {

    lateinit var tab_main : TabLayout
    lateinit var viewPager : ViewPager2

    lateinit var backBtn : ImageView

    private val myInformationFragment by lazy { MyInformationFragment() }
    private val myReviewFragment by lazy { MyReviewFragment() }
    private val myFreeFragment by lazy { MyFreeFragment() }
    private val myQuestionFragment by lazy { MyQuestionFragment() }
    private val myDealFragment by lazy { MyDealFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_my_community)

        viewPager = findViewById(R.id.viewpager)
        tab_main = findViewById(R.id.tabs)

        backBtn = findViewById(R.id.back)
        backBtn.setOnClickListener { // 뒤로 가기 버튼 클릭 시 화면 나오기
            finish()
        }

        val pagerAdapter = PagerFragmentStateAdapter(this)
        // 5개의 fragment add
        pagerAdapter.addFragment(myInformationFragment)
        pagerAdapter.addFragment(myReviewFragment)
        pagerAdapter.addFragment(myFreeFragment)
        pagerAdapter.addFragment(myQuestionFragment)
        pagerAdapter.addFragment(myDealFragment)

        // adapter
        viewPager.adapter = pagerAdapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int){
                super.onPageSelected(position)
                Log.e("ViewPagerFragment", "Page ${position+1}")
            }
        })
        // tablayout attach
        val tabTitles = listOf<String>("정보", "후기", "자유", "질문", "거래")
        TabLayoutMediator(tab_main, viewPager){ tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
}