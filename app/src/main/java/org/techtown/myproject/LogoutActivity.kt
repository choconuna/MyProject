package org.techtown.myproject

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LogoutActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor : SharedPreferences.Editor

    lateinit var mAuth : FirebaseAuth

    private val TAG: String = LogoutActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)

        mAuth = Firebase.auth

        clearPreferences()
        logout()
    }

    private fun clearPreferences() { // 자동 로그인 해제
        Log.d(TAG, "clearPreferences() called")
        sharedPreferences = this.getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        editor.remove("userEmail")
        // editor.clear()
        editor.commit()
    }

    private fun logout() { // MainActivity 닫고 StartActivity 열기
        Log.d(TAG, "logout() called")
        mAuth.signOut()
        var intent = Intent(this, StartActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // 기존의 액티비티 모두 삭제하기
        startActivity(intent)

        finish()
    }
}