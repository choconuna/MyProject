package org.techtown.myproject.note

import android.app.Activity
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogDungModel
import org.techtown.myproject.utils.DogPeeModel
import org.techtown.myproject.utils.FBRef

class PlusDungActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var nowDate : String

    lateinit var timeSlot : String

    lateinit var regularMinusBtn : Button
    lateinit var regularCnt : EditText
    lateinit var regularPlusBtn : Button

    lateinit var wateryMinusBtn : Button
    lateinit var wateryCnt : EditText
    lateinit var wateryPlusBtn : Button

    lateinit var diarrheaMinusBtn : Button
    lateinit var diarrheaCnt : EditText
    lateinit var diarrheaPlusBtn : Button

    lateinit var hardMinusBtn : Button
    lateinit var hardCnt : EditText
    lateinit var hardPlusBtn : Button

    lateinit var redMinusBtn : Button
    lateinit var redCnt : EditText
    lateinit var redPlusBtn : Button

    lateinit var blackMinusBtn : Button
    lateinit var blackCnt : EditText
    lateinit var blackPlusBtn : Button

    lateinit var whiteMinusBtn : Button
    lateinit var whiteCnt : EditText
    lateinit var whitePlusBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plus_dung)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        nowDate = intent.getStringExtra("date").toString() // 선택된 날짜

        findViewById<TextView>(R.id.today).text = nowDate

        setFirstData()

        regularPlusBtn.setOnClickListener {
            var nowRegularCnt = regularCnt.text.toString().toInt()
            nowRegularCnt += 1
            regularCnt.setText(nowRegularCnt.toString())
        }
        regularMinusBtn.setOnClickListener {
            var nowRegularCnt = regularCnt.text.toString().toInt()
            if(nowRegularCnt > 0)
                nowRegularCnt -= 1
            regularCnt.setText(nowRegularCnt.toString())
        }

        wateryPlusBtn.setOnClickListener {
            var nowWateryCnt = wateryCnt.text.toString().toInt()
            nowWateryCnt += 1
            wateryCnt.setText(nowWateryCnt.toString())
        }
        wateryMinusBtn.setOnClickListener {
            var nowWateryCnt = wateryCnt.text.toString().toInt()
            if(nowWateryCnt > 0)
                nowWateryCnt -= 1
            wateryCnt.setText(nowWateryCnt.toString())
        }

        diarrheaPlusBtn.setOnClickListener {
            var nowDiarrheaCnt = diarrheaCnt.text.toString().toInt()
            nowDiarrheaCnt += 1
            diarrheaCnt.setText(nowDiarrheaCnt.toString())
        }
        diarrheaMinusBtn.setOnClickListener {
            var nowDiarrheaCnt = diarrheaCnt.text.toString().toInt()
            if(nowDiarrheaCnt > 0)
                nowDiarrheaCnt -= 1
            diarrheaCnt.setText(nowDiarrheaCnt.toString())
        }

        hardPlusBtn.setOnClickListener {
            var nowHardCnt = hardCnt.text.toString().toInt()
            nowHardCnt += 1
            hardCnt.setText(nowHardCnt.toString())
        }
        hardMinusBtn.setOnClickListener {
            var nowHardCnt = hardCnt.text.toString().toInt()
            if(nowHardCnt > 0)
                nowHardCnt -= 1
            hardCnt.setText(nowHardCnt.toString())
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

        blackPlusBtn.setOnClickListener {
            var nowLBlackCnt = blackCnt.text.toString().toInt()
            nowLBlackCnt += 1
            blackCnt.setText(nowLBlackCnt.toString())
        }
        blackMinusBtn.setOnClickListener {
            var nowLBlackCnt = blackCnt.text.toString().toInt()
            if(nowLBlackCnt > 0)
                nowLBlackCnt -= 1
            blackCnt.setText(nowLBlackCnt.toString())
        }

        whitePlusBtn.setOnClickListener {
            var nowWhiteCnt = whiteCnt.text.toString().toInt()
            nowWhiteCnt += 1
            whiteCnt.setText(nowWhiteCnt.toString())
        }
        whiteMinusBtn.setOnClickListener {
            var nowWhiteCnt = whiteCnt.text.toString().toInt()
            if(nowWhiteCnt > 0)
                nowWhiteCnt -= 1
            whiteCnt.setText(nowWhiteCnt.toString())
        }

        val plusBtn = findViewById<Button>(R.id.plusBtn)
        plusBtn.setOnClickListener {
            if(regularCnt.text.toString().toInt() == 0 && wateryCnt.text.toString().toInt() == 0 && diarrheaCnt.text.toString().toInt() == 0 && hardCnt.text.toString().toInt() == 0 && redCnt.text.toString().toInt() == 0 && blackCnt.text.toString().toInt() == 0 && whiteCnt.text.toString().toInt() == 0) {
                Toast.makeText(this, "대변량을 추가하세요!", Toast.LENGTH_LONG).show()
            } else {
                if(regularCnt.text.toString().toInt() > 0)
                    plusDungNote(nowDate, "regular", regularCnt.text.toString())
                if(wateryCnt.text.toString().toInt() > 0)
                    plusDungNote(nowDate, "watery", wateryCnt.text.toString())
                if(diarrheaCnt.text.toString().toInt() > 0)
                    plusDungNote(nowDate, "diarrhea", diarrheaCnt.text.toString())
                if(hardCnt.text.toString().toInt() > 0)
                    plusDungNote(nowDate, "hard", hardCnt.text.toString())
                if(redCnt.text.toString().toInt() > 0)
                    plusDungNote(nowDate, "red", redCnt.text.toString())
                if(blackCnt.text.toString().toInt() > 0)
                    plusDungNote(nowDate, "black", blackCnt.text.toString())
                if(whiteCnt.text.toString().toInt() > 0)
                    plusDungNote(nowDate, "white", whiteCnt.text.toString())

                finish()
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setFirstData() {
        regularMinusBtn = findViewById(R.id.regularMinusBtn)
        regularCnt = findViewById(R.id.regularCnt)
        regularCnt.setText("0")
        regularPlusBtn = findViewById(R.id.regularPlusBtn)

        wateryMinusBtn = findViewById(R.id.wateryMinusBtn)
        wateryCnt = findViewById(R.id.wateryCnt)
        wateryCnt.setText("0")
        wateryPlusBtn = findViewById(R.id.wateryPlusBtn)

        diarrheaMinusBtn = findViewById(R.id.diarrheaMinusBtn)
        diarrheaCnt = findViewById(R.id.diarrheaCnt)
        diarrheaCnt.setText("0")
        diarrheaPlusBtn = findViewById(R.id.diarrheaPlusBtn)

        hardMinusBtn = findViewById(R.id.hardMinusBtn)
        hardCnt = findViewById(R.id.hardCnt)
        hardCnt.setText("0")
        hardPlusBtn = findViewById(R.id.hardPlusBtn)

        redMinusBtn = findViewById(R.id.redMinusBtn)
        redCnt = findViewById(R.id.redCnt)
        redCnt.setText("0")
        redPlusBtn = findViewById(R.id.redPlusBtn)

        blackMinusBtn = findViewById(R.id.blackMinusBtn)
        blackCnt = findViewById(R.id.blackCnt)
        blackCnt.setText("0")
        blackPlusBtn = findViewById(R.id.blackPlusBtn)

        whiteMinusBtn = findViewById(R.id.whiteMinusBtn)
        whiteCnt = findViewById(R.id.whiteCnt)
        whiteCnt.setText("0")
        whitePlusBtn = findViewById(R.id.whitePlusBtn)
    }

    private fun plusDungNote(date : String, dungType : String, dungCount : String) { // 반려견 대변 데이터 DB에 저장
        val key = FBRef.dungRef.child(userId).child(dogId).push().key.toString() // 키 값을 먼저 받아옴

        FBRef.dungRef.child(userId).child(dogId).child(key).setValue(DogDungModel(key, dogId, date, dungType, dungCount)) // 반려견 대변 정보 데이터베이스에 저장
    }
}