package org.techtown.myproject.receipt

import org.techtown.myproject.utils.ReceiptModel

data class Receipt (
    val selectedDate : String, // 전체 날짜
    val day : String, // 며칠
    val date : String // 무슨 요일
)