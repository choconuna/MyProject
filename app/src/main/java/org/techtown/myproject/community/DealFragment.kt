package org.techtown.myproject.community

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.utils.FBRef

class DealFragment : Fragment() {
    private val TAG = DealFragment::class.java.simpleName

    lateinit var writeBtn : ImageView
    lateinit var communityListView : ListView

    private val communityDataList = mutableListOf<CommunityModel>() // 각 게시물을 넣는 리스트
    private val communityKeyList = mutableListOf<String>() // 각 게시물의 키값을 넣는 리스트

    lateinit var communityRVAdapter : CommunityListVAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v : View? = inflater.inflate(R.layout.fragment_deal, container, false)

        communityRVAdapter = CommunityListVAdapter(communityDataList)
        communityListView = v!!.findViewById(R.id.communityListView)
        communityListView.adapter = communityRVAdapter

        // 게시물 클릭 시 그 게시물 보이기
        communityListView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(context, SpecificCommunityInActivity::class.java)
            intent.putExtra("category", "거래") // 게시물의 카테고리 넘기기
            intent.putExtra("key", communityKeyList[position]) // 게시물의 key 값 넘기기
            startActivity(intent)
        }

        writeBtn = v!!.findViewById(R.id.writeBtn)
        writeBtn.setOnClickListener {
            val intent = Intent(context, WriteSpecificCommunityActivity::class.java)
            intent.putExtra("category", "거래")
            startActivity(intent)
        }

        getDealCommunityData()

        return v
    }

    private fun getDealCommunityData() { // 파이어베이스로부터 커뮤니티 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                communityDataList.clear() // 똑같은 데이터 복사 출력되는 것 막기 위한 초기화

                for(dataModel in dataSnapshot.children) {
                    Log.d(TAG, dataModel.toString())
                    // dataModel.key
                    val item = dataModel.getValue(CommunityModel::class.java)
                    communityDataList.add(item!!)
                    communityKeyList.add(dataModel.key.toString())
                    Log.d(TAG, communityKeyList.toString())
                }

                communityKeyList.reverse() // 키 값을 최신순으로 정렬
                communityDataList.reverse() // 게시물을 최신순으로 정렬
                communityRVAdapter.notifyDataSetChanged() // 동기화

                Log.d("communityDataList", communityDataList.toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.communityRef.child("거래").addValueEventListener(postListener)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Log.v(TAG, "setUserVisibleHint $isVisibleToUser")
        if(isVisibleToUser) {
            //데이터 받아오기
        }
    }
}