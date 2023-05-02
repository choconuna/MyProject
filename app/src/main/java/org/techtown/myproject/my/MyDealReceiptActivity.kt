package org.techtown.myproject.my

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.statistics.MealStatisticsFragment
import org.techtown.myproject.utils.DealModel
import org.techtown.myproject.utils.FBRef
import java.lang.Math.abs
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class MyDealReceiptActivity : AppCompatActivity() {

    private val TAG = MyDealReceiptActivity::class.java.simpleName

    private lateinit var backMonth : Button
    private lateinit var nextMonth : Button
    private lateinit var dateArea : TextView

    private var selectedDate = LocalDate.now()
    private lateinit var year : String
    private lateinit var month : String
    private lateinit var day : String
    private lateinit var dayOfWeek : String

    private lateinit var myUid : String
    private lateinit var nowDate : String

    private lateinit var monthArea : TextView
    private lateinit var monthPriceArea : TextView
    private lateinit var sellCountArea : TextView // 판매 건수
    private lateinit var sellPriceArea : TextView // 판매 금액
    private lateinit var buyCountArea : TextView // 구매 건수
    private lateinit var buyPriceArea : TextView // 구매 금액
    private lateinit var shareCountArea : TextView // 나눔 건수

    private var monthPrice = 0
    private var sellCnt = 0
    private var sellPrice = 0
    private var buyCnt = 0
    private var buyPrice = 0
    private var shareCnt = 0

    private lateinit var receiptChart : BarChart

    private var dateList : ArrayList<String> = ArrayList()
    private var labelList : ArrayList<String> = ArrayList()
    private var valueList : MutableMap<Int, Float> = mutableMapOf()
    private var receiptMap : MutableMap<Int, Float> = mutableMapOf()

    lateinit var backBtn : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_deal_receipt)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        setData()

        backMonth.setOnClickListener {
            selectedDate = selectedDate.minusMonths(1)
            val format = "yyyy년 MM월"
            val sdf = DateTimeFormatter.ofPattern(format)
            dateArea.text = selectedDate.format(sdf)

            val datee = selectedDate.toString().split("-")
            year = datee[0]
            month = datee[1]
            day = datee[2]
            dayOfWeek = selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
            Log.d("selectedDate", "$year $month $day $dayOfWeek")

            if(month.toInt() < 10)
                monthArea.text = month.replace("0", "")
            else
                monthArea.text = month

            showDealReceipt(year, month)

            for (i in 0 until 12) {
                val f = "MM"
                val s = DateTimeFormatter.ofPattern(f)

                val df = "yyyy.MM"
                var sd = DateTimeFormatter.ofPattern(df)

                if(selectedDate.minusMonths(i.toLong()).format(s).toInt() < 12)
                    labelList.add(selectedDate.minusMonths(i.toLong()).format(s).replace("0", ""))
                else
                    labelList.add(selectedDate.minusMonths(i.toLong()).format(s))

                dateList.add(selectedDate.minusMonths(i.toLong()).format(sd))
            }

            getReadyDealReceiptChart(dateList, labelList)
        }

        nextMonth.setOnClickListener {
            selectedDate = selectedDate.plusMonths(1)
            val format = "yyyy년 MM월"
            val sdf = DateTimeFormatter.ofPattern(format)
            dateArea.text = selectedDate.format(sdf)

            val datee = selectedDate.toString().split("-")
            year = datee[0]
            month = datee[1]
            day = datee[2]
            dayOfWeek = selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
            Log.d("selectedDate", "$year $month $day $dayOfWeek")

            if(month.toInt() < 10)
                monthArea.text = month.replace("0", "")
            else
                monthArea.text = month

            showDealReceipt(year, month)

            for (i in 0 until 12) {
                val f = "MM"
                val s = DateTimeFormatter.ofPattern(f)

                val df = "yyyy.MM"
                var sd = DateTimeFormatter.ofPattern(df)

                if(selectedDate.minusMonths(i.toLong()).format(s).toInt() < 12)
                    labelList.add(selectedDate.minusMonths(i.toLong()).format(s).replace("0", ""))
                else
                    labelList.add(selectedDate.minusMonths(i.toLong()).format(s))

                dateList.add(selectedDate.minusMonths(i.toLong()).format(sd))
            }

            getReadyDealReceiptChart(dateList, labelList)
        }

        backBtn.setOnClickListener { // 뒤로 가기 버튼 클릭 시 화면 나오기
            finish()
        }
    }

    private fun setData() {

        backMonth = findViewById(R.id.backMonth)
        nextMonth = findViewById(R.id.nextMonth)
        dateArea = findViewById(R.id.date)

        val cal: Calendar = Calendar.getInstance()
        val format = "yyyy년 MM월"
        val sdf = SimpleDateFormat(format)
        nowDate = sdf.format(cal.time)
        dateArea.text = nowDate

        val cal2 : Calendar = Calendar.getInstance()
        val formatt = "yyyy.MM"
        val sfd = SimpleDateFormat(formatt)
        val date2 = sfd.format(cal2.time).split(".")
        year = date2[0]
        month = date2[1]

        for (i in 0 until 12) {
            val f = "MM"
            val s = DateTimeFormatter.ofPattern(f)

            val df = "yyyy.MM"
            var sd = DateTimeFormatter.ofPattern(df)

            if(selectedDate.minusMonths(i.toLong()).format(s).toInt() < 12)
                labelList.add(selectedDate.minusMonths(i.toLong()).format(s).replace("0", ""))
            else
                labelList.add(selectedDate.minusMonths(i.toLong()).format(s))

            dateList.add(selectedDate.minusMonths(i.toLong()).format(sd))
        }

        monthArea = findViewById(R.id.monthArea)
        if(month.toInt() < 10)
            monthArea.text = month.replace("0", "")
        else
            monthArea.text = month

        monthPriceArea = findViewById(R.id.monthPriceArea)

        sellCountArea = findViewById(R.id.sellCountArea)
        sellPriceArea = findViewById(R.id.sellPriceArea)
        buyCountArea = findViewById(R.id.buyCountArea)
        buyPriceArea = findViewById(R.id.buyPriceArea)
        shareCountArea = findViewById(R.id.shareCountArea)

        receiptChart = findViewById(R.id.receiptChart)

        showDealReceipt(year, month)
        getReadyDealReceiptChart(dateList, labelList)

        backBtn = findViewById(R.id.back)
    }

    private fun showDealReceipt(year : String, month : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {

                    monthPrice = 0
                    sellCnt = 0
                    sellPrice = 0
                    buyCnt = 0
                    buyPrice = 0
                    shareCnt = 0

                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DealModel::class.java)

                        Log.d("dealData", item!!.toString())

                        if (item!!.state == "거래 완료") { // 거래가 완료된 데이터만

                            var dateSp = item!!.buyDate.split(" ")[0].split(".")
                            var dateYear = dateSp[0]
                            var dateMonth = dateSp[1]

                            if (dateYear.toInt() == year.toInt() && dateMonth.toInt() == month.toInt()) {

                                if (item!!.sellerId == myUid) {
                                    if (item!!.price.toInt() > 0) {
                                        sellPrice += item!!.price.toInt()
                                        sellCnt += 1
                                    } else {
                                        shareCnt += 1
                                    }
                                }

                                if (item!!.buyerId == myUid) {
                                    buyPrice += item!!.price.toInt()
                                    buyCnt += 1
                                }
                            }
                        }
                    }

                    monthPrice = sellPrice - buyPrice // 한달 거래 비용은 판매 비용 - 구매 비용
                    val decimalFormat = DecimalFormat("#,###")
                    monthPriceArea!!.text = decimalFormat.format(monthPrice.toString().replace(",","").toDouble()) + "원"

                    if(monthPrice >= 0)
                        monthPriceArea.setTextColor(Color.parseColor("#c08457"))
                    else
                        monthPriceArea.setTextColor(Color.parseColor("#DC143C"))

                    if(sellCnt > 0)
                        sellCountArea.text = sellCnt.toString() + "건"
                    sellPriceArea!!.text = decimalFormat.format(sellPrice.toString().replace(",","").toDouble()) + "원"

                    if(buyCnt > 0)
                        buyCountArea.text = buyCnt.toString() + "건"
                    buyPriceArea!!.text = decimalFormat.format(buyPrice.toString().replace(",","").toDouble()) + "원"

                    shareCountArea.text = shareCnt.toString() + "번"

                } catch (e: Exception) {
                    Log.d(TAG, "거래 기록 삭제 완료")
                    Log.d("dealData", e.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dealRef.addValueEventListener(postListener)
    }

    private fun getReadyDealReceiptChart(dateList : ArrayList<String>, labelList : ArrayList<String>) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {

                    for(i in 1 until 13)
                        valueList[i] = 0.toFloat()

                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DealModel::class.java)

                        Log.d("dealData", item!!.toString())

                        if (item!!.state == "거래 완료") { // 거래가 완료된 데이터만

                            for(i in 1 until 13) {

                                var y = dateList[i].split(".")[0]
                                var m = dateList[i].split(".")[1]

                                var dateSp = item!!.buyDate.split(" ")[0].split(".")
                                var dateYear = dateSp[0]
                                var dateMonth = dateSp[1]

                                if (dateYear.toInt() == y.toInt() && dateMonth.toInt() == m.toInt()) {

                                    if (item!!.sellerId == myUid || item!!.buyerId == myUid) {
                                        valueList[i] = valueList[i]!! + item!!.price.toFloat()
                                    }
                                }
                            }
                        }
                    }

                    Log.d("dealChart", "$labelList $valueList")
                    getDealReceiptChart(labelList, valueList)

                } catch (e: Exception) {
                    Log.d(TAG, "거래 기록 삭제 완료")
                    Log.d("dealData", e.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dealRef.addValueEventListener(postListener)
    }

    private fun getDealReceiptChart(labelList : ArrayList<String>, valueList : MutableMap<Int, Float>) {

        var rLabelList = ArrayList<String>()
        for (i in 11 downTo 0) {
            rLabelList.add(labelList[i])
        }

        receiptChart.run {
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
                valueFormatter = IndexAxisValueFormatter(rLabelList)
            }

            axisRight.isEnabled = false
            setTouchEnabled(false)
            animateY(100)
            legend.isEnabled = false
        }

        val entries = ArrayList<BarEntry>()
        for (i in 11 downTo 0) {
            entries.add(BarEntry(i.toFloat(), valueList[i]!!))
        }


        var depenses = BarDataSet(entries, "날짜")
        depenses.axisDependency = YAxis.AxisDependency.LEFT
         depenses.color = Color.parseColor("#87CEEB")
        depenses.valueFormatter = CustomFormatter()

        val data = BarData(depenses)
        data.barWidth = 0.5f

        receiptChart.setFitBars(true)
        receiptChart.animateY(2000)
        receiptChart.setDrawGridBackground(false)
        receiptChart.setDrawBarShadow(false)
        receiptChart.legend.isEnabled = false
        receiptChart.setPinchZoom(false)
        receiptChart.setScaleEnabled(false)
        receiptChart.setTouchEnabled(true)
        receiptChart.isDragEnabled = true
        receiptChart.axisRight.isEnabled = false
        receiptChart.axisLeft.setDrawGridLines(false)
        receiptChart.axisRight.setDrawGridLines(false)
        receiptChart.xAxis.setDrawGridLines(false)
        receiptChart.axisLeft.axisMinimum = 0f
        receiptChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        receiptChart.setScaleEnabled(true)
        receiptChart.setPinchZoom(false)

        receiptChart.setVisibleXRangeMaximum(12f)
        receiptChart.moveViewToX(0f)
        receiptChart.run {
            notifyDataSetChanged()
            this.data = data
            setFitBars(true)
            invalidate()
        }

        receiptChart.onChartGestureListener = object : OnChartGestureListener {
            private var lastX = 0f
            private val minimumSwipeDistance = 50f // 이동할 수 있는 최소한의 거리

            override fun onChartGestureEnd(
                me: MotionEvent?,
                lastPerformedGesture: ChartTouchListener.ChartGesture?
            ) {
                if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP) {
                    receiptChart.moveViewToX(receiptChart.lowestVisibleX)
                }
            }

            override fun onChartGestureStart(
                me: MotionEvent?,
                lastPerformedGesture: ChartTouchListener.ChartGesture?
            ) {
                lastX = receiptChart.lowestVisibleX
            }

            override fun onChartSingleTapped(me: MotionEvent?) {
            }

            override fun onChartDoubleTapped(me: MotionEvent?) {
            }

            override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
            }

            override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
                if (abs(dX) > minimumSwipeDistance) {
                    receiptChart.moveViewToX(lastX - dX)
                }
            }

            override fun onChartLongPressed(me: MotionEvent?) {
            }

            override fun onChartFling(
                me1: MotionEvent?,
                me2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ) {
                // No-op
            }
        }
    }

    class CustomFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val waterWeight = value.toString().split(".")
            return waterWeight[0] + "원"
        }
    }
}