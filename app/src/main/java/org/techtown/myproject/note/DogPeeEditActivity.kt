package org.techtown.myproject.note

import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogPeeModel
import org.techtown.myproject.utils.DogWaterModel
import org.techtown.myproject.utils.FBRef

class DogPeeEditActivity : AppCompatActivity() {

    private val TAG = DogPeeEditActivity::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var nowDate : String
    lateinit var dogPeeId :String
    lateinit var peeType : String

    lateinit var peeColor : CircleImageView
    lateinit var peeTypeArea : TextView
    lateinit var peeContentArea : TextView

    lateinit var minusBtn : Button
    lateinit var plusBtn : Button

    lateinit var peeCntArea : EditText
    lateinit var peeCnt : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_pee_edit)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        dogPeeId = intent.getStringExtra("id").toString() // dog pee id
        nowDate = intent.getStringExtra("date").toString() // 선택된 날짜
        peeType = intent.getStringExtra("peeType").toString() // 소변 타입

        findViewById<TextView>(R.id.today).text = nowDate

        peeColor = findViewById(R.id.peeColor)
        peeTypeArea = findViewById(R.id.peeTypeArea)
        peeContentArea = findViewById(R.id.peeContentArea)
        peeCntArea = findViewById(R.id.peeCnt)

        plusBtn = findViewById(R.id.plusBtn)
        minusBtn = findViewById(R.id.minusBtn)

        getData()

        plusBtn.setOnClickListener {
            var nowCnt = peeCntArea.text.toString().toInt()
            nowCnt += 1
            peeCntArea.setText(nowCnt.toString())
        }
        minusBtn.setOnClickListener {
            var nowCnt = peeCntArea.text.toString().toInt()
            if(nowCnt > 0)
                nowCnt -= 1
            peeCntArea.setText(nowCnt.toString())
        }

        val editBtn = findViewById<Button>(R.id.editBtn)
        editBtn.setOnClickListener {

            peeCnt = peeCntArea.text.toString().trim()

            when (peeCnt) {
                "0" -> {
                    Toast.makeText(this, "소변 횟수가 0 이상이어야 합니다!", Toast.LENGTH_SHORT).show()
                    peeCntArea.setSelection(0)
                }
                else -> {
                    saveDogPee(peeCnt)
                    Toast.makeText(this, "소변 횟수가 수정되었습니다!", Toast.LENGTH_SHORT).show()
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
        when (peeType) {
            "transparent" -> {
                peeTypeArea!!.text = "투명한 무색 소변"
                peeContentArea!!.text = "물을 많이 마신 상태"
                peeColor!!.setBackgroundColor(R.drawable.circle_white)
            }
            "lightYellow" -> {
                peeTypeArea!!.text = "투명한 노란색 소변"
                peeContentArea!!.text = "적절한 수분을 보유하고 있어 정상인 상태"
                peeColor!!.setBackgroundColor((Color.parseColor("#FFFFE0")))
            }
            "darkYellow" -> {
                peeTypeArea!!.text = "주황색과 어두운 노란색 소변"
                peeContentArea!!.text = "물을 충분히 마시지 않은 상태이거나, 황달과 관련이 높으며, 간이 손상되었거나 쓸개나 이자에 문제가 생겼을 확률이 높은 상태"
                peeColor!!.setBackgroundColor((Color.parseColor("#FFE4B5")))
            }
            "red" -> {
                peeTypeArea!!.text = "붉은색 소변"
                peeContentArea!!.text = "요로 또는 방광에 일어나는 감염이나 결석, 종양에 의한 방광염, 양파 중독, 타이레놀 중독과 같은 적혈구가 파괴되는 질병과 관련이 높은 상태"
                peeColor!!.setBackgroundColor((Color.parseColor("#FA8072")))
            }
            "brown" -> {
                peeTypeArea!!.text = "갈색 소변"
                peeContentArea!!.text = "혈액 세포 손상, 사고나 외상으로 인한 심각한 근육 손상, 독성 물질에 의한 체내 손상 등을 의심할 수 있으며, 기생충 감염 또는 간질환이 있거나 심각한 탈수 상태일 수 있는 심각한 상태"
                peeColor!!.setBackgroundColor((Color.parseColor("#A0522D")))
            }
        }

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val post = dataSnapshot.getValue(DogPeeModel::class.java)

                    peeCntArea.setText(post!!.peeCount)
                } catch (e: Exception) {
                    Log.d(TAG, " 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.peeRef.child(userId).child(dogId).child(dogPeeId).addValueEventListener(postListener)
    }

    private fun saveDogPee(peeCnt : String) {
        FBRef.peeRef.child(userId).child(dogId).child(dogPeeId).setValue(DogPeeModel(dogPeeId, dogId, nowDate, peeType, peeCnt))  // 반려견 소변 정보 데이터베이스에 저장
    }
}