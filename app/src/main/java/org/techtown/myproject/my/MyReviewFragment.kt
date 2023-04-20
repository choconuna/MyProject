package org.techtown.myproject.my

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import org.techtown.myproject.R
import org.techtown.myproject.comment.CommentModel
import org.techtown.myproject.community.CommunityModel
import org.techtown.myproject.community.SpecificCommunityInActivity
import org.techtown.myproject.note.Decorator
import org.techtown.myproject.note.NoteFragment
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.ReCommentModel

class MyReviewFragment : Fragment() {
    private val TAG = MyReviewFragment::class.java.simpleName

    lateinit var uid : String // 사용자 uid

    lateinit var group : RadioGroup
    private var category : String = "내가 쓴 글"

    lateinit var myWritingReVAdapter: MyWritingReVAdapter
    private var myWritingDataList = ArrayList<CommunityModel>() // 각 게시물 데이터를 넣는 리스트
    private var  myWritingKeyList = mutableListOf<String>() // 각 게시물의 키값을 넣는 리스트
    lateinit var myWritingRecyclerView: RecyclerView
    lateinit var layoutManager : RecyclerView.LayoutManager

    lateinit var myCommentReVAdapter: MyCommentReVAdapter
    private var myCommentDataList = ArrayList<CommentModel>() // 댓글 데이터를 넣는 리스트
    lateinit var myCommentRecyclerView: RecyclerView
    lateinit var mLayoutManager : RecyclerView.LayoutManager

    lateinit var myReCommentReVAdapter: MyReCommentReVAdapter
    private var myReCommentDataList = ArrayList<ReCommentModel>() // 각 대댓글 데이터를 넣는 리스트
    lateinit var myReCommentRecyclerView: RecyclerView
    lateinit var rLayoutManager : RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v : View? = inflater.inflate(R.layout.fragment_my_question, container, false)

        uid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        setData(v!!)

        setView(v!!)

        group.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.writing -> {
                    category = "내가 쓴 글"
                    group.check(v.findViewById<RadioButton>(R.id.writing).id)

                    getMyWritingData()

                    myWritingRecyclerView.visibility = View.VISIBLE
                    myCommentRecyclerView.visibility = View.GONE
                    myReCommentRecyclerView.visibility = View.GONE
                }
                R.id.comment -> {
                    category = "나의 댓글"
                    group.check(v.findViewById<RadioButton>(R.id.comment).id)

                    getMyCommentData()

                    myWritingRecyclerView.visibility = View.GONE
                    myCommentRecyclerView.visibility = View.VISIBLE
                    myReCommentRecyclerView.visibility = View.GONE
                }
                R.id.reComment -> {
                    category = "나의 대댓글"
                    group.check(v.findViewById<RadioButton>(R.id.reComment).id)

                    getMyReCommentData()

                    myWritingRecyclerView.visibility = View.GONE
                    myCommentRecyclerView.visibility = View.GONE
                    myReCommentRecyclerView.visibility = View.VISIBLE
                }
                else -> {
                    category = "내가 쓴 글"
                    group.check(v.findViewById<RadioButton>(R.id.writing).id)

                    getMyWritingData()

                    myWritingRecyclerView.visibility = View.VISIBLE
                    myCommentRecyclerView.visibility = View.GONE
                    myReCommentRecyclerView.visibility = View.GONE
                }
            }
        }

        when(category) {
            "내가 쓴 글" -> {
                group.check(v.findViewById<RadioButton>(R.id.writing).id)

                getMyWritingData()

                myWritingRecyclerView.visibility = View.VISIBLE
                myCommentRecyclerView.visibility = View.GONE
                myReCommentRecyclerView.visibility = View.GONE

            }
            "나의 댓글" -> {
                group.check(v.findViewById<RadioButton>(R.id.comment).id)

                getMyCommentData()

                myWritingRecyclerView.visibility = View.GONE
                myCommentRecyclerView.visibility = View.VISIBLE
                myReCommentRecyclerView.visibility = View.GONE

            }
            "나의 대댓글" -> {
                group.check(v.findViewById<RadioButton>(R.id.reComment).id)

                getMyReCommentData()

                myWritingRecyclerView.visibility = View.GONE
                myCommentRecyclerView.visibility = View.GONE
                myReCommentRecyclerView.visibility = View.VISIBLE

            }
            else -> {
                category = "내가 쓴 글"
                group.check(v.findViewById<RadioButton>(R.id.writing).id)

                getMyWritingData()

                myWritingRecyclerView.visibility = View.VISIBLE
                myCommentRecyclerView.visibility = View.GONE
                myReCommentRecyclerView.visibility = View.GONE
            }
        }

        myWritingReVAdapter.setItemClickListener(object: MyWritingReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val intent = Intent(context, SpecificCommunityInActivity::class.java)
                intent.putExtra("category", "후기") // 게시물의 카테고리 넘기기
                intent.putExtra("key", myWritingKeyList[position]) // 게시물의 key 값 넘기기
                startActivity(intent)
            }
        })

        myCommentReVAdapter.setItemClickListener(object: MyCommentReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val intent = Intent(context, SpecificCommunityInActivity::class.java)
                intent.putExtra("category", "후기") // 게시물의 카테고리 넘기기
                intent.putExtra("key", myCommentDataList[position].communityId) // 게시물의 key 값 넘기기
                startActivity(intent)
            }
        })

        myReCommentReVAdapter.setItemClickListener(object: MyReCommentReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val intent = Intent(context, SpecificCommunityInActivity::class.java)
                intent.putExtra("category", "후기") // 게시물의 카테고리 넘기기
                intent.putExtra("key", myReCommentDataList[position].communityId) // 게시물의 key 값 넘기기
                startActivity(intent)
            }
        })

        return v
    }

    private fun setData(v : View) {

        myWritingReVAdapter = MyWritingReVAdapter(myWritingDataList)
        myWritingRecyclerView = v!!.findViewById(R.id.myWritingRecyclerView)
        myWritingRecyclerView.setItemViewCacheSize(20)
        myWritingRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        myWritingRecyclerView.layoutManager = layoutManager
        myWritingRecyclerView.adapter = myWritingReVAdapter

        myCommentReVAdapter = MyCommentReVAdapter(myCommentDataList)
        myCommentRecyclerView = v!!.findViewById(R.id.myCommentRecyclerView)
        myCommentRecyclerView.setItemViewCacheSize(20)
        myCommentRecyclerView.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        myCommentRecyclerView.layoutManager = mLayoutManager
        myCommentRecyclerView.adapter = myCommentReVAdapter

        myReCommentReVAdapter = MyReCommentReVAdapter(myReCommentDataList)
        myReCommentRecyclerView = v!!.findViewById(R.id.myReCommentRecyclerView)
        myReCommentRecyclerView.setItemViewCacheSize(20)
        myReCommentRecyclerView.setHasFixedSize(true)
        rLayoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        myReCommentRecyclerView.layoutManager = rLayoutManager
        myReCommentRecyclerView.adapter = myReCommentReVAdapter
    }

    private fun setView(v : View) {
        group = v!!.findViewById(R.id.categoryGroup)
        group.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.writing -> {
                    category = "내가 쓴 글"
                    group.check(v.findViewById<RadioButton>(R.id.writing).id)

                    getMyWritingData()

                    myWritingRecyclerView.visibility = View.VISIBLE
                    myCommentRecyclerView.visibility = View.GONE
                    myReCommentRecyclerView.visibility = View.GONE
                }
                R.id.comment -> {
                    category = "나의 댓글"
                    group.check(v.findViewById<RadioButton>(R.id.comment).id)

                    getMyCommentData()

                    myWritingRecyclerView.visibility = View.GONE
                    myCommentRecyclerView.visibility = View.VISIBLE
                    myReCommentRecyclerView.visibility = View.GONE
                }
                R.id.reComment -> {
                    category = "나의 대댓글"
                    group.check(v.findViewById<RadioButton>(R.id.reComment).id)

                    getMyReCommentData()

                    myWritingRecyclerView.visibility = View.GONE
                    myCommentRecyclerView.visibility = View.GONE
                    myReCommentRecyclerView.visibility = View.VISIBLE
                }
                else -> {
                    category = "내가 쓴 글"
                    group.check(v.findViewById<RadioButton>(R.id.writing).id)

                    getMyWritingData()

                    myWritingRecyclerView.visibility = View.VISIBLE
                    myCommentRecyclerView.visibility = View.GONE
                    myReCommentRecyclerView.visibility = View.GONE
                }
            }
        }

        when(category) {
            "내가 쓴 글" -> {
                group.check(v.findViewById<RadioButton>(R.id.writing).id)

                getMyWritingData()

                myWritingRecyclerView.visibility = View.VISIBLE
                myCommentRecyclerView.visibility = View.GONE
                myReCommentRecyclerView.visibility = View.GONE

            }
            "나의 댓글" -> {
                group.check(v.findViewById<RadioButton>(R.id.comment).id)

                getMyCommentData()

                myWritingRecyclerView.visibility = View.GONE
                myCommentRecyclerView.visibility = View.VISIBLE
                myReCommentRecyclerView.visibility = View.GONE

            }
            "나의 대댓글" -> {
                group.check(v.findViewById<RadioButton>(R.id.reComment).id)

                getMyReCommentData()

                myWritingRecyclerView.visibility = View.GONE
                myCommentRecyclerView.visibility = View.GONE
                myReCommentRecyclerView.visibility = View.VISIBLE

            }
            else -> {
                category = "내가 쓴 글"
                group.check(v.findViewById<RadioButton>(R.id.writing).id)

                getMyWritingData()

                myWritingRecyclerView.visibility = View.VISIBLE
                myCommentRecyclerView.visibility = View.GONE
                myReCommentRecyclerView.visibility = View.GONE
            }
        }
    }

    private fun getMyWritingData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {

                    myWritingDataList.clear()
                    myWritingKeyList.clear()

                    for (dataModel in dataSnapshot.children) {
                        Log.d(TAG, dataModel.toString())
                        val item = dataModel.getValue(CommunityModel::class.java)
                        if (item!!.uid == uid) {
                            myWritingDataList.add(item!!)
                            myWritingKeyList.add(item!!.communityId)
                        }
                    }

                    myWritingKeyList.reverse() // 키 값을 최신순으로 정렬
                    myWritingDataList.reverse() // 게시물을 최신순으로 정렬
                    myWritingReVAdapter.notifyDataSetChanged() // 동기화

                } catch (e: Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.communityRef.child("후기").addValueEventListener(postListener)
    }


    private fun getMyCommentData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {

                    myCommentDataList.clear()

                    for(dataModel in dataSnapshot.children) {
                        Log.d(TAG, dataModel.toString())
                        val item = dataModel.getValue(CommunityModel::class.java)

                        val postListener = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                try {

                                    for (dataModel in dataSnapshot.children) {
                                        val item = dataModel.getValue(CommentModel::class.java)

                                        if(item!!.uid == uid)
                                            myCommentDataList.add(item!!)
                                    }

                                    myCommentReVAdapter.notifyDataSetChanged()
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
                } catch(e : Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.communityRef.child("후기").addValueEventListener(postListener)
    }

    private fun getMyReCommentData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {

                    myReCommentDataList.clear()

                    for(dataModel in dataSnapshot.children) {
                        Log.d(TAG, dataModel.toString())
                        val item = dataModel.getValue(CommunityModel::class.java)

                        val postListener = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                try {

                                    for (dataModel in dataSnapshot.children) {
                                        val item = dataModel.getValue(CommentModel::class.java)

                                        val postListener = object : ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                try {

                                                    for(dataModel in dataSnapshot.children) {
                                                        val item = dataModel.getValue(
                                                            ReCommentModel::class.java)

                                                        if(item!!.uid == uid)
                                                            myReCommentDataList.add(item!!)
                                                    }

                                                    myReCommentReVAdapter.notifyDataSetChanged()
                                                } catch(e : Exception) {
                                                }
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {
                                            }
                                        }
                                        FBRef.reCommentRef.child(item!!.communityId).child(item!!.commentId).addValueEventListener(postListener)
                                    }

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

                } catch(e : Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.communityRef.child("후기").addValueEventListener(postListener)
    }
}