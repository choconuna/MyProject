package org.techtown.myproject.my

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import org.techtown.myproject.R
import org.techtown.myproject.community.WriteCommunityActivity

class AlarmActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        val backBtn = findViewById<ImageView>(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }

        val writeBtn = findViewById<ImageView>(R.id.writeBtn)
        writeBtn.setOnClickListener {
            val intent = Intent(this, MakeAlarmActivity::class.java)
            startActivity(intent)
        }
    }
}