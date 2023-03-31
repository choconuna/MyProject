package org.techtown.myproject.note

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.techtown.myproject.R

class MedicineFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var v : View? = inflater.inflate(R.layout.fragment_medicine, container, false)

        v!!.findViewById<TextView>(R.id.date).text = arguments?.getString("nowDate")

        return v
    }
}