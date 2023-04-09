package org.techtown.myproject.utils

class ReceiptModel(
    val userId : String = "", // 사용자 id
    val receiptId : String = "", // 가계부 id
    val date : String = "", // 날짜
    val category : String = "", // 카테고리
    val price : String ="", // 금액
    val content : String = "" // 내용
)