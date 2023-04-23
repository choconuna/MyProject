package org.techtown.myproject.chat

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.note.MealReVAdapter
import org.techtown.myproject.utils.ChatConnection
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.MessageModel
import java.util.ArrayList

class ChatFragment : Fragment() {

    private val TAG = ChatFragment::class.java.simpleName

    private lateinit var myUid : String

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var chatRecyclerView : RecyclerView
    private val chatDataList = ArrayList<ChatConnection>() // 채팅 목록 리스트
    lateinit var chatRVAdapter : ChatReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v : View? = inflater.inflate(R.layout.fragment_chat, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        Log.d("showChatList", myUid)

        setData(v!!)

        getChatData()

        sharedPreferences = v!!.context.getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        Log.d("getToken", sharedPreferences.getString("token", "").toString())

        chatRVAdapter.setItemClickListener(object: ChatReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val intent = Intent(v!!.context, ChatInActivity::class.java) // 해당 채팅방으로 이동
                intent.putExtra("chatConnectionId", chatDataList[position]!!.chatConnectionId)

                val your1 = FBRef.chatConnectionRef.child(chatDataList[position]!!.chatConnectionId).child("userId1").get().addOnSuccessListener {
                    if(it.value.toString() != myUid) {
                        intent.putExtra("yourUid", it.value.toString())
                        startActivity(intent)
                    }
                }
                val your2 = FBRef.chatConnectionRef.child(chatDataList[position]!!.chatConnectionId).child("userId2").get().addOnSuccessListener {
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
        chatRVAdapter = ChatReVAdapter(chatDataList)
        chatRecyclerView = v!!.findViewById(R.id.chatRecyclerView)
        chatRecyclerView.setItemViewCacheSize(20)
        chatRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        chatRecyclerView.layoutManager = layoutManager
        chatRecyclerView.adapter = chatRVAdapter
    }

    private fun getChatData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    chatDataList.clear()
                    Log.d("showChatList", "실행")

                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(ChatConnection::class.java)

                        Log.d("showChatList", item!!.toString())

                        if(item!!.userId1 == myUid || item!!.userId2 == myUid) {
                            Log.d("showChatList", item!!.toString())
                            chatDataList.add(item!!)
                        }
                    }

                    chatRVAdapter.notifyDataSetChanged()

                } catch(e : Exception) {
                    Log.d("showChatList", e.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Geting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.chatConnectionRef.addValueEventListener(postListener)
    }
}