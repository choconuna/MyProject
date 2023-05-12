package org.techtown.myproject.statistics

import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogWaterModel
import org.techtown.myproject.utils.FBRef
import java.text.SimpleDateFormat
import java.util.*

class WaterStatisticsFragment : Fragment() {

    private val TAG = WaterStatisticsFragment::class.java.simpleName

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

    private lateinit var oneDayChart : BarChart
    private lateinit var weekChart : BarChart
    private lateinit var oneMonthChart : BarChart
    private lateinit var threeMonthChart : BarChart
    private lateinit var sixMonthChart : BarChart
    private lateinit var yearChart : BarChart

    private var labelList : ArrayList<String> = ArrayList()
    private var valueList : ArrayList<Float> = ArrayList()

    private var weekLabelList : ArrayList<String> = ArrayList()
    private var weekValueList : ArrayList<Float> = ArrayList()
    private var weekLabelMap : MutableMap<Int, String> = mutableMapOf()
    private var weekMap : MutableMap<Int, Float> = mutableMapOf()

    private var oneMonthLabelList : ArrayList<String> = ArrayList()
    private var oneMonthValueList : ArrayList<Float> = ArrayList()
    private var oneMonthMap : MutableMap<Int, Float> = mutableMapOf()

    private var threeMonthLabelList : ArrayList<String> = ArrayList()
    private var threeMonthValueList : ArrayList<Float> = ArrayList()
    private var threeMonthMap : MutableMap<Int, Float> = mutableMapOf()

    private var sixMonthLabelList : ArrayList<String> = ArrayList()
    private var sixMonthValueList : ArrayList<Float> = ArrayList()
    private var sixMonthMap : MutableMap<Int, Float> = mutableMapOf()

    private var yearLabelList : ArrayList<String> = ArrayList()
    private var yearValueList : ArrayList<Float> = ArrayList()
    private var yearMap : MutableMap<Int, Float> = mutableMapOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v : View? = inflater.inflate(R.layout.fragment_water_statistics, container, false)

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
        oneDayChart = v.findViewById(R.id.chart_one_day)
        weekChart = v.findViewById(R.id.chart_week)
        oneMonthChart = v.findViewById(R.id.chart_one_month)
        threeMonthChart = v.findViewById(R.id.chart_three_month)
        sixMonthChart = v.findViewById(R.id.chart_six_month)
        yearChart = v.findViewById(R.id.chart_year)

        spinner = v.findViewById(R.id.spinner)

        setShowChart(v, "오늘")
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
                oneDayChart.visibility = VISIBLE
                weekChart.visibility = GONE
                oneMonthChart.visibility = GONE
                threeMonthChart.visibility = GONE
                sixMonthChart.visibility = GONE
                yearChart.visibility = GONE

                Log.d("oneDayMap", "$valueList $labelList")
                setTodayChart(v)
            }
            "1주일" -> {
                oneDayChart.visibility = GONE
                weekChart.visibility = VISIBLE
                oneMonthChart.visibility = GONE
                threeMonthChart.visibility = GONE
                sixMonthChart.visibility = GONE
                yearChart.visibility = GONE

                weekLabelList.clear()
                weekValueList.clear()

                setWeekChartReady(v)

                Log.d("weekDay", "$weekMap $weekLabelMap")
            }
            "1개월" -> {
                oneDayChart.visibility = GONE
                weekChart.visibility = GONE
                oneMonthChart.visibility = VISIBLE
                threeMonthChart.visibility = GONE
                sixMonthChart.visibility = GONE
                yearChart.visibility = GONE

                oneMonthLabelList.clear()

                setOneMonthChartReady(v)

                Log.d("oneMonthDay", "$oneMonthMap")
            }
            "3개월" -> {
                oneDayChart.visibility = GONE
                weekChart.visibility = GONE
                oneMonthChart.visibility = GONE
                threeMonthChart.visibility = VISIBLE
                sixMonthChart.visibility = GONE
                yearChart.visibility = GONE

                threeMonthLabelList.clear()
                threeMonthValueList.clear()

                setThreeMonthChartReady(v)

                Log.d("threeMonthDay", "$threeMonthMap")
            }
            "6개월" -> {
                oneDayChart.visibility = GONE
                weekChart.visibility = GONE
                oneMonthChart.visibility = GONE
                threeMonthChart.visibility = GONE
                sixMonthChart.visibility = VISIBLE
                yearChart.visibility = GONE

                sixMonthLabelList.clear()
                sixMonthValueList.clear()

                setSixMonthChartReady(v)

                Log.d("sixMonthDay", "$sixMonthMap")
            }
            "1년" -> {
                oneDayChart.visibility = GONE
                weekChart.visibility = GONE
                oneMonthChart.visibility = GONE
                threeMonthChart.visibility = GONE
                sixMonthChart.visibility = GONE
                yearChart.visibility = VISIBLE

                setYearChartReady(v)

                Log.d("yearMonthDay", "$yearMap")
            }
        }
    }

    private fun setTodayChart(v : View) {
        val nowSp = nowDate.split(".") // 오늘 날짜
        val nowYear = nowSp[0].toInt()
        val nowMonth = nowSp[1].toInt()
        val nowDay = nowSp[2].toInt()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 물 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    labelList.clear()
                    valueList.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogWaterModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if(year == nowYear && month == nowMonth && day == nowDay) {
                            labelList.add(item!!.timeSlot)
                            valueList.add(item!!.waterWeight.toFloat())
                        }
                    }

                    barTodayChartGraph(v, oneDayChart, valueList, labelList)

                } catch (e: Exception) {
                    Log.d(TAG, "물 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.waterRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun barTodayChartGraph(v : View, barChart : BarChart, valList : ArrayList<Float>, labelList : ArrayList<String>) {

        barChart.extraBottomOffset = 15f // 간격
        barChart.description.isEnabled = false // chart 밑에 description 표시 유무
        barChart.setTouchEnabled(false)
        barChart.setDrawBarShadow(false)
        barChart.setDrawGridBackground(false)

        barChart.run {
            description.isEnabled = false
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawGridBackground(false)

            axisLeft.run {
                axisMinimum = 0f
                granularity = 50f
                setDrawLabels(true)
                setDrawGridLines(true)
                setDrawAxisLine(false)
                axisLineColor = ContextCompat.getColor(v.context, R.color.mainColor)
                textSize = 14f
            }

            xAxis.run {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawAxisLine(true)
                setDrawGridLines(false)
                textSize = 14f
                valueFormatter = IndexAxisValueFormatter(labelList)

            }

            axisRight.isEnabled = false
            setTouchEnabled(false)
            animateY(100)
            legend.isEnabled = false
        }

        var entries : ArrayList<BarEntry> = ArrayList()
        for (i in 0 until valList.size) {
            entries.add(BarEntry(i.toFloat(), valList[i]))
        }

        var depenses = BarDataSet(entries, "음수량")
        depenses.axisDependency = YAxis.AxisDependency.LEFT
        depenses.color = Color.parseColor("#87CEEB")
        depenses.valueFormatter = CustomFormatter()
        depenses.valueTextSize = 10f

        val data = BarData(depenses)
        data.barWidth = 0.8f

        barChart.run {
            this.data = data
            setFitBars(true)
            invalidate()
        }
    }

    private fun setWeekChartReady(v : View) {
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
                try { // 물 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogWaterModel::class.java)
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
                                    weekLabelMap[dayNum.toInt()] = date
                                    weekMap[dayNum.toInt()] = 0.toFloat()
                                }
                            } else if(month < nowMonth && month == weekMonth) { // 일주일 전이 전달일 경우
                                if(day >= weekDay) {
                                    weekLabelMap[dayNum.toInt()] = date
                                    weekMap[dayNum.toInt()] = 0.toFloat()
                                }
                            } else if(month == nowMonth && month > weekMonth) { // 일주일 전이 이번달일 경우
                                if(day <= nowDay) {
                                    weekLabelMap[dayNum.toInt()] = date
                                    weekMap[dayNum.toInt()] = 0.toFloat()
                                }
                            }
                        } else if(year < nowYear && year == weekYear) { // 일주일 전이 전년도일 경우
                            if(weekMonth == month && day >= weekDay) {
                                weekLabelMap[dayNum.toInt()] = date
                                weekMap[dayNum.toInt()] = 0.toFloat()
                            }
                        }
                    }

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
                            try { // 물 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                                for (dataModel in dataSnapshot.children) {
                                    val item = dataModel.getValue(DogWaterModel::class.java)
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
                                                weekMap[dayNum.toInt()] = weekMap[dayNum.toInt()]!!.toFloat() + item!!.waterWeight.toFloat()
                                            }
                                        } else if(month < nowMonth && month == weekMonth) { // 일주일 전이 전달일 경우
                                            if(day >= weekDay) {
                                                weekMap[dayNum.toInt()] = weekMap[dayNum.toInt()]!!.toFloat() + item!!.waterWeight.toFloat()
                                            }
                                        } else if(month == nowMonth && month > weekMonth) { // 일주일 전이 이번달일 경우
                                            if(day <= nowDay) {
                                                weekMap[dayNum.toInt()] = weekMap[dayNum.toInt()]!!.toFloat() + item!!.waterWeight.toFloat()
                                            }
                                        }
                                    } else if(year < nowYear && year == weekYear) { // 일주일 전이 전년도일 경우
                                        if(weekMonth == month && day >= weekDay) {
                                            weekMap[dayNum.toInt()] = weekMap[dayNum.toInt()]!!.toFloat() + item!!.waterWeight.toFloat()
                                        }
                                    }
                                }

                                val sortedWeekLabelMap = sortMapByKey1(weekLabelMap)
                                // key(String)에 따른 정렬
                                for((key, value) in sortedWeekLabelMap.entries) {
                                    weekLabelList.add(value)
                                }

                                val sortedWeekMap = sortMapByKey2(weekMap)
                                // key(String)에 따른 정렬
                                for((key, value) in sortedWeekMap.entries) {
                                    weekValueList.add(value)
                                }

                                Log.d("weekDay", "$weekLabelList $weekValueList")

                                barWeekChartGraph(v, weekChart, weekValueList, weekLabelList)

                            } catch (e: Exception) {
                                Log.d(TAG, "물 기록 삭제 완료")
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Getting Post failed, log a message
                            Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                        }
                    }
                    FBRef.waterRef.child(myUid).child(dogId).addValueEventListener(postListener)

                } catch (e: Exception) {
                    Log.d(TAG, "물 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.waterRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun barWeekChartGraph(v : View, barChart : BarChart, valList : ArrayList<Float>, labelList : ArrayList<String>) {

        barChart.extraBottomOffset = 15f // 간격
        barChart.description.isEnabled = false // chart 밑에 description 표시 유무
        barChart.setTouchEnabled(false)
        barChart.setDrawBarShadow(false)
        barChart.setDrawGridBackground(false)

        var label : ArrayList<String> = ArrayList()
        for(i in 0 until labelList.size) {
            var labelSp = labelList[i].split(".")
            label.add(labelSp[1]+"."+labelSp[2]+".")
        }

        barChart.run {
            description.isEnabled = false
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawGridBackground(false)

            axisLeft.run {
                axisMinimum = 0f
                granularity = 50f
                setDrawLabels(true)
                setDrawGridLines(true)
                setDrawAxisLine(false)
                textSize = 10f
            }

            xAxis.run {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawAxisLine(true)
                setDrawGridLines(false)
                textSize = 10f
                valueFormatter = IndexAxisValueFormatter(label)
            }

            axisRight.isEnabled = false
            setTouchEnabled(false)
            animateY(100)
            legend.isEnabled = false
        }

        var entries : ArrayList<BarEntry> = ArrayList()
        for (i in 0 until valList.size) {
            entries.add(BarEntry(i.toFloat(), valList[i]))
        }

        var depenses = BarDataSet(entries, "음수량 (ml)")
        depenses.axisDependency = YAxis.AxisDependency.LEFT
        depenses.color = Color.parseColor("#87CEEB")
        depenses.valueFormatter = CustomFormatter()
        depenses.valueTextSize = 10f

        val data = BarData(depenses)
        data.barWidth = 0.5f

        barChart.run {
            this.data = data
            setFitBars(true)
            invalidate()
        }
    }

    private fun setOneMonthChartReady(v : View) {
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

        var yearCount : MutableMap<Int, Int> = mutableMapOf()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 물 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    oneMonthValueList.clear()

                    oneMonthMap[1] = 0.toFloat()
                    oneMonthMap[2] = 0.toFloat()
                    oneMonthMap[3] = 0.toFloat()
                    oneMonthMap[4] = 0.toFloat()

                    yearCount[1] = 0
                    yearCount[2] = 0
                    yearCount[3] = 0
                    yearCount[4] = 0

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogWaterModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if(year == nowYear && year == weekYear) { // 일주일 전이 같은 연도일 경우
                            if(month == nowMonth && month == weekMonth) { // 일주일 전이 같은 달일 경우
                                if(day in weekDay..nowDay) {
                                    oneMonthMap[4] = oneMonthMap[4]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[4] = yearCount[4]!! + 1
                                }
                            } else if(month < nowMonth && month == weekMonth) { // 일주일 전이 이전 달일 경우
                                if(day >= weekDay) {
                                    oneMonthMap[4] = oneMonthMap[4]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[4] = yearCount[4]!! + 1
                                }
                            } else if(month == nowMonth && month > weekMonth) { // 일주일 전이 이번 달일 경우
                                if(day <= nowDay) {
                                    oneMonthMap[4] = oneMonthMap[4]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[4] = yearCount[4]!! + 1
                                }
                            }
                        } else if(year < nowYear && year == weekYear) { // 일주일 전이 전년도일 경우
                            if(weekMonth == month && day >= weekDay) {
                                oneMonthMap[4] = oneMonthMap[4]!! + item!!.waterWeight.toFloat()

                                if(item!!.waterWeight.toInt() > 0)
                                    yearCount[4] = yearCount[4]!! + 1
                            }
                        }

                        if(year == weekYear && year == twoWeekYear) { // 2주일 전이 같은 연도일 경우
                            if(month == weekMonth && month == twoWeekMonth) { // 2주일 전이 같은 달일 경우
                                if(day in twoWeekDay until weekDay) {
                                    oneMonthMap[3] = oneMonthMap[3]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[3] = yearCount[3]!! + 1
                                }
                            } else if(month < weekMonth && month == twoWeekMonth) { // 2주일 전이 이전 달일 경우
                                if(day >= twoWeekDay) {
                                    oneMonthMap[3] = oneMonthMap[3]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[3] = yearCount[3]!! + 1
                                }
                            } else if(month == weekMonth && month > twoWeekMonth) { // 2주일 전이 이번 달일 경우
                                if(day < weekDay) {
                                    oneMonthMap[3] = oneMonthMap[3]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[3] = yearCount[3]!! + 1
                                }
                            }
                        } else if(year < weekYear && year == twoWeekYear) { // 2주일 전이 전년도일 경우
                            if(twoWeekMonth == month && day >= twoWeekDay) {
                                oneMonthMap[3] = oneMonthMap[3]!! + item!!.waterWeight.toFloat()

                                if(item!!.waterWeight.toInt() > 0)
                                    yearCount[3] = yearCount[3]!! + 1
                            }
                        }

                        if(year == twoWeekYear && year == threeWeekYear) { // 3주일 전이 같은 연도일 경우
                            if(month == twoWeekMonth && month == threeWeekMonth) { // 3주일 전이 같은 달일 경우
                                if(day in threeWeekDay until twoWeekDay) {
                                    oneMonthMap[2] = oneMonthMap[2]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[2] = yearCount[2]!! + 1
                                }
                            } else if(month < twoWeekMonth && month == threeWeekMonth) { // 3주일 전이 이전 달일 경우
                                if(day >= threeWeekDay) {
                                    oneMonthMap[2] = oneMonthMap[2]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[2] = yearCount[2]!! + 1
                                }
                            } else if(month == twoWeekMonth && month > threeWeekMonth) { // 3주일 전이 이번 달일 경우
                                if(day < twoWeekDay) {
                                    oneMonthMap[2] = oneMonthMap[2]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[2] = yearCount[2]!! + 1
                                }
                            }
                        } else if(year < twoWeekYear && year == threeWeekYear) { // 3주일 전이 전년도일 경우
                            if(threeWeekMonth == month && day >= threeWeekDay) {
                                oneMonthMap[2] = oneMonthMap[2]!! + item!!.waterWeight.toFloat()

                                if(item!!.waterWeight.toInt() > 0)
                                    yearCount[2] = yearCount[2]!! + 1
                            }
                        }

                        if(year == threeWeekYear && year == oneMonthYear) { // 한달 전이 같은 연도일 경우
                            if(month == threeWeekMonth && month == oneMonthMonth) { // 한달 전이 같은 달일 경우
                                if(day in oneMonthDay until threeWeekDay) {
                                    oneMonthMap[1] = oneMonthMap[1]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[1] = yearCount[1]!! + 1
                                }
                            } else if(month < threeWeekMonth && month == oneMonthMonth) { // 한달 전이 이전 달일 경우
                                if(day >= oneMonthDay) {
                                    oneMonthMap[1] = oneMonthMap[1]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[1] = yearCount[1]!! + 1
                                }
                            } else if(month == threeWeekMonth && month > oneMonthMonth) { // 한달 전이 이번 달일 경우
                                if(day < threeWeekDay) {
                                    oneMonthMap[1] = oneMonthMap[1]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[1] = yearCount[1]!! + 1
                                }
                            }
                        } else if(year < threeWeekYear && year == oneMonthYear) { // 한달 전이 전년도일 경우
                            if(oneMonthMonth == month && day >= oneMonthDay) {
                                oneMonthMap[1] = oneMonthMap[1]!! + item!!.waterWeight.toFloat()

                                if(item!!.waterWeight.toInt() > 0)
                                    yearCount[1] = yearCount[1]!! + 1
                            }
                        }
                    }

                    oneMonthLabelList.add("1주차")
                    oneMonthLabelList.add("2주차")
                    oneMonthLabelList.add("3주차")
                    oneMonthLabelList.add("4주차")

                    val sortedMonthMap = sortMapByKey2(oneMonthMap)
                    val sortedMonthCountMap = sortMapByKey3(yearCount)
                    var count :  ArrayList<Int> = ArrayList()
                    for((key, value) in sortedMonthCountMap)
                        count.add(value)

                    var i = 0
                    for((key, value) in sortedMonthMap.entries) {
                        if(count[i] == 0)
                            oneMonthValueList.add(value)
                        else
                            oneMonthValueList.add(value / count[i])
                        i += 1
                    }

                    Log.d("oneMonthDay", "$oneMonthLabelList $oneMonthValueList")

                    barOneMonthChartGraph(v, oneMonthChart, oneMonthValueList, oneMonthLabelList)

                } catch (e: Exception) {
                    Log.d(TAG, "물 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.waterRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun barOneMonthChartGraph(v : View, barChart : BarChart, valList : ArrayList<Float>, labelList : ArrayList<String>) {

        barChart.extraBottomOffset = 15f // 간격
        barChart.description.isEnabled = false // chart 밑에 description 표시 유무
        barChart.setTouchEnabled(false)
        barChart.setDrawBarShadow(false)
        barChart.setDrawGridBackground(false)

        barChart.run {
            description.isEnabled = false
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawGridBackground(false)

            axisLeft.run {
                axisMinimum = 0f
                granularity = 10f
                setDrawLabels(true)
                setDrawGridLines(true)
                setDrawAxisLine(false)
                textSize = 10f
            }

            xAxis.run {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawAxisLine(true)
                setDrawGridLines(false)
                textSize = 10f
                valueFormatter = IndexAxisValueFormatter(labelList)
            }

            axisRight.isEnabled = false
            setTouchEnabled(false)
            animateY(100)
            legend.isEnabled = false
        }

        var entries : ArrayList<BarEntry> = ArrayList()
        for (i in 0 until valList.size) {
            entries.add(BarEntry(i.toFloat(), valList[i]))
        }

        var depenses = BarDataSet(entries, "음수량 (ml)")
        depenses.axisDependency = YAxis.AxisDependency.LEFT
        depenses.color = Color.parseColor("#87CEEB")
        depenses.valueFormatter = CustomFormatter()
        depenses.valueTextSize = 10f

        val data = BarData(depenses)
        data.barWidth = 0.8f

        barChart.run {
            notifyDataSetChanged()
            this.data = data
            setFitBars(true)
            invalidate()
        }
    }

    private fun setThreeMonthChartReady(v : View) {
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

        var yearCount : MutableMap<Int, Int> = mutableMapOf()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 물 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    threeMonthValueList.clear()

                    threeMonthMap[1] = 0.toFloat()
                    threeMonthMap[2] = 0.toFloat()
                    threeMonthMap[3] = 0.toFloat()

                    yearCount[1] = 0
                    yearCount[2] = 0
                    yearCount[3] = 0

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogWaterModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if (year == nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도일 경우
                            if (month == nowMonth && month == oneMonthMonth) { // 1개월 전이 같은 달일 경우
                                if (day in oneMonthDay..nowDay) {
                                    threeMonthMap[3] =
                                        threeMonthMap[3]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[3] = yearCount[3]!! + 1
                                }
                            } else if (month < nowMonth && month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    threeMonthMap[3] =
                                        threeMonthMap[3]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[3] = yearCount[3]!! + 1
                                }
                            } else if (month == nowMonth && month > oneMonthMonth) { // 1개월 전이 이번 달일 경우
                                if (day <= nowDay) {
                                    threeMonthMap[3] =
                                        threeMonthMap[3]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[3] = yearCount[3]!! + 1
                                }
                            }
                        } else if (year < nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 년도일 경우
                            if (month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    threeMonthMap[3] =
                                        threeMonthMap[3]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[3] = yearCount[3]!! + 1
                                }
                            }
                        } else if (year == nowYear && year > oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == nowMonth) { // 1개월 전이 이번달일 경우
                                if (day <= nowDay) {
                                    threeMonthMap[3] =
                                        threeMonthMap[3]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[3] = yearCount[3]!! + 1
                                }
                            }
                        }

                        if (year == oneMonthYear && year == twoMonthYear) { // 2개월 전에 오늘일 경우
                            if (month == oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 같은 달일 경우
                                if (day in twoMonthDay until oneMonthDay) {
                                    threeMonthMap[2] =
                                        threeMonthMap[2]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[2] = yearCount[2]!! + 1
                                }
                            } else if (month < oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    threeMonthMap[2] =
                                        threeMonthMap[2]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[2] = yearCount[2]!! + 1
                                }
                            } else if (month == oneMonthMonth && month > twoMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    threeMonthMap[2] =
                                        threeMonthMap[2]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[2] = yearCount[2]!! + 1
                                }
                            }
                        } else if (year < oneMonthYear && year == twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    threeMonthMap[2] =
                                        threeMonthMap[2]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[2] = yearCount[2]!! + 1
                                }
                            }
                        } else if (year == oneMonthYear && year > twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == oneMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    threeMonthMap[2] =
                                        threeMonthMap[2]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[2] = yearCount[2]!! + 1
                                }
                            }
                        }

                        if (year == twoMonthYear && year == threeMonthYear) { // 3개월 전이 올해일 경우
                            if (month == twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 같은 달일 경우
                                if (day in threeMonthDay until twoMonthDay) {
                                    threeMonthMap[1] =
                                        threeMonthMap[1]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[1] = yearCount[1]!! + 1
                                }
                            } else if (month < twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    threeMonthMap[1] =
                                        threeMonthMap[1]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[1] = yearCount[1]!! + 1
                                }
                            } else if (month == twoMonthMonth && month > threeMonthMonth) { // 3개월 전이 이번달일 경우
                                if (day < twoMonthDay) {
                                    threeMonthMap[1] =
                                        threeMonthMap[1]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[1] = yearCount[1]!! + 1
                                }
                            }
                        } else if (year < twoMonthYear && year == threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    threeMonthMap[1] =
                                        threeMonthMap[1]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[1] = yearCount[1]!! + 1
                                }
                            }
                        } else if (year == twoMonthYear && year > threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == twoMonthMonth) {
                                if (day < twoMonthDay) {
                                    threeMonthMap[1] =
                                        threeMonthMap[1]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[1] = yearCount[1]!! + 1
                                }
                            }
                        }
                    }

                    threeMonthLabelList.add("1개월")
                    threeMonthLabelList.add("2개월")
                    threeMonthLabelList.add("3개월")

                    val sortedThreeMonthMap = sortMapByKey2(threeMonthMap)
                    val sortedThreeMonthCountMap = sortMapByKey3(yearCount)
                    var count :  ArrayList<Int> = ArrayList()
                    for((key, value) in sortedThreeMonthCountMap)
                        count.add(value)

                    var i = 0
                    for((key, value) in sortedThreeMonthMap.entries) {
                        if(count[i] == 0)
                            threeMonthValueList.add(value)
                        else
                            threeMonthValueList.add(value / count[i])
                        i += 1
                    }

                    Log.d("threeMonthDay", "$threeMonthLabelList $threeMonthValueList")

                    barThreeMonthChartGraph(v, threeMonthChart, threeMonthValueList, threeMonthLabelList)

                } catch (e: Exception) {
                    Log.d(TAG, "물 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.waterRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun barThreeMonthChartGraph(v : View, barChart : BarChart, valList : ArrayList<Float>, labelList : ArrayList<String>) {

        barChart.extraBottomOffset = 15f // 간격
        barChart.description.isEnabled = false // chart 밑에 description 표시 유무
        barChart.setTouchEnabled(false)
        barChart.setDrawBarShadow(false)
        barChart.setDrawGridBackground(false)

        barChart.run {
            description.isEnabled = false
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawGridBackground(false)

            axisLeft.run {
                axisMinimum = 0f
                granularity = 10f
                setDrawLabels(true)
                setDrawGridLines(true)
                setDrawAxisLine(false)
                textSize = 10f
            }

            xAxis.run {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawAxisLine(true)
                setDrawGridLines(false)
                textSize = 10f
                valueFormatter = IndexAxisValueFormatter(labelList)
            }

            axisRight.isEnabled = false
            setTouchEnabled(false)
            animateY(100)
            legend.isEnabled = false
        }

        var entries : ArrayList<BarEntry> = ArrayList()
        for (i in 0 until valList.size) {
            entries.add(BarEntry(i.toFloat(), valList[i]))
        }

        var depenses = BarDataSet(entries, "음수량 (ml)")
        depenses.axisDependency = YAxis.AxisDependency.LEFT
        depenses.color = Color.parseColor("#87CEEB")
        depenses.valueFormatter = CustomFormatter()
        depenses.valueTextSize = 10f

        val data = BarData(depenses)
        data.barWidth = 0.8f

        barChart.run {
            notifyDataSetChanged()
            this.data = data
            setFitBars(true)
            invalidate()
        }
    }

    private fun setSixMonthChartReady(v : View) {
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

        var yearCount : MutableMap<Int, Int> = mutableMapOf()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 물 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    sixMonthValueList.clear()

                    sixMonthMap[1] = 0.toFloat()
                    sixMonthMap[2] = 0.toFloat()
                    sixMonthMap[3] = 0.toFloat()
                    sixMonthMap[4] = 0.toFloat()
                    sixMonthMap[5] = 0.toFloat()
                    sixMonthMap[6] = 0.toFloat()

                    yearCount[1] = 0
                    yearCount[2] = 0
                    yearCount[3] = 0
                    yearCount[4] = 0
                    yearCount[5] = 0
                    yearCount[6] = 0

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogWaterModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if (year == nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도일 경우
                            if (month == nowMonth && month == oneMonthMonth) { // 1개월 전이 같은 달일 경우
                                if (day in oneMonthDay..nowDay) {
                                    sixMonthMap[6] =
                                        sixMonthMap[6]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[6] = yearCount[6]!! + 1
                                }
                            } else if (month < nowMonth && month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    sixMonthMap[6] =
                                        sixMonthMap[6]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[6] = yearCount[6]!! + 1
                                }
                            } else if (month == nowMonth && month > oneMonthMonth) { // 1개월 전이 이번 달일 경우
                                if (day <= nowDay) {
                                    sixMonthMap[6] =
                                        sixMonthMap[6]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[6] = yearCount[6]!! + 1
                                }
                            }
                        } else if (year < nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 년도일 경우
                            if (month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    sixMonthMap[6] =
                                        sixMonthMap[6]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[6] = yearCount[6]!! + 1
                                }
                            }
                        } else if (year == nowYear && year > oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == nowMonth) { // 1개월 전이 이번달일 경우
                                if (day <= nowDay) {
                                    sixMonthMap[6] =
                                        sixMonthMap[6]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[6] = yearCount[6]!! + 1
                                }
                            }
                        }

                        if (year == oneMonthYear && year == twoMonthYear) { // 2개월 전에 오늘일 경우
                            if (month == oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 같은 달일 경우
                                if (day in twoMonthDay until oneMonthDay) {
                                    sixMonthMap[5] =
                                        sixMonthMap[5]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[5] = yearCount[5]!! + 1
                                }
                            } else if (month < oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    sixMonthMap[5] =
                                        sixMonthMap[5]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[5] = yearCount[5]!! + 1
                                }
                            } else if (month == oneMonthMonth && month > twoMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    sixMonthMap[5] =
                                        sixMonthMap[5]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[5] = yearCount[5]!! + 1
                                }
                            }
                        } else if (year < oneMonthYear && year == twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    sixMonthMap[5] =
                                        sixMonthMap[5]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[5] = yearCount[5]!! + 1
                                }
                            }
                        } else if (year == oneMonthYear && year > twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == oneMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    sixMonthMap[5] =
                                        sixMonthMap[5]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[5] = yearCount[5]!! + 1
                                }
                            }
                        }

                        if (year == twoMonthYear && year == threeMonthYear) { // 3개월 전이 올해일 경우
                            if (month == twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 같은 달일 경우
                                if (day in threeMonthDay until twoMonthDay) {
                                    sixMonthMap[4] =
                                        sixMonthMap[4]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[4] = yearCount[4]!! + 1
                                }
                            } else if (month < twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    sixMonthMap[4] =
                                        sixMonthMap[4]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[4] = yearCount[4]!! + 1
                                }
                            } else if (month == twoMonthMonth && month > threeMonthMonth) { // 3개월 전이 이번달일 경우
                                if (day < twoMonthDay) {
                                    sixMonthMap[4] =
                                        sixMonthMap[4]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[4] = yearCount[4]!! + 1
                                }
                            }
                        } else if (year < twoMonthYear && year == threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    sixMonthMap[4] =
                                        sixMonthMap[4]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[4] = yearCount[4]!! + 1
                                }
                            }
                        } else if (year == twoMonthYear && year > threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == twoMonthMonth) {
                                if (day < twoMonthDay) {
                                    sixMonthMap[4] =
                                        sixMonthMap[4]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[4] = yearCount[4]!! + 1
                                }
                            }
                        }

                        if (year == threeMonthYear && year == fourMonthYear) { // 4개월 전이 올해일 경우
                            if (month == threeMonthMonth && month == fourMonthMonth) { // 4개월 전이 같은 달일 경우
                                if (day in fourMonthDay until threeMonthDay) {
                                    sixMonthMap[3] =
                                        sixMonthMap[3]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[3] = yearCount[3]!! + 1
                                }
                            } else if (month < threeMonthMonth && month == fourMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    sixMonthMap[3] =
                                        sixMonthMap[3]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[3] = yearCount[3]!! + 1
                                }
                            } else if (month == threeMonthMonth && month > fourMonthMonth) { // 4개월 전이 이번달일 경우
                                if (day < threeMonthDay) {
                                    sixMonthMap[3] =
                                        sixMonthMap[3]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[3] = yearCount[3]!! + 1
                                }
                            }
                        } else if (year < threeMonthYear && year == fourMonthYear) { // 4개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == fourMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    sixMonthMap[3] =
                                        sixMonthMap[3]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[3] = yearCount[3]!! + 1
                                }
                            }
                        } else if (year == threeMonthYear && year > fourMonthYear) { // 4개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == threeMonthMonth) {
                                if (day < threeMonthDay) {
                                    sixMonthMap[3] =
                                        sixMonthMap[3]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[3] = yearCount[3]!! + 1
                                }
                            }
                        }

                        if (year == fourMonthYear && year == fiveMonthYear) { // 5개월 전이 올해일 경우
                            if (month == fourMonthMonth && month == fiveMonthMonth) { // 5개월 전이 같은 달일 경우
                                if (day in fiveMonthDay until fourMonthDay) {
                                    sixMonthMap[2] =
                                        sixMonthMap[2]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[2] = yearCount[2]!! + 1
                                }
                            } else if (month < fourMonthMonth && month == fiveMonthMonth) { // 5개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    sixMonthMap[2] =
                                        sixMonthMap[2]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[2] = yearCount[2]!! + 1
                                }
                            } else if (month == fourMonthMonth && month > fiveMonthMonth) { // 5개월 전이 이번달일 경우
                                if (day < fiveMonthDay) {
                                    sixMonthMap[2] =
                                        sixMonthMap[2]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[2] = yearCount[2]!! + 1
                                }
                            }
                        } else if (year < fourMonthYear && year == fiveMonthYear) { // 5개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == fiveMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fiveMonthDay) {
                                    sixMonthMap[2] =
                                        sixMonthMap[2]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[2] = yearCount[2]!! + 1
                                }
                            }
                        } else if (year == fourMonthYear && year > fiveMonthYear) { // 5개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == fourMonthMonth) {
                                if (day < fourMonthDay) {
                                    sixMonthMap[2] =
                                        sixMonthMap[2]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[2] = yearCount[2]!! + 1
                                }
                            }
                        }

                        if (year == fiveMonthYear && year == sixMonthYear) { // 6개월 전이 올해일 경우
                            if (month == fiveMonthMonth && month == sixMonthMonth) { // 6개월 전이 같은 달일 경우
                                if (day in sixMonthDay until fiveMonthDay) {
                                    sixMonthMap[1] =
                                        sixMonthMap[1]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[1] = yearCount[1]!! + 1
                                }
                            } else if (month < fiveMonthMonth && month == sixMonthMonth) { // 6개월 전이 전달일 경우
                                if (day >= sixMonthDay) {
                                    sixMonthMap[1] =
                                        sixMonthMap[1]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[1] = yearCount[1]!! + 1
                                }
                            } else if (month == fiveMonthMonth && month > sixMonthMonth) { // 6개월 전이 이번달일 경우
                                if (day < fiveMonthDay) {
                                    sixMonthMap[1] =
                                        sixMonthMap[1]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[1] = yearCount[1]!! + 1
                                }
                            }
                        } else if (year < fiveMonthYear && year == sixMonthYear) { // 6개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == sixMonthMonth) { // 6개월 전이 전달일 경우
                                if (day >= sixMonthDay) {
                                    sixMonthMap[1] =
                                        sixMonthMap[1]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[1] = yearCount[1]!! + 1
                                }
                            }
                        } else if (year == fiveMonthYear && year > sixMonthYear) { // 6개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(day < fiveMonthDay) {
                                sixMonthMap[1] =
                                    sixMonthMap[1]!! + item!!.waterWeight.toFloat()

                                if(item!!.waterWeight.toInt() > 0)
                                    yearCount[1] = yearCount[1]!! + 1
                            }
                        }

                    }

                    sixMonthLabelList.add("1개월")
                    sixMonthLabelList.add("2개월")
                    sixMonthLabelList.add("3개월")
                    sixMonthLabelList.add("4개월")
                    sixMonthLabelList.add("5개월")
                    sixMonthLabelList.add("6개월")

                    val sortedSixMonthMap = sortMapByKey2(sixMonthMap)
                    val sortedSixMonthCountMap = sortMapByKey3(yearCount)
                    var count :  ArrayList<Int> = ArrayList()
                    for((key, value) in sortedSixMonthCountMap)
                        count.add(value)

                    // key(String)에 따른 정렬
                    var i = 0
                    for((key, value) in sortedSixMonthMap.entries) {
                        if(count[i] == 0)
                            sixMonthValueList.add(value)
                        else
                            sixMonthValueList.add(value / count[i])
                        i += 1
                    }

                    Log.d("sixMonthDay", "$sixMonthLabelList $sixMonthValueList")

                    barSixMonthChartGraph(v, sixMonthChart, sixMonthValueList, sixMonthLabelList)

                } catch (e: Exception) {
                    Log.d(TAG, "물 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.waterRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun barSixMonthChartGraph(v : View, barChart : BarChart, valList : ArrayList<Float>, labelList : ArrayList<String>) {

        barChart.extraBottomOffset = 15f // 간격
        barChart.description.isEnabled = false // chart 밑에 description 표시 유무
        barChart.setTouchEnabled(false)
        barChart.setDrawBarShadow(false)
        barChart.setDrawGridBackground(false)

        barChart.run {
            description.isEnabled = false
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawGridBackground(false)

            axisLeft.run {
                axisMinimum = 0f
                granularity = 10f
                setDrawLabels(true)
                setDrawGridLines(true)
                setDrawAxisLine(false)
                textSize = 10f
            }

            xAxis.run {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawAxisLine(true)
                setDrawGridLines(false)
                textSize = 10f
                valueFormatter = IndexAxisValueFormatter(labelList)
            }

            axisRight.isEnabled = false
            setTouchEnabled(false)
            animateY(100)
            legend.isEnabled = false
        }

        var entries : ArrayList<BarEntry> = ArrayList()
        for (i in 0 until valList.size) {
            entries.add(BarEntry(i.toFloat(), valList[i]))
        }

        var depenses = BarDataSet(entries, "음수량 (ml)")
        depenses.axisDependency = YAxis.AxisDependency.LEFT
        depenses.color = Color.parseColor("#87CEEB")
        depenses.valueFormatter = CustomFormatter()
        depenses.valueTextSize = 8f

        val data = BarData(depenses)
        data.barWidth = 0.5f

        barChart.run {
            notifyDataSetChanged()
            this.data = data
            setFitBars(true)
            invalidate()
        }
    }

    private fun setYearChartReady(v : View) {
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

        var yearCount : MutableMap<Int, Int> = mutableMapOf()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 물 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    yearValueList.clear()

                    yearMap[1] = 0.toFloat()
                    yearMap[2] = 0.toFloat()
                    yearMap[3] = 0.toFloat()
                    yearMap[4] = 0.toFloat()
                    yearMap[5] = 0.toFloat()
                    yearMap[6] = 0.toFloat()
                    yearMap[7] = 0.toFloat()
                    yearMap[8] = 0.toFloat()
                    yearMap[9] = 0.toFloat()
                    yearMap[10] = 0.toFloat()
                    yearMap[11] = 0.toFloat()
                    yearMap[12] = 0.toFloat()

                    yearCount[1] = 0
                    yearCount[2] = 0
                    yearCount[3] = 0
                    yearCount[4] = 0
                    yearCount[5] = 0
                    yearCount[6] = 0
                    yearCount[7] = 0
                    yearCount[8] = 0
                    yearCount[9] = 0
                    yearCount[10] = 0
                    yearCount[11] = 0
                    yearCount[12] = 0

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogWaterModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if (year == nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도일 경우
                            if (month == nowMonth && month == oneMonthMonth) { // 1개월 전이 같은 달일 경우
                                if (day in oneMonthDay..nowDay) {
                                    yearMap[12] =
                                        yearMap[12]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[12] = yearCount[12]!! + 1
                                }
                            } else if (month < nowMonth && month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    yearMap[12] =
                                        yearMap[12]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[12] = yearCount[12]!! + 1
                                }
                            } else if (month == nowMonth && month > oneMonthMonth) { // 1개월 전이 이번 달일 경우
                                if (day <= nowDay) {
                                    yearMap[12] =
                                        yearMap[12]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[12] = yearCount[12]!! + 1
                                }
                            }
                        } else if (year < nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 년도일 경우
                            if (month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    yearMap[12] =
                                        yearMap[12]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[12] = yearCount[12]!! + 1
                                }
                            }
                        } else if (year == nowYear && year > oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == nowMonth) { // 1개월 전이 이번달일 경우
                                if (day <= nowDay) {
                                    yearMap[12] =
                                        yearMap[12]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[12] = yearCount[12]!! + 1
                                }
                            }
                        }

                        if (year == oneMonthYear && year == twoMonthYear) { // 2개월 전에 오늘일 경우
                            if (month == oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 같은 달일 경우
                                if (day in twoMonthDay until oneMonthDay) {
                                    yearMap[11] =
                                        yearMap[11]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[11] = yearCount[11]!! + 1
                                }
                            } else if (month < oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    yearMap[11] =
                                        yearMap[11]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[11] = yearCount[11]!! + 1
                                }
                            } else if (month == oneMonthMonth && month > twoMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    yearMap[11] =
                                        yearMap[11]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[11] = yearCount[11]!! + 1
                                }
                            }
                        } else if (year < oneMonthYear && year == twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    yearMap[11] =
                                        yearMap[11]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[11] = yearCount[11]!! + 1
                                }
                            }
                        } else if (year == oneMonthYear && year > twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == oneMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    yearMap[11] =
                                        yearMap[11]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[11] = yearCount[11]!! + 1
                                }
                            }
                        }

                        if (year == twoMonthYear && year == threeMonthYear) { // 3개월 전이 올해일 경우
                            if (month == twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 같은 달일 경우
                                if (day in threeMonthDay until twoMonthDay) {
                                    yearMap[10] =
                                        yearMap[10]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[10] = yearCount[10]!! + 1
                                }
                            } else if (month < twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    yearMap[10] =
                                        yearMap[10]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[10] = yearCount[10]!! + 1
                                }
                            } else if (month == twoMonthMonth && month > threeMonthMonth) { // 3개월 전이 이번달일 경우
                                if (day < twoMonthDay) {
                                    yearMap[10] =
                                        yearMap[10]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[10] = yearCount[10]!! + 1
                                }
                            }
                        } else if (year < twoMonthYear && year == threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    yearMap[10] =
                                        yearMap[10]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[10] = yearCount[10]!! + 1
                                }
                            }
                        } else if (year == twoMonthYear && year > threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == twoMonthMonth) {
                                if (day < twoMonthDay) {
                                    yearMap[10] =
                                        yearMap[10]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[10] = yearCount[10]!! + 1
                                }
                            }
                        }

                        if (year == threeMonthYear && year == fourMonthYear) { // 4개월 전이 올해일 경우
                            if (month == threeMonthMonth && month == fourMonthMonth) { // 4개월 전이 같은 달일 경우
                                if (day in fourMonthDay until threeMonthDay) {
                                    yearMap[9] =
                                        yearMap[9]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[9] = yearCount[9]!! + 1
                                }
                            } else if (month < threeMonthMonth && month == fourMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    yearMap[9] =
                                        yearMap[9]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[9] = yearCount[9]!! + 1
                                }
                            } else if (month == threeMonthMonth && month > fourMonthMonth) { // 4개월 전이 이번달일 경우
                                if (day < threeMonthDay) {
                                    yearMap[9] =
                                        yearMap[9]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[9] = yearCount[9]!! + 1
                                }
                            }
                        } else if (year < threeMonthYear && year == fourMonthYear) { // 4개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == fourMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    yearMap[9] =
                                        yearMap[9]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[9] = yearCount[9]!! + 1
                                }
                            }
                        } else if (year == threeMonthYear && year > fourMonthYear) { // 4개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == threeMonthMonth) {
                                if (day < threeMonthDay) {
                                    yearMap[9] =
                                        yearMap[9]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[9] = yearCount[9]!! + 1
                                }
                            }
                        }

                        if (year == fourMonthYear && year == fiveMonthYear) { // 5개월 전이 올해일 경우
                            if (month == fourMonthMonth && month == fiveMonthMonth) { // 5개월 전이 같은 달일 경우
                                if (day in fiveMonthDay until fourMonthDay) {
                                    yearMap[8] =
                                        yearMap[8]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[8] = yearCount[8]!! + 1
                                }
                            } else if (month < fourMonthMonth && month == fiveMonthMonth) { // 5개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    yearMap[8] =
                                        yearMap[8]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[8] = yearCount[8]!! + 1
                                }
                            } else if (month == fourMonthMonth && month > fiveMonthMonth) { // 5개월 전이 이번달일 경우
                                if (day < fiveMonthDay) {
                                    yearMap[8] =
                                        yearMap[8]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[8] = yearCount[8]!! + 1
                                }
                            }
                        } else if (year < fourMonthYear && year == fiveMonthYear) { // 5개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == fiveMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fiveMonthDay) {
                                    yearMap[8] =
                                        yearMap[8]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[8] = yearCount[8]!! + 1
                                }
                            }
                        } else if (year == fourMonthYear && year > fiveMonthYear) { // 5개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == fourMonthMonth) {
                                if (day < fourMonthDay) {
                                    yearMap[8] =
                                        yearMap[8]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[8] = yearCount[8]!! + 1
                                }
                            }
                        }

                        if (year == fiveMonthYear && year == sixMonthYear) { // 6개월 전이 올해일 경우
                            if (month == fiveMonthMonth && month == sixMonthMonth) { // 6개월 전이 같은 달일 경우
                                if (day in sixMonthDay until fiveMonthDay) {
                                    yearMap[7] =
                                        yearMap[7]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[7] = yearCount[7]!! + 1
                                }
                            } else if (month < fiveMonthMonth && month == sixMonthMonth) { // 6개월 전이 전달일 경우
                                if (day >= sixMonthDay) {
                                    yearMap[7] =
                                        yearMap[7]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[7] = yearCount[7]!! + 1
                                }
                            } else if (month == fiveMonthMonth && month > sixMonthMonth) { // 6개월 전이 이번달일 경우
                                if (day < fiveMonthDay) {
                                    yearMap[7] =
                                        yearMap[7]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[7] = yearCount[7]!! + 1
                                }
                            }
                        } else if (year < fiveMonthYear && year == sixMonthYear) { // 6개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == sixMonthMonth) { // 6개월 전이 전달일 경우
                                if (day >= sixMonthDay) {
                                    yearMap[7] =
                                        yearMap[7]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[7] = yearCount[7]!! + 1
                                }
                            }
                        } else if (year == fiveMonthYear && year > sixMonthYear) { // 6개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == fiveMonthMonth) {
                                if (day < fiveMonthDay) {
                                    yearMap[7] =
                                        yearMap[7]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[7] = yearCount[7]!! + 1
                                }
                            }
                        }

                        if (year == sixMonthYear && year == sevenMonthYear) { // 7개월 전이 올해일 경우
                            if (month == sixMonthMonth && month == sevenMonthMonth) { // 7개월 전이 같은 달일 경우
                                if (day in sevenMonthDay until sixMonthDay) {
                                    yearMap[6] =
                                        yearMap[6]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[6] = yearCount[6]!! + 1
                                }
                            } else if (month < sixMonthMonth && month == sevenMonthMonth) { // 7개월 전이 전달일 경우
                                if (day >= sevenMonthDay) {
                                    yearMap[6] =
                                        yearMap[6]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[6] = yearCount[6]!! + 1
                                }
                            } else if (month == sixMonthMonth && month > sevenMonthMonth) { // 7개월 전이 이번달일 경우
                                if (day < sixMonthDay) {
                                    yearMap[6] =
                                        yearMap[6]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[6] = yearCount[6]!! + 1
                                }
                            }
                        } else if (year < sixMonthYear && year == sevenMonthYear) { // 7개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == sevenMonthMonth) { // 7개월 전이 전달일 경우
                                if (day >= sevenMonthDay) {
                                    yearMap[6] =
                                        yearMap[6]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[6] = yearCount[6]!! + 1
                                }
                            }
                        } else if (year == sixMonthYear && year > sevenMonthYear) { // 7개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == sixMonthMonth) {
                                if (day < sixMonthDay) {
                                    yearMap[6] =
                                        yearMap[6]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[6] = yearCount[6]!! + 1
                                }
                            }
                        }

                        if (year == sevenMonthYear && year == eightMonthYear) { // 8개월 전이 올해일 경우
                            if (month == sevenMonthMonth && month == eightMonthMonth) { // 8개월 전이 같은 달일 경우
                                if (day in eightMonthDay until sevenMonthDay) {
                                    yearMap[5] =
                                        yearMap[5]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[5] = yearCount[5]!! + 1
                                }
                            } else if (month < sevenMonthMonth && month == eightMonthMonth) { // 8개월 전이 전달일 경우
                                if (day >= eightMonthDay) {
                                    yearMap[5] =
                                        yearMap[5]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[5] = yearCount[5]!! + 1
                                }
                            } else if (month == sevenMonthMonth && month > eightMonthMonth) { // 8개월 전이 이번달일 경우
                                if (day < sevenMonthDay) {
                                    yearMap[5] =
                                        yearMap[5]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[5] = yearCount[5]!! + 1
                                }
                            }
                        } else if (year < sevenMonthYear && year == eightMonthYear) { // 8개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == eightMonthMonth) { // 8개월 전이 전달일 경우
                                if (day >= eightMonthDay) {
                                    yearMap[5] =
                                        yearMap[5]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[5] = yearCount[5]!! + 1
                                }
                            }
                        } else if (year == sevenMonthYear && year > eightMonthYear) { // 8개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == sevenMonthMonth) {
                                if (day < sevenMonthDay) {
                                    yearMap[5] =
                                        yearMap[5]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[5] = yearCount[5]!! + 1
                                }
                            }
                        }

                        if (year == eightMonthYear && year == nineMonthYear) { // 9개월 전이 올해일 경우
                            if (month == eightMonthMonth && month == nineMonthMonth) { // 9개월 전이 같은 달일 경우
                                if (day in nineMonthDay until eightMonthDay) {
                                    yearMap[4] =
                                        yearMap[4]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[4] = yearCount[4]!! + 1
                                }
                            } else if (month < eightMonthMonth && month == nineMonthMonth) { // 9개월 전이 전달일 경우
                                if (day >= nineMonthDay) {
                                    yearMap[4] =
                                        yearMap[4]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[4] = yearCount[4]!! + 1
                                }
                            } else if (month == eightMonthMonth && month > nineMonthMonth) { // 9개월 전이 이번달일 경우
                                if (day < eightMonthDay) {
                                    yearMap[4] =
                                        yearMap[4]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[4] = yearCount[4]!! + 1
                                }
                            }
                        } else if (year < eightMonthYear && year == nineMonthYear) { // 9개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == nineMonthMonth) { // 9개월 전이 전달일 경우
                                if (day >= nineMonthDay) {
                                    yearMap[4] =
                                        yearMap[4]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[4] = yearCount[4]!! + 1
                                }
                            }
                        } else if (year == eightMonthYear && year > nineMonthYear) { // 9개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == eightMonthMonth) {
                                if (day < eightMonthDay) {
                                    yearMap[4] =
                                        yearMap[4]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[4] = yearCount[4]!! + 1
                                }
                            }
                        }

                        if (year == nineMonthYear && year == tenMonthYear) { // 10개월 전이 올해일 경우
                            if (month == nineMonthMonth && month == tenMonthMonth) { // 10개월 전이 같은 달일 경우
                                if (day in tenMonthDay until nineMonthDay) {
                                    yearMap[3] =
                                        yearMap[3]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[3] = yearCount[3]!! + 1
                                }
                            } else if (month < nineMonthMonth && month == tenMonthMonth) { // 10개월 전이 전달일 경우
                                if (day >= tenMonthDay) {
                                    yearMap[3] =
                                        yearMap[3]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[3] = yearCount[3]!! + 1
                                }
                            } else if (month == nineMonthMonth && month > tenMonthMonth) { // 10개월 전이 이번달일 경우
                                if (day < nineMonthDay) {
                                    yearMap[3] =
                                        yearMap[3]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[3] = yearCount[3]!! + 1
                                }
                            }
                        } else if (year < nineMonthYear && year == tenMonthYear) { // 10개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == tenMonthMonth) { // 10개월 전이 전달일 경우
                                if (day >= tenMonthDay) {
                                    yearMap[3] =
                                        yearMap[3]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[3] = yearCount[3]!! + 1
                                }
                            }
                        } else if (year == nineMonthYear && year > tenMonthYear) { // 10개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == nineMonthMonth) {
                                if (day < nineMonthDay) {
                                    yearMap[3] =
                                        yearMap[3]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[3] = yearCount[3]!! + 1
                                }
                            }
                        }

                        if (year == tenMonthYear && year == elevenMonthYear) { // 11개월 전이 올해일 경우
                            if (month == tenMonthMonth && month == elevenMonthMonth) { // 11개월 전이 같은 달일 경우
                                if (day in elevenMonthDay until tenMonthDay) {
                                    yearMap[2] =
                                        yearMap[2]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[2] = yearCount[2]!! + 1
                                }
                            } else if (month < tenMonthMonth && month == elevenMonthMonth) { // 11개월 전이 전달일 경우
                                if (day >= elevenMonthDay) {
                                    yearMap[2] =
                                        yearMap[2]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[2] = yearCount[2]!! + 1
                                }
                            } else if (month == tenMonthMonth && month > elevenMonthMonth) { // 11개월 전이 이번달일 경우
                                if (day < tenMonthDay) {
                                    yearMap[2] =
                                        yearMap[2]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[2] = yearCount[2]!! + 1
                                }
                            }
                        } else if (year < tenMonthYear && year == elevenMonthYear) { // 11개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == elevenMonthMonth) { // 11개월 전이 전달일 경우
                                if (day >= elevenMonthDay) {
                                    yearMap[2] =
                                        yearMap[2]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[2] = yearCount[2]!! + 1
                                }
                            }
                        } else if (year == tenMonthYear && year > elevenMonthYear) { // 11개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == tenMonthMonth) {
                                if (day < tenMonthDay) {
                                    yearMap[2] =
                                        yearMap[2]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[2] = yearCount[2]!! + 1
                                }
                            }
                        }

                        if (year == elevenMonthYear && year == yearYear) { // 1년 전이 올해일 경우
                            if (month == elevenMonthMonth && month == yearMonth) { // 1년 전이 같은 달일 경우
                                if (day in yearDay until elevenMonthDay) {
                                    yearMap[1] =
                                        yearMap[1]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[1] = yearCount[1]!! + 1
                                }
                            } else if (month < elevenMonthMonth && month == yearMonth) { // 1년 전이 전달일 경우
                                if (day >= yearDay) {
                                    yearMap[1] =
                                        yearMap[1]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[1] = yearCount[1]!! + 1
                                }
                            } else if (month == elevenMonthMonth && month > yearMonth) { // 1년 전이 이번달일 경우
                                if (day < elevenMonthDay) {
                                    yearMap[1] =
                                        yearMap[1]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[1] = yearCount[1]!! + 1
                                }
                            }
                        } else if (year < elevenMonthYear && year == yearYear) { // 1년 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == yearMonth) { // 1년 전이 전달일 경우
                                if (day >= yearDay) {
                                    yearMap[1] =
                                        yearMap[1]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[1] = yearCount[1]!! + 1
                                }
                            }
                        } else if (year == elevenMonthYear && year > yearYear) { // 1년 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == elevenMonthMonth) {
                                if (day < elevenMonthDay) {
                                    yearMap[1] =
                                        yearMap[1]!! + item!!.waterWeight.toFloat()

                                    if(item!!.waterWeight.toInt() > 0)
                                        yearCount[1] = yearCount[1]!! + 1
                                }
                            }
                        }
                    }

                    yearLabelList.add("1개월")
                    yearLabelList.add("2개월")
                    yearLabelList.add("3개월")
                    yearLabelList.add("4개월")
                    yearLabelList.add("5개월")
                    yearLabelList.add("6개월")
                    yearLabelList.add("7개월")
                    yearLabelList.add("8개월")
                    yearLabelList.add("9개월")
                    yearLabelList.add("10개월")
                    yearLabelList.add("11개월")
                    yearLabelList.add("12개월")

                    val sortedSixMonthMap = sortMapByKey2(yearMap)
                    val sortedSixMonthCountMap = sortMapByKey3(yearCount)
                    var count :  ArrayList<Int> = ArrayList()
                    for((key, value) in sortedSixMonthCountMap)
                        count.add(value)

                    // key(String)에 따른 정렬
                    var i = 0
                    for((key, value) in sortedSixMonthMap.entries) {
                        if(count[i] == 0)
                            yearValueList.add(value)
                        else
                            yearValueList.add(value / count[i])
                        i += 1
                    }

                    Log.d("yearDay", "$yearLabelList $yearValueList")

                    barYearChartGraph(v, yearChart, yearValueList, yearLabelList)

                } catch (e: Exception) {
                    Log.d(TAG, "물 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.waterRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun barYearChartGraph(v : View, barChart : BarChart, valList : ArrayList<Float>, labelList : ArrayList<String>) {

        barChart.extraBottomOffset = 15f // 간격
        barChart.description.isEnabled = false // chart 밑에 description 표시 유무
        barChart.setTouchEnabled(false)
        barChart.setDrawBarShadow(false)
        barChart.setDrawGridBackground(false)

        barChart.run {
            description.isEnabled = false
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawGridBackground(false)

            axisLeft.run {
                axisMinimum = 0f
                granularity = 1f
                setDrawLabels(true)
                setDrawGridLines(true)
                setDrawAxisLine(false)
                textSize = 10f
            }

            xAxis.run {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawAxisLine(true)
                setDrawGridLines(false)
                textSize = 8f
                valueFormatter = IndexAxisValueFormatter(labelList)
            }

            axisRight.isEnabled = false
            setTouchEnabled(false)
            animateY(100)
            legend.isEnabled = false
        }

        var entries : ArrayList<BarEntry> = ArrayList()
        for (i in 0 until valList.size) {
            entries.add(BarEntry(i.toFloat(), valList[i]))
        }

        var depenses = BarDataSet(entries, "음수량 (ml)")
        depenses.axisDependency = YAxis.AxisDependency.LEFT
        depenses.color = Color.parseColor("#87CEEB")
        depenses.valueFormatter = CustomFormatter()

        val data = BarData(depenses)
        data.barWidth = 0.5f

        barChart.run {
            notifyDataSetChanged()
            this.data = data
            setFitBars(true)
            invalidate()
        }
    }

    private fun sortMapByKey1(map: MutableMap<Int, String>): LinkedHashMap<Int, String> {
        val entries = LinkedList(map.entries)

        entries.sortBy { it.key }

        val result = LinkedHashMap<Int, String>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }

    private fun sortMapByKey2(map: MutableMap<Int, Float>): LinkedHashMap<Int, Float> {
        val entries = LinkedList(map.entries)

        entries.sortBy { it.key }

        val result = LinkedHashMap<Int, Float>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }

    fun sortMapByKey3(map: MutableMap<Int, Int>): LinkedHashMap<Int, Int> {
        val entries = LinkedList(map.entries)

        entries.sortBy { it.key }

        val result = LinkedHashMap<Int, Int>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }

    class CustomFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val waterWeight = value.toString().split(".")
            return waterWeight[0]+"ml"
        }
    }
}