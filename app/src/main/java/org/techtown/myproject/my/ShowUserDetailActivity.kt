package org.techtown.myproject.my

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.UserInfo

class ShowUserDetailActivity : AppCompatActivity() {

    private val TAG = UserManageActivity::class.java.simpleName

    lateinit var userId : String

    lateinit var backBtn : ImageView

    lateinit var profileFile : String
    lateinit var textView : TextView
    lateinit var imageView : CircleImageView
    lateinit var  nameArea : TextView
    lateinit var emailArea : TextView
    lateinit var nickNameArea : TextView

    lateinit var userDogsRecyclerView : RecyclerView
    private val userDogsReDataList = ArrayList<DogModel>() // 사용자의 반려견 프로필을 넣는 리스트
    lateinit var userDogsReVAdapter: UserDogsReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_user_detail)

        userId = intent.getStringExtra("userId").toString() // 사용자의 uid를 받아옴

        getData()

        getUserInfo()

        getUserDogs()

        backBtn = findViewById(R.id.back)
        backBtn.setOnClickListener { // 뒤로 가기 버튼 클릭 시 화면 나오기
            finish()
        }
    }

    private fun getData() {
        textView = findViewById(R.id.textView)
        imageView = findViewById(R.id.imageView)
        nameArea = findViewById(R.id.nameArea)
        emailArea = findViewById(R.id.emailArea)
        nickNameArea = findViewById(R.id.nickNameArea)

        userDogsReVAdapter = UserDogsReVAdapter(userDogsReDataList)
        userDogsRecyclerView = findViewById(R.id.userDogsRecyclerView)
        userDogsRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        userDogsRecyclerView.layoutManager = layoutManager
        userDogsRecyclerView.adapter = userDogsReVAdapter
    }

    private fun getUserInfo() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val post = dataSnapshot.getValue(UserInfo::class.java)

                textView.text = post!!.userName
                nameArea.text = post!!.userName
                nickNameArea.text = post!!.nickName
                emailArea.text = post!!.email

                profileFile = post!!.profileImage // 가져올 유저의 profile 사진

                val profileFile =
                    FBRef.userRef.child(userId).child("profileImage").get().addOnSuccessListener {
                        val storageReference =
                            Firebase.storage.reference.child(it.value.toString()) // 유저의 profile 사진을 DB의 storage로부터 가져옴

                        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Glide.with(applicationContext).load(task.result)
                                    .into(imageView) // 유저의 profile 사진을 게시자 이름의 왼편에 표시함
                            } else {
                                imageView.isVisible = false
                            }
                        })
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.userRef.child(userId).addValueEventListener(postListener)
    }

    private fun getUserDogs() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                userDogsReDataList.clear()

                for(dataModel in dataSnapshot.children) {
                    Log.d(TAG, dataModel.toString())
                    val item = dataModel.getValue(DogModel::class.java)
                    userDogsReDataList.add(item!!)
                }

                userDogsReVAdapter.notifyDataSetChanged()

                Log.d(TAG, userDogsReDataList.toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dogRef.child(userId).addValueEventListener(postListener)
    }
}