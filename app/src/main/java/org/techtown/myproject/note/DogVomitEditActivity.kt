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
import org.techtown.myproject.utils.DogVomitModel
import org.techtown.myproject.utils.FBRef

class DogVomitEditActivity : AppCompatActivity() {

    private val TAG = DogVomitEditActivity::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var nowDate : String
    lateinit var dogVomitId :String
    lateinit var vomitType : String

    lateinit var vomitColor : CircleImageView
    lateinit var vomitTypeArea : TextView
    lateinit var vomitContentArea : TextView

    lateinit var minusBtn : Button
    lateinit var plusBtn : Button

    lateinit var vomitCntArea : EditText
    lateinit var vomitCnt : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_vomit_edit)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        dogVomitId = intent.getStringExtra("id").toString() // dog vomit id
        nowDate = intent.getStringExtra("date").toString() // 선택된 날짜
        vomitType = intent.getStringExtra("vomitType").toString() // 구토 타입

        findViewById<TextView>(R.id.today).text = nowDate

        vomitColor = findViewById(R.id.vomitColor)
        vomitTypeArea = findViewById(R.id.vomitTypeArea)
        vomitContentArea = findViewById(R.id.vomitContentArea)
        vomitCntArea = findViewById(R.id.vomitCnt)

        plusBtn = findViewById(R.id.plusBtn)
        minusBtn = findViewById(R.id.minusBtn)

        getData()

        plusBtn.setOnClickListener {
            var nowCnt = vomitCntArea.text.toString().toInt()
            nowCnt += 1
            vomitCntArea.setText(nowCnt.toString())
        }
        minusBtn.setOnClickListener {
            var nowCnt = vomitCntArea.text.toString().toInt()
            if(nowCnt > 0)
                nowCnt -= 1
            vomitCntArea.setText(nowCnt.toString())
        }

        val editBtn = findViewById<Button>(R.id.editBtn)
        editBtn.setOnClickListener {

            vomitCnt = vomitCntArea.text.toString().trim()

            when (vomitCnt) {
                "0" -> {
                    Toast.makeText(this, "구토 횟수가 0 이상이어야 합니다!", Toast.LENGTH_SHORT).show()
                    vomitCntArea.setSelection(0)
                }
                else -> {
                    saveDogVomit(vomitCnt)
                    Toast.makeText(this, "구토 횟수가 수정되었습니다!", Toast.LENGTH_SHORT).show()
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
        when (vomitType) {
            "transparent" -> {
                vomitTypeArea!!.text = "투명한 무색 구토"
                vomitContentArea!!.text = "물, 위액, 침이 역류한 것입니다. 한 번 정도 증상을 보이는 것은 문제가 없을 경우가 많으니 하루 정도 지켜봐 주세요."
                vomitColor!!.setBackgroundResource(R.drawable.circle_white)
            }
            "bubble" -> {
                vomitTypeArea!!.text = "흰색 거품이 섞인 구토"
                vomitContentArea!!.text = "토가 역류할 때 공기를 삼켜 거품이 된 것입니다. 큰 문제가 없는 경우가 많으니 지켜봐 주세요."
                vomitColor!!.setBackgroundResource(R.drawable.bubble)
            }
            "food" -> {
                vomitTypeArea!!.text = "음식이 섞인 구토"
                vomitContentArea!!.text = "사료나 간식을 급하게 먹거나, 과식하는 경우 소화불량으로 구토하는 경우. 소화가 잘 되게 작게 잘라 주거나, 사료를 물에 불려 급여해 주세요."
                vomitColor!!.setBackgroundColor(Color.parseColor("#CD853F"))
            }
            "yellow" -> {
                vomitTypeArea!!.text = "노란색 구토"
                vomitContentArea!!.text = "주로 불규칙한 식습관과 식사 간격이 문제될 때 발생하며, 공북의 상태가 길어질 경우 담즙(소화액)이 위를 자극해 거품과 함께 구토하는 경우가 많습니다."
                vomitColor!!.setBackgroundColor(Color.parseColor("#FFE4B5"))
            }
            "leaf" -> {
                vomitTypeArea!!.text = "잎사귀가 섞인 초록색 구토"
                vomitContentArea!!.text = "위장 상태가 나쁠 때 일부러 잎사귀를 먹고 위 속을 토해냅니다. 구토 후 기력이 없지 않은지 지켜봐 주세요. 이상이 있을 경우 바로 병원에 데려가는 게 좋습니다."
                vomitColor!!.setBackgroundResource(R.drawable.leaf)
            }
            "pink" -> {
                vomitTypeArea!!.text = "분홍색 구토"
                vomitContentArea!!.text = "입 안 또는 식도, 위, 장에 부정적인 출혈이 생겼을 수 있으니 지켜봐 주세요. 가끔 일어나는 증상일 경우 문제가 없을 수 있으나, 여러 번 반복하여 하는 경우 병원에 방문해 주세요."
                vomitColor!!.setBackgroundColor(Color.parseColor("#FFC0CB"))
            }
            "brown" -> {
                vomitTypeArea!!.text = "짙은 갈색 구토"
                vomitContentArea!!.text = "상부 소화기에 출혈이 생긴 경우가 있으며, 질병을 의심할 수 있습니다. 출혈이 많을 경우 위험한 상황이 발생할 수 있어, 반드시 병원에 방문하세요!"
                vomitColor!!.setBackgroundColor(Color.parseColor("#8B4513"))
            }
            "green" -> {
                vomitTypeArea!!.text = "녹색 구토"
                vomitContentArea!!.text = "주로 식습관과 식사 간격이 문제될 때 발생하며, 공복의 사태가 길어질 경우 담즙(소화액)이 위를 자극해 거품과 함께 구토하는 경우가 많습니다. 반복적인 녹색 구토는 췌장염이나 장폐색 같은 응급 질환과 관련 있을 가능성이 있으므로 반드시 병원에 내원하세요!"
                vomitColor!!.setBackgroundColor(Color.parseColor("#228B22"))
            }
            "substance" -> {
                vomitTypeArea!!.text = "이물질이 섞인 구토"
                vomitContentArea!!.text = "이물질이 역류하여 장이나 위에 상처가 생길 수 있습니다. 구토하는 도중 식도가 막히면 위험한 상황이 발생할 수 있으니 반드시 병원에 방문하세요!"
                vomitColor!!.setBackgroundResource(R.drawable.snow)
            }
            "red" -> {
                vomitTypeArea!!.text = "붉은색 구토"
                vomitContentArea!!.text = "입 속, 식도, 위, 장에 출혈이 크게 생겼을 수 있습니다. 출혈량이 많다면 위험한 상황일 수 있으니 병원에 꼭 내원하세요!"
                vomitColor!!.setBackgroundColor(Color.parseColor("#B22222"))
            }
        }

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val post = dataSnapshot.getValue(DogVomitModel::class.java)

                    vomitCntArea.setText(post!!.vomitCount)
                } catch (e: Exception) {
                    Log.d(TAG, " 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.vomitRef.child(userId).child(dogId).child(dogVomitId).addValueEventListener(postListener)
    }

    private fun saveDogVomit(vomitCnt : String) {
        FBRef.vomitRef.child(userId).child(dogId).child(dogVomitId).setValue(DogVomitModel(dogVomitId, dogId, nowDate, vomitType, vomitCnt))  // 반려견 구토 정보 데이터베이스에 저장
    }
}