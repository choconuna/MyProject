package org.techtown.myproject

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
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
import org.techtown.myproject.utils.DogDungModel
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.FCMToken
import org.techtown.myproject.utils.UserLocationModel
import org.techtown.myproject.walk.WalkFragment
import java.io.IOException
import com.google.firebase.database.DataSnapshot as DataSnapshot1

class MainActivity : AppCompatActivity() {

    private val TAG : String = MainActivity::class.java.simpleName

    private val LOCATION_PERMISSION_REQUEST_CODE = 800

    lateinit var sharedPreferences: SharedPreferences
    lateinit var mDatabaseReference: DatabaseReference
    lateinit var editor : SharedPreferences.Editor
    lateinit var mAuth : FirebaseAuth
    lateinit var uid : String
    lateinit var dogName : String
    lateinit var userName : String

    lateinit var profileFile : String

    lateinit var bnv_main : BottomNavigationView

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

            FBRef.tokenRef.child(uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot1) {
                    try {
                        // 경로에 대한 데이터 변경 발생 시 실행되는 코드
                        val data = dataSnapshot.getValue(FCMToken::class.java)
                        Log.d("tokenRef", data!!.toString())
                        if (data != null) {
                            Log.d("tokenRef", "data 존재")
                        } else if(data == null) {

                            Log.d("tokenRef", "data 존재 X")

                            val key = FBRef.tokenRef.child(uid).push().key.toString() // 키 값을 먼저 받아옴

                            FBRef.tokenRef.child(uid).child(key).setValue(FCMToken(uid, token, key)) // 토큰 정보 데이터베이스에 저장
                        }
                    } catch(e : Exception) { }
                }

                override fun onCancelled(error: DatabaseError) {
                    // 데이터 조회에 실패한 경우 실행되는 코드
                }
            })

            editor = sharedPreferences.edit()
            editor.putString(uid + "Token", token)
            editor.commit()

//            if (!sharedPreferences.contains(uid + "Location")) { // 앱에 처음 접속했을 때 사용자의 위치 정보를 가져와 저장
//                checkLocationPermission()
//            }

            checkLocationPermission()

            Log.d("getToken", msg)
        })

        bnv_main = findViewById(R.id.bottom_menu)
        initNavigationBar()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getLastLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    val g = Geocoder(this)
                    var address: MutableList<Address> = mutableListOf()

                    try {
                        address = g.getFromLocation(location.latitude, location.longitude, 20)
                        Log.d("getLocation", address.toString())
//                        Log.d("getLocation", address[0].getAddressLine(0).split(" ")[3])
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    if (address != null && address.isNotEmpty() && address[0].getAddressLine(0).split(" ").size > 3 && address[0].getAddressLine(0).split(" ")[2].last() == '구') {
                        var adminArea = address[0].adminArea
                        var subLocality = address[0].subLocality
//                        var thoroughfare = address[0].thoroughfare
                        var thoroughfare = address[0].getAddressLine(0).split(" ")[3]

                        sharedPreferences.edit()
                            .putString(uid + "Location", "$adminArea $subLocality $thoroughfare")
                            .apply()

                        sharedPreferences.edit()
                            .putString(uid + "SearchLocation", "$adminArea $subLocality $thoroughfare")
                            .apply()

                        FBRef.userLocationRef.child(uid).addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot1) {
                                var location = "$adminArea $subLocality $thoroughfare"
                                var dataExists = false

                                // dataSnapshot에서 모든 데이터를 반복
                                dataSnapshot.children.forEach { childSnapshot ->
                                    val data = childSnapshot.getValue(UserLocationModel::class.java)
                                    if (data?.id != null) {
                                        dataExists = true
                                        Log.d("userLocationRef", "data 존재")
                                        // id가 null이 아닌 경우 기존 데이터 업데이트
                                        FBRef.userLocationRef.child(uid).child(data.id).setValue(UserLocationModel(uid, data.id, location))
                                    }
                                }

                                if (!dataExists) {
                                    // 데이터가 없는 경우 새로운 키 생성
                                    Log.d("userLocationRef", "data 존재 X")
                                    val key = FBRef.userLocationRef.child(uid).push().key.toString()
                                    FBRef.userLocationRef.child(uid).child(key).setValue(UserLocationModel(uid, key, location))
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // 데이터 조회에 실패한 경우 실행되는 코드
                                Log.d("userLocationRefError", error.toString())
                            }
                        })
                    }
                }
            }
    }

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