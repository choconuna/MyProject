package org.techtown.myproject.statistics

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.view.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogCheckUpInputModel
import org.techtown.myproject.utils.FBRef
import java.util.*

class CheckUpStatisticsFragment : Fragment() {

    private val TAG = CheckUpStatisticsFragment::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    private lateinit var myUid : String
    private lateinit var dogId : String

    lateinit var spinner : Spinner
    private var checkUpNameList : MutableMap<String, Int> = mutableMapOf()
    var selectedCheckUpInputItem : String = ""

    private lateinit var checkUpInputProgressRecyclerView: RecyclerView

    private var checkUpInputMap : MutableMap<DogCheckUpInputModel, Int> = mutableMapOf()
    private val checkUpInputList = ArrayList<DogCheckUpInputModel>() // 검사 목록 리스트
    lateinit var checkUpInputStatisticsRVAdapter : CheckUpInputStatisticsReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v : View? = inflater.inflate(R.layout.fragment_check_up_statistics, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = v!!.context.getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(myUid, "").toString() // 현재 대표 반려견의 id

        setData(v)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                selectedCheckUpInputItem = parent.getItemAtPosition(position).toString()
                Log.d("selectedCheckUpItem", selectedCheckUpInputItem)
                setShowChart(v, selectedCheckUpInputItem)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        return v
    }

    private fun setData(v : View) {
        checkUpInputProgressRecyclerView = v.findViewById(R.id.checkUpInputProgressRecyclerView)
        checkUpInputStatisticsRVAdapter = CheckUpInputStatisticsReVAdapter(checkUpInputList)
        checkUpInputProgressRecyclerView.setItemViewCacheSize(20)
        checkUpInputProgressRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        checkUpInputProgressRecyclerView.layoutManager = layoutManager
        checkUpInputProgressRecyclerView.adapter = checkUpInputStatisticsRVAdapter

        spinner = v.findViewById(R.id.spinner)

        getCheckInInputData(myUid, dogId)
        dynamicSpinner(v, spinner, checkUpNameList)

        val sortedCheckUpInputMap = sortMapByKey(checkUpInputMap)
        if(spinner.size > 0)
            setCheckInInputData(spinner.getItemAtPosition(0).toString(), sortedCheckUpInputMap)
    }

    private fun setShowChart(v : View, selectedCheckUpInputItem : String) {

        getCheckInInputData(myUid, dogId)
//        dynamicSpinner(v, spinner, checkUpNameList)
//        Log.d("spinnerSize", spinner.size.toString())

        val sortedCheckUpInputMap = sortMapByKey(checkUpInputMap)
        setCheckInInputData(selectedCheckUpInputItem, sortedCheckUpInputMap)
    }

    private fun getCheckInInputData(userId : String, dogId : String) { // 파이어베이스로부터 검사 수치 기록 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    checkUpNameList.clear() // 똑같은 데이터 복사 출력되는 것 막기 위한 초기화
                    checkUpInputMap.clear()

                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogCheckUpInputModel::class.java)

                        val date = item!!.date
                        val sp = date.split(".")

                        var dayNum = ""
                        if(sp[1].length == 1 && sp[2].length ==1) {
                            dayNum = sp[0] + "0" + sp[1] + "0" + sp[2]
                        } else if(sp[1].length == 1 && sp[2].length == 2) {
                            dayNum = sp[0] + "0" + sp[1] + sp[2]
                        } else if(sp[1].length == 2 && sp[2].length == 1) {
                            dayNum = sp[0] + sp[1] + "0" + sp[2]
                        } else if(sp[1].length == 2 && sp[2].length == 2) {
                            dayNum = sp[0] + sp[1] + sp[2]
                        }

                        checkUpInputMap[item!!] = dayNum.toInt()
                        checkUpNameList[item!!.name] = 0
                    }
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

    private fun setCheckInInputData(selectedCheckUpInputItem : String, checkUpInputMap : MutableMap<DogCheckUpInputModel, Int>) { // 파이어베이스로부터 검사 수치 기록 데이터 불러오기
        checkUpInputList.clear()

        for((key, value) in checkUpInputMap.entries) {
            if(key.name == selectedCheckUpInputItem) {
                checkUpInputList.add(key)
            }
        }

        checkUpInputStatisticsRVAdapter.notifyDataSetChanged()
    }

    private fun sortMapByKey(map: MutableMap<DogCheckUpInputModel, Int>): LinkedHashMap<DogCheckUpInputModel, Int> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.value }

        val result = LinkedHashMap<DogCheckUpInputModel, Int>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }

    private fun dynamicSpinner(v : View, spinner : Spinner, spinnerName : MutableMap<String, Int>) {
        var spinnerNameList : ArrayList<String> = ArrayList()

        for((key, value) in spinnerName.entries) {
            spinnerNameList.add(key)
        }

        val adapter = ArrayAdapter(v.context, android.R.layout.simple_list_item_1, spinnerNameList)
        Log.d("spinnerNameList", spinnerNameList.toString())

        spinner.adapter = adapter
    }
}