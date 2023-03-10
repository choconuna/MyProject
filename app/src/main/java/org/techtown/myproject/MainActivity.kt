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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val TAG : String = "로그"

    lateinit var navMenu : ImageView
    lateinit var navigationView : NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var navHeaderView : View
    lateinit var headerEmailText : TextView
    lateinit var toolbar : Toolbar

    lateinit var sharedPreferences: SharedPreferences
    private val prefUserEmail = "userEmail"
    lateinit var email : String

    lateinit var bnv_main : BottomNavigationView

    private val noteFragment by lazy { NoteFragment() }
    private val receiptFragment by lazy { ReceiptFragment() }
    private val walkFragment by lazy { WalkFragment() }
    private val myFragment by lazy { MyFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "MainActivity - onCreate() called")

        toolbar  = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        setToolbar()

        initNavigationMenu()

        bnv_main = findViewById(R.id.bottom_menu)
        initNavigationBar()
    }

    private fun setToolbar() {
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu)
        supportActionBar!!.setDisplayShowTitleEnabled(false) // 타이틀 안 보이게 하기
    }

    private fun initNavigationMenu() { // 초기 네비베이션 메뉴 설정
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        navigationView.setNavigationItemSelectedListener(this)

        navHeaderView = navigationView.getHeaderView(0)

        // 네비게이션 헤더바 로그인한 계정 정보로 설정
        sharedPreferences = this.getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        email = sharedPreferences.getString(prefUserEmail, "").toString()

        headerEmailText = navHeaderView.findViewById(R.id.email)
        headerEmailText.text = email

        navMenu = findViewById(R.id.iv_menu)
        navMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    // 툴바 메뉴 버튼이 클릭됐을 때 콜백
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "MainActivity - onNavigationItemSelected() called")
        when(item!!.itemId) {
            R.id.community -> Snackbar.make(toolbar, "Community menu pressed", Snackbar.LENGTH_LONG).show()
            R.id.chat -> Snackbar.make(toolbar, "Chat menu pressed", Snackbar.LENGTH_LONG).show()
            R.id.restaurant -> Snackbar.make(toolbar, "Restaurant menu pressed", Snackbar.LENGTH_LONG).show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() { // 뒤로 가기 시 drawerLayout 닫기
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }

    // 하단 탭에 맞는 fragment 띄우기
    private fun initNavigationBar() {
        bnv_main.run {
            setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.note_tab -> {
                        changeFragment(noteFragment)
                    }
                    R.id.receipt_tab -> {
                        changeFragment(receiptFragment)
                    }
                    R.id.walk_tab -> {
                        changeFragment(walkFragment)
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

    fun changeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame, fragment).commit()
    }


    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000) {
            if (resultCode == RESULT_OK) {
                val userEmail = data?.getStringExtra("userEmail");
                Toast.makeText(this, "USER EMAIL: " + userEmail, Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "로그인 오류 발생", Toast.LENGTH_LONG).show()
            } else {
            }
        }
    }
}