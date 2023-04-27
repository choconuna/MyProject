package org.techtown.myproject.chat

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.prolificinteractive.materialcalendarview.CalendarDay
import org.techtown.myproject.R
import org.techtown.myproject.community.PagerFragmentStateAdapter
import org.techtown.myproject.deal.DealChatFragment
import org.techtown.myproject.note.NoteFragment
import org.techtown.myproject.note.RecordFragment
import org.techtown.myproject.statistics.StatisticsFragment

class AllChatFragment : Fragment() {

    lateinit var tab_main : TabLayout
    lateinit var viewPager : ViewPager2

    private val chatFragment by lazy { ChatFragment() }
    private val dealChatFragment by lazy { DealChatFragment() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v : View? = inflater.inflate(R.layout.fragment_all_chat, container, false)

        viewPager = v!!.findViewById(R.id.viewpager)
        tab_main = v!!.findViewById(R.id.tabs)

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val pagerAdapter = PagerFragmentStateAdapter(requireActivity())
        // 5개의 fragment add
        pagerAdapter.addFragment(chatFragment)
        pagerAdapter.addFragment(dealChatFragment)

        // adapter
        viewPager.adapter = pagerAdapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int){
                super.onPageSelected(position)
                Log.e("ViewPagerFragment", "Page ${position+1}")
            }
        })
        // tablayout attach
        val tabTitles = listOf<String>("일반 채팅", "거래 채팅")
        TabLayoutMediator(tab_main, viewPager){ tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
}