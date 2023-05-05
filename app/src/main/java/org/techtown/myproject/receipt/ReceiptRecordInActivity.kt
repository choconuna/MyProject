package org.techtown.myproject.receipt

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.community.CommunityImageAdapter
import org.techtown.myproject.community.CommunityImageDetailActivity
import org.techtown.myproject.community.GalleryAdapter
import org.techtown.myproject.note.ImageDetailActivity
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.ReceiptModel
import java.io.File
import java.text.DecimalFormat
import java.time.LocalDate
import java.util.ArrayList

class ReceiptRecordInActivity : AppCompatActivity() {

    private val TAG = ReceiptRecordInActivity::class.java.simpleName
    private lateinit var userId : String
    private lateinit var key : String

    private lateinit var selectedDateArea : TextView
    private lateinit var originSelectedDate : String

    private lateinit var priceArea : TextView

    private lateinit var categoryArea : TextView

    private lateinit var payMethodArea : TextView
    private lateinit var payMethod : String

    private lateinit var originDivide : String // 기존 카드 결제 방식 (일시불 or 할부)

    private lateinit var payMonthShow : ConstraintLayout
    private lateinit var payMonthStateNum : TextView
    private lateinit var payMonthStatePrice : TextView

    lateinit var placeArea : TextView
    private lateinit var contentArea : TextView

    private val decimalFormat = DecimalFormat("#,###")
    private var result: String = ""

    lateinit var imageListView : RecyclerView
    private val imageDataList = ArrayList<String>()
    lateinit var communityImageVAdapter : CommunityImageAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager

    private var divideReceiptGroup = ArrayList<String>()
    private var divideReceiptDetailGroup = ArrayList<ReceiptModel>()

    private var count = 0 // 첨부한 사진 수

    private lateinit var back : ImageView
    private lateinit var editBtn : ImageView
    private lateinit var deleteBtn : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt_record_in)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        key = intent.getStringExtra("key").toString() // 해당 가계부 id

        setData()

        getData()

        communityImageVAdapter.setItemClickListener(object: CommunityImageAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val intent = Intent(applicationContext, ImageDetailActivity::class.java)
                Log.d("imageDataList", imageDataList[position])
                intent.putExtra("image", imageDataList[position])
                startActivity(intent)
            }
        })

        back.setOnClickListener {
            finish()
        }

        editBtn.setOnClickListener {
            val intent = Intent(applicationContext, ReceiptRecordEditActivity::class.java)
            intent.putExtra("key", key)
            startActivity(intent)
        }

        deleteBtn.setOnClickListener {
            if(payMethod == "카드" && originDivide == "할부") { // 결제 방법이 카드이고 할부인 경우
                Log.d("divideReceiptGroup", divideReceiptGroup.toString())
                Log.d("divideReceiptGroup", divideReceiptGroup.toString())

                for(i in 0 until divideReceiptGroup.size) { // 할부 내역에 해당하는 모든 가계부 데이터를 삭제

                    var receiptKey = divideReceiptGroup[i]

                    if (count >= 1) { // 기존의 이미지가 존재했다면 전부 삭제함
                        for (index in 0 until count) {
                            Firebase.storage.reference.child("receiptImage/$userId/$receiptKey/$receiptKey$index.png")
                                .delete().addOnSuccessListener { // 사진 삭제
                                }.addOnFailureListener {
                                }
                        }
                    }

                    FBRef.receiptRef.child(userId).child(divideReceiptGroup[i]).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제
                }

            } else {
                for (index in 0 until count) {
                    Firebase.storage.reference.child("receiptImage/$userId/$key/$key$index.png")
                        .delete().addOnSuccessListener { // 사진 삭제
                        }.addOnFailureListener {
                        }
                }

                FBRef.receiptRef.child(userId).child(key).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제
            }
            Toast.makeText(this, "지출 내역이 삭제되었습니다!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setData() {
        selectedDateArea = findViewById(R.id.selectedDateArea)
        priceArea = findViewById(R.id.priceArea)

        categoryArea = findViewById(R.id.categoryArea)

        priceArea = findViewById(R.id.priceArea)


        payMethodArea = findViewById(R.id.payMethodArea)

        payMonthShow = findViewById(R.id.payMonthShow)
        payMonthStateNum = findViewById(R.id.payMonthStateNum)
        payMonthStatePrice = findViewById(R.id.payMonthStatePrice)

        placeArea = findViewById(R.id.placeArea)

        contentArea = findViewById(R.id.contentArea)

        communityImageVAdapter = CommunityImageAdapter(imageDataList)
        imageListView = findViewById(R.id.imageRecyclerView)
        imageListView.setItemViewCacheSize(20)
        imageListView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        imageListView.layoutManager = layoutManager
        imageListView.adapter = communityImageVAdapter

        back = findViewById(R.id.back)
        editBtn = findViewById(R.id.editBtn)
        deleteBtn = findViewById(R.id.deleteBtn)
    }

    private fun getData() { // 기존에 저장된 데이터를 가져옴
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val post = dataSnapshot.getValue(ReceiptModel::class.java)
                    originSelectedDate = post!!.date
                    val dateSp = originSelectedDate.split(".")

                    selectedDateArea.text = dateSp[0] + "." + dateSp[1] + "." + dateSp[2] + " (" + dateSp[3] + ")"
                    originSelectedDate = post!!.date

                    val decimalFormat = DecimalFormat("#,###")

                    placeArea.text = post!!.place
                    contentArea.text = post!!.content

                    categoryArea.text = post!!.category

                    payMethodArea.text = post!!.payMethod
                    payMethod = post!!.payMethod

                    originDivide = post!!.payMonthRole

                    if(post!!.payMethod == "카드") {
                        if (post!!.payMonthRole == "일시불") {
                            priceArea.text = decimalFormat.format(post!!.price.replace(",", "").toDouble())
                        }
                        else if(post!!.payMonthRole == "할부") {

                            payMonthShow.visibility = View.VISIBLE
                            payMonthStateNum.text = "(" + post!!.nowPayMonth + "개월/" + post!!.payMonth + "개월)"

                            priceArea.text = decimalFormat.format(post!!.price.replace(",", "").toDouble()) + "원"

                            getDivideReceiptData(post!!.startPayRoleId) // 할부에 해당하는 receipt 데이터 가져오기
                            Log.d("divideReceiptGroup", divideReceiptGroup.toString())
                            getDivideDetailReceiptData(post!!.startPayRoleId) // 할부에 해당하는 receipt 데이터 가져오기
                        }
                    } else if(post!!.payMethod == "현금") {
                        priceArea.text = decimalFormat.format(post!!.price.replace(",", "").toDouble()) + "원"
                    }

                    count = post!!.imgCnt.toInt()

                    if(count >= 1) {
                        for(index in 0 until count) {
                            imageDataList.add("receiptImage/$userId/$key/$key$index.png")
                        }
                        Log.d("imageDataList", imageDataList.toString())
                    }
                    communityImageVAdapter.notifyDataSetChanged()

                } catch (e: Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.receiptRef.child(userId).child(key).addValueEventListener(postListener)
    }

    private fun getDivideReceiptData(startPayRoleId : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {

                    var totalPrice = 0
                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(ReceiptModel::class.java)
                        if(item!!.startPayRoleId == startPayRoleId) {
                            Log.d("divideReceipt", item!!.toString())
                            divideReceiptGroup.add(item!!.receiptId)
                            totalPrice += item!!.price.toInt()
                        }
                    }

                    payMonthStatePrice.text = "총액=" + decimalFormat.format(totalPrice.toString().replace(",", "").toDouble()) + "원"

                } catch (e: Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.receiptRef.child(userId).addValueEventListener(postListener)
    }

    private fun getDivideDetailReceiptData(startPayRoleId : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {

                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(ReceiptModel::class.java)
                        if(item!!.startPayRoleId == startPayRoleId) {
                            Log.d("divideReceipt", item!!.toString())
                            divideReceiptDetailGroup.add(item!!)
                        }
                    }
                } catch (e: Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.receiptRef.child(userId).addValueEventListener(postListener)
    }
}