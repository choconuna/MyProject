package org.techtown.myproject.note

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.community.CommunityImageAdapter
import org.techtown.myproject.community.CommunityModel
import org.techtown.myproject.community.SpecificCommunityInActivity
import org.techtown.myproject.utils.DogMemoModel
import org.techtown.myproject.utils.FBRef
import java.util.ArrayList

class DogMemoInActivity : AppCompatActivity() {

    private val TAG = DogMemoInActivity::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    private lateinit var myUid : String
    private lateinit var dogId : String
    lateinit var dogMemoId : String
    lateinit var nowDate : String

    lateinit var titleArea : TextView
    lateinit var dateArea : TextView
    lateinit var timeArea : TextView
    lateinit var contentArea : TextView
    lateinit var memoSetIcon : ImageView

    lateinit var imageListView : RecyclerView
    private val imageDataList = ArrayList<String>() // 게시글의 사진을 넣는 리스트
    lateinit var memoImageVAdapter : MemoImageAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_memo_in)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(myUid, "").toString() // 현재 대표 반려견의 id
        dogMemoId = intent.getStringExtra("id").toString() // dogMemo id
        nowDate = intent.getStringExtra("date").toString() // 선택된 날짜를 받아옴

        memoImageVAdapter = MemoImageAdapter(imageDataList)
        imageListView = findViewById(R.id.imageListView)
        imageListView.setItemViewCacheSize(20)
        imageListView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        imageListView.layoutManager = layoutManager
        imageListView.adapter = memoImageVAdapter

        titleArea = findViewById(R.id.titleArea)
        dateArea = findViewById(R.id.dateArea)
        timeArea = findViewById(R.id.timeArea)
        contentArea = findViewById(R.id.contentArea)

        getMemoData(myUid, dogId)

        memoImageVAdapter.setItemClickListener(object: MemoImageAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                val intent = Intent(applicationContext, ImageDetailActivity::class.java)
                intent.putExtra("image", imageDataList[position]) // 사진 링크 넘기기
                startActivity(intent)
            }
        })

        memoSetIcon = findViewById(R.id.memoSet)
        memoSetIcon.setOnClickListener {
            showDialog(dogMemoId)
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun showDialog(id : String) { // 메모 수정/삭제를 위한 다이얼로그 띄우기
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.eat_dialog, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)

        val alertDialog = mBuilder.show()
        val editBtn = alertDialog.findViewById<Button>(R.id.editBtn)
        editBtn?.setOnClickListener { // 수정 버튼 클릭 시
            Log.d(TAG, "edit Button Clicked")

            val intent = Intent(this, DogMemoEditActivity::class.java)
            intent.putExtra("id", id) // dogMemo Id 전송
            intent.putExtra("date", nowDate) // 선택된 날짜 전송
            startActivity(intent) // 수정 페이지로 이동

            alertDialog.dismiss()
        }

        val rmBtn = alertDialog.findViewById<Button>(R.id.rmBtn)
        rmBtn?.setOnClickListener {  // 삭제 버튼 클릭 시
            Log.d(TAG, "remove Button Clicked")
            alertDialog.dismiss()

            deleteMemoImage(myUid, dogId, id) // 메모 이미지 삭제
            FBRef.memoRef.child(myUid).child(dogId).child(id).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제
            Toast.makeText(this, "메모 삭제 완료", Toast.LENGTH_SHORT).show()

            finish()
        }
    }

    private fun deleteMemoImage(userId : String, dogId : String, dogMemoId : String) {
        val postListener = object : ValueEventListener { // 파이어베이스 안의 값이 변화할 시 작동됨
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 사진 삭제 후 그 키 값에 해당하는 사진이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                    val dataModel = dataSnapshot.getValue(DogMemoModel::class.java)

                    if (dataModel!!.count.toInt() >= 1) {
                        for (index in 0 until dataModel.count.toInt()) {
                            Firebase.storage.reference.child("memoImage/$userId/$dogId/$dogMemoId/$dogMemoId$index.png")
                                .delete().addOnSuccessListener { // 사진 삭제
                                }.addOnFailureListener {
                                }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "사진 삭제 완료")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.memoRef.child(userId).child(dogId).child(dogMemoId).addValueEventListener(postListener)
    }

    private fun getMemoData(userId : String, dogId : String) { // 파이어베이스로부터 메모 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val dataModel = dataSnapshot.getValue(DogMemoModel::class.java)

                    titleArea.text = dataModel!!.title
                    dateArea.text = dataModel!!.date
                    timeArea.text = dataModel!!.time
                    contentArea.text = dataModel!!.content

                    imageDataList.clear()

                    if(dataModel.count.toInt() >= 1) {
                        for(index in 0 until dataModel.count.toInt()) {
                            imageDataList.add("memoImage/$userId/$dogId/$dogMemoId/$dogMemoId$index.png")
                        }
                        Log.d("imageDataList", imageDataList.toString())
                    }
                    memoImageVAdapter.notifyDataSetChanged()

                } catch (e: Exception) {
                    Log.d(TAG, "메모 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.memoRef.child(userId).child(dogId).child(dogMemoId).addValueEventListener(postListener)
    }
}