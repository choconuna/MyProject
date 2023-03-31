package org.techtown.myproject.receipt

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.techtown.myproject.R

class ReceiptFragment : Fragment() {


    private val TAG: String = ReceiptFragment::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v : View? = inflater.inflate(R.layout.fragment_receipt, container, false)

        return v
    }
}