package org.techtown.myproject.my

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.techtown.myproject.LogoutActivity
import org.techtown.myproject.R
import org.techtown.myproject.StartActivity
import org.techtown.myproject.utils.FBRef

class LeaveActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor : SharedPreferences.Editor

    lateinit var mAuth : FirebaseAuth

    private val TAG: String = LeaveActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leave)

        mAuth = Firebase.auth

        clearPreferences()
        leave()
    }

    private fun clearPreferences() { // 자동 로그인 해제
        Log.d(TAG, "clearPreferences() called")
        sharedPreferences = this.getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        editor.remove("userEmail")
        // editor.clear()
        editor.commit()
    }

    private fun leave() { // MainActivity 닫고 StartActivity 열기
        Log.d(TAG, "leave() called")
        val user = mAuth.currentUser
        user?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var intent = Intent(this, StartActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // 기존의 액티비티 모두 삭제하기
                startActivity(intent)
                finish()
            } else {
                finish()
            }
        }
    }
}