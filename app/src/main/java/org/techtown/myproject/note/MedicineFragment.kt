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
import android.widget.TextView
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

class MedicineFragment : Fragment() {

    private val TAG = HealthFragment::class.java.simpleName

    lateinit var medicinePlanPlusBtn : Button
    lateinit var medicinePlusBtn : Button

    lateinit var sharedPreferences: SharedPreferences
    private lateinit var myUid : String
    private lateinit var dogId : String

    private lateinit var nowDate : String

    private lateinit var medicinePlanListView : RecyclerView
    private val medicinePlanDataList = ArrayList<DogMedicinePlanModel>() // 투약 일정 목록 리스트
    lateinit var medicinePlanRVAdapter : MedicinePlanReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    private lateinit var medicineListView : RecyclerView
    private val medicineDataList = ArrayList<DogMedicineModel>() // 투약 기록 목록 리스트
    lateinit var medicineRVAdapter : MedicineReVAdapter
    lateinit var mLayoutManager : RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var v : View? = inflater.inflate(R.layout.fragment_medicine, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = v!!.context.getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(myUid, "").toString() // 현재 대표 반려견의 id

        // 투약 일정 목록 recycler 어댑터
        medicinePlanRVAdapter = MedicinePlanReVAdapter(medicinePlanDataList)
        medicinePlanListView = v!!.findViewById(R.id.medicinePlanRecyclerView)
        medicinePlanListView.setItemViewCacheSize(20)
        medicinePlanListView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        medicinePlanListView.layoutManager = layoutManager
        medicinePlanListView.adapter = medicinePlanRVAdapter

        // 투약 기록 목록 recycler 어댑터
        medicineRVAdapter = MedicineReVAdapter(medicineDataList)
        medicineListView = v!!.findViewById(R.id.medicineRecyclerView)
        medicineListView.setItemViewCacheSize(20)
        medicineListView.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        medicineListView.layoutManager = mLayoutManager
        medicineListView.adapter = medicineRVAdapter

        nowDate = arguments?.getString("nowDate").toString() // 선택된 날짜를 받아옴

        getMeidicinePlanData(myUid, dogId, nowDate)
        getMedicineData(myUid, dogId, nowDate)

        medicinePlanPlusBtn = v!!.findViewById(R.id.medicinePlanPlusBtn)
        medicinePlanPlusBtn.setOnClickListener {
            val intent = Intent(context, PlusMedicinePlanActivity::class.java)
            startActivity(intent)
        }

        medicinePlanRVAdapter.setItemClickListener(object: MedicinePlanReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                showPlanDialog(v, position, medicinePlanDataList[position].dogMedicinePlanId)
            }
        })

        medicinePlusBtn = v!!.findViewById(R.id.medicinePlusBtn)
        medicinePlusBtn.setOnClickListener {
            val intent = Intent(context, PlusMedicineActivity::class.java)
            startActivity(intent)
        }

        medicineRVAdapter.setItemClickListener(object: MedicineReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                showDialog(v, position, medicineDataList[position].dogMedicineId)
            }
        })

        return v
    }

    private fun showPlanDialog(v : View, position : Int, id : String) { // 투약 일정 기록 수정/삭제를 위한 다이얼로그 띄우기
        val mDialogView = LayoutInflater.from(v.context).inflate(R.layout.medicine_plan_dialog, null)
        val mBuilder = AlertDialog.Builder(v.context).setView(mDialogView)

        val alertDialog = mBuilder.show()

        val checkBtn = alertDialog.findViewById<Button>(R.id.checkBtn)
        checkBtn?.setOnClickListener {
        }

        val editBtn = alertDialog.findViewById<Button>(R.id.editBtn)
        editBtn?.setOnClickListener { // 수정 버튼 클릭 시
            Log.d(TAG, "edit Button Clicked")

            val intent = Intent(v.context, DogMedicinePlanEditActivity::class.java)
            intent.putExtra("id", id) // dogMedicinePlan id 전송
            startActivity(intent) // 수정 페이지로 이동

            alertDialog.dismiss()
        }

        val rmBtn = alertDialog.findViewById<Button>(R.id.rmBtn)
        rmBtn?.setOnClickListener {  // 삭제 버튼 클릭 시
            Log.d(TAG, "remove Button Clicked")
            alertDialog.dismiss()

            FBRef.medicinePlanRef.child(myUid).child(dogId).child(id).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제
        }
    }

    private fun showDialog(v : View, position : Int, id : String) { // 투약 기록 수정/삭제를 위한 다이얼로그 띄우기
        val mDialogView = LayoutInflater.from(v.context).inflate(R.layout.eat_dialog, null)
        val mBuilder = AlertDialog.Builder(v.context).setView(mDialogView)

        val alertDialog = mBuilder.show()

        val editBtn = alertDialog.findViewById<Button>(R.id.editBtn)
        editBtn?.setOnClickListener { // 수정 버튼 클릭 시
            Log.d(TAG, "edit Button Clicked")

            val intent = Intent(v.context, DogMedicineEditActivity::class.java)
            intent.putExtra("id", id) // dogMedicine id 전송
            startActivity(intent) // 수정 페이지로 이동

            alertDialog.dismiss()
        }

        val rmBtn = alertDialog.findViewById<Button>(R.id.rmBtn)
        rmBtn?.setOnClickListener {  // 삭제 버튼 클릭 시
            Log.d(TAG, "remove Button Clicked")
            alertDialog.dismiss()

            FBRef.medicineRef.child(myUid).child(dogId).child(id).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제
        }
    }

    private fun getMeidicinePlanData(userId : String, dogId : String, nowDate : String) { // 파이어베이스로부터 투약 일정 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 투약 일정 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                    medicinePlanDataList.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogMedicinePlanModel::class.java)

                        if(item!!.repeat == "하루") {

                            val startDate = item!!.startDate
                            val startDateSplit = startDate.split(".")
                            val nowDateSplit = nowDate.split(".")

                            if(nowDateSplit[0].toInt() == startDateSplit[0].toInt() && nowDateSplit[1].toInt() == startDateSplit[1].toInt() && nowDateSplit[2].toInt() == startDateSplit[2].toInt())
                                medicinePlanDataList.add(item!!)

                        } else if (item!!.repeat == "매일") {

                            val startDate = item!!.startDate
                            val startDateSplit = startDate.split(".")

                            val endDate = item!!.endDate
                            val endDateSplit = endDate.split(".")

                            val nowDateSplit = nowDate.split(".")

                            if (nowDateSplit[0].toInt() >= startDateSplit[0].toInt() && nowDateSplit[0].toInt() <= endDateSplit[0].toInt()) { // 투약 일정 내에 있는 데이터만 추가
                                if (nowDateSplit[0] > startDateSplit[0] && nowDateSplit[0] < endDateSplit[0]) // 시작 연도와 종료 연도 사이에 있는 연도일 경우 모든 날짜에 일정이 추가되도록
                                    medicinePlanDataList.add(item!!)
                                else if (nowDateSplit[0].toInt() == startDateSplit[0].toInt() && nowDateSplit[0].toInt() == endDateSplit[0].toInt()) { // 시작 연도와 종료 연도가 같을 경우
                                    if (nowDateSplit[1].toInt() > startDateSplit[1].toInt() && nowDateSplit[1].toInt() < endDateSplit[1].toInt()) { // 시작 날짜와 종료 날짜 달 사이에 있을 경우
                                        medicinePlanDataList.add(item!!)
                                    } else if (nowDateSplit[1].toInt() == startDateSplit[1].toInt()) { // 시작 날짜와 같은 달일 경우
                                        if (nowDateSplit[2].toInt() >= startDateSplit[2].toInt()) // 시작 날짜 이후의 날에 일정 추가
                                            medicinePlanDataList.add(item!!)
                                    } else if (nowDateSplit[1].toInt() == endDateSplit[1].toInt()) { // 종료 날짜와 같은 달일 경우
                                        if (nowDateSplit[2].toInt() <= endDateSplit[2].toInt()) // 종료 날짜 이전의 날에 일정 추가
                                            medicinePlanDataList.add(item!!)
                                    }
                                } else if (nowDateSplit[0].toInt() == startDateSplit[0].toInt()) { // 시작 연도와 현재 연도가 같고 종료 연도 이전일 경우
                                    if (nowDateSplit[1].toInt() == startDateSplit[1].toInt()) {
                                        if (nowDateSplit[2].toInt() >= startDateSplit[2].toInt())
                                            medicinePlanDataList.add(item!!)
                                    } else if (nowDateSplit[1].toInt() > startDateSplit[1].toInt())
                                        medicinePlanDataList.add(item!!)
                                } else if (nowDateSplit[0].toInt() == endDateSplit[0].toInt()) { // 종료 연도와 현재 연도가 같을 경우
                                    if (nowDateSplit[1].toInt() == endDateSplit[1].toInt()) {
                                        if (nowDateSplit[2].toInt() <= endDateSplit[2].toInt())
                                            medicinePlanDataList.add(item!!)
                                    } else if (nowDateSplit[1].toInt() < endDateSplit[1].toInt())
                                        medicinePlanDataList.add(item!!)
                                }
                            }
                        }
                    }

                    Log.d("medicinePlanDataList", medicinePlanDataList.toString())
                    medicinePlanRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, " 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.medicinePlanRef.child(userId).child(dogId).addValueEventListener(postListener)
    }

    private fun getMedicineData(userId : String, dogId : String, nowDate : String) { // 파이어베이스로부터 투약 기록 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 투약 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                    medicineDataList.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogMedicineModel::class.java)

                        val date = item!!.date
                        val dateSplit = date.split(".")
                        val nowDateSplit = nowDate.split(".")

                        if(nowDateSplit[0].toInt() == dateSplit[0].toInt() && nowDateSplit[1].toInt() == dateSplit[1].toInt() && nowDateSplit[2].toInt() == dateSplit[2].toInt())
                            medicineDataList.add(item!!) // 현재 날짜에 해당하는 투약 기록 데이터만 추가
                    }

                    Log.d("medicineDataList", medicineDataList.toString())
                    medicineRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, " 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.medicineRef.child(userId).child(dogId).addValueEventListener(postListener)
    }
}