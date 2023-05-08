package org.techtown.myproject.deal

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.chat.ChatInActivity
import org.techtown.myproject.chat.ChatReVAdapter
import org.techtown.myproject.utils.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.typeOf

class DealChatFragment : Fragment() {

    private val TAG = DealChatFragment::class.java.simpleName

    private lateinit var myUid: String

    private val blockList = mutableListOf<String>() // 차단된 uid 값을 넣는 리스트

    private lateinit var sharedPreferences: SharedPreferences

    private var unsortedDealChatDataList: MutableList<DealChatConnection> = mutableListOf()
    private lateinit var dealChatRecyclerView: RecyclerView
    private val dealChatDataList = ArrayList<DealChatConnection>() // 거래 채팅 목록 리스트
    lateinit var dealChatRVAdapter: DealChatReVAdapter
    lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View? = inflater.inflate(R.layout.fragment_deal_chat, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        Log.d("showChatList", myUid)

        setData(v!!)
        dealChatDataList.clear()
        getChatData()

        dealChatRVAdapter.setItemClickListener(object : DealChatReVAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val intent = Intent(v!!.context, DealChatInActivity::class.java) // 해당 채팅방으로 이동
                intent.putExtra("dealId", dealChatDataList[position]!!.dealId)
                intent.putExtra("chatConnectionId", dealChatDataList[position]!!.chatConnectionId)

                val your1 = FBRef.dealChatConnectionRef.child(dealChatDataList[position]!!.dealId)
                    .child(dealChatDataList[position]!!.chatConnectionId).child("userId1").get()
                    .addOnSuccessListener {
                        if (it.value.toString() != myUid) {
                            intent.putExtra("yourUid", it.value.toString())
                            startActivity(intent)
                        }
                    }
                val your2 = FBRef.dealChatConnectionRef.child(dealChatDataList[position]!!.dealId)
                    .child(dealChatDataList[position]!!.chatConnectionId).child("userId2").get()
                    .addOnSuccessListener {
                        if (it.value.toString() != myUid) {
                            intent.putExtra("yourUid", it.value.toString())
                            startActivity(intent)
                        }
                    }
            }
        })

        return v
    }

    private fun setData(v: View) {
        dealChatRVAdapter = DealChatReVAdapter(dealChatDataList)
        dealChatRecyclerView = v!!.findViewById(R.id.dealChatRecyclerView)
        dealChatRecyclerView.setItemViewCacheSize(20)
        dealChatRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        dealChatRecyclerView.layoutManager = layoutManager
        dealChatRecyclerView.adapter = dealChatRVAdapter
    }

    private fun getChatData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    blockList.clear()

                    for(dataModel in dataSnapshot.children) {
                        Log.d(TAG, dataModel.toString())
                        // dataModel.key
                        val item = dataModel.getValue(BlockModel::class.java)
                        blockList.add(item!!.blockUid)
                    }

                    val postListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            try {
                                dealChatDataList.clear()

                                for(dataModel in dataSnapshot.children) {
                                    val item = dataModel.getValue(DealModel::class.java)

                                    val postListener2 = object : ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            try {
                                                val unsortedDealChatDataList = mutableListOf<DealChatConnection>()

                                                for(dataModel in dataSnapshot.children) {
                                                    val item = dataModel.getValue(DealChatConnection::class.java)
                                                    var isBlocked = false

                                                    if (item!!.userId1 != myUid && item!!.userId2 == myUid) {
                                                        if (!blockList.contains(item!!.userId1)) {
                                                            val index = dealChatDataList.indexOfFirst { it.dealId == item.dealId }
                                                            if (index == -1) {
                                                                Log.d("showChatList", item!!.toString())
                                                                unsortedDealChatDataList.add(item!!)
                                                            }
                                                        }
                                                    } else if (item!!.userId2 != myUid && item!!.userId1 == myUid) {
                                                        if (!blockList.contains(item!!.userId2)) {
                                                            val index = dealChatDataList.indexOfFirst { it.dealId == item.dealId }
                                                            if (index == -1) {
                                                                Log.d("showChatList", item!!.toString())
                                                                unsortedDealChatDataList.add(item!!)
                                                            }
                                                        }
                                                    }
                                                }

                                                val sortedDealChatDataList = unsortedDealChatDataList.sortedByDescending { it.lastTime.toLong() }

                                                // 마지막으로 추가된 채팅 시간 저장
                                                val lastAddedChatTime = if (dealChatDataList.isEmpty()) 0L else dealChatDataList[0].lastTime.toLong()

                                                for (chat in sortedDealChatDataList) { // 중복되지 않는 새로운 채팅만 추가
                                                    if (chat.lastTime.toLong() > lastAddedChatTime) {
                                                        dealChatDataList.add(0, chat)
                                                    } else {
                                                        break
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
                                    FBRef.dealChatConnectionRef.child(item!!.dealId).addValueEventListener(postListener2)
                                }

                            } catch(e : Exception) {
                                Log.d("showChatList", e.toString())
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Geting Post failed, log a message
                            Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                        }
                    }
                    FBRef.dealRef.addValueEventListener(postListener)

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