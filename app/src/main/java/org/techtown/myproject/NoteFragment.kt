package org.techtown.myproject

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class NoteFragment: Fragment() {

    lateinit var sharedPreferences: SharedPreferences
    private val prefUserEmail = "userEmail"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v : View? = inflater.inflate(R.layout.fragment_note, container, false)

        sharedPreferences = this.requireActivity().getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        var userEmail : String = sharedPreferences.getString(prefUserEmail, "").toString()
        var textView : TextView = v!!.findViewById(R.id.memo)
        textView.text = userEmail

        return v
    }
}