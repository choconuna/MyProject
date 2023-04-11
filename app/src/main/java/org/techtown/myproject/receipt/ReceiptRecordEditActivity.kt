package org.techtown.myproject.receipt

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
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
    private lateinit var originSelectedDate : String
    private var originPrice = 0

    private lateinit var priceArea : EditText
    private lateinit var categorySpinner : Spinner
    private lateinit var category : String
    private lateinit var payMethodArea : LinearLayout
    private lateinit var payMethodSpinner : Spinner
    private lateinit var payMethod : String
    private lateinit var originPayMethod : String // 기존 결제 방식
    private lateinit var divideArea : LinearLayout
    private lateinit var divide : String
    private lateinit var originDivide : String // 기존 카드 결제 방식 (일시불 or 할부)
    private lateinit var divideRadioGroup: RadioGroup
    lateinit var divideMonthArea : LinearLayout
    lateinit var divideMonth : EditText
    private lateinit var payMonthShow : ConstraintLayout
    private lateinit var payMonthStateNum : TextView
    private lateinit var payMonthStatePrice : TextView
    private lateinit var startPayRoleId : String // 할부가 시작되는 개월의 id
    private lateinit var originDivideMonth : String // 기존 할부 개월수
    private lateinit var nowPayMonth : String // 현재 할부 개월수 (3개월 할부의 2개월째라면 2)
    lateinit var placeArea : EditText
    private lateinit var contentArea : EditText

    private var divideReceiptGroup = ArrayList<String>()

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

        divide = "일시불"
        divideRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.no -> divide = "일시불"
                R.id.yes -> divide = "할부"
            }

            when(divide) {
                "일시불" -> divideMonthArea.visibility = GONE
                "할부" -> divideMonthArea.visibility = VISIBLE
            }
        }

        payMethod = "현금"
        payMethodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                payMethod = parent.getItemAtPosition(position).toString()

                when(payMethod) {
                    "현금" -> {
                        divideMonthArea.visibility = GONE
                        divideArea.visibility = GONE
                    } "카드" -> {
//                    divideMonthArea.visibility = VISIBLE
                    divideArea.visibility = VISIBLE
                }
                }
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
            val place = placeArea.text.toString().trim()
            val content = contentArea.text.toString().trim()
            val selectedDateSp = selectedDate.split(".")
            val originSelectedDateSp = originSelectedDate.split(".")
            val formatt = "yyyy.MM.dd.E"

            when {
                price == "" -> {
                    Toast.makeText(this, "금액을 입력하세요!", Toast.LENGTH_SHORT).show()
                    priceArea.setSelection(0)
                }
                payMethod == "카드" && divide == "할부" && divideMonth.text.toString() == "" -> { // 카드 결제 할부인데 개월수를 적지 않았을 경우
                    Toast.makeText(this, "내용을 입력하세요!", Toast.LENGTH_SHORT).show()
                    divideMonth.setSelection(0)
                }
                place == "" -> {
                    Toast.makeText(this, "장소를 입력하세요!", Toast.LENGTH_SHORT).show()
                    placeArea.setSelection(0)
                }
                content == "" -> {
                    Toast.makeText(this, "내용을 입력하세요!", Toast.LENGTH_SHORT).show()
                    contentArea.setSelection(0)
                }
                originPayMethod == payMethod -> { // 결제 방법이 원래 방법과 같았을 경우
                    if(originPayMethod == "현금") { // 결제 방법이 현금일 경우
                        FBRef.receiptRef.child(userId).child(key).setValue(ReceiptModel(userId, key, "", selectedDate, category, price, payMethod, divide, "", "", place, content))
                        Toast.makeText(this, "지출 내역이 수정되었습니다!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else if(originPayMethod == "카드") { // 결제 방법이 카드일 경우
                        if(divide == originDivide && originDivide == "일시불") { // 기존, 현재 카드 결제 방식이 일시불일 경우
                            FBRef.receiptRef.child(userId).child(key).setValue(ReceiptModel(userId, key,"", selectedDate, category, price, payMethod, divide,"","" , place, content))
                            Toast.makeText(this, "지출 내역이 수정되었습니다!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else if(divide != originDivide && divide == "할부") { // 카드 결제 방식을 일시불에서 할부로 변경한 경우
                            FBRef.receiptRef.child(userId).child(key).setValue(ReceiptModel(userId, key, key, selectedDate, category, (price.toInt() / divideMonth.text.toString().toInt()).toString(), payMethod, divide, divideMonth.text.toString(), "1", place, content))

                            for(i in 2 until divideMonth.text.toString().toInt() + 1) {
                                var childKey = FBRef.receiptRef.child(userId).push().key.toString() // 키 값을 먼저 받아옴
                                var date = LocalDate.of(selectedDateSp[0].toInt(), selectedDateSp[1].toInt(), selectedDateSp[2].toInt())
                                date = date.plusMonths((i-1).toLong())
                                val sfd = DateTimeFormatter.ofPattern(formatt)

                                FBRef.receiptRef.child(userId).child(childKey).setValue(ReceiptModel(userId, childKey, key, date.format(sfd), category, (price.toInt() / divideMonth.text.toString().toInt()).toString(), payMethod, divide, divideMonth.text.toString(), i.toString(), place, content))
                            }
                        } else if(divide == originDivide && originDivide == "할부") { // 기존, 현재 카드 결제 방식이 할부일 경우
                            if(key == startPayRoleId) { // 현재 시점이 할부를 시작하는 시점일 경우 (ex. 3개월 할부 중 1개월째)
                                var date = LocalDate.of(selectedDateSp[0].toInt(), selectedDateSp[1].toInt(), selectedDateSp[2].toInt())
                                val sfd = DateTimeFormatter.ofPattern(formatt)
                                FBRef.receiptRef.child(userId).child(key).setValue((ReceiptModel(userId, key, key, date.format(sfd), category, price, payMethod, divide, divideMonth.text.toString(), (1).toString(), place, content)))
                            } else if(key != startPayRoleId) { // 현재 시점이 할부가 시작된 이후의 시점일 경우 (ex. 3개월 할부 중 2, 3 개월째)
                                var date = LocalDate.of(selectedDateSp[0].toInt(), selectedDateSp[1].toInt(), selectedDateSp[2].toInt())
                                val sfd = DateTimeFormatter.ofPattern(formatt)
                                FBRef.receiptRef.child(userId).child(key).setValue((ReceiptModel(userId, key, startPayRoleId, date.format(sfd), category, price, payMethod, divide, divideMonth.text.toString(), nowPayMonth, place, content)))
                            }
                        }
                    }
                    Toast.makeText(this, "지출 내역이 수정되었습니다!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                originPayMethod != payMethod -> { // 결제 방법이 다를 경우 (현금 -> 카드 or 카드 -> 현금)
                    if(originPayMethod == "현금" && payMethod == "카드") {
                        if(payMethod == "카드" && divide == "일시불") {
                            FBRef.receiptRef.child(userId).child(key).setValue(ReceiptModel(userId, key, "", selectedDate, category, price, payMethod, divide, "", "", place, content))
                        } else if(payMethod == "카드" && divide == "할부") {
                            FBRef.receiptRef.child(userId).child(key).setValue(ReceiptModel(userId, key, key, selectedDate, category, (price.toInt() / divideMonth.text.toString().toInt()).toString(), payMethod, divide, divideMonth.text.toString(), "1", place, content))

                            for(i in 2 until divideMonth.text.toString().toInt() + 1) {
                                var childKey = FBRef.receiptRef.child(userId).push().key.toString() // 키 값을 먼저 받아옴
                                var date = LocalDate.of(selectedDateSp[0].toInt(), selectedDateSp[1].toInt(), selectedDateSp[2].toInt())
                                date = date.plusMonths((i-1).toLong())
                                val sfd = DateTimeFormatter.ofPattern(formatt)

                                FBRef.receiptRef.child(userId).child(childKey).setValue(ReceiptModel(userId, childKey, key, date.format(sfd), category, (price.toInt() / divideMonth.text.toString().toInt()).toString(), payMethod, divide, divideMonth.text.toString(), i.toString(), place, content))
                            }
                        }
                    } else if(originPayMethod == "카드" && payMethod == "현금") {
                        if(originDivide == "할부") {
                            Toast.makeText(this, "할부 내역은 변경 불가능합니다!", Toast.LENGTH_SHORT).show()
                        } else if(originDivide == "일시불") {
                            FBRef.receiptRef.child(userId).child(key).setValue(ReceiptModel(userId, key, "", selectedDate, category, price, payMethod, divide, "", "", place, content))
                        }
                    }
                    Toast.makeText(this, "지출 내역이 수정되었습니다!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        val deleteBtn = findViewById<ImageView>(R.id.deleteBtn)
        deleteBtn.setOnClickListener {
            if(originPayMethod == "카드" && (divide == originDivide && originDivide == "할부")) { // 결제 방법이 카드이고 할부인 경우
                Log.d("divideReceiptGroup", divideReceiptGroup.toString())
                Log.d("divideReceiptGroup", divideReceiptGroup.toString())

                for(i in 0 until divideReceiptGroup.size) // 할부 내역에 해당하는 모든 가계부 데이터를 삭제
                    FBRef.receiptRef.child(userId).child(divideReceiptGroup[i]).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제

            } else {
                FBRef.receiptRef.child(userId).child(key).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제
            }
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
        payMethodArea = findViewById(R.id.payMethodArea)
        payMethodSpinner = findViewById(R.id.payMethodSpinner)
        divideArea = findViewById(R.id.divideArea)
        divideRadioGroup = findViewById(R.id.divideRadioGroup)
        divideMonthArea = findViewById(R.id.divideMonthArea)
        divideMonth = findViewById(R.id.divideMonth)
        payMonthShow = findViewById(R.id.payMonthShow)
        payMonthStateNum = findViewById(R.id.payMonthStateNum)
        payMonthStatePrice = findViewById(R.id.payMonthStatePrice)
        placeArea = findViewById(R.id.placeArea)
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
                    originSelectedDate = post!!.date

                    val decimalFormat = DecimalFormat("#,###")

                    placeArea.setText(post!!.place)
                    contentArea.setText(post!!.content)

                    originPrice = post!!.price.toInt()

                    category = post!!.category

                    for (i in 0 until categorySpinner.count) {
                        if (categorySpinner.getItemAtPosition(i).toString() == category) {
                            categorySpinner.setSelection(i)
                            break
                        }
                    }

                    originPayMethod = post!!.payMethod
                    payMethod = post!!.payMethod
                    for (i in 0 until payMethodSpinner.count) {
                        if(payMethodSpinner.getItemAtPosition(i).toString() == payMethod) {
                            payMethodSpinner.setSelection(i)
                            break
                        }
                    }

                    originDivide = post!!.payMonthRole
                    if(payMethod == "카드") {
                        divideArea.visibility = VISIBLE
                        if (post!!.payMonthRole == "일시불") {
                            divideRadioGroup.check(findViewById<RadioButton>(R.id.no).id)
                            priceArea.setText(
                                decimalFormat.format(
                                    post!!.price.replace(",", "").toDouble()
                                )
                            )
                        }
                        else if(post!!.payMonthRole == "할부") {
                            divideRadioGroup.check(findViewById<RadioButton>(R.id.yes).id)
                            payMethodArea.visibility = GONE // 결제 수단 변경 못하도록 설정 -> 카드만 할부가 가능하므로
                            divideMonth.isEnabled = false // 할부 개월수 변경 못하도록 설정
                            originDivideMonth = post!!.payMonth
                            divideMonth.setText(post!!.payMonth)
                            startPayRoleId = post!!.startPayRoleId
                            nowPayMonth = post!!.nowPayMonth
                            divideArea.visibility = GONE

                            payMonthShow.visibility = VISIBLE
                            payMonthStateNum.text = "(" + post!!.nowPayMonth + "개월/" + post!!.payMonth + "개월)"

                            priceArea.setText(
                                decimalFormat.format(
                                    post!!.price.replace(",", "").toDouble()
                                )
                            )
                        }
                    } else if(payMethod == "현금") {
                        divideArea.visibility = GONE
                        priceArea.setText(
                            decimalFormat.format(
                                post!!.price.replace(",", "").toDouble()
                            )
                        )
                    }

                    if(originPayMethod == "카드" && originDivide == "할부") {
                        getDivideReceiptData(post!!.startPayRoleId) // 할부에 해당하는 receipt 데이터 가져오기
                        Log.d("divideReceiptGroup", divideReceiptGroup.toString())
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
}