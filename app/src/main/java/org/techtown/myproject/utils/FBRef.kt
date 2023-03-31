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

        val alarmRef = database.getReference("alarm") // 알림 데이터를 DB에 삽입하는 경로
   }

}