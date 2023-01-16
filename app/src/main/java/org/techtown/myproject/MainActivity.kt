package org.techtown.myproject

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val TAG : String = "로그"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "MainActivity - onCreate() called")

        setProfile()

        intent = Intent(this, StartActivity::class.java)
        startActivityForResult(intent, 1000)
    }

    @SuppressLint("ResourceType")
    private fun setProfile() {
        try{
            val user = Firebase.auth.currentUser
            user?.let {
                val header : View = findViewById<NavigationView>(R.id.naviView).getHeaderView(0)
                val uName = header.findViewById<TextView>(R.id.text_userName)
                val uEmail = header.findViewById<TextView>(R.id.text_userEmail)
                val uPhoto = header.findViewById<ImageView>(R.id.profilepic)

                uName.text = user.displayName
                uEmail.text = user.email
                uPhoto.setImageURI(user.photoUrl)
            }
        }
        catch(e: NullPointerException){
            Log.d(TAG, "NullPointerException", e)
            Toast.makeText(GlobalApplication.instance, "NullPointerException: $e", Toast.LENGTH_LONG).show()
        }
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        TODO("Not yet implemented")
    }
}