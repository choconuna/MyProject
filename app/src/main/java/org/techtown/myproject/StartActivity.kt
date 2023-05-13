package org.techtown.myproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MotionEvent
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
    lateinit var loginArea : LinearLayout

    lateinit var email : String
    lateinit var pw : String

    private lateinit var sharedPreferences: SharedPreferences
    lateinit var editor : SharedPreferences.Editor
    private val prefUserEmail = "userEmail"

    private val TAG: String = StartActivity::class.java.simpleName

    private lateinit var mFirebaseAuth : FirebaseAuth // Firebase Auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        autoLogin()

        loginArea = findViewById(R.id.login)

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

    public override fun onStart() {
        super.onStart()
        Log.d(TAG, "LoginActivity - onStart() called")
    }

    private fun autoLogin() { // 자동 로그인 처리
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        if(sharedPreferences.getString(prefUserEmail, "").toString().isNotEmpty()) {
            Log.d(TAG, "자동 로그인 성공")
            var intent : Intent = Intent(this, MainActivity::class.java)
            intent.putExtra("userEmail", sharedPreferences.getString(prefUserEmail, "").toString())
            startActivity(intent)
            finish()
        }
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
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@StartActivity, "등록되지 않은 이메일이거나 잘못된 비밀번호입니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}