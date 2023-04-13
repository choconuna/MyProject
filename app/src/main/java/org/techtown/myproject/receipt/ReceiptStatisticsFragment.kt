package org.techtown.myproject.receipt

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.prolificinteractive.materialcalendarview.format.DateFormatTitleFormatter
import org.techtown.myproject.R
import org.techtown.myproject.note.*
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class ReceiptStatisticsFragment : Fragment() {

    private val TAG = ReceiptStatisticsFragment::class.java.simpleName

    private lateinit var back : Button
    private lateinit var next : Button
    private lateinit var startDateArea : TextView
    private lateinit var between : TextView
    private lateinit var endDateArea : TextView

    private lateinit var selectedMenu : String

    private var endDate = LocalDate.now() // 현재 날짜
    private lateinit var endYear : String
    private lateinit var endMonth : String
    private lateinit var endDay : String

    private lateinit var startDate : LocalDate
    private lateinit var startYear : String
    private lateinit var startMonth : String
    private lateinit var startDay : String

    private lateinit var myUid : String

    private lateinit var spinner : Spinner

    lateinit var receiptGroup : RadioGroup
    private var receiptCategory : String = "비율"

    lateinit var receiptView : LinearLayout

    lateinit var bundle: Bundle

    private val receiptPieFragment by lazy { ReceiptPieFragment() }
    private val receiptCharFragment by lazy { ReceiptChartFragment() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v : View? = inflater.inflate(R.layout.fragment_receipt_statistics, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        setData(v!!)
        spinner = v!!.findViewById(R.id.spinner)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                selectedMenu = parent.getItemAtPosition(position).toString()
                setDate(v, selectedMenu)
                setView(v!!)
                setCategoryDate(v!!, bundle)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        back.setOnClickListener {
            when(selectedMenu) {
                "주별" -> endDate = endDate.minusWeeks(1) // 기준 날짜를 한주 전으로 설정
                "월별" -> endDate = endDate.minusMonths(1) // 기준 날짜를 한달 전으로 설정
                "연별" -> endDate = endDate.minusYears(1) // 기준 날짜를 1년 전으로 설정
            }
            setDate(v, selectedMenu)
            setView(v!!)
            setCategoryDate(v!!, bundle)
        }

        next.setOnClickListener {
            when(selectedMenu) {
                "주별" -> endDate = endDate.plusWeeks(1) // 기준 날짜를 한주 후로 설정
                "월별" -> endDate = endDate.plusMonths(1) // 기준 날짜를 한달 후로 설정
                "연별" -> endDate = endDate.plusYears(1) // 기준 날짜를 1년 후로 설정
            }
            setDate(v, selectedMenu)
            setView(v!!)
            setCategoryDate(v!!, bundle)
        }

        return v
    }

    private fun setData(v : View) {
        back = v!!.findViewById(R.id.backDate)
        next = v!!.findViewById(R.id.nextDate)
        between = v!!.findViewById(R.id.between)
        startDateArea = v!!.findViewById(R.id.startDateArea)
        endDateArea = v!!.findViewById(R.id.endDateArea)

        spinner = v!!.findViewById(R.id.spinner)

        receiptGroup = v!!.findViewById(R.id.categoryGroup)
    }

    private fun setDate(v : View, selectedMenu : String) {

        when(selectedMenu) {
            "주별" -> {

                week(endDate.toString())

                endDateArea.visibility = VISIBLE

                val endDateSplit = endDateArea.text.toString().split(".")
                endYear = endDateSplit[0]
                endMonth = endDateSplit[1]
                endDay = endDateSplit[2]

                between.visibility = VISIBLE

                startDateArea.visibility = VISIBLE

                val startDateSplit = startDateArea.text.toString().split(".")
                startYear = startDateSplit[0]
                startMonth = startDateSplit[1]
                startDay = startDateSplit[2]

            }
            "월별" -> {
                val format = "yyyy년 MM월"
                val formatt = "yyyy.MM"
                val sdf = DateTimeFormatter.ofPattern(format)
                val sfd = DateTimeFormatter.ofPattern(formatt)
                endDateArea.text = endDate.format(sdf)
                endDateArea.visibility = VISIBLE

                val endDateSplit = endDate.format(sfd).toString().split(".")
                endYear = endDateSplit[0]
                endMonth = endDateSplit[1]

                between.visibility = GONE
                startDateArea.visibility = GONE
            }
            "연별" -> {
                val format = "yyyy년"
                val sdf = DateTimeFormatter.ofPattern(format)
                endDateArea.text = endDate.format(sdf)
                endDateArea.visibility = VISIBLE

                val endDateSplit = endDateArea.text.toString().split("년")
                endYear = endDateSplit[0]

                between.visibility = GONE
                startDateArea.visibility = GONE
            }
        }
    }

    private fun setView(v : View) {

        when(receiptCategory) {
            "비율" -> {
                receiptGroup.check(v.findViewById<RadioButton>(R.id.pie).id)

                when(selectedMenu) {
                    "주별" -> {
                        bundle = Bundle(1)
                        bundle.putString("category", selectedMenu)
                        bundle.putString("startDate", startDateArea.text.toString())
                        bundle.putString("endDate", endDateArea.text.toString())
                    }
                    "월별" -> {
                        bundle = Bundle(1)
                        bundle.putString("category", selectedMenu)
                        bundle.putString("endDate", endDateArea.text.toString())
                    }
                    "연별" -> {
                        bundle = Bundle(1)
                        bundle.putString("category", selectedMenu)
                        bundle.putString("endDate", endDateArea.text.toString())
                    }
                }

                changeFragment(receiptPieFragment)
            }
            "차트" -> {
                receiptGroup.check(v.findViewById<RadioButton>(R.id.chart).id)

                when(selectedMenu) {
                    "주별" -> {
                        bundle = Bundle(1)
                        bundle.putString("category", selectedMenu)
                        bundle.putString("startDate", startDateArea.text.toString())
                        bundle.putString("endDate", endDateArea.text.toString())
                    }
                    "월별" -> {
                        bundle = Bundle(1)
                        bundle.putString("category", selectedMenu)
                        bundle.putString("endDate", endDateArea.text.toString())
                    }
                    "연별" -> {
                        bundle = Bundle(1)
                        bundle.putString("category", selectedMenu)
                        bundle.putString("endDate", endDateArea.text.toString())
                    }
                }

                changeFragment(receiptCharFragment)
            }
            else -> {
                receiptCategory = "비율"
                receiptGroup.check(v.findViewById<RadioButton>(R.id.pie).id)

                when(selectedMenu) {
                    "주별" -> {
                        bundle = Bundle(1)
                        bundle.putString("category", selectedMenu)
                        bundle.putString("startDate", startDateArea.text.toString())
                        bundle.putString("endDate", endDateArea.text.toString())
                    }
                    "월별" -> {
                        bundle = Bundle(1)
                        bundle.putString("category", selectedMenu)
                        bundle.putString("endDate", endDateArea.text.toString())
                    }
                    "연별" -> {
                        bundle = Bundle(1)
                        bundle.putString("category", selectedMenu)
                        bundle.putString("endDate", endDateArea.text.toString())
                    }
                }

                changeFragment(receiptPieFragment)
            }
        }

        receiptGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.pie -> {
                    receiptCategory = "비율"

                    when(selectedMenu) {
                        "주별" -> {
                            bundle = Bundle(1)
                            bundle.putString("category", selectedMenu)
                            bundle.putString("startDate", startDateArea.text.toString())
                            bundle.putString("endDate", endDateArea.text.toString())
                        }
                        "월별" -> {
                            bundle = Bundle(1)
                            bundle.putString("category", selectedMenu)
                            bundle.putString("endDate", endDateArea.text.toString())
                        }
                        "연별" -> {
                            bundle = Bundle(1)
                            bundle.putString("category", selectedMenu)
                            bundle.putString("endDate", endDateArea.text.toString())
                        }
                    }

                    changeFragment(receiptPieFragment)
                    receiptGroup.check(v.findViewById<RadioButton>(R.id.pie).id)
                }
                R.id.chart -> {
                    receiptCategory = "차트"

                    when(selectedMenu) {
                        "주별" -> {
                            bundle = Bundle(1)
                            bundle.putString("category", selectedMenu)
                            bundle.putString("startDate", startDateArea.text.toString())
                            bundle.putString("endDate", endDateArea.text.toString())
                        }
                        "월별" -> {
                            bundle = Bundle(1)
                            bundle.putString("category", selectedMenu)
                            bundle.putString("endDate", endDateArea.text.toString())
                        }
                        "연별" -> {
                            bundle = Bundle(1)
                            bundle.putString("category", selectedMenu)
                            bundle.putString("endDate", endDateArea.text.toString())
                        }
                    }

                    changeFragment(receiptCharFragment)
                    receiptGroup.check(v.findViewById<RadioButton>(R.id.chart).id)
                }
                else -> {
                    receiptCategory = "비율"

                    when(selectedMenu) {
                        "주별" -> {
                            bundle = Bundle(1)
                            bundle.putString("category", selectedMenu)
                            bundle.putString("startDate", startDateArea.text.toString())
                            bundle.putString("endDate", endDateArea.text.toString())
                        }
                        "월별" -> {
                            bundle = Bundle(1)
                            bundle.putString("category", selectedMenu)
                            bundle.putString("endDate", endDateArea.text.toString())
                        }
                        "연별" -> {
                            bundle = Bundle(1)
                            bundle.putString("category", selectedMenu)
                            bundle.putString("endDate", endDateArea.text.toString())
                        }
                    }

                    receiptGroup.check(v.findViewById<RadioButton>(R.id.pie).id)
                    changeFragment(receiptPieFragment)
                }
            }
        }
    }

    private fun changeFragment(fragment: Fragment) {
        fragment.arguments = bundle
        parentFragmentManager.beginTransaction().replace(R.id.receiptView, fragment).commit()
    }

    private fun setCategoryDate(v : View, bundle : Bundle) {
        when(receiptCategory) {
            "비율" -> {
                receiptGroup.check(v.findViewById<RadioButton>(R.id.pie).id)
                val receiptPieFragment = ReceiptPieFragment()
                receiptPieFragment.arguments = bundle
                changeFragment(receiptPieFragment)
            }
            "차트" -> {
                receiptGroup.check(v.findViewById<RadioButton>(R.id.chart).id)
                val receiptChartFragment = ReceiptChartFragment()
                receiptChartFragment.arguments = bundle
                changeFragment(receiptChartFragment)
            }
            else -> {
                receiptCategory = "비율"
                receiptGroup.check(v.findViewById<RadioButton>(R.id.pie).id)
                val receiptPieFragment = ReceiptPieFragment()
                receiptPieFragment.arguments = bundle
                changeFragment(receiptPieFragment)
            }
        }
    }

    fun week(eventDate: String) { // 선택된 날짜가 포함된 1주일간의 날짜 범위를 구하는 함수
        val dateArray = eventDate.split("-").toTypedArray()

        val cal = Calendar.getInstance()
        cal[dateArray[0].toInt(), dateArray[1].toInt() - 1] = dateArray[2].toInt()

        cal.firstDayOfWeek = Calendar.SUNDAY // 일주일의 첫날을 일요일로 지정

        val dayOfWeek = cal[Calendar.DAY_OF_WEEK] - cal.firstDayOfWeek // 시작일과 특정날짜의 차이를 구함

        cal.add(Calendar.DAY_OF_MONTH, -dayOfWeek) // 해당 주차의 첫째날 지정

        val sf = SimpleDateFormat("yyyy.MM.dd")

        val startDt = sf.format(cal.time) // 해당 주차의 첫째 날짜

        cal.add(Calendar.DAY_OF_MONTH, 6)  // 해당 주차의 마지막 날짜 지정

        val endDt = sf.format(cal.time) // 해당 주차의 마지막 날짜

        startDateArea.text = startDt.toString()
        endDateArea.text = endDt.toString()

        Log.d("getWeekDate", "특정 날짜 = [$eventDate] >> 시작 날짜 = [$startDt], 종료 날짜 = [$endDt]")
    }
}