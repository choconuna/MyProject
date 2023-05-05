package org.techtown.myproject.utils

class ReceiptModel(
    val userId : String = "", // 사용자 id
    val receiptId : String = "", // 가계부 id
    val startPayRoleId : String = "", // 할부라면 할부가 시작되는 가계의 id를 저장
    val date : String = "", // 날짜
    val category : String = "", // 카테고리
    val price : String ="", // 금액
    val payMethod : String = "", // 결제 방식 (현금 or 카드)
    val payMonthRole : String = "", // 카드 결제 시 할부인지 일시불인지
    val payMonth : String = "", // 할부 개월수
    val nowPayMonth : String = "", // 할부 몇 개월째인지, 3개월 할부인데 1개월째라면 1, 3개월째라면 3
    val place : String = "", // 장소
    val content : String = "", // 내용
    val imgCnt : String = "" // 사진 개수
)