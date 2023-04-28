package org.techtown.myproject.deal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.utils.DealChatConnection
import org.techtown.myproject.utils.DealModel
import org.techtown.myproject.utils.FBRef


class ChoiceBuyerActivity : AppCompatActivity() {

    private val TAG = ChoiceBuyerActivity::class.java.simpleName

    private lateinit var myUid : String

    private lateinit var dealId : String

    private lateinit var itemNameArea : TextView

    private lateinit var dealChatRecyclerView : RecyclerView
    private val dealChatDataList = ArrayList<DealChatConnection>() // 거래 채팅 목록 리스트
    lateinit var dealChatRVAdapter : DealChatReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice_buyer)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        dealId = intent.getStringExtra("dealId").toString() // deal id

        setData()
        getChatData()

        dealChatRVAdapter.setItemClickListener(object: DealChatReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val intent = Intent(v!!.context, DealChatInActivity::class.java) // 해당 채팅방으로 이동

                val your1 = FBRef.dealChatConnectionRef.child(dealChatDataList[position]!!.dealId).child(dealChatDataList[position]!!.chatConnectionId).child("userId1").get().addOnSuccessListener {
                    if(it.value.toString() != myUid) {
                        FBRef.dealRef.child(dealChatDataList[position].dealId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val item = snapshot.getValue(DealModel::class.java)
                                    FBRef.dealRef.child(dealId).setValue(DealModel(dealId, myUid, item!!.location, item!!.category, item!!.price, item!!.title, item!!.content, item!!.imgCnt, item!!.method, "거래 완료", item!!.date, it.value.toString()))

                                    val buyerNickName = FBRef.userRef.child(it.value.toString()).child("nickName").get().addOnSuccessListener {
                                        Toast.makeText(applicationContext, it.value.toString() + "님이 구매자로 선택되었습니다!", Toast.LENGTH_SHORT).show()
                                    }

                                    finish()
                                } else {
                                    Toast.makeText(applicationContext, it.value.toString() + "채팅 내역이 존재하지 않습니다!", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(applicationContext, it.value.toString() + "채팅 내역이 존재하지 않습니다!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        })
                    }
                }
                val your2 = FBRef.dealChatConnectionRef.child(dealChatDataList[position]!!.dealId).child(dealChatDataList[position]!!.chatConnectionId).child("userId2").get().addOnSuccessListener {
                    if(it.value.toString() != myUid) {
                        FBRef.dealRef.child(dealChatDataList[position].dealId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val item = snapshot.getValue(DealModel::class.java)
                                    FBRef.dealRef.child(dealId).setValue(DealModel(dealId, myUid, item!!.location, item!!.category, item!!.price, item!!.title, item!!.content, item!!.imgCnt, item!!.method, "거래 완료", item!!.date, it.value.toString()))

                                    val buyerNickName = FBRef.userRef.child(it.value.toString()).child("nickName").get().addOnSuccessListener {
                                        Toast.makeText(applicationContext, it.value.toString() + "님이 구매자로 선택되었습니다!", Toast.LENGTH_SHORT).show()
                                    }

                                    finish()
                                } else {
                                    Toast.makeText(applicationContext, it.value.toString() + "채팅 내역이 존재하지 않습니다!", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(applicationContext, it.value.toString() + "채팅 내역이 존재하지 않습니다!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        })
                    }
                }
            }
        })

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setData() {

        itemNameArea = findViewById(R.id.itemNameArea)
        val item = FBRef.dealRef.child(dealId).child("title").get().addOnSuccessListener {
            itemNameArea.text = "\'" + it.value.toString() + "\'"
        }

        dealChatRVAdapter = DealChatReVAdapter(dealChatDataList)
        dealChatRecyclerView = findViewById(R.id.dealChatRecyclerView)
        dealChatRecyclerView.setItemViewCacheSize(20)
        dealChatRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        dealChatRecyclerView.layoutManager = layoutManager
        dealChatRecyclerView.adapter = dealChatRVAdapter
    }

    private fun getChatData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    dealChatDataList.clear()

                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DealChatConnection::class.java)


                        if(item!!.userId1 == myUid || item!!.userId2 == myUid) {
                            Log.d("showChatList", item!!.toString())
                            dealChatDataList.add(item!!)
                        }
                    }

                    dealChatRVAdapter.notifyDataSetChanged()

                } catch(e : Exception) {
                    Log.d("showChatList", e.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Geting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dealChatConnectionRef.child(dealId).addValueEventListener(postListener)
    }
}