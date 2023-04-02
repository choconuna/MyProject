package org.techtown.myproject.note

import android.app.Activity
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogDungModel
import org.techtown.myproject.utils.DogVomitModel
import org.techtown.myproject.utils.FBRef

class PlusVomitActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var nowDate : String

    lateinit var timeSlot : String

    lateinit var transparentMinusBtn : Button
    lateinit var transparentCnt : EditText
    lateinit var transparentPlusBtn : Button

    lateinit var bubbleMinusBtn : Button
    lateinit var bubbleCnt : EditText
    lateinit var bubblePlusBtn : Button

    lateinit var foodMinusBtn : Button
    lateinit var foodCnt : EditText
    lateinit var foodPlusBtn : Button

    lateinit var yellowMinusBtn : Button
    lateinit var yellowCnt : EditText
    lateinit var yellowPlusBtn : Button

    lateinit var leafMinusBtn : Button
    lateinit var leafCnt : EditText
    lateinit var leafPlusBtn : Button

    lateinit var pinkMinusBtn : Button
    lateinit var pinkCnt : EditText
    lateinit var pinkPlusBtn : Button

    lateinit var brownMinusBtn : Button
    lateinit var brownCnt : EditText
    lateinit var brownPlusBtn : Button

    lateinit var greenMinusBtn : Button
    lateinit var greenCnt : EditText
    lateinit var greenPlusBtn : Button

    lateinit var substanceMinusBtn : Button
    lateinit var substanceCnt : EditText
    lateinit var substancePlusBtn : Button

    lateinit var redMinusBtn : Button
    lateinit var redCnt : EditText
    lateinit var redPlusBtn : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plus_vomit)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        nowDate = intent.getStringExtra("date").toString() // 선택된 날짜

        findViewById<TextView>(R.id.today).text = nowDate

        setFirstData()

        transparentPlusBtn.setOnClickListener {
            var nowTransparentCnt = transparentCnt.text.toString().toInt()
            nowTransparentCnt += 1
            transparentCnt.setText(nowTransparentCnt.toString())
        }
        transparentMinusBtn.setOnClickListener {
            var nowTransparentCnt = transparentCnt.text.toString().toInt()
            if(nowTransparentCnt > 0)
                nowTransparentCnt -= 1
            transparentCnt.setText(nowTransparentCnt.toString())
        }

        bubblePlusBtn.setOnClickListener {
            var nowBubbleCnt = bubbleCnt.text.toString().toInt()
            nowBubbleCnt += 1
            bubbleCnt.setText(nowBubbleCnt.toString())
        }
        bubbleMinusBtn.setOnClickListener {
            var nowBubbleCnt = bubbleCnt.text.toString().toInt()
            if(nowBubbleCnt > 0)
                nowBubbleCnt -= 1
            bubbleCnt.setText(nowBubbleCnt.toString())
        }

        foodPlusBtn.setOnClickListener {
            var nowFoodCnt = foodCnt.text.toString().toInt()
            nowFoodCnt += 1
            foodCnt.setText(nowFoodCnt.toString())
        }
        foodMinusBtn.setOnClickListener {
            var nowFoodCnt = foodCnt.text.toString().toInt()
            if(nowFoodCnt > 0)
                nowFoodCnt -= 1
            foodCnt.setText(nowFoodCnt.toString())
        }

        yellowPlusBtn.setOnClickListener {
            var nowYellowCnt = yellowCnt.text.toString().toInt()
            nowYellowCnt += 1
            yellowCnt.setText(nowYellowCnt.toString())
        }
        yellowMinusBtn.setOnClickListener {
            var nowYellowCnt = yellowCnt.text.toString().toInt()
            if(nowYellowCnt > 0)
                nowYellowCnt -= 1
            yellowCnt.setText(nowYellowCnt.toString())
        }

        leafPlusBtn.setOnClickListener {
            var nowLeafCnt = leafCnt.text.toString().toInt()
            nowLeafCnt += 1
            leafCnt.setText(nowLeafCnt.toString())
        }
        leafMinusBtn.setOnClickListener {
            var nowLeafCnt = leafCnt.text.toString().toInt()
            if(nowLeafCnt > 0)
                nowLeafCnt -= 1
            leafCnt.setText(nowLeafCnt.toString())
        }

        pinkPlusBtn.setOnClickListener {
            var nowPinkCnt = pinkCnt.text.toString().toInt()
            nowPinkCnt += 1
            pinkCnt.setText(nowPinkCnt.toString())
        }
        pinkMinusBtn.setOnClickListener {
            var nowPinkCnt = pinkCnt.text.toString().toInt()
            if(nowPinkCnt > 0)
                nowPinkCnt -= 1
            pinkCnt.setText(nowPinkCnt.toString())
        }

        brownPlusBtn.setOnClickListener {
            var nowBrownCnt = brownCnt.text.toString().toInt()
            nowBrownCnt += 1
            brownCnt.setText(nowBrownCnt.toString())
        }
        brownMinusBtn.setOnClickListener {
            var nowWhiteCnt = brownCnt.text.toString().toInt()
            if(nowWhiteCnt > 0)
                nowWhiteCnt -= 1
            brownCnt.setText(nowWhiteCnt.toString())
        }

        greenPlusBtn.setOnClickListener {
            var nowGreenCnt = greenCnt.text.toString().toInt()
            nowGreenCnt += 1
            greenCnt.setText(nowGreenCnt.toString())
        }
        greenMinusBtn.setOnClickListener {
            var nowGreenCnt = greenCnt.text.toString().toInt()
            if(nowGreenCnt > 0)
                nowGreenCnt -= 1
            greenCnt.setText(nowGreenCnt.toString())
        }

        substancePlusBtn.setOnClickListener {
            var nowSubstanceCnt = substanceCnt.text.toString().toInt()
            nowSubstanceCnt += 1
            substanceCnt.setText(nowSubstanceCnt.toString())
        }
        substanceMinusBtn.setOnClickListener {
            var nowSubstanceCnt = substanceCnt.text.toString().toInt()
            if(nowSubstanceCnt > 0)
                nowSubstanceCnt -= 1
            substanceCnt.setText(nowSubstanceCnt.toString())
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
            if(transparentCnt.text.toString().toInt() == 0 && bubbleCnt.text.toString().toInt() == 0 && foodCnt.text.toString().toInt() == 0 && yellowCnt.text.toString().toInt() == 0 && leafCnt.text.toString().toInt() == 0 && pinkCnt.text.toString().toInt() == 0 && brownCnt.text.toString().toInt() == 0 && greenCnt.text.toString().toInt() == 0 && substanceCnt.text.toString().toInt() == 0 && redCnt.text.toString().toInt() == 0) {
                Toast.makeText(this, "대변량을 추가하세요!", Toast.LENGTH_LONG).show()
            } else {
                if(transparentCnt.text.toString().toInt() > 0)
                    plusVomitNote(nowDate, "transparent", transparentCnt.text.toString())
                if(bubbleCnt.text.toString().toInt() > 0)
                    plusVomitNote(nowDate, "bubble", bubbleCnt.text.toString())
                if(foodCnt.text.toString().toInt() > 0)
                    plusVomitNote(nowDate, "food", foodCnt.text.toString())
                if(yellowCnt.text.toString().toInt() > 0)
                    plusVomitNote(nowDate, "yellow", yellowCnt.text.toString())
                if(leafCnt.text.toString().toInt() > 0)
                    plusVomitNote(nowDate, "leaf", leafCnt.text.toString())
                if(pinkCnt.text.toString().toInt() > 0)
                    plusVomitNote(nowDate, "pink", pinkCnt.text.toString())
                if(brownCnt.text.toString().toInt() > 0)
                    plusVomitNote(nowDate, "brown", brownCnt.text.toString())
                if(greenCnt.text.toString().toInt() > 0)
                    plusVomitNote(nowDate, "green", greenCnt.text.toString())
                if(substanceCnt.text.toString().toInt() > 0)
                    plusVomitNote(nowDate, "substance", substanceCnt.text.toString())
                if(redCnt.text.toString().toInt() > 0)
                    plusVomitNote(nowDate, "red", redCnt.text.toString())

                finish()
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setFirstData() {
        transparentMinusBtn = findViewById(R.id.transparentMinusBtn)
        transparentCnt = findViewById(R.id.transparentCnt)
        transparentCnt.setText("0")
        transparentPlusBtn = findViewById(R.id.transparentPlusBtn)

        bubbleMinusBtn = findViewById(R.id.bubbleMinusBtn)
        bubbleCnt = findViewById(R.id.bubbleCnt)
        bubbleCnt.setText("0")
        bubblePlusBtn = findViewById(R.id.bubblePlusBtn)

        foodMinusBtn = findViewById(R.id.foodMinusBtn)
        foodCnt = findViewById(R.id.foodCnt)
        foodCnt.setText("0")
        foodPlusBtn = findViewById(R.id.foodPlusBtn)

        yellowMinusBtn = findViewById(R.id.yellowMinusBtn)
        yellowCnt = findViewById(R.id.yellowCnt)
        yellowCnt.setText("0")
        yellowPlusBtn = findViewById(R.id.yellowPlusBtn)

        leafMinusBtn = findViewById(R.id.leafMinusBtn)
        leafCnt = findViewById(R.id.leafCnt)
        leafCnt.setText("0")
        leafPlusBtn = findViewById(R.id.leafPlusBtn)

        pinkMinusBtn = findViewById(R.id.pinkMinusBtn)
        pinkCnt = findViewById(R.id.pinkCnt)
        pinkCnt.setText("0")
        pinkPlusBtn = findViewById(R.id.pinkPlusBtn)

        brownMinusBtn = findViewById(R.id.brownMinusBtn)
        brownCnt = findViewById(R.id.brownCnt)
        brownCnt.setText("0")
        brownPlusBtn = findViewById(R.id.brownPlusBtn)

        greenMinusBtn = findViewById(R.id.greenMinusBtn)
        greenCnt = findViewById(R.id.greenCnt)
        greenCnt.setText("0")
        greenPlusBtn = findViewById(R.id.greenPlusBtn)

        substanceMinusBtn = findViewById(R.id.substanceMinusBtn)
        substanceCnt = findViewById(R.id.substanceCnt)
        substanceCnt.setText("0")
        substancePlusBtn = findViewById(R.id.substancePlusBtn)

        redMinusBtn = findViewById(R.id.redMinusBtn)
        redCnt = findViewById(R.id.redCnt)
        redCnt.setText("0")
        redPlusBtn = findViewById(R.id.redPlusBtn)
    }

    private fun plusVomitNote(date : String, vomitType : String, vomitCount : String) { // 반려견 구토 데이터 DB에 저장
        val key = FBRef.vomitRef.child(userId).child(dogId).push().key.toString() // 키 값을 먼저 받아옴

        FBRef.vomitRef.child(userId).child(dogId).child(key).setValue(DogVomitModel(key, dogId, date, vomitType, vomitCount)) // 반려견 구토 정보 데이터베이스에 저장
    }
}