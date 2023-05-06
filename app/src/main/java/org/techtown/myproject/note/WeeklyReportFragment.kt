package org.techtown.myproject.note

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.utils.*
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*

class WeeklyReportFragment : Fragment() {

    private val TAG = WeeklyReportFragment::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    private lateinit var myUid : String
    private lateinit var dogId : String

    private lateinit var currentWeekTextView: TextView

    private lateinit var todayBtn : TextView

    private lateinit var backWeekBtn : Button
    private lateinit var nextWeekBtn : Button

    private lateinit var weekFields : WeekFields
    private lateinit var weekStartTextView: TextView
    private lateinit var weekEndTextView: TextView
    private lateinit var currentDate: LocalDate
    private lateinit var startOfWeek: LocalDate
    private lateinit var endOfWeek: LocalDate

    private lateinit var showMealArea : LinearLayout // 사료 기록 있을 시 보여줌
    private lateinit var mealWeightArea : TextView // 먹인 하루 평균 사료양
    private lateinit var showNoMealArea : LinearLayout // 사료 기록 없을 시 보여줌
    private lateinit var mealList : TextView // 사료 리스트

    private lateinit var showSnackArea : LinearLayout // 간식 기록 있을 시 보여줌
    private lateinit var snackWeightArea : TextView // 먹인 하루 간식 사료양
    private lateinit var showNoSnackArea : LinearLayout // 간식 기록 없을 시 보여줌
    private lateinit var snackList : TextView // 간식 리스트

    private lateinit var showTonicArea : LinearLayout // 영양제 기록 있을 시 보여줌
    private lateinit var tonicWeightArea : TextView // 먹인 하루 평균 사료양
    private lateinit var showNoTonicArea : LinearLayout // 영양제 기록 없을 시 보여줌
    private lateinit var tonicList : TextView // 영양제 리스트

    private lateinit var showWaterArea : LinearLayout // 물 기록 있을 시 보여줌
    private lateinit var waterWeightArea : TextView // 먹인 하루 평균 물
    private lateinit var showNoWaterArea : LinearLayout // 물 기록 없을 시 보여줌
    private lateinit var waterMoreLess : TextView // 물의 양 통계

    private lateinit var showPeeArea : LinearLayout // 소변 기록 있을 시 보여줌
    private lateinit var peeNumArea : TextView // 하루 평균 소변
    private lateinit var showNoPeeArea : LinearLayout // 소변 기록 없을 시 보여줌
    private lateinit var normalPeeList : TextView // 소변 리스트
    private lateinit var badPeeList :  TextView

    private lateinit var showDungArea : LinearLayout // 대변 기록 있을 시 보여줌
    private lateinit var dungNumArea : TextView // 하루 평균 대변
    private lateinit var showNoDungArea : LinearLayout // 소변 기록 없을 시 보여줌
    private lateinit var normalDungList : TextView // 대변 리스트
    private lateinit var badDungList :  TextView

    private lateinit var showVomitArea : LinearLayout // 구토 기록 있을 시 보여줌
    private lateinit var vomitNumArea : TextView // 일주일 구토 횟수
    private lateinit var showNoVomitArea : LinearLayout // 구토 기록 없을 시 보여줌
    private lateinit var warnVomitList : TextView // 구토 리스트
    private lateinit var badVomitList : TextView

    private lateinit var showHeartArea : LinearLayout // 호흡수 기록 있을 시 보여줌
    private lateinit var heartNumArea : TextView // 일주일 평균 호흡수
    private lateinit var showNoHeartArea : LinearLayout // 호흡 기록 없을 시 보여줌
    private lateinit var heartMoreNormalShow : FrameLayout
    private lateinit var heartMore : TextView // 호흡수가 30회 초과인지 보여줌
    private lateinit var heartNormal : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v : View? = inflater.inflate(R.layout.fragment_weekly_report, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = v!!.context.getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(myUid, "").toString() // 현재 대표 반려견의 id
        Log.d("mainDogId", dogId)

        setData(v!!)

        todayBtn.setOnClickListener {
            currentDate = LocalDate.now()

            startOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            endOfWeek = currentDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

            // 현재 주차와 주차의 시작 날짜와 끝 날짜 보여주기
            val weekOfMonth = startOfWeek.get(weekFields.weekOfMonth())
            currentWeekTextView.text = "${currentDate.monthValue}월 ${weekOfMonth}주차"
            weekStartTextView.text = "${startOfWeek.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))}"
            weekEndTextView.text = "${endOfWeek.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))}"

            getEatData()

            getHealth()
        }

        backWeekBtn.setOnClickListener {
            startOfWeek = startOfWeek.minusWeeks(1)
            endOfWeek = endOfWeek.minusWeeks(1)

            val currentWeekStart = startOfWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val currentWeekOfMonth = currentWeekStart.get(weekFields.weekOfMonth())

            currentWeekTextView.text = "${startOfWeek.monthValue}월 ${currentWeekOfMonth}주차"
            weekStartTextView.text = "${startOfWeek.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))}"
            weekEndTextView.text = "${endOfWeek.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))}"

            getEatData()

            getHealth()
        }

        nextWeekBtn.setOnClickListener {
            startOfWeek = startOfWeek.plusWeeks(1)
            endOfWeek = endOfWeek.plusWeeks(1)

            val currentWeekStart = startOfWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val currentWeekOfMonth = currentWeekStart.get(weekFields.weekOfMonth())

            currentWeekTextView.text = "${startOfWeek.monthValue}월 ${currentWeekOfMonth}주차"
            weekStartTextView.text = "${startOfWeek.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))}"
            weekEndTextView.text = "${endOfWeek.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))}"

            getEatData()

            getHealth()
        }

        return v
    }

    private fun setData(v : View) {
        currentWeekTextView = v.findViewById(R.id.currentWeekTextView)

        todayBtn = v.findViewById(R.id.todayBtn)

        backWeekBtn = v.findViewById(R.id.backWeekBtn)
        nextWeekBtn = v.findViewById(R.id.nextWeekBtn)

        weekStartTextView = v.findViewById(R.id.weekStartTextView)
        weekEndTextView = v.findViewById(R.id.weekEndTextView)

        currentDate = LocalDate.now()

        // 현재 날짜가 속한 주의 월요일과 일요일 구하기
        weekFields = WeekFields.of(Locale.KOREA)
        startOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        endOfWeek = currentDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        // 현재 주차와 주차의 시작 날짜와 끝 날짜 보여주기
        val weekOfMonth = startOfWeek.get(weekFields.weekOfMonth())
        currentWeekTextView.text = "${currentDate.monthValue}월 ${weekOfMonth}주차"
        weekStartTextView.text = "${startOfWeek.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))}"
        weekEndTextView.text = "${endOfWeek.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))}"

        showMealArea = v.findViewById(R.id.showMealArea)
        mealWeightArea = v.findViewById(R.id.mealWeightArea)
        showNoMealArea = v.findViewById(R.id.showNoMealArea)
        mealList = v.findViewById(R.id.mealList)

        showSnackArea = v.findViewById(R.id.showSnackArea)
        snackWeightArea = v.findViewById(R.id.snackWeightArea)
        showNoSnackArea = v.findViewById(R.id.showNoSnackArea)
        snackList = v.findViewById(R.id.snackList)

        showTonicArea = v.findViewById(R.id.showTonicArea)
        tonicWeightArea = v.findViewById(R.id.tonicWeightArea)
        showNoTonicArea = v.findViewById(R.id.showNoTonicArea)
        tonicList = v.findViewById(R.id.tonicList)

        showWaterArea = v.findViewById(R.id.showWaterArea)
        waterWeightArea = v.findViewById(R.id.waterWeightArea)
        showNoWaterArea = v.findViewById(R.id.showNoWaterArea)
        waterMoreLess = v.findViewById(R.id.waterMoreLess)

        showPeeArea = v.findViewById(R.id.showPeeArea)
        peeNumArea = v.findViewById(R.id.peeNumArea)
        showNoPeeArea = v.findViewById(R.id.showNoPeeArea)
        normalPeeList = v.findViewById(R.id.normalPeeList)
        badPeeList = v.findViewById(R.id.badPeeList)

        showDungArea = v.findViewById(R.id.showDungArea)
        dungNumArea = v.findViewById(R.id.dungNumArea)
        showNoDungArea = v.findViewById(R.id.showNoDungArea)
        normalDungList = v.findViewById(R.id.normalDungList)
        badDungList = v.findViewById(R.id.badDungList)

        showVomitArea = v.findViewById(R.id.showVomitArea)
        vomitNumArea = v.findViewById(R.id.vomitNumArea)
        showNoVomitArea = v.findViewById(R.id.showNoVomitArea)
        warnVomitList = v.findViewById(R.id.warnVomitList)
        badVomitList = v.findViewById(R.id.badVomitList)

        showHeartArea = v.findViewById(R.id.showHeartArea)
        heartNumArea = v.findViewById(R.id.heartNumArea)
        showNoHeartArea = v.findViewById(R.id.showNoHeartArea)
        heartMoreNormalShow = v.findViewById(R.id.heartMoreNormalShow)
        heartMore = v.findViewById(R.id.heartMore)
        heartNormal = v.findViewById(R.id.heartNormal)

        getEatData()

        getHealth()
    }

    private fun getEatData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {

                    var totalMeal = 0
                    var weekMap : MutableMap<LocalDate, Int> = mutableMapOf()
                    var mealMap : MutableMap<String, Int> = mutableMapOf()

                    val startDate = LocalDate.parse(weekStartTextView.text, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))
                    val endDate = LocalDate.parse(weekEndTextView.text, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))

                    Log.d("showWeekly", "$startDate $endDate")

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogMealModel::class.java)
                        val dateSp = item!!.date.split(".")
                        lateinit var parsedDate : LocalDate

                        if(dateSp[1].length == 1 && dateSp[2].length == 1) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.M.d"))
                        } else if(dateSp[1].length == 1 && dateSp[2].length == 2) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.M.dd"))
                        } else if(dateSp[1].length == 2 && dateSp[2].length == 1) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.MM.d"))
                        } else if(dateSp[1].length == 2 && dateSp[2].length == 2) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        }

                        if(isBetweenDates(parsedDate, startDate, endDate)) {
                            totalMeal += item!!.mealWeight.toInt()
                            weekMap[parsedDate] = 0
                            mealMap[item!!.mealName] = 0
                        }
                    }

                    Log.d("weeklyMap", weekMap.toString())
                    Log.d("weeklyMap", totalMeal.toString())
                    Log.d("weeklyMap", mealMap.toString())

                    if(totalMeal == 0) {
                        showMealArea.visibility = GONE
                        showNoMealArea.visibility = VISIBLE
                        mealList.visibility = GONE
                    } else {
                        mealWeightArea.text = (totalMeal / weekMap.size).toString() + "g"
                        showMealArea.visibility = VISIBLE
                        showNoMealArea.visibility = GONE

                        mealList.text = ""

                        for ((key, value) in mealMap.entries) {
                            Log.d("weeklyMap", "key = $key")
                            if (key == mealMap.keys.last()) { // 마지막 요소일 경우
                                mealList.text = mealList.text.toString() + key
                            } else {
                                mealList.text = mealList.text.toString() + key + " ∙ "
                            }
                        }
                        mealList.visibility = VISIBLE
                    }

                } catch (e: Exception) {
                    Log.d(TAG, "사료 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.mealRef.child(myUid).child(dogId).addValueEventListener(postListener)

        val postListener2 = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    var totalMl = 0
                    var totalUn = 0
                    var totalG = 0
                    var mlMap : MutableMap<LocalDate, Int> = mutableMapOf()
                    var unMap : MutableMap<LocalDate, Int> = mutableMapOf()
                    var gMap : MutableMap<LocalDate, Int> = mutableMapOf()
                    var snackMap : MutableMap<String, Int> = mutableMapOf()

                    val startDate = LocalDate.parse(weekStartTextView.text, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))
                    val endDate = LocalDate.parse(weekEndTextView.text, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogSnackModel::class.java)
                        val dateSp = item!!.date.split(".")
                        lateinit var parsedDate : LocalDate

                        if(dateSp[1].length == 1 && dateSp[2].length == 1) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.M.d"))
                        } else if(dateSp[1].length == 1 && dateSp[2].length == 2) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.M.dd"))
                        } else if(dateSp[1].length == 2 && dateSp[2].length == 1) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.MM.d"))
                        } else if(dateSp[1].length == 2 && dateSp[2].length == 2) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        }

                        if(isBetweenDates(parsedDate, startDate, endDate)) {
                            when (item!!.snackUnit) {
                                "ml" -> {
                                    totalMl += item!!.snackWeight.toInt()
                                    mlMap[parsedDate] = 0
                                }
                                "개" -> {
                                    totalUn += item!!.snackWeight.toInt()
                                    unMap[parsedDate] = 0
                                }
                                "g" -> {
                                    totalG += item!!.snackWeight.toInt()
                                    gMap[parsedDate] = 0
                                }
                            }

                            snackMap[item!!.snackName] = 0
                        }

                    }

                    if(totalMl == 0 && totalUn == 0 && totalG == 0) {
                        showSnackArea.visibility = GONE
                        showNoSnackArea.visibility = VISIBLE
                        snackList.visibility = GONE
                    } else {
                        snackWeightArea.text = ""
                        if(totalMl > 0)
                            snackWeightArea.text = snackWeightArea.text.toString() + (totalMl / mlMap.size).toString() + "ml "
                        if(totalUn > 0)
                            snackWeightArea.text = snackWeightArea.text.toString() + (totalUn / unMap.size).toString() + "개 "
                        if(totalG > 0)
                            snackWeightArea.text = snackWeightArea.text.toString() + (totalG / gMap.size).toString() + "g "

                        showSnackArea.visibility = VISIBLE
                        showNoSnackArea.visibility = GONE

                        snackList.text = ""

                        for ((key, value) in snackMap.entries) {
                            if (key == snackMap.keys.last()) { // 마지막 요소일 경우
                                snackList.text = snackList.text.toString() + key
                            } else {
                                snackList.text = snackList.text.toString() + key + " ∙ "
                            }
                        }
                        snackList.visibility = VISIBLE
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
        FBRef.snackRef.child(myUid).child(dogId).addValueEventListener(postListener2)

        val postListener3 = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    var totalMl = 0
                    var totalUn = 0
                    var totalG = 0
                    var mlMap : MutableMap<LocalDate, Int> = mutableMapOf()
                    var unMap : MutableMap<LocalDate, Int> = mutableMapOf()
                    var gMap : MutableMap<LocalDate, Int> = mutableMapOf()
                    var tonicMap : MutableMap<String, Int> = mutableMapOf()

                    val startDate = LocalDate.parse(weekStartTextView.text, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))
                    val endDate = LocalDate.parse(weekEndTextView.text, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogTonicModel::class.java)
                        val dateSp = item!!.date.split(".")
                        lateinit var parsedDate : LocalDate

                        if(dateSp[1].length == 1 && dateSp[2].length == 1) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.M.d"))
                        } else if(dateSp[1].length == 1 && dateSp[2].length == 2) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.M.dd"))
                        } else if(dateSp[1].length == 2 && dateSp[2].length == 1) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.MM.d"))
                        } else if(dateSp[1].length == 2 && dateSp[2].length == 2) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        }

                        if(isBetweenDates(parsedDate, startDate, endDate)) {
                            when (item!!.tonicUnit) {
                                "ml" -> {
                                    totalMl += item!!.tonicWeight.toInt()
                                    mlMap[parsedDate] = 0
                                }
                                "개" -> {
                                    totalUn += item!!.tonicWeight.toInt()
                                    unMap[parsedDate] = 0
                                }
                                "g" -> {
                                    totalG += item!!.tonicWeight.toInt()
                                    gMap[parsedDate] = 0
                                }
                            }

                            tonicMap[item!!.tonicName] = 0
                        }

                    }

                    if(totalMl == 0 && totalUn == 0 && totalG == 0) {
                        showTonicArea.visibility = GONE
                        showNoTonicArea.visibility = VISIBLE
                        tonicList.visibility = GONE
                    } else {
                        tonicWeightArea.text = ""
                        if(totalMl > 0)
                            tonicWeightArea.text = tonicWeightArea.text.toString() + (totalMl / mlMap.size).toString() + "ml  "
                        if(totalUn > 0)
                            tonicWeightArea.text = tonicWeightArea.text.toString() + (totalUn / unMap.size).toString() + "개  "
                        if(totalG > 0)
                            tonicWeightArea.text = tonicWeightArea.text.toString() + (totalG / gMap.size).toString() + "g"

                        showTonicArea.visibility = VISIBLE
                        showNoTonicArea.visibility = GONE

                        tonicList.text = ""

                        for ((key, value) in tonicMap.entries) {
                            if (key == tonicMap.keys.last()) { // 마지막 요소일 경우
                                tonicList.text = tonicList.text.toString() + key
                            } else {
                                tonicList.text = tonicList.text.toString() + key + " ∙ "
                            }
                        }
                        tonicList.visibility = VISIBLE
                    }


                } catch (e: Exception) {
                    Log.d(TAG, "영양제 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.tonicRef.child(myUid).child(dogId).addValueEventListener(postListener3)

        val postListener4 = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {

                    var totalWater = 0
                    var weekMap : MutableMap<LocalDate, Int> = mutableMapOf()

                    val startDate = LocalDate.parse(weekStartTextView.text, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))
                    val endDate = LocalDate.parse(weekEndTextView.text, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))

                    Log.d("showWeekly", "$startDate $endDate")

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogWaterModel::class.java)
                        val dateSp = item!!.date.split(".")
                        lateinit var parsedDate : LocalDate

                        if(dateSp[1].length == 1 && dateSp[2].length == 1) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.M.d"))
                        } else if(dateSp[1].length == 1 && dateSp[2].length == 2) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.M.dd"))
                        } else if(dateSp[1].length == 2 && dateSp[2].length == 1) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.MM.d"))
                        } else if(dateSp[1].length == 2 && dateSp[2].length == 2) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        }

                        if(isBetweenDates(parsedDate, startDate, endDate)) {
                            totalWater += item!!.waterWeight.toInt()
                            weekMap[parsedDate] = 0
                        }
                    }

                    val dogWeight = FBRef.dogRef.child(myUid).child(dogId).child("dogWeight").get().addOnSuccessListener {
                        val w = it.value.toString().toInt()
                        var minWeight = w*50
                        var maxWeight = w*60

                        Log.d("totalWater", totalWater.toString())
                        waterMoreLess.text = ""
                        Log.d("totalWater", weekMap.toString())

                        if (totalWater == 0) {
                            showWaterArea.visibility = GONE
                            showNoWaterArea.visibility = VISIBLE
                            waterMoreLess.text = "음수량이 최소 권장 음수량보다 " + (minWeight - totalWater).toString() + "ml 부족합니다."
                        } else {
                            if ((totalWater / weekMap.size) < minWeight) {
                                waterWeightArea.text = (totalWater / weekMap.size).toString() + "ml"
                                showWaterArea.visibility = VISIBLE
                                showNoWaterArea.visibility = GONE
                                waterMoreLess.text = "음수량이 최소 권장 음수량보다 " + (minWeight - (totalWater / weekMap.size)).toString() + "ml 부족합니다."
                            } else if ((totalWater / weekMap.size) in minWeight..maxWeight) {
                                waterWeightArea.text = (totalWater / weekMap.size).toString() + "ml"
                                showWaterArea.visibility = VISIBLE
                                showNoWaterArea.visibility = GONE
                                waterMoreLess.text = "음수량이 권장 음수량을 만족합니다."
                            } else if ((totalWater / weekMap.size) < minWeight || (totalWater / weekMap.size) > maxWeight) {
                                waterWeightArea.text = (totalWater / weekMap.size).toString() + "ml"
                                showWaterArea.visibility = VISIBLE
                                showNoWaterArea.visibility = GONE
                                waterMoreLess.text = "음수량이 최대 권장 음수량을 " + ((totalWater / weekMap.size) - maxWeight).toString() + "ml 초과했습니다."
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "물 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.waterRef.child(myUid).child(dogId).addValueEventListener(postListener4)
    }

    private fun getHealth() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {

                    var weekMap : MutableMap<LocalDate, Int> = mutableMapOf()
                    var totalPee = 0

                    var normalPee : MutableMap<String, Int> = mutableMapOf()
                    normalPee["투명한 무색 소변"] = 0
                    normalPee["투명한 노란색 소변"] = 0

                    var badPee : MutableMap<String, Int> = mutableMapOf()
                    badPee["주황색과 어두운 노란색 소변"] = 0
                    badPee["붉은색 소변"] = 0
                    badPee["갈색 소변"] = 0

                    val startDate = LocalDate.parse(weekStartTextView.text, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))
                    val endDate = LocalDate.parse(weekEndTextView.text, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogPeeModel::class.java)

                        val dateSp = item!!.date.split(".")
                        lateinit var parsedDate : LocalDate

                        if(dateSp[1].length == 1 && dateSp[2].length == 1) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.M.d"))
                        } else if(dateSp[1].length == 1 && dateSp[2].length == 2) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.M.dd"))
                        } else if(dateSp[1].length == 2 && dateSp[2].length == 1) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.MM.d"))
                        } else if(dateSp[1].length == 2 && dateSp[2].length == 2) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        }

                        if(isBetweenDates(parsedDate, startDate, endDate)) {
                            totalPee += item!!.peeCount.toInt()
                            weekMap[parsedDate] = 0

                            when (item!!.peeType) {
                                "transparent" -> normalPee["투명한 무색 소변"] = normalPee["투명한 무색 소변"]!!.plus(item!!.peeCount.toInt())
                                "lightYellow" -> normalPee["투명한 노란색 소변"] = normalPee["투명한 노란색 소변"]!!.plus(item!!.peeCount.toInt())
                                "darkYellow" -> badPee["주황색과 어두운 노란색 소변"] = badPee["주황색과 어두운 노란색 소변"]!!.plus(item!!.peeCount.toInt())
                                "red" -> badPee["붉은색 소변"] = badPee["붉은색 소변"]!!.plus(item!!.peeCount.toInt())
                                "brown" -> badPee["갈색 소변"] = badPee["갈색 소변"]!!.plus(item!!.peeCount.toInt())
                            }
                        }
                    }

                    Log.d("getPeeData", "$normalPee $badPee")

                    var normalExist = false
                    var badExist = false

                    if(totalPee == 0) {
                        showPeeArea.visibility = GONE
                        showNoPeeArea.visibility = VISIBLE
                        normalPeeList.visibility = GONE
                        badPeeList.visibility = GONE
                    } else {
                        peeNumArea.text = (totalPee / weekMap.size).toString() + "회"
                        showPeeArea.visibility = VISIBLE
                        showNoPeeArea.visibility = GONE

                        for ((key, value) in normalPee.entries) {
                            if(value > 0) {
                                normalExist = true
                                break
                            }
                        }

                        for ((key, value) in badPee.entries) {
                            if(value > 0) {
                                badExist = true
                                break
                            }
                        }

                        if(!normalExist)
                            normalPeeList.visibility = GONE
                        if(!badExist)
                            badPeeList.visibility = GONE

                        if(normalExist) {
                            normalPeeList.text = ""

                            for ((key, value) in normalPee.entries) {
                                if(value != 0) {
                                    normalPeeList.text = normalPeeList.text.toString() + key + ": " + value.toString() + "회\n"
                                }
                            }

                            normalPeeList.visibility = VISIBLE
                        }
                        if(badExist) {
                            badPeeList.text = ""

                            Log.d("getPeeData", "badExist 실행")

                            for ((key, value) in badPee.entries) {
                                if(value != 0) {
                                    badPeeList.text = badPeeList.text.toString() + key + ": " + value.toString() + "회\n"
                                }
                            }

                            badPeeList.visibility = VISIBLE
                        }
                    }


                } catch (e: Exception) {
                    Log.d(TAG, " 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.peeRef.child(myUid).child(dogId).addValueEventListener(postListener)

        val postListener2 = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    var weekMap : MutableMap<LocalDate, Int> = mutableMapOf()
                    var totalDung = 0

                    var normalDung : MutableMap<String, Int> = mutableMapOf()
                    normalDung["보통 변"] = 0
                    normalDung["묽은 변"] = 0

                    var badDung : MutableMap<String, Int> = mutableMapOf()
                    badDung["설사"] = 0
                    badDung["짙고 딱딱한 변"] = 0
                    badDung["붉은색 변"] = 0
                    badDung["검은색 변"] = 0
                    badDung["하얀색 점이 있는 변"] = 0

                    val startDate = LocalDate.parse(weekStartTextView.text, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))
                    val endDate = LocalDate.parse(weekEndTextView.text, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogDungModel::class.java)

                        val dateSp = item!!.date.split(".")
                        lateinit var parsedDate : LocalDate

                        if(dateSp[1].length == 1 && dateSp[2].length == 1) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.M.d"))
                        } else if(dateSp[1].length == 1 && dateSp[2].length == 2) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.M.dd"))
                        } else if(dateSp[1].length == 2 && dateSp[2].length == 1) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.MM.d"))
                        } else if(dateSp[1].length == 2 && dateSp[2].length == 2) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        }

                        if(isBetweenDates(parsedDate, startDate, endDate)) {
                            totalDung += item!!.dungCount.toInt()
                            weekMap[parsedDate] = 0

                            when(item!!.dungType) {
                                "regular" -> normalDung["보통 변"] = normalDung["보통 변"]!!.plus(item!!.dungCount.toInt())
                                "watery" -> normalDung["묽은 변"] = normalDung["묽은 변"]!!.plus(item!!.dungCount.toInt())
                                "diarrhea" -> badDung["설사"] = badDung["설사"]!!.plus(item!!.dungCount.toInt())
                                "hard" -> badDung["짙고 딱딱한 변"] = badDung["짙고 딱딱한 변"]!!.plus(item!!.dungCount.toInt())
                                "red" -> badDung["붉은색 변"] = badDung["붉은색 변"]!!.plus(item!!.dungCount.toInt())
                                "black" -> badDung["검은색 변"] = badDung["검은색 변"]!!.plus(item!!.dungCount.toInt())
                                "white" -> badDung["하얀색 점이 있는 변"] = badDung["하얀색 점이 있는 변"]!!.plus(item!!.dungCount.toInt())
                            }
                        }
                    }

                    var normalExist = false
                    var badExist = false

                    if(totalDung == 0) {
                        showDungArea.visibility = GONE
                        showNoDungArea.visibility = VISIBLE
                        normalDungList.visibility = GONE
                        badDungList.visibility = GONE
                    } else {
                        dungNumArea.text = (totalDung / weekMap.size).toString() + "회"
                        showDungArea.visibility = VISIBLE
                        showNoDungArea.visibility = GONE

                        for ((key, value) in normalDung.entries) {
                            if (value > 0) {
                                normalExist = true
                                break
                            }
                        }

                        for ((key, value) in badDung.entries) {
                            if (value > 0) {
                                badExist = true
                                break
                            }
                        }

                        if (!normalExist)
                            normalDungList.visibility = GONE
                        if (!badExist)
                            badDungList.visibility = GONE

                        if (normalExist) {
                            normalDungList.text = ""

                            for ((key, value) in normalDung.entries) {
                                if (value != 0) {
                                    normalDungList.text = normalDungList.text.toString() + key + ": " + value.toString() + "회\n"
                                }
                            }

                            normalDungList.visibility = VISIBLE
                        }

                        if (badExist) {
                            badDungList.text = ""

                            for ((key, value) in badDung.entries) {
                                if (value != 0) {
                                    badDungList.text = badDungList.text.toString() + key + ": " + value.toString() + "회\n"
                                }
                            }

                            badDungList.visibility = VISIBLE
                        }
                    }

                } catch (e: Exception) {
                    Log.d(TAG, "대변 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dungRef.child(myUid).child(dogId).addValueEventListener(postListener2)

        val postListener3 = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    var weekMap : MutableMap<LocalDate, Int> = mutableMapOf()
                    var totalVomit = 0

                    var warnVomit : MutableMap<String, Int> = mutableMapOf()
                    warnVomit["투명한 무색 구토"] = 0
                    warnVomit["흰색 거품이 섞인 구토"] = 0
                    warnVomit["음식이 섞인 구토"] = 0
                    warnVomit["노란색 구토"] = 0
                    warnVomit["잎사귀가 섞인 초록색 구토"] = 0
                    warnVomit["분홍색 구토"] = 0

                    var badVomit : MutableMap<String, Int> = mutableMapOf()
                    badVomit["짙은 갈색 구토"] = 0
                    badVomit["녹색 구토"] = 0
                    badVomit["이물질이 섞인 구토"] = 0
                    badVomit["붉은색 구토"] = 0

                    val startDate = LocalDate.parse(weekStartTextView.text, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))
                    val endDate = LocalDate.parse(weekEndTextView.text, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogVomitModel::class.java)

                        val dateSp = item!!.date.split(".")
                        lateinit var parsedDate : LocalDate

                        if(dateSp[1].length == 1 && dateSp[2].length == 1) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.M.d"))
                        } else if(dateSp[1].length == 1 && dateSp[2].length == 2) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.M.dd"))
                        } else if(dateSp[1].length == 2 && dateSp[2].length == 1) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.MM.d"))
                        } else if(dateSp[1].length == 2 && dateSp[2].length == 2) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        }

                        if(isBetweenDates(parsedDate, startDate, endDate)) {
                            totalVomit += item!!.vomitCount.toInt()
                            weekMap[parsedDate] = 0

                            when(item!!.vomitType) {
                                "transparent" -> warnVomit["투명한 무색 구토"] = warnVomit["투명한 무색 구토"]!!.plus(item!!.vomitCount.toInt())
                                "bubble" -> warnVomit["흰색 거품이 섞인 구토"] = warnVomit["흰색 거품이 섞인 구토"]!!.plus(item!!.vomitCount.toInt())
                                "food" -> warnVomit["음식이 섞인 구토"] = warnVomit["음식이 섞인 구토"]!!.plus(item!!.vomitCount.toInt())
                                "yellow" -> warnVomit["노란색 구토"] = warnVomit["노란색 구토"]!!.plus(item!!.vomitCount.toInt())
                                "leaf" -> warnVomit["잎사귀가 섞인 초록색 구토"] = warnVomit["잎사귀가 섞인 초록색 구토"]!!.plus(item!!.vomitCount.toInt())
                                "pink" -> warnVomit["분홍색 구토"] = warnVomit["분홍색 구토"]!!.plus(item!!.vomitCount.toInt())
                                "brown" -> badVomit["짙은 갈색 구토"] = badVomit["짙은 갈색 구토"]!!.plus(item!!.vomitCount.toInt())
                                "green" -> badVomit["녹색 구토"] = badVomit["녹색 구토"]!!.plus(item!!.vomitCount.toInt())
                                "substance" -> badVomit["이물질이 섞인 구토"] = badVomit["이물질이 섞인 구토"]!!.plus(item!!.vomitCount.toInt())
                                "red" -> badVomit["붉은색 구토"] = badVomit["붉은색 구토"]!!.plus(item!!.vomitCount.toInt())
                            }
                        }
                    }

                    var warnExist = false
                    var badExist = false

                    if(totalVomit == 0) {
                        showVomitArea.visibility = GONE
                        showNoVomitArea.visibility = VISIBLE
                        warnVomitList.visibility = GONE
                        badVomitList.visibility = GONE
                    } else {
                        vomitNumArea.text = (totalVomit / weekMap.size).toString() + "회"
                        showVomitArea.visibility = VISIBLE
                        showNoVomitArea.visibility = GONE

                        for ((key, value) in warnVomit.entries) {
                            if (value > 0) {
                                warnExist = true
                                break
                            }
                        }

                        for ((key, value) in badVomit.entries) {
                            if (value > 0) {
                                badExist = true
                                break
                            }
                        }

                        if (!warnExist)
                            warnVomitList.visibility = GONE
                        if (!badExist)
                            badVomitList.visibility = GONE

                        if (warnExist) {
                            warnVomitList.text = ""

                            for ((key, value) in warnVomit.entries) {
                                if (value != 0) {
                                    warnVomitList.text = warnVomitList.text.toString() + key + ": " + value.toString() + "회\n"
                                }
                            }

                            warnVomitList.visibility = VISIBLE
                        }

                        if (badExist) {
                            badVomitList.text = ""

                            for ((key, value) in badVomit.entries) {
                                if (value != 0) {
                                    badVomitList.text = badVomitList.text.toString() + key + ": " + value.toString() + "회\n"
                                }
                            }

                            badVomitList.visibility = VISIBLE
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
        FBRef.vomitRef.child(myUid).child(dogId).addValueEventListener(postListener3)

        val postListener4 = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    var weekMap : MutableList<LocalDate> = mutableListOf()
                    var totalHeart = 0

                    val startDate = LocalDate.parse(weekStartTextView.text, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))
                    val endDate = LocalDate.parse(weekEndTextView.text, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.KOREA))

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogHeartModel::class.java)

                        val dateSp = item!!.date.split(".")
                        lateinit var parsedDate : LocalDate

                        if(dateSp[1].length == 1 && dateSp[2].length == 1) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.M.d"))
                        } else if(dateSp[1].length == 1 && dateSp[2].length == 2) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.M.dd"))
                        } else if(dateSp[1].length == 2 && dateSp[2].length == 1) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.MM.d"))
                        } else if(dateSp[1].length == 2 && dateSp[2].length == 2) {
                            parsedDate = LocalDate.parse(item!!.date, DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        }

                        if(isBetweenDates(parsedDate, startDate, endDate)) {
                            totalHeart += item!!.heartCount.toInt()
                            weekMap.add(parsedDate)
                        }
                    }

                    if(totalHeart == 0) {
                        showHeartArea.visibility = GONE
                        showNoHeartArea.visibility = VISIBLE
                        heartMoreNormalShow.visibility = GONE
                    } else {
                        heartNumArea.text = (totalHeart / weekMap.size).toString() + "회"
                        showHeartArea.visibility = VISIBLE
                        showNoHeartArea.visibility = GONE
                        heartMoreNormalShow.visibility = VISIBLE

                        if(totalHeart / weekMap.size > 30) {
                            heartMore.visibility = VISIBLE
                            heartNormal.visibility = GONE
                        } else {
                            heartMore.visibility = GONE
                            heartNormal.visibility = VISIBLE
                        }
                    }

                } catch (e: Exception) {
                    Log.d(TAG, "호흡수 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.heartRef.child(myUid).child(dogId).addValueEventListener(postListener4)
    }

    fun isBetweenDates(date: LocalDate, startDate: LocalDate, endDate: LocalDate): Boolean {
        return (date.isEqual(startDate) || date.isAfter(startDate)) && (date.isEqual(endDate) || date.isBefore(endDate))
    }
}