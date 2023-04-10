package org.techtown.myproject.receipt

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.ReceiptModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class ReceiptRecordEditActivity : AppCompatActivity() {

    private val TAG = ReceiptRecordEditActivity::class.java.simpleName
    private lateinit var userId : String
    private lateinit var key : String

    private lateinit var selectedDateArea : EditText
    private var date = LocalDate.now()
    private lateinit var selectedDate : String

    private lateinit var priceArea : EditText
    private lateinit var price : String
    private lateinit var categorySpinner : Spinner
    private lateinit var category : String
    private lateinit var contentArea : EditText
    private lateinit var content : String

    private val decimalFormat = DecimalFormat("#,###")
    private var result: String = ""

    private lateinit var editBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt_record_edit)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        key = intent.getStringExtra("key").toString() // 해당 가계부 id

        setData()
        getData()

        selectedDateArea.setOnClickListener {
            val datepickercalendar = Calendar.getInstance()
            val year = datepickercalendar.get(Calendar.YEAR)
            val month = datepickercalendar.get(Calendar.MONTH)
            val day = datepickercalendar.get(Calendar.DAY_OF_MONTH)

            if(!this.isFinishing) {
                val dpd = DatePickerDialog(
                    this@ReceiptRecordEditActivity, R.style.MySpinnerDatePickerStyle,
                    { _, year, monthOfYear, dayOfMonth ->
//                  월이 0부터 시작하여 1을 더해주어야함
                        val month = monthOfYear + 1
//                   선택한 날짜의 요일을 구하기 위한 calendar
                        val calendar = Calendar.getInstance()
//                    선택한 날짜 세팅
                        calendar.set(year, monthOfYear, dayOfMonth)
                        val date = calendar.time
                        val simpledateformat = SimpleDateFormat("E", Locale.getDefault())
                        val dayName: String = simpledateformat.format(date)

                        if(month.toString().length == 1 && dayOfMonth.toString().length == 1) {
                            selectedDate = "$year.0$month.0$dayOfMonth.$dayName"
                            selectedDateArea.setText("$year.0$month.0$dayOfMonth ($dayName)")
                        } else if(dayOfMonth.toString().length == 1) {
                            selectedDate = "$year.$month.0$dayOfMonth.$dayName"
                            selectedDateArea.setText("$year.$month.0$dayOfMonth ($dayName)")
                        } else if(month.toString().length == 1) {
                            selectedDate = "$year.0$month.$dayOfMonth.$dayName"
                            selectedDateArea.setText("$year.0$month.$dayOfMonth ($dayName)")
                        } else {
                            selectedDate = "$year.$month.$dayOfMonth.$dayName"
                            selectedDateArea.setText("$year.$month.$dayOfMonth ($dayName)")
                        }

                        Log.d("selectedDate", selectedDate)
                    }, year, month, day
                )
                dpd.show()
            }
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                category = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!TextUtils.isEmpty(charSequence.toString()) && charSequence.toString() != result){
                    result = decimalFormat.format(charSequence.toString().replace(",","").toDouble())
                    priceArea.setText(result);
                    priceArea.setSelection(result.length);
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        }

        priceArea.addTextChangedListener(watcher)

        editBtn.setOnClickListener {
            val price = priceArea.text.toString().replace(",", "")
            val content = contentArea.text.toString().trim()

            when {
                price == "" -> {
                    Toast.makeText(this, "금액을 입력하세요!", Toast.LENGTH_SHORT).show()
                    priceArea.setSelection(0)
                }
                content == "" -> {
                    Toast.makeText(this, "내용을 입력하세요!", Toast.LENGTH_SHORT).show()
                    contentArea.setSelection(0)
                }
                else -> {
                    FBRef.receiptRef.child(userId).child(key).setValue(ReceiptModel(userId, key, selectedDate, category, price, content))
                    Toast.makeText(this, "지출 내역이 수정되었습니다!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        val deleteBtn = findViewById<ImageView>(R.id.deleteBtn)
        deleteBtn.setOnClickListener {
            FBRef.receiptRef.child(userId).child(key).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제
            Toast.makeText(this, "지출 내역이 삭제되었습니다!", Toast.LENGTH_SHORT).show()
            finish()
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setData() {
        selectedDateArea = findViewById(R.id.selectedDateArea)
        priceArea = findViewById(R.id.priceArea)
        categorySpinner = findViewById(R.id.categorySpinner)
        contentArea = findViewById(R.id.contentArea)
        editBtn = findViewById(R.id.editBtn)
    }

    private fun getData() { // 기존에 저장된 데이터를 가져옴
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val post = dataSnapshot.getValue(ReceiptModel::class.java)
                    selectedDate = post!!.date
                    val dateSp = selectedDate.split(".")

                    selectedDateArea.setText(dateSp[0] + "." + dateSp[1] + "." + dateSp[2] + " (" + dateSp[3] + ")")

                    val decimalFormat = DecimalFormat("#,###")
                    priceArea.setText(
                        decimalFormat.format(
                            post!!.price.replace(",", "").toDouble()
                        )
                    )
                    contentArea.setText(post!!.content)

                    category = post!!.category

                    for (i in 0 until categorySpinner.count) {
                        if (categorySpinner.getItemAtPosition(i).toString() == category) {
                            categorySpinner.setSelection(i)
                            break
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
        FBRef.receiptRef.child(userId).child(key).addValueEventListener(postListener)
    }
}