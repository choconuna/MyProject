package org.techtown.myproject.utils

class DogSnackModel (
    val dogSnackId : String = "", // 간식 id
    val dogId : String = "", // 반려견 id
    val date : String = "", // 날짜
    val snackImageFile : String = "", // 간식 이미지
    val timeSlot : String = "", // 시간대
    val snackType : String = "", // 간식 타입
    val snackName : String = "", // 간식 이름
    val snackWeight : String = "", // 간식 양
    val snackUnit : String = "" // 간식 양 단위
)