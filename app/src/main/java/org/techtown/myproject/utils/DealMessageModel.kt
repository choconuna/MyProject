package org.techtown.myproject.utils

class DealMessageModel (
    val dealId : String = "", // deal id
    val chatConnectionId : String = "", // 채팅 id
    val messageId : String = "", // 메시지 id
    val sendUid : String = "", // 보낸 사람 uid
    val type : String = "", // 메시지 타입 (사진인지 글인지)
    val picNum : String = "", // 사진인 경우 사진이 몇 장인지
    val content : String = "", // 보낸 메시지 내용
    val sendDate : String = "", // 보낸 날짜
    val shown : String = "" // 메시지 확인 여부
)