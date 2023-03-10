package org.techtown.myproject

/**
 * 사용자 계정 정보 모델 클래스
 */

class UserAccount {
    private lateinit var idToken : String   // Firebase Uid (고유 토큰 정보)
    private lateinit var emailId : String   // 이메일 아이디
    private lateinit var password : String  // 비밀번호

    class UserAccount() { }

    fun getIdToken() : String {
        return idToken
    }

    fun setIdToken(idToken: String) {
        this.idToken = idToken
    }

    fun getEmailId() : String {
        return emailId
    }

    fun setEmailId(emailId: String) {
        this.emailId = emailId
    }

    fun getPassword() : String {
        return password
    }

    fun setPassword(password : String) {
        this.password = password
    }
}