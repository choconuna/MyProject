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
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class JoinActivity : AppCompatActivity() {

    lateinit var mFirebaseAuth: FirebaseAuth            // 파이어베이스 인증
    lateinit var mDatabaseReference: DatabaseReference  // 실시간 데이터베이스

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
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("mungNote")

        emailAlert = findViewById(R.id.emailAlert)
        emailArea = findViewById(R.id.emailArea)
        emailVeri = findViewById(R.id.emailVeri)
        emailAlert.visibility = GONE

        pwAlert = findViewById(R.id.pwAlert)
        pwArea = findViewById(R.id.pwArea)
        pwCheckArea = findViewById(R.id.pwCheckArea)
        pwAlert.visibility = GONE

        emailFormCheck()

        pwCheck()

        joinBtn = findViewById(R.id.joinBtn)

        joinBtn.setOnClickListener {
            Log.d("Main", email + " " + pw)

            if(!email.equals("") && !pw.equals("")) {
                createUser(email, pw)
                finish()
            } else if(email.equals("") && !pw.equals("")) {
                Toast.makeText(this, "이메일을 입력하세요!", Toast.LENGTH_LONG).show()
            } else if(!email.equals("") && pw.equals("")) {
                Toast.makeText(this, "비밀번호를 입력하세요!", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun emailFormCheck() { // 이메일 형식으로 입력한 것인지 확인
        emailArea.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                emailAlert.visibility = View.VISIBLE
                if (!Patterns.EMAIL_ADDRESS.matcher(s.toString().trim()).matches()) {
                    if(emailArea.text.toString().equals("")) {
                        emailAlert.visibility = View.GONE
                    } else {
                        emailAlert.setText("이메일 형식으로 입력해 주세요.") // 경고 메세지
                    }
                } else {
                    emailAlert.visibility = View.GONE //에러 메세지 제거
                    email = emailArea.getText().toString().trim()
                    emailVeri.setOnClickListener {
                    }
                }
            }
        })
    }

    fun pwCheck() {
        pwCheckArea.addTextChangedListener(object : TextWatcher {
            //입력하기 전
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                pwAlert.setText("")
            }
            //텍스트 변화가 있을 시
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(pwArea.getText().toString().trim().equals(pwCheckArea.getText().toString().trim())){
                    pwAlert.visibility = GONE
                    // 가입하기 버튼 활성화
                    joinBtn.isEnabled=true
                }
                else{
                    pwAlert.setText("비밀번호가 일치하지 않습니다.")
                    // 가입하기 버튼 비활성화
                    joinBtn.isEnabled=false
                }
            }
            // 텍스트 변화가 있을 시
            override fun afterTextChanged(p0: Editable?) {
                pwAlert.visibility = VISIBLE
                if(pwArea.getText().toString().trim().equals(pwCheckArea.getText().toString().trim()) && !pwArea.getText().toString().equals("")){
                    pwAlert.visibility = GONE
                    pw = pwArea.getText().toString().trim()
                    // 가입하기 버튼 활성화
                    joinBtn.isEnabled=true
                }
                else{
                    if(!pwArea.getText().toString().trim().equals(""))
                        pwAlert.setText("비밀번호가 일치하지 않습니다.")
                    else if(pwArea.getText().toString().trim().equals(""))
                        pwAlert.visibility = GONE
                    // 가입하기 버튼 비활성화
                    joinBtn.isEnabled=false
                }
            }
        })
    }

    fun createUser(email : String, pw : String) {
        mFirebaseAuth.createUserWithEmailAndPassword(email, pw).addOnCompleteListener(this)
        { task ->
            if (task.isSuccessful) {
                var firebaseUser: FirebaseUser? = mFirebaseAuth.currentUser
                var account: UserAccount = UserAccount()
                account.setIdToken(firebaseUser?.uid!!)
                account.setEmailId(firebaseUser?.email!!)
                account.setPassword(pw)

                // setValue : database에 insert(삽입) 행위
                mDatabaseReference.child("UserAccount").child(firebaseUser.uid).setValue(account)

                Toast.makeText(this@JoinActivity, "회원가입 성공!", Toast.LENGTH_SHORT).show()
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

