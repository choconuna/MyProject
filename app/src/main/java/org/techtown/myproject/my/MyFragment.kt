package org.techtown.myproject.my

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.LogoutActivity
import org.techtown.myproject.R
import org.techtown.myproject.comment.CommentModel
import org.techtown.myproject.community.CommunityInActivity
import org.techtown.myproject.community.CommunityListVAdapter
import org.techtown.myproject.community.CommunityModel
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.UserInfo
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.ReCommentModel
import androidx.recyclerview.widget.RecyclerView as RecyclerView

class MyFragment : Fragment() {
    private val TAG = MyFragment::class.java.simpleName

    lateinit var mDatabaseReference: DatabaseReference
    lateinit var mAuth : FirebaseAuth
    lateinit var uid : String
    lateinit var nickName : String
    lateinit var nickNameArea : TextView
    lateinit var checkManager : TextView
    lateinit var checkUser : TextView
    lateinit var userName : String
    lateinit var userNameArea : TextView
    lateinit var imageView : ImageView

    lateinit var profileFile : String

    lateinit var dogReView : RecyclerView
    private val dogReDataList = ArrayList<DogModel>() // 각 반려견의 프로필을 넣는 리스트
    private val dogKeyList = mutableListOf<String>() // 각 반려견의 키값을 넣는 리스트
    lateinit var dogReVAdapter: DogReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    lateinit var managerLine : LinearLayout
    lateinit var userManagement : LinearLayout

    lateinit var userEmail : String
    lateinit var managerEmail : String

    lateinit var sharedPreferences: SharedPreferences

    lateinit var myCommunityArea : ConstraintLayout
    lateinit var myWritingCnt : TextView
    lateinit var myCommentCnt : TextView
    lateinit var myReCommentCnt : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v : View? = inflater.inflate(R.layout.fragment_my, container, false)

        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser?.uid.toString()

        mDatabaseReference = FBRef.userRef.child(uid)
        setProfile()

        managerLine = v!!.findViewById(R.id.managerLine)
        userManagement = v!!.findViewById(R.id.userManagement)

        sharedPreferences = v!!.context.getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        managerEmail = sharedPreferences.getString("manager", "").toString() // 현재 대표 반려견의 id

        checkManager = v!!.findViewById(R.id.checkManager)
        checkUser = v!!.findViewById(R.id.checkUser)

        val userEmail = FBRef.userRef.child(uid).child("email").get().addOnSuccessListener {
            userEmail = it.value.toString() // 현재 로그인된 사용자의 이메일을 가져옴

            if(userEmail == managerEmail) { // 현재 사용자의 이메일과 관리자 이메일이 같다면
                userManagement.visibility = VISIBLE // 사용자 관리 옵션이 보이도록 함
                checkManager.visibility = VISIBLE // 관리자라는 표시가 보이도록 함
                checkUser.visibility = GONE // 사용자라는 표시가 보이지 않도록 함
                managerLine.visibility = VISIBLE
            } else {
                checkManager.visibility = GONE // 관리자라는 표시가 보이지 않도록 함
                checkUser.visibility = VISIBLE // 사용자라는 표시가 보이도록 함
                managerLine.visibility = GONE
            }
        }

        userManagement.setOnClickListener { // 사용자 관리 화면으로 넘어감
            val intent = Intent(v!!.context, UserManageActivity::class.java)
            startActivity(intent)
        }

        nickNameArea = v!!.findViewById(R.id.nickNameArea)
        userNameArea = v!!.findViewById(R.id.userNameArea)
        imageView = v!!.findViewById(R.id.imageView)

        myCommunityArea = v!!.findViewById(R.id.myCommunityArea)

        myWritingCnt = v!!.findViewById(R.id.myWritingCnt)
        myCommentCnt = v!!.findViewById(R.id.myCommentCnt)
        myReCommentCnt = v!!.findViewById(R.id.myReCommentCnt)

        dogReVAdapter = DogReVAdapter(dogReDataList)
        dogReView = v!!.findViewById(R.id.dogRecyclerView)
        dogReView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        dogReView.layoutManager = layoutManager
        dogReView.adapter = dogReVAdapter

        getFBDogData()
        setMyCommunityCount()

        val profileEditBtn = v?.findViewById<Button>(R.id.editProfile)
        profileEditBtn!!.setOnClickListener {
            val intent = Intent(context, ProfileEditActivity::class.java)
            startActivity(intent)
        }

        val editDogBtn = v?.findViewById<LinearLayout>(R.id.editDogs)
        editDogBtn!!.setOnClickListener {
            val intent = Intent(context, WriteDogProfileActivity::class.java)
            startActivity(intent)
        }

        myCommunityArea.setOnClickListener { // 나의 활동을 보여주는 activity로 이동
            val intent = Intent(v!!.context, ShowMyCommunityActivity::class.java)
            requireActivity().startActivity(intent)
        }

        val mySellRecord = v?.findViewById<LinearLayout>(R.id.mySellRecord)
        mySellRecord.setOnClickListener {
            val intent = Intent(v!!.context, ShowMyDealActivity::class.java)
            startActivity(intent)
        }

        val myBuyRecord = v?.findViewById<LinearLayout>(R.id.myBuyRecord)
        myBuyRecord.setOnClickListener {
            val intent = Intent(v!!.context, ShowMyBuyActivity::class.java)
            startActivity(intent)
        }

        val mainDogChoice = v?.findViewById<LinearLayout>(R.id.choiceMainDog)
        mainDogChoice!!.setOnClickListener {
            val intent = Intent(context, ChoiceMainDogActivity::class.java)
            startActivity(intent)
        }

        val logout = v?.findViewById<LinearLayout>(R.id.logout)
        logout!!.setOnClickListener {
            val intent = Intent(context, LogoutActivity::class.java)
            startActivity(intent) // 로그아웃 액티비티 실행시키기
        }

        // 각각의 반려견 프로필 클릭 시 반려견 프로필 화면 보이기
        dogReVAdapter.setItemClickListener(object: DogReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                val intent = Intent(context, DogProfileInActivity::class.java)
                intent.putExtra("key", dogKeyList[position])
                startActivity(intent)
            }
        })

        // Fragment 클래스에서 사용 시
        refreshFragment(this, requireFragmentManager())

        return v
    }

    private fun setProfile() { // 프로필 설정
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val post = dataSnapshot.getValue(UserInfo::class.java)
                nickName = post!!.nickName
                nickNameArea.text = nickName
                userName = post!!.userName
                userNameArea.text = userName

                profileFile = post!!.profileImage // 가져올 유저의 profile 사진

                val profileFile =
                    mDatabaseReference.child("profileImage").get().addOnSuccessListener {
                        val storageReference =
                            Firebase.storage.reference.child(it.value.toString()) // 유저의 profile 사진을 DB의 storage로부터 가져옴

                        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Glide.with(context!!).load(task.result)
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

        mDatabaseReference.addValueEventListener(postListener)
    }

    private fun getFBDogData() { // 파이어베이스로부터 반려견 프로필 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                dogKeyList.clear()
                dogReDataList.clear()
                // dogDataList.clear() // 똑같은 데이터 복사 출력되는 것 막기 위한 초기화

                for(dataModel in dataSnapshot.children) {
                    Log.d(TAG, dataModel.toString())
                    val item = dataModel.getValue(DogModel::class.java)
                    dogReDataList.add(item!!)
                    dogKeyList.add(dataModel.key!!)
                    Log.d("key", dogKeyList.toString())
                    // dogDataList.add(item!!)
                }

                dogReVAdapter.notifyDataSetChanged()
                // dogRVAdapter.notifyDataSetChanged() // 동기화

                Log.d(TAG, dogReDataList.toString())
                // Log.d(TAG, dogDataList.toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dogRef.child(uid).addValueEventListener(postListener)
    }

    private fun setMyCommunityCount() {
        var myWriting = 0
        var myComment = 0
        var myReComment = 0

        var categoryList : List<String> = listOf("정보", "후기", "자유", "질문")
        for(index in categoryList) {
            Log.d("categoryList", index)
            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {

                        for(dataModel in dataSnapshot.children) {
                            Log.d(TAG, dataModel.toString())
                            val item = dataModel.getValue(CommunityModel::class.java)
                            if(item!!.uid == uid)
                                myWriting++

                            val postListener = object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    try {

                                        for (dataModel in dataSnapshot.children) {
                                            val item = dataModel.getValue(CommentModel::class.java)

                                            if(item!!.uid == uid)
                                                myComment++

                                            val postListener = object : ValueEventListener {
                                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                    try {

                                                        for(dataModel in dataSnapshot.children) {
                                                            val item = dataModel.getValue(
                                                                ReCommentModel::class.java)

                                                            if(item!!.uid == uid)
                                                                myReComment++
                                                        }

                                                        myReCommentCnt.text = myReComment.toString()
                                                    } catch(e : Exception) {
                                                    }
                                                }

                                                override fun onCancelled(databaseError: DatabaseError) {
                                                }
                                            }
                                            FBRef.reCommentRef.child(item!!.communityId).child(item!!.commentId).addValueEventListener(postListener)
                                        }

                                        myCommentCnt.text = myComment.toString()
                                    } catch(e : Exception) {
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Getting Post failed, log a message
                                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                                }
                            }
                            FBRef.commentRef.child(item!!.communityId).addValueEventListener(postListener)
                        }

                        myWritingCnt.text = myWriting.toString()
                    } catch(e : Exception) {
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                }
            }
            FBRef.communityRef.child(index).addValueEventListener(postListener)
        }
    }

    // Fragment 새로고침
    fun refreshFragment(fragment: Fragment, fragmentManager: FragmentManager) {
        var ft: FragmentTransaction = fragmentManager.beginTransaction()
        ft.detach(fragment).attach(fragment).commit()
    }
}