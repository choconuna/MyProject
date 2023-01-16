package org.techtown.myproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.kakao.sdk.common.util.Utility
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.kakao.auth.AuthType
import com.kakao.auth.Session
import org.json.JSONObject

class  StartActivity : AppCompatActivity() {
    lateinit var loginBtn : Button
    lateinit var kakaoBtn : ImageButton
    lateinit var joinBtn : Button
    lateinit var emailArea : EditText
    lateinit var pwArea : EditText
    lateinit var title : ImageView
    lateinit var loginArea : LinearLayout

    private val TAG: String = "로그"

    // 로그인 공통 callback (login 결과를 SessionCallback.kt 으로 전송)
    private lateinit var callback : SessionCallback
    private lateinit var mFirebaseAuth : FirebaseAuth // Firebase Auth
    private lateinit var mDatabaseRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val keyHash = Utility.getKeyHash(this)
        Log.d("Hash", keyHash)

        title = findViewById(R.id.titleView)
        val animOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        title.startAnimation(animOut)
        val animIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        title.startAnimation(animIn)

        loginArea = findViewById(R.id.login)

        Handler().postDelayed({
            title.visibility = View.GONE
            loginArea.visibility = View.VISIBLE
        }, StartActivity.DURATION)

        emailArea = findViewById(R.id.emailArea)
        pwArea = findViewById(R.id.pwArea)

        loginBtn = findViewById(R.id.loginBtn)
        loginBtn.setOnClickListener {
            loginCheck()
        }

        Log.d(TAG, "LoginActivity - onCreate() called")

        // mFirebaseAuth = Firebase.auth // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("mungNote")
        callback = SessionCallback(this) // Initialize Session

        kakaoBtn = findViewById(R.id.kakao_login_button)
        kakaoBtn.setOnClickListener {
            kakaoLoginStart()
        }

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

    /* KAKAO LOGIN */
    private fun kakaoLoginStart(){
        Log.d(TAG, "LoginActivity - kakaoLoginStart() called")

        val keyHash = Utility.getKeyHash(this) // keyHash 발급
        Log.d(TAG, "KEY_HASH : $keyHash")

        Session.getCurrentSession().addCallback(callback)
        Session.getCurrentSession().open(AuthType.KAKAO_LOGIN_ALL, this)
    }

    open fun getFirebaseJwt(kakaoAccessToken: String): Task<String> {
        Log.d(TAG, "LoginActivity - getFirebaseJwt() called")
        val source = TaskCompletionSource<String>()
        val queue = Volley.newRequestQueue(this)
        val url = "http://IP주소:8000/verifyToken" // validation server
        val validationObject: HashMap<String?, String?> = HashMap()
        validationObject["token"] = kakaoAccessToken

        val request: JsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url,
            JSONObject(validationObject as Map<*, *>),
            Response.Listener { response ->
                try {
                    val firebaseToken = response.getString("firebase_token")
                    source.setResult(firebaseToken)
                } catch (e: Exception) {
                    source.setException(e)
                }
            },
            Response.ErrorListener { error ->
                Log.e(TAG, error.toString())
                source.setException(error)
            }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Authorization"] = String.format("Basic %s", Base64.encodeToString(
                    String.format("%s:%s", "token", kakaoAccessToken)
                        .toByteArray(), Base64.DEFAULT)
                )
                return params
            }
        }
        queue.add(request)
        return source.task // call validation server and retrieve firebase token
    }

    fun startMainActivity(){
        Log.d(TAG, "StartActivity - startMainActivity() called")

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun loginCheck() { // 로그인 형식 체크
        var email = emailArea.text.toString().trim()
        var pw = pwArea.text.toString().trim()

        if(email != "" && pw != "") {
            loginUser(email, pw)
        } else  if(email == ""){
            Toast.makeText(this, "이메일을 입력하세요!", Toast.LENGTH_LONG).show()
            emailArea.setSelection(0)
        } else if(email != "" && pw == "") {
            Toast.makeText(this, "비밀번호를 입력하세요!", Toast.LENGTH_LONG).show()
            pwArea.setSelection(0)
        }
    }

    // 로그인 완료 시 MainActivity로 이동
    private fun loginUser(email : String, pw : String) {
        mFirebaseAuth.createUserWithEmailAndPassword(email, pw).addOnCompleteListener(this, object:
            OnCompleteListener<AuthResult> {
            override fun onComplete(task: Task<AuthResult>) {
                // 로그인 성공
                if(task.isSuccessful) {
                    val intent = Intent(this@StartActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish() // 현재 액티비티 파괴
                }
                else {
                    Toast.makeText(this@StartActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                }
            }
        })

        intent = Intent()
        intent.putExtra("userEmail", email)
        setResult(RESULT_OK, intent)
        finish()
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

    override fun onDestroy() {
        super.onDestroy()
        Session.getCurrentSession().removeCallback(callback)
    }
}