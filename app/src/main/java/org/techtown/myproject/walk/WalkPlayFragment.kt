package org.techtown.myproject.walk


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.my.DogProfileInActivity
import org.techtown.myproject.my.DogReVAdapter
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef
import java.util.*

class WalkPlayFragment : Fragment() {

    lateinit var myUid: String

    private val TAG = WalkPlayFragment::class.java.simpleName

    lateinit var walkDogRecyclerView: RecyclerView
    private val walkDogReDataList = ArrayList<DogModel>() // 각 반려견의 프로필을 넣는 리스트
    private val dogKeyList = mutableListOf<String>() // 각 반려견의 키값을 넣는 리스트
    lateinit var walkDogReVAdapter: WalkDogReVAdapter
    lateinit var layoutManager: RecyclerView.LayoutManager
    private var isSelected : MutableMap<String, Boolean> = mutableMapOf() // 반려견이 선택되었는지 안 되었는지

    private lateinit var walkStartBtn: Button // 산책하기 버튼

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(R.layout.fragment_walk_play, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        setData(v!!)
        getFBDogData()

        walkDogReVAdapter.setItemClickListener(object: WalkDogReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                if(isSelected[dogKeyList[position]] == false) { // 반려견이 선택되었을 경우
                    isSelected[dogKeyList[position]] = true // 선택되었음을 표시
                    Log.d("isSelected", isSelected.toString())
                } else if(isSelected[dogKeyList[position]] == true) { // 선택되었음에도 다시 선택했을 경우
                    isSelected[dogKeyList[position]] = false // 선택 취소되었음을 표시
                    Log.d("isSelected", isSelected.toString())
                }
            }
        })

        walkStartBtn.setOnClickListener {
            var totalDogNum = isSelected.size

            var checkedNum = 0
            for((key, value) in isSelected) {
                if(value)
                    checkedNum += 1
            }

            if(checkedNum == 0) { // 산책할 반려견을 선택하지 않았을 경우
                Toast.makeText(v!!.context, "산책할 반려견을 하나 이상 선택하세요!", Toast.LENGTH_SHORT).show()
            } else if(checkedNum > 0) { // 산책할 반려견을 선택했을 경우
                val intent = Intent(v!!.context, StartWalkActivity::class.java)
                var checkedDogId = ArrayList<String>()
                var i = 0
                for((key, value) in isSelected) {
                    if(value) {
                        Log.d("checkedDogId", key)
                        checkedDogId.add(key)
                        i += 1
                    }
                }

                var dogIdString = Array(i) { _ -> "" }
                var index = 0
                for((key, value) in isSelected) {
                    if(value) {
                        dogIdString[index] = key
                        index += 1
                    }
                }

                Log.d("checkedDogId", checkedDogId.toString())
                Log.d("checkedDogId", dogIdString.contentToString())

                intent.putExtra("checkedDogIdList", dogIdString) // 산책할 반려견의 dogId 리스트를 넘겨줌
                startActivity(intent)
            }
        }


        return v
    }

    private fun setData(v : View) {
        walkStartBtn = v.findViewById(R.id.walkStartBtn)

        walkDogReVAdapter = WalkDogReVAdapter(walkDogReDataList)
        walkDogRecyclerView = v!!.findViewById(R.id.walkDogRecyclerView)
        walkDogRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        walkDogRecyclerView.layoutManager = layoutManager
        walkDogRecyclerView.adapter = walkDogReVAdapter
    }

    private fun getFBDogData() { // 파이어베이스로부터 반려견 프로필 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                dogKeyList.clear()
                walkDogReDataList.clear()
                isSelected.clear()
                // dogDataList.clear() // 똑같은 데이터 복사 출력되는 것 막기 위한 초기화

                for(dataModel in dataSnapshot.children) {
                    Log.d(TAG, dataModel.toString())
                    val item = dataModel.getValue(DogModel::class.java)
                    walkDogReDataList.add(item!!)
                    dogKeyList.add(dataModel.key!!)
                    isSelected[dataModel.key!!] = false
                    Log.d("key", dogKeyList.toString())
                    // dogDataList.add(item!!)
                }

                walkDogReVAdapter.notifyDataSetChanged()

                Log.d(TAG, walkDogReDataList.toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dogRef.child(myUid).addValueEventListener(postListener)
    }
}