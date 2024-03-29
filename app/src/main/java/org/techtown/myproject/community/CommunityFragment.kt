package org.techtown.myproject.community

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.techtown.myproject.*
import org.techtown.myproject.chat.AllChatFragment
import org.techtown.myproject.community_search.SearchCommunityActivity
import org.techtown.myproject.deal.DealFragment

class CommunityFragment : Fragment() {

    private val TAG = CommunityFragment::class.java.simpleName

    private lateinit var searchBtn : ImageView

    lateinit var tab_main : TabLayout
    lateinit var viewPager : ViewPager2

    private val informationFragment by lazy { InformationFragment() }
    private val reviewFragment by lazy { ReviewFragment() }
    private val freeFragment by lazy { FreeFragment() }
    private val questionFragment by lazy { QuestionFragment() }
    private val dealFragment by lazy { DealFragment() }
    private val allChatFragment by lazy { AllChatFragment() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v : View? = inflater.inflate(R.layout.fragment_community, container, false)

        viewPager = v!!.findViewById(R.id.viewpager)
        tab_main = v!!.findViewById(R.id.tabs)

        searchBtn = v!!.findViewById(R.id.searchBtn)
        searchBtn.setOnClickListener {
            val intent = Intent(v!!.context, SearchCommunityActivity::class.java)
            v!!.context.startActivity(intent)
        }

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val pagerAdapter = PagerFragmentStateAdapter(requireActivity())
        // 6개의 fragment add
        pagerAdapter.addFragment(informationFragment)
        pagerAdapter.addFragment(reviewFragment)
        pagerAdapter.addFragment(freeFragment)
        pagerAdapter.addFragment(questionFragment)
        pagerAdapter.addFragment(dealFragment)
        pagerAdapter.addFragment(allChatFragment)

        // adapter
        viewPager.adapter = pagerAdapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int){
                super.onPageSelected(position)
                Log.e("ViewPagerFragment", "Page ${position+1}")
            }
        })
        // tablayout attach
        val tabTitles = listOf<String>("정보", "후기", "자유", "질문", "거래", "채팅")
        TabLayoutMediator(tab_main, viewPager){ tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
}