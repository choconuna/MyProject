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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.community.SpecificCommunityEditActivity
import org.techtown.myproject.my.DogProfileInActivity
import org.techtown.myproject.my.DogReVAdapter
import org.techtown.myproject.utils.*
import java.util.*

class EatFragment() : Fragment() {

    private val TAG = EatFragment::class.java.simpleName

    lateinit var mealPlusBtn : Button
    lateinit var snackPlusBtn : Button
    lateinit var tonicPlusBtn : Button
    lateinit var waterPlusBtn : Button

    private lateinit var totalMealArea : TextView
    private lateinit var totalSnackArea : TextView
    private lateinit var totalTonicArea : TextView

    private lateinit var mealListView : RecyclerView
    private val mealDataList = ArrayList<DogMealModel>() // 사료 목록 리스트
    lateinit var mealRVAdapter : MealReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    private lateinit var snackListView : RecyclerView
    private val snackDataList = ArrayList<DogSnackModel>() // 간식 목록 리스트
    lateinit var snackRVAdapter : SnackReVAdapter
    lateinit var sLayoutManager : RecyclerView.LayoutManager

    private lateinit var tonicListView : RecyclerView
    private val tonicDataList = ArrayList<DogTonicModel>() // 영양제 목록 리스트
    lateinit var tonicRVAdapter : TonicReVAdapter
    lateinit var tLayoutManager : RecyclerView.LayoutManager

    private lateinit var totalWeightProgress : ProgressBar
    private lateinit var waterListView : RecyclerView
    private val waterDataList = ArrayList<DogWaterModel>() // 물 목록 리스트
    lateinit var waterRVAdapter : WaterReVAdapter
    lateinit var wLayoutManager : RecyclerView.LayoutManager
    var nowWeight = 0

    lateinit var sharedPreferences: SharedPreferences
    private lateinit var myUid : String
    private lateinit var dogId : String

    private lateinit var nowDate : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var v : View? = inflater.inflate(R.layout.fragment_eat, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = v!!.context.getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(myUid, "").toString() // 현재 대표 반려견의 id
        Log.d("mainDogId", dogId)

        totalMealArea = v!!.findViewById(R.id.totalMealArea)
        totalSnackArea = v!!.findViewById(R.id.totalSnackArea)
        totalTonicArea = v!!.findViewById(R.id.totalTonicArea)

        // 사료 목록 recycler 어댑터
        mealRVAdapter = MealReVAdapter(mealDataList)
        mealListView = v!!.findViewById(R.id.mealRecyclerView)
        mealListView.setItemViewCacheSize(20)
        mealListView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        mealListView.layoutManager = layoutManager
        mealListView.adapter = mealRVAdapter

        // 간식 목록 recycler 어댑터
        snackRVAdapter = SnackReVAdapter(snackDataList)
        snackListView = v!!.findViewById(R.id.snackRecyclerView)
        snackListView.setItemViewCacheSize(20)
        snackListView.setHasFixedSize(true)
        sLayoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        snackListView.layoutManager = sLayoutManager
        snackListView.adapter = snackRVAdapter

        // 영양제 목록 recycler 어댑터
        tonicRVAdapter = TonicReVAdapter(tonicDataList)
        tonicListView = v!!.findViewById(R.id.tonicRecyclerView)
        tonicListView.setItemViewCacheSize(20)
        tonicListView.setHasFixedSize(true)
        tLayoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        tonicListView.layoutManager = tLayoutManager
        tonicListView.adapter = tonicRVAdapter

        // 물 목록 recycler 어댑터
        waterRVAdapter = WaterReVAdapter(waterDataList)
        waterListView = v!!.findViewById(R.id.waterRecyclerView)
        waterListView.setItemViewCacheSize(20)
        waterListView.setHasFixedSize(true)
        wLayoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        waterListView.layoutManager = wLayoutManager
        waterListView.adapter = waterRVAdapter

        totalWeightProgress = v!!.findViewById(R.id.totalWeightProgress)
        val dogWeight = FBRef.dogRef.child(myUid).child(dogId).child("dogWeight").get().addOnSuccessListener {
            totalWeightProgress.max = (it.value.toString().toFloat()).toInt() * 60
            v!!.findViewById<TextView>(R.id.fullWeight).text = ((it.value.toString().toFloat()).toInt()  * 60).toString()
        }

        nowDate = arguments?.getString("nowDate").toString() // 선택된 날짜를 받아옴

        getMealData(myUid, dogId, nowDate)

        getSnackData(myUid, dogId, nowDate)

        getTonicData(myUid, dogId, nowDate)

        getWaterData(myUid, dogId, nowDate, v)

        mealPlusBtn = v!!.findViewById(R.id.mealPlusBtn)
        mealPlusBtn.setOnClickListener {
            val intent = Intent(context, PlusMealActivity::class.java)
            intent.putExtra("date", nowDate) // 선택된 날짜를 넘겨줌
            startActivity(intent)
        }

        mealRVAdapter.setItemClickListener(object: MealReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                showDialog(v, "meal", mealDataList[position].dogMealId)
            }
        })

        snackPlusBtn = v!!.findViewById(R.id.snackPlusBtn)
        snackPlusBtn.setOnClickListener {
            val intent = Intent(context, PlusSnackActivity::class.java)
            intent.putExtra("date", nowDate) // 선택된 날짜를 넘겨줌
            startActivity(intent)
        }

        snackRVAdapter.setItemClickListener(object: SnackReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                showDialog(v, "snack", snackDataList[position].dogSnackId)
            }
        })

        tonicPlusBtn = v!!.findViewById(R.id.tonicPlusBtn)
        tonicPlusBtn.setOnClickListener {
            val intent = Intent(context, PlusTonicActivity::class.java)
            intent.putExtra("date", nowDate) // 선택된 날짜를 넘겨줌
            startActivity(intent)
        }

        tonicRVAdapter.setItemClickListener(object: TonicReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                showDialog(v, "tonic", tonicDataList[position].dogTonicId)
            }
        })

        waterPlusBtn = v!!.findViewById(R.id.waterPlusBtn)
        waterPlusBtn.setOnClickListener {
            val intent = Intent(context, PlusWaterActivity::class.java)
            intent.putExtra("date", nowDate) // 선택된 날짜를 넘겨줌
            startActivity(intent)
        }

        waterRVAdapter.setItemClickListener(object: WaterReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                showDialog(v, "water", waterDataList[position].dogWaterId)
            }
        })

        return v
    }

    private fun showDialog(v : View, category : String, id : String) { // 기록 수정/삭제를 위한 다이얼로그 띄우기
        val mDialogView = LayoutInflater.from(v.context).inflate(R.layout.eat_dialog, null)
        val mBuilder = AlertDialog.Builder(v.context).setView(mDialogView)

        val alertDialog = mBuilder.show()
        val editBtn = alertDialog.findViewById<Button>(R.id.editBtn)
        editBtn?.setOnClickListener { // 수정 버튼 클릭 시
            Log.d(TAG, "edit Button Clicked")

            when (category) {
                "meal" -> {
                    val intent = Intent(v.context, DogMealEditActivity::class.java)
                    intent.putExtra("id", id) // mealId 전송
                    intent.putExtra("date", nowDate) // 선택된 날짜 전송
                    startActivity(intent) // 수정 페이지로 이동
                }
                "snack" -> {
                    val intent = Intent(v.context, DogSnackEditActivity::class.java)
                    intent.putExtra("id", id) // snackId 전송
                    intent.putExtra("date", nowDate) // 선택된 날짜 전송
                    startActivity(intent) // 수정 페이지로 이동
                }
                "tonic" -> {
                    val intent = Intent(v.context, DogTonicEditActivity::class.java)
                    intent.putExtra("id", id) // tonicId 전송
                    intent.putExtra("date", nowDate) // 선택된 날짜 전송
                    startActivity(intent) // 수정 페이지로 이동
                }
                "water" -> {
                    val intent = Intent(v.context, DogWaterEditActivity::class.java)
                    intent.putExtra("id", id) // tonicId 전송
                    intent.putExtra("date", nowDate) // 선택된 날짜 전송
                    startActivity(intent) // 수정 페이지로 이동
                }
            }

            alertDialog.dismiss()
        }

        val rmBtn = alertDialog.findViewById<Button>(R.id.rmBtn)
        rmBtn?.setOnClickListener {  // 삭제 버튼 클릭 시
            Log.d(TAG, "remove Button Clicked")
            alertDialog.dismiss()
            // deleteImage(category)

            when (category) {
                "meal" -> {
                    FBRef.mealRef.child(myUid).child(dogId).child(id).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제
                    Firebase.storage.reference.child("dogMealImage/$myUid/$dogId/$id.png")
                        .delete().addOnSuccessListener { // 사진 삭제
                        }.addOnFailureListener {
                        }
                }
                "snack" -> {
                    FBRef.snackRef.child(myUid).child(dogId).child(id).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제
                    Firebase.storage.reference.child("dogSnackImage/$myUid/$dogId/$id.png")
                        .delete().addOnSuccessListener { // 사진 삭제
                        }.addOnFailureListener {
                        }
                }
                "tonic" -> {
                    FBRef.tonicRef.child(myUid).child(dogId).child(id).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제
                    Firebase.storage.reference.child("dogTonicImage/$myUid/$dogId/$id.png")
                        .delete().addOnSuccessListener { // 사진 삭제
                        }.addOnFailureListener {
                        }
                }
                "water" -> {
                    FBRef.waterRef.child(myUid).child(dogId).child(id).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제
                }
            }
        }
    }

    private fun getMealData(userId : String, dogId : String, nowDate : String) { // 파이어베이스로부터 사료 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 사료 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                    mealDataList.clear()

                    totalMealArea.text = ""
                    var totalMeal = 0

                    var mealMap : MutableMap<DogMealModel, Long> = mutableMapOf()
                    var mealSortedMap : MutableMap<DogMealModel, Long> = mutableMapOf()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogMealModel::class.java)
                        if(item!!.date == nowDate) { // 선택된 날짜에 맞는 사료 데이터만 추가
                            mealMap[item!!] = (item!!.time.split(":")[0] + item!!.time.split(":")[1]).toLong()
                            totalMeal += item!!.mealWeight.toInt()
                        }
                    }

                    mealSortedMap = sortMapByKey(mealMap)
                    for((key, value) in mealSortedMap.entries) {
                        mealDataList.add(key)
                    }

                    if(totalMeal > 0)
                        totalMealArea.text = totalMeal.toString() + "g"

                    mealRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, "사료 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.mealRef.child(userId).child(dogId).addValueEventListener(postListener)
    }

    private fun getSnackData(userId : String, dogId : String, nowDate : String) { // 파이어베이스로부터 간식 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 간식 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                    snackDataList.clear()

                    val breakfastList = mutableListOf<DogSnackModel>()
                    val lunchList = mutableListOf<DogSnackModel>()
                    val dinnerList = mutableListOf<DogSnackModel>()

                    totalSnackArea.text = ""

                    var snackMl = 0
                    var snackUn = 0
                    var snackG = 0

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogSnackModel::class.java)
                        if(item!!.date == nowDate) { // 선택된 날짜에 맞는 간식 데이터만 추가
                            when (item!!.timeSlot) {
                                "아침" -> breakfastList.add(item!!)
                                "점심" -> lunchList.add(item!!)
                                "저녁" -> dinnerList.add(item!!)
                            }

                            when (item!!.snackUnit) {
                                "ml" -> snackMl += item!!.snackWeight.toInt()
                                "개" -> snackUn += item!!.snackWeight.toInt()
                                "g" -> snackG += item!!.snackWeight.toInt()
                            }
                        }
                    }

                    if(snackMl != 0)
                        totalSnackArea.text = totalSnackArea.text.toString() + snackMl + "ml  "
                    if(snackUn != 0)
                        totalSnackArea.text = totalSnackArea.text.toString() + snackUn + "개  "
                    if(snackG != 0)
                        totalSnackArea.text = totalSnackArea.text.toString() + snackG + "g"

                    for(i in breakfastList)
                        snackDataList.add(i)
                    for(i in lunchList)
                        snackDataList.add(i)
                    for(i in dinnerList)
                        snackDataList.add(i)

                    snackRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, "간식 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.snackRef.child(userId).child(dogId).addValueEventListener(postListener)
    }

    private fun getTonicData(userId : String, dogId : String, nowDate : String) { // 파이어베이스로부터 영양제 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 영양제 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                    tonicDataList.clear()

                    val breakfastList = mutableListOf<DogTonicModel>()
                    val lunchList = mutableListOf<DogTonicModel>()
                    val dinnerList = mutableListOf<DogTonicModel>()

                    totalTonicArea.text = ""

                    var tonicMl = 0
                    var tonicUn = 0
                    var tonicG = 0

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogTonicModel::class.java)
                        if(item!!.date == nowDate) { // 선택된 날짜에 맞는 영양제 데이터만 추가
                            when (item.timeSlot) {
                                "아침" -> breakfastList.add(item!!)
                                "점심" -> lunchList.add(item!!)
                                "저녁" -> dinnerList.add(item!!)
                            }

                            when (item!!.tonicUnit) {
                                "ml" -> tonicMl += item!!.tonicWeight.toInt()
                                "개" -> tonicUn += item!!.tonicWeight.toInt()
                                "g" -> tonicG += item!!.tonicWeight.toInt()
                            }
                        }
                    }

                    if(tonicMl != 0)
                        totalTonicArea.text = totalTonicArea.text.toString() + tonicMl + "ml  "
                    if(tonicUn != 0)
                        totalTonicArea.text = totalTonicArea.text.toString() + tonicUn + "개  "
                    if(tonicG != 0)
                        totalTonicArea.text = totalTonicArea.text.toString() + tonicG + "g"

                    for(i in breakfastList)
                        tonicDataList.add(i)
                    for(i in lunchList)
                        tonicDataList.add(i)
                    for(i in dinnerList)
                        tonicDataList.add(i)

                    tonicRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, "영양제 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.tonicRef.child(userId).child(dogId).addValueEventListener(postListener)
    }

    private fun getWaterData(userId : String, dogId : String, nowDate : String, v: View) { // 파이어베이스로부터 물 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 물 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                    waterDataList.clear()
                    totalWeightProgress.progress = 0
                    nowWeight = 0

                    val breakfastList = mutableListOf<DogWaterModel>()
                    val lunchList = mutableListOf<DogWaterModel>()
                    val dinnerList = mutableListOf<DogWaterModel>()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogWaterModel::class.java)
                        if(item!!.date == nowDate) { // 선택된 날짜에 맞는 물 데이터만 추가
                            totalWeightProgress.incrementProgressBy(item!!.waterWeight.toInt())
                            nowWeight += item!!.waterWeight.toInt()

                            when (item.timeSlot) {
                                "아침" -> breakfastList.add(item!!)
                                "점심" -> lunchList.add(item!!)
                                "저녁" -> dinnerList.add(item!!)
                            }
                        }
                    }

                    v.findViewById<TextView>(R.id.nowWeight).text = nowWeight.toString()

                    for(i in breakfastList)
                        waterDataList.add(i)
                    for(i in lunchList)
                        waterDataList.add(i)
                    for(i in dinnerList)
                        waterDataList.add(i)

                    waterRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, "물 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.waterRef.child(userId).child(dogId).addValueEventListener(postListener)
    }

    private fun sortMapByKey(map: MutableMap<DogMealModel, Long>): LinkedHashMap<DogMealModel, Long> { // 시간순으로 정렬
        val entries = map.entries.toList()

        val sortedEntries = entries.sortedBy { it.value }

        val result = LinkedHashMap<DogMealModel, Long>()

        for (entry in sortedEntries) {
            result[entry.key] = entry.value
        }

        return result
    }
}