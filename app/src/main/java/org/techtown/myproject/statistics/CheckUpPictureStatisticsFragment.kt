package org.techtown.myproject.statistics

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import androidx.core.view.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.note.CheckUpInputReVAdapter
import org.techtown.myproject.note.DogCheckUpPictureInActivity
import org.techtown.myproject.utils.DogCheckUpInputModel
import org.techtown.myproject.utils.DogCheckUpPictureModel
import org.techtown.myproject.utils.FBRef
import java.util.*

class CheckUpPictureStatisticsFragment : Fragment() {

    private val TAG = CheckUpPictureStatisticsFragment::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    private lateinit var myUid : String
    private lateinit var dogId : String

    lateinit var spinner : Spinner
    lateinit var selectedCheckUpInputItem : String

    private lateinit var checkUpPictureProgressRecyclerView: RecyclerView

    private var checkUpPictureMap : MutableMap<DogCheckUpPictureModel, Int> = mutableMapOf()
    private val checkUpPictureList = ArrayList<DogCheckUpPictureModel>() // 검사 목록 리스트
    lateinit var checkUpPictureStatisticsRVAdapter : CheckUpPictureStatisticsReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_check_up_picture_statistics, container, false)

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

        checkUpPictureStatisticsRVAdapter.setItemClickListener(object: CheckUpPictureStatisticsReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val intent = Intent(context, CheckUpPictureStatisticsInActivity::class.java)
                intent.putExtra("date", checkUpPictureList[position].date)
                intent.putExtra("id", checkUpPictureList[position].dogCheckUpPictureId)
                startActivity(intent)
            }
        })

        return v
    }

    private fun setData(v : View) {
        checkUpPictureProgressRecyclerView = v.findViewById(R.id.checkUpPictureProgressRecyclerView)
        checkUpPictureStatisticsRVAdapter = CheckUpPictureStatisticsReVAdapter(checkUpPictureList)
        checkUpPictureProgressRecyclerView.setItemViewCacheSize(20)
        checkUpPictureProgressRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        checkUpPictureProgressRecyclerView.layoutManager = layoutManager
        checkUpPictureProgressRecyclerView.adapter = checkUpPictureStatisticsRVAdapter

        spinner = v.findViewById(R.id.spinner)

        getCheckInInputData(myUid, dogId)

        val sortedCheckUpInputMap = sortMapByKey(checkUpPictureMap)
        setCheckInInputData(spinner.getItemAtPosition(0).toString(), sortedCheckUpInputMap)
    }

    private fun setShowChart(v : View, selectedCheckUpInputItem : String) {

        getCheckInInputData(myUid, dogId)

        val sortedCheckUpInputMap = sortMapByKey(checkUpPictureMap)
        setCheckInInputData(selectedCheckUpInputItem, sortedCheckUpInputMap)
    }

    private fun getCheckInInputData(userId : String, dogId : String) { // 파이어베이스로부터 검사 수치 기록 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    checkUpPictureMap.clear()

                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogCheckUpPictureModel::class.java)

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

                        checkUpPictureMap[item!!] = dayNum.toInt()
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
        FBRef.checkUpPictureRef.child(userId).child(dogId).addValueEventListener(postListener)
    }

    private fun setCheckInInputData(selectedCheckUpPictureItem : String, checkUpPictureMap : MutableMap<DogCheckUpPictureModel, Int>) { // 파이어베이스로부터 검사 수치 기록 데이터 불러오기
        checkUpPictureList.clear()

        for((key, value) in checkUpPictureMap.entries) {
            if(key.checkUpCategory == selectedCheckUpInputItem) {
                checkUpPictureList.add(key)
            }
        }

        checkUpPictureStatisticsRVAdapter.notifyDataSetChanged()
    }

    private fun sortMapByKey(map: MutableMap<DogCheckUpPictureModel, Int>): LinkedHashMap<DogCheckUpPictureModel, Int> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.value }

        val result = LinkedHashMap<DogCheckUpPictureModel, Int>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }
}