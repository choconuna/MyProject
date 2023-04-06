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
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogPeeModel
import org.techtown.myproject.utils.DogVomitModel
import org.techtown.myproject.utils.FBRef
import java.text.SimpleDateFormat
import java.util.*

class VomitStatisticsFragment : Fragment() {

    private val TAG = VomitStatisticsFragment::class.java.simpleName

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

    private lateinit var oneDayChart : PieChart
    private lateinit var weekChart : PieChart
    private lateinit var oneMonthChart : PieChart
    private lateinit var threeMonthChart : PieChart
    private lateinit var sixMonthChart : PieChart
    private lateinit var yearChart : PieChart

    private var oneDayMap : MutableMap<String, Float> = mutableMapOf()

    private var weekMap : MutableMap<String, Float> = mutableMapOf()

    private var oneMonthMap : MutableMap<String, Float> = mutableMapOf()

    private var threeMonthMap : MutableMap<String, Float> = mutableMapOf()

    private var sixMonthMap : MutableMap<String, Float> = mutableMapOf()

    private var yearMap : MutableMap<String, Float> = mutableMapOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v : View? = inflater.inflate(R.layout.fragment_vomit_statistics, container, false)

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

        setTodayChart()
        Log.d("oneDayMap", oneDayMap.toString())
        barTodayChartGraph(v, oneDayChart, oneDayMap)
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
                oneDayChart.visibility = View.VISIBLE
                weekChart.visibility = View.GONE
                oneMonthChart.visibility = View.GONE
                threeMonthChart.visibility = View.GONE
                sixMonthChart.visibility = View.GONE
                yearChart.visibility = View.GONE

                Log.d("oneDayMap", "$oneDayMap")
                setTodayChart()
                barTodayChartGraph(v, oneDayChart, oneDayMap)
            }
            "1주일" -> {
                oneDayChart.visibility = View.GONE
                weekChart.visibility = View.VISIBLE
                oneMonthChart.visibility = View.GONE
                threeMonthChart.visibility = View.GONE
                sixMonthChart.visibility = View.GONE
                yearChart.visibility = View.GONE

                setWeekChartReady()
                barWeekChartGraph(v, weekChart, weekMap)
            }
            "1개월" -> {
                oneDayChart.visibility = View.GONE
                weekChart.visibility = View.GONE
                oneMonthChart.visibility = View.VISIBLE
                threeMonthChart.visibility = View.GONE
                sixMonthChart.visibility = View.GONE
                yearChart.visibility = View.GONE

                setOneMonthChartReady()
                barOneMonthChartGraph(v, oneMonthChart, oneMonthMap)
            }
            "3개월" -> {
                oneDayChart.visibility = View.GONE
                weekChart.visibility = View.GONE
                oneMonthChart.visibility = View.GONE
                threeMonthChart.visibility = View.VISIBLE
                sixMonthChart.visibility = View.GONE
                yearChart.visibility = View.GONE

                setThreeMonthChartReady()
                barThreeMonthChartGraph(v, threeMonthChart, threeMonthMap)
            }
            "6개월" -> {
                oneDayChart.visibility = View.GONE
                weekChart.visibility = View.GONE
                oneMonthChart.visibility = View.GONE
                threeMonthChart.visibility = View.GONE
                sixMonthChart.visibility = View.VISIBLE
                yearChart.visibility = View.GONE

                setSixMonthChartReady()
                barSixMonthChartGraph(v, sixMonthChart, sixMonthMap)
            }
            "1년" -> {
                oneDayChart.visibility = View.GONE
                weekChart.visibility = View.GONE
                oneMonthChart.visibility = View.GONE
                threeMonthChart.visibility = View.GONE
                sixMonthChart.visibility = View.GONE
                yearChart.visibility = View.VISIBLE

                setYearChartReady()
                barYearChartGraph(v, yearChart, yearMap)
            }
        }
    }

    private fun setTodayChart() {
        val nowSp = nowDate.split(".") // 오늘 날짜
        val nowYear = nowSp[0].toInt()
        val nowMonth = nowSp[1].toInt()
        val nowDay = nowSp[2].toInt()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 구토 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogVomitModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if(year == nowYear && month == nowMonth && day == nowDay) {
                            oneDayMap[item!!.vomitType] = item!!.vomitCount.toFloat()
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "구토 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.vomitRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun barTodayChartGraph(v : View, pieChart: PieChart, valList : MutableMap<String, Float>) {

        pieChart.extraBottomOffset = 15f // 간격
        pieChart.description.isEnabled = false // chart 밑에 description 표시 유무
        pieChart.setTouchEnabled(false)

        pieChart.run {
            description.isEnabled = false

            setTouchEnabled(false)
            legend.isEnabled = false

            setEntryLabelTextSize(10f)
            setEntryLabelColor(Color.parseColor("#000000"))
        }

        var entries : ArrayList<PieEntry> = ArrayList()
        for((key, value) in valList.entries) {
            when(key) {
                "transparent" -> entries.add(PieEntry(value, "투명 무색"))
                "bubble" -> entries.add(PieEntry(value, "흰색 거품"))
                "food" -> entries.add(PieEntry(value, "음식"))
                "yellow" -> entries.add(PieEntry(value, "노랑"))
                "leaf" -> entries.add(PieEntry(value, "잎 초록"))
                "pink" -> entries.add(PieEntry(value, "분홍"))
                "brown" -> entries.add(PieEntry(value, "짙은 갈색"))
                "green" -> entries.add(PieEntry(value, "녹색"))
                "substance" -> entries.add(PieEntry(value, "이물질"))
                "red" -> entries.add(PieEntry(value, "붉은색"))
            }
        }

        var depenses = PieDataSet(entries, "")
        with(depenses) {
            sliceSpace = 3f
            selectionShift = 5f
            valueTextSize = 5f
        }
        depenses.valueFormatter = CustomFormatter()
        depenses.color = Color.parseColor("#778899")

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
            val des = Description()
            des.text = "구토"
            des.textSize = 12f
            description = des
        }
    }

    private fun setWeekChartReady() {
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
                try { // 구토 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    weekMap["transparent"] = (0).toFloat()
                    weekMap["bubble"] = (0).toFloat()
                    weekMap["food"] = (0).toFloat()
                    weekMap["yellow"] = (0).toFloat()
                    weekMap["leaf"] = (0).toFloat()
                    weekMap["pink"] = (0).toFloat()
                    weekMap["brown"] = (0).toFloat()
                    weekMap["green"] = (0).toFloat()
                    weekMap["substance"] = (0).toFloat()
                    weekMap["red"] = (0).toFloat()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogVomitModel::class.java)
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
                                    weekMap[item!!.vomitType] = weekMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if(month < nowMonth && month == weekMonth) { // 일주일 전이 전달일 경우
                                if(day >= weekDay) {
                                    weekMap[item!!.vomitType] = weekMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if(month == nowMonth && month > weekMonth) { // 일주일 전이 이번달일 경우
                                if(day <= nowDay) {
                                    weekMap[item!!.vomitType] = weekMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if(year < nowYear && year == weekYear) { // 일주일 전이 전년도일 경우
                            if(weekMonth == month && day >= weekDay) {
                                weekMap[item!!.vomitType] = weekMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "구토 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.vomitRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }


    private fun barWeekChartGraph(v : View, pieChart: PieChart, valList : MutableMap<String, Float>) {

        pieChart.extraBottomOffset = 15f // 간격
        pieChart.description.isEnabled = false // chart 밑에 description 표시 유무
        pieChart.setTouchEnabled(false)

        pieChart.run {
            description.isEnabled = false

            setTouchEnabled(false)
            legend.isEnabled = false

            setEntryLabelTextSize(10f)
            setEntryLabelColor(Color.parseColor("#000000"))
        }

        var entries : ArrayList<PieEntry> = ArrayList()
        for((key, value) in valList.entries) {
            if(value > 0) {
                when (key) {
                    "transparent" -> entries.add(PieEntry(value, "투명 무색"))
                    "bubble" -> entries.add(PieEntry(value, "흰색 거품"))
                    "food" -> entries.add(PieEntry(value, "음식"))
                    "yellow" -> entries.add(PieEntry(value, "노랑"))
                    "leaf" -> entries.add(PieEntry(value, "잎 초록"))
                    "pink" -> entries.add(PieEntry(value, "분홍"))
                    "brown" -> entries.add(PieEntry(value, "짙은 갈색"))
                    "green" -> entries.add(PieEntry(value, "녹색"))
                    "substance" -> entries.add(PieEntry(value, "이물질"))
                    "red" -> entries.add(PieEntry(value, "붉은색"))
                }
            }
        }

        var depenses = PieDataSet(entries, "")
        with(depenses) {
            sliceSpace = 3f
            selectionShift = 5f
            valueTextSize = 5f
        }
        depenses.valueFormatter = CustomFormatter()
        depenses.color = Color.parseColor("#778899")

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
            val des = Description()
            des.text = "구토"
            des.textSize = 12f
            description = des
        }
    }

    private fun setOneMonthChartReady() {
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
                try { // 구토 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    oneMonthMap["transparent"] = (0).toFloat()
                    oneMonthMap["bubble"] = (0).toFloat()
                    oneMonthMap["food"] = (0).toFloat()
                    oneMonthMap["yellow"] = (0).toFloat()
                    oneMonthMap["leaf"] = (0).toFloat()
                    oneMonthMap["pink"] = (0).toFloat()
                    oneMonthMap["brown"] = (0).toFloat()
                    oneMonthMap["green"] = (0).toFloat()
                    oneMonthMap["substance"] = (0).toFloat()
                    oneMonthMap["red"] = (0).toFloat()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogVomitModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if(year == nowYear && year == weekYear) { // 일주일 전이 같은 연도일 경우
                            if(month == nowMonth && month == weekMonth) { // 일주일 전이 같은 달일 경우
                                if(day in weekDay..nowDay) {
                                    oneMonthMap[item!!.vomitType] = oneMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if(month < nowMonth && month == weekMonth) { // 일주일 전이 이전 달일 경우
                                if(day >= weekDay) {
                                    oneMonthMap[item!!.vomitType] = oneMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if(month == nowMonth && month > weekMonth) { // 일주일 전이 이번 달일 경우
                                if(day <= nowDay) {
                                    oneMonthMap[item!!.vomitType] = oneMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if(year < nowYear && year == weekYear) { // 일주일 전이 전년도일 경우
                            if(weekMonth == month && day >= weekDay) {
                                oneMonthMap[item!!.vomitType] = oneMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                            }
                        }

                        if(year == weekYear && year == twoWeekYear) { // 2주일 전이 같은 연도일 경우
                            if(month == weekMonth && month == twoWeekMonth) { // 2주일 전이 같은 달일 경우
                                if(day in twoWeekDay until weekDay) {
                                    oneMonthMap[item!!.vomitType] = oneMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if(month < weekMonth && month == twoWeekMonth) { // 2주일 전이 이전 달일 경우
                                if(day >= twoWeekDay) {
                                    oneMonthMap[item!!.vomitType] = oneMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if(month == weekMonth && month > twoWeekMonth) { // 2주일 전이 이번 달일 경우
                                if(day < weekDay) {
                                    oneMonthMap[item!!.vomitType] = oneMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if(year < weekYear && year == twoWeekYear) { // 2주일 전이 전년도일 경우
                            if(twoWeekMonth == month && day >= twoWeekDay) {
                                oneMonthMap[item!!.vomitType] = oneMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                            }
                        }

                        if(year == twoWeekYear && year == threeWeekYear) { // 3주일 전이 같은 연도일 경우
                            if(month == twoWeekMonth && month == threeWeekMonth) { // 3주일 전이 같은 달일 경우
                                if(day in threeWeekDay until twoWeekDay) {
                                    oneMonthMap[item!!.vomitType] = oneMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if(month < twoWeekMonth && month == threeWeekMonth) { // 3주일 전이 이전 달일 경우
                                if(day >= threeWeekDay) {
                                    oneMonthMap[item!!.vomitType] = oneMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if(month == twoWeekMonth && month > threeWeekMonth) { // 3주일 전이 이번 달일 경우
                                if(day < twoWeekDay) {
                                    oneMonthMap[item!!.vomitType] = oneMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if(year < twoWeekYear && year == threeWeekYear) { // 3주일 전이 전년도일 경우
                            if(threeWeekMonth == month && day >= threeWeekDay) {
                                oneMonthMap[item!!.vomitType] = oneMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                            }
                        }

                        if(year == threeWeekYear && year == oneMonthYear) { // 한달 전이 같은 연도일 경우
                            if(month == threeWeekMonth && month == oneMonthMonth) { // 한달 전이 같은 달일 경우
                                if(day in oneMonthDay until threeWeekDay) {
                                    oneMonthMap[item!!.vomitType] = oneMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if(month < threeWeekMonth && month == oneMonthMonth) { // 한달 전이 이전 달일 경우
                                if(day >= oneMonthDay) {
                                    oneMonthMap[item!!.vomitType] = oneMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if(month == threeWeekMonth && month > oneMonthMonth) { // 한달 전이 이번 달일 경우
                                if(day < threeWeekDay) {
                                    oneMonthMap[item!!.vomitType] = oneMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if(year < threeWeekYear && year == oneMonthYear) { // 한달 전이 전년도일 경우
                            if(oneMonthMonth == month && day >= oneMonthDay) {
                                oneMonthMap[item!!.vomitType] = oneMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "구토 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.vomitRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun barOneMonthChartGraph(v : View, pieChart: PieChart, valList : MutableMap<String, Float>) {

        pieChart.extraBottomOffset = 15f // 간격
        pieChart.description.isEnabled = false // chart 밑에 description 표시 유무
        pieChart.setTouchEnabled(false)

        pieChart.run {
            description.isEnabled = false

            setTouchEnabled(false)
            legend.isEnabled = false

            setEntryLabelTextSize(10f)
            setEntryLabelColor(Color.parseColor("#000000"))
        }

        var entries : ArrayList<PieEntry> = ArrayList()
        for((key, value) in valList.entries) {
            if(value > 0) {
                when (key) {
                    "transparent" -> entries.add(PieEntry(value, "투명 무색"))
                    "bubble" -> entries.add(PieEntry(value, "흰색 거품"))
                    "food" -> entries.add(PieEntry(value, "음식"))
                    "yellow" -> entries.add(PieEntry(value, "노랑"))
                    "leaf" -> entries.add(PieEntry(value, "잎 초록"))
                    "pink" -> entries.add(PieEntry(value, "분홍"))
                    "brown" -> entries.add(PieEntry(value, "짙은 갈색"))
                    "green" -> entries.add(PieEntry(value, "녹색"))
                    "substance" -> entries.add(PieEntry(value, "이물질"))
                    "red" -> entries.add(PieEntry(value, "붉은색"))
                }
            }
        }

        var depenses = PieDataSet(entries, "")
        with(depenses) {
            sliceSpace = 3f
            selectionShift = 5f
            valueTextSize = 5f
        }
        depenses.valueFormatter = CustomFormatter()
        depenses.color = Color.parseColor("#778899")

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
            val des = Description()
            des.text = "구토"
            des.textSize = 12f
            description = des
        }
    }

    private fun setThreeMonthChartReady() {
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
                try { // 소변 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    threeMonthMap["transparent"] = (0).toFloat()
                    threeMonthMap["bubble"] = (0).toFloat()
                    threeMonthMap["food"] = (0).toFloat()
                    threeMonthMap["yellow"] = (0).toFloat()
                    threeMonthMap["leaf"] = (0).toFloat()
                    threeMonthMap["pink"] = (0).toFloat()
                    threeMonthMap["brown"] = (0).toFloat()
                    threeMonthMap["green"] = (0).toFloat()
                    threeMonthMap["substance"] = (0).toFloat()
                    threeMonthMap["red"] = (0).toFloat()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogVomitModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if (year == nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도일 경우
                            if (month == nowMonth && month == oneMonthMonth) { // 1개월 전이 같은 달일 경우
                                if (day in oneMonthDay..nowDay) {
                                    threeMonthMap[item!!.vomitType] = threeMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < nowMonth && month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    threeMonthMap[item!!.vomitType] = threeMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == nowMonth && month > oneMonthMonth) { // 1개월 전이 이번 달일 경우
                                if (day <= nowDay) {
                                    threeMonthMap[item!!.vomitType] = threeMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 년도일 경우
                            if (month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    threeMonthMap[item!!.vomitType] = threeMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == nowYear && year > oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == nowMonth) { // 1개월 전이 이번달일 경우
                                if (day <= nowDay) {
                                    threeMonthMap[item!!.vomitType] = threeMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        }

                        if (year == oneMonthYear && year == twoMonthYear) { // 2개월 전에 오늘일 경우
                            if (month == oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 같은 달일 경우
                                if (day in twoMonthDay until oneMonthDay) {
                                    threeMonthMap[item!!.vomitType] = threeMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    threeMonthMap[item!!.vomitType] = threeMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == oneMonthMonth && month > twoMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    threeMonthMap[item!!.vomitType] = threeMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < oneMonthYear && year == twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    threeMonthMap[item!!.vomitType] = threeMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == oneMonthYear && year > twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == oneMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    threeMonthMap[item!!.vomitType] = threeMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        }

                        if (year == twoMonthYear && year == threeMonthYear) { // 3개월 전이 올해일 경우
                            if (month == twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 같은 달일 경우
                                if (day in threeMonthDay until twoMonthDay) {
                                    threeMonthMap[item!!.vomitType] = threeMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    threeMonthMap[item!!.vomitType] = threeMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == twoMonthMonth && month > threeMonthMonth) { // 3개월 전이 이번달일 경우
                                if (day < twoMonthDay) {
                                    threeMonthMap[item!!.vomitType] = threeMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < twoMonthYear && year == threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    threeMonthMap[item!!.vomitType] = threeMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == twoMonthYear && year > threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == twoMonthMonth) {
                                if (day < twoMonthDay) {
                                    threeMonthMap[item!!.vomitType] = threeMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "구토 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.vomitRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun barThreeMonthChartGraph(v : View, pieChart: PieChart, valList : MutableMap<String, Float>) {

        pieChart.extraBottomOffset = 15f // 간격
        pieChart.description.isEnabled = false // chart 밑에 description 표시 유무
        pieChart.setTouchEnabled(false)

        pieChart.run {
            description.isEnabled = false

            setTouchEnabled(false)
            legend.isEnabled = false

            setEntryLabelTextSize(10f)
            setEntryLabelColor(Color.parseColor("#000000"))
        }

        var entries : ArrayList<PieEntry> = ArrayList()
        for((key, value) in valList.entries) {
            if(value > 0) {
                when (key) {
                    "transparent" -> entries.add(PieEntry(value, "투명 무색"))
                    "bubble" -> entries.add(PieEntry(value, "흰색 거품"))
                    "food" -> entries.add(PieEntry(value, "음식"))
                    "yellow" -> entries.add(PieEntry(value, "노랑"))
                    "leaf" -> entries.add(PieEntry(value, "잎 초록"))
                    "pink" -> entries.add(PieEntry(value, "분홍"))
                    "brown" -> entries.add(PieEntry(value, "짙은 갈색"))
                    "green" -> entries.add(PieEntry(value, "녹색"))
                    "substance" -> entries.add(PieEntry(value, "이물질"))
                    "red" -> entries.add(PieEntry(value, "붉은색"))
                }
            }
        }

        var depenses = PieDataSet(entries, "")
        with(depenses) {
            sliceSpace = 3f
            selectionShift = 5f
            valueTextSize = 5f
        }
        depenses.valueFormatter = CustomFormatter()
        depenses.color = Color.parseColor("#778899")

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
            val des = Description()
            des.text = "구토"
            des.textSize = 12f
            description = des
        }
    }

    private fun setSixMonthChartReady() {
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
                try { // 물 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    sixMonthMap["transparent"] = (0).toFloat()
                    sixMonthMap["bubble"] = (0).toFloat()
                    sixMonthMap["food"] = (0).toFloat()
                    sixMonthMap["yellow"] = (0).toFloat()
                    sixMonthMap["leaf"] = (0).toFloat()
                    sixMonthMap["pink"] = (0).toFloat()
                    sixMonthMap["brown"] = (0).toFloat()
                    sixMonthMap["green"] = (0).toFloat()
                    sixMonthMap["substance"] = (0).toFloat()
                    sixMonthMap["red"] = (0).toFloat()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogVomitModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if (year == nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도일 경우
                            if (month == nowMonth && month == oneMonthMonth) { // 1개월 전이 같은 달일 경우
                                if (day in oneMonthDay..nowDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < nowMonth && month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == nowMonth && month > oneMonthMonth) { // 1개월 전이 이번 달일 경우
                                if (day <= nowDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 년도일 경우
                            if (month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == nowYear && year > oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == nowMonth) { // 1개월 전이 이번달일 경우
                                if (day <= nowDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        }

                        if (year == oneMonthYear && year == twoMonthYear) { // 2개월 전에 오늘일 경우
                            if (month == oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 같은 달일 경우
                                if (day in twoMonthDay until oneMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == oneMonthMonth && month > twoMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < oneMonthYear && year == twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == oneMonthYear && year > twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == oneMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        }

                        if (year == twoMonthYear && year == threeMonthYear) { // 3개월 전이 올해일 경우
                            if (month == twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 같은 달일 경우
                                if (day in threeMonthDay until twoMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == twoMonthMonth && month > threeMonthMonth) { // 3개월 전이 이번달일 경우
                                if (day < twoMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < twoMonthYear && year == threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == twoMonthYear && year > threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == twoMonthMonth) {
                                if (day < twoMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        }

                        if (year == threeMonthYear && year == fourMonthYear) { // 4개월 전이 올해일 경우
                            if (month == threeMonthMonth && month == fourMonthMonth) { // 4개월 전이 같은 달일 경우
                                if (day in fourMonthDay until threeMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < threeMonthMonth && month == fourMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == threeMonthMonth && month > fourMonthMonth) { // 4개월 전이 이번달일 경우
                                if (day < threeMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < threeMonthYear && year == fourMonthYear) { // 4개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == fourMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == threeMonthYear && year > fourMonthYear) { // 4개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == threeMonthMonth) {
                                if (day < threeMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        }

                        if (year == fourMonthYear && year == fiveMonthYear) { // 5개월 전이 올해일 경우
                            if (month == fourMonthMonth && month == fiveMonthMonth) { // 5개월 전이 같은 달일 경우
                                if (day in fiveMonthDay until fourMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < fourMonthMonth && month == fiveMonthMonth) { // 5개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == fourMonthMonth && month > fiveMonthMonth) { // 5개월 전이 이번달일 경우
                                if (day < fiveMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < fourMonthYear && year == fiveMonthYear) { // 5개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == fiveMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fiveMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == fourMonthYear && year > fiveMonthYear) { // 5개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == fourMonthMonth) {
                                if (day < fourMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        }

                        if (year == fiveMonthYear && year == sixMonthYear) { // 6개월 전이 올해일 경우
                            if (month == fiveMonthMonth && month == sixMonthMonth) { // 6개월 전이 같은 달일 경우
                                if (day in sixMonthDay until fiveMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < fiveMonthMonth && month == sixMonthMonth) { // 6개월 전이 전달일 경우
                                if (day >= sixMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == fiveMonthMonth && month > sixMonthMonth) { // 6개월 전이 이번달일 경우
                                if (day < fiveMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < fiveMonthYear && year == sixMonthYear) { // 6개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == sixMonthMonth) { // 6개월 전이 전달일 경우
                                if (day >= sixMonthDay) {
                                    sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == fiveMonthYear && year > sixMonthYear) { // 6개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(day < fiveMonthDay) {
                                sixMonthMap[item!!.vomitType] = sixMonthMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                            }
                        }

                    }
                } catch (e: Exception) {
                    Log.d(TAG, "구토 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.vomitRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun barSixMonthChartGraph(v : View, pieChart: PieChart, valList : MutableMap<String, Float>) {

        pieChart.extraBottomOffset = 15f // 간격
        pieChart.description.isEnabled = false // chart 밑에 description 표시 유무
        pieChart.setTouchEnabled(false)

        pieChart.run {
            description.isEnabled = false

            setTouchEnabled(false)
            legend.isEnabled = false

            setEntryLabelTextSize(10f)
            setEntryLabelColor(Color.parseColor("#000000"))
        }

        var entries : ArrayList<PieEntry> = ArrayList()
        for((key, value) in valList.entries) {
            if(value > 0) {
                when (key) {
                    "transparent" -> entries.add(PieEntry(value, "투명 무색"))
                    "bubble" -> entries.add(PieEntry(value, "흰색 거품"))
                    "food" -> entries.add(PieEntry(value, "음식"))
                    "yellow" -> entries.add(PieEntry(value, "노랑"))
                    "leaf" -> entries.add(PieEntry(value, "잎 초록"))
                    "pink" -> entries.add(PieEntry(value, "분홍"))
                    "brown" -> entries.add(PieEntry(value, "짙은 갈색"))
                    "green" -> entries.add(PieEntry(value, "녹색"))
                    "substance" -> entries.add(PieEntry(value, "이물질"))
                    "red" -> entries.add(PieEntry(value, "붉은색"))
                }
            }
        }

        var depenses = PieDataSet(entries, "")
        with(depenses) {
            sliceSpace = 3f
            selectionShift = 5f
            valueTextSize = 5f
        }
        depenses.valueFormatter = CustomFormatter()
        depenses.color = Color.parseColor("#778899")

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
            val des = Description()
            des.text = "구토"
            des.textSize = 12f
            description = des
        }
    }

    private fun setYearChartReady() {
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
                try { // 구토 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 적용

                    yearMap["transparent"] = (0).toFloat()
                    yearMap["bubble"] = (0).toFloat()
                    yearMap["food"] = (0).toFloat()
                    yearMap["yellow"] = (0).toFloat()
                    yearMap["leaf"] = (0).toFloat()
                    yearMap["pink"] = (0).toFloat()
                    yearMap["brown"] = (0).toFloat()
                    yearMap["green"] = (0).toFloat()
                    yearMap["substance"] = (0).toFloat()
                    yearMap["red"] = (0).toFloat()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogVomitModel::class.java)
                        val date = item!!.date
                        val sp = date.split(".")
                        val year = sp[0].toInt()
                        val month = sp[1].toInt()
                        val day = sp[2].toInt()

                        if (year == nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도일 경우
                            if (month == nowMonth && month == oneMonthMonth) { // 1개월 전이 같은 달일 경우
                                if (day in oneMonthDay..nowDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < nowMonth && month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == nowMonth && month > oneMonthMonth) { // 1개월 전이 이번 달일 경우
                                if (day <= nowDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < nowYear && year == oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 년도일 경우
                            if (month == oneMonthMonth) { // 1개월 전이 전달일 경우
                                if (day >= oneMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == nowYear && year > oneMonthYear) { // 1개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == nowMonth) { // 1개월 전이 이번달일 경우
                                if (day <= nowDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        }

                        if (year == oneMonthYear && year == twoMonthYear) { // 2개월 전에 오늘일 경우
                            if (month == oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 같은 달일 경우
                                if (day in twoMonthDay until oneMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < oneMonthMonth && month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == oneMonthMonth && month > twoMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < oneMonthYear && year == twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == twoMonthMonth) { // 2개월 전이 전달일 경우
                                if (day >= twoMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == oneMonthYear && year > twoMonthYear) { // 2개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if (month == oneMonthMonth) { // 2개월 전이 이번달일 경우
                                if (day < oneMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        }

                        if (year == twoMonthYear && year == threeMonthYear) { // 3개월 전이 올해일 경우
                            if (month == twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 같은 달일 경우
                                if (day in threeMonthDay until twoMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < twoMonthMonth && month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == twoMonthMonth && month > threeMonthMonth) { // 3개월 전이 이번달일 경우
                                if (day < twoMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < twoMonthYear && year == threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == threeMonthMonth) { // 3개월 전이 전달일 경우
                                if (day >= threeMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == twoMonthYear && year > threeMonthYear) { // 3개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == twoMonthMonth) {
                                if (day < twoMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        }

                        if (year == threeMonthYear && year == fourMonthYear) { // 4개월 전이 올해일 경우
                            if (month == threeMonthMonth && month == fourMonthMonth) { // 4개월 전이 같은 달일 경우
                                if (day in fourMonthDay until threeMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < threeMonthMonth && month == fourMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == threeMonthMonth && month > fourMonthMonth) { // 4개월 전이 이번달일 경우
                                if (day < threeMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < threeMonthYear && year == fourMonthYear) { // 4개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == fourMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == threeMonthYear && year > fourMonthYear) { // 4개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == threeMonthMonth) {
                                if (day < threeMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        }

                        if (year == fourMonthYear && year == fiveMonthYear) { // 5개월 전이 올해일 경우
                            if (month == fourMonthMonth && month == fiveMonthMonth) { // 5개월 전이 같은 달일 경우
                                if (day in fiveMonthDay until fourMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < fourMonthMonth && month == fiveMonthMonth) { // 5개월 전이 전달일 경우
                                if (day >= fourMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == fourMonthMonth && month > fiveMonthMonth) { // 5개월 전이 이번달일 경우
                                if (day < fiveMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < fourMonthYear && year == fiveMonthYear) { // 5개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == fiveMonthMonth) { // 4개월 전이 전달일 경우
                                if (day >= fiveMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == fourMonthYear && year > fiveMonthYear) { // 5개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == fourMonthMonth) {
                                if (day < fourMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        }

                        if (year == fiveMonthYear && year == sixMonthYear) { // 6개월 전이 올해일 경우
                            if (month == fiveMonthMonth && month == sixMonthMonth) { // 6개월 전이 같은 달일 경우
                                if (day in sixMonthDay until fiveMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < fiveMonthMonth && month == sixMonthMonth) { // 6개월 전이 전달일 경우
                                if (day >= sixMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == fiveMonthMonth && month > sixMonthMonth) { // 6개월 전이 이번달일 경우
                                if (day < fiveMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < fiveMonthYear && year == sixMonthYear) { // 6개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == sixMonthMonth) { // 6개월 전이 전달일 경우
                                if (day >= sixMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == fiveMonthYear && year > sixMonthYear) { // 6개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == fiveMonthMonth) {
                                if (day < fiveMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        }

                        if (year == sixMonthYear && year == sevenMonthYear) { // 7개월 전이 올해일 경우
                            if (month == sixMonthMonth && month == sevenMonthMonth) { // 7개월 전이 같은 달일 경우
                                if (day in sevenMonthDay until sixMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < sixMonthMonth && month == sevenMonthMonth) { // 7개월 전이 전달일 경우
                                if (day >= sevenMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == sixMonthMonth && month > sevenMonthMonth) { // 7개월 전이 이번달일 경우
                                if (day < sixMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < sixMonthYear && year == sevenMonthYear) { // 7개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == sevenMonthMonth) { // 7개월 전이 전달일 경우
                                if (day >= sevenMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == sixMonthYear && year > sevenMonthYear) { // 7개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == sixMonthMonth) {
                                if (day < sixMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        }

                        if (year == sevenMonthYear && year == eightMonthYear) { // 8개월 전이 올해일 경우
                            if (month == sevenMonthMonth && month == eightMonthMonth) { // 8개월 전이 같은 달일 경우
                                if (day in eightMonthDay until sevenMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < sevenMonthMonth && month == eightMonthMonth) { // 8개월 전이 전달일 경우
                                if (day >= eightMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == sevenMonthMonth && month > eightMonthMonth) { // 8개월 전이 이번달일 경우
                                if (day < sevenMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < sevenMonthYear && year == eightMonthYear) { // 8개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == eightMonthMonth) { // 8개월 전이 전달일 경우
                                if (day >= eightMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == sevenMonthYear && year > eightMonthYear) { // 8개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == sevenMonthMonth) {
                                if (day < sevenMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        }

                        if (year == eightMonthYear && year == nineMonthYear) { // 9개월 전이 올해일 경우
                            if (month == eightMonthMonth && month == nineMonthMonth) { // 9개월 전이 같은 달일 경우
                                if (day in nineMonthDay until eightMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < eightMonthMonth && month == nineMonthMonth) { // 9개월 전이 전달일 경우
                                if (day >= nineMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == eightMonthMonth && month > nineMonthMonth) { // 9개월 전이 이번달일 경우
                                if (day < eightMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < eightMonthYear && year == nineMonthYear) { // 9개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == nineMonthMonth) { // 9개월 전이 전달일 경우
                                if (day >= nineMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == eightMonthYear && year > nineMonthYear) { // 9개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == eightMonthMonth) {
                                if (day < eightMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        }

                        if (year == nineMonthYear && year == tenMonthYear) { // 10개월 전이 올해일 경우
                            if (month == nineMonthMonth && month == tenMonthMonth) { // 10개월 전이 같은 달일 경우
                                if (day in tenMonthDay until nineMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < nineMonthMonth && month == tenMonthMonth) { // 10개월 전이 전달일 경우
                                if (day >= tenMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == nineMonthMonth && month > tenMonthMonth) { // 10개월 전이 이번달일 경우
                                if (day < nineMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < nineMonthYear && year == tenMonthYear) { // 10개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == tenMonthMonth) { // 10개월 전이 전달일 경우
                                if (day >= tenMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == nineMonthYear && year > tenMonthYear) { // 10개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == nineMonthMonth) {
                                if (day < nineMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        }

                        if (year == tenMonthYear && year == elevenMonthYear) { // 11개월 전이 올해일 경우
                            if (month == tenMonthMonth && month == elevenMonthMonth) { // 11개월 전이 같은 달일 경우
                                if (day in elevenMonthDay until tenMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < tenMonthMonth && month == elevenMonthMonth) { // 11개월 전이 전달일 경우
                                if (day >= elevenMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == tenMonthMonth && month > elevenMonthMonth) { // 11개월 전이 이번달일 경우
                                if (day < tenMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < tenMonthYear && year == elevenMonthYear) { // 11개월 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == elevenMonthMonth) { // 11개월 전이 전달일 경우
                                if (day >= elevenMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == tenMonthYear && year > elevenMonthYear) { // 11개월 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == tenMonthMonth) {
                                if (day < tenMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        }

                        if (year == elevenMonthYear && year == yearYear) { // 1년 전이 올해일 경우
                            if (month == elevenMonthMonth && month == yearMonth) { // 1년 전이 같은 달일 경우
                                if (day in yearDay until elevenMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month < elevenMonthMonth && month == yearMonth) { // 1년 전이 전달일 경우
                                if (day >= yearDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            } else if (month == elevenMonthMonth && month > yearMonth) { // 1년 전이 이번달일 경우
                                if (day < elevenMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year < elevenMonthYear && year == yearYear) { // 1년 전후가 같은 연도가 아닌데 현재 날짜가 이전 연도일 경우
                            if (month == yearMonth) { // 1년 전이 전달일 경우
                                if (day >= yearDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        } else if (year == elevenMonthYear && year > yearYear) { // 1년 전후가 같은 연도가 아닌데 현재 날짜가 이번 년도인 경우
                            if(month == elevenMonthMonth) {
                                if (day < elevenMonthDay) {
                                    yearMap[item!!.vomitType] = yearMap[item!!.vomitType]!! + item!!.vomitCount.toFloat()
                                }
                            }
                        }

                    }
                } catch (e: Exception) {
                    Log.d(TAG, "구토 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.vomitRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun barYearChartGraph(v : View, pieChart: PieChart, valList : MutableMap<String, Float>) {

        pieChart.extraBottomOffset = 15f // 간격
        pieChart.description.isEnabled = false // chart 밑에 description 표시 유무
        pieChart.setTouchEnabled(false)

        pieChart.run {
            description.isEnabled = false

            setTouchEnabled(false)
            legend.isEnabled = false

            setEntryLabelTextSize(10f)
            setEntryLabelColor(Color.parseColor("#000000"))
        }

        var entries : ArrayList<PieEntry> = ArrayList()
        for((key, value) in valList.entries) {
            if(value > 0) {
                when (key) {
                    "transparent" -> entries.add(PieEntry(value, "투명 무색"))
                    "bubble" -> entries.add(PieEntry(value, "흰색 거품"))
                    "food" -> entries.add(PieEntry(value, "음식"))
                    "yellow" -> entries.add(PieEntry(value, "노랑"))
                    "leaf" -> entries.add(PieEntry(value, "잎 초록"))
                    "pink" -> entries.add(PieEntry(value, "분홍"))
                    "brown" -> entries.add(PieEntry(value, "짙은 갈색"))
                    "green" -> entries.add(PieEntry(value, "녹색"))
                    "substance" -> entries.add(PieEntry(value, "이물질"))
                    "red" -> entries.add(PieEntry(value, "붉은색"))
                }
            }
        }

        var depenses = PieDataSet(entries, "")
        with(depenses) {
            sliceSpace = 3f
            selectionShift = 5f
            valueTextSize = 5f
        }
        depenses.valueFormatter = CustomFormatter()
        depenses.color = Color.parseColor("#778899")

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
            val des = Description()
            des.text = "구토"
            des.textSize = 12f
            description = des
        }
    }

    class CustomFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val waterWeight = value.toString().split(".")
            return waterWeight[0] + "회"
        }
    }
}