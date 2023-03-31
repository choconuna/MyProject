package org.techtown.myproject.note

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import org.techtown.myproject.R
import org.techtown.myproject.community.PagerFragmentStateAdapter
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef


class NoteFragment: Fragment() {

    private val TAG = NoteFragment::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences

    lateinit var calendarView : MaterialCalendarView
    private var decorator = Decorator()

    lateinit var nowDate : CalendarDay

    lateinit var mDatabaseReference: DatabaseReference
    lateinit var mAuth : FirebaseAuth
    lateinit var uid : String // 사용자 uid

    lateinit var mainDogId : String // 대표 반려견 id
    lateinit var dogProfileImageArea : ImageView
    lateinit var dogProfileImage : String
    lateinit var dogNameArea : TextView

    lateinit var noteGroup : RadioGroup
    private var noteCategory : String = "식단"

    lateinit var noteView : LinearLayout

    lateinit var tab_main : TabLayout
    lateinit var viewPager : ViewPager2

    lateinit var bundle: Bundle

    private val eatFragment by lazy { EatFragment() }
    private val healthFragment by lazy { HealthFragment() }
    private val medicineFragment by lazy { MedicineFragment() }
    private val checkUpFragment by lazy { CheckUpFragment() }
    private val statisticsFragment by lazy { StatisticsFragment() }

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

        dogNameArea = v!!.findViewById(R.id.dogNameArea)
        dogProfileImageArea = v!!.findViewById(R.id.dogProfileImage)
        setHeader(v)

        calendarView = v!!.findViewById(R.id.calendarView)
        setCalendarSet(v, calendarView)
        calendarView.selectedDate = CalendarDay.today()

        calendarView.setOnDateChangedListener { widget, date, selected ->
            nowDate = date
            v!!.findViewById<TextView>(R.id.date).text = nowDate.year.toString() + "." + (nowDate.month + 1).toString() + "." +nowDate.day.toString()
            bundle = Bundle(1)
            bundle.putString("nowDate", nowDate.year.toString() + "." + (nowDate.month + 1).toString() + "." +nowDate.day.toString())
            setDate(v, bundle)
        }

        val todayBtn = v.findViewById<Button>(R.id.todayBtn)
        todayBtn.setOnClickListener {
            calendarView.selectedDate = CalendarDay.today()
            nowDate = CalendarDay.today()
            v!!.findViewById<TextView>(R.id.date).text =
                nowDate.year.toString() + "." + (nowDate.month + 1).toString() + "." + nowDate.day.toString()
            bundle = Bundle(1)
            bundle.putString("nowDate", nowDate.year.toString() + "." + (nowDate.month + 1).toString() + "." +nowDate.day.toString())
            eatFragment.arguments = bundle
            setDate(v, bundle)
        }
        // viewPager = v!!.findViewById(R.id.viewpager)
        // tab_main = v!!.findViewById(R.id.tabs)

        noteView = v!!.findViewById(R.id.noteView)

        noteGroup = v!!.findViewById(R.id.categoryGroup)

        setView(v)

        return v
    }

    private fun setHeader(v : View) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val post = dataSnapshot.getValue(DogModel::class.java)
                dogNameArea.text = post!!.dogName

                dogProfileImage = post!!.dogProfileFile

                val profileFile =
                    Firebase.storage.reference.child(dogProfileImage).downloadUrl.addOnCompleteListener(
                        OnCompleteListener { task ->
                            if(task.isSuccessful) {
                                Glide.with(v.context).load(task.result).into(dogProfileImageArea!!)
                            } else {
                                v.findViewById<ImageView>(R.id.dogProfileImage)!!.isVisible = false
                            }
                        })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dogRef.child(uid).child(mainDogId).addValueEventListener(postListener)
    }

    private fun setCalendarSet(v : View, calendarView: MaterialCalendarView) { // 달력을 현재 날짜에 맞게 세팅
        calendarView.selectedDate = CalendarDay.today()
        calendarView.state().edit().isCacheCalendarPositionEnabled(false).setCalendarDisplayMode(CalendarMode.MONTHS).commit()
        calendarView.showOtherDates = MaterialCalendarView.SHOW_NONE
        calendarView.addDecorators(decorator)

        val date = calendarView.selectedDate
        nowDate = date
        v!!.findViewById<TextView>(R.id.date).text = nowDate.year.toString() + "." + (nowDate.month + 1).toString() + "." +nowDate.day.toString()
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
                R.id.statistics -> {
                    noteCategory = "통계"
                    changeFragment(statisticsFragment)
                    noteGroup.check(v.findViewById<RadioButton>(R.id.statistics).id)
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
            "통계" -> {
                noteGroup.check(v.findViewById<RadioButton>(R.id.statistics).id)
                changeFragment(statisticsFragment)
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
            "통계" -> {
                noteGroup.check(v.findViewById<RadioButton>(R.id.statistics).id)
                val statisticsFragment = StatisticsFragment()
                statisticsFragment.arguments = bundle
                changeFragment(statisticsFragment)
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

    /* override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val pagerAdapter = PagerFragmentStateAdapter(requireActivity())

        // 4개의 fragment add
        pagerAdapter.addFragment(eatFragment)
        pagerAdapter.addFragment(healthFragment)
        pagerAdapter.addFragment(medicineFragment)
        pagerAdapter.addFragment(checkUpFragment)
        pagerAdapter.addFragment(statisticsFragment)

        // adapter
        viewPager.adapter = pagerAdapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int){
                super.onPageSelected(position)
                Log.e("ViewPagerFragment", "Page ${position+1}")
            }
        })
        // tablayout attach
        val tabTitles = listOf<String>("식단", "건강", "투약", "검사", "통계")
        TabLayoutMediator(tab_main, viewPager){ tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    } */
}