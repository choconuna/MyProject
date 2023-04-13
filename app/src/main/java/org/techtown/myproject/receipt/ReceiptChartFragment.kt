package org.techtown.myproject.receipt

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.techtown.myproject.R

class ReceiptChartFragment : Fragment() {

    private lateinit var category : TextView
    private lateinit var date : TextView

    private lateinit var nowCategory : String
    private lateinit var nowDate : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v : View? = inflater.inflate(R.layout.fragment_receipt_chart, container, false)

        nowCategory = arguments?.getString("category").toString() // 선택된 날짜를 받아옴

        category = v!!.findViewById(R.id.category)
        date = v!!.findViewById(R.id.date)

        category.text = nowCategory
        if(nowCategory == "주별") {
            date.text = arguments?.getString("startDate") + " ~ " + arguments?.getString("endDate")
        } else {
            date.text = arguments?.getString("endDate")
        }

        return v
    }
}