package org.techtown.myproject.receipt

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.deal.LocalReVAdapter
import org.techtown.myproject.note.CheckUpPictureReVAdapter
import org.techtown.myproject.note.DogCheckUpPictureInActivity
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.ReceiptModel
import org.techtown.myproject.utils.UserLocationModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class SearchReceiptActivity : AppCompatActivity() {

    private lateinit var myUid : String

    private lateinit var dateSpinner : Spinner
    private lateinit var date : String

    private lateinit var dateShowArea : LinearLayout
    private lateinit var showStartDate : LinearLayout // 클릭 시 달력 나옴 -> 날짜 선택하도록
    private lateinit var startDateArea : TextView // 선택된 시작 날짜
    private lateinit var showEndDate : LinearLayout // 클릭 시 달력 나옴 -> 날짜 선택하도록
    private lateinit var endDateArea : TextView // 선택된 종료 날짜

    private lateinit var backBtn : ImageView
    private lateinit var categorySpinner : Spinner
    private lateinit var category : String
    private lateinit var searchArea: EditText
    private lateinit var searchBtn : ImageView

    private lateinit var showPriceArea : ConstraintLayout
    private lateinit var priceArea : TextView
    private lateinit var frame3 : LinearLayout

    private lateinit var receiptSearchRecyclerView: RecyclerView
    private lateinit var receiptSearchRVAdapter: ReceiptSearchReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager
    private val receiptSearchList = ArrayList<ReceiptModel>()

    private var receiptMap : MutableMap<ReceiptModel, Long> = mutableMapOf()
    private var sortedMap : MutableMap<ReceiptModel, Long> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_receipt)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        setData()

        dateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, id: Long) {
                date = adapterView.getItemAtPosition(position).toString()

                if(date == "기간") {
                    dateShowArea.visibility = VISIBLE
                } else {
                    dateShowArea.visibility = GONE
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }

        showStartDate.setOnClickListener {
            val datepickercalendar = Calendar.getInstance()
            val year = datepickercalendar.get(Calendar.YEAR)
            val month = datepickercalendar.get(Calendar.MONTH)
            val day = datepickercalendar.get(Calendar.DAY_OF_MONTH)

            // 이전에 선택된 날짜 가져오기
            val selectedDate = startDateArea.text.toString().split('.')
            val selectedStartYear = selectedDate[0].toIntOrNull()
            val selectedStartMonth = selectedDate[1].toIntOrNull()?.minus(1) // 월은 0부터 시작하기 때문에 1을 빼줌
            val selectedStartDay = selectedDate[2].toIntOrNull()

            if(!this.isFinishing) {
                val dpd = DatePickerDialog(
                    this@SearchReceiptActivity, R.style.MySpinnerDatePickerStyle,
                    { _, year, monthOfYear, dayOfMonth ->

                        val month = monthOfYear + 1 // 월이 0부터 시작하여 1을 더해줌
                        val calendar = Calendar.getInstance() //선택한 날짜의 요일을 구하기 위한 calendar
                        calendar.set(year, monthOfYear, dayOfMonth) //선택한 날짜 세팅

                        if(month.toString().length == 1 && dayOfMonth.toString().length == 1) {
                            startDateArea.text = "$year.0$month.0$dayOfMonth"
                        } else if(dayOfMonth.toString().length == 1) {
                            startDateArea.text = "$year.$month.0$dayOfMonth"
                        } else if(month.toString().length == 1) {
                            startDateArea.text = "$year.0$month.$dayOfMonth"
                        } else {
                            startDateArea.text = "$year.$month.$dayOfMonth"
                        }
                    }, selectedStartYear!!, selectedStartMonth!!, selectedStartDay!!
                )
                dpd.show()
            }
        }

        showEndDate.setOnClickListener {
            val datepickercalendar = Calendar.getInstance()
            val year = datepickercalendar.get(Calendar.YEAR)
            val month = datepickercalendar.get(Calendar.MONTH)
            val day = datepickercalendar.get(Calendar.DAY_OF_MONTH)

            // 이전에 선택된 날짜 가져오기
            val selectedDate = endDateArea.text.toString().split('.')
            val selectedEndYear = selectedDate[0].toIntOrNull()
            val selectedEndMonth = selectedDate[1].toIntOrNull()?.minus(1) // 월은 0부터 시작하기 때문에 1을 빼줌
            val selectedEndDay = selectedDate[2].toIntOrNull()

            if(!this.isFinishing) {
                val dpd = DatePickerDialog(
                    this@SearchReceiptActivity, R.style.MySpinnerDatePickerStyle,
                    { _, year, monthOfYear, dayOfMonth ->

                        val month = monthOfYear + 1 // 월이 0부터 시작하여 1을 더해줌
                        val calendar = Calendar.getInstance() //선택한 날짜의 요일을 구하기 위한 calendar
                        calendar.set(year, monthOfYear, dayOfMonth) //선택한 날짜 세팅

                        if(month.toString().length == 1 && dayOfMonth.toString().length == 1) {
                            endDateArea.text = "$year.0$month.0$dayOfMonth"
                        } else if(dayOfMonth.toString().length == 1) {
                            endDateArea.text = "$year.$month.0$dayOfMonth"
                        } else if(month.toString().length == 1) {
                            endDateArea.text = "$year.0$month.$dayOfMonth"
                        } else {
                            endDateArea.text = "$year.$month.$dayOfMonth"
                        }
                    }, selectedEndYear!!, selectedEndMonth!!, selectedEndDay!!
                )
                dpd.show()
            }
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, id: Long) {
                category = adapterView.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }

        searchBtn.setOnClickListener {

            val searchText = searchArea.text.toString().trim()
            getReceiptData(date, category, searchText)

            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(searchArea.windowToken, 0)
        }

        receiptSearchRVAdapter.setItemClickListener(object: ReceiptSearchReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val intent = Intent(applicationContext, ReceiptRecordEditActivity::class.java)
                intent.putExtra("key", receiptSearchList[position].receiptId)
                startActivity(intent)
            }
        })

        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setData() {

        dateSpinner = findViewById(R.id.dateSpinner)
        date = dateSpinner.getItemAtPosition(0).toString()
        dateShowArea = findViewById(R.id.dateShowArea)

        showStartDate = findViewById(R.id.showStartDate)
        startDateArea = findViewById(R.id.startDateArea)
        showEndDate = findViewById(R.id.showEndDate)
        endDateArea = findViewById(R.id.endDateArea)

        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)
        startDateArea.text = formattedDate // 시작 날짜를 일단 현재 날짜로 설정
        endDateArea.text = formattedDate // 종료 날짜를 일단 현재 날짜로 설정

        backBtn = findViewById(R.id.backBtn)
        searchBtn = findViewById(R.id.searchBtn)
        categorySpinner = findViewById(R.id.categorySpinner)
        category = categorySpinner.getItemAtPosition(0).toString()
        searchArea = findViewById(R.id.searchArea)

        showPriceArea = findViewById(R.id.showPriceArea)
        priceArea = findViewById(R.id.priceArea)
        frame3 = findViewById(R.id.frame3)

        receiptSearchRecyclerView = findViewById(R.id.receiptSearchRecyclerView)
        receiptSearchRVAdapter = ReceiptSearchReVAdapter(receiptSearchList)
        receiptSearchRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        receiptSearchRecyclerView.layoutManager = layoutManager
        receiptSearchRecyclerView.adapter = receiptSearchRVAdapter // 어댑터 연결
    }

    private fun getReceiptData(date : String, category : String, searchText : String) {
        val query = FBRef.receiptRef.child(myUid).orderByChild("content")
        Log.d("receiptModel", searchText)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                try {

                    var price = 0

                    receiptSearchList.clear()
                    receiptMap.clear()
                    sortedMap.clear()

                    for (snapshot in dataSnapshot.children) {
                        val receiptModel = snapshot.getValue(ReceiptModel::class.java)

                        Log.d("receiptModel", receiptModel!!.toString())

                        if (receiptModel!!.content.contains(searchText)) {
                            if (date == "전체") { // 전체 기간일 경우
                                if (category == "전체") { // 전체 카테고리를 선택했을 경우

                                    var nowDateSp = receiptModel!!.date.split(".")
                                    var nowDate = nowDateSp[0] + nowDateSp[1] + nowDateSp[2]

                                    price += receiptModel!!.price.toInt()

                                    receiptMap[receiptModel] = nowDate.toLong()
                                } else { // 특정 카테고리를 선택했을 경우
                                    if (receiptModel!!.category == category) {
                                        var nowDateSp = receiptModel!!.date.split(".")
                                        var nowDate = nowDateSp[0] + nowDateSp[1] + nowDateSp[2]

                                        price += receiptModel!!.price.toInt()

                                        receiptMap[receiptModel] = nowDate.toLong()
                                    }
                                }
                            } else { // 전체 기간이 아닐 경우
                                if (category == "전체") { // 전체 카테고리를 선택했을 경우

                                    // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                    val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                    val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                    val nowDateFm = receiptModel!!.date.split(".")[0] + "." + receiptModel!!.date.split(".")[1] + "." + receiptModel!!.date.split(".")[2]

                                    if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                        var nowDateSp = receiptModel!!.date.split(".")
                                        var nowDate = nowDateSp[0] + nowDateSp[1] + nowDateSp[2]

                                        price += receiptModel!!.price.toInt()

                                        receiptMap[receiptModel] = nowDate.toLong()
                                    }
                                } else { // 특정 카테고리를 선택했을 경우
                                    if (receiptModel!!.category == category) {

                                        // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                        val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                        val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                        val nowDateFm = receiptModel!!.date.split(".")[0] + "." + receiptModel!!.date.split(".")[1] + "." + receiptModel!!.date.split(".")[2]

                                        if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                            var nowDateSp = receiptModel!!.date.split(".")
                                            var nowDate = nowDateSp[0] + nowDateSp[1] + nowDateSp[2]

                                            price += receiptModel!!.price.toInt()

                                            receiptMap[receiptModel] = nowDate.toLong()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val decimalFormat = DecimalFormat("#,###")
                    priceArea.text = decimalFormat.format(price.toString().replace(",","").toDouble()) + "원"

                    showPriceArea.visibility = VISIBLE
                    frame3.visibility = VISIBLE

                    sortedMap = sortMapByKey(receiptMap)
                    for((key, value) in sortedMap.entries) {
                        receiptSearchList.add(key)
                        Log.d("sortedMap", key.toString())
                    }

                    receiptSearchRVAdapter.notifyDataSetChanged()
                } catch(e : Exception) { }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 에러 처리
            }
        })
    }

    fun isBetweenDates(dateString: String, startDate: Date, endDate: Date): Boolean { // dateString이 기간 사이에 있는지 확인하기 위한 함수
        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        return date?.after(startDate) == true && date.before(endDate)
    }

    private fun sortMapByKey(map: MutableMap<ReceiptModel, Long>): LinkedHashMap<ReceiptModel, Long> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.value }

        val result = LinkedHashMap<ReceiptModel, Long>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }
}