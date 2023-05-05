package org.techtown.myproject.note

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import org.techtown.myproject.R
import org.techtown.myproject.utils.FBRef
import java.text.SimpleDateFormat
import java.util.*


class NoteFragment: Fragment() {

    private val TAG = NoteFragment::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences

    lateinit var scrollView : ScrollView

    lateinit var layoutDetail : MaterialCardView
    lateinit var layoutBtn : ImageView

    lateinit var calendarView : MaterialCalendarView
    private var decorator = Decorator()

    lateinit var nowDate : CalendarDay

    lateinit var mDatabaseReference: DatabaseReference
    lateinit var mAuth : FirebaseAuth
    lateinit var uid : String // 사용자 uid

    lateinit var mainDogId : String // 대표 반려견 id

    lateinit var noteGroup : RadioGroup
    private var noteCategory : String = "식단"

    lateinit var noteView : LinearLayout

    lateinit var bundle: Bundle

    private val eatFragment by lazy { EatFragment() }
    private val healthFragment by lazy { HealthFragment() }
    private val medicineFragment by lazy { MedicineFragment() }
    private val checkUpFragment by lazy { CheckUpFragment() }
    private val memoFragment by lazy { MemoFragment() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v : View? = inflater.inflate(R.layout.fragment_note, container, false)

        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser?.uid.toString()
        mDatabaseReference = FBRef.userRef.child(uid)
        sharedPreferences = v!!.context.getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        mainDogId = sharedPreferences.getString(uid, "").toString()

        scrollView = v!!.findViewById(R.id.scrollView)

        layoutDetail = v!!.findViewById(R.id.layoutDetail)
        layoutBtn = v!!.findViewById(R.id.layoutBtn)

        calendarView = v!!.findViewById(R.id.calendarView)
        setCalendarSet(v, calendarView)
        calendarView.selectedDate = CalendarDay.today()

        v!!.findViewById<MaterialCardView>(R.id.layout).setOnClickListener {
            if(layoutDetail.visibility == View.VISIBLE) {
                layoutDetail.visibility = View.GONE
                layoutBtn.animate().apply {
                    duration = 300
                    rotation(0f)
                }
            } else {
                layoutDetail.visibility = View.VISIBLE
                layoutBtn.animate().apply {
                    duration = 300
                    rotation(180f)
                }
            }
        }

        calendarView.setOnDateChangedListener { widget, date, selected ->
            nowDate = date

            val calendar = Calendar.getInstance() //선택한 날짜의 요일을 구하기 위한 calendar
            calendar.set(nowDate.year.toString().toInt(), (nowDate.month).toString().toInt(), nowDate.day.toString().toInt()) //선택한 날짜 세팅
            val dat = calendar.time
            val simpledateformat = SimpleDateFormat("E", Locale.getDefault())
            val dayName: String = simpledateformat.format(dat)

//            v!!.findViewById<TextView>(R.id.date).text = nowDate.year.toString() + "." + (nowDate.month + 1).toString() + "." +nowDate.day.toString()

            v!!.findViewById<TextView>(R.id.date).text =
                nowDate.year.toString() + "." + (nowDate.month + 1).toString() + "." + nowDate.day.toString() + " (" + dayName + ")"

            bundle = Bundle(1)
            bundle.putString("nowDate", nowDate.year.toString() + "." + (nowDate.month + 1).toString() + "." +nowDate.day.toString())
            setDate(v, bundle)

            scrollView.smoothScrollTo(0,0)
        }

        val todayBtn = v.findViewById<Button>(R.id.todayBtn)
        todayBtn.setOnClickListener {
            calendarView.selectedDate = CalendarDay.today()
            nowDate = CalendarDay.today()

            val calendar = Calendar.getInstance() //선택한 날짜의 요일을 구하기 위한 calendar
            calendar.set(nowDate.year.toString().toInt(), (nowDate.month).toString().toInt(), nowDate.day.toString().toInt()) //선택한 날짜 세팅
            val dat = calendar.time
            val simpledateformat = SimpleDateFormat("E", Locale.getDefault())
            val dayName: String = simpledateformat.format(dat)

            v!!.findViewById<TextView>(R.id.date).text =
                nowDate.year.toString() + "." + (nowDate.month + 1).toString() + "." + nowDate.day.toString() + " (" + dayName + ")"
            bundle = Bundle(1)
            bundle.putString("nowDate", nowDate.year.toString() + "." + (nowDate.month + 1).toString() + "." +nowDate.day.toString())
            eatFragment.arguments = bundle
            setDate(v, bundle)
        }

        noteView = v!!.findViewById(R.id.noteView)

        noteGroup = v!!.findViewById(R.id.categoryGroup)

        setView(v)

        return v
    }


    private fun setCalendarSet(v : View, calendarView: MaterialCalendarView) { // 달력을 현재 날짜에 맞게 세팅
        calendarView.selectedDate = CalendarDay.today()
        calendarView.state().edit().isCacheCalendarPositionEnabled(false).setCalendarDisplayMode(CalendarMode.MONTHS).commit()
        calendarView.showOtherDates = MaterialCalendarView.SHOW_NONE
        calendarView.addDecorators(decorator)

        val date = calendarView.selectedDate
        nowDate = date

        val calendar = Calendar.getInstance() //선택한 날짜의 요일을 구하기 위한 calendar
        calendar.set(nowDate.year.toString().toInt(), (nowDate.month).toString().toInt(), nowDate.day.toString().toInt()) //선택한 날짜 세팅
        val dat = calendar.time
        val simpledateformat = SimpleDateFormat("E", Locale.getDefault())
        val dayName: String = simpledateformat.format(dat)

//        v!!.findViewById<TextView>(R.id.date).text = nowDate.year.toString() + "." + (nowDate.month + 1).toString() + "." + nowDate.day.toString()
        v!!.findViewById<TextView>(R.id.date).text = nowDate.year.toString() + "." + (nowDate.month + 1).toString() + "." + nowDate.day.toString() + " (" + dayName + ")"
        bundle = Bundle(1)
        bundle.putString("nowDate", nowDate.year.toString() + "." + (nowDate.month + 1).toString() + "." +nowDate.day.toString())
        eatFragment.arguments = bundle
    }

    private fun setView(v : View) {
        noteGroup = v!!.findViewById(R.id.categoryGroup)
        noteGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.eat -> {
                    noteCategory = "식단"
                    changeFragment(eatFragment)
                    noteGroup.check(v.findViewById<RadioButton>(R.id.eat).id)
                }
                R.id.health -> {
                    noteCategory = "건강"
                    changeFragment(healthFragment)
                    noteGroup.check(v.findViewById<RadioButton>(R.id.health).id)
                }
                R.id.medicine -> {
                    noteCategory = "투약"
                    changeFragment(medicineFragment)
                    noteGroup.check(v.findViewById<RadioButton>(R.id.medicine).id)
                }
                R.id.checkUp -> {
                    noteCategory = "검사"
                    changeFragment(checkUpFragment)
                    noteGroup.check(v.findViewById<RadioButton>(R.id.checkUp).id)
                }
                R.id.memo -> {
                    noteCategory = "메모"
                    changeFragment(memoFragment)
                    noteGroup.check(v.findViewById<RadioButton>(R.id.memo).id)
                } else -> {
                noteCategory = "식단"
                noteGroup.check(v.findViewById<RadioButton>(R.id.eat).id)
                changeFragment(eatFragment)
            }
            }
        }

        when(noteCategory) {
            "식단" -> {
                noteGroup.check(v.findViewById<RadioButton>(R.id.eat).id)
                changeFragment(eatFragment)
            }
            "건강" -> {
                noteGroup.check(v.findViewById<RadioButton>(R.id.health).id)
                changeFragment(healthFragment)
            }
            "투약" -> {
                noteGroup.check(v.findViewById<RadioButton>(R.id.medicine).id)
                changeFragment(medicineFragment)
            }
            "검사" -> {
                noteGroup.check(v.findViewById<RadioButton>(R.id.checkUp).id)
                changeFragment(checkUpFragment)
            }
            "메모" -> {
                noteGroup.check(v.findViewById<RadioButton>(R.id.memo).id)
                changeFragment(memoFragment)
            }
            else -> {
                noteCategory = "식단"
                noteGroup.check(v.findViewById<RadioButton>(R.id.eat).id)
                changeFragment(eatFragment)
            }
        }
    }

    private fun changeFragment(fragment: Fragment) {
        fragment.arguments = bundle
        parentFragmentManager.beginTransaction().replace(R.id.noteView, fragment).commit()
    }

    private fun setDate(v : View, bundle : Bundle) {
        when(noteCategory) {
            "식단" -> {
                noteGroup.check(v.findViewById<RadioButton>(R.id.eat).id)
                val eatFragment = EatFragment()
                eatFragment.arguments = bundle
                changeFragment(eatFragment)
            }
            "건강" -> {
                noteGroup.check(v.findViewById<RadioButton>(R.id.health).id)
                val healthFragment = HealthFragment()
                healthFragment.arguments = bundle
                changeFragment(healthFragment)
            }
            "투약" -> {
                noteGroup.check(v.findViewById<RadioButton>(R.id.medicine).id)
                val medicineFragment = MedicineFragment()
                medicineFragment.arguments = bundle
                changeFragment(medicineFragment)
            }
            "검사" -> {
                noteGroup.check(v.findViewById<RadioButton>(R.id.checkUp).id)
                val checkUpFragment = CheckUpFragment()
                checkUpFragment.arguments = bundle
                changeFragment(checkUpFragment)
            }
            "메모" -> {
                noteGroup.check(v.findViewById<RadioButton>(R.id.memo).id)
                val memoFragment = MemoFragment()
                memoFragment.arguments = bundle
                changeFragment(memoFragment)
            }
            else -> {
                noteCategory = "식단"
                noteGroup.check(v.findViewById<RadioButton>(R.id.eat).id)
                val eatFragment = EatFragment()
                eatFragment.arguments = bundle
                changeFragment(eatFragment)
            }
        }
    }
}