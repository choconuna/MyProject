package org.techtown.myproject.receipt

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.disklrucache.DiskLruCache
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.statistics.WaterStatisticsFragment
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.ReceiptModel
import org.techtown.myproject.utils.ReceiptPieModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class ReceiptChartFragment : Fragment() {

    private val TAG = ReceiptChartFragment::class.java.simpleName

    private lateinit var myUid : String

    private lateinit var category : TextView
    private lateinit var date : TextView

    private lateinit var nowCategory : String

    private lateinit var afterEndDate : String
    private lateinit var endDate : String
    private var endYear : Int = 0
    private var endMonth : Int = 0
    private var endDay : Int = 0

    private lateinit var startDate : String
    private var startYear : Int = 0
    private var startMonth : Int = 0
    private var startDay : Int = 0

    private lateinit var totalPriceArea : TextView

    private lateinit var lineWeek : LineChart // 주별에 해당하는 통계 차트
    private lateinit var lineMonth : LineChart // 월별에 해당하는 통계 차트
    private lateinit var lineYear : LineChart // 연별에 해당하는 통계 차트

    private var weekLineMap : MutableMap<Int, Float> = mutableMapOf() // 날짜 순으로 정렬하기 위한 map, MutableMap<날짜, 총액>
    private var monthLineMap : MutableMap<Int, Float> = mutableMapOf() // 날짜 순으로 정렬하기 위한 map, MutableMap<날짜, 총액>
    private var yearLineMap : MutableMap<Int, Float> = mutableMapOf() // 날짜 순으로 정렬하기 위한 map, MutableMap<날짜, 총액>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v : View? = inflater.inflate(R.layout.fragment_receipt_chart, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        nowCategory = arguments?.getString("category").toString() // 선택된 날짜를 받아옴

        setData(v!!)

        return v
    }

    private fun setData(v : View) {

        totalPriceArea =v.findViewById(R.id.totalPriceArea)

        lineWeek = v.findViewById(R.id.line_week)
        lineMonth = v.findViewById(R.id.line_month)
        lineYear = v.findViewById(R.id.line_year)

        when (nowCategory) {
            "주별" -> {
                startDate = arguments?.getString("startDate")!!
                endDate = arguments?.getString("endDate")!!

                setWeekLineChart(v)

                lineWeek.visibility = View.VISIBLE
                lineMonth.visibility = View.GONE
                lineYear.visibility = View.GONE
            }
            "월별" -> {
                endDate = arguments?.getString("endDate")!!

                afterEndDate = endDate.replace("년", ".")
                afterEndDate = afterEndDate.replace("월", "")
                afterEndDate = afterEndDate.replace(" ", "")

                setMonthLineChart(v)

                lineWeek.visibility = View.GONE
                lineMonth.visibility = View.VISIBLE
                lineYear.visibility = View.GONE
            }
            "연별" -> {
                endDate = arguments?.getString("endDate")!!

                afterEndDate = endDate.replace("년", "")

                setYearLineChart(v)

                lineWeek.visibility = View.GONE
                lineMonth.visibility = View.GONE
                lineYear.visibility = View.VISIBLE
            }
        }
    }

    private fun setWeekLineChart(v : View) {
        val startSp = startDate.split(".") // 시작 날짜
        startYear = startSp[0].toInt()
        startMonth = startSp[1].toInt()
        startDay = startSp[2].toInt()

        val endSp = endDate.split(".") // 종료 날짜
        endYear = endSp[0].toInt()
        endMonth = endSp[1].toInt()
        endDay = endSp[2].toInt()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 가계부 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    weekLineMap.clear()
                    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
                    val date: LocalDate = LocalDate.parse(startDate, formatter)

                    var intDate = 0
                    for(i in 0 until 7) {
                        val dateSp = date.plusDays(i.toLong()).toString().split("-")
                        intDate = (dateSp[0] + dateSp[1] + dateSp[2]).toInt()
                        weekLineMap[intDate] = 0.toFloat()
                        Log.d("localDate", intDate.toString())
                    }

                    Log.d("weekLineMap", weekLineMap.toString())

                    var totalPrice = 0

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(ReceiptModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val selectedDate = (sp[0] + sp[1] + sp[2]).toInt()

                        Log.d("selectedDate", selectedDate.toString())

                        for((key, value) in weekLineMap) {
                            if(key == selectedDate) {
                                weekLineMap[key] = weekLineMap[key]!!.plus(item!!.price.toFloat())
                                totalPrice += item!!.price.toInt()
                            }
                        }
                    }

                    Log.d("weekLineMap", weekLineMap.toString())

                    var labelList = ArrayList<String>()
                    for((key, value) in weekLineMap) {
                        val sb = StringBuffer() // 입력된 생년월일이 20100409라면 2010.04.09로 변환하여 화면에 출력하기 위해 StringBuffer() 사용
                        sb.append(key.toString())
                        sb.insert(4, ".")
                        sb.insert(7, ".")
                        val stringb = sb.toString().split(".")
                        labelList.add(stringb[1] + "." + stringb[2])
                    }

                    Log.d("labelList", labelList.toString())

                    val decimalFormat = DecimalFormat("#,###")
                    totalPriceArea.text = decimalFormat.format(totalPrice.toString().replace(",","").toDouble())

                    weekLineChartGraph(v, lineWeek, weekLineMap, labelList)

                } catch (e: Exception) {
                    Log.d(TAG, "가계부 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.receiptRef.child(myUid).addValueEventListener(postListener)
    }

    private fun setMonthLineChart(v : View) {

        val endSp = afterEndDate.split(".") // 종료 날짜
        endYear = endSp[0].toInt()
        endMonth = endSp[1].toInt()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 가계부 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    monthLineMap.clear()
                    var labelList = ArrayList<String>()

                    val cal = Calendar.getInstance()
                    cal.set(endYear, endMonth - 1, 1)
                    if(cal.getActualMaximum(Calendar.DAY_OF_MONTH) == 28) { // 말일이 28일일 경우
                        for(i in 1 until 5)
                            monthLineMap[i] = 0.toFloat()
                        labelList.add("첫째 주")
                        labelList.add("둘째 주")
                        labelList.add("셋째 주")
                        labelList.add("넷째 주")
                    } else if(cal.getActualMaximum(Calendar.DAY_OF_MONTH) > 28) { // 말일이 28일 이후일 경우
                        for(i in 1 until 6)
                            monthLineMap[i] = 0.toFloat()
                        labelList.add("첫째 주")
                        labelList.add("둘째 주")
                        labelList.add("셋째 주")
                        labelList.add("넷째 주")
                        labelList.add("다섯째 주")
                    }

                    var totalPrice = 0

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(ReceiptModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if(year == endYear && month == endMonth) {
                            if (cal.getActualMaximum(Calendar.DAY_OF_MONTH) == 28) {
                                when (day) {
                                    in 1..7 -> monthLineMap[1] = monthLineMap[1]!!.plus(item!!.price.toInt())
                                    in 8..14 -> monthLineMap[2] = monthLineMap[2]!!.plus(item!!.price.toInt())
                                    in 15..21 -> monthLineMap[3] = monthLineMap[3]!!.plus(item!!.price.toInt())
                                    in 22..28 -> monthLineMap[4] = monthLineMap[4]!!.plus(item!!.price.toInt())
                                }
                            } else if (cal.getActualMaximum(Calendar.DAY_OF_MONTH) > 28) {
                                when {
                                    day in 1..7 -> monthLineMap[1] = monthLineMap[1]!!.plus(item!!.price.toInt())
                                    day in 8..14 -> monthLineMap[2] = monthLineMap[2]!!.plus(item!!.price.toInt())
                                    day in 15..21 -> monthLineMap[3] = monthLineMap[3]!!.plus(item!!.price.toInt())
                                    day in 22..28 -> monthLineMap[4] = monthLineMap[4]!!.plus(item!!.price.toInt())
                                    day > 28 -> monthLineMap[5] = monthLineMap[5]!!.plus(item!!.price.toInt())
                                }
                            }
                            totalPrice += item!!.price.toInt()
                        }
                    }

                    Log.d("labelList", labelList.toString())

                    val decimalFormat = DecimalFormat("#,###")
                    totalPriceArea.text = decimalFormat.format(totalPrice.toString().replace(",","").toDouble())

                    monthLineChartGraph(v, lineMonth, monthLineMap, labelList)

                } catch (e: Exception) {
                    Log.d(TAG, "가계부 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.receiptRef.child(myUid).addValueEventListener(postListener)
    }

    private fun setYearLineChart(v : View) {

        endYear = afterEndDate.toInt()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 가계부 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    yearLineMap.clear()
                    yearLineMap[1] = 0.toFloat()
                    yearLineMap[2] = 0.toFloat()
                    yearLineMap[3] = 0.toFloat()
                    yearLineMap[4] = 0.toFloat()
                    yearLineMap[5] = 0.toFloat()
                    yearLineMap[6] = 0.toFloat()
                    yearLineMap[7] = 0.toFloat()
                    yearLineMap[8] = 0.toFloat()
                    yearLineMap[9] = 0.toFloat()
                    yearLineMap[10] = 0.toFloat()
                    yearLineMap[11] = 0.toFloat()
                    yearLineMap[12] = 0.toFloat()

                    var totalPrice = 0

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(ReceiptModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()

                        if(year == endYear) {
                            for((key, value) in yearLineMap) {
                                if(key == month) {
                                    yearLineMap[key] = yearLineMap[key]!!.plus(item!!.price.toFloat())
                                }
                            }
                            totalPrice += item!!.price.toInt()
                        }
                    }

                    var labelList = ArrayList<String>()
                    for(i in 1 until 13) {
                        labelList.add(i.toString() + "월")
                    }

                    val decimalFormat = DecimalFormat("#,###")
                    totalPriceArea.text = decimalFormat.format(totalPrice.toString().replace(",","").toDouble())

                    yearLineChartGraph(v, lineYear, yearLineMap, labelList)

                } catch (e: Exception) {
                    Log.d(TAG, "가계부 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.receiptRef.child(myUid).addValueEventListener(postListener)
    }

    private fun weekLineChartGraph(v : View, lineChart: LineChart, valList : MutableMap<Int, Float>, labelList : ArrayList<String>) {
        lineChart.invalidate()
        lineChart.clear()

        //차트 전체 설정
        lineChart.apply {
            axisRight.isEnabled = false   //y축 사용여부
            axisLeft.isEnabled = true
            axisLeft.axisMinimum = 0f
            axisLeft.setDrawAxisLine(false)
            legend.isEnabled = false    //legend 사용여부
            description.isEnabled = false //주석
            setVisibleXRangeMinimum((60 * 60 * 24 * 1000 * 7).toFloat())
        }

        //X축 설정
        lineChart.xAxis.apply {
            setDrawGridLines(false)
            setDrawAxisLine(true)
            setDrawLabels(true)
            isDrawLabelsEnabled
            isCenterAxisLabelsEnabled
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(labelList)
            labelCount = 7
            textColor = Color.parseColor("#000000")
            granularity = 1f
            textSize = 10f
//            labelRotationAngle = 0f
        }

        Log.d("valList", "$valList $labelList")

        val entries: MutableList<Entry> = mutableListOf()
        var index = 0
        for ((key, value) in valList){
            entries.add(Entry(index.toFloat(), value))
            index += 1
        }
        Log.d("entries", entries.toString())

        var depenses = LineDataSet(entries, "지출 비용")
        depenses.apply {
            color = Color.parseColor("#c08457")
            circleRadius = 4f
            lineWidth = 2f
            setCircleColor(color)
            circleHoleColor = Color.parseColor("#FFFFFF")
            setDrawHighlightIndicators(false)
            setDrawValues(true) // 숫자 표시
            valueTextColor = Color.parseColor("#000000")
            valueFormatter = DefaultValueFormatter(0)  // 소숫점 자릿수 설정
            valueTextSize = 10f
        }

        lineChart.run {
            notifyDataSetChanged()
            this.data = LineData(depenses)
            isDoubleTapToZoomEnabled = false
            setDrawGridBackground(false)
            description = description
            animateY(500, Easing.EaseInCubic)
            invalidate()
        }
    }

    private fun monthLineChartGraph(v : View, lineChart: LineChart, valList : MutableMap<Int, Float>, labelList : ArrayList<String>) {
        lineChart.invalidate()
        lineChart.clear()

        //차트 전체 설정
        lineChart.apply {
            axisRight.isEnabled = false   //y축 사용여부
            axisLeft.isEnabled = true
            axisLeft.axisMinimum = 0f
            axisLeft.setDrawAxisLine(false)
            legend.isEnabled = false    //legend 사용여부
            description.isEnabled = false //주석
            setVisibleXRangeMinimum((60 * 60 * 24 * 1000 * 5).toFloat())
        }

        //X축 설정
        lineChart.xAxis.apply {
            setDrawGridLines(false)
            setDrawAxisLine(true)
            setDrawLabels(true)
            isDrawLabelsEnabled
            isCenterAxisLabelsEnabled
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(labelList)
            labelCount = 5
            textColor = Color.parseColor("#000000")
            granularity = 1f
            textSize = 10f
//            labelRotationAngle = 0f
        }

        Log.d("valList", "$valList $labelList")

        val entries: MutableList<Entry> = mutableListOf()
        var index = 0
        for ((key, value) in valList){
            entries.add(Entry(index.toFloat(), value))
            index += 1
        }
        Log.d("entries", entries.toString())

        var depenses = LineDataSet(entries, "지출 비용")
        depenses.apply {
            color = Color.parseColor("#c08457")
            circleRadius = 6f
            lineWidth = 2f
            setCircleColor(color)
            circleHoleColor = Color.parseColor("#FFFFFF")
            setDrawHighlightIndicators(false)
            setDrawValues(true) // 숫자 표시
            valueTextColor = Color.parseColor("#000000")
            valueFormatter = DefaultValueFormatter(0)  // 소숫점 자릿수 설정
            valueTextSize = 10f
        }

        lineChart.run {
            notifyDataSetChanged()
            this.data = LineData(depenses)
            isDoubleTapToZoomEnabled = false
            setDrawGridBackground(false)
            description = description
            animateY(500, Easing.EaseInCubic)
            invalidate()
        }
    }

    private fun yearLineChartGraph(v : View, lineChart: LineChart, valList : MutableMap<Int, Float>, labelList : ArrayList<String>) {
        lineChart.invalidate()
        lineChart.clear()

        //차트 전체 설정
        lineChart.apply {
            axisRight.isEnabled = false   //y축 사용여부
            axisLeft.isEnabled = true
            axisLeft.axisMinimum = 0f
            axisLeft.setDrawAxisLine(false)
            legend.isEnabled = false    //legend 사용여부
            description.isEnabled = false //주석
            setVisibleXRangeMinimum((60 * 60 * 24 * 1000 * 12).toFloat())
        }

        //X축 설정
        lineChart.xAxis.apply {
            setDrawGridLines(false)
            setDrawAxisLine(true)
            setDrawLabels(true)
            isDrawLabelsEnabled
            isCenterAxisLabelsEnabled
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(labelList)
            labelCount = 12
            textColor = Color.parseColor("#000000")
            granularity = 1f
            textSize = 8f
//            labelRotationAngle = 0f
        }

        Log.d("valList", "$valList $labelList")

        val entries: MutableList<Entry> = mutableListOf()
        var index = 0
        for ((key, value) in valList){
            entries.add(Entry(index.toFloat(), value))
            index += 1
        }
        Log.d("entries", entries.toString())

        var depenses = LineDataSet(entries, "지출 비용")
        depenses.apply {
            color = Color.parseColor("#c08457")
            circleRadius = 4f
            lineWidth = 2f
            setCircleColor(color)
            circleHoleColor = Color.parseColor("#FFFFFF")
            setDrawHighlightIndicators(false)
            setDrawValues(true) // 숫자 표시
            valueTextColor = Color.parseColor("#000000")
            valueFormatter = DefaultValueFormatter(0)  // 소숫점 자릿수 설정
            valueTextSize = 10f
        }

        lineChart.run {
            notifyDataSetChanged()
            this.data = LineData(depenses)
            isDoubleTapToZoomEnabled = false
            setDrawGridBackground(false)
            description = description
            animateY(500, Easing.EaseInCubic)
            invalidate()
        }
    }

    class CustomFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val receiptCategoryPercent = value.toString().split(".")
            return receiptCategoryPercent[0] + "원"
        }
    }
}