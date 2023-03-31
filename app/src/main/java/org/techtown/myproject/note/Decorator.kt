package org.techtown.myproject.note

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.util.*

class Decorator : DayViewDecorator {

    private lateinit var date : CalendarDay

    constructor() {
        date = CalendarDay.today()
    }

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return day!! == date
    }

    override fun decorate(view: DayViewFacade?) {
        view!!.addSpan(ForegroundColorSpan(Color.parseColor("#c08457")))
    }

    fun setDate(date : Date) {
        this.date = CalendarDay.from(date)
    }
}