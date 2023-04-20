package org.techtown.myproject.my

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.community.SpecificCommunityInActivity
import org.techtown.myproject.utils.DogMealModel
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.UserInfo

class UserManageActivity : AppCompatActivity() {

    private val TAG = UserManageActivity::class.java.simpleName

    lateinit var backBtn : ImageView

    lateinit var usersRecyclerView : RecyclerView
    private val usersReDataList = ArrayList<UserInfo>() // 사용자의 프로필을 넣는 리스트
    lateinit var usersReVAdapter: UsersReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_manage)

        setData()

        getUsers()

        usersReVAdapter.setItemClickListener(object: UsersReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val intent = Intent(applicationContext, ShowUserDetailActivity::class.java)
                intent.putExtra("userId", usersReDataList[position].uid) // 선택된 사용자의 uid를 넘겨줌
                startActivity(intent)
            }
        })

        backBtn = findViewById(R.id.back)
        backBtn.setOnClickListener { // 뒤로 가기 버튼 클릭 시 화면 나오기
            finish()
        }
    }

    private fun setData() {
        usersReVAdapter = UsersReVAdapter(usersReDataList)
        usersRecyclerView = findViewById(R.id.usersRecyclerView)
        usersRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        usersRecyclerView.layoutManager = layoutManager
        usersRecyclerView.adapter = usersReVAdapter
    }

    private fun getUsers() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 사료 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                    usersReDataList.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(UserInfo::class.java)
                        usersReDataList.add(item!!)
                    }

                    Log.d("usersReDataList", usersReDataList.toString())
                    usersReVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, "사용자 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.userRef.addValueEventListener(postListener)
    }
}