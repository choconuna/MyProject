package org.techtown.myproject.receipt

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.ReceiptModel
import org.techtown.myproject.utils.ReceiptPieModel
import java.text.SimpleDateFormat
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
                    val format = SimpleDateFormat("yyyy.MM.dd")
                    val date = format.parse(startDate)
                    val cal = Calendar.getInstance()
                    cal.time = date

                    Toast.makeText(v!!.context, cal.time.toString(), Toast.LENGTH_SHORT).show()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(ReceiptModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if(year == startYear && year == endYear) { // 일주일 전이 같은 연도일 경우
                            if(month == startMonth && month == endMonth) { // 일주일 전이 같은 달일 경우
                                if(day in startDay..endDay) {

                                }
                            } else if(month < endMonth && month == startMonth) { // 일주일 전이 전달일 경우
                                if(day >= startDay) {

                                }
                            } else if(month == endMonth && month > startMonth) {  // 일주일 전이 이번달일 경우
                                if(day <= endDay) {

                                }
                            }
                        } else if(year < endYear && year == startYear) { // 일주일 전이 전년도일 경우
                            if(startMonth == month && day >= startDay) {

                            }
                        }
                    }

//                    pieChartGraph(v, lineWeek, weekLineMap)

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


                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(ReceiptModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()

                        if(year == endYear && month == endMonth) {
                        }
                    }

//                    lineChartGraph(v, pieMonth, monthPieMap)

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

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(ReceiptModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()

                        if(year == endYear) {
                        }
                    }

//                    lineChartGraph(v, pieYear, yearPieMap)

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

    private fun lineChartGraph(v : View, pieChart: PieChart, valList : MutableMap<String, Float>) {

        pieChart.extraBottomOffset = 15f // 간격
        pieChart.description.isEnabled = false // chart 밑에 description 표시 유무
        pieChart.setTouchEnabled(false)
        pieChart.isDrawHoleEnabled = false

        pieChart.run {
            description.isEnabled = false

            setTouchEnabled(false)
            legend.isEnabled = true

            setEntryLabelTextSize(10f)
            setEntryLabelColor(Color.parseColor("#FFFFFF"))

            animateY(1400, Easing.EaseInOutQuad);
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
        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)
        for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)

        var depenses = PieDataSet(entries, "")
        with(depenses) {
            sliceSpace = 3f
            selectionShift = 5f
            valueTextSize = 5f
            setColors(colors)
        }
        depenses.valueFormatter = CustomFormatter()

        val data = PieData(depenses)
        with(data) {
            setValueTextSize(10f)
            setValueTextColor(Color.parseColor("#FFFFFF"))
        }

        pieChart.run {
            this.data = data
            description.isEnabled = false
            animate()
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