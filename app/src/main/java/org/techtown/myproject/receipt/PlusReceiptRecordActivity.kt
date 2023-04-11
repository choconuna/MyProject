package org.techtown.myproject.receipt

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.*
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.firebase.auth.FirebaseAuth
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogMealModel
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.ReceiptModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class PlusReceiptRecordActivity : AppCompatActivity() {

    private lateinit var userId : String

    private lateinit var selectedDateArea : EditText
    private var date = LocalDate.now()
    private lateinit var selectedDate : String

    private lateinit var priceArea : EditText
    private lateinit var categorySpinner : Spinner
    private lateinit var category : String
    private lateinit var payMethodSpinner : Spinner
    private lateinit var payMethod : String
    private lateinit var divideArea : LinearLayout
    lateinit var divide : String
    private lateinit var divideRadioGroup: RadioGroup
    lateinit var divideMonthArea : LinearLayout
    lateinit var divideMonth : EditText
    lateinit var placeArea : EditText
    private lateinit var contentArea : EditText

    private val decimalFormat = DecimalFormat("#,###")
    private var result: String = ""

    private lateinit var plusBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plus_receipt_record)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        setData()

        selectedDateArea.setOnClickListener {
            val datepickercalendar = Calendar.getInstance()
            val year = datepickercalendar.get(Calendar.YEAR)
            val month = datepickercalendar.get(Calendar.MONTH)
            val day = datepickercalendar.get(Calendar.DAY_OF_MONTH)

            if(!this.isFinishing) {
                val dpd = DatePickerDialog(
                    this@PlusReceiptRecordActivity, R.style.MySpinnerDatePickerStyle,
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

        plusBtn.setOnClickListener {
            val price = priceArea.text.toString().replace(",", "")
            val place = placeArea.text.toString().trim()
            val content = contentArea.text.toString().trim()
            val selectedDateSp = selectedDate.split(".")
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
                else -> {
                    var key = ""
                    Log.d("plusList", "$payMethod $divide")

                    if(payMethod == "현금") {
                        key = FBRef.receiptRef.child(userId).push().key.toString() // 키 값을 먼저 받아옴
                        FBRef.receiptRef.child(userId).child(key).setValue(ReceiptModel(userId, key, "", selectedDate, category, price, payMethod, divide, "", "", place, content))
                    } else if(payMethod == "카드" && divide == "일시불") {
                        key = FBRef.receiptRef.child(userId).push().key.toString() // 키 값을 먼저 받아옴
                        FBRef.receiptRef.child(userId).child(key).setValue(ReceiptModel(userId, key, "", selectedDate, category, price, payMethod, divide, "", "", place, content))
                    } else if(payMethod == "카드" && divide == "할부") {
                        key = FBRef.receiptRef.child(userId).push().key.toString() // 키 값을 먼저 받아옴
                        FBRef.receiptRef.child(userId).child(key).setValue(ReceiptModel(userId, key, key, selectedDate, category, (price.toInt() / divideMonth.text.toString().toInt()).toString(), payMethod, divide, divideMonth.text.toString(), "1", place, content))

                        for(i in 2 until divideMonth.text.toString().toInt() + 1) {
                            var childKey = FBRef.receiptRef.child(userId).push().key.toString() // 키 값을 먼저 받아옴
                            var date = LocalDate.of(selectedDateSp[0].toInt(), selectedDateSp[1].toInt(), selectedDateSp[2].toInt())
                            date = date.plusMonths((i-1).toLong())
                            val sfd = DateTimeFormatter.ofPattern(formatt)

                            FBRef.receiptRef.child(userId).child(childKey).setValue(ReceiptModel(userId, childKey, key, date.format(sfd), category, (price.toInt() / divideMonth.text.toString().toInt()).toString(), payMethod, divide, divideMonth.text.toString(), i.toString(), place, content))
                        }
                    }

                    Toast.makeText(this, "지출 내역이 추가되었습니다!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
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
        payMethodSpinner = findViewById(R.id.payMethodSpinner)
        divideArea = findViewById(R.id.divideArea)
        divideRadioGroup = findViewById(R.id.divideRadioGroup)
        divideMonthArea = findViewById(R.id.divideMonthArea)
        divideMonth = findViewById(R.id.divideMonth)
        placeArea = findViewById(R.id.placeArea)
        contentArea = findViewById(R.id.contentArea)
        plusBtn = findViewById(R.id.plusBtn)

        val format = "yyyy.MM.dd (E)"
        val sdf = DateTimeFormatter.ofPattern(format)
        selectedDateArea.setText(date.format(sdf))

        val formatt = "yyyy.MM.dd.E"
        val sfd = DateTimeFormatter.ofPattern(formatt)
        selectedDate = date.format(sfd)
        Log.d("selectedDate", selectedDate)
    }
}