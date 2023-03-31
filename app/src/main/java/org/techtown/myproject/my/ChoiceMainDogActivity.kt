package org.techtown.myproject.my

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef

class ChoiceMainDogActivity : AppCompatActivity() {

    private val TAG = ChoiceMainDogActivity::class.java.simpleName

    lateinit var mAuth : FirebaseAuth
    lateinit var uid : String

    private lateinit var sharedPreferences: SharedPreferences // 대표 반려견의 id를 로그인이 유지되어 있는 동안 저장하기 위함
    lateinit var editor : SharedPreferences.Editor

    lateinit var dogListView : ListView
    private val dogDataList = mutableListOf<DogModel>() // 각 반려견의 프로필을 넣는 리스트
    lateinit var dogRVAdapter : DogListVAdapter
    private val dogKeyList = mutableListOf<String>() // 각 반려견의 키값을 넣는 리스트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice_main_dog)

        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser?.uid.toString()

        dogRVAdapter = DogListVAdapter(dogDataList)
        dogListView = findViewById(R.id.dogListView)
        dogListView.adapter = dogRVAdapter

        // 프로필 클릭 시 해당 반려견을 대표 반려견으로 선정
        dogListView.setOnItemClickListener { _, _, position, _ ->
            sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE) // sharedPreferences 이름의 기본모드 설정
            val editor = sharedPreferences.edit() //sharedPreferences를 제어할 editor를 선언
            editor.putString(uid, dogKeyList[position]) // key,value 형식으로 저장
            editor.commit() //최종 커밋. 커밋을 해야 저장됨.
            Toast.makeText(this, "대표 반려견으로 선택되었습니다!", Toast.LENGTH_SHORT).show()
        }

        getFBDogData()

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun getFBDogData() { // 파이어베이스로부터 반려견 프로필 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                dogKeyList.clear()
                dogDataList.clear() // 똑같은 데이터 복사 출력되는 것 막기 위한 초기화

                for(dataModel in dataSnapshot.children) {
                    Log.d(TAG, dataModel.toString())
                    val item = dataModel.getValue(DogModel::class.java)
                    dogKeyList.add(dataModel.key!!)
                    Log.d("mainKey", dogKeyList.toString())
                    dogDataList.add(item!!)
                }

                dogRVAdapter.notifyDataSetChanged() // 동기화

                Log.d(TAG, dogDataList.toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dogRef.child(uid).addValueEventListener(postListener)
    }
}