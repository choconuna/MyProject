package org.techtown.myproject.note

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
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
import org.techtown.myproject.R
import org.techtown.myproject.community.PagerFragmentStateAdapter
import org.techtown.myproject.note_search.SearchNoteActivity
import org.techtown.myproject.statistics.StatisticsFragment
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef

class RecordFragment : Fragment() {

    lateinit var tab_main : TabLayout
    lateinit var viewPager : ViewPager2

    private val TAG = RecordFragment::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences

    lateinit var nowDate : CalendarDay

    lateinit var mDatabaseReference: DatabaseReference
    lateinit var mAuth : FirebaseAuth
    lateinit var uid : String // 사용자 uid

    lateinit var mainDogId : String // 대표 반려견 id
    lateinit var dogProfileImageArea : ImageView
    lateinit var dogProfileImage : String
    lateinit var dogNameArea : TextView

    private val noteFragment by lazy { NoteFragment() }
    private val statisticsFragment by lazy { StatisticsFragment() }
    private val weeklyReportFragment by lazy { WeeklyReportFragment() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v : View? = inflater.inflate(R.layout.fragment_record, container, false)

        viewPager = v!!.findViewById(R.id.viewpager)
        tab_main = v!!.findViewById(R.id.tabs)

        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser?.uid.toString()
        mDatabaseReference = FBRef.userRef.child(uid)
        sharedPreferences = v!!.context.getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        mainDogId = sharedPreferences.getString(uid, "").toString()

        dogNameArea = v!!.findViewById(R.id.dogNameArea)
        dogProfileImageArea = v!!.findViewById(R.id.dogProfileImage)
        setHeader(v)

        val searchBtn = v!!.findViewById<ImageView>(R.id.searchBtn)
        searchBtn.setOnClickListener {
            val intent = Intent(v!!.context, SearchNoteActivity::class.java)
            v!!.context.startActivity(intent)
        }

        return v
    }

    private fun setHeader(v : View) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val post = dataSnapshot.getValue(DogModel::class.java)
                dogNameArea.text = post!!.dogName

                dogProfileImage = post!!.dogProfileFile

                val profileFile = Firebase.storage.reference.child(dogProfileImage).downloadUrl.addOnCompleteListener(
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val pagerAdapter = PagerFragmentStateAdapter(requireActivity())
        // 3개의 fragment add
        pagerAdapter.addFragment(noteFragment)
        pagerAdapter.addFragment(statisticsFragment)
        pagerAdapter.addFragment(weeklyReportFragment)

        // adapter
        viewPager.adapter = pagerAdapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int){
                super.onPageSelected(position)
                Log.e("ViewPagerFragment", "Page ${position+1}")
            }
        })
        // tablayout attach
        val tabTitles = listOf<String>("기록", "통계", "주간 리포트")
        TabLayoutMediator(tab_main, viewPager){ tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
}