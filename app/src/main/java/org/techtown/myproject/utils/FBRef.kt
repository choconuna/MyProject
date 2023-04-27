package org.techtown.myproject.utils

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FBRef {

    companion object {

        private val database = Firebase.database

        val userRef = database.getReference("Users") // 가입한 사용자 데이터를 DB에 삽입하는 경로

        val dogRef = database.getReference("dogs") // 가입한 사용자의 반려견 데이터를 DB에 삽입하는 경로

        val userMainDogRef = database.getReference("userMainDog") // 사용자의 대표 강아지의 id를 DB에 삽입하는 경로

        val communityRef = database.getReference("community") // 게시판 데이터를 DB에 삽입하는 경로

        val commentRef = database.getReference("comment") // 게시판 댓글 데이터를 DB에 삽입하는 경로

        val dealRef = database.getReference("deal") // 거래 데이터를 DB에 삽입하는 경로

        val reCommentRef = database.getReference("reComment") // 게시판 대댓글 데이터를 DB에 삽입하는 경로

        val mealRef = database.getReference("meal") // 반려견의 사료 데이터를 DB에 삽입하는 경로

        val snackRef = database.getReference("snack") // 반려견의 간식 데이터를 DB에 삽입하는 경로

        val tonicRef = database.getReference("tonic") // 반려견의 영양제 데이터를 DB에 삽입하는 경로

        val waterRef = database.getReference("water") // 반려견의 물 데이터를 DB에 삽입하는 경로

        val peeRef = database.getReference("pee") // 반려견의 소변 데이터를 DB에 삽입하는 경로

        val dungRef = database.getReference("dung") // 반려견의 대변 데이터를 DB에 삽입하는 경로

        val vomitRef = database.getReference("vomit") // 반려견의 구토 데이터를 DB에 삽입하는 경로

        val heartRef = database.getReference("heart") // 반려견의 호흡수 데이터를 DB에 삽입하는 경로

        val medicinePlanRef = database.getReference("medicinePlan") // 반려견의 투약 일정 데이터를 DB에 삽입하는 경로

        val medicineRef = database.getReference("medicine") // 반려견의 투약 기록 데이터를 DB에 삽입하는 경로

        val memoRef = database.getReference("memo") // 반려견의 메모 데이터를 DB에 삽입하는 경로

        val checkUpInputRef = database.getReference("checkUpInput") // 반려견의 검사 데이터를 DB에 삽입하는 경로

        val checkUpPictureRef = database.getReference("checkUpPicture") // 반려견의 검사 사진 데이터를 DB에 삽입하는 경로

        val receiptRef = database.getReference("receipt") // 가계부 데이터를 DB에 삽입하는 경로

        val walkRef = database.getReference("walk") // 산책 데이터를 DB에 삽입하는 경로

        val walkDogRef = database.getReference("walkDog") // 산책하는 반려견 데이터를 DB에 삽입하는 경로

        val chatConnectionRef = database.getReference("chatConnection") // 채팅 커넥션 데이터를 DB에 삽입하는 경로

        val messageRef = database.getReference("message") // 메시지 데이터를 DB에 삽입하는 경로

        val dealChatConnectionRef = database.getReference("dealChatConnection") // 거래 채팅 커넥션 데이터를 DB에 삽입하는 경로

        val dealMessageRef = database.getReference("dealMessage") // 거래 메시지 데이터를 DB에 삽입하는 경로

        val tokenRef = database.getReference("FCMToken") // FCM 토큰 데이터를 DB에 삽입하는 경로

        val alarmRef = database.getReference("alarm") // 알림 데이터를 DB에 삽입하는 경로
   }

}