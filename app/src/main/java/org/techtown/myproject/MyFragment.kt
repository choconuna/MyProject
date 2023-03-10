package org.techtown.myproject

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.techtown.myproject.*
import kotlin.math.log

class MyFragment : Fragment() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor : SharedPreferences.Editor

    lateinit var logoutBtn : TextView

    private val TAG: String = "log"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v : View? = inflater.inflate(R.layout.fragment_my, container, false)

        logoutBtn = v!!.findViewById(R.id.logout)
        logoutBtn.setOnClickListener {
            clearPreferences()
            logout()
        }

        return v
    }

    fun clearPreferences() { // 자동 로그인 해제
        Log.d(TAG, "clearPreferences() called")
        sharedPreferences = this.requireActivity().getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        editor.clear()
        editor.commit()
    }

    fun logout() { // MainActivity 닫고 StartActivity 열기
        Log.d(TAG, "logout() called")
        var intent = Intent(this.activity, StartActivity::class.java)
        startActivity(intent)

       requireActivity().finish()
    }
}