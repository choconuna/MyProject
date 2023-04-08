package org.techtown.myproject.statistics

import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogMealModel
import org.techtown.myproject.utils.DogSnackModel
import org.techtown.myproject.utils.DogWaterModel
import org.techtown.myproject.utils.FBRef
import java.text.SimpleDateFormat
import java.util.*

class SnackStatisticsFragment : Fragment() {

    private val TAG = SnackStatisticsFragment::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    private lateinit var myUid : String
    private lateinit var dogId : String
    private lateinit var nowDate : String
    private lateinit var weekDate : String
    private lateinit var twoWeekDate : String
    private lateinit var threeWeekDate : String
    private lateinit var oneMonthDate : String
    private lateinit var twoMonthDate : String
    private lateinit var threeMonthDate : String
    private lateinit var fourMonthDate : String
    private lateinit var fiveMonthDate : String
    private lateinit var sixMonthDate : String
    private lateinit var sevenMonthDate : String
    private lateinit var eightMonthDate : String
    private lateinit var nineMonthDate : String
    private lateinit var tenMonthDate : String
    private lateinit var elevenMonthDate :String
    private lateinit var yearDate : String

    lateinit var spinner : Spinner
    lateinit var selectedDate : String

    private lateinit var oneDayPieChart : PieChart
    private lateinit var weekPieChart : PieChart
    private lateinit var oneMonthPieChart : PieChart
    private lateinit var threeMonthPieChart : PieChart
    private lateinit var sixMonthPieChart : PieChart
    private lateinit var yearPieChart : PieChart

    private var oneDayPieMap : MutableMap<String, Float> = mutableMapOf()
    private var weekPieMap : MutableMap<String, Float> = mutableMapOf()
    private var oneMonthPieMap : MutableMap<String, Float> = mutableMapOf()
    private var threeMonthPieMap : MutableMap<String, Float> = mutableMapOf()
    private var sixMonthPieMap : MutableMap<String, Float> = mutableMapOf()
    private var yearPieMap : MutableMap<String, Float> = mutableMapOf()

    private lateinit var oneDayRecyclerView: RecyclerView
    private lateinit var weekRecyclerView : RecyclerView
    private lateinit var oneMonthRecyclerView : RecyclerView
    private lateinit var threeMonthRecyclerView : RecyclerView
    private lateinit var sixMonthRecyclerView : RecyclerView
    private lateinit var yearRecyclerView : RecyclerView

    private var oneDayMap : MutableMap<String, Int> = mutableMapOf()
    private var labelList : ArrayList<String> = ArrayList()
    private val oneDayDataList = ArrayList<String>() // 오늘 영양제 목록 리스트
    lateinit var snackStatisticsRVAdapter : SnackStatisticsReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    private var weekLabelList : ArrayList<String> = ArrayList()
    private var weekMap : MutableMap<String, Int> = mutableMapOf()
    private val weekDataList = ArrayList<String>() // 1주일 영양제 목록 리스트
    lateinit var weekSnackStatisticsRVAdapter : SnackStatisticsReVAdapter
    lateinit var wLayoutManager : RecyclerView.LayoutManager

    private var oneMonthLabelList : ArrayList<String> = ArrayList()
    private var oneMonthMap : MutableMap<String, Int> = mutableMapOf()
    private val oneMonthDataList = ArrayList<String>() // 1개월 영양제 목록 리스트
    lateinit var oneMonthSnackStatisticsRVAdapter : SnackStatisticsReVAdapter
    lateinit var oLayoutManager : RecyclerView.LayoutManager

    private var threeMonthLabelList : ArrayList<String> = ArrayList()
    private var threeMonthMap : MutableMap<String, Int> = mutableMapOf()
    private val threeMonthDataList = ArrayList<String>() // 3개월 영양제 목록 리스트
    lateinit var threeMonthSnackStatisticsRVAdapter : SnackStatisticsReVAdapter
    lateinit var tLayoutManager : RecyclerView.LayoutManager

    private var sixMonthLabelList : ArrayList<String> = ArrayList()
    private var sixMonthMap : MutableMap<String, Int> = mutableMapOf()
    private val sixMonthDataList = ArrayList<String>() // 6개월 영양제 목록 리스트
    lateinit var sixMonthSnackStatisticsRVAdapter : SnackStatisticsReVAdapter
    lateinit var sLayoutManager : RecyclerView.LayoutManager

    private var yearLabelList : ArrayList<String> = ArrayList()
    private var yearMap : MutableMap<String, Int> = mutableMapOf()
    private val yearDataList = ArrayList<String>() // 1년 영양제 목록 리스트
    lateinit var yearSnackStatisticsRVAdapter : SnackStatisticsReVAdapter
    lateinit var yLayoutManager : RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v : View? = inflater.inflate(R.layout.fragment_snack_statistics, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = v!!.context.getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(myUid, "").toString() // 현재 대표 반려견의 id

        getDate()
        setData(v)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                selectedDate = parent.getItemAtPosition(position).toString()
                setShowChart(v, selectedDate)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        return v
    }

    private fun setData(v : View) {
        oneDayPieChart = v.findViewById(R.id.pieChart_one_day)
        weekPieChart = v.findViewById(R.id.pieChart_week)
        oneMonthPieChart = v.findViewById(R.id.pieChart_one_month)
        threeMonthPieChart = v.findViewById(R.id.pieChart_three_month)
        sixMonthPieChart = v.findViewById(R.id.pieChart_six_month)
        yearPieChart = v.findViewById(R.id.pieChart_year)

        oneDayRecyclerView = v.findViewById(R.id.oneDayRecyclerView)
        weekRecyclerView = v.findViewById(R.id.weekRecyclerView)
        oneMonthRecyclerView = v.findViewById(R.id.oneMonthRecyclerView)
        threeMonthRecyclerView = v.findViewById(R.id.threeMonthRecyclerView)
        sixMonthRecyclerView = v.findViewById(R.id.sixMonthRecyclerView)
        yearRecyclerView = v.findViewById(R.id.yearRecyclerView)

        snackStatisticsRVAdapter = SnackStatisticsReVAdapter(oneDayDataList)
        oneDayRecyclerView = v!!.findViewById(R.id.oneDayRecyclerView)
        oneDayRecyclerView.setItemViewCacheSize(20)
        oneDayRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        oneDayRecyclerView.layoutManager = layoutManager
        oneDayRecyclerView.adapter = snackStatisticsRVAdapter

        weekSnackStatisticsRVAdapter = SnackStatisticsReVAdapter(weekDataList)
        weekRecyclerView = v!!.findViewById(R.id.weekRecyclerView)
        weekRecyclerView.setItemViewCacheSize(20)
        weekRecyclerView.setHasFixedSize(true)
        wLayoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        weekRecyclerView.layoutManager = wLayoutManager
        weekRecyclerView.adapter = weekSnackStatisticsRVAdapter

        oneMonthSnackStatisticsRVAdapter = SnackStatisticsReVAdapter(oneMonthDataList)
        oneMonthRecyclerView = v!!.findViewById(R.id.oneMonthRecyclerView)
        oneMonthRecyclerView.setItemViewCacheSize(20)
        oneMonthRecyclerView.setHasFixedSize(true)
        oLayoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        oneMonthRecyclerView.layoutManager = oLayoutManager
        oneMonthRecyclerView.adapter = oneMonthSnackStatisticsRVAdapter

        threeMonthSnackStatisticsRVAdapter = SnackStatisticsReVAdapter(threeMonthDataList)
        threeMonthRecyclerView = v!!.findViewById(R.id.threeMonthRecyclerView)
        threeMonthRecyclerView.setItemViewCacheSize(20)
        threeMonthRecyclerView.setHasFixedSize(true)
        tLayoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        threeMonthRecyclerView.layoutManager = tLayoutManager
        threeMonthRecyclerView.adapter = threeMonthSnackStatisticsRVAdapter

        sixMonthSnackStatisticsRVAdapter = SnackStatisticsReVAdapter(sixMonthDataList)
        sixMonthRecyclerView = v!!.findViewById(R.id.sixMonthRecyclerView)
        sixMonthRecyclerView.setItemViewCacheSize(20)
        sixMonthRecyclerView.setHasFixedSize(true)
        sLayoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        sixMonthRecyclerView.layoutManager = sLayoutManager
        sixMonthRecyclerView.adapter = sixMonthSnackStatisticsRVAdapter

        yearSnackStatisticsRVAdapter = SnackStatisticsReVAdapter(yearDataList)
        yearRecyclerView = v!!.findViewById(R.id.yearRecyclerView)
        yearRecyclerView.setItemViewCacheSize(20)
        yearRecyclerView.setHasFixedSize(true)
        yLayoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        yearRecyclerView.layoutManager = yLayoutManager
        yearRecyclerView.adapter = yearSnackStatisticsRVAdapter

        spinner = v.findViewById(R.id.spinner)

        setTodayPieChartReady()
        setTodayPieChart()
        pieTodayChartGraph(v, oneDayPieChart, oneDayPieMap)
        setShowChart(v, "오늘")

        for((key, value) in oneDayMap.entries) {
            labelList.add(key)
        }

        for(i in 0 until labelList.size)
            oneDayDataList.add(labelList[i])

        snackStatisticsRVAdapter.notifyDataSetChanged()
    }

    private fun getDate() { // 오늘, 일주일 전, 1개월 전, 3개월 전, 6개월 전, 1년 전 날짜 구하기
        val cal: Calendar = Calendar.getInstance()
        val format = "yyyy.MM.dd"
        val sdf = SimpleDateFormat(format)
        nowDate = sdf.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, -6)
        weekDate = sdf.format(cal.time) // 1주 전

        val cal2 : Calendar = Calendar.getInstance()
        cal2.add(Calendar.DAY_OF_YEAR, -13)
        twoWeekDate = sdf.format(cal2.time) // 2주 전

        val cal3 : Calendar = Calendar.getInstance()
        cal3.add(Calendar.DAY_OF_YEAR, -20)
        threeWeekDate = sdf.format(cal3.time) // 3주 전

        val cal4: Calendar = Calendar.getInstance()
        cal4.add(Calendar.MONTH, -1)
        oneMonthDate = sdf.format(cal4.time) // 1개월 전

        val cal5: Calendar = Calendar.getInstance()
        cal5.add(Calendar.MONTH, -2)
        twoMonthDate = sdf.format(cal5.time) // 2개월 전

        val cal6: Calendar = Calendar.getInstance()
        cal6.add(Calendar.MONTH, -3)
        threeMonthDate = sdf.format(cal6.time) // 3개월 전

        val cal7: Calendar = Calendar.getInstance()
        cal7.add(Calendar.MONTH, -4)
        fourMonthDate = sdf.format(cal7.time) // 4개월 전

        val cal8: Calendar = Calendar.getInstance()
        cal8.add(Calendar.MONTH, -5)
        fiveMonthDate = sdf.format(cal8.time) // 5개월 전

        val cal9: Calendar = Calendar.getInstance()
        cal9.add(Calendar.MONTH, -6)
        sixMonthDate = sdf.format(cal9.time) // 6개월 전

        val cal10: Calendar = Calendar.getInstance()
        cal10.add(Calendar.MONTH, -7)
        sevenMonthDate = sdf.format(cal10.time) // 7개월 전

        val cal11: Calendar = Calendar.getInstance()
        cal11.add(Calendar.MONTH, -8)
        eightMonthDate = sdf.format(cal11.time) // 8개월 전

        val cal12: Calendar = Calendar.getInstance()
        cal12.add(Calendar.MONTH, -9)
        nineMonthDate = sdf.format(cal12.time) // 9개월 전

        val cal13: Calendar = Calendar.getInstance()
        cal13.add(Calendar.MONTH, -10)
        tenMonthDate = sdf.format(cal13.time) // 10개월 전

        val cal14: Calendar = Calendar.getInstance()
        cal14.add(Calendar.MONTH, -11)
        elevenMonthDate = sdf.format(cal14.time) // 11개월 전

        val cal15: Calendar = Calendar.getInstance()
        cal15.add(Calendar.YEAR, -1)
        yearDate = sdf.format(cal15.time) // 1년 전

        Log.d("showDate", "$oneMonthDate $twoMonthDate $threeMonthDate $fourMonthDate $fiveMonthDate $sixMonthDate $sevenMonthDate $eightMonthDate $nineMonthDate $tenMonthDate $elevenMonthDate $yearDate")
    }

    private fun setShowChart(v : View, selectedDate : String) {
        when(selectedDate) {
            "오늘" -> {
                oneDayPieChart.visibility = View.VISIBLE
                oneDayRecyclerView.visibility = View.VISIBLE
                weekPieChart.visibility = View.GONE
                weekRecyclerView.visibility = View.GONE
                oneMonthPieChart.visibility = View.GONE
                oneMonthRecyclerView.visibility = View.GONE
                threeMonthPieChart.visibility = View.GONE
                threeMonthRecyclerView.visibility = View.GONE
                sixMonthPieChart.visibility = View.GONE
                sixMonthRecyclerView.visibility = View.GONE
                yearPieChart.visibility = View.GONE
                yearRecyclerView.visibility = View.GONE

                Log.d("oneDayPieMap", "$oneDayPieMap")
                setTodayPieChartReady()
                setTodayPieChart()
                pieTodayChartGraph(v, oneDayPieChart, oneDayPieMap)
                setTodaySnack()
                Log.d("snackList", labelList.toString())
            }
            "1주일" -> {
                oneDayPieChart.visibility = View.GONE
                oneDayRecyclerView.visibility = View.GONE
                weekPieChart.visibility = View.VISIBLE
                weekRecyclerView.visibility = View.VISIBLE
                oneMonthPieChart.visibility = View.GONE
                oneMonthRecyclerView.visibility = View.GONE
                threeMonthPieChart.visibility = View.GONE
                threeMonthRecyclerView.visibility = View.GONE
                sixMonthPieChart.visibility = View.GONE
                sixMonthRecyclerView.visibility = View.GONE
                yearPieChart.visibility = View.GONE
                yearRecyclerView.visibility = View.GONE

                setWeekPieChartReady()
                setWeekPieChart()
                pieWeekChartGraph(v, weekPieChart, weekPieMap)
                setWeekSnack()
                Log.d("snackList", weekLabelList.toString())
            }
            "1개월" -> {
                oneDayPieChart.visibility = View.GONE
                oneDayRecyclerView.visibility = View.GONE
                weekPieChart.visibility = View.GONE
                weekRecyclerView.visibility = View.GONE
                oneMonthPieChart.visibility = View.VISIBLE
                oneMonthRecyclerView.visibility = View.VISIBLE
                threeMonthPieChart.visibility = View.GONE
                threeMonthRecyclerView.visibility = View.GONE
                sixMonthPieChart.visibility = View.GONE
                sixMonthRecyclerView.visibility = View.GONE
                yearPieChart.visibility = View.GONE
                yearRecyclerView.visibility = View.GONE

                setOneMonthPieChartReady()
                setOneMonthPieChart()
                pieOneMonthPieChartGraph(v, oneMonthPieChart, oneMonthPieMap)
                setOneMonthSnack()
                Log.d("snackList", oneMonthLabelList.toString())
            }
            "3개월" -> {
                oneDayPieChart.visibility = View.GONE
                oneDayRecyclerView.visibility = View.GONE
                weekPieChart.visibility = View.GONE
                weekRecyclerView.visibility = View.GONE
                oneMonthPieChart.visibility = View.GONE
                oneMonthRecyclerView.visibility = View.GONE
                threeMonthPieChart.visibility = View.VISIBLE
                threeMonthRecyclerView.visibility = View.VISIBLE
                sixMonthPieChart.visibility = View.GONE
                sixMonthRecyclerView.visibility = View.GONE
                yearPieChart.visibility = View.GONE
                yearRecyclerView.visibility = View.GONE

                setThreeMonthBarChartReady()
                setThreeMonthBarChart()
                pieThreeMonthBarChartGraph(v, threeMonthPieChart, threeMonthPieMap)
                setThreeMonthSnack()
                Log.d("snackList", threeMonthLabelList.toString())
            }
            "6개월" -> {
                oneDayPieChart.visibility = View.GONE
                oneDayRecyclerView.visibility = View.GONE
                weekPieChart.visibility = View.GONE
                weekRecyclerView.visibility = View.GONE
                oneMonthPieChart.visibility = View.GONE
                oneMonthRecyclerView.visibility = View.GONE
                threeMonthPieChart.visibility = View.GONE
                threeMonthRecyclerView.visibility = View.GONE
                sixMonthPieChart.visibility = View.VISIBLE
                sixMonthRecyclerView.visibility = View.VISIBLE
                yearPieChart.visibility = View.GONE
                yearRecyclerView.visibility = View.GONE

                setSixPieMonthChartReady()
                setSixPieMonthChart()
                pieSixMonthChartGraph(v, sixMonthPieChart, sixMonthPieMap)
                setSixMonthSnack()
                Log.d("snackList", sixMonthLabelList.toString())
            }
            "1년" -> {
                oneDayPieChart.visibility = View.GONE
                oneDayRecyclerView.visibility = View.GONE
                weekPieChart.visibility = View.GONE
                weekRecyclerView.visibility = View.GONE
                oneMonthPieChart.visibility = View.GONE
                oneMonthRecyclerView.visibility = View.GONE
                threeMonthPieChart.visibility = View.GONE
                threeMonthRecyclerView.visibility = View.GONE
                sixMonthPieChart.visibility = View.GONE
                sixMonthRecyclerView.visibility = View.GONE
                yearPieChart.visibility = View.VISIBLE
                yearRecyclerView.visibility = View.VISIBLE

                setYearPieChartReady()
                setYearPieChart()
                pieYearChartGraph(v, yearPieChart, yearPieMap)
                yearSnack()
                Log.d("snackList", yearLabelList.toString())
            }
        }
    }

    private fun setTodayPieChartReady() {
        val nowSp = nowDate.split(".") // 오늘 날짜
        val nowYear = nowSp[0].toInt()
        val nowMonth = nowSp[1].toInt()
        val nowDay = nowSp[2].toInt()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 간식 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    oneDayPieMap.clear()
                    oneDayMap.clear()
                    oneDayDataList.clear()
                    labelList.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogSnackModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if(year == nowYear && month == nowMonth && day == nowDay) {
                            oneDayPieMap[item!!.snackType] = 0.toFloat()
                            oneDayMap[item!!.snackName] = 0
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "간식 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.snackRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun setTodayPieChart() {
        val nowSp = nowDate.split(".") // 오늘 날짜
        val nowYear = nowSp[0].toInt()
        val nowMonth = nowSp[1].toInt()
        val nowDay = nowSp[2].toInt()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 간식 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogSnackModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if(year == nowYear && month == nowMonth && day == nowDay) {
                            oneDayPieMap[item!!.snackType] = oneDayPieMap[item!!.snackType]!! + 1.toFloat()
                            // oneDayBarMap[item!!.timeSlot] = oneDayBarMap[item!!.timeSlot]!! + item!!.mealWeight.toFloat()
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "간식 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.snackRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun pieTodayChartGraph(v : View, pieChart: PieChart, valList : MutableMap<String, Float>) {

        pieChart.extraBottomOffset = 15f // 간격
        pieChart.description.isEnabled = false // chart 밑에 description 표시 유무
        pieChart.setTouchEnabled(false)

        pieChart.run {
            description.isEnabled = false

            setTouchEnabled(false)
            legend.isEnabled = true

            setEntryLabelTextSize(10f)
            setEntryLabelColor(Color.parseColor("#000000"))
        }

        var sum = 0.toFloat()
        for((key, value) in valList.entries) {
            if(value >= 1) {
                sum += value
            }
        }

        var entries : ArrayList<PieEntry> = ArrayList()
        for((key, value) in valList.entries) {
            entries.add(PieEntry(value / sum * 100, key))
        }

        val colors: ArrayList<Int> = ArrayList()
        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)

        var depenses = PieDataSet(entries, "")
        with(depenses) {
            sliceSpace = 3f
            selectionShift = 5f
            valueTextSize = 5f
            setColors(colors)
        }
        depenses.valueFormatter = CustomFormatter()
        // depenses.color = Color.parseColor("#F9AC3A")

        val data = PieData(depenses)
        with(data) {
            setValueTextSize(10f)
            setValueTextColor(Color.parseColor("#000000"))
        }

        pieChart.run {
            this.data = data
            description.isEnabled = false
            animate()
            invalidate()
            centerText = "간식"
        }
    }

    private fun setTodaySnack() {
        labelList.clear()
        oneDayDataList.clear()

        for((key, value) in oneDayMap.entries) {
            labelList.add(key)
        }

        for(i in 0 until labelList.size)
            oneDayDataList.add(labelList[i])

        snackStatisticsRVAdapter.notifyDataSetChanged()
    }

    private fun setWeekPieChartReady() {
        val nowSp = nowDate.split(".") // 오늘 날짜
        val nowYear = nowSp[0].toInt()
        val nowMonth = nowSp[1].toInt()
        val nowDay = nowSp[2].toInt()
        val nowDateNum = nowSp[0] + nowSp[1] + nowSp[2]

        val weekSp = weekDate.split(".") // 일주일 전 날짜
        val weekYear = weekSp[0].toInt()
        val weekMonth = weekSp[1].toInt()
        val weekDay = weekSp[2].toInt()
        val weekDateNum = weekSp[0] + weekSp[1] + weekSp[2]

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 간식 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    weekPieMap.clear()
                    weekLabelList.clear()
                    weekMap.clear()
                    weekDataList.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogSnackModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

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

                        if(year == nowYear && year == weekYear) { // 일주일 전이 같은 연도일 경우
                            if(month == nowMonth && month == weekMonth) { // 일주일 전이 같은 달일 경우
                                if(day in weekDay..nowDay) {
                                    weekPieMap[item!!.snackType] = 0.toFloat()
                                    weekMap[item!!.snackName] = 0
                                }
                            } else if(month < nowMonth && month == weekMonth) { // 일주일 전이 전달일 경우
                                if(day >= weekDay) {
                                    weekPieMap[item!!.snackType] = 0.toFloat()
                                    weekMap[item!!.snackName] = 0
                                }
                            } else if(month == nowMonth && month > weekMonth) { // 일주일 전이 이번달일 경우
                                if(day <= nowDay) {
                                    weekPieMap[item!!.snackType] = 0.toFloat()
                                    weekMap[item!!.snackName] = 0
                                }
                            }
                        } else if(year < nowYear && year == weekYear) { // 일주일 전이 전년도일 경우
                            if(weekMonth == month && day >= weekDay) {
                                weekPieMap[item!!.snackType] = 0.toFloat()
                                weekMap[item!!.snackName] = 0
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "간식 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.snackRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun setWeekPieChart() {
        val nowSp = nowDate.split(".") // 오늘 날짜
        val nowYear = nowSp[0].toInt()
        val nowMonth = nowSp[1].toInt()
        val nowDay = nowSp[2].toInt()
        val nowDateNum = nowSp[0] + nowSp[1] + nowSp[2]

        val weekSp = weekDate.split(".") // 일주일 전 날짜
        val weekYear = weekSp[0].toInt()
        val weekMonth = weekSp[1].toInt()
        val weekDay = weekSp[2].toInt()
        val weekDateNum = weekSp[0] + weekSp[1] + weekSp[2]

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 간식 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogSnackModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

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

                        if(year == nowYear && year == weekYear) { // 일주일 전이 같은 연도일 경우
                            if(month == nowMonth && month == weekMonth) { // 일주일 전이 같은 달일 경우
                                if(day in weekDay..nowDay) {
                                    weekPieMap[item!!.snackType] = weekPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if(month < nowMonth && month == weekMonth) { // 일주일 전이 전달일 경우
                                if(day >= weekDay) {
                                    weekPieMap[item!!.snackType] = weekPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if(month == nowMonth && month > weekMonth) { // 일주일 전이 이번달일 경우
                                if(day <= nowDay) {
                                    weekPieMap[item!!.snackType] = weekPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if(year < nowYear && year == weekYear) { // 일주일 전이 전년도일 경우
                            if(weekMonth == month && day >= weekDay) {
                                weekPieMap[item!!.snackType] = weekPieMap[item!!.snackType]!! + 1.toFloat()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "간식 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.snackRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun pieWeekChartGraph(v : View, pieChart: PieChart, valList : MutableMap<String, Float>) {

        pieChart.extraBottomOffset = 15f // 간격
        pieChart.description.isEnabled = false // chart 밑에 description 표시 유무
        pieChart.setTouchEnabled(false)

        pieChart.run {
            description.isEnabled = false

            setTouchEnabled(false)
            legend.isEnabled = true

            setEntryLabelTextSize(10f)
            setEntryLabelColor(Color.parseColor("#000000"))
        }

        var sum = 0.toFloat()
        for((key, value) in valList.entries) {
            if(value >= 1) {
                sum += value
            }
        }

        var entries : ArrayList<PieEntry> = ArrayList()
        for((key, value) in valList.entries) {
            if(value >= 1) {
                entries.add(PieEntry(value / sum * 100, key))
            }
        }

        val colors: ArrayList<Int> = ArrayList()
        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)

        var depenses = PieDataSet(entries, "")
        with(depenses) {
            sliceSpace = 3f
            selectionShift = 5f
            valueTextSize = 5f
            setColors(colors)
        }
        depenses.valueFormatter = CustomFormatter()
        // depenses.color = Color.parseColor("#F9AC3A")

        val data = PieData(depenses)
        with(data) {
            setValueTextSize(10f)
            setValueTextColor(Color.parseColor("#000000"))
        }

        pieChart.run {
            this.data = data
            description.isEnabled = false
            animate()
            invalidate()
            centerText = "간식"
        }
    }

    private fun setWeekSnack() {
        weekLabelList.clear()
        weekDataList.clear()

              for((key, value) in weekMap.entries) {
            weekLabelList.add(key)
        }

        for(i in 0 until weekLabelList.size)
            weekDataList.add(weekLabelList[i])

        weekSnackStatisticsRVAdapter.notifyDataSetChanged()
    }

    private fun setOneMonthPieChartReady() {
        val nowSp = nowDate.split(".") // 오늘 날짜
        val nowYear = nowSp[0].toInt()
        val nowMonth = nowSp[1].toInt()
        val nowDay = nowSp[2].toInt()
        val nowDateNum = nowSp[0] + nowSp[1] + nowSp[2]

        val weekSp = weekDate.split(".") // 1주 전 날짜
        val weekYear = weekSp[0].toInt()
        val weekMonth = weekSp[1].toInt()
        val weekDay = weekSp[2].toInt()
        val weekDateNum = weekSp[0] + weekSp[1] + weekSp[2]

        val twoWeekSp = twoWeekDate.split(".") // 2주 전 날짜
        val twoWeekYear = twoWeekSp[0].toInt()
        val twoWeekMonth = twoWeekSp[1].toInt()
        val twoWeekDay = twoWeekSp[2].toInt()

        val threeWeekSp = threeWeekDate.split(".") // 3주 전 날짜
        val threeWeekYear = threeWeekSp[0].toInt()
        val threeWeekMonth = threeWeekSp[1].toInt()
        val threeWeekDay = threeWeekSp[2].toInt()

        val oneMonthWeekSp = oneMonthDate.split(".") // 1개월 전 날짜
        val oneMonthYear = oneMonthWeekSp[0].toInt()
        val oneMonthMonth = oneMonthWeekSp[1].toInt()
        val oneMonthDay = oneMonthWeekSp[2].toInt()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 간식 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    oneMonthPieMap.clear()
                    oneMonthMap.clear()
                    oneMonthDataList.clear()
                    oneMonthLabelList.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogSnackModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if(year == nowYear && year == weekYear) { // 일주일 전이 같은 연도일 경우
                            if(month == nowMonth && month == weekMonth) { // 일주일 전이 같은 달일 경우
                                if(day in weekDay..nowDay) {
                                    oneMonthPieMap[item!!.snackType] = 0.toFloat()
                                    oneMonthMap[item!!.snackName] = 0
                                }
                            } else if(month < nowMonth && month == weekMonth) { // 일주일 전이 이전 달일 경우
                                if(day >= weekDay) {
                                    oneMonthPieMap[item!!.snackType] = 0.toFloat()
                                    oneMonthMap[item!!.snackName] = 0
                                }
                            } else if(month == nowMonth && month > weekMonth) { // 일주일 전이 이번 달일 경우
                                if(day <= nowDay) {
                                    oneMonthPieMap[item!!.snackType] = 0.toFloat()
                                    oneMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if(year < nowYear && year == weekYear) { // 일주일 전이 전년도일 경우
                            if(weekMonth == month && day >= weekDay) {
                                oneMonthPieMap[item!!.snackType] = 0.toFloat()
                                oneMonthMap[item!!.snackName] = 0
                            }
                        }

                        if(year == weekYear && year == twoWeekYear) { // 2주일 전이 같은 연도일 경우
                            if(month == weekMonth && month == twoWeekMonth) { // 2주일 전이 같은 달일 경우
                                if(day in twoWeekDay until weekDay) {
                                    oneMonthPieMap[item!!.snackType] = 0.toFloat()
                                    oneMonthMap[item!!.snackName] = 0
                                }
                            } else if(month < weekMonth && month == twoWeekMonth) { // 2주일 전이 이전 달일 경우
                                if(day >= twoWeekDay) {
                                    oneMonthPieMap[item!!.snackType] = 0.toFloat()
                                    oneMonthMap[item!!.snackName] = 0
                                }
                            } else if(month == weekMonth && month > twoWeekMonth) { // 2주일 전이 이번 달일 경우
                                if(day < weekDay) {
                                    oneMonthPieMap[item!!.snackType] = 0.toFloat()
                                    oneMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if(year < weekYear && year == twoWeekYear) { // 2주일 전이 전년도일 경우
                            if(twoWeekMonth == month && day >= twoWeekDay) {
                                oneMonthPieMap[item!!.snackType] = 0.toFloat()
                                oneMonthMap[item!!.snackName] = 0
                            }
                        }

                        if(year == twoWeekYear && year == threeWeekYear) { // 3주일 전이 같은 연도일 경우
                            if(month == twoWeekMonth && month == threeWeekMonth) { // 3주일 전이 같은 달일 경우
                                if(day in threeWeekDay until twoWeekDay) {
                                    oneMonthPieMap[item!!.snackType] = 0.toFloat()
                                    oneMonthMap[item!!.snackName] = 0
                                }
                            } else if(month < twoWeekMonth && month == threeWeekMonth) { // 3주일 전이 이전 달일 경우
                                if(day >= threeWeekDay) {
                                    oneMonthPieMap[item!!.snackType] = 0.toFloat()
                                    oneMonthMap[item!!.snackName] = 0
                                }
                            } else if(month == twoWeekMonth && month > threeWeekMonth) { // 3주일 전이 이번 달일 경우
                                if(day < twoWeekDay) {
                                    oneMonthPieMap[item!!.snackType] = 0.toFloat()
                                    oneMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if(year < twoWeekYear && year == threeWeekYear) { // 3주일 전이 전년도일 경우
                            if(threeWeekMonth == month && day >= threeWeekDay) {
                                oneMonthPieMap[item!!.snackType] = 0.toFloat()
                                oneMonthMap[item!!.snackName] = 0
                            }
                        }

                        if(year == threeWeekYear && year == oneMonthYear) { // 한달 전이 같은 연도일 경우
                            if(month == threeWeekMonth && month == oneMonthMonth) { // 한달 전이 같은 달일 경우
                                if(day in oneMonthDay until threeWeekDay) {
                                    oneMonthPieMap[item!!.snackType] = 0.toFloat()
                                    oneMonthMap[item!!.snackName] = 0
                                }
                            } else if(month < threeWeekMonth && month == oneMonthMonth) { // 한달 전이 이전 달일 경우
                                if(day >= oneMonthDay) {
                                    oneMonthPieMap[item!!.snackType] = 0.toFloat()
                                    oneMonthMap[item!!.snackName] = 0
                                }
                            } else if(month == threeWeekMonth && month > oneMonthMonth) { // 한달 전이 이번 달일 경우
                                if(day < threeWeekDay) {
                                    oneMonthPieMap[item!!.snackType] = 0.toFloat()
                                    oneMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if(year < threeWeekYear && year == oneMonthYear) { // 한달 전이 전년도일 경우
                            if(oneMonthMonth == month && day >= oneMonthDay) {
                                oneMonthPieMap[item!!.snackType] = 0.toFloat()
                                oneMonthMap[item!!.snackName] = 0
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "간식 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.snackRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun setOneMonthPieChart() {
        val nowSp = nowDate.split(".") // 오늘 날짜
        val nowYear = nowSp[0].toInt()
        val nowMonth = nowSp[1].toInt()
        val nowDay = nowSp[2].toInt()
        val nowDateNum = nowSp[0] + nowSp[1] + nowSp[2]

        val weekSp = weekDate.split(".") // 1주 전 날짜
        val weekYear = weekSp[0].toInt()
        val weekMonth = weekSp[1].toInt()
        val weekDay = weekSp[2].toInt()
        val weekDateNum = weekSp[0] + weekSp[1] + weekSp[2]

        val twoWeekSp = twoWeekDate.split(".") // 2주 전 날짜
        val twoWeekYear = twoWeekSp[0].toInt()
        val twoWeekMonth = twoWeekSp[1].toInt()
        val twoWeekDay = twoWeekSp[2].toInt()

        val threeWeekSp = threeWeekDate.split(".") // 3주 전 날짜
        val threeWeekYear = threeWeekSp[0].toInt()
        val threeWeekMonth = threeWeekSp[1].toInt()
        val threeWeekDay = threeWeekSp[2].toInt()

        val oneMonthWeekSp = oneMonthDate.split(".") // 1개월 전 날짜
        val oneMonthYear = oneMonthWeekSp[0].toInt()
        val oneMonthMonth = oneMonthWeekSp[1].toInt()
        val oneMonthDay = oneMonthWeekSp[2].toInt()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 간식 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogSnackModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if(year == nowYear && year == weekYear) { // 일주일 전이 같은 연도일 경우
                            if(month == nowMonth && month == weekMonth) { // 일주일 전이 같은 달일 경우
                                if(day in weekDay..nowDay) {
                                    oneMonthPieMap[item!!.snackType] = oneMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if(month < nowMonth && month == weekMonth) { // 일주일 전이 이전 달일 경우
                                if(day >= weekDay) {
                                    oneMonthPieMap[item!!.snackType] = oneMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if(month == nowMonth && month > weekMonth) { // 일주일 전이 이번 달일 경우
                                if(day <= nowDay) {
                                    oneMonthPieMap[item!!.snackType] = oneMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if(year < nowYear && year == weekYear) { // 일주일 전이 전년도일 경우
                            if(weekMonth == month && day >= weekDay) {
                                oneMonthPieMap[item!!.snackType] = oneMonthPieMap[item!!.snackType]!! + 1.toFloat()
                            }
                        }

                        if(year == weekYear && year == twoWeekYear) { // 2주일 전이 같은 연도일 경우
                            if(month == weekMonth && month == twoWeekMonth) { // 2주일 전이 같은 달일 경우
                                if(day in twoWeekDay until weekDay) {
                                    oneMonthPieMap[item!!.snackType] = oneMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if(month < weekMonth && month == twoWeekMonth) { // 2주일 전이 이전 달일 경우
                                if(day >= twoWeekDay) {
                                    oneMonthPieMap[item!!.snackType] = oneMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if(month == weekMonth && month > twoWeekMonth) { // 2주일 전이 이번 달일 경우
                                if(day < weekDay) {
                                    oneMonthPieMap[item!!.snackType] = oneMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if(year < weekYear && year == twoWeekYear) { // 2주일 전이 전년도일 경우
                            if(twoWeekMonth == month && day >= twoWeekDay) {
                                oneMonthPieMap[item!!.snackType] = oneMonthPieMap[item!!.snackType]!! + 1.toFloat()
                            }
                        }

                        if(year == twoWeekYear && year == threeWeekYear) { // 3주일 전이 같은 연도일 경우
                            if(month == twoWeekMonth && month == threeWeekMonth) { // 3주일 전이 같은 달일 경우
                                if(day in threeWeekDay until twoWeekDay) {
                                    oneMonthPieMap[item!!.snackType] = oneMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if(month < twoWeekMonth && month == threeWeekMonth) { // 3주일 전이 이전 달일 경우
                                if(day >= threeWeekDay) {
                                    oneMonthPieMap[item!!.snackType] = oneMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if(month == twoWeekMonth && month > threeWeekMonth) { // 3주일 전이 이번 달일 경우
                                if(day < twoWeekDay) {
                                    oneMonthPieMap[item!!.snackType] = oneMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if(year < twoWeekYear && year == threeWeekYear) { // 3주일 전이 전년도일 경우
                            if(threeWeekMonth == month && day >= threeWeekDay) {
                                oneMonthPieMap[item!!.snackType] = oneMonthPieMap[item!!.snackType]!! + 1.toFloat()
                            }
                        }

                        if(year == threeWeekYear && year == oneMonthYear) { // 한달 전이 같은 연도일 경우
                            if(month == threeWeekMonth && month == oneMonthMonth) { // 한달 전이 같은 달일 경우
                                if(day in oneMonthDay until threeWeekDay) {
                                    oneMonthPieMap[item!!.snackType] = oneMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if(month < threeWeekMonth && month == oneMonthMonth) { // 한달 전이 이전 달일 경우
                                if(day >= oneMonthDay) {
                                    oneMonthPieMap[item!!.snackType] = oneMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if(month == threeWeekMonth && month > oneMonthMonth) { // 한달 전이 이번 달일 경우
                                if(day < threeWeekDay) {
                                    oneMonthPieMap[item!!.snackType] = oneMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if(year < threeWeekYear && year == oneMonthYear) { // 한달 전이 전년도일 경우
                            if(oneMonthMonth == month && day >= oneMonthDay) {
                                oneMonthPieMap[item!!.snackType] = oneMonthPieMap[item!!.snackType]!! + 1.toFloat()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "간식 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.snackRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun pieOneMonthPieChartGraph(v : View, pieChart : PieChart, valList : MutableMap<String, Float>) {

        pieChart.extraBottomOffset = 15f // 간격
        pieChart.description.isEnabled = false // chart 밑에 description 표시 유무
        pieChart.setTouchEnabled(false)

        pieChart.run {
            description.isEnabled = false

            setTouchEnabled(false)
            legend.isEnabled = true

            setEntryLabelTextSize(10f)
            setEntryLabelColor(Color.parseColor("#000000"))
        }

        var sum = 0.toFloat()
        for((key, value) in valList.entries) {
            if(value >= 1) {
                sum += value
            }
        }

        var entries : ArrayList<PieEntry> = ArrayList()
        for((key, value) in valList.entries) {
            if(value >= 1) {
                entries.add(PieEntry(value / sum * 100, key))
            }
        }

        val colors: ArrayList<Int> = ArrayList()
        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)

        var depenses = PieDataSet(entries, "")
        with(depenses) {
            sliceSpace = 3f
            selectionShift = 5f
            valueTextSize = 5f
            setColors(colors)
        }
        depenses.valueFormatter = CustomFormatter()
        // depenses.color = Color.parseColor("#F9AC3A")

        val data = PieData(depenses)
        with(data) {
            setValueTextSize(10f)
            setValueTextColor(Color.parseColor("#000000"))
        }

        pieChart.run {
            this.data = data
            description.isEnabled = false
            animate()
            invalidate()
            centerText = "간식"
        }
    }

    private fun setOneMonthSnack() {
        oneMonthLabelList.clear()
        oneMonthDataList.clear()

        for((key, value) in oneMonthMap.entries) {
            oneMonthLabelList.add(key)
        }

        for(i in 0 until oneMonthLabelList.size)
            oneMonthDataList.add(oneMonthLabelList[i])

        oneMonthSnackStatisticsRVAdapter.notifyDataSetChanged()
    }

    private fun setThreeMonthBarChartReady() {
        val nowSp = nowDate.split(".") // 오늘 날짜
        val nowYear = nowSp[0].toInt()
        val nowMonth = nowSp[1].toInt()
        val nowDay = nowSp[2].toInt()
        val nowDateNum = nowSp[0] + nowSp[1] + nowSp[2]

        val oneMonthWeekSp = oneMonthDate.split(".") // 1개월 전 날짜
        val oneMonthYear = oneMonthWeekSp[0].toInt()
        val oneMonthMonth = oneMonthWeekSp[1].toInt()
        val oneMonthDay = oneMonthWeekSp[2].toInt()

        val twoMonthWeekSp = twoMonthDate.split(".") // 2개월 전 날짜
        val twoMonthYear = twoMonthWeekSp[0].toInt()
        val twoMonthMonth = twoMonthWeekSp[1].toInt()
        val twoMonthDay = twoMonthWeekSp[2].toInt()

        val threeMonthWeekSp = threeMonthDate.split(".") // 3개월 전 날짜
        val threeMonthYear = threeMonthWeekSp[0].toInt()
        val threeMonthMonth = threeMonthWeekSp[1].toInt()
        val threeMonthDay = threeMonthWeekSp[2].toInt()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 간식 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    threeMonthPieMap.clear()
                    threeMonthMap.clear()
                    threeMonthDataList.clear()
                    threeMonthLabelList.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogSnackModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if (year == nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도일 경우
                            if (month == nowMonth && month == oneMonthMonth) { // 1개월 전이 같은 달일 경우
                                if (day in oneMonthDay..nowDay) {
                                    threeMonthPieMap[item!!.snackType] = 0.toFloat()
                                    threeMonthMap[item!!.snackName] = 0
                                }
                            } else if (month < nowMonth && month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = 0.toFloat()
                                    threeMonthMap[item!!.snackName] = 0
                                }
                            } else if (month == nowMonth && month > oneMonthMonth) { // 1개월 전이 이번 달일 경우
                                if (day <= nowDay) {
                                    threeMonthPieMap[item!!.snackType] = 0.toFloat()
                                    threeMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 년도일 경우
                            if (month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = 0.toFloat()
                                    threeMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == nowYear && year > oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == nowMonth) { // 1개월 전이 이번달일 경우
                                if (day <= nowDay) {
                                    threeMonthPieMap[item!!.snackType] = 0.toFloat()
                                    threeMonthMap[item!!.snackName] = 0
                                }
                            }
                        }

                        if (year == oneMonthYear && year == twoMonthYear) { // 2개월 전에 오늘일 경우
                            if (month == oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 같은 달일 경우
                                if (day in twoMonthDay until oneMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = 0.toFloat()
                                    threeMonthMap[item!!.snackName] = 0
                                }
                            } else if (month < oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = 0.toFloat()
                                    threeMonthMap[item!!.snackName] = 0
                                }
                            } else if (month == oneMonthMonth && month > twoMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = 0.toFloat()
                                    threeMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < oneMonthYear && year == twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = 0.toFloat()
                                    threeMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == oneMonthYear && year > twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == oneMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = 0.toFloat()
                                    threeMonthMap[item!!.snackName] = 0
                                }
                            }
                        }

                        if (year == twoMonthYear && year == threeMonthYear) { // 3개월 전이 올해일 경우
                            if (month == twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 같은 달일 경우
                                if (day in threeMonthDay until twoMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = 0.toFloat()
                                    threeMonthMap[item!!.snackName] = 0
                                }
                            } else if (month < twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = 0.toFloat()
                                    threeMonthMap[item!!.snackName] = 0
                                }
                            } else if (month == twoMonthMonth && month > threeMonthMonth) { // 3개월 전이 이번달일 경우
                                if (day < twoMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = 0.toFloat()
                                    threeMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < twoMonthYear && year == threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = 0.toFloat()
                                    threeMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == twoMonthYear && year > threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == twoMonthMonth) {
                                if (day < twoMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = 0.toFloat()
                                    threeMonthMap[item!!.snackName] = 0
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "간식 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.snackRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun setThreeMonthBarChart() {
        val nowSp = nowDate.split(".") // 오늘 날짜
        val nowYear = nowSp[0].toInt()
        val nowMonth = nowSp[1].toInt()
        val nowDay = nowSp[2].toInt()
        val nowDateNum = nowSp[0] + nowSp[1] + nowSp[2]

        val oneMonthWeekSp = oneMonthDate.split(".") // 1개월 전 날짜
        val oneMonthYear = oneMonthWeekSp[0].toInt()
        val oneMonthMonth = oneMonthWeekSp[1].toInt()
        val oneMonthDay = oneMonthWeekSp[2].toInt()

        val twoMonthWeekSp = twoMonthDate.split(".") // 2개월 전 날짜
        val twoMonthYear = twoMonthWeekSp[0].toInt()
        val twoMonthMonth = twoMonthWeekSp[1].toInt()
        val twoMonthDay = twoMonthWeekSp[2].toInt()

        val threeMonthWeekSp = threeMonthDate.split(".") // 3개월 전 날짜
        val threeMonthYear = threeMonthWeekSp[0].toInt()
        val threeMonthMonth = threeMonthWeekSp[1].toInt()
        val threeMonthDay = threeMonthWeekSp[2].toInt()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 간식 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogSnackModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if (year == nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도일 경우
                            if (month == nowMonth && month == oneMonthMonth) { // 1개월 전이 같은 달일 경우
                                if (day in oneMonthDay..nowDay) {
                                    threeMonthPieMap[item!!.snackType] = threeMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < nowMonth && month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = threeMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == nowMonth && month > oneMonthMonth) { // 1개월 전이 이번 달일 경우
                                if (day <= nowDay) {
                                    threeMonthPieMap[item!!.snackType] = threeMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 년도일 경우
                            if (month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = threeMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == nowYear && year > oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == nowMonth) { // 1개월 전이 이번달일 경우
                                if (day <= nowDay) {
                                    threeMonthPieMap[item!!.snackType] = threeMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        }

                        if (year == oneMonthYear && year == twoMonthYear) { // 2개월 전에 오늘일 경우
                            if (month == oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 같은 달일 경우
                                if (day in twoMonthDay until oneMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = threeMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = threeMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == oneMonthMonth && month > twoMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = threeMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < oneMonthYear && year == twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = threeMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == oneMonthYear && year > twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == oneMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = threeMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        }

                        if (year == twoMonthYear && year == threeMonthYear) { // 3개월 전이 올해일 경우
                            if (month == twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 같은 달일 경우
                                if (day in threeMonthDay until twoMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = threeMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = threeMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == twoMonthMonth && month > threeMonthMonth) { // 3개월 전이 이번달일 경우
                                if (day < twoMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = threeMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < twoMonthYear && year == threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = threeMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == twoMonthYear && year > threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == twoMonthMonth) {
                                if (day < twoMonthDay) {
                                    threeMonthPieMap[item!!.snackType] = threeMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "간식 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.snackRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun pieThreeMonthBarChartGraph(v : View, pieChart : PieChart, valList : MutableMap<String, Float>) {

        pieChart.extraBottomOffset = 15f // 간격
        pieChart.description.isEnabled = false // chart 밑에 description 표시 유무
        pieChart.setTouchEnabled(false)

        pieChart.run {
            description.isEnabled = false

            setTouchEnabled(false)
            legend.isEnabled = true

            setEntryLabelTextSize(10f)
            setEntryLabelColor(Color.parseColor("#000000"))
        }

        var sum = 0.toFloat()
        for((key, value) in valList.entries) {
            if(value >= 1) {
                sum += value
            }
        }

        var entries : ArrayList<PieEntry> = ArrayList()
        for((key, value) in valList.entries) {
            if(value >= 1) {
                entries.add(PieEntry(value / sum * 100, key))
            }
        }

        val colors: ArrayList<Int> = ArrayList()
        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)

        var depenses = PieDataSet(entries, "")
        with(depenses) {
            sliceSpace = 3f
            selectionShift = 5f
            valueTextSize = 5f
            setColors(colors)
        }
        depenses.valueFormatter = CustomFormatter()
        // depenses.color = Color.parseColor("#F9AC3A")

        val data = PieData(depenses)
        with(data) {
            setValueTextSize(10f)
            setValueTextColor(Color.parseColor("#000000"))
        }

        pieChart.run {
            this.data = data
            description.isEnabled = false
            animate()
            invalidate()
            centerText = "간식"
        }
    }

    private fun setThreeMonthSnack() {
        threeMonthLabelList.clear()
        threeMonthDataList.clear()

        for((key, value) in threeMonthMap.entries) {
            threeMonthLabelList.add(key)
        }

        for(i in 0 until threeMonthLabelList.size)
            threeMonthDataList.add(threeMonthLabelList[i])

        threeMonthSnackStatisticsRVAdapter.notifyDataSetChanged()
    }

    private fun setSixPieMonthChartReady() {
        val nowSp = nowDate.split(".") // 오늘 날짜
        val nowYear = nowSp[0].toInt()
        val nowMonth = nowSp[1].toInt()
        val nowDay = nowSp[2].toInt()
        val nowDateNum = nowSp[0] + nowSp[1] + nowSp[2]

        val oneMonthWeekSp = oneMonthDate.split(".") // 1개월 전 날짜
        val oneMonthYear = oneMonthWeekSp[0].toInt()
        val oneMonthMonth = oneMonthWeekSp[1].toInt()
        val oneMonthDay = oneMonthWeekSp[2].toInt()

        val twoMonthWeekSp = twoMonthDate.split(".") // 2개월 전 날짜
        val twoMonthYear = twoMonthWeekSp[0].toInt()
        val twoMonthMonth = twoMonthWeekSp[1].toInt()
        val twoMonthDay = twoMonthWeekSp[2].toInt()

        val threeMonthWeekSp = threeMonthDate.split(".") // 3개월 전 날짜
        val threeMonthYear = threeMonthWeekSp[0].toInt()
        val threeMonthMonth = threeMonthWeekSp[1].toInt()
        val threeMonthDay = threeMonthWeekSp[2].toInt()

        val fourMonthWeekSp = fourMonthDate.split(".") // 4개월 전 날짜
        val fourMonthYear = fourMonthWeekSp[0].toInt()
        val fourMonthMonth = fourMonthWeekSp[1].toInt()
        val fourMonthDay = fourMonthWeekSp[2].toInt()

        val fiveMonthWeekSp = fiveMonthDate.split(".") // 5개월 전 날짜
        val fiveMonthYear = fiveMonthWeekSp[0].toInt()
        val fiveMonthMonth = fiveMonthWeekSp[1].toInt()
        val fiveMonthDay = fiveMonthWeekSp[2].toInt()

        val sixMonthWeekSp = sixMonthDate.split(".") // 6개월 전 날짜
        val sixMonthYear = sixMonthWeekSp[0].toInt()
        val sixMonthMonth = sixMonthWeekSp[1].toInt()
        val sixMonthDay = sixMonthWeekSp[2].toInt()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 간식 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    sixMonthPieMap.clear()
                    sixMonthMap.clear()
                    sixMonthDataList.clear()
                    sixMonthLabelList.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogSnackModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if (year == nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도일 경우
                            if (month == nowMonth && month == oneMonthMonth) { // 1개월 전이 같은 달일 경우
                                if (day in oneMonthDay..nowDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            } else if (month < nowMonth && month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            } else if (month == nowMonth && month > oneMonthMonth) { // 1개월 전이 이번 달일 경우
                                if (day <= nowDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 년도일 경우
                            if (month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == nowYear && year > oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == nowMonth) { // 1개월 전이 이번달일 경우
                                if (day <= nowDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            }
                        }

                        if (year == oneMonthYear && year == twoMonthYear) { // 2개월 전에 오늘일 경우
                            if (month == oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 같은 달일 경우
                                if (day in twoMonthDay until oneMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            } else if (month < oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            } else if (month == oneMonthMonth && month > twoMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < oneMonthYear && year == twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == oneMonthYear && year > twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == oneMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            }
                        }

                        if (year == twoMonthYear && year == threeMonthYear) { // 3개월 전이 올해일 경우
                            if (month == twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 같은 달일 경우
                                if (day in threeMonthDay until twoMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            } else if (month < twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            } else if (month == twoMonthMonth && month > threeMonthMonth) { // 3개월 전이 이번달일 경우
                                if (day < twoMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < twoMonthYear && year == threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == twoMonthYear && year > threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == twoMonthMonth) {
                                if (day < twoMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            }
                        }

                        if (year == threeMonthYear && year == fourMonthYear) { // 4개월 전이 올해일 경우
                            if (month == threeMonthMonth && month == fourMonthMonth) { // 4개월 전이 같은 달일 경우
                                if (day in fourMonthDay until threeMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            } else if (month < threeMonthMonth && month == fourMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            } else if (month == threeMonthMonth && month > fourMonthMonth) { // 4개월 전이 이번달일 경우
                                if (day < threeMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < threeMonthYear && year == fourMonthYear) { // 4개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == fourMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == threeMonthYear && year > fourMonthYear) { // 4개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == threeMonthMonth) {
                                if (day < threeMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            }
                        }

                        if (year == fourMonthYear && year == fiveMonthYear) { // 5개월 전이 올해일 경우
                            if (month == fourMonthMonth && month == fiveMonthMonth) { // 5개월 전이 같은 달일 경우
                                if (day in fiveMonthDay until fourMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            } else if (month < fourMonthMonth && month == fiveMonthMonth) { // 5개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            } else if (month == fourMonthMonth && month > fiveMonthMonth) { // 5개월 전이 이번달일 경우
                                if (day < fiveMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < fourMonthYear && year == fiveMonthYear) { // 5개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == fiveMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fiveMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == fourMonthYear && year > fiveMonthYear) { // 5개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == fourMonthMonth) {
                                if (day < fourMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            }
                        }

                        if (year == fiveMonthYear && year == sixMonthYear) { // 6개월 전이 올해일 경우
                            if (month == fiveMonthMonth && month == sixMonthMonth) { // 6개월 전이 같은 달일 경우
                                if (day in sixMonthDay until fiveMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            } else if (month < fiveMonthMonth && month == sixMonthMonth) { // 6개월 전이 전달일 경우
                                if (day >= sixMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            } else if (month == fiveMonthMonth && month > sixMonthMonth) { // 6개월 전이 이번달일 경우
                                if (day < fiveMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < fiveMonthYear && year == sixMonthYear) { // 6개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == sixMonthMonth) { // 6개월 전이 전달일 경우
                                if (day >= sixMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                    sixMonthMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == fiveMonthYear && year > sixMonthYear) { // 6개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(day < fiveMonthDay) {
                                sixMonthPieMap[item!!.snackType] = 0.toFloat()
                                sixMonthMap[item!!.snackName] = 0
                            }
                        }

                    }
                } catch (e: Exception) {
                    Log.d(TAG, "간식 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.snackRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun setSixPieMonthChart() {
        val nowSp = nowDate.split(".") // 오늘 날짜
        val nowYear = nowSp[0].toInt()
        val nowMonth = nowSp[1].toInt()
        val nowDay = nowSp[2].toInt()
        val nowDateNum = nowSp[0] + nowSp[1] + nowSp[2]

        val oneMonthWeekSp = oneMonthDate.split(".") // 1개월 전 날짜
        val oneMonthYear = oneMonthWeekSp[0].toInt()
        val oneMonthMonth = oneMonthWeekSp[1].toInt()
        val oneMonthDay = oneMonthWeekSp[2].toInt()

        val twoMonthWeekSp = twoMonthDate.split(".") // 2개월 전 날짜
        val twoMonthYear = twoMonthWeekSp[0].toInt()
        val twoMonthMonth = twoMonthWeekSp[1].toInt()
        val twoMonthDay = twoMonthWeekSp[2].toInt()

        val threeMonthWeekSp = threeMonthDate.split(".") // 3개월 전 날짜
        val threeMonthYear = threeMonthWeekSp[0].toInt()
        val threeMonthMonth = threeMonthWeekSp[1].toInt()
        val threeMonthDay = threeMonthWeekSp[2].toInt()

        val fourMonthWeekSp = fourMonthDate.split(".") // 4개월 전 날짜
        val fourMonthYear = fourMonthWeekSp[0].toInt()
        val fourMonthMonth = fourMonthWeekSp[1].toInt()
        val fourMonthDay = fourMonthWeekSp[2].toInt()

        val fiveMonthWeekSp = fiveMonthDate.split(".") // 5개월 전 날짜
        val fiveMonthYear = fiveMonthWeekSp[0].toInt()
        val fiveMonthMonth = fiveMonthWeekSp[1].toInt()
        val fiveMonthDay = fiveMonthWeekSp[2].toInt()

        val sixMonthWeekSp = sixMonthDate.split(".") // 6개월 전 날짜
        val sixMonthYear = sixMonthWeekSp[0].toInt()
        val sixMonthMonth = sixMonthWeekSp[1].toInt()
        val sixMonthDay = sixMonthWeekSp[2].toInt()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 간식 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogSnackModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if (year == nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도일 경우
                            if (month == nowMonth && month == oneMonthMonth) { // 1개월 전이 같은 달일 경우
                                if (day in oneMonthDay..nowDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < nowMonth && month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == nowMonth && month > oneMonthMonth) { // 1개월 전이 이번 달일 경우
                                if (day <= nowDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 년도일 경우
                            if (month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == nowYear && year > oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == nowMonth) { // 1개월 전이 이번달일 경우
                                if (day <= nowDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        }

                        if (year == oneMonthYear && year == twoMonthYear) { // 2개월 전에 오늘일 경우
                            if (month == oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 같은 달일 경우
                                if (day in twoMonthDay until oneMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == oneMonthMonth && month > twoMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < oneMonthYear && year == twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == oneMonthYear && year > twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == oneMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        }

                        if (year == twoMonthYear && year == threeMonthYear) { // 3개월 전이 올해일 경우
                            if (month == twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 같은 달일 경우
                                if (day in threeMonthDay until twoMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == twoMonthMonth && month > threeMonthMonth) { // 3개월 전이 이번달일 경우
                                if (day < twoMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < twoMonthYear && year == threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == twoMonthYear && year > threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == twoMonthMonth) {
                                if (day < twoMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        }

                        if (year == threeMonthYear && year == fourMonthYear) { // 4개월 전이 올해일 경우
                            if (month == threeMonthMonth && month == fourMonthMonth) { // 4개월 전이 같은 달일 경우
                                if (day in fourMonthDay until threeMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < threeMonthMonth && month == fourMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == threeMonthMonth && month > fourMonthMonth) { // 4개월 전이 이번달일 경우
                                if (day < threeMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < threeMonthYear && year == fourMonthYear) { // 4개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == fourMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == threeMonthYear && year > fourMonthYear) { // 4개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == threeMonthMonth) {
                                if (day < threeMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        }

                        if (year == fourMonthYear && year == fiveMonthYear) { // 5개월 전이 올해일 경우
                            if (month == fourMonthMonth && month == fiveMonthMonth) { // 5개월 전이 같은 달일 경우
                                if (day in fiveMonthDay until fourMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < fourMonthMonth && month == fiveMonthMonth) { // 5개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == fourMonthMonth && month > fiveMonthMonth) { // 5개월 전이 이번달일 경우
                                if (day < fiveMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < fourMonthYear && year == fiveMonthYear) { // 5개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == fiveMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fiveMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == fourMonthYear && year > fiveMonthYear) { // 5개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == fourMonthMonth) {
                                if (day < fourMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        }

                        if (year == fiveMonthYear && year == sixMonthYear) { // 6개월 전이 올해일 경우
                            if (month == fiveMonthMonth && month == sixMonthMonth) { // 6개월 전이 같은 달일 경우
                                if (day in sixMonthDay until fiveMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < fiveMonthMonth && month == sixMonthMonth) { // 6개월 전이 전달일 경우
                                if (day >= sixMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == fiveMonthMonth && month > sixMonthMonth) { // 6개월 전이 이번달일 경우
                                if (day < fiveMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < fiveMonthYear && year == sixMonthYear) { // 6개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == sixMonthMonth) { // 6개월 전이 전달일 경우
                                if (day >= sixMonthDay) {
                                    sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == fiveMonthYear && year > sixMonthYear) { // 6개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(day < fiveMonthDay) {
                                sixMonthPieMap[item!!.snackType] = sixMonthPieMap[item!!.snackType]!! + 1.toFloat()
                            }
                        }

                    }
                } catch (e: Exception) {
                    Log.d(TAG, "간식 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.snackRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun pieSixMonthChartGraph(v : View, pieChart : PieChart, valList : MutableMap<String, Float>) {

        pieChart.extraBottomOffset = 15f // 간격
        pieChart.description.isEnabled = false // chart 밑에 description 표시 유무
        pieChart.setTouchEnabled(false)

        pieChart.run {
            description.isEnabled = false

            setTouchEnabled(false)
            legend.isEnabled = true

            setEntryLabelTextSize(10f)
            setEntryLabelColor(Color.parseColor("#000000"))
        }

        var sum = 0.toFloat()
        for((key, value) in valList.entries) {
            if(value >= 1) {
                sum += value
            }
        }

        var entries : ArrayList<PieEntry> = ArrayList()
        for((key, value) in valList.entries) {
            if(value >= 1) {
                entries.add(PieEntry(value / sum * 100, key))
            }
        }

        val colors: ArrayList<Int> = ArrayList()
        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)

        var depenses = PieDataSet(entries, "")
        with(depenses) {
            sliceSpace = 3f
            selectionShift = 5f
            valueTextSize = 5f
            setColors(colors)
        }
        depenses.valueFormatter = MealStatisticsFragment.CustomFormatter()
        // depenses.color = Color.parseColor("#F9AC3A")

        val data = PieData(depenses)
        with(data) {
            setValueTextSize(10f)
            setValueTextColor(Color.parseColor("#000000"))
        }

        pieChart.run {
            this.data = data
            description.isEnabled = false
            animate()
            invalidate()
            centerText = "간식"
        }
    }

    private fun setSixMonthSnack() {
        sixMonthLabelList.clear()
        sixMonthDataList.clear()

        for((key, value) in sixMonthMap.entries) {
            sixMonthLabelList.add(key)
        }

        for(i in 0 until sixMonthLabelList.size)
            sixMonthDataList.add(sixMonthLabelList[i])

        sixMonthSnackStatisticsRVAdapter.notifyDataSetChanged()
    }

    private fun setYearPieChartReady() {
        val nowSp = nowDate.split(".") // 오늘 날짜
        val nowYear = nowSp[0].toInt()
        val nowMonth = nowSp[1].toInt()
        val nowDay = nowSp[2].toInt()
        val nowDateNum = nowSp[0] + nowSp[1] + nowSp[2]

        val oneMonthWeekSp = oneMonthDate.split(".") // 1개월 전 날짜
        val oneMonthYear = oneMonthWeekSp[0].toInt()
        val oneMonthMonth = oneMonthWeekSp[1].toInt()
        val oneMonthDay = oneMonthWeekSp[2].toInt()

        val twoMonthWeekSp = twoMonthDate.split(".") // 2개월 전 날짜
        val twoMonthYear = twoMonthWeekSp[0].toInt()
        val twoMonthMonth = twoMonthWeekSp[1].toInt()
        val twoMonthDay = twoMonthWeekSp[2].toInt()

        val threeMonthWeekSp = threeMonthDate.split(".") // 3개월 전 날짜
        val threeMonthYear = threeMonthWeekSp[0].toInt()
        val threeMonthMonth = threeMonthWeekSp[1].toInt()
        val threeMonthDay = threeMonthWeekSp[2].toInt()

        val fourMonthWeekSp = fourMonthDate.split(".") // 4개월 전 날짜
        val fourMonthYear = fourMonthWeekSp[0].toInt()
        val fourMonthMonth = fourMonthWeekSp[1].toInt()
        val fourMonthDay = fourMonthWeekSp[2].toInt()

        val fiveMonthWeekSp = fiveMonthDate.split(".") // 5개월 전 날짜
        val fiveMonthYear = fiveMonthWeekSp[0].toInt()
        val fiveMonthMonth = fiveMonthWeekSp[1].toInt()
        val fiveMonthDay = fiveMonthWeekSp[2].toInt()

        val sixMonthWeekSp = sixMonthDate.split(".") // 6개월 전 날짜
        val sixMonthYear = sixMonthWeekSp[0].toInt()
        val sixMonthMonth = sixMonthWeekSp[1].toInt()
        val sixMonthDay = sixMonthWeekSp[2].toInt()

        val sevenMonthWeekSp = sevenMonthDate.split(".") // 7개월 전 날짜
        val sevenMonthYear = sevenMonthWeekSp[0].toInt()
        val sevenMonthMonth = sevenMonthWeekSp[1].toInt()
        val sevenMonthDay = sevenMonthWeekSp[2].toInt()

        val eightMonthWeekSp = eightMonthDate.split(".") // 8개월 전 날짜
        val eightMonthYear = eightMonthWeekSp[0].toInt()
        val eightMonthMonth = eightMonthWeekSp[1].toInt()
        val eightMonthDay = eightMonthWeekSp[2].toInt()

        val nineMonthWeekSp = nineMonthDate.split(".") // 9개월 전 날짜
        val nineMonthYear = nineMonthWeekSp[0].toInt()
        val nineMonthMonth = nineMonthWeekSp[1].toInt()
        val nineMonthDay = nineMonthWeekSp[2].toInt()

        val tenMonthWeekSp = tenMonthDate.split(".") // 10개월 전 날짜
        val tenMonthYear = tenMonthWeekSp[0].toInt()
        val tenMonthMonth = tenMonthWeekSp[1].toInt()
        val tenMonthDay = tenMonthWeekSp[2].toInt()

        val elevenMonthWeekSp = elevenMonthDate.split(".") // 11개월 전 날짜
        val elevenMonthYear = elevenMonthWeekSp[0].toInt()
        val elevenMonthMonth = elevenMonthWeekSp[1].toInt()
        val elevenMonthDay = elevenMonthWeekSp[2].toInt()

        val yearWeekSp = yearDate.split(".") // 1년 전 날짜
        val yearYear = yearWeekSp[0].toInt()
        val yearMonth = yearWeekSp[1].toInt()
        val yearDay = yearWeekSp[2].toInt()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 간식 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    yearPieMap.clear()
                    yearMap.clear()
                    yearDataList.clear()
                    yearLabelList.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogSnackModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if (year == nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도일 경우
                            if (month == nowMonth && month == oneMonthMonth) { // 1개월 전이 같은 달일 경우
                                if (day in oneMonthDay..nowDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month < nowMonth && month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month == nowMonth && month > oneMonthMonth) { // 1개월 전이 이번 달일 경우
                                if (day <= nowDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 년도일 경우
                            if (month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == nowYear && year > oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == nowMonth) { // 1개월 전이 이번달일 경우
                                if (day <= nowDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        }

                        if (year == oneMonthYear && year == twoMonthYear) { // 2개월 전에 오늘일 경우
                            if (month == oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 같은 달일 경우
                                if (day in twoMonthDay until oneMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month < oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month == oneMonthMonth && month > twoMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < oneMonthYear && year == twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == oneMonthYear && year > twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == oneMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        }

                        if (year == twoMonthYear && year == threeMonthYear) { // 3개월 전이 올해일 경우
                            if (month == twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 같은 달일 경우
                                if (day in threeMonthDay until twoMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month < twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month == twoMonthMonth && month > threeMonthMonth) { // 3개월 전이 이번달일 경우
                                if (day < twoMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < twoMonthYear && year == threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == twoMonthYear && year > threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == twoMonthMonth) {
                                if (day < twoMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        }

                        if (year == threeMonthYear && year == fourMonthYear) { // 4개월 전이 올해일 경우
                            if (month == threeMonthMonth && month == fourMonthMonth) { // 4개월 전이 같은 달일 경우
                                if (day in fourMonthDay until threeMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month < threeMonthMonth && month == fourMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month == threeMonthMonth && month > fourMonthMonth) { // 4개월 전이 이번달일 경우
                                if (day < threeMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < threeMonthYear && year == fourMonthYear) { // 4개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == fourMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == threeMonthYear && year > fourMonthYear) { // 4개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == threeMonthMonth) {
                                if (day < threeMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        }

                        if (year == fourMonthYear && year == fiveMonthYear) { // 5개월 전이 올해일 경우
                            if (month == fourMonthMonth && month == fiveMonthMonth) { // 5개월 전이 같은 달일 경우
                                if (day in fiveMonthDay until fourMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month < fourMonthMonth && month == fiveMonthMonth) { // 5개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month == fourMonthMonth && month > fiveMonthMonth) { // 5개월 전이 이번달일 경우
                                if (day < fiveMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < fourMonthYear && year == fiveMonthYear) { // 5개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == fiveMonthMonth) { // 5개월 전이 전달일 경우
                                if (day >= fiveMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == fourMonthYear && year > fiveMonthYear) { // 5개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == fourMonthMonth) {
                                if (day < fourMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        }

                        if (year == fiveMonthYear && year == sixMonthYear) { // 6개월 전이 올해일 경우
                            if (month == fiveMonthMonth && month == sixMonthMonth) { // 6개월 전이 같은 달일 경우
                                if (day in sixMonthDay until fiveMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month < fiveMonthMonth && month == sixMonthMonth) { // 6개월 전이 전달일 경우
                                if (day >= sixMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month == fiveMonthMonth && month > sixMonthMonth) { // 6개월 전이 이번달일 경우
                                if (day < fiveMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < fiveMonthYear && year == sixMonthYear) { // 6개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == sixMonthMonth) { // 6개월 전이 전달일 경우
                                if (day >= sixMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == fiveMonthYear && year > sixMonthYear) { // 6개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == fiveMonthMonth) {
                                if (day < fiveMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        }

                        if (year == sixMonthYear && year == sevenMonthYear) { // 7개월 전이 올해일 경우
                            if (month == sixMonthMonth && month == sevenMonthMonth) { // 7개월 전이 같은 달일 경우
                                if (day in sevenMonthDay until sixMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month < sixMonthMonth && month == sevenMonthMonth) { // 7개월 전이 전달일 경우
                                if (day >= sevenMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month == sixMonthMonth && month > sevenMonthMonth) { // 7개월 전이 이번달일 경우
                                if (day < sixMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < sixMonthYear && year == sevenMonthYear) { // 7개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == sevenMonthMonth) { // 7개월 전이 전달일 경우
                                if (day >= sevenMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == sixMonthYear && year > sevenMonthYear) { // 7개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == sixMonthMonth) {
                                if (day < sixMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        }

                        if (year == sevenMonthYear && year == eightMonthYear) { // 8개월 전이 올해일 경우
                            if (month == sevenMonthMonth && month == eightMonthMonth) { // 8개월 전이 같은 달일 경우
                                if (day in eightMonthDay until sevenMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month < sevenMonthMonth && month == eightMonthMonth) { // 8개월 전이 전달일 경우
                                if (day >= eightMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month == sevenMonthMonth && month > eightMonthMonth) { // 8개월 전이 이번달일 경우
                                if (day < sevenMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < sevenMonthYear && year == eightMonthYear) { // 8개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == eightMonthMonth) { // 8개월 전이 전달일 경우
                                if (day >= eightMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == sevenMonthYear && year > eightMonthYear) { // 8개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == sevenMonthMonth) {
                                if (day < sevenMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        }

                        if (year == eightMonthYear && year == nineMonthYear) { // 9개월 전이 올해일 경우
                            if (month == eightMonthMonth && month == nineMonthMonth) { // 9개월 전이 같은 달일 경우
                                if (day in nineMonthDay until eightMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month < eightMonthMonth && month == nineMonthMonth) { // 9개월 전이 전달일 경우
                                if (day >= nineMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month == eightMonthMonth && month > nineMonthMonth) { // 9개월 전이 이번달일 경우
                                if (day < eightMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < eightMonthYear && year == nineMonthYear) { // 9개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == nineMonthMonth) { // 9개월 전이 전달일 경우
                                if (day >= nineMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == eightMonthYear && year > nineMonthYear) { // 9개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == eightMonthMonth) {
                                if (day < eightMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        }

                        if (year == nineMonthYear && year == tenMonthYear) { // 10개월 전이 올해일 경우
                            if (month == nineMonthMonth && month == tenMonthMonth) { // 10개월 전이 같은 달일 경우
                                if (day in tenMonthDay until nineMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month < nineMonthMonth && month == tenMonthMonth) { // 10개월 전이 전달일 경우
                                if (day >= tenMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month == nineMonthMonth && month > tenMonthMonth) { // 10개월 전이 이번달일 경우
                                if (day < nineMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < nineMonthYear && year == tenMonthYear) { // 10개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == tenMonthMonth) { // 10개월 전이 전달일 경우
                                if (day >= tenMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == nineMonthYear && year > tenMonthYear) { // 10개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == nineMonthMonth) {
                                if (day < nineMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        }

                        if (year == tenMonthYear && year == elevenMonthYear) { // 11개월 전이 올해일 경우
                            if (month == tenMonthMonth && month == elevenMonthMonth) { // 11개월 전이 같은 달일 경우
                                if (day in elevenMonthDay until tenMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month < tenMonthMonth && month == elevenMonthMonth) { // 11개월 전이 전달일 경우
                                if (day >= elevenMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month == tenMonthMonth && month > elevenMonthMonth) { // 11개월 전이 이번달일 경우
                                if (day < tenMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < tenMonthYear && year == elevenMonthYear) { // 11개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == elevenMonthMonth) { // 11개월 전이 전달일 경우
                                if (day >= elevenMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == tenMonthYear && year > elevenMonthYear) { // 11개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == tenMonthMonth) {
                                if (day < tenMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        }

                        if (year == elevenMonthYear && year == yearYear) { // 1년 전이 올해일 경우
                            if (month == elevenMonthMonth && month == yearMonth) { // 1년 전이 같은 달일 경우
                                if (day in yearDay until elevenMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month < elevenMonthMonth && month == yearMonth) { // 1년 전이 전달일 경우
                                if (day >= yearDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            } else if (month == elevenMonthMonth && month > yearMonth) { // 1년 전이 이번달일 경우
                                if (day < elevenMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year < elevenMonthYear && year == yearYear) { // 1년 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == yearMonth) { // 1년 전이 전달일 경우
                                if (day >= yearDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        } else if (year == elevenMonthYear && year > yearYear) { // 1년 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == elevenMonthMonth) {
                                if (day < elevenMonthDay) {
                                    yearPieMap[item!!.snackType] = 0.toFloat()
                                    yearMap[item!!.snackName] = 0
                                }
                            }
                        }

                    }
                } catch (e: Exception) {
                    Log.d(TAG, "간식 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.snackRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun setYearPieChart() {
        val nowSp = nowDate.split(".") // 오늘 날짜
        val nowYear = nowSp[0].toInt()
        val nowMonth = nowSp[1].toInt()
        val nowDay = nowSp[2].toInt()
        val nowDateNum = nowSp[0] + nowSp[1] + nowSp[2]

        val oneMonthWeekSp = oneMonthDate.split(".") // 1개월 전 날짜
        val oneMonthYear = oneMonthWeekSp[0].toInt()
        val oneMonthMonth = oneMonthWeekSp[1].toInt()
        val oneMonthDay = oneMonthWeekSp[2].toInt()

        val twoMonthWeekSp = twoMonthDate.split(".") // 2개월 전 날짜
        val twoMonthYear = twoMonthWeekSp[0].toInt()
        val twoMonthMonth = twoMonthWeekSp[1].toInt()
        val twoMonthDay = twoMonthWeekSp[2].toInt()

        val threeMonthWeekSp = threeMonthDate.split(".") // 3개월 전 날짜
        val threeMonthYear = threeMonthWeekSp[0].toInt()
        val threeMonthMonth = threeMonthWeekSp[1].toInt()
        val threeMonthDay = threeMonthWeekSp[2].toInt()

        val fourMonthWeekSp = fourMonthDate.split(".") // 4개월 전 날짜
        val fourMonthYear = fourMonthWeekSp[0].toInt()
        val fourMonthMonth = fourMonthWeekSp[1].toInt()
        val fourMonthDay = fourMonthWeekSp[2].toInt()

        val fiveMonthWeekSp = fiveMonthDate.split(".") // 5개월 전 날짜
        val fiveMonthYear = fiveMonthWeekSp[0].toInt()
        val fiveMonthMonth = fiveMonthWeekSp[1].toInt()
        val fiveMonthDay = fiveMonthWeekSp[2].toInt()

        val sixMonthWeekSp = sixMonthDate.split(".") // 6개월 전 날짜
        val sixMonthYear = sixMonthWeekSp[0].toInt()
        val sixMonthMonth = sixMonthWeekSp[1].toInt()
        val sixMonthDay = sixMonthWeekSp[2].toInt()

        val sevenMonthWeekSp = sevenMonthDate.split(".") // 7개월 전 날짜
        val sevenMonthYear = sevenMonthWeekSp[0].toInt()
        val sevenMonthMonth = sevenMonthWeekSp[1].toInt()
        val sevenMonthDay = sevenMonthWeekSp[2].toInt()

        val eightMonthWeekSp = eightMonthDate.split(".") // 8개월 전 날짜
        val eightMonthYear = eightMonthWeekSp[0].toInt()
        val eightMonthMonth = eightMonthWeekSp[1].toInt()
        val eightMonthDay = eightMonthWeekSp[2].toInt()

        val nineMonthWeekSp = nineMonthDate.split(".") // 9개월 전 날짜
        val nineMonthYear = nineMonthWeekSp[0].toInt()
        val nineMonthMonth = nineMonthWeekSp[1].toInt()
        val nineMonthDay = nineMonthWeekSp[2].toInt()

        val tenMonthWeekSp = tenMonthDate.split(".") // 10개월 전 날짜
        val tenMonthYear = tenMonthWeekSp[0].toInt()
        val tenMonthMonth = tenMonthWeekSp[1].toInt()
        val tenMonthDay = tenMonthWeekSp[2].toInt()

        val elevenMonthWeekSp = elevenMonthDate.split(".") // 11개월 전 날짜
        val elevenMonthYear = elevenMonthWeekSp[0].toInt()
        val elevenMonthMonth = elevenMonthWeekSp[1].toInt()
        val elevenMonthDay = elevenMonthWeekSp[2].toInt()

        val yearWeekSp = yearDate.split(".") // 1년 전 날짜
        val yearYear = yearWeekSp[0].toInt()
        val yearMonth = yearWeekSp[1].toInt()
        val yearDay = yearWeekSp[2].toInt()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 간식 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogSnackModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if (year == nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도일 경우
                            if (month == nowMonth && month == oneMonthMonth) { // 1개월 전이 같은 달일 경우
                                if (day in oneMonthDay..nowDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < nowMonth && month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == nowMonth && month > oneMonthMonth) { // 1개월 전이 이번 달일 경우
                                if (day <= nowDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 년도일 경우
                            if (month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == nowYear && year > oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == nowMonth) { // 1개월 전이 이번달일 경우
                                if (day <= nowDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        }

                        if (year == oneMonthYear && year == twoMonthYear) { // 2개월 전에 오늘일 경우
                            if (month == oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 같은 달일 경우
                                if (day in twoMonthDay until oneMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == oneMonthMonth && month > twoMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < oneMonthYear && year == twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == oneMonthYear && year > twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == oneMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        }

                        if (year == twoMonthYear && year == threeMonthYear) { // 3개월 전이 올해일 경우
                            if (month == twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 같은 달일 경우
                                if (day in threeMonthDay until twoMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == twoMonthMonth && month > threeMonthMonth) { // 3개월 전이 이번달일 경우
                                if (day < twoMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < twoMonthYear && year == threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == twoMonthYear && year > threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == twoMonthMonth) {
                                if (day < twoMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        }

                        if (year == threeMonthYear && year == fourMonthYear) { // 4개월 전이 올해일 경우
                            if (month == threeMonthMonth && month == fourMonthMonth) { // 4개월 전이 같은 달일 경우
                                if (day in fourMonthDay until threeMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < threeMonthMonth && month == fourMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == threeMonthMonth && month > fourMonthMonth) { // 4개월 전이 이번달일 경우
                                if (day < threeMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < threeMonthYear && year == fourMonthYear) { // 4개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == fourMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == threeMonthYear && year > fourMonthYear) { // 4개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == threeMonthMonth) {
                                if (day < threeMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        }

                        if (year == fourMonthYear && year == fiveMonthYear) { // 5개월 전이 올해일 경우
                            if (month == fourMonthMonth && month == fiveMonthMonth) { // 5개월 전이 같은 달일 경우
                                if (day in fiveMonthDay until fourMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < fourMonthMonth && month == fiveMonthMonth) { // 5개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == fourMonthMonth && month > fiveMonthMonth) { // 5개월 전이 이번달일 경우
                                if (day < fiveMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < fourMonthYear && year == fiveMonthYear) { // 5개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == fiveMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fiveMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == fourMonthYear && year > fiveMonthYear) { // 5개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == fourMonthMonth) {
                                if (day < fourMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        }

                        if (year == fiveMonthYear && year == sixMonthYear) { // 6개월 전이 올해일 경우
                            if (month == fiveMonthMonth && month == sixMonthMonth) { // 6개월 전이 같은 달일 경우
                                if (day in sixMonthDay until fiveMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < fiveMonthMonth && month == sixMonthMonth) { // 6개월 전이 전달일 경우
                                if (day >= sixMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == fiveMonthMonth && month > sixMonthMonth) { // 6개월 전이 이번달일 경우
                                if (day < fiveMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < fiveMonthYear && year == sixMonthYear) { // 6개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == sixMonthMonth) { // 6개월 전이 전달일 경우
                                if (day >= sixMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == fiveMonthYear && year > sixMonthYear) { // 6개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == fiveMonthMonth) {
                                if (day < fiveMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        }

                        if (year == sixMonthYear && year == sevenMonthYear) { // 7개월 전이 올해일 경우
                            if (month == sixMonthMonth && month == sevenMonthMonth) { // 7개월 전이 같은 달일 경우
                                if (day in sevenMonthDay until sixMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < sixMonthMonth && month == sevenMonthMonth) { // 7개월 전이 전달일 경우
                                if (day >= sevenMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == sixMonthMonth && month > sevenMonthMonth) { // 7개월 전이 이번달일 경우
                                if (day < sixMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < sixMonthYear && year == sevenMonthYear) { // 7개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == sevenMonthMonth) { // 7개월 전이 전달일 경우
                                if (day >= sevenMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == sixMonthYear && year > sevenMonthYear) { // 7개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == sixMonthMonth) {
                                if (day < sixMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        }

                        if (year == sevenMonthYear && year == eightMonthYear) { // 8개월 전이 올해일 경우
                            if (month == sevenMonthMonth && month == eightMonthMonth) { // 8개월 전이 같은 달일 경우
                                if (day in eightMonthDay until sevenMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < sevenMonthMonth && month == eightMonthMonth) { // 8개월 전이 전달일 경우
                                if (day >= eightMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == sevenMonthMonth && month > eightMonthMonth) { // 8개월 전이 이번달일 경우
                                if (day < sevenMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < sevenMonthYear && year == eightMonthYear) { // 8개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == eightMonthMonth) { // 8개월 전이 전달일 경우
                                if (day >= eightMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == sevenMonthYear && year > eightMonthYear) { // 8개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == sevenMonthMonth) {
                                if (day < sevenMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        }

                        if (year == eightMonthYear && year == nineMonthYear) { // 9개월 전이 올해일 경우
                            if (month == eightMonthMonth && month == nineMonthMonth) { // 9개월 전이 같은 달일 경우
                                if (day in nineMonthDay until eightMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < eightMonthMonth && month == nineMonthMonth) { // 9개월 전이 전달일 경우
                                if (day >= nineMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == eightMonthMonth && month > nineMonthMonth) { // 9개월 전이 이번달일 경우
                                if (day < eightMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < eightMonthYear && year == nineMonthYear) { // 9개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == nineMonthMonth) { // 9개월 전이 전달일 경우
                                if (day >= nineMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == eightMonthYear && year > nineMonthYear) { // 9개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == eightMonthMonth) {
                                if (day < eightMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        }

                        if (year == nineMonthYear && year == tenMonthYear) { // 10개월 전이 올해일 경우
                            if (month == nineMonthMonth && month == tenMonthMonth) { // 10개월 전이 같은 달일 경우
                                if (day in tenMonthDay until nineMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < nineMonthMonth && month == tenMonthMonth) { // 10개월 전이 전달일 경우
                                if (day >= tenMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == nineMonthMonth && month > tenMonthMonth) { // 10개월 전이 이번달일 경우
                                if (day < nineMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < nineMonthYear && year == tenMonthYear) { // 10개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == tenMonthMonth) { // 10개월 전이 전달일 경우
                                if (day >= tenMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == nineMonthYear && year > tenMonthYear) { // 10개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == nineMonthMonth) {
                                if (day < nineMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        }

                        if (year == tenMonthYear && year == elevenMonthYear) { // 11개월 전이 올해일 경우
                            if (month == tenMonthMonth && month == elevenMonthMonth) { // 11개월 전이 같은 달일 경우
                                if (day in elevenMonthDay until tenMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < tenMonthMonth && month == elevenMonthMonth) { // 11개월 전이 전달일 경우
                                if (day >= elevenMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == tenMonthMonth && month > elevenMonthMonth) { // 11개월 전이 이번달일 경우
                                if (day < tenMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < tenMonthYear && year == elevenMonthYear) { // 11개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == elevenMonthMonth) { // 11개월 전이 전달일 경우
                                if (day >= elevenMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == tenMonthYear && year > elevenMonthYear) { // 11개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == tenMonthMonth) {
                                if (day < tenMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        }

                        if (year == elevenMonthYear && year == yearYear) { // 1년 전이 올해일 경우
                            if (month == elevenMonthMonth && month == yearMonth) { // 1년 전이 같은 달일 경우
                                if (day in yearDay until elevenMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month < elevenMonthMonth && month == yearMonth) { // 1년 전이 전달일 경우
                                if (day >= yearDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            } else if (month == elevenMonthMonth && month > yearMonth) { // 1년 전이 이번달일 경우
                                if (day < elevenMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year < elevenMonthYear && year == yearYear) { // 1년 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == yearMonth) { // 1년 전이 전달일 경우
                                if (day >= yearDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        } else if (year == elevenMonthYear && year > yearYear) { // 1년 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == elevenMonthMonth) {
                                if (day < elevenMonthDay) {
                                    yearPieMap[item!!.snackType] = yearPieMap[item!!.snackType]!! + 1.toFloat()
                                }
                            }
                        }

                    }
                } catch (e: Exception) {
                    Log.d(TAG, "간식 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.snackRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun pieYearChartGraph(v : View, pieChart : PieChart, valList : MutableMap<String, Float>) {

        pieChart.extraBottomOffset = 15f // 간격
        pieChart.description.isEnabled = false // chart 밑에 description 표시 유무
        pieChart.setTouchEnabled(false)

        pieChart.run {
            description.isEnabled = false

            setTouchEnabled(false)
            legend.isEnabled = true

            setEntryLabelTextSize(10f)
            setEntryLabelColor(Color.parseColor("#000000"))
        }

        var sum = 0.toFloat()
        for((key, value) in valList.entries) {
            if(value >= 1) {
                sum += value
            }
        }

        var entries : ArrayList<PieEntry> = ArrayList()
        for((key, value) in valList.entries) {
            if(value >= 1) {
                entries.add(PieEntry(value / sum * 100, key))
            }
        }

        val colors: ArrayList<Int> = ArrayList()
        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)

        var depenses = PieDataSet(entries, "")
        with(depenses) {
            sliceSpace = 3f
            selectionShift = 5f
            valueTextSize = 5f
            setColors(colors)
        }
        depenses.valueFormatter = CustomFormatter()
        // depenses.color = Color.parseColor("#F9AC3A")

        val data = PieData(depenses)
        with(data) {
            setValueTextSize(10f)
            setValueTextColor(Color.parseColor("#000000"))
        }

        pieChart.run {
            this.data = data
            description.isEnabled = false
            animate()
            invalidate()
            centerText = "간식"
        }
    }

    private fun yearSnack() {
        yearLabelList.clear()
        yearDataList.clear()

        for((key, value) in yearMap.entries) {
            yearLabelList.add(key)
        }

        for(i in 0 until yearLabelList.size)
            yearDataList.add(yearLabelList[i])

        yearSnackStatisticsRVAdapter.notifyDataSetChanged()
    }

    class CustomFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val waterWeight = value.toString().split(".")
            return waterWeight[0] + "%"
        }
    }
}