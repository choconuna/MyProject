package org.techtown.myproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class  StartActivity : AppCompatActivity() {
    lateinit var loginBtn : Button
    lateinit var joinBtn : Button
    lateinit var emailArea : EditText
    lateinit var pwArea : EditText
    lateinit var title : ImageView
    lateinit var loginArea : LinearLayout

    lateinit var email : String
    lateinit var pw : String

    private lateinit var sharedPreferences: SharedPreferences
    lateinit var editor : SharedPreferences.Editor
    private val prefUserEmail = "userEmail"

    private val TAG: String = "log"

    private lateinit var mFirebaseAuth : FirebaseAuth // Firebase Auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        title = findViewById(R.id.titleView)
        val animOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        title.startAnimation(animOut)
        val animIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        title.startAnimation(animIn)

        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        if(sharedPreferences.getString(prefUserEmail, "").toString().isNotEmpty()) {
            Log.d(TAG, "자동 로그인 성공")
            var intent : Intent = Intent(this, MainActivity::class.java)
            intent.putExtra("userEmail", sharedPreferences.getString(prefUserEmail, "").toString())
            startActivity(intent)
            finish()
        }

        loginArea = findViewById(R.id.login)

        Handler().postDelayed({
            title.visibility = View.GONE
            loginArea.visibility = View.VISIBLE
        }, StartActivity.DURATION)

        emailArea = findViewById(R.id.emailArea)
        pwArea = findViewById(R.id.pwArea)

        loginBtn = findViewById(R.id.loginBtn)
        loginBtn.setOnClickListener {
            email = emailArea.text.toString().trim()
            pw = pwArea.text.toString().trim()

            login(email, pw)
        }

        Log.d(TAG, "LoginActivity - onCreate() called")

        // mFirebaseAuth = Firebase.auth // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance()

        joinBtn = findViewById(R.id.joinBtn)
        joinBtn.setOnClickListener {
            intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }
    }

    companion object {
        private const val DURATION : Long = 4000
    }

    public override fun onStart() {
        super.onStart()
        Log.d(TAG, "LoginActivity - onStart() called")
    }

    private fun autoLogin() {

    }

    private fun login(email : String, pw : String) { // 로그인
       if(email == ""){
            Toast.makeText(this, "이메일을 입력하세요!", Toast.LENGTH_LONG).show()
            emailArea.setSelection(0)
        } else if(email != "" && pw == "") {
            Toast.makeText(this, "비밀번호를 입력하세요!", Toast.LENGTH_LONG).show()
            pwArea.setSelection(0)
        } else if(email != "" && pw != ""){
            mFirebaseAuth.signInWithEmailAndPassword(email, pw).addOnCompleteListener(this) {
                task ->
                if(task.isSuccessful) {
                    Log.d(TAG, "LoginActivity - login() called")
                    editor = sharedPreferences.edit()
                    editor.putString(prefUserEmail, email)
                    editor.commit()
                    var intent : Intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@StartActivity, "등록되지 않은 이메일이거나 잘못된 비밀번호입니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean { // 다른 영역 터치 시 키보드 숨기기
        val view = currentFocus
        if (view != null && (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_MOVE) && view is EditText && !view.javaClass.name.startsWith(
                "android.webkit."
            )
        ) {
            val scrcoords = IntArray(2)
            view.getLocationOnScreen(scrcoords)
            val x = ev.rawX + view.getLeft() - scrcoords[0]
            val y = ev.rawY + view.getTop() - scrcoords[1]
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom()) (this.getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager).hideSoftInputFromWindow(
                this.window.decorView.applicationWindowToken, 0
            )
        }
        return super.dispatchTouchEvent(ev)
    }
}