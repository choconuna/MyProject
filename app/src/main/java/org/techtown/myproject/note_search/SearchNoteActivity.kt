package org.techtown.myproject.note_search

import android.app.Activity
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
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.community.CommunityModel
import org.techtown.myproject.note.*
import org.techtown.myproject.receipt.ReceiptRecordEditActivity
import org.techtown.myproject.receipt.ReceiptSearchReVAdapter
import org.techtown.myproject.utils.*
import java.text.SimpleDateFormat
import java.util.*

class SearchNoteActivity : AppCompatActivity() {

    private val TAG = SearchNoteActivity::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    private lateinit var myUid : String
    private lateinit var dogId : String

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

    private lateinit var checkUpCategorySpinner : Spinner
    private lateinit var checkUpCategory : String

    private lateinit var checkUpPartSpinner : Spinner
    private lateinit var checkUpPart : String

    private lateinit var snackCategorySpinner : Spinner
    private lateinit var snackCategory : String

    private lateinit var tonicPartSpinner : Spinner
    private lateinit var tonicPart : String

    private lateinit var rangeSpinner : Spinner
    private lateinit var range : String

    private lateinit var mealSearchRecyclerView : RecyclerView
    private val mealDataList = ArrayList<DogMealModel>()
    lateinit var mealRVAdapter : MealSearchReVAdapter
    lateinit var layoutManager : RecyclerView.LayoutManager
    private var mealMap : MutableMap<DogMealModel, Long> = mutableMapOf()
    private var mealSortedMap : MutableMap<DogMealModel, Long> = mutableMapOf()

    private lateinit var snackSearchRecyclerView : RecyclerView
    private val snackDataList = ArrayList<DogSnackModel>()
    lateinit var snackRVAdapter : SnackSearchReVAdapter
    lateinit var sLayoutManager : RecyclerView.LayoutManager
    private var snackMap : MutableMap<DogSnackModel, Long> = mutableMapOf()
    private var snackSortedMap : MutableMap<DogSnackModel, Long> = mutableMapOf()

    private lateinit var tonicSearchRecyclerView : RecyclerView
    private val tonicDataList = ArrayList<DogTonicModel>()
    lateinit var tonicRVAdapter : TonicSearchReVAdapter
    lateinit var tLayoutManager : RecyclerView.LayoutManager
    private var tonicMap : MutableMap<DogTonicModel, Long> = mutableMapOf()
    private var tonicSortedMap : MutableMap<DogTonicModel, Long> = mutableMapOf()

    private lateinit var waterSearchRecyclerView : RecyclerView
    private val waterDataList = ArrayList<DogWaterModel>()
    lateinit var waterRVAdapter : WaterSearchReVAdapter
    lateinit var wLayoutManager : RecyclerView.LayoutManager
    private var waterMap : MutableMap<DogWaterModel, Long> = mutableMapOf()
    private var waterSortedMap : MutableMap<DogWaterModel, Long> = mutableMapOf()

    private lateinit var peeSearchRecyclerView : RecyclerView
    private val peeDataList = ArrayList<DogPeeModel>()
    lateinit var peeRVAdapter : PeeSearchReVAdapter
    lateinit var pLayoutManager : RecyclerView.LayoutManager
    private var peeMap : MutableMap<DogPeeModel, Long> = mutableMapOf()
    private var peeSortedMap : MutableMap<DogPeeModel, Long> = mutableMapOf()

    private lateinit var dungSearchRecyclerView : RecyclerView
    private val dungDataList = ArrayList<DogDungModel>()
    lateinit var dungRVAdapter : DungSearchReVAdapter
    lateinit var dLayoutManager : RecyclerView.LayoutManager
    private var dungMap : MutableMap<DogDungModel, Long> = mutableMapOf()
    private var dungSortedMap : MutableMap<DogDungModel, Long> = mutableMapOf()

    private lateinit var vomitSearchRecyclerView : RecyclerView
    private val vomitDataList = ArrayList<DogVomitModel>()
    lateinit var vomitRVAdapter : VomitSearchReVAdapter
    lateinit var vLayoutManager : RecyclerView.LayoutManager
    private var vomitMap : MutableMap<DogVomitModel, Long> = mutableMapOf()
    private var vomitSortedMap : MutableMap<DogVomitModel, Long> = mutableMapOf()

    private lateinit var heartSearchRecyclerView : RecyclerView
    private val heartDataList = ArrayList<DogHeartModel>()
    lateinit var heartRVAdapter : HeartSearchReVAdapter
    lateinit var hLayoutManager : RecyclerView.LayoutManager
    private var heartMap : MutableMap<DogHeartModel, Long> = mutableMapOf()
    private var heartSortedMap : MutableMap<DogHeartModel, Long> = mutableMapOf()

    private lateinit var medicineSearchRecyclerView : RecyclerView
    private val medicineDataList = ArrayList<DogMedicineModel>()
    lateinit var medicineRVAdapter : MedicineSearchReVAdapter
    lateinit var mLayoutManager : RecyclerView.LayoutManager
    private var medicineMap : MutableMap<DogMedicineModel, Long> = mutableMapOf()
    private var medicineSortedMap : MutableMap<DogMedicineModel, Long> = mutableMapOf()

    private lateinit var checkUpInputSearchRecyclerView : RecyclerView
    private val checkUpInputDataList = ArrayList<DogCheckUpInputModel>()
    lateinit var checkUpInputRVAdapter : CheckUpInputSearchReVAdapter
    lateinit var ciLayoutManager : RecyclerView.LayoutManager
    private var checkUpInputMap : MutableMap<DogCheckUpInputModel, Long> = mutableMapOf()
    private var checkUpInputSortedMap : MutableMap<DogCheckUpInputModel, Long> = mutableMapOf()

    private lateinit var checkUpPictureSearchRecyclerView : RecyclerView
    private val checkUpPictureDataList = ArrayList<DogCheckUpPictureModel>()
    lateinit var checkUpPictureRVAdapter : CheckUpPictureSearchReVAdapter
    lateinit var cpLayoutManager : RecyclerView.LayoutManager
    private var checkUpPictureMap : MutableMap<DogCheckUpPictureModel, Long> = mutableMapOf()
    private var checkUpPictureSortedMap : MutableMap<DogCheckUpPictureModel, Long> = mutableMapOf()

    private lateinit var memoSearchRecyclerView : RecyclerView
    private val memoDataList = ArrayList<DogMemoModel>()
    lateinit var memoRVAdapter : MemoReVAdapter
    lateinit var mmLayoutManager : RecyclerView.LayoutManager
    private var memoMap : MutableMap<DogMemoModel, Long> = mutableMapOf()
    private var memoSortedMap : MutableMap<DogMemoModel, Long> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_note)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(myUid, "").toString() // 현재 대표 반려견의 id

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
                    this@SearchNoteActivity, R.style.MySpinnerDatePickerStyle,
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
                    this@SearchNoteActivity, R.style.MySpinnerDatePickerStyle,
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

                when (category) {
                    "검사 사진" -> {
                        checkUpCategorySpinner.visibility = VISIBLE
                        checkUpPartSpinner.visibility = GONE
                        rangeSpinner.visibility = GONE
                        snackCategorySpinner.visibility = GONE
                        tonicPartSpinner.visibility = GONE
                        searchArea.visibility = VISIBLE
                    }
                    "수치 검사" -> {
                        checkUpCategorySpinner.visibility = GONE
                        checkUpPartSpinner.visibility = VISIBLE
                        rangeSpinner.visibility = VISIBLE
                        snackCategorySpinner.visibility = GONE
                        tonicPartSpinner.visibility = GONE
                        searchArea.visibility = VISIBLE
                    }
                    "호흡수" -> {
                        checkUpCategorySpinner.visibility = GONE
                        checkUpPartSpinner.visibility = GONE
                        rangeSpinner.visibility = VISIBLE
                        snackCategorySpinner.visibility = GONE
                        tonicPartSpinner.visibility = GONE
                        searchArea.visibility = GONE
                    }
                    "영양제" -> {
                        checkUpCategorySpinner.visibility = GONE
                        checkUpPartSpinner.visibility = GONE
                        rangeSpinner.visibility = GONE
                        snackCategorySpinner.visibility = GONE
                        tonicPartSpinner.visibility = VISIBLE
                        searchArea.visibility = VISIBLE
                    }
                    "간식" -> {
                        checkUpCategorySpinner.visibility = GONE
                        checkUpPartSpinner.visibility = GONE
                        rangeSpinner.visibility = GONE
                        snackCategorySpinner.visibility = VISIBLE
                        tonicPartSpinner.visibility = GONE
                        searchArea.visibility = VISIBLE
                    }
                    else -> {
                        checkUpCategorySpinner.visibility = GONE
                        checkUpPartSpinner.visibility = GONE
                        rangeSpinner.visibility = GONE
                        snackCategorySpinner.visibility = GONE
                        tonicPartSpinner.visibility = GONE
                        searchArea.visibility = VISIBLE
                    }
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }

        checkUpCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, id: Long) {
                checkUpCategory = adapterView.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }

        checkUpPartSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, id: Long) {
                checkUpPart = adapterView.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }

        snackCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, id: Long) {
                snackCategory = adapterView.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }

        tonicPartSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, id: Long) {
                tonicPart = adapterView.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }

        rangeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, id: Long) {
                range = adapterView.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }

        searchBtn.setOnClickListener {

            val searchText = searchArea.text.toString().trim()
            getNoteData(date, category, checkUpCategory, checkUpPart, snackCategory, tonicPart, range, searchText)

            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(searchArea.windowToken, 0)
        }

        checkUpPictureRVAdapter.setItemClickListener(object: CheckUpPictureSearchReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                val intent = Intent(applicationContext, DogCheckUpPictureInActivity::class.java)
                intent.putExtra("date", checkUpPictureDataList[position].date)
                intent.putExtra("id", checkUpPictureDataList[position].dogCheckUpPictureId)
                startActivity(intent)
            }
        })

        memoRVAdapter.setItemClickListener(object: MemoReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                val intent = Intent(applicationContext, DogMemoInActivity::class.java)
                intent.putExtra("nowDate", memoDataList[position].date)
                intent.putExtra("id", memoDataList[position].dogMemoId)
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

        checkUpCategorySpinner = findViewById(R.id.checkUpCategorySpinner)
        checkUpCategory = checkUpCategorySpinner.getItemAtPosition(0).toString()

        checkUpPartSpinner = findViewById(R.id.checkUpPartSpinner)
        checkUpPart = checkUpPartSpinner.getItemAtPosition(0).toString()

        snackCategorySpinner = findViewById(R.id.snackCategorySpinner)
        snackCategory = snackCategorySpinner.getItemAtPosition(0).toString()

        tonicPartSpinner = findViewById(R.id.tonicPartSpinner)
        tonicPart = tonicPartSpinner.getItemAtPosition(0).toString()

        rangeSpinner = findViewById(R.id.rangeSpinner)
        range = rangeSpinner.getItemAtPosition(0).toString()

        mealSearchRecyclerView = findViewById(R.id.mealSearchRecyclerView)
        mealRVAdapter = MealSearchReVAdapter(mealDataList)
        mealSearchRecyclerView.setItemViewCacheSize(20)
        mealSearchRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mealSearchRecyclerView.layoutManager = layoutManager
        mealSearchRecyclerView.adapter = mealRVAdapter

        snackSearchRecyclerView = findViewById(R.id.snackSearchRecyclerView)
        snackRVAdapter = SnackSearchReVAdapter(snackDataList)
        snackSearchRecyclerView.setItemViewCacheSize(20)
        snackSearchRecyclerView.setHasFixedSize(true)
        sLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        snackSearchRecyclerView.layoutManager = sLayoutManager
        snackSearchRecyclerView.adapter = snackRVAdapter

        tonicSearchRecyclerView = findViewById(R.id.tonicSearchRecyclerView)
        tonicRVAdapter = TonicSearchReVAdapter(tonicDataList)
        tonicSearchRecyclerView.setItemViewCacheSize(20)
        tonicSearchRecyclerView.setHasFixedSize(true)
        tLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        tonicSearchRecyclerView.layoutManager = tLayoutManager
        tonicSearchRecyclerView.adapter = tonicRVAdapter

        waterSearchRecyclerView = findViewById(R.id.waterSearchRecyclerView)
        waterRVAdapter = WaterSearchReVAdapter(waterDataList)
        waterSearchRecyclerView.setItemViewCacheSize(20)
        waterSearchRecyclerView.setHasFixedSize(true)
        wLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        waterSearchRecyclerView.layoutManager = wLayoutManager
        waterSearchRecyclerView.adapter = waterRVAdapter

        peeSearchRecyclerView = findViewById(R.id.peeSearchRecyclerView)
        peeRVAdapter = PeeSearchReVAdapter(peeDataList)
        peeSearchRecyclerView.setItemViewCacheSize(20)
        peeSearchRecyclerView.setHasFixedSize(true)
        pLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        peeSearchRecyclerView.layoutManager = pLayoutManager
        peeSearchRecyclerView.adapter = peeRVAdapter

        dungSearchRecyclerView = findViewById(R.id.dungSearchRecyclerView)
        dungRVAdapter = DungSearchReVAdapter(dungDataList)
        dungSearchRecyclerView.setItemViewCacheSize(20)
        dungSearchRecyclerView.setHasFixedSize(true)
        dLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        dungSearchRecyclerView.layoutManager = dLayoutManager
        dungSearchRecyclerView.adapter = dungRVAdapter

        vomitSearchRecyclerView = findViewById(R.id.vomitSearchRecyclerView)
        vomitRVAdapter = VomitSearchReVAdapter(vomitDataList)
        vomitSearchRecyclerView.setItemViewCacheSize(20)
        vomitSearchRecyclerView.setHasFixedSize(true)
        vLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        vomitSearchRecyclerView.layoutManager = vLayoutManager
        vomitSearchRecyclerView.adapter = vomitRVAdapter

        heartSearchRecyclerView = findViewById(R.id.heartSearchRecyclerView)
        heartRVAdapter = HeartSearchReVAdapter(heartDataList)
        heartSearchRecyclerView.setItemViewCacheSize(20)
        heartSearchRecyclerView.setHasFixedSize(true)
        hLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        heartSearchRecyclerView.layoutManager = hLayoutManager
        heartSearchRecyclerView.adapter = heartRVAdapter

        medicineSearchRecyclerView = findViewById(R.id.medicineSearchRecyclerView)
        medicineRVAdapter = MedicineSearchReVAdapter(medicineDataList)
        medicineSearchRecyclerView.setItemViewCacheSize(20)
        medicineSearchRecyclerView.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        medicineSearchRecyclerView.layoutManager = mLayoutManager
        medicineSearchRecyclerView.adapter = medicineRVAdapter

        checkUpInputSearchRecyclerView = findViewById(R.id.checkUpInputSearchRecyclerView)
        checkUpInputRVAdapter = CheckUpInputSearchReVAdapter(checkUpInputDataList)
        checkUpInputSearchRecyclerView.setItemViewCacheSize(20)
        checkUpInputSearchRecyclerView.setHasFixedSize(true)
        ciLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        checkUpInputSearchRecyclerView.layoutManager = ciLayoutManager
        checkUpInputSearchRecyclerView.adapter = checkUpInputRVAdapter

        checkUpPictureSearchRecyclerView = findViewById(R.id.checkUpPictureSearchRecyclerView)
        checkUpPictureRVAdapter = CheckUpPictureSearchReVAdapter(checkUpPictureDataList)
        checkUpPictureSearchRecyclerView.setItemViewCacheSize(20)
        checkUpPictureSearchRecyclerView.setHasFixedSize(true)
        cpLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        checkUpPictureSearchRecyclerView.layoutManager = cpLayoutManager
        checkUpPictureSearchRecyclerView.adapter = checkUpPictureRVAdapter

        memoSearchRecyclerView = findViewById(R.id.memoSearchRecyclerView)
        memoRVAdapter = MemoReVAdapter(memoDataList)
        memoSearchRecyclerView.setItemViewCacheSize(20)
        memoSearchRecyclerView.setHasFixedSize(true)
        mmLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        memoSearchRecyclerView.layoutManager = mmLayoutManager
        memoSearchRecyclerView.adapter = memoRVAdapter
    }

    private fun getNoteData(date : String, category : String, checkUpCategory : String, checkUpPart : String, snackCategory : String, tonicPart : String, range : String, searchText : String) {
        when (category) {
            "사료" ->  {
                getMealData(date, searchText)
                mealSearchRecyclerView.visibility = VISIBLE
                snackSearchRecyclerView.visibility = GONE
                tonicSearchRecyclerView.visibility = GONE
                waterSearchRecyclerView.visibility = GONE
                peeSearchRecyclerView.visibility = GONE
                dungSearchRecyclerView.visibility = GONE
                vomitSearchRecyclerView.visibility = GONE
                heartSearchRecyclerView.visibility = GONE
                medicineSearchRecyclerView.visibility = GONE
                checkUpInputSearchRecyclerView.visibility = GONE
                checkUpPictureSearchRecyclerView.visibility = GONE
                memoSearchRecyclerView.visibility = GONE
            }
            "간식" -> {
                getSnackData(date, snackCategory, searchText)
                mealSearchRecyclerView.visibility = GONE
                snackSearchRecyclerView.visibility = VISIBLE
                tonicSearchRecyclerView.visibility = GONE
                waterSearchRecyclerView.visibility = GONE
                peeSearchRecyclerView.visibility = GONE
                dungSearchRecyclerView.visibility = GONE
                vomitSearchRecyclerView.visibility = GONE
                heartSearchRecyclerView.visibility = GONE
                medicineSearchRecyclerView.visibility = GONE
                checkUpInputSearchRecyclerView.visibility = GONE
                checkUpPictureSearchRecyclerView.visibility = GONE
                memoSearchRecyclerView.visibility = GONE
            }
            "영양제" -> {
                getTonicData(date, tonicPart, searchText)
                mealSearchRecyclerView.visibility = GONE
                snackSearchRecyclerView.visibility = GONE
                tonicSearchRecyclerView.visibility = VISIBLE
                waterSearchRecyclerView.visibility = GONE
                peeSearchRecyclerView.visibility = GONE
                dungSearchRecyclerView.visibility = GONE
                vomitSearchRecyclerView.visibility = GONE
                heartSearchRecyclerView.visibility = GONE
                medicineSearchRecyclerView.visibility = GONE
                checkUpInputSearchRecyclerView.visibility = GONE
                checkUpPictureSearchRecyclerView.visibility = GONE
                memoSearchRecyclerView.visibility = GONE
            }
            "물" -> {
                getWaterData(date, searchText)
                mealSearchRecyclerView.visibility = GONE
                snackSearchRecyclerView.visibility = GONE
                tonicSearchRecyclerView.visibility = GONE
                waterSearchRecyclerView.visibility = VISIBLE
                peeSearchRecyclerView.visibility = GONE
                dungSearchRecyclerView.visibility = GONE
                vomitSearchRecyclerView.visibility = GONE
                heartSearchRecyclerView.visibility = GONE
                medicineSearchRecyclerView.visibility = GONE
                checkUpInputSearchRecyclerView.visibility = GONE
                checkUpPictureSearchRecyclerView.visibility = GONE
                memoSearchRecyclerView.visibility = GONE
            }
            "소변" -> {
                getPeeData(date, searchText)
                mealSearchRecyclerView.visibility = GONE
                snackSearchRecyclerView.visibility = GONE
                tonicSearchRecyclerView.visibility = GONE
                waterSearchRecyclerView.visibility = GONE
                peeSearchRecyclerView.visibility = VISIBLE
                dungSearchRecyclerView.visibility = GONE
                vomitSearchRecyclerView.visibility = GONE
                heartSearchRecyclerView.visibility = GONE
                medicineSearchRecyclerView.visibility = GONE
                checkUpInputSearchRecyclerView.visibility = GONE
                checkUpPictureSearchRecyclerView.visibility = GONE
                memoSearchRecyclerView.visibility = GONE
            }
            "대변" -> {
                getDungData(date, searchText)
                mealSearchRecyclerView.visibility = GONE
                snackSearchRecyclerView.visibility = GONE
                tonicSearchRecyclerView.visibility = GONE
                waterSearchRecyclerView.visibility = GONE
                peeSearchRecyclerView.visibility = GONE
                dungSearchRecyclerView.visibility = VISIBLE
                vomitSearchRecyclerView.visibility = GONE
                heartSearchRecyclerView.visibility = GONE
                medicineSearchRecyclerView.visibility = GONE
                checkUpInputSearchRecyclerView.visibility = GONE
                checkUpPictureSearchRecyclerView.visibility = GONE
                memoSearchRecyclerView.visibility = GONE
            }
            "구토" -> {
                getVomitData(date, searchText)
                mealSearchRecyclerView.visibility = GONE
                snackSearchRecyclerView.visibility = GONE
                tonicSearchRecyclerView.visibility = GONE
                waterSearchRecyclerView.visibility = GONE
                peeSearchRecyclerView.visibility = GONE
                dungSearchRecyclerView.visibility = GONE
                vomitSearchRecyclerView.visibility = VISIBLE
                heartSearchRecyclerView.visibility = GONE
                medicineSearchRecyclerView.visibility = GONE
                checkUpInputSearchRecyclerView.visibility = GONE
                checkUpPictureSearchRecyclerView.visibility = GONE
                memoSearchRecyclerView.visibility = GONE
            }
            "호흡수" -> {
                getHeartData(date, range)
                mealSearchRecyclerView.visibility = GONE
                snackSearchRecyclerView.visibility = GONE
                tonicSearchRecyclerView.visibility = GONE
                waterSearchRecyclerView.visibility = GONE
                peeSearchRecyclerView.visibility = GONE
                dungSearchRecyclerView.visibility = GONE
                vomitSearchRecyclerView.visibility = GONE
                heartSearchRecyclerView.visibility = VISIBLE
                medicineSearchRecyclerView.visibility = GONE
                checkUpInputSearchRecyclerView.visibility = GONE
                checkUpPictureSearchRecyclerView.visibility = GONE
                memoSearchRecyclerView.visibility = GONE
            }
            "투약" -> {
                getMedicineData(date, searchText)
                mealSearchRecyclerView.visibility = GONE
                snackSearchRecyclerView.visibility = GONE
                tonicSearchRecyclerView.visibility = GONE
                waterSearchRecyclerView.visibility = GONE
                peeSearchRecyclerView.visibility = GONE
                dungSearchRecyclerView.visibility = GONE
                vomitSearchRecyclerView.visibility = GONE
                heartSearchRecyclerView.visibility = GONE
                medicineSearchRecyclerView.visibility = VISIBLE
                checkUpInputSearchRecyclerView.visibility = GONE
                checkUpPictureSearchRecyclerView.visibility = GONE
                memoSearchRecyclerView.visibility = GONE
            }
            "수치 검사" -> {
                getCheckUpInputData(date, checkUpPart, range, searchText)
                mealSearchRecyclerView.visibility = GONE
                snackSearchRecyclerView.visibility = GONE
                tonicSearchRecyclerView.visibility = GONE
                waterSearchRecyclerView.visibility = GONE
                peeSearchRecyclerView.visibility = GONE
                dungSearchRecyclerView.visibility = GONE
                vomitSearchRecyclerView.visibility = GONE
                heartSearchRecyclerView.visibility = GONE
                medicineSearchRecyclerView.visibility = GONE
                checkUpInputSearchRecyclerView.visibility = VISIBLE
                checkUpPictureSearchRecyclerView.visibility = GONE
                memoSearchRecyclerView.visibility = GONE
            }
            "검사 사진" -> {
                getCheckUpPictureData(date, checkUpCategory, searchText)
                mealSearchRecyclerView.visibility = GONE
                snackSearchRecyclerView.visibility = GONE
                tonicSearchRecyclerView.visibility = GONE
                waterSearchRecyclerView.visibility = GONE
                peeSearchRecyclerView.visibility = GONE
                dungSearchRecyclerView.visibility = GONE
                vomitSearchRecyclerView.visibility = GONE
                heartSearchRecyclerView.visibility = GONE
                medicineSearchRecyclerView.visibility = GONE
                checkUpInputSearchRecyclerView.visibility = GONE
                checkUpPictureSearchRecyclerView.visibility = VISIBLE
                memoSearchRecyclerView.visibility = GONE
            }
            "메모" -> {
                getMemoData(date, searchText)
                mealSearchRecyclerView.visibility = GONE
                snackSearchRecyclerView.visibility = GONE
                tonicSearchRecyclerView.visibility = GONE
                waterSearchRecyclerView.visibility = GONE
                peeSearchRecyclerView.visibility = GONE
                dungSearchRecyclerView.visibility = GONE
                vomitSearchRecyclerView.visibility = GONE
                heartSearchRecyclerView.visibility = GONE
                medicineSearchRecyclerView.visibility = GONE
                checkUpInputSearchRecyclerView.visibility = GONE
                checkUpPictureSearchRecyclerView.visibility = GONE
                memoSearchRecyclerView.visibility = VISIBLE
            }
        }
    }

    private fun getMealData(date : String, searchText : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    mealDataList.clear()
                    mealMap.clear()
                    mealSortedMap.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogMealModel::class.java)
                        if(item!!.mealName.contains(searchText) || item!!.mealType.contains(searchText)) {
                            if(date == "전체") {
                                var nowDateSp = item!!.date.split(".")
                                var month = nowDateSp[1]
                                var day = nowDateSp[2]

                                if(month.length == 1)
                                    month = "0$month"
                                if(day.length == 1)
                                    day = "0$day"

                                var nowDate = nowDateSp[0] + month + day

                                mealMap[item!!] = nowDate.toLong()
                            } else {
                                var nowDateSp = item!!.date.split(".")
                                var month = nowDateSp[1]
                                var day = nowDateSp[2]

                                if(month.length == 1)
                                    month = "0$month"
                                if(day.length == 1)
                                    day = "0$day"

                                // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                    var nowDate = nowDateSp[0] + month + day

                                    mealMap[item!!] = nowDate.toLong()
                                }
                            }
                        }
                    }

                    mealSortedMap = mealSortMapByKey(mealMap)
                    for((key, value) in mealSortedMap.entries) {
                        mealDataList.add(key)
                    }

                    mealRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, "사료 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.mealRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun getSnackData(date : String, snackCategory: String, searchText : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    snackDataList.clear()
                    snackMap.clear()
                    snackSortedMap.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogSnackModel::class.java)
                        if(snackCategory == "전체") {
                            if(item!!.snackName.contains(searchText) || item!!.snackType.contains(searchText)) {
                                if (date == "전체") {
                                    var nowDateSp = item!!.date.split(".")
                                    var month = nowDateSp[1]
                                    var day = nowDateSp[2]

                                    if (month.length == 1)
                                        month = "0$month"
                                    if (day.length == 1)
                                        day = "0$day"

                                    var nowDate = nowDateSp[0] + month + day

                                    snackMap[item!!] = nowDate.toLong()
                                } else {
                                    var nowDateSp = item!!.date.split(".")
                                    var month = nowDateSp[1]
                                    var day = nowDateSp[2]

                                    if (month.length == 1)
                                        month = "0$month"
                                    if (day.length == 1)
                                        day = "0$day"

                                    // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                    val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                    val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                    val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                    if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                        var nowDate = nowDateSp[0] + month + day

                                        snackMap[item!!] = nowDate.toLong()
                                    }
                                }
                            }
                        } else {
                            if(item!!.snackType == snackCategory) {
                                if(item!!.snackName.contains(searchText) || item!!.snackType.contains(searchText)) {
                                    if (date == "전체") {
                                        var nowDateSp = item!!.date.split(".")
                                        var month = nowDateSp[1]
                                        var day = nowDateSp[2]

                                        if (month.length == 1)
                                            month = "0$month"
                                        if (day.length == 1)
                                            day = "0$day"

                                        var nowDate = nowDateSp[0] + month + day

                                        snackMap[item!!] = nowDate.toLong()
                                    } else {
                                        var nowDateSp = item!!.date.split(".")
                                        var month = nowDateSp[1]
                                        var day = nowDateSp[2]

                                        if (month.length == 1)
                                            month = "0$month"
                                        if (day.length == 1)
                                            day = "0$day"

                                        // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                        val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                        val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                        val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                        if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                            var nowDate = nowDateSp[0] + month + day

                                            snackMap[item!!] = nowDate.toLong()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    snackSortedMap = snackSortMapByKey(snackMap)
                    for((key, value) in snackSortedMap.entries) {
                        snackDataList.add(key)
                    }

                    snackRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, "간식 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.snackRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun getTonicData(date : String, tonicPart : String, searchText : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    tonicDataList.clear()
                    tonicMap.clear()
                    tonicSortedMap.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogTonicModel::class.java)
                        if(tonicPart == "전체") {
                            if (item!!.tonicName.contains(searchText) || item!!.tonicPart.contains(searchText)) {
                                if (date == "전체") {
                                    var nowDateSp = item!!.date.split(".")
                                    var month = nowDateSp[1]
                                    var day = nowDateSp[2]

                                    if (month.length == 1)
                                        month = "0$month"
                                    if (day.length == 1)
                                        day = "0$day"

                                    var nowDate = nowDateSp[0] + month + day

                                    tonicMap[item!!] = nowDate.toLong()
                                } else {
                                    var nowDateSp = item!!.date.split(".")
                                    var month = nowDateSp[1]
                                    var day = nowDateSp[2]

                                    if (month.length == 1)
                                        month = "0$month"
                                    if (day.length == 1)
                                        day = "0$day"

                                    // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                    val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                    val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                    val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                    if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                        var nowDate = nowDateSp[0] + month + day

                                        tonicMap[item!!] = nowDate.toLong()
                                    }
                                }
                            }
                        } else {
                            if(item!!.tonicPart == tonicPart) {
                                if (item!!.tonicName.contains(searchText) || item!!.tonicPart.contains(searchText)) {
                                    if (date == "전체") {
                                        var nowDateSp = item!!.date.split(".")
                                        var month = nowDateSp[1]
                                        var day = nowDateSp[2]

                                        if (month.length == 1)
                                            month = "0$month"
                                        if (day.length == 1)
                                            day = "0$day"

                                        var nowDate = nowDateSp[0] + month + day

                                        tonicMap[item!!] = nowDate.toLong()
                                    } else {
                                        var nowDateSp = item!!.date.split(".")
                                        var month = nowDateSp[1]
                                        var day = nowDateSp[2]

                                        if (month.length == 1)
                                            month = "0$month"
                                        if (day.length == 1)
                                            day = "0$day"

                                        // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                        val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                        val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                        val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                        if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                            var nowDate = nowDateSp[0] + month + day

                                            tonicMap[item!!] = nowDate.toLong()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    tonicSortedMap = tonicSortMapByKey(tonicMap)
                    for((key, value) in tonicSortedMap.entries) {
                        tonicDataList.add(key)
                    }

                    tonicRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, "영양제 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.tonicRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun getWaterData(date : String, searchText : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    waterDataList.clear()
                    waterMap.clear()
                    waterSortedMap.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogWaterModel::class.java)
                        if(date == "전체") {
                            var nowDateSp = item!!.date.split(".")
                            var month = nowDateSp[1]
                            var day = nowDateSp[2]

                            if(month.length == 1)
                                month = "0$month"
                            if(day.length == 1)
                                day = "0$day"

                            var nowDate = nowDateSp[0] + month + day

                            waterMap[item!!] = nowDate.toLong()
                        } else {
                            var nowDateSp = item!!.date.split(".")
                            var month = nowDateSp[1]
                            var day = nowDateSp[2]

                            if(month.length == 1)
                                month = "0$month"
                            if(day.length == 1)
                                day = "0$day"

                            // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                            val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                            val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                            val nowDateFm = nowDateSp[0] + "." + month + "." + day

                            if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                var nowDate = nowDateSp[0] + month + day

                                waterMap[item!!] = nowDate.toLong()
                            }
                        }
                    }

                    waterSortedMap = waterSortMapByKey(waterMap)
                    for((key, value) in waterSortedMap.entries) {
                        waterDataList.add(key)
                    }

                    waterRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, "물 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.waterRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun getPeeData(date : String, searchText : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    peeDataList.clear()
                    peeMap.clear()
                    peeSortedMap.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogPeeModel::class.java)
                        if(item!!.peeType.contains(searchText)) {
                            if(date == "전체") {
                                var nowDateSp = item!!.date.split(".")
                                var month = nowDateSp[1]
                                var day = nowDateSp[2]

                                if(month.length == 1)
                                    month = "0$month"
                                if(day.length == 1)
                                    day = "0$day"

                                var nowDate = nowDateSp[0] + month + day

                                peeMap[item!!] = nowDate.toLong()
                            } else {
                                var nowDateSp = item!!.date.split(".")
                                var month = nowDateSp[1]
                                var day = nowDateSp[2]

                                if(month.length == 1)
                                    month = "0$month"
                                if(day.length == 1)
                                    day = "0$day"

                                // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                    var nowDate = nowDateSp[0] + month + day

                                    peeMap[item!!] = nowDate.toLong()
                                }
                            }
                        }
                    }

                    peeSortedMap = peeSortMapByKey(peeMap)
                    for((key, value) in peeSortedMap.entries) {
                        peeDataList.add(key)
                    }

                    peeRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, "소변 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.peeRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun getDungData(date : String, searchText : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    dungDataList.clear()
                    dungMap.clear()
                    dungSortedMap.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogDungModel::class.java)
                        if(item!!.dungType.contains(searchText)) {
                            if(date == "전체") {
                                var nowDateSp = item!!.date.split(".")
                                var month = nowDateSp[1]
                                var day = nowDateSp[2]

                                if(month.length == 1)
                                    month = "0$month"
                                if(day.length == 1)
                                    day = "0$day"

                                var nowDate = nowDateSp[0] + month + day

                                dungMap[item!!] = nowDate.toLong()
                            } else {
                                var nowDateSp = item!!.date.split(".")
                                var month = nowDateSp[1]
                                var day = nowDateSp[2]

                                if(month.length == 1)
                                    month = "0$month"
                                if(day.length == 1)
                                    day = "0$day"

                                // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                    var nowDate = nowDateSp[0] + month + day

                                    dungMap[item!!] = nowDate.toLong()
                                }
                            }
                        }
                    }

                    dungSortedMap = dungSortMapByKey(dungMap)
                    for((key, value) in dungSortedMap.entries) {
                        dungDataList.add(key)
                    }

                    dungRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, "대변 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dungRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun getVomitData(date : String, searchText : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    vomitDataList.clear()
                    vomitMap.clear()
                    vomitSortedMap.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogVomitModel::class.java)
                        if(item!!.vomitType.contains(searchText)) {
                            if(date == "전체") {
                                var nowDateSp = item!!.date.split(".")
                                var month = nowDateSp[1]
                                var day = nowDateSp[2]

                                if(month.length == 1)
                                    month = "0$month"
                                if(day.length == 1)
                                    day = "0$day"

                                var nowDate = nowDateSp[0] + month + day

                                vomitMap[item!!] = nowDate.toLong()
                            } else {
                                var nowDateSp = item!!.date.split(".")
                                var month = nowDateSp[1]
                                var day = nowDateSp[2]

                                if(month.length == 1)
                                    month = "0$month"
                                if(day.length == 1)
                                    day = "0$day"

                                // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                    var nowDate = nowDateSp[0] + month + day

                                    vomitMap[item!!] = nowDate.toLong()
                                }
                            }
                        }
                    }

                    vomitSortedMap = vomitSortMapByKey(vomitMap)
                    for((key, value) in vomitSortedMap.entries) {
                        vomitDataList.add(key)
                    }

                    vomitRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, "구토 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.vomitRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun getHeartData(date : String, range : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    heartDataList.clear()
                    heartMap.clear()
                    heartSortedMap.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogHeartModel::class.java)
                        if(date == "전체") {
                            if(range == "전체") {
                                var nowDateSp = item!!.date.split(".")
                                var month = nowDateSp[1]
                                var day = nowDateSp[2]

                                if (month.length == 1)
                                    month = "0$month"
                                if (day.length == 1)
                                    day = "0$day"

                                var nowDate = nowDateSp[0] + month + day

                                heartMap[item!!] = nowDate.toLong()
                            } else if(range == "범위 내") {
                                if(item!!.heartCount.toInt() <= 30) {
                                    var nowDateSp = item!!.date.split(".")
                                    var month = nowDateSp[1]
                                    var day = nowDateSp[2]

                                    if (month.length == 1)
                                        month = "0$month"
                                    if (day.length == 1)
                                        day = "0$day"

                                    var nowDate = nowDateSp[0] + month + day

                                    heartMap[item!!] = nowDate.toLong()
                                }
                            } else if(range == "범위 외") {
                                if(item!!.heartCount.toInt() > 30) {
                                    var nowDateSp = item!!.date.split(".")
                                    var month = nowDateSp[1]
                                    var day = nowDateSp[2]

                                    if (month.length == 1)
                                        month = "0$month"
                                    if (day.length == 1)
                                        day = "0$day"

                                    var nowDate = nowDateSp[0] + month + day

                                    heartMap[item!!] = nowDate.toLong()
                                }
                            }
                        } else {
                            if (range == "전체") {
                                var nowDateSp = item!!.date.split(".")
                                var month = nowDateSp[1]
                                var day = nowDateSp[2]

                                if (month.length == 1)
                                    month = "0$month"
                                if (day.length == 1)
                                    day = "0$day"

                                // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                    var nowDate = nowDateSp[0] + month + day

                                    heartMap[item!!] = nowDate.toLong()
                                }
                            } else if(range == "범위 내") {
                                if(item!!.heartCount.toInt() <= 30) {
                                    var nowDateSp = item!!.date.split(".")
                                    var month = nowDateSp[1]
                                    var day = nowDateSp[2]

                                    if (month.length == 1)
                                        month = "0$month"
                                    if (day.length == 1)
                                        day = "0$day"

                                    // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                    val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                    val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                    val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                    if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                        var nowDate = nowDateSp[0] + month + day

                                        heartMap[item!!] = nowDate.toLong()
                                    }
                                }
                            } else if(range == "범위 외") {
                                if(item!!.heartCount.toInt() > 30) {
                                    var nowDateSp = item!!.date.split(".")
                                    var month = nowDateSp[1]
                                    var day = nowDateSp[2]

                                    if (month.length == 1)
                                        month = "0$month"
                                    if (day.length == 1)
                                        day = "0$day"

                                    // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                    val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                    val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                    val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                    if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                        var nowDate = nowDateSp[0] + month + day

                                        heartMap[item!!] = nowDate.toLong()
                                    }
                                }
                            }
                        }
                    }

                    heartSortedMap = heartSortMapByKey(heartMap)
                    for((key, value) in heartSortedMap.entries) {
                        heartDataList.add(key)
                    }

                    heartRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, "호흡수 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.heartRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun getMedicineData(date : String, searchText : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    medicineDataList.clear()
                    medicineMap.clear()
                    medicineSortedMap.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogMedicineModel::class.java)
                        if(item!!.medicineName.contains(searchText)) {
                            if(date == "전체") {
                                var nowDateSp = item!!.date.split(".")
                                var month = nowDateSp[1]
                                var day = nowDateSp[2]

                                if(month.length == 1)
                                    month = "0$month"
                                if(day.length == 1)
                                    day = "0$day"

                                var nowDate = nowDateSp[0] + month + day

                                medicineMap[item!!] = nowDate.toLong()
                            } else {
                                var nowDateSp = item!!.date.split(".")
                                var month = nowDateSp[1]
                                var day = nowDateSp[2]

                                if(month.length == 1)
                                    month = "0$month"
                                if(day.length == 1)
                                    day = "0$day"

                                // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                    var nowDate = nowDateSp[0] + month + day

                                    medicineMap[item!!] = nowDate.toLong()
                                }
                            }
                        }
                    }

                    medicineSortedMap = medicineSortMapByKey(medicineMap)
                    for((key, value) in medicineSortedMap.entries) {
                        medicineDataList.add(key)
                    }

                    medicineRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, "투약 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.medicineRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun getCheckUpInputData(date : String, checkUpPart : String, range : String, searchText : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    checkUpInputDataList.clear()
                    checkUpInputMap.clear()
                    checkUpInputSortedMap.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogCheckUpInputModel::class.java)
                        if (checkUpPart == "전체") {
                            if (date == "전체") {
                                if(range == "전체") {
                                    var nowDateSp = item!!.date.split(".")
                                    var month = nowDateSp[1]
                                    var day = nowDateSp[2]

                                    if (month.length == 1)
                                        month = "0$month"
                                    if (day.length == 1)
                                        day = "0$day"

                                    var nowDate = nowDateSp[0] + month + day

                                    checkUpInputMap[item!!] = nowDate.toLong()
                                } else if(range == "범위 내") {
                                    if(item!!.result.toFloat() >= item!!.min.toFloat() && item!!.result.toFloat() <= item!!.max.toFloat()) {
                                        var nowDateSp = item!!.date.split(".")
                                        var month = nowDateSp[1]
                                        var day = nowDateSp[2]

                                        if (month.length == 1)
                                            month = "0$month"
                                        if (day.length == 1)
                                            day = "0$day"

                                        var nowDate = nowDateSp[0] + month + day

                                        checkUpInputMap[item!!] = nowDate.toLong()
                                    }
                                } else if(range == "범위 외") {
                                    if(item!!.result.toFloat() < item!!.min.toFloat() || item!!.result.toFloat() > item!!.max.toFloat()) {
                                        var nowDateSp = item!!.date.split(".")
                                        var month = nowDateSp[1]
                                        var day = nowDateSp[2]

                                        if (month.length == 1)
                                            month = "0$month"
                                        if (day.length == 1)
                                            day = "0$day"

                                        var nowDate = nowDateSp[0] + month + day

                                        checkUpInputMap[item!!] = nowDate.toLong()
                                    }
                                }
                            } else {
                                if(range == "전체") {
                                    var nowDateSp = item!!.date.split(".")
                                    var month = nowDateSp[1]
                                    var day = nowDateSp[2]

                                    if (month.length == 1)
                                        month = "0$month"
                                    if (day.length == 1)
                                        day = "0$day"

                                    // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                    val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                    val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                    val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                    if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                        var nowDate = nowDateSp[0] + month + day

                                        checkUpInputMap[item!!] = nowDate.toLong()
                                    }
                                } else if(range == "범위 내") {
                                    if(item!!.result.toFloat() >= item!!.min.toFloat() && item!!.result.toFloat() <= item!!.max.toFloat()) {
                                        var nowDateSp = item!!.date.split(".")
                                        var month = nowDateSp[1]
                                        var day = nowDateSp[2]

                                        if (month.length == 1)
                                            month = "0$month"
                                        if (day.length == 1)
                                            day = "0$day"

                                        // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                        val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                        val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                        val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                        if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                            var nowDate = nowDateSp[0] + month + day

                                            checkUpInputMap[item!!] = nowDate.toLong()
                                        }
                                    }
                                } else if(range == "범위 외") {
                                    if(item!!.result.toInt() < item!!.min.toFloat() || item!!.result.toFloat() > item!!.max.toFloat()) {
                                        var nowDateSp = item!!.date.split(".")
                                        var month = nowDateSp[1]
                                        var day = nowDateSp[2]

                                        if (month.length == 1)
                                            month = "0$month"
                                        if (day.length == 1)
                                            day = "0$day"

                                        // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                        val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                        val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                        val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                        if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                            var nowDate = nowDateSp[0] + month + day

                                            checkUpInputMap[item!!] = nowDate.toLong()
                                        }
                                    }
                                }
                            }
                        } else if(checkUpPart == "항목명"){
                            if(item!!.name.contains(searchText)) {
                                if (date == "전체") {
                                    if(range == "전체") {
                                        var nowDateSp = item!!.date.split(".")
                                        var month = nowDateSp[1]
                                        var day = nowDateSp[2]

                                        if (month.length == 1)
                                            month = "0$month"
                                        if (day.length == 1)
                                            day = "0$day"

                                        var nowDate = nowDateSp[0] + month + day

                                        checkUpInputMap[item!!] = nowDate.toLong()
                                    } else if(range == "범위 내") {
                                        if(item!!.result.toFloat() >= item!!.min.toFloat() && item!!.result.toFloat() <= item!!.max.toFloat()) {
                                            var nowDateSp = item!!.date.split(".")
                                            var month = nowDateSp[1]
                                            var day = nowDateSp[2]

                                            if (month.length == 1)
                                                month = "0$month"
                                            if (day.length == 1)
                                                day = "0$day"

                                            var nowDate = nowDateSp[0] + month + day

                                            checkUpInputMap[item!!] = nowDate.toLong()
                                        }
                                    } else if(range == "범위 외") {
                                        if(item!!.result.toFloat() < item!!.min.toFloat() || item!!.result.toFloat() > item!!.max.toFloat()) {
                                            var nowDateSp = item!!.date.split(".")
                                            var month = nowDateSp[1]
                                            var day = nowDateSp[2]

                                            if (month.length == 1)
                                                month = "0$month"
                                            if (day.length == 1)
                                                day = "0$day"

                                            var nowDate = nowDateSp[0] + month + day

                                            checkUpInputMap[item!!] = nowDate.toLong()
                                        }
                                    }
                                } else {
                                    if(range == "전체") {
                                        var nowDateSp = item!!.date.split(".")
                                        var month = nowDateSp[1]
                                        var day = nowDateSp[2]

                                        if (month.length == 1)
                                            month = "0$month"
                                        if (day.length == 1)
                                            day = "0$day"

                                        // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                        val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                        val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                        val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                        if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                            var nowDate = nowDateSp[0] + month + day

                                            checkUpInputMap[item!!] = nowDate.toLong()
                                        }
                                    } else if(range == "범위 내") {
                                        if(item!!.result.toFloat() >= item!!.min.toFloat() && item!!.result.toFloat() <= item!!.max.toFloat()) {
                                            var nowDateSp = item!!.date.split(".")
                                            var month = nowDateSp[1]
                                            var day = nowDateSp[2]

                                            if (month.length == 1)
                                                month = "0$month"
                                            if (day.length == 1)
                                                day = "0$day"

                                            // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                            val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                            val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                            val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                            if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                var nowDate = nowDateSp[0] + month + day

                                                checkUpInputMap[item!!] = nowDate.toLong()
                                            }
                                        }
                                    } else if(range == "범위 외") {
                                        if(item!!.result.toFloat() < item!!.min.toFloat() || item!!.result.toFloat() > item!!.max.toFloat()) {
                                            var nowDateSp = item!!.date.split(".")
                                            var month = nowDateSp[1]
                                            var day = nowDateSp[2]

                                            if (month.length == 1)
                                                month = "0$month"
                                            if (day.length == 1)
                                                day = "0$day"

                                            // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                            val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                            val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                            val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                            if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                                var nowDate = nowDateSp[0] + month + day

                                                checkUpInputMap[item!!] = nowDate.toLong()
                                            }
                                        }
                                    }
                                }
                            }
                        } else if(item!!.part.contains(searchText)) {
                            if (date == "전체") {
                                if(range == "전체") {
                                    var nowDateSp = item!!.date.split(".")
                                    var month = nowDateSp[1]
                                    var day = nowDateSp[2]

                                    if (month.length == 1)
                                        month = "0$month"
                                    if (day.length == 1)
                                        day = "0$day"

                                    var nowDate = nowDateSp[0] + month + day

                                    checkUpInputMap[item!!] = nowDate.toLong()
                                } else if(range == "범위 내") {
                                    if(item!!.result.toFloat() >= item!!.min.toFloat() && item!!.result.toFloat() <= item!!.max.toFloat()) {
                                        var nowDateSp = item!!.date.split(".")
                                        var month = nowDateSp[1]
                                        var day = nowDateSp[2]

                                        if (month.length == 1)
                                            month = "0$month"
                                        if (day.length == 1)
                                            day = "0$day"

                                        var nowDate = nowDateSp[0] + month + day

                                        checkUpInputMap[item!!] = nowDate.toLong()
                                    }
                                } else if(range == "범위 외") {
                                    if(item!!.result.toFloat() < item!!.min.toFloat() || item!!.result.toFloat() > item!!.max.toFloat()) {
                                        var nowDateSp = item!!.date.split(".")
                                        var month = nowDateSp[1]
                                        var day = nowDateSp[2]

                                        if (month.length == 1)
                                            month = "0$month"
                                        if (day.length == 1)
                                            day = "0$day"

                                        var nowDate = nowDateSp[0] + month + day

                                        checkUpInputMap[item!!] = nowDate.toLong()
                                    }
                                }
                            } else {
                                if(range == "전체") {
                                    var nowDateSp = item!!.date.split(".")
                                    var month = nowDateSp[1]
                                    var day = nowDateSp[2]

                                    if (month.length == 1)
                                        month = "0$month"
                                    if (day.length == 1)
                                        day = "0$day"

                                    // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                    val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                    val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                    val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                    if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                        var nowDate = nowDateSp[0] + month + day

                                        checkUpInputMap[item!!] = nowDate.toLong()
                                    }
                                } else if(range == "범위 내") {
                                    if(item!!.result.toFloat() >= item!!.min.toFloat() && item!!.result.toFloat() <= item!!.max.toFloat()) {
                                        var nowDateSp = item!!.date.split(".")
                                        var month = nowDateSp[1]
                                        var day = nowDateSp[2]

                                        if (month.length == 1)
                                            month = "0$month"
                                        if (day.length == 1)
                                            day = "0$day"

                                        // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                        val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                        val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                        val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                        if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                            var nowDate = nowDateSp[0] + month + day

                                            checkUpInputMap[item!!] = nowDate.toLong()
                                        }
                                    }
                                } else if(range == "범위 외") {
                                    if(item!!.result.toFloat() < item!!.min.toFloat() || item!!.result.toFloat() > item!!.max.toFloat()) {
                                        var nowDateSp = item!!.date.split(".")
                                        var month = nowDateSp[1]
                                        var day = nowDateSp[2]

                                        if (month.length == 1)
                                            month = "0$month"
                                        if (day.length == 1)
                                            day = "0$day"

                                        // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                        val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                        val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                        val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                        if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                            var nowDate = nowDateSp[0] + month + day

                                            checkUpInputMap[item!!] = nowDate.toLong()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    checkUpInputSortedMap = checkUpInputSortMapByKey(checkUpInputMap)
                    for((key, value) in checkUpInputSortedMap.entries) {
                        checkUpInputDataList.add(key)
                    }

                    checkUpInputRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, "수치 검사 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.checkUpInputRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun getCheckUpPictureData(date : String, checkUpCategory : String, searchText : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    checkUpPictureDataList.clear()
                    checkUpPictureMap.clear()
                    checkUpPictureSortedMap.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogCheckUpPictureModel::class.java)
                        if(item!!.content.contains(searchText) || item!!.hospitalName.contains(searchText)) {
                            if (checkUpCategory == "전체") {
                                if (date == "전체") {
                                    var nowDateSp = item!!.date.split(".")
                                    var month = nowDateSp[1]
                                    var day = nowDateSp[2]

                                    if (month.length == 1)
                                        month = "0$month"
                                    if (day.length == 1)
                                        day = "0$day"

                                    var nowDate = nowDateSp[0] + month + day

                                    checkUpPictureMap[item!!] = nowDate.toLong()
                                } else {
                                    var nowDateSp = item!!.date.split(".")
                                    var month = nowDateSp[1]
                                    var day = nowDateSp[2]

                                    if (month.length == 1)
                                        month = "0$month"
                                    if (day.length == 1)
                                        day = "0$day"

                                    // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                    val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                    val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                    val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                    if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                        var nowDate = nowDateSp[0] + month + day

                                        checkUpPictureMap[item!!] = nowDate.toLong()
                                    }
                                }
                            } else {
                                if(item!!.checkUpCategory == checkUpCategory) {
                                    if (date == "전체") {
                                        var nowDateSp = item!!.date.split(".")
                                        var month = nowDateSp[1]
                                        var day = nowDateSp[2]

                                        if (month.length == 1)
                                            month = "0$month"
                                        if (day.length == 1)
                                            day = "0$day"

                                        var nowDate = nowDateSp[0] + month + day

                                        checkUpPictureMap[item!!] = nowDate.toLong()
                                    } else {
                                        var nowDateSp = item!!.date.split(".")
                                        var month = nowDateSp[1]
                                        var day = nowDateSp[2]

                                        if (month.length == 1)
                                            month = "0$month"
                                        if (day.length == 1)
                                            day = "0$day"

                                        // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                        val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                        val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                        val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                        if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                            var nowDate = nowDateSp[0] + month + day

                                            checkUpPictureMap[item!!] = nowDate.toLong()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    checkUpPictureSortedMap = checkUpPictureSortMapByKey(checkUpPictureMap)
                    for((key, value) in checkUpPictureSortedMap.entries) {
                        checkUpPictureDataList.add(key)
                    }

                    checkUpPictureRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, "검사 사진 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.checkUpPictureRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    private fun getMemoData(date : String, searchText : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    memoDataList.clear()
                    memoMap.clear()
                    memoSortedMap.clear()

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogMemoModel::class.java)
                        if(item!!.title.contains(searchText) || item!!.content.contains(searchText)) {
                            if(date == "전체") {
                                var nowDateSp = item!!.date.split(".")
                                var month = nowDateSp[1]
                                var day = nowDateSp[2]

                                if(month.length == 1)
                                    month = "0$month"
                                if(day.length == 1)
                                    day = "0$day"

                                var nowDate = nowDateSp[0] + month + day

                                memoMap[item!!] = nowDate.toLong()
                            } else {
                                var nowDateSp = item!!.date.split(".")
                                var month = nowDateSp[1]
                                var day = nowDateSp[2]

                                if(month.length == 1)
                                    month = "0$month"
                                if(day.length == 1)
                                    day = "0$day"

                                // 날짜 비교를 위해 시작 날짜와 종료 날짜를 date로 설정
                                val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDateArea.text.toString())
                                val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(endDateArea.text.toString())
                                val nowDateFm = nowDateSp[0] + "." + month + "." + day

                                if (isBetweenDates(nowDateFm, startDate, endDate)) {

                                    var nowDate = nowDateSp[0] + month + day

                                    memoMap[item!!] = nowDate.toLong()
                                }
                            }
                        }
                    }

                    memoSortedMap = memoSortMapByKey(memoMap)
                    for((key, value) in memoSortedMap.entries) {
                        memoDataList.add(key)
                    }

                    memoRVAdapter.notifyDataSetChanged() // 데이터 동기화

                } catch (e: Exception) {
                    Log.d(TAG, "메모 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.memoRef.child(myUid).child(dogId).addValueEventListener(postListener)
    }

    fun isBetweenDates(dateString: String, startDate: Date, endDate: Date): Boolean {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        return (date?.after(startDate) == true || date?.equals(startDate) == true) && (date?.before(endDate) || date?.equals(endDate))
    }

    private fun mealSortMapByKey(map: MutableMap<DogMealModel, Long>): LinkedHashMap<DogMealModel, Long> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.value }

        val result = LinkedHashMap<DogMealModel, Long>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }

    private fun snackSortMapByKey(map: MutableMap<DogSnackModel, Long>): LinkedHashMap<DogSnackModel, Long> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.value }

        val result = LinkedHashMap<DogSnackModel, Long>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }

    private fun tonicSortMapByKey(map: MutableMap<DogTonicModel, Long>): LinkedHashMap<DogTonicModel, Long> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.value }

        val result = LinkedHashMap<DogTonicModel, Long>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }

    private fun waterSortMapByKey(map: MutableMap<DogWaterModel, Long>): LinkedHashMap<DogWaterModel, Long> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.value }

        val result = LinkedHashMap<DogWaterModel, Long>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }

    private fun peeSortMapByKey(map: MutableMap<DogPeeModel, Long>): LinkedHashMap<DogPeeModel, Long> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.value }

        val result = LinkedHashMap<DogPeeModel, Long>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }

    private fun dungSortMapByKey(map: MutableMap<DogDungModel, Long>): LinkedHashMap<DogDungModel, Long> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.value }

        val result = LinkedHashMap<DogDungModel, Long>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }

    private fun vomitSortMapByKey(map: MutableMap<DogVomitModel, Long>): LinkedHashMap<DogVomitModel, Long> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.value }

        val result = LinkedHashMap<DogVomitModel, Long>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }

    private fun heartSortMapByKey(map: MutableMap<DogHeartModel, Long>): LinkedHashMap<DogHeartModel, Long> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.value }

        val result = LinkedHashMap<DogHeartModel, Long>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }

    private fun medicineSortMapByKey(map: MutableMap<DogMedicineModel, Long>): LinkedHashMap<DogMedicineModel, Long> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.value }

        val result = LinkedHashMap<DogMedicineModel, Long>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }

    private fun checkUpInputSortMapByKey(map: MutableMap<DogCheckUpInputModel, Long>): LinkedHashMap<DogCheckUpInputModel, Long> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.value }

        val result = LinkedHashMap<DogCheckUpInputModel, Long>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }

    private fun checkUpPictureSortMapByKey(map: MutableMap<DogCheckUpPictureModel, Long>): LinkedHashMap<DogCheckUpPictureModel, Long> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.value }

        val result = LinkedHashMap<DogCheckUpPictureModel, Long>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }

    private fun memoSortMapByKey(map: MutableMap<DogMemoModel, Long>): LinkedHashMap<DogMemoModel, Long> { // 시간순으로 정렬
        val entries = LinkedList(map.entries)

        entries.sortByDescending { it.value }

        val result = LinkedHashMap<DogMemoModel, Long>()
        for(entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }
}