package org.techtown.myproject.utils

class DogMedicinePlanModel (
    val dogMedicinePlanId : String = "", // 반려견 투약 id
    val dogId : String = "", // 반려견 아이디
    val startDate : String = "", // 시작 날짜
    val endDate : String = "", // 종료 날짜
    val time : String = "", // 시각
    val repeat : String = "", // 반복 여부 (하루일 경우 하루, 매일일 경우 매일)
    val medicineName : String = "" // 약 이름
)