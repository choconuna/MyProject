package org.techtown.myproject.note

import android.app.Activity
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogCheckUpInputModel
import org.techtown.myproject.utils.DogMedicineModel
import org.techtown.myproject.utils.FBRef

class DogCheckUpInputEditActivity : AppCompatActivity() {

    private val TAG = DogCheckUpInputEditActivity::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var dogCheckUpInputId : String

    private lateinit var date : String
    private lateinit var name : String
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
        setContentView(R.layout.activity_dog_check_up_input_edit)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        dogCheckUpInputId = intent.getStringExtra("id").toString() // dogCheckUpInput id

        nameArea = findViewById(R.id.nameArea)

        yearArea = findViewById(R.id.yearArea)
        monthArea = findViewById(R.id.monthArea)
        dayArea = findViewById(R.id.dayArea)

        partArea = findViewById(R.id.partArea)
        minArea = findViewById(R.id.minArea)
        maxArea = findViewById(R.id.maxArea)
        resultArea = findViewById(R.id.resultArea)

        getData()

        val editBtn = findViewById<Button>(R.id.editBtn)
        editBtn.setOnClickListener {

            name = nameArea.text.toString().trim()

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

                editCheckUpInputNote(date, nameArea.text.toString().trim(), minArea.text.toString().trim(), maxArea.text.toString().trim(), resultArea.text.toString().trim(), partArea.text.toString())
                Toast.makeText(this, "검사 항목 수정 완료!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun getData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val post = dataSnapshot.getValue(DogCheckUpInputModel::class.java)

                    nameArea.setText(post!!.name)

                    val date = post!!.date
                    val dateSplit = date.split(".")
                    yearArea.setText(dateSplit[0])
                    monthArea.setText(dateSplit[1])
                    dayArea.setText(dateSplit[2])

                    partArea.setText(post!!.part)
                    minArea.setText(post!!.min)
                    maxArea.setText(post!!.max)
                    resultArea.setText(post!!.result)

                } catch (e: Exception) {
                    Log.d(TAG, " 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.checkUpInputRef.child(userId).child(dogId).child(dogCheckUpInputId).addValueEventListener(postListener)
    }

    private fun editCheckUpInputNote(date : String, name : String, min : String, max : String, result : String, part : String) {

        FBRef.checkUpInputRef.child(userId).child(dogId).child(dogCheckUpInputId).setValue(DogCheckUpInputModel(dogCheckUpInputId, dogId, date, name, min, max, result, part)) // 반려견 검사 기록 정보 데이터베이스에 저장
    }
}