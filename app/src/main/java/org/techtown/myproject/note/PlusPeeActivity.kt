package org.techtown.myproject.note

import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogPeeModel
import org.techtown.myproject.utils.DogSnackModel
import org.techtown.myproject.utils.FBRef

class PlusPeeActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var nowDate : String

    lateinit var timeSlot : String

    lateinit var transParentMinusBtn : Button
    lateinit var transParentCnt : EditText
    lateinit var transParentPlusBtn : Button

    lateinit var lightYellowMinusBtn : Button
    lateinit var lightYellowCnt : EditText
    lateinit var lightYellowPlusBtn : Button

    lateinit var darkYellowMinusBtn : Button
    lateinit var darkYellowCnt : EditText
    lateinit var darkYellowPlusBtn : Button

    lateinit var redMinusBtn : Button
    lateinit var redCnt : EditText
    lateinit var redPlusBtn : Button

    lateinit var brownMinusBtn : Button
    lateinit var brownCnt : EditText
    lateinit var brownPlusBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plus_pee)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        nowDate = intent.getStringExtra("date").toString() // 선택된 날짜

        findViewById<TextView>(R.id.today).text = nowDate

        setFirstData()

        transParentPlusBtn.setOnClickListener {
            var nowTransParentCnt = transParentCnt.text.toString().toInt()
            nowTransParentCnt += 1
            transParentCnt.setText(nowTransParentCnt.toString())
        }
        transParentMinusBtn.setOnClickListener {
            var nowTransParentCnt = transParentCnt.text.toString().toInt()
            if(nowTransParentCnt > 0)
                nowTransParentCnt -= 1
            transParentCnt.setText(nowTransParentCnt.toString())
        }

        lightYellowPlusBtn.setOnClickListener {
            var nowLightYellowCnt = lightYellowCnt.text.toString().toInt()
            nowLightYellowCnt += 1
            lightYellowCnt.setText(nowLightYellowCnt.toString())
        }
        lightYellowMinusBtn.setOnClickListener {
            var nowLightYellowCnt = lightYellowCnt.text.toString().toInt()
            if(nowLightYellowCnt > 0)
                nowLightYellowCnt -= 1
            lightYellowCnt.setText(nowLightYellowCnt.toString())
        }

        darkYellowPlusBtn.setOnClickListener {
            var nowDartYellowCnt = darkYellowCnt.text.toString().toInt()
            nowDartYellowCnt += 1
            darkYellowCnt.setText(nowDartYellowCnt.toString())
        }
        darkYellowMinusBtn.setOnClickListener {
            var nowDartYellowCnt = darkYellowCnt.text.toString().toInt()
            if(nowDartYellowCnt > 0)
                nowDartYellowCnt -= 1
            darkYellowCnt.setText(nowDartYellowCnt.toString())
        }

        redPlusBtn.setOnClickListener {
            var nowRedCnt = redCnt.text.toString().toInt()
            nowRedCnt += 1
            redCnt.setText(nowRedCnt.toString())
        }
        redMinusBtn.setOnClickListener {
            var nowRedCnt = redCnt.text.toString().toInt()
            if(nowRedCnt > 0)
                nowRedCnt -= 1
            redCnt.setText(nowRedCnt.toString())
        }

        redPlusBtn.setOnClickListener {
            var nowRedCnt = redCnt.text.toString().toInt()
            nowRedCnt += 1
            redCnt.setText(nowRedCnt.toString())
        }
        redMinusBtn.setOnClickListener {
            var nowRedCnt = redCnt.text.toString().toInt()
            if(nowRedCnt > 0)
                nowRedCnt -= 1
            redCnt.setText(nowRedCnt.toString())
        }

        val plusBtn = findViewById<Button>(R.id.plusBtn)
        plusBtn.setOnClickListener {
            if(transParentCnt.text.toString().toInt() == 0 && lightYellowCnt.text.toString().toInt() == 0 && darkYellowCnt.text.toString().toInt() == 0 && redCnt.text.toString().toInt() == 0 && brownCnt.text.toString().toInt() == 0) {
                Toast.makeText(this, "소변량을 추가하세요!", Toast.LENGTH_LONG).show()
            } else {
                if(transParentCnt.text.toString().toInt() > 0)
                    plusPeeNote(nowDate, "transparent", transParentCnt.text.toString())
                if(lightYellowCnt.text.toString().toInt() > 0)
                    plusPeeNote(nowDate, "lightYellow", lightYellowCnt.text.toString())
                if(darkYellowCnt.text.toString().toInt() > 0)
                    plusPeeNote(nowDate, "darkYellow", darkYellowCnt.text.toString())
                if(redCnt.text.toString().toInt() > 0)
                    plusPeeNote(nowDate, "red", redCnt.text.toString())
                if(brownCnt.text.toString().toInt() > 0)
                    plusPeeNote(nowDate, "brown", brownCnt.text.toString())

                finish()
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setFirstData() {
        transParentMinusBtn = findViewById(R.id.transParentMinusBtn)
        transParentCnt = findViewById(R.id.transParentCnt)
        transParentCnt.setText("0")
        transParentPlusBtn = findViewById(R.id.transParentPlusBtn)

        lightYellowMinusBtn = findViewById(R.id.lightYellowMinusBtn)
        lightYellowCnt = findViewById(R.id.lightYellowCnt)
        lightYellowCnt.setText("0")
        lightYellowPlusBtn = findViewById(R.id.lightYellowPlusBtn)

        darkYellowMinusBtn = findViewById(R.id.darkYellowMinusBtn)
        darkYellowCnt = findViewById(R.id.darkYellowCnt)
        darkYellowCnt.setText("0")
        darkYellowPlusBtn = findViewById(R.id.darkYellowPlusBtn)

        redMinusBtn = findViewById(R.id.redMinusBtn)
        redCnt = findViewById(R.id.redCnt)
        redCnt.setText("0")
        redPlusBtn = findViewById(R.id.redPlusBtn)

        brownMinusBtn = findViewById(R.id.brownMinusBtn)
        brownCnt = findViewById(R.id.brownCnt)
        brownCnt.setText("0")
        brownPlusBtn = findViewById(R.id.brownPlusBtn)
    }


    private fun plusPeeNote(date : String, peeType : String, peeCount : String) { // 반려견 소변 데이터 DB에 저장
        val key = FBRef.peeRef.child(userId).child(dogId).push().key.toString() // 키 값을 먼저 받아옴

        FBRef.peeRef.child(userId).child(dogId).child(key).setValue(DogPeeModel(key, dogId, date, peeType, peeCount)) // 반려견 소변 정보 데이터베이스에 저장
    }
}