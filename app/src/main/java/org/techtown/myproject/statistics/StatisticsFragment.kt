package org.techtown.myproject.statistics

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

class StatisticsFragment : Fragment() {

    lateinit var tab_main : TabLayout
    lateinit var viewPager : ViewPager2

    private val mealStatisticsFragment by lazy { MealStatisticsFragment() }
    private val snackStatisticsFragment by lazy { SnackStatisticsFragment() }
    private val tonicStatisticsFragment by lazy { TonicStatisticsFragment() }
    private val waterStatisticsFragment by lazy { WaterStatisticsFragment() }
    private val peeStatisticsFragment by lazy { PeeStatisticsFragment() }
    private val dungStatisticsFragment by lazy { DungStatisticsFragment() }
    private val vomitStatisticsFragment by lazy { VomitStatisticsFragment() }
    private val heartStatisticsFragment by lazy { HeartStatisticsFragment() }
    private val checkUpStatisticsFragment by lazy { CheckUpStatisticsFragment() }
    private val checkUpPictureStatisticsFragment by lazy { CheckUpPictureStatisticsFragment() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v : View? = inflater.inflate(R.layout.fragment_statistics, container, false)

        viewPager = v!!.findViewById(R.id.viewpager)
        tab_main = v!!.findViewById(R.id.tabs)

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val pagerAdapter = PagerFragmentStateAdapter(requireActivity())

        // 10개의 fragment add
        pagerAdapter.addFragment(mealStatisticsFragment)
        pagerAdapter.addFragment(snackStatisticsFragment)
        pagerAdapter.addFragment(tonicStatisticsFragment)
        pagerAdapter.addFragment(waterStatisticsFragment)
        pagerAdapter.addFragment(peeStatisticsFragment)
        pagerAdapter.addFragment(dungStatisticsFragment)
        pagerAdapter.addFragment(vomitStatisticsFragment)
        pagerAdapter.addFragment(heartStatisticsFragment)
        pagerAdapter.addFragment(checkUpStatisticsFragment)
        pagerAdapter.addFragment(checkUpPictureStatisticsFragment)

        // adapter
        viewPager.adapter = pagerAdapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int){
                super.onPageSelected(position)
                Log.e("ViewPagerFragment", "Page ${position+1}")
            }
        })
        // tablayout attach
        val tabTitles = listOf<String>("사료", "간식", "영양제", "물", "소변", "대변", "구토", "호흡수", "검사지표", "검사사진")
        TabLayoutMediator(tab_main, viewPager){ tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
}