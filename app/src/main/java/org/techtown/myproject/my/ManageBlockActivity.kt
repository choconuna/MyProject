package org.techtown.myproject.my

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
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
import org.techtown.myproject.R
import org.techtown.myproject.utils.BlockModel
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.UserInfo

class ManageBlockActivity : AppCompatActivity() {

    private val TAG = ManageBlockActivity::class.java.simpleName

    private lateinit var myUid : String

    private val blockList = mutableListOf<String>() // 차단된 uid 값을 넣는 리스트

    lateinit var blockRecyclerView : RecyclerView
    private val blockDataList = ArrayList<UserInfo>() // 차단한 유저의 프로필을 넣는 리스트
    lateinit var blockReVAdapter : BlockReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_block)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        setData()

        getBlockUsers()

        blockReVAdapter.setItemClickListener(object: BlockReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                FBRef.userRef.child(blockDataList[position].uid).child("nickName").get().addOnSuccessListener {
                    var userName = it.value.toString() // 게시글에 작성자의 닉네임을 가져옴

                    val mDialogView = LayoutInflater.from(this@ManageBlockActivity).inflate(R.layout.un_block_dialog, null)
                    val mBuilder = AlertDialog.Builder(this@ManageBlockActivity).setView(mDialogView)

                    val alertDialog = mBuilder.show()

                    val userNameArea = alertDialog.findViewById<TextView>(R.id.userNameArea)
                    userNameArea!!.text = userName

                    val yesBtn = alertDialog.findViewById<Button>(R.id.yesBtn)
                    yesBtn?.setOnClickListener { // 예 버튼 클릭 시
                        Log.d(TAG, "yes Button Clicked")

                        val postListener = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                try {
                                    for (dataModel in dataSnapshot.children) {
                                        Log.d(TAG, dataModel.toString())
                                        val item = dataModel.getValue(BlockModel::class.java)

                                        if (item!!.blockUid == blockDataList[position].uid) {

                                            FBRef.userRef.child(blockDataList[position].uid).child("nickName").get().addOnSuccessListener {
                                                FBRef.blockRef.child(myUid).child(item.blockId).removeValue()
                                                Toast.makeText(applicationContext, it.value.toString() + "님이 차단 해제되었습니다.", Toast.LENGTH_SHORT).show()

                                                getBlockUsers()
                                                alertDialog.dismiss()
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error while removing block data", e)
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // onCancelled 메소드에서 에러 처리 로직 작성
                                Log.e(TAG, "loadPost:onCancelled", databaseError.toException())
                            }
                        }
                        FBRef.blockRef.child(myUid).addValueEventListener(postListener)
                    }

                    val noBtn = alertDialog.findViewById<Button>(R.id.noBtn)
                    noBtn?.setOnClickListener {  // 아니오 버튼 클릭 시
                        Log.d(TAG, "no Button Clicked")

                        alertDialog.dismiss() // 다이얼로그 창 닫기
                    }
                }
            }
        })

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener { // 뒤로 가기 버튼 클릭 시 이전 화면으로 돌아가기
            finish()
        }
    }

    private fun setData() {
        blockReVAdapter = BlockReVAdapter(blockDataList)
        blockRecyclerView = findViewById(R.id.blockRecyclerView)
        blockRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        blockRecyclerView.layoutManager = layoutManager
        blockRecyclerView.adapter = blockReVAdapter
    }

    private fun getBlockUsers() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    blockList.clear()
                    for(dataModel in dataSnapshot.children) {
                        val dataModel = dataModel.getValue(BlockModel::class.java)
                        blockList.add(dataModel!!.blockUid)
                    }

                    val postListener2 = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            try {

                                blockDataList.clear()

                                for (snapShot in dataSnapshot.children) {
                                    val item = snapShot.getValue(UserInfo::class.java)

                                    if(blockList.contains(item!!.uid))
                                        blockDataList.add(item!!)
                                }

                                Log.d("blockDataList", blockDataList.toString())
                                blockReVAdapter.notifyDataSetChanged() // 데이터 동기화

                            } catch (e: Exception) {
                                Log.d(TAG, "사용자 기록 삭제 완료")
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Getting Post failed, log a message
                            Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                        }
                    }
                    FBRef.userRef.addValueEventListener(postListener2)

                } catch(e : Exception) { }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.blockRef.child(myUid).addValueEventListener(postListener)
    }
}