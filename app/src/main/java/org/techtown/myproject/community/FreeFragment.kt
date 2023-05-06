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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.utils.BlockModel
import org.techtown.myproject.utils.FBRef

class FreeFragment : Fragment() {
    private val TAG = FreeFragment::class.java.simpleName

    private lateinit var myUid : String

    lateinit var writeBtn : ImageView
    lateinit var communityListView : ListView

    private val blockList = mutableListOf<String>() // 차단된 uid 값을 넣는 리스트

    private val communityDataList = mutableListOf<CommunityModel>() // 각 게시물을 넣는 리스트
    private val communityKeyList = mutableListOf<String>() // 각 게시물의 키값을 넣는 리스트

    lateinit var communityRVAdapter : CommunityListVAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v : View? = inflater.inflate(R.layout.fragment_free, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        communityRVAdapter = CommunityListVAdapter(communityDataList)
        communityListView = v!!.findViewById(R.id.communityListView)
        communityListView.adapter = communityRVAdapter

        // 게시물 클릭 시 그 게시물 보이기
        communityListView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(context, SpecificCommunityInActivity::class.java)
            intent.putExtra("category", "자유") // 게시물의 카테고리 넘기기
            intent.putExtra("key", communityKeyList[position]) // 게시물의 key 값 넘기기
            startActivity(intent)
        }

        writeBtn = v!!.findViewById(R.id.writeBtn)
        writeBtn.setOnClickListener {
            val intent = Intent(context, WriteSpecificCommunityActivity::class.java)
            intent.putExtra("category", "자유")
            startActivity(intent)
        }

        getFreeCommunityData()

        return v
    }

    private fun getFreeCommunityData() { // 파이어베이스로부터 커뮤니티 데이터 불러오기

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {

                    blockList.clear()

                    for(dataModel in dataSnapshot.children) {
                        Log.d(TAG, dataModel.toString())
                        // dataModel.key
                        val item = dataModel.getValue(BlockModel::class.java)
                        blockList.add(item!!.blockUid)
                    }

                    val postListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            try {
                                communityDataList.clear()
                                communityKeyList.clear()

                                Log.d("blockListCom", blockList.toString())

                                for (dataModel in dataSnapshot.children) {
                                    val item = dataModel.getValue(CommunityModel::class.java)
                                    if (!blockList.contains(item!!.uid)) {
                                        communityDataList.add(item)
                                        communityKeyList.add(dataModel.key.toString())
                                    }
                                }

                                communityKeyList.reverse()
                                communityDataList.reverse()
                                communityRVAdapter.notifyDataSetChanged()
                                Log.d("communityDataList", communityDataList.toString())

                            } catch (e: Exception) {
                                Log.e(TAG, "Error while getting community data", e)
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e(TAG, "Error while getting community data", databaseError.toException())
                        }
                    }
                    FBRef.communityRef.child("자유").addListenerForSingleValueEvent(postListener)

                } catch(e : Exception) { }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.blockRef.child(myUid).addValueEventListener(postListener)
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Log.v(TAG, "setUserVisibleHint $isVisibleToUser")
        if(isVisibleToUser) {
            //데이터 받아오기
        }
    }
}