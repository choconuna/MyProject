package org.techtown.myproject.note

import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogDungModel
import org.techtown.myproject.utils.DogPeeModel
import org.techtown.myproject.utils.FBRef

class DogDungEditActivity : AppCompatActivity() {

    private val TAG = DogDungEditActivity::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var nowDate : String
    lateinit var dogDungId :String
    lateinit var dungType : String

    lateinit var dungColor : CircleImageView
    lateinit var dungTypeArea : TextView
    lateinit var dungContentArea : TextView

    lateinit var minusBtn : Button
    lateinit var plusBtn : Button

    lateinit var dungCntArea : EditText
    lateinit var dungCnt : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_dung_edit)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        dogDungId = intent.getStringExtra("id").toString() // dog pee id
        nowDate = intent.getStringExtra("date").toString() // 선택된 날짜
        dungType = intent.getStringExtra("dungType").toString() // 대변 타입

        findViewById<TextView>(R.id.today).text = nowDate

        dungColor = findViewById(R.id.dungColor)
        dungTypeArea = findViewById(R.id.dungTypeArea)
        dungContentArea = findViewById(R.id.dungContentArea)
        dungCntArea = findViewById(R.id.dungCnt)

        plusBtn = findViewById(R.id.plusBtn)
        minusBtn = findViewById(R.id.minusBtn)

        getData()

        plusBtn.setOnClickListener {
            var nowCnt = dungCntArea.text.toString().toInt()
            nowCnt += 1
            dungCntArea.setText(nowCnt.toString())
        }
        minusBtn.setOnClickListener {
            var nowCnt = dungCntArea.text.toString().toInt()
            if(nowCnt > 0)
                nowCnt -= 1
            dungCntArea.setText(nowCnt.toString())
        }

        val editBtn = findViewById<Button>(R.id.editBtn)
        editBtn.setOnClickListener {

            dungCnt = dungCntArea.text.toString().trim()

            when (dungCnt) {
                "0" -> {
                    Toast.makeText(this, "대변 횟수가 0 이상이어야 합니다!", Toast.LENGTH_SHORT).show()
                    dungCntArea.setSelection(0)
                }
                else -> {
                    saveDogDung(dungCnt)
                    Toast.makeText(this, "대변 횟수가 수정되었습니다!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun getData() {
        when (dungType) {
            "regular" -> {
                dungTypeArea!!.text = "보통 변"
                dungContentArea!!.text = "적정한 수분을 보유하고 있는 정상인 상태"
                dungColor!!.setBackgroundColor(Color.parseColor("#CD853F"))
            }
            "watery" -> {
                dungTypeArea!!.text = "묽은 변"
                dungContentArea!!.text = "변의 경도는 수분 섭취량에 따라 일시적으로 대변이 무르거나 딱딱할 수 있습니다."
                dungColor!!.setBackgroundColor((Color.parseColor("#DEB887")))
            }
            "diarrhea" -> {
                dungTypeArea!!.text = "설사"
                dungContentArea!!.text = "변의 경도는 수분 섭취량에 따라 일시적으로 대변이 무를 수 있지만, 지속적인 실사는 장에 세균성 감염이 원인일 수 있으므로 가까운 동물 병원에 내원하셔야 합니다."
                dungColor!!.setBackgroundColor((Color.parseColor("#CD853F")))
            }
            "hard" -> {
                dungTypeArea!!.text = "짙고 딱딱한 변"
                dungContentArea!!.text = "변비 기미가 있거나, 신장에 문제가 있을 경우에 볼 수 있습니다.\\n또한 사료양이 적을 때도 나타날 수 있습니다."
                dungColor!!.setBackgroundColor((Color.parseColor("#A0522D")))
            }
            "red" -> {
                dungTypeArea!!.text = "붉은색 변"
                dungContentArea!!.text = "소화기, 특히 상복부 소화기에 출혈이 있음을 알 수 있는 경우로 빠른 시간 내로 수의사의 진찰을 받는 것이 중요합니다."
                dungColor!!.setBackgroundColor((Color.parseColor("#8B0000")))
            }
            "black" -> {
                dungTypeArea!!.text = "검은색 변"
                dungContentArea!!.text = "상부 소화기의 출혈이 생겼거나, 수분이 부족해 나타날 수 있습니다.\\n의심이 되는 경우 수의사의 진찰을 받는 것이 중요합니다."
                dungColor!!.setBackgroundColor((Color.parseColor("#696969")))
            }
            "white" -> {
                dungTypeArea!!.text = "하얀색 점이 있는 변"
                dungContentArea!!.text = "변에 보이는 하얀색 점을 기생충 감염을 의미할 수 있으므로\\n정확한 진단이 필요한 상태"
                dungColor!!.setBackgroundColor((Color.parseColor("#CD853F")))
            }
        }

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val post = dataSnapshot.getValue(DogDungModel::class.java)

                    dungCntArea.setText(post!!.dungCount)
                } catch (e: Exception) {
                    Log.d(TAG, " 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dungRef.child(userId).child(dogId).child(dogDungId).addValueEventListener(postListener)
    }

    private fun saveDogDung(dungCnt : String) {
        FBRef.dungRef.child(userId).child(dogId).child(dogDungId).setValue(DogDungModel(dogDungId, dogId, nowDate, dungType, dungCnt))  // 반려견 대변 정보 데이터베이스에 저장
    }
}