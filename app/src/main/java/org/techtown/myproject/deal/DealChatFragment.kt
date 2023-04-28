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
import org.techtown.myproject.utils.ChatConnection
import org.techtown.myproject.utils.DealChatConnection
import org.techtown.myproject.utils.DealModel
import org.techtown.myproject.utils.FBRef
import java.util.ArrayList

class DealChatFragment : Fragment() {

    private val TAG = DealChatFragment::class.java.simpleName

    private lateinit var myUid : String

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var dealChatRecyclerView : RecyclerView
    private val dealChatDataList = ArrayList<DealChatConnection>() // 거래 채팅 목록 리스트
    lateinit var dealChatRVAdapter : DealChatReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v : View? = inflater.inflate(R.layout.fragment_deal_chat, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        Log.d("showChatList", myUid)

        setData(v!!)
        getChatData()

        dealChatRVAdapter.setItemClickListener(object: DealChatReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val intent = Intent(v!!.context, DealChatInActivity::class.java) // 해당 채팅방으로 이동
                intent.putExtra("dealId", dealChatDataList[position]!!.dealId)
                intent.putExtra("chatConnectionId", dealChatDataList[position]!!.chatConnectionId)

                val your1 = FBRef.dealChatConnectionRef.child(dealChatDataList[position]!!.dealId).child(dealChatDataList[position]!!.chatConnectionId).child("userId1").get().addOnSuccessListener {
                    if(it.value.toString() != myUid) {
                        intent.putExtra("yourUid", it.value.toString())
                        startActivity(intent)
                    }
                }
                val your2 = FBRef.dealChatConnectionRef.child(dealChatDataList[position]!!.dealId).child(dealChatDataList[position]!!.chatConnectionId).child("userId2").get().addOnSuccessListener {
                    if(it.value.toString() != myUid) {
                        intent.putExtra("yourUid", it.value.toString())
                        startActivity(intent)
                    }
                }
            }
        })

        return v
    }

    private fun setData(v : View) {
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

                    dealChatDataList.clear()

                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DealModel::class.java)

                        val postListener = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                try {

                                    for(dataModel in dataSnapshot.children) {
                                        val item = dataModel.getValue(DealChatConnection::class.java)

                                        if(item!!.userId1 == myUid || item!!.userId2 == myUid) {
                                            Log.d("showChatList", item!!.toString())
                                            dealChatDataList.add(item!!)
                                            dealChatRVAdapter.notifyDataSetChanged()
                                        }
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
                        FBRef.dealChatConnectionRef.child(item!!.dealId).addValueEventListener(postListener)
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
    }
}