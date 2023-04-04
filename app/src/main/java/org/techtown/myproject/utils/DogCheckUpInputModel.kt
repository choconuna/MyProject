package org.techtown.myproject.utils

class DogCheckUpInputModel (
    val dogCheckUpInputId : String = "", // 반려견 검사 id
    val dogId : String = "", // 반려견 아이디
    val date : String = "", // 검사 날짜
    val name : String = "", // 항목명
    val min : String = "", // 최소 수치
    val max : String = "", // 최대 수치
    val result : String = "" // 수치값
)