package org.techtown.myproject.deal

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.note.CheckUpInputReVAdapter
import org.techtown.myproject.utils.DealModel
import org.techtown.myproject.utils.FBRef
import java.io.IOException
import java.util.*


class DealFragment : Fragment() {
    private val TAG = DealFragment::class.java.simpleName

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var pullLocationName : String

    private lateinit var selectCategory : TextView

    private lateinit var adminArea : String
    private lateinit var subLocality : String
    private lateinit var thoroughfare : String

    private lateinit var myUid : String

    private lateinit var locationArea : TextView
    private var locationList : MutableMap<String, Int> = mutableMapOf()

    lateinit var writeBtn: Button

    lateinit var dealRecyclerView: RecyclerView

    private var dealMap : MutableMap<DealModel, Long> = mutableMapOf()
    private val dealList = ArrayList<DealModel>() // 거래 목록 리스트
    lateinit var dealRVAdapter : DealReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    private var checkedLocation : String = "" // 카테고리에서 선택된 지역
    private var checkedCategory : MutableList<String> = mutableListOf() // 카테고리에서 선택된 판매 용품 종류 리스트

    private lateinit var categoryRecyclerView : RecyclerView
    private lateinit var categoryRVAdapter : CategoryReVAdapter
    private val categoryList = ArrayList<String>() // 카테고리 목록 리스트
    lateinit var cLayoutManager : RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v: View? = inflater.inflate(R.layout.fragment_deal, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        sharedPreferences = v!!.context.getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        pullLocationName = sharedPreferences.getString(myUid + "Location", "").toString() // 사용자의 위치 정보를 받아옴
        Log.d("getLocation", pullLocationName)
        adminArea = pullLocationName.split(" ")[0]
        subLocality = pullLocationName.split(" ")[1]
        thoroughfare = pullLocationName.split(" ")[2]

        setData(v!!)

        selectCategory.setOnClickListener {
            showDialog(v!!)
        }

        getDealData(checkedLocation, checkedCategory)

        locationArea.setOnClickListener {
            val intent = Intent(v!!.context, SearchLocalActivity::class.java)
            v!!.context.startActivity(intent)
        }

        writeBtn = v!!.findViewById(R.id.writeBtn)
        writeBtn.setOnClickListener {
            if(adminArea != null && subLocality != null && thoroughfare != null) {
                val intent = Intent(context, WriteDealActivity::class.java)
                intent.putExtra("category", "거래")
                intent.putExtra("지역", pullLocationName)
                v!!.context.startActivity(intent)
            }
        }

        dealRVAdapter.setItemClickListener(object: DealReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val intent = Intent(v!!.context, DealInActivity::class.java)
                intent.putExtra("dealId", dealList[position].dealId)
                intent.putExtra("sellerId", dealList[position].sellerId)
                v!!.context.startActivity(intent)
            }
        })

        return v
    }

    override fun onResume() {
        super.onResume()

        pullLocationName = sharedPreferences.getString(myUid + "Location", "").toString() // 사용자의 위치 정보를 받아옴
        Log.d("getLocation", pullLocationName)
        adminArea = pullLocationName.split(" ")[0]
        subLocality = pullLocationName.split(" ")[1]
        thoroughfare = pullLocationName.split(" ")[2]

        locationArea.text = "$adminArea $subLocality $thoroughfare"
    }

    private fun setData(v : View) {

        selectCategory = v.findViewById(R.id.selectCategory)
        locationArea = v.findViewById(R.id.locationArea)

        dealRecyclerView = v.findViewById(R.id.dealRecyclerView)
        dealRVAdapter = DealReVAdapter(dealList)
        dealRecyclerView.setItemViewCacheSize(20)
        dealRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        dealRecyclerView.layoutManager = layoutManager
        dealRecyclerView.adapter = dealRVAdapter

        categoryRecyclerView = v.findViewById(R.id.categoryRecyclerView)
        categoryRVAdapter = CategoryReVAdapter(categoryList)
        categoryRecyclerView.setItemViewCacheSize(20)
        categoryRecyclerView.setHasFixedSize(true)
        cLayoutManager = LinearLayoutManager(v.context, LinearLayoutManager.HORIZONTAL, false)
        categoryRecyclerView.layoutManager = cLayoutManager
        categoryRecyclerView.adapter = categoryRVAdapter

        categoryList.clear()
        categoryList.add("전체 지역")
        categoryList.add("전체")
        categoryRVAdapter.notifyDataSetChanged()

        locationList.clear()

        locationList["모두"] = 0
        locationList[adminArea] = 0
        locationList["$adminArea $subLocality"] = 0
        locationList["$adminArea $subLocality $thoroughfare"] = 0

        locationArea.text = "$adminArea $subLocality $thoroughfare"
    }

    private fun getDealData(checkedLocation : String, checkedCategory : MutableList<String>) { // 파이어베이스로부터 거래 기록 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    dealList.clear()
                    dealMap.clear()

                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DealModel::class.java)

                        if (item!!.state != "거래 완료") { // 거래가 완료된 데이터 제외

                            if(checkedLocation == "" && checkedCategory.isEmpty()) { // 선택된 지역과 선택된 카테고리가 없을 경우 -> 처음으로 화면에 진입했을 경우

                                val date = item!!.date
                                val sp = date.split(" ")
                                val dateSp = sp[0].split(".")
                                val timeSp = sp[1].split(":")

                                var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                dealMap[item!!] = dayNum.toLong()
                            } else { // 선택된 지역과 선택된 카테고리가 존재할 경우
                                if(checkedLocation == "전체 지역") { // 전체 지역이 선택됐을 경우
                                    if(checkedCategory.contains("전체")) {
                                        val date = item!!.date
                                        val sp = date.split(" ")
                                        val dateSp = sp[0].split(".")
                                        val timeSp = sp[1].split(":")

                                        var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                        dealMap[item!!] = dayNum.toLong()
                                    } else {
                                        for(i in 0 until checkedCategory.size) {
                                            if (item!!.category == checkedCategory[i]) {
                                                val date = item!!.date
                                                val sp = date.split(" ")
                                                val dateSp = sp[0].split(".")
                                                val timeSp = sp[1].split(":")

                                                var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                dealMap[item!!] = dayNum.toLong()
                                            }
                                        }
                                    }
                                } else if(checkedLocation != "전체 지역") {
                                    if(checkedLocation.last() == '동') {
                                        if(checkedCategory.contains("전체")) {
                                            if(item!!.location.contains(checkedLocation.substring(0, 2))) {
                                                val date = item!!.date
                                                val sp = date.split(" ")
                                                val dateSp = sp[0].split(".")
                                                val timeSp = sp[1].split(":")

                                                var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                dealMap[item!!] = dayNum.toLong()
                                            }
                                        } else {
                                            if(item!!.location.contains(checkedLocation.substring(0, 2))) {
                                                for(i in 0 until checkedCategory.size) {
                                                    if (item!!.category == checkedCategory[i]) {
                                                        val date = item!!.date
                                                        val sp = date.split(" ")
                                                        val dateSp = sp[0].split(".")
                                                        val timeSp = sp[1].split(":")

                                                        var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                        dealMap[item!!] = dayNum.toLong()
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        if(checkedCategory.contains("전체")) {
                                            if(item!!.location.contains(checkedLocation)) {
                                                val date = item!!.date
                                                val sp = date.split(" ")
                                                val dateSp = sp[0].split(".")
                                                val timeSp = sp[1].split(":")

                                                var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                dealMap[item!!] = dayNum.toLong()
                                            }
                                        } else {
                                            if(item!!.location.contains(checkedLocation)) {
                                                for(i in 0 until checkedCategory.size) {
                                                    if (item!!.category == checkedCategory[i]) {
                                                        val date = item!!.date
                                                        val sp = date.split(" ")
                                                        val dateSp = sp[0].split(".")
                                                        val timeSp = sp[1].split(":")

                                                        var dayNum = dateSp[0] + dateSp[1] + dateSp[2] + timeSp[0] + timeSp[1] + timeSp[2]

                                                        dealMap[item!!] = dayNum.toLong()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val sortedDealMap = sortMapByKey(dealMap)
                    for((key, value) in dealMap.entries) {
                        dealList.add(key)
                    }

                    dealRVAdapter.notifyDataSetChanged()

                } catch (e: Exception) {
                    Log.d(TAG, "거래 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dealRef.addValueEventListener(postListener)
    }

    private fun sortMapByKey(map: MutableMap<DealModel, Long>): LinkedHashMap<DealModel, Long> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.value }

        val result = LinkedHashMap<DealModel, Long>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }

    fun showDialog(v : View) {
        val dialogView = LayoutInflater.from(v!!.context).inflate(R.layout.category_dialog, null)
        val dialog = Dialog(v!!.context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)

        val height = (resources.displayMetrics.heightPixels * 0.5).toInt()
        dialog.window?.apply {
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, height)
            setGravity(Gravity.BOTTOM)
            setBackgroundDrawable(ColorDrawable(Color.WHITE))
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            setWindowAnimations(android.R.style.Animation_InputMethod)
        }

        dialog.show()

        val location1 = dialogView?.findViewById<RadioButton>(R.id.location1)!!
        val location2 = dialogView?.findViewById<RadioButton>(R.id.location2)!!
        val location3 = dialogView?.findViewById<RadioButton>(R.id.location3)!!
        val location4 = dialogView?.findViewById<RadioButton>(R.id.location4)!!

        location2.text = adminArea
        location3.text = subLocality
        location4.text = thoroughfare

        when {
            categoryList.contains(location1.text.toString()) -> {
                location1.isChecked = true
                location1.setTextColor(Color.WHITE)
                location2.setTextColor(Color.parseColor("#c08457"))
                location3.setTextColor(Color.parseColor("#c08457"))
                location4.setTextColor(Color.parseColor("#c08457"))
            }
            categoryList.contains(location2.text.toString()) -> {
                location2.isChecked = true
                location2.setTextColor(Color.WHITE)
                location1.setTextColor(Color.parseColor("#c08457"))
                location3.setTextColor(Color.parseColor("#c08457"))
                location4.setTextColor(Color.parseColor("#c08457"))
            }
            categoryList.contains(location3.text.toString()) -> {
                location3.isChecked = true
                location3.setTextColor(Color.WHITE)
                location1.setTextColor(Color.parseColor("#c08457"))
                location2.setTextColor(Color.parseColor("#c08457"))
                location4.setTextColor(Color.parseColor("#c08457"))
            }
            categoryList.contains(location4.text.toString()) -> {
                location4.isChecked = true
                location4.setTextColor(Color.WHITE)
                location1.setTextColor(Color.parseColor("#c08457"))
                location2.setTextColor(Color.parseColor("#c08457"))
                location3.setTextColor(Color.parseColor("#c08457"))
            }
        }

        var radioGroup = dialogView?.findViewById<RadioGroup>(R.id.radioGroup)!!
        val selectedRadioButton = dialogView?.findViewById<RadioButton>(radioGroup.checkedRadioButtonId)
        checkedLocation = selectedRadioButton.text.toString()
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val checkedRadioButton = dialogView?.findViewById<RadioButton>(checkedId)
            checkedRadioButton.setTextColor(Color.WHITE)
            checkedLocation = checkedRadioButton.text.toString()

            for (i in 0 until group.childCount) {
                val radioButton = group.getChildAt(i) as RadioButton
                if (radioButton.id != checkedId) {
                    radioButton.setTextColor(Color.parseColor("#c08457"))
                }
            }
        }

        Log.d("categoryList", checkedCategory.toString())

        val categoryBox1 = dialogView?.findViewById<CheckBox>(R.id.categoryBox1)
        if(categoryList.contains(categoryBox1.text.toString())) {
            categoryBox1.isChecked = true
        }
        val categoryBox2 = dialogView?.findViewById<CheckBox>(R.id.categoryBox2)
        if(categoryList.contains(categoryBox2.text.toString())) {
            categoryBox2.isChecked = true
        }
        val categoryBox3 = dialogView?.findViewById<CheckBox>(R.id.categoryBox3)
        if(categoryList.contains(categoryBox3.text.toString())) {
            categoryBox3.isChecked = true
        }
        val categoryBox4 = dialogView?.findViewById<CheckBox>(R.id.categoryBox4)
        if(categoryList.contains(categoryBox4.text.toString())) {
            categoryBox4.isChecked = true
        }
        val categoryBox5 = dialogView?.findViewById<CheckBox>(R.id.categoryBox5)
        if(categoryList.contains(categoryBox5.text.toString())) {
            categoryBox5.isChecked = true
        }
        val categoryBox6 = dialogView?.findViewById<CheckBox>(R.id.categoryBox6)
        if(categoryList.contains(categoryBox6.text.toString())) {
            categoryBox6.isChecked = true
        }

        checkedCategory.clear()
        if(categoryList.contains(categoryBox1.text.toString())) {
            categoryBox1.isChecked = true
            checkedCategory.add(categoryBox1.text.toString())
        }
        categoryBox1?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!checkedCategory.contains(categoryBox1.text.toString())) {
                    checkedCategory.add(categoryBox1.text.toString())
                }
            } else {
                checkedCategory.remove(categoryBox1.text.toString())
            }
        }

        if(categoryList.contains(categoryBox2.text.toString())) {
            categoryBox2.isChecked = true
            checkedCategory.add(categoryBox2.text.toString())
        }
        categoryBox2?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!checkedCategory.contains(categoryBox2.text.toString())) {
                    checkedCategory.add(categoryBox2.text.toString())
                }
            } else {
                checkedCategory.remove(categoryBox2.text.toString())
            }
        }

        if(categoryList.contains(categoryBox3.text.toString())) {
            categoryBox3.isChecked = true
            checkedCategory.add(categoryBox3.text.toString())
        }
        categoryBox3?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!checkedCategory.contains(categoryBox3.text.toString())) {
                    checkedCategory.add(categoryBox3.text.toString())
                }
            } else {
                checkedCategory.remove(categoryBox3.text.toString())
            }
        }

        if(categoryList.contains(categoryBox4.text.toString())) {
            categoryBox4.isChecked = true
            checkedCategory.add(categoryBox4.text.toString())
        }
        categoryBox4?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!checkedCategory.contains(categoryBox4.text.toString())) {
                    checkedCategory.add(categoryBox4.text.toString())
                }
            } else {
                checkedCategory.remove(categoryBox4.text.toString())
            }
        }

        if(categoryList.contains(categoryBox5.text.toString())) {
            categoryBox5.isChecked = true
            checkedCategory.add(categoryBox5.text.toString())
        }
        categoryBox5?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!checkedCategory.contains(categoryBox5.text.toString())) {
                    checkedCategory.add(categoryBox5.text.toString())
                }
            } else {
                checkedCategory.remove(categoryBox5.text.toString())
            }
        }

        if(categoryList.contains(categoryBox6.text.toString())) {
            categoryBox6.isChecked = true
            checkedCategory.add(categoryBox6.text.toString())
        }
        categoryBox6?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!checkedCategory.contains(categoryBox6.text.toString())) {
                    checkedCategory.add(categoryBox6.text.toString())
                }
            } else {
                checkedCategory.remove(categoryBox6.text.toString())
            }
        }

        val checkBtn = dialogView?.findViewById<ImageView>(R.id.checkBtn)
        checkBtn.setOnClickListener {
            if(checkedCategory.size == 0) {
                Toast.makeText(v!!.context, "카테고리를 하나 이상 선택하세요!", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("checkedCategory", "$checkedLocation $checkedCategory")

                categoryList.clear()

                categoryList.add(checkedLocation)
                for(i in 0 until checkedCategory.size)
                    categoryList.add(checkedCategory[i])

                categoryRVAdapter.notifyDataSetChanged()

                getDealData(checkedLocation, checkedCategory)

                dialog.dismiss()
            }
        }

        val backBtn = dialogView?.findViewById<ImageView>(R.id.backBtn)
        backBtn!!.setOnClickListener {
           dialog.dismiss()
        }
    }
}