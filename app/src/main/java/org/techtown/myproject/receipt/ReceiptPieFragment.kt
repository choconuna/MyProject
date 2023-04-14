package org.techtown.myproject.receipt

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.github.mikephil.charting.animation.Easing
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
import org.techtown.myproject.statistics.TonicStatisticsReVAdapter
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.ReceiptModel
import org.techtown.myproject.utils.ReceiptPieModel
import java.util.*

class ReceiptPieFragment : Fragment() {

    private val TAG = ReceiptPieFragment::class.java.simpleName

    private lateinit var myUid : String

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

    private lateinit var pieWeek : PieChart // 주별에 해당하는 비율 차트
    private lateinit var pieMonth : PieChart // 월별에 해당하는 비율 차트
    private lateinit var pieYear : PieChart // 연별에 해당하는 비율 차트

    private var weekPieMap : MutableMap<String, Float> = mutableMapOf() // 비율이 높은 순으로 정렬하기 위한 map, MutableMap<카테고리, 비율>
    private var monthPieMap : MutableMap<String, Float> = mutableMapOf() // 비율이 높은 순으로 정렬하기 위한 map, MutableMap<카테고리, 비율>
    private var yearPieMap : MutableMap<String, Float> = mutableMapOf() // 비율이 높은 순으로 정렬하기 위한 map, MutableMap<카테고리, 비율>

    private lateinit var weekRecyclerView : RecyclerView
    private var weekMap : MutableMap<ReceiptPieModel, Int> = mutableMapOf()
    private var weekMapList : ArrayList<ReceiptPieModel> = ArrayList()
    lateinit var weekReceiptPieStatisticsRVAdapter : ReceiptStatisticsPieReVAdapter
    lateinit var wLayoutManager : RecyclerView.LayoutManager

    private lateinit var monthRecyclerView : RecyclerView
    private var monthMap : MutableMap<ReceiptPieModel, Int> = mutableMapOf()
    private var monthMapList : ArrayList<ReceiptPieModel> = ArrayList()
    lateinit var monthReceiptPieStatisticsRVAdapter : MonthReceiptStatisticsPieReVAdapter
    lateinit var mLayoutManager : RecyclerView.LayoutManager

    private lateinit var yearRecyclerView : RecyclerView
    private var yearMap : MutableMap<ReceiptPieModel, Int> = mutableMapOf()
    private var yearMapList : ArrayList<ReceiptPieModel> = ArrayList()
    lateinit var yearReceiptPieStatisticsRVAdapter : YearReceiptStatisticsPieReVAdapter
    lateinit var yLayoutManager : RecyclerView.LayoutManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v : View? = inflater.inflate(R.layout.fragment_receipt_pie, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        nowCategory = arguments?.getString("category").toString() // 선택된 날짜 카테고리를 받아옴
        setData(v!!)

        return v
    }

    private fun setData(v : View) {

        pieWeek = v.findViewById(R.id.pie_week)
        pieMonth = v.findViewById(R.id.pie_month)
        pieYear = v.findViewById(R.id.pie_year)

        weekRecyclerView = v.findViewById(R.id.weekRecyclerView)
        monthRecyclerView = v.findViewById(R.id.monthRecyclerView)
        yearRecyclerView = v.findViewById(R.id.yearRecyclerView)

        weekReceiptPieStatisticsRVAdapter = ReceiptStatisticsPieReVAdapter(weekMapList)
        weekRecyclerView.setItemViewCacheSize(20)
        weekRecyclerView.setHasFixedSize(true)
        wLayoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        weekRecyclerView.layoutManager = wLayoutManager
        weekRecyclerView.adapter = weekReceiptPieStatisticsRVAdapter

        monthReceiptPieStatisticsRVAdapter = MonthReceiptStatisticsPieReVAdapter(monthMapList)
        monthRecyclerView.setItemViewCacheSize(20)
        monthRecyclerView.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        monthRecyclerView.layoutManager = mLayoutManager
        monthRecyclerView.adapter = monthReceiptPieStatisticsRVAdapter

        yearReceiptPieStatisticsRVAdapter = YearReceiptStatisticsPieReVAdapter(yearMapList)
        yearRecyclerView.setItemViewCacheSize(20)
        yearRecyclerView.setHasFixedSize(true)
        yLayoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        yearRecyclerView.layoutManager = yLayoutManager
        yearRecyclerView.adapter = yearReceiptPieStatisticsRVAdapter


        when (nowCategory) {
            "주별" -> {
                startDate = arguments?.getString("startDate")!!
                endDate = arguments?.getString("endDate")!!

                setWeekPieChart(v)

                weekRecyclerView.visibility = VISIBLE
                monthRecyclerView.visibility = GONE
                yearRecyclerView.visibility = GONE

                pieWeek.visibility = VISIBLE
                pieMonth.visibility = GONE
                pieYear.visibility = GONE
            }
            "월별" -> {
                endDate = arguments?.getString("endDate")!!

                afterEndDate = endDate.replace("년", ".")
                afterEndDate = afterEndDate.replace("월", "")
                afterEndDate = afterEndDate.replace(" ", "")

                setMonthPieChart(v)

                weekRecyclerView.visibility = GONE
                monthRecyclerView.visibility = VISIBLE
                yearRecyclerView.visibility = GONE

                pieWeek.visibility = GONE
                pieMonth.visibility = VISIBLE
                pieYear.visibility = GONE
            }
            "연별" -> {
                endDate = arguments?.getString("endDate")!!

                afterEndDate = endDate.replace("년", "")

                setYearPieChart(v)

                weekRecyclerView.visibility = GONE
                monthRecyclerView.visibility = GONE
                yearRecyclerView.visibility = VISIBLE

                pieWeek.visibility = GONE
                pieMonth.visibility = GONE
                pieYear.visibility = VISIBLE
            }
        }
    }

    private fun setWeekPieChart(v : View) {
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

                    weekPieMap.clear()
                    weekMap.clear()
                    weekMapList.clear()

                    weekPieMap["식비"] = 0.toFloat()
                    weekPieMap["병원"] = 0.toFloat()
                    weekPieMap["용품"] = 0.toFloat()
                    weekPieMap["교육"] = 0.toFloat()
                    weekPieMap["미용"] = 0.toFloat()
                    weekPieMap["교통"] = 0.toFloat()
                    weekPieMap["여행"] = 0.toFloat()
                    weekPieMap["기타"]  = 0.toFloat()

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
                                    weekPieMap[item!!.category] = weekPieMap[item!!.category]!! + item!!.price.toFloat()
                                }
                            } else if(month < endMonth && month == startMonth) { // 일주일 전이 전달일 경우
                                if(day >= startDay) {
                                    weekPieMap[item!!.category] = weekPieMap[item!!.category]!! + item!!.price.toFloat()
                                }
                            } else if(month == endMonth && month > startMonth) {  // 일주일 전이 이번달일 경우
                                if(day <= endDay) {
                                    weekPieMap[item!!.category] = weekPieMap[item!!.category]!! + item!!.price.toFloat()
                                }
                            }
                        } else if(year < endYear && year == startYear) { // 일주일 전이 전년도일 경우
                            if(startMonth == month && day >= startDay) {
                                weekPieMap[item!!.category] = weekPieMap[item!!.category]!! + item!!.price.toFloat()
                            }
                        }
                    }

                    var totalPrice = 0
                    for((key, value) in weekPieMap) {
                        totalPrice += value.toInt()
                    }

                    for((key, value) in weekPieMap) {
                        if(value > 0) {
                            val percent = (("%.2f".format(value / totalPrice)).toFloat() * 100).toInt()
                            weekMap[ReceiptPieModel(key, value.toString(), percent.toString())] =
                                percent
                        }
                    }

                    weekMap = sortMapByKey(weekMap)
                    for((key, value) in weekMap)
                        weekMapList.add(key)

                    weekReceiptPieStatisticsRVAdapter.notifyDataSetChanged()
                    pieChartGraph(v, pieWeek, weekPieMap)

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

    private fun setMonthPieChart(v : View) {

        val endSp = afterEndDate.split(".") // 종료 날짜
        endYear = endSp[0].toInt()
        endMonth = endSp[1].toInt()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 가계부 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    monthPieMap.clear()
                    monthMap.clear()
                    monthMapList.clear()

                    monthPieMap["식비"] = 0.toFloat()
                    monthPieMap["병원"] = 0.toFloat()
                    monthPieMap["용품"] = 0.toFloat()
                    monthPieMap["교육"] = 0.toFloat()
                    monthPieMap["미용"] = 0.toFloat()
                    monthPieMap["교통"] = 0.toFloat()
                    monthPieMap["여행"] = 0.toFloat()
                    monthPieMap["기타"]  = 0.toFloat()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(ReceiptModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()

                        if(year == endYear && month == endMonth) {
                            monthPieMap[item!!.category] = monthPieMap[item!!.category]!! + item!!.price.toFloat()
                        }
                    }

                    var totalPrice = 0
                    for((key, value) in monthPieMap) {
                        totalPrice += value.toInt()
                    }

                    for((key, value) in monthPieMap) {
                        if(value > 0) {
                            val percent = (("%.2f".format(value / totalPrice)).toFloat() * 100).toInt()
                            monthMap[ReceiptPieModel(key, value.toString(), percent.toString())] =
                                percent
                        }
                    }

                    monthMap = sortMapByKey(monthMap)
                    for((key, value) in monthMap)
                        monthMapList.add(key)

                    monthReceiptPieStatisticsRVAdapter.notifyDataSetChanged()

                    pieChartGraph(v, pieMonth, monthPieMap)

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

    private fun setYearPieChart(v : View) {

        endYear = afterEndDate.toInt()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 가계부 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    yearPieMap.clear()
                    yearMap.clear()
                    yearMapList.clear()

                    yearPieMap["식비"] = 0.toFloat()
                    yearPieMap["병원"] = 0.toFloat()
                    yearPieMap["용품"] = 0.toFloat()
                    yearPieMap["교육"] = 0.toFloat()
                    yearPieMap["미용"] = 0.toFloat()
                    yearPieMap["교통"] = 0.toFloat()
                    yearPieMap["여행"] = 0.toFloat()
                    yearPieMap["기타"]  = 0.toFloat()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(ReceiptModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()

                        if(year == endYear) {
                            yearPieMap[item!!.category] = yearPieMap[item!!.category]!! + item!!.price.toFloat()
                        }
                    }

                    var totalPrice = 0
                    for((key, value) in yearPieMap) {
                        totalPrice += value.toInt()
                    }

                    for((key, value) in yearPieMap) {
                        if(value > 0) {
                            val percent = (("%.2f".format(value / totalPrice)).toFloat() * 100).toInt()
                            yearMap[ReceiptPieModel(key, value.toString(), percent.toString())] =
                                percent
                        }
                    }

                    yearMap = sortMapByKey(yearMap)
                    for((key, value) in yearMap)
                        yearMapList.add(key)

                    yearReceiptPieStatisticsRVAdapter.notifyDataSetChanged()

                    pieChartGraph(v, pieYear, yearPieMap)

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

    private fun pieChartGraph(v : View, pieChart: PieChart, valList : MutableMap<String, Float>) {

        pieChart.extraBottomOffset = 10f // 간격
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
                entries.add(PieEntry(("%.2f".format(value / sum)).toFloat() * 100, key))
            }
        }

        val colors: ArrayList<Int> = ArrayList()
        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)
        for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)

        var depenses = PieDataSet(entries, "")
        with(depenses) {
            sliceSpace = 1f
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
            return receiptCategoryPercent[0] + "%"
        }
    }

    private fun sortMapByKey(map: MutableMap<ReceiptPieModel, Int>): LinkedHashMap<ReceiptPieModel, Int> { // 퍼센트 별로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.value }

        val result = LinkedHashMap<ReceiptPieModel, Int>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }
}