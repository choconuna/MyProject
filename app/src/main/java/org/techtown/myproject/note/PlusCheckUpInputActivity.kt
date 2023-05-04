package org.techtown.myproject.note

import android.app.Activity
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.community.GalleryAdapter
import org.techtown.myproject.utils.DogCheckUpInputModel
import org.techtown.myproject.utils.FBRef
import java.util.ArrayList

class PlusCheckUpInputActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var nowDate : String

    private lateinit var date : String
    private lateinit var nameArea : EditText

    private lateinit var yearArea : EditText
    private lateinit var monthArea : EditText
    private lateinit var dayArea : EditText

    private lateinit var partArea : EditText
    private lateinit var minArea : EditText
    private lateinit var maxArea : EditText
    private lateinit var resultArea : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plus_check_up_input)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        date = intent.getStringExtra("date").toString()

        nameArea = findViewById(R.id.nameArea)

        yearArea = findViewById(R.id.yearArea)
        monthArea = findViewById(R.id.monthArea)
        dayArea = findViewById(R.id.dayArea)

        partArea = findViewById(R.id.partArea)
        minArea = findViewById(R.id.minArea)
        maxArea = findViewById(R.id.maxArea)
        resultArea = findViewById(R.id.resultArea)

        setDate()

        val plusBtn = findViewById<Button>(R.id.plusBtn)
        plusBtn.setOnClickListener {

            val month = monthArea.text.toString().toInt()
            val day = dayArea.text.toString().toInt()

            if(nameArea.text.toString() == "") {
                Toast.makeText(this, "항목명을 입력하세요!", Toast.LENGTH_LONG).show()
                nameArea.setSelection(0)
            }
            else if(yearArea.text.toString() == "" || monthArea.text.toString() == "" || dayArea.text.toString() == "" || month < 1 || month > 12 || day < 1 || day > 31) {
                if(((month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) && day > 31) || ((month == 2 || month == 4 || month == 6 || month == 9 || month == 11) && day > 30)) {
                    Toast.makeText(this, "날짜를 정확하게 입력하세요!", Toast.LENGTH_LONG).show()
                    yearArea.setSelection(0)
                } else {
                    Toast.makeText(this, "날짜를 정확하게 입력하세요!", Toast.LENGTH_LONG).show()
                    yearArea.setSelection(0)
                }
            }  else if(minArea.text.toString().trim() == "") {
                Toast.makeText(this, "최소 수치를 입력하세요!", Toast.LENGTH_LONG).show()
                minArea.setSelection(0)
            }  else if(maxArea.text.toString().trim() == "") {
                Toast.makeText(this, "최대 수치를 입력하세요!", Toast.LENGTH_LONG).show()
                maxArea.setSelection(0)
            }  else if(resultArea.text.toString().trim() == "") {
                Toast.makeText(this, "수치값을 입력하세요!", Toast.LENGTH_LONG).show()
                resultArea.setSelection(0)
            } else {
                val date = yearArea.text.toString() + "." + monthArea.text.toString() + "." + dayArea.text.toString()

                plusCheckUpInputNote(date,nameArea.text.toString().trim(), minArea.text.toString().trim(), maxArea.text.toString().trim(), resultArea.text.toString().trim(), partArea.text.toString().trim())
                Toast.makeText(this, "검사 항목 추가 완료!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setDate() {
        val sb = date.split(".")

        yearArea.setText(sb[0])
        monthArea.setText(sb[1])
        dayArea.setText(sb[2])
    }

    private fun plusCheckUpInputNote(date : String, name : String, min : String, max : String, result : String, part : String) {
        val key = FBRef.checkUpInputRef.child(userId).child(dogId).push().key.toString() // 키 값을 먼저 받아옴

        FBRef.checkUpInputRef.child(userId).child(dogId).child(key).setValue(DogCheckUpInputModel(key, dogId, date, name, min, max, result, part)) // 반려견 검사 기록 정보 데이터베이스에 저장
    }
}