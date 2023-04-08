package org.techtown.myproject.statistics

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.note.DogCheckUpPictureInActivity
import org.techtown.myproject.note.ImageDetailActivity
import org.techtown.myproject.note.MemoImageAdapter
import org.techtown.myproject.utils.DogCheckUpPictureModel
import org.techtown.myproject.utils.FBRef
import java.util.ArrayList

class CheckUpPictureStatisticsInActivity : AppCompatActivity() {

    private val TAG = CheckUpPictureStatisticsInActivity::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var dogCheckUpPictureId : String

    private lateinit var date : String

    private lateinit var nameArea : TextView
    private lateinit var checkUpCategoryArea : TextView
    private lateinit var dateArea : TextView
    private lateinit var contentArea : TextView

    lateinit var imageListView : RecyclerView
    private val imageDataList = ArrayList<String>() // 검사 사진을 넣는 리스트
    lateinit var memoImageVAdapter : MemoImageAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_up_picture_statistics_in)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        date = intent.getStringExtra("date").toString()
        dogCheckUpPictureId = intent.getStringExtra("id").toString() // dogCheckUpPicture id

        memoImageVAdapter = MemoImageAdapter(imageDataList)
        imageListView = findViewById(R.id.imageListView)
        imageListView.setItemViewCacheSize(20)
        imageListView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        imageListView.layoutManager = layoutManager
        imageListView.adapter = memoImageVAdapter

        nameArea = findViewById(R.id.hospitalArea)
        checkUpCategoryArea = findViewById(R.id.checkUpCategoryArea)
        dateArea = findViewById(R.id.dateArea)
        contentArea = findViewById(R.id.contentArea)

        getCheckUpPictureData(userId, dogId)

        memoImageVAdapter.setItemClickListener(object: MemoImageAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                val intent = Intent(applicationContext, ImageDetailActivity::class.java)
                intent.putExtra("image", imageDataList[position]) // 사진 링크 넘기기
                startActivity(intent)
            }
        })

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun getCheckUpPictureData(userId : String, dogId : String) { // 파이어베이스로부터 검사 사진 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val dataModel = dataSnapshot.getValue(DogCheckUpPictureModel::class.java)

                    checkUpCategoryArea.text = dataModel!!.checkUpCategory
                    nameArea.text = dataModel!!.hospitalName
                    dateArea.text = dataModel!!.date
                    contentArea.text = dataModel!!.content

                    imageDataList.clear()

                    if(dataModel.count.toInt() >= 1) {
                        for(index in 0 until dataModel.count.toInt()) {
                            imageDataList.add("checkUpImage/$userId/$dogId/$dogCheckUpPictureId/$dogCheckUpPictureId$index.png")
                        }
                        Log.d("imageDataList", imageDataList.toString())
                    }
                    memoImageVAdapter.notifyDataSetChanged()

                } catch (e: Exception) {
                    Log.d(TAG, "검사 사진 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.checkUpPictureRef.child(userId).child(dogId).child(dogCheckUpPictureId).addValueEventListener(postListener)
    }
}