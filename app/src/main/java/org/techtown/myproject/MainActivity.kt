package org.techtown.myproject

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.my.MyFragment
import org.techtown.myproject.community.CommunityFragment
import org.techtown.myproject.my.MyActivity
import org.techtown.myproject.note.NoteFragment
import org.techtown.myproject.note.RecordFragment
import org.techtown.myproject.receipt.ReceiptFragment
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.walk.WalkFragment
import com.google.firebase.database.DataSnapshot as DataSnapshot1

class MainActivity : AppCompatActivity() {

    private val TAG : String = MainActivity::class.java.simpleName

    lateinit var navMenu : ImageView
    lateinit var navigationView : NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var navHeaderView : View
    private lateinit var headerEmailText : TextView
    private lateinit var headerDogName : TextView
    lateinit var toolbar : Toolbar

    lateinit var sharedPreferences: SharedPreferences
    lateinit var mDatabaseReference: DatabaseReference
    lateinit var editor : SharedPreferences.Editor
    lateinit var mAuth : FirebaseAuth
    lateinit var uid : String
    private val prefUserEmail = "userEmail"
    lateinit var email : String
    lateinit var dogName : String
    lateinit var userName : String

    lateinit var profileFile : String

    lateinit var bnv_main : BottomNavigationView

    private val noteFragment by lazy { NoteFragment() }
    private val recordFragment by lazy { RecordFragment() }
    private val receiptFragment by lazy { ReceiptFragment() }
    private val walkFragment by lazy { WalkFragment() }
    private val communityFragment by lazy { CommunityFragment() }
    private val myFragment by lazy { MyFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "MainActivity - onCreate() called")

        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser?.uid.toString()

        mDatabaseReference = FBRef.userRef.child(uid)

        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        editor.putString("manager", "test1@test.com")
        editor.commit()

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)

            editor = sharedPreferences.edit()
            editor.putString(uid + "Token", token)
            editor.commit()

            Log.d("getToken", msg)
        })

        bnv_main = findViewById(R.id.bottom_menu)
        initNavigationBar()
    }

    /* private fun getUserDB() {
        mDatabaseReference = Firebase.database.reference.child("Users").child(uid)

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot1) {
                val post = dataSnapshot.getValue(UserInfo::class.java)
                dogName = post!!.dogName
                Log.d(TAG, "dogName: $dogName")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        mDatabaseReference.addValueEventListener(postListener)
    } */

    private fun initNavigationBar() { // 하단 탭에 맞는 fragment 띄우기
        bnv_main.run {
            setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.note_tab -> {
                        // changeFragment(noteFragment)
                        changeFragment(recordFragment)
                    }
                    R.id.receipt_tab -> {
                        changeFragment(receiptFragment)
                    }
                    R.id.walk_tab -> {
                        changeFragment(walkFragment)
                    }
                    R.id.community_tab -> {
                        changeFragment(communityFragment)
                    }
                    R.id.my_tab -> {
                        changeFragment(myFragment)
                    }
                }
                true
            }
            selectedItemId = R.id.note_tab
        }
    }

    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame, fragment).commit()
    }


    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000) {
            if (resultCode == RESULT_OK) {
                val userEmail = data?.getStringExtra("userEmail");
                Toast.makeText(this, "USER EMAIL: $userEmail", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "로그인 오류 발생", Toast.LENGTH_LONG).show()
            } else {
            }
        }
    }
}