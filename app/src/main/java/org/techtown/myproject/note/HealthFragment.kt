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
import android.widget.ProgressBar
import android.widget.TextView
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
import org.techtown.myproject.utils.*
import java.util.ArrayList

class HealthFragment : Fragment() {

    private val TAG = HealthFragment::class.java.simpleName

    lateinit var peePlusBtn : Button
    lateinit var dungPlusBtn : Button
    lateinit var vomitPlusBtn : Button
    lateinit var heartPlusBtn : Button

    private lateinit var peeListView : RecyclerView
    private val peeDataList = ArrayList<DogPeeModel>() // 소변 목록 리스트
    lateinit var peeRVAdapter : PeeReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    private lateinit var dungListView : RecyclerView
    private val dungDataList = ArrayList<DogDungModel>() // 대변 목록 리스트
    lateinit var dungRVAdapter : DungReVAdapter
    lateinit var dLayoutManager : RecyclerView.LayoutManager

    private lateinit var vomitListView : RecyclerView
    private val vomitDataList = ArrayList<DogVomitModel>() // 구토 목록 리스트
    lateinit var vomitRVAdapter : VomitReVAdapter
    lateinit var vLayoutManager : RecyclerView.LayoutManager

    private lateinit var heartListView : RecyclerView
    private val heartDataList = ArrayList<DogHeartModel>() // 호흡수 목록 리스트
    lateinit var heartRVAdapter : HeartReVAdapter
    lateinit var hLayoutManager : RecyclerView.LayoutManager

    lateinit var sharedPreferences: SharedPreferences
    private lateinit var myUid : String
    private lateinit var dogId : String

    private lateinit var nowDate : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var v : View? = inflater.inflate(R.layout.fragment_health, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = v!!.context.getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(myUid, "").toString() // 현재 대표 반려견의 id

        // 소변 목록 recycler 어댑터
        peeRVAdapter = PeeReVAdapter(peeDataList)
        peeListView = v!!.findViewById(R.id.peeRecyclerView)
        peeListView.setItemViewCacheSize(20)
        peeListView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        peeListView.layoutManager = layoutManager
        peeListView.adapter = peeRVAdapter

        // 대변 목록 recycler 어댑터
        dungRVAdapter = DungReVAdapter(dungDataList)
        dungListView = v!!.findViewById(R.id.dungRecyclerView)
        dungListView.setItemViewCacheSize(20)
        dungListView.setHasFixedSize(true)
        dLayoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        dungListView.layoutManager = dLayoutManager
        dungListView.adapter = dungRVAdapter

        // 구토 목록 recycler 어댑터
        vomitRVAdapter = VomitReVAdapter(vomitDataList)
        vomitListView = v!!.findViewById(R.id.vomitRecyclerView)
        vomitListView.setItemViewCacheSize(20)
        vomitListView.setHasFixedSize(true)
        vLayoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        vomitListView.layoutManager = vLayoutManager
        vomitListView.adapter = vomitRVAdapter

        // 호흡수 목록 recycler 어댑터
        heartRVAdapter = HeartReVAdapter(heartDataList)
        heartListView = v!!.findViewById(R.id.heartRecyclerView)
        heartListView.setItemViewCacheSize(20)
        heartListView.setHasFixedSize(true)
        hLayoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        heartListView.layoutManager = hLayoutManager
        heartListView.adapter = heartRVAdapter

        nowDate = arguments?.getString("nowDate").toString() // 선택된 날짜를 받아옴

        getPeeData(myUid, dogId, nowDate)
        getDungData(myUid, dogId, nowDate)
        getVomitData(myUid, dogId, nowDate)
        getHeartData(myUid, dogId, nowDate)

        peePlusBtn = v!!.findViewById(R.id.peePlusBtn)
        peePlusBtn.setOnClickListener {
            val intent = Intent(context, PlusPeeActivity::class.java)
            intent.putExtra("date", nowDate) // 선택된 날짜를 넘겨줌
            startActivity(intent)
        }

        peeRVAdapter.setItemClickListener(object: PeeReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                showDialog(v, "pee", peeDataList[position].dogPeeId, peeDataList[position].peeType)
            }
        })

        dungPlusBtn = v!!.findViewById(R.id.dungPlusBtn)
        dungPlusBtn.setOnClickListener {
            val intent = Intent(context, PlusDungActivity::class.java)
            intent.putExtra("date", nowDate) // 선택된 날짜를 넘겨줌
            startActivity(intent)
        }

        dungRVAdapter.setItemClickListener(object: DungReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                showDialog(v, "dung", dungDataList[position].dogDungId, dungDataList[position].dungType)
            }
        })

        vomitPlusBtn = v!!.findViewById(R.id.vomitPlusBtn)
        vomitPlusBtn.setOnClickListener {
            val intent = Intent(context, PlusVomitActivity::class.java)
            intent.putExtra("date", nowDate) // 선택된 날짜를 넘겨줌
            startActivity(intent)
        }

        vomitRVAdapter.setItemClickListener(object: VomitReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                showDialog(v, "vomit", vomitDataList[position].dogVomitId, vomitDataList[position].vomitType)
            }
        })

        heartPlusBtn = v!!.findViewById(R.id.heartPlusBtn)
        heartPlusBtn.setOnClickListener {
            val intent = Intent(context, PlusHeartActivity::class.java)
            intent.putExtra("date", nowDate) // 선택된 날짜를 넘겨줌
            startActivity(intent)
        }

        heartRVAdapter.setItemClickListener(object: HeartReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                showHeartDialog(v, heartDataList[position].dogHeartId)
            }
        })

        return v
    }

    private fun showDialog(v : View, category : String, id : String, type : String) { // 기록 수정/삭제를 위한 다이얼로그 띄우기
        val mDialogView = LayoutInflater.from(v.context).inflate(R.layout.eat_dialog, null)
        val mBuilder = AlertDialog.Builder(v.context).setView(mDialogView)

        val alertDialog = mBuilder.show()
        val editBtn = alertDialog.findViewById<Button>(R.id.editBtn)
        editBtn?.setOnClickListener { // 수정 버튼 클릭 시
            Log.d(TAG, "edit Button Clicked")

            when (category) {
                "pee" -> {
                    val intent = Intent(v.context, DogPeeEditActivity::class.java)
                    intent.putExtra("id", id) // peeId 전송
                    intent.putExtra("date", nowDate) // 선택된 날짜 전송
                    intent.putExtra("peeType", type) // 소변 타입 전송
                    startActivity(intent) // 수정 페이지로 이동
                }
                "dung" -> {
                    val intent = Intent(v.context, DogDungEditActivity::class.java)
                    intent.putExtra("id", id) // dungId 전송
                    intent.putExtra("date", nowDate) // 선택된 날짜 전송
                    intent.putExtra("dungType", type) // 대변 타입 전송
                    startActivity(intent) // 수정 페이지로 이동
                }
                "vomit" -> {
                    val intent = Intent(v.context, DogVomitEditActivity::class.java)
                    intent.putExtra("id", id) // vomitId 전송
                    intent.putExtra("date", nowDate) // 선택된 날짜 전송
                    intent.putExtra("vomitType", type) // 구토 타입 전송
                    startActivity(intent) // 수정 페이지로 이동
                }
            }
            alertDialog.dismiss()
        }

        val rmBtn = alertDialog.findViewById<Button>(R.id.rmBtn)
        rmBtn?.setOnClickListener {  // 삭제 버튼 클릭 시
            Log.d(TAG, "remove Button Clicked")
            alertDialog.dismiss()

            when (category) {
                "pee" -> {
                    FBRef.peeRef.child(myUid).child(dogId).child(id).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제
                }
                "dung" -> {
                    FBRef.dungRef.child(myUid).child(dogId).child(id).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제
                }
                "vomit" -> {
                    FBRef.vomitRef.child(myUid).child(dogId).child(id).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제
                }
            }
        }
    }

    private fun showHeartDialog(v : View, id : String) { // 기록 삭제를 위한 다이얼로그 띄우기
        val mDialogView = LayoutInflater.from(v.context).inflate(R.layout.heart_delete_dialog, null)
        val mBuilder = AlertDialog.Builder(v.context).setView(mDialogView)

        val alertDialog = mBuilder.show()
        val editBtn = alertDialog.findViewById<Button>(R.id.noBtn)
        editBtn?.setOnClickListener { // 아니오 버튼 클릭 시
            Log.d(TAG, "edit Button Clicked")
            alertDialog.dismiss()
        }

        val rmBtn = alertDialog.findViewById<Button>(R.id.deleteBtn)
        rmBtn?.setOnClickListener {  // 예 버튼 클릭 시
            Log.d(TAG, "remove Button Clicked")

            FBRef.heartRef.child(myUid).child(dogId).child(id).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제

            alertDialog.dismiss()
        }
    }

    private fun getPeeData(userId : String, dogId : String, nowDate : String) { // 파이어베이스로부터 소변 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 소변 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                    peeDataList.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogPeeModel::class.java)
                        if(item!!.date == nowDate) { // 선택된 날짜에 맞는 소변 데이터만 추가
                            peeDataList.add(item!!)
                        }
                    }

                    Log.d("peeDataList", peeDataList.toString())
                    peeRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, " 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.peeRef.child(userId).child(dogId).addValueEventListener(postListener)
    }

    private fun getDungData(userId : String, dogId : String, nowDate : String) { // 파이어베이스로부터 대변 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 대변 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                    dungDataList.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogDungModel::class.java)
                        if(item!!.date == nowDate) { // 선택된 날짜에 맞는 대변 데이터만 추가
                            dungDataList.add(item!!)
                        }
                    }

                    Log.d("dungDataList", dungDataList.toString())
                    dungRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, " 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dungRef.child(userId).child(dogId).addValueEventListener(postListener)
    }

    private fun getVomitData(userId : String, dogId : String, nowDate : String) { // 파이어베이스로부터 구토 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 구토 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                    vomitDataList.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogVomitModel::class.java)
                        if(item!!.date == nowDate) { // 선택된 날짜에 맞는 구토 데이터만 추가
                            vomitDataList.add(item!!)
                        }
                    }

                    Log.d("vomitDataList", vomitDataList.toString())
                    vomitRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, " 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.vomitRef.child(userId).child(dogId).addValueEventListener(postListener)
    }

    private fun getHeartData(userId : String, dogId : String, nowDate : String) { // 파이어베이스로부터 호흡수 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 호흡수 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                    heartDataList.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogHeartModel::class.java)
                        if(item!!.date == nowDate) { // 선택된 날짜에 맞는 호흡수 데이터만 추가
                            heartDataList.add(item!!)
                        }
                    }

                    Log.d("heartDataList", heartDataList.toString())
                    heartRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, " 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.heartRef.child(userId).child(dogId).addValueEventListener(postListener)
    }
}