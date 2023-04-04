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
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.utils.*
import java.util.ArrayList

class CheckUpFragment : Fragment() {

    private val TAG = CheckUpFragment::class.java.simpleName

    lateinit var checkUpInputPlusBtn : Button
    lateinit var checkUpPicturePlusBtn : Button

    lateinit var sharedPreferences: SharedPreferences
    private lateinit var myUid : String
    private lateinit var dogId : String

    private lateinit var nowDate : String

    private lateinit var checkUpInputListView : RecyclerView
    private val checkUpInputDataList = ArrayList<DogCheckUpInputModel>() // 수치 검사 데이터 리스트
    lateinit var checkUpInputRVAdapter : CheckUpInputReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    private lateinit var checkUpPictureListView : RecyclerView
    private val checkUpPictureDataList = ArrayList<DogCheckUpPictureModel>() // 검사 사진 데이터 리스트
    lateinit var checkUpPictureRVAdapter : CheckUpPictureReVAdapter
    lateinit var pLayoutManager : RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var v : View? = inflater.inflate(R.layout.fragment_check_up, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = v!!.context.getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(myUid, "").toString() // 현재 대표 반려견의 id

        // 수치 검사 목록 recycler 어댑터
        checkUpInputRVAdapter = CheckUpInputReVAdapter(checkUpInputDataList)
        checkUpInputListView = v!!.findViewById(R.id.checkUpInputRecyclerView)
        checkUpInputListView.setItemViewCacheSize(20)
        checkUpInputListView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        checkUpInputListView.layoutManager = layoutManager
        checkUpInputListView.adapter = checkUpInputRVAdapter

        // 검사 사진 목록 recycler 어댑터
        checkUpPictureRVAdapter = CheckUpPictureReVAdapter(checkUpPictureDataList)
        checkUpPictureListView = v!!.findViewById(R.id.checkUpPictureRecyclerView)
        checkUpPictureListView.setItemViewCacheSize(20)
        checkUpPictureListView.setHasFixedSize(true)
        pLayoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        checkUpPictureListView.layoutManager = pLayoutManager
        checkUpPictureListView.adapter = checkUpPictureRVAdapter

        nowDate = arguments?.getString("nowDate").toString() // 선택된 날짜를 받아옴

        getCheckInInputData(myUid, dogId, nowDate)
        getCheckInPictureData(myUid, dogId, nowDate)

        checkUpInputPlusBtn = v!!.findViewById(R.id.checkUpInputPlusBtn)
        checkUpInputPlusBtn.setOnClickListener {
            val intent = Intent(v.context, PlusCheckUpInputActivity::class.java)
            intent.putExtra("date", nowDate)
            startActivity(intent) // 검사 추가 페이지로 이동
        }

        checkUpInputRVAdapter.setItemClickListener(object: CheckUpInputReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                showDialog(v, checkUpInputDataList[position].dogCheckUpInputId)
            }
        })

        checkUpPicturePlusBtn = v!!.findViewById(R.id.checkUpPicturePlusBtn)
        checkUpPicturePlusBtn.setOnClickListener {
            val intent = Intent(v.context, PlusCheckUpPictureActivity::class.java)
            intent.putExtra("date", nowDate)
            startActivity(intent) // 검사 추가 페이지로 이동
        }

        checkUpPictureRVAdapter.setItemClickListener(object: CheckUpPictureReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                val intent = Intent(context, DogCheckUpPictureInActivity::class.java)
                intent.putExtra("date", checkUpPictureDataList[position].date)
                intent.putExtra("id", checkUpPictureDataList[position].dogCheckUpPictureId)
                startActivity(intent)
            }
        })

        return v
    }

    private fun showDialog(v : View, id : String) { // 수치 검사 기록 수정/삭제를 위한 다이얼로그 띄우기
        val mDialogView = LayoutInflater.from(v.context).inflate(R.layout.eat_dialog, null)
        val mBuilder = AlertDialog.Builder(v.context).setView(mDialogView)

        val alertDialog = mBuilder.show()
        val editBtn = alertDialog.findViewById<Button>(R.id.editBtn)
        editBtn?.setOnClickListener { // 수정 버튼 클릭 시
            Log.d(TAG, "edit Button Clicked")

            val intent = Intent(v.context, DogCheckUpInputEditActivity::class.java)
            intent.putExtra("id", id) // mealId 전송
            startActivity(intent) // 수정 페이지로 이동

            alertDialog.dismiss()
        }

        val rmBtn = alertDialog.findViewById<Button>(R.id.rmBtn)
        rmBtn?.setOnClickListener {  // 삭제 버튼 클릭 시
            Log.d(TAG, "remove Button Clicked")
            alertDialog.dismiss()

            FBRef.checkUpInputRef.child(myUid).child(dogId).child(id).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제
        }
    }

    private fun getCheckInInputData(userId : String, dogId : String, nowDate : String) { // 파이어베이스로부터 검사 수치 기록 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    checkUpInputDataList.clear() // 똑같은 데이터 복사 출력되는 것 막기 위한 초기화

                    for(dataModel in dataSnapshot.children) {
                        Log.d(TAG, dataModel.toString())
                        // dataModel.key
                        val item = dataModel.getValue(DogCheckUpInputModel::class.java)
                        if(item!!.date == nowDate) { // 선택된 날짜에 맞는 수치 기록 데이터만 추가
                            checkUpInputDataList.add(item!!)
                        }
                    }

                    checkUpInputRVAdapter.notifyDataSetChanged() // 동기화

                    Log.d("checkUpInputDataList", checkUpInputDataList.toString())
                } catch (e: Exception) {
                    Log.d(TAG, "검사 수치 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.checkUpInputRef.child(userId).child(dogId).addValueEventListener(postListener)
    }

    private fun getCheckInPictureData(userId : String, dogId : String, nowDate : String) { // 파이어베이스로부터 검사 사진 기록 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    checkUpPictureDataList.clear() // 똑같은 데이터 복사 출력되는 것 막기 위한 초기화

                    for(dataModel in dataSnapshot.children) {
                        Log.d(TAG, dataModel.toString())
                        // dataModel.key
                        val item = dataModel.getValue(DogCheckUpPictureModel::class.java)
                        if(item!!.date == nowDate) { // 선택된 날짜에 맞는 사진 기록 데이터만 추가
                            checkUpPictureDataList.add(item!!)
                        }
                    }

                    checkUpPictureRVAdapter.notifyDataSetChanged() // 동기화

                    Log.d("checkUpPictureDataList", checkUpPictureDataList.toString())
                } catch (e: Exception) {
                    Log.d(TAG, "검사 사진 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.checkUpPictureRef.child(userId).child(dogId).addValueEventListener(postListener)
    }
}