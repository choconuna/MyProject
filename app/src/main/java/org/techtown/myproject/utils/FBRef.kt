package org.techtown.myproject.utils

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FBRef {

    companion object {

        private val database = Firebase.database

        val userRef = database.getReference("Users") // 가입한 사용자 데이터를 DB에 삽입하는 경로

        val dogRef = database.getReference("dogs") // 가입한 사용자의 반려견 데이터를 DB에 삽입하는 경로

        val communityRef = database.getReference("community") // 게시판 데이터를 DB에 삽입하는 경로

        val commentRef = database.getReference("comment") // 게시판 댓글 데이터를 DB에 삽입하는 경로

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

        val alarmRef = database.getReference("alarm") // 알림 데이터를 DB에 삽입하는 경로
   }

}