package org.techtown.myproject.utils

class MessageModel (
    val chatConnectionId : String = "", // 채팅 id
    val messageId : String = "", // 메시지 id
    val sendUid : String = "", // 보낸 사람 uid
    val content : String = "", // 보낸 메시지 내용
    val sendDate : String = "", // 보낸 날짜
    val shown : String = "" // 메시지 확인 여부
)