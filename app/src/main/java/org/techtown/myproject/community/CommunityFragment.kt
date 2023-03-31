package org.techtown.myproject.community

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.techtown.myproject.*

class CommunityFragment : Fragment() {

    private val TAG = CommunityFragment::class.java.simpleName

    lateinit var writeBtn : ImageView
    lateinit var communityListView : ListView

    lateinit var tab_main : TabLayout
    lateinit var viewPager : ViewPager2

    private val informationFragment by lazy { InformationFragment() }
    private val reviewFragment by lazy { ReviewFragment() }
    private val freeFragment by lazy { FreeFragment() }
    private val questionFragment by lazy { QuestionFragment() }
    private val dealFragment by lazy { DealFragment() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v : View? = inflater.inflate(R.layout.fragment_community, container, false)

        viewPager = v!!.findViewById(R.id.viewpager)
        tab_main = v!!.findViewById(R.id.tabs)

        /* communityRVAdapter = CommunityListVAdapter(communityDataList)
        communityListView = v!!.findViewById(R.id.communityListView)
        communityListView.adapter = communityRVAdapter

        // 각각의 게시물 클릭 시 그 게시물 보이기
        communityListView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(context, CommunityInActivity::class.java)
            intent.putExtra("key", communityKeyList[position])
            startActivity(intent)
        }

        writeBtn = v!!.findViewById(R.id.writeBtn)
        writeBtn.setOnClickListener {
            val intent = Intent(context, WriteCommunityActivity::class.java)
            startActivity(intent)
        }

        getFBCommunityData() // 커뮤니티 데이터 목록 가져오기 */

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val pagerAdapter = PagerFragmentStateAdapter(requireActivity())
        // 5개의 fragment add
        pagerAdapter.addFragment(informationFragment)
        pagerAdapter.addFragment(reviewFragment)
        pagerAdapter.addFragment(freeFragment)
        pagerAdapter.addFragment(questionFragment)
        pagerAdapter.addFragment(dealFragment)

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

    /* private fun initTopLayout() { // 상단 탭에 맞는 fragment 띄우기
        tab_main.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.text.toString()) {
                    "전체" -> changeFragment(allFragment)
                    "정보" -> changeFragment(informationFragment)
                    "후기" -> changeFragment(reviewFragment)
                    "자유" -> changeFragment(freeFragment)
                    "질문" -> changeFragment(questionFragment)
                    "거래" -> changeFragment(dealFragment)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
    }

    private fun changeFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction().replace(R.id.frame, fragment).commit()
    }

    private fun getFBCommunityData() { // 파이어베이스로부터 커뮤니티 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                communityDataList.clear() // 똑같은 데이터 복사 출력되는 것 막기 위한 초기화

                for(dataModel in dataSnapshot.children) {
                    Log.d(TAG, dataModel.toString())
                    // dataModel.key
                    val item = dataModel.getValue(CommunityModel::class.java)
                    communityDataList.add(item!!)
                    communityKeyList.add(dataModel.key.toString())
                }

                communityKeyList.reverse() // 키 값을 최신순으로 정렬
                communityDataList.reverse() // 게시물을 최신순으로 정렬
                communityRVAdapter.notifyDataSetChanged() // 동기화

                Log.d(TAG, communityDataList.toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.communityRef.addValueEventListener(postListener)
    } */
}