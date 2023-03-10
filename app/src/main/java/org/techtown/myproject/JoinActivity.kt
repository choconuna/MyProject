package org.techtown.myproject

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.firebase.ui.auth.data.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class JoinActivity : AppCompatActivity() {
    lateinit var mFirebaseAuth: FirebaseAuth            // 파이어베이스 인증

    lateinit var emailArea : EditText
    lateinit var emailAlert : TextView
    lateinit var emailVeri : Button

    lateinit var pwAlert : TextView
    lateinit var pwArea : EditText
    lateinit var pwCheckArea : EditText

    lateinit var email : String
    lateinit var pw : String

    lateinit var joinBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        val toolbar = findViewById<Toolbar> (R.id.toolbar)
        setSupportActionBar(toolbar)

        mFirebaseAuth = FirebaseAuth.getInstance()

        emailAlert = findViewById(R.id.emailAlert)
        emailArea = findViewById(R.id.emailArea)
        emailVeri = findViewById(R.id.emailVeri)
        emailAlert.visibility = GONE

        pwAlert = findViewById(R.id.pwAlert)
        pwArea = findViewById(R.id.pwArea)
        pwCheckArea = findViewById(R.id.pwCheckArea)
        pwAlert.visibility = GONE

        joinBtn = findViewById(R.id.joinBtn)

        joinBtn.setOnClickListener {
            email = emailArea.text.toString().trim()
            pw = pwArea.text.toString().trim()

            Log.d("Join", email + " " + pw)

            createUser(email, pw)
        }
    }

    fun createUser(email : String, pw : String) {
        mFirebaseAuth.createUserWithEmailAndPassword(email, pw).addOnCompleteListener(this)
        { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@JoinActivity, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                finish()
            }
            else if (!task.isSuccessful) {
                try {
                    throw task.exception!!
                } catch(e : FirebaseAuthWeakPasswordException) {
                    Toast.makeText(this@JoinActivity, "비밀번호가 간단합니다.", Toast.LENGTH_LONG).show()
                } catch(e : FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(this@JoinActivity, "이메일 형식에 맞지 않습니다.", Toast.LENGTH_LONG).show()
                } catch(e : FirebaseAuthUserCollisionException) {
                    Toast.makeText(this@JoinActivity, "이미 존재하는 이메일입니다.", Toast.LENGTH_LONG).show()
                } catch(e : Exception) {
                    Toast.makeText(this@JoinActivity, "정확히 입력했는지 확인하세요.", Toast.LENGTH_LONG).show()
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

