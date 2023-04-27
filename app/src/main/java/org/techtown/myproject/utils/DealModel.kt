package org.techtown.myproject.utils

class DealModel (
    val dealId : String = "", // deal id
    val sellerId : String = "", // 판매자 uid
    val location : String = "", // 판매 장소
    val category : String = "", // 판매 카테고리
    val price : String = "", // 판매 가격
    val title : String = "", // 제목
    val content : String = "", // 내용
    val imgCnt : String = "", // 첨부한 이미지 수
    val method : String = "", // 판매 방법 (직거래 / 택배 거래)
    val state : String = "", // 판매 상태 (판매 중, 거래 중, 거래 완료)
    val date : String = "" // 판매 날짜
)