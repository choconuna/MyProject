package org.techtown.myproject.note

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.community.CommunityModel
import org.techtown.myproject.utils.*
import java.util.ArrayList

class MemoFragment : Fragment() {

    private val TAG = MemoFragment::class.java.simpleName

    lateinit var memoPlusBtn : Button

    private lateinit var memoListView : RecyclerView
    private val memoDataList = ArrayList<DogMemoModel>() // 메모 목록 리스트
    lateinit var memoRVAdapter : MemoReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    lateinit var sharedPreferences: SharedPreferences
    private lateinit var myUid : String
    private lateinit var dogId : String

    private lateinit var nowDate : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v : View? = inflater.inflate(R.layout.fragment_memo, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = v!!.context.getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(myUid, "").toString() // 현재 대표 반려견의 id

        // 메모 목록 recycler 어댑터
        memoRVAdapter = MemoReVAdapter(memoDataList)
        memoListView = v!!.findViewById(R.id.memoRecyclerView)
        memoListView.setItemViewCacheSize(20)
        memoListView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        memoListView.layoutManager = layoutManager
        memoListView.adapter = memoRVAdapter

        nowDate = arguments?.getString("nowDate").toString() // 선택된 날짜를 받아옴

        getMemoData(myUid, dogId, nowDate)

        memoPlusBtn = v!!.findViewById(R.id.memoPlusBtn)
        memoPlusBtn.setOnClickListener {
            val intent = Intent(context, PlusMemoActivity::class.java)
            intent.putExtra("date", nowDate) // 선택된 날짜를 넘겨줌
            startActivity(intent)
        }

        memoRVAdapter.setItemClickListener(object: MemoReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                val intent = Intent(context, DogMemoInActivity::class.java)
                intent.putExtra("nowDate", nowDate)
                intent.putExtra("id", memoDataList[position].dogMemoId)
                startActivity(intent)
                // showDialog(v, memoDataList[position].dogMemoId)
            }
        })

        return v
    }

    private fun getMemoData(userId : String, dogId : String, nowDate : String) { // 파이어베이스로부터 메모 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    memoDataList.clear() // 똑같은 데이터 복사 출력되는 것 막기 위한 초기화

                    for(dataModel in dataSnapshot.children) {
                        Log.d(TAG, dataModel.toString())
                        // dataModel.key
                        val item = dataModel.getValue(DogMemoModel::class.java)
                        if(item!!.date == nowDate) { // 선택된 날짜에 맞는 메모 데이터만 추가
                            memoDataList.add(item!!)
                        }
                    }

                    memoRVAdapter.notifyDataSetChanged() // 동기화

                    Log.d("memoDataList", memoDataList.toString())
                } catch (e: Exception) {
                    Log.d(TAG, "메모 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.memoRef.child(userId).child(dogId).addValueEventListener(postListener)
    }
}