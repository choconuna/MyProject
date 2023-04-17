package org.techtown.myproject.walk

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.techtown.myproject.R
import org.techtown.myproject.community.PagerFragmentStateAdapter
import org.techtown.myproject.receipt.ReceiptFragment
import org.techtown.myproject.receipt.ReceiptRecordFragment
import org.techtown.myproject.receipt.ReceiptStatisticsFragment

class WalkFragment : Fragment() {

    lateinit var tab_main : TabLayout
    lateinit var viewPager : ViewPager2

    private val TAG = WalkFragment::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences

    private val walkPlayFragment by lazy { WalkPlayFragment() }
    private val walkLogFragment by lazy { WalkLogFragment() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v : View? = inflater.inflate(R.layout.fragment_walk, container, false)

        viewPager = v!!.findViewById(R.id.viewpager)
        tab_main = v!!.findViewById(R.id.tabs)

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val pagerAdapter = PagerFragmentStateAdapter(requireActivity())
        // 2개의 fragment add
        pagerAdapter.addFragment(walkPlayFragment)
        pagerAdapter.addFragment(walkLogFragment)

        // adapter
        viewPager.adapter = pagerAdapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int){
                super.onPageSelected(position)
                Log.e("ViewPagerFragment", "Page ${position+1}")
            }
        })
        // tablayout attach
        val tabTitles = listOf<String>("산책하기", "산책 일지")
        TabLayoutMediator(tab_main, viewPager){ tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
}