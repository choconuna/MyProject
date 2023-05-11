package org.techtown.myproject

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.View.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.UserInfo
import org.techtown.myproject.utils.UserMainDogModel
import java.io.ByteArrayOutputStream


class JoinActivity : AppCompatActivity() {
    private val TAG = JoinActivity::class.java.simpleName

    lateinit var mFirebaseAuth: FirebaseAuth // 파이어베이스 인증
    lateinit var userDB : DatabaseReference

    private lateinit var sharedPreferences: SharedPreferences // 대표 반려견 id를 저장하기 위함
    lateinit var editor : SharedPreferences.Editor

    lateinit var userNameArea : TextView
    lateinit var userName : String

    lateinit var nickNameArea : EditText
    lateinit var nickName : String
    var nickNameCheckClicked : Boolean = false
    var nickNameChecked : Boolean = false

    lateinit var emailArea : EditText
    lateinit var emailAlert : TextView

    lateinit var pwAlert : TextView
    lateinit var pwArea : EditText
    lateinit var pwCheckArea : EditText

    lateinit var email : String
    lateinit var pw : String

    lateinit var dogDB : DatabaseReference

    lateinit var dogNameArea : EditText
    lateinit var dogName : String

    lateinit var dogBirthArea : EditText
    lateinit var dogBirthDate : String

    lateinit var dogSex : String

    lateinit var spinner : Spinner
    lateinit var dogSpecies : String

    lateinit var dogWeightArea : EditText
    lateinit var dogWeight : String

    lateinit var neutralization : String

    lateinit var joinBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }

        userDB = FBRef.userRef
        mFirebaseAuth = FirebaseAuth.getInstance()

        userNameArea = findViewById(R.id.userNameArea)
        nickNameArea = findViewById(R.id.nickNameArea)
        val nickNameCheckBtn = findViewById<Button>(R.id.nickNameCheckBtn)
        nickNameCheckBtn.setOnClickListener {
            nickNameCheckClicked = true
            nickName = nickNameArea.text.toString().trim()
            checkNickName(nickName)
        }

        emailAlert = findViewById(R.id.emailAlert)
        emailArea = findViewById(R.id.emailArea)
        emailAlert.visibility = GONE
        emailFormCheck()

        pwAlert = findViewById(R.id.pwAlert)
        pwArea = findViewById(R.id.pwArea)
        pwCheckArea = findViewById(R.id.pwCheckArea)
        pwAlert.visibility = GONE
        pwCheck()

        dogNameArea = findViewById(R.id.dogNameArea)

        dogBirthArea = findViewById(R.id.dogBirth)
        dogWeightArea = findViewById(R.id.dogWeightArea)

        dogSex = "수컷"
        val sexGroup = findViewById<RadioGroup>(R.id.sexGroup)
        sexGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.male -> dogSex = "수컷"
                R.id.female -> dogSex = "암컷"
            }
        }

        spinner = findViewById(R.id.spinner)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                dogSpecies = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        neutralization = "YES"
        val neutralizationGroup = findViewById<RadioGroup>(R.id.neutralization)
        neutralizationGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.yes -> neutralization = "YES"
                R.id.no -> neutralization = "NO"
            }
        }


        joinBtn = findViewById(R.id.joinBtn)
        joinBtn.setOnClickListener {
            userName = userNameArea.text.toString().trim()
            nickName = nickNameArea.text.toString().trim()
            email = emailArea.text.toString().trim()
            pw = pwArea.text.toString().trim()

            dogName = dogNameArea.text.toString().trim()
            dogBirthDate = dogBirthArea.text.toString().trim()
            dogWeight = dogWeightArea.text.toString().trim()

            createUser(userName, nickName, email, pw, dogName, dogBirthDate, dogWeight)
        }
    }

    private fun checkNickName(nickname : String) { // 닉네임이 중복되었는지 확인
        FBRef.userRef.orderByChild("nickName").equalTo(nickname).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                nickNameChecked = if (!dataSnapshot.exists()) {
                    Toast.makeText(this@JoinActivity, "닉네임이 중복되지 않습니다!", Toast.LENGTH_SHORT).show()
                    true
                } else {
                    Toast.makeText(this@JoinActivity, "닉네임이 중복됩니다!", Toast.LENGTH_SHORT).show()
                    false
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@JoinActivity, databaseError.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun emailFormCheck() { // 이메일 형식에 맞게 입력했는지 확인
        emailArea.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                emailAlert.visibility = VISIBLE
                if (!Patterns.EMAIL_ADDRESS.matcher(s.toString().trim()).matches()) {
                    if(emailArea.text.toString() == "") {
                        emailAlert.visibility = GONE
                        joinBtn.isEnabled=false // 가입하기 버튼 비활성화
                    } else {
                        emailAlert.text = "이메일 형식으로 입력해 주세요." // 경고 메세지
                        joinBtn.isEnabled=false // 가입하기 버튼 비활성화
                    }
                } else {
                    emailAlert.visibility = GONE //에러 메세지 제거
                }
            }
        })
    }

    private fun pwCheck() { // 비밀번호가 동일하게 입력된 것인지 확인
        pwCheckArea.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) { //입력이 끝났을 때 비밀번호 일치하는지 확인
                pwAlert.visibility = VISIBLE
                if(pwArea.text.toString().trim().equals(pwCheckArea.text.toString().trim()) && !pwArea.text.toString().equals("")){
                    pwAlert.visibility = GONE
                    pw = pwArea.text.toString()
                    joinBtn.isEnabled=true // 가입하기 버튼 활성화
                }
                else{
                    if(pwArea.text.toString().trim() != "")
                        pwAlert.text = "비밀번호가 일치하지 않습니다."
                    else if(pwArea.text.toString().trim() == "")
                        pwAlert.visibility = GONE
                    joinBtn.isEnabled=false // 가입하기 버튼 비활성화
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { //입력하기 전
                pwAlert.text = ""
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { //텍스트 변화가 있을 시
                if(pwArea.text.toString().trim() == pwCheckArea.text.toString().trim()){
                    pwAlert.visibility = GONE
                    joinBtn.isEnabled=true // 가입하기 버튼 활성화
                } else if(pwCheckArea.text.toString().trim() == "")
                    pwAlert.visibility = GONE
                else if(pwArea.text.toString().trim() != pwCheckArea.text.toString().trim()){
                    pwAlert.text = "비밀번호가 일치하지 않습니다."
                    joinBtn.isEnabled=false // 가입하기 버튼 비활성화
                }
            }
        })
    }

    private fun createUser(userName : String, nickName: String, email : String, pw : String, dogName : String, dogBirthDate : String, dogWeight : String) { // 사용자 가입 동작 구현
        if(userName == "") {
            Toast.makeText(this, "이름을 입력하세요!", Toast.LENGTH_LONG).show()
        } else if(nickName == "" ) {
            Toast.makeText(this, "닉네임을 입력하세요!", Toast.LENGTH_LONG).show()
        } else if(!nickNameCheckClicked) {
            Toast.makeText(this, "닉네임이 중복되는지 확인하세요!", Toast.LENGTH_LONG).show()
            nickNameArea.setSelection(0)
        } else if(!nickNameChecked) {
            Toast.makeText(this, "닉네임이 중복됩니다. 다시 입력하세요!", Toast.LENGTH_LONG).show()
            nickNameArea.setText("")
            nickNameArea.setSelection(0)
            nickNameCheckClicked = false
        } else if(email == ""){
            Toast.makeText(this, "이메일을 입력하세요!", Toast.LENGTH_LONG).show()
            emailArea.setSelection(0)
        }  else if(pw == "") {
            Toast.makeText(this, "비밀번호를 입력하세요!", Toast.LENGTH_LONG).show()
            pwArea.setSelection(0)
        } else if(dogName == "") {
            Toast.makeText(this, "반려견 이름을 입력하세요!", Toast.LENGTH_LONG).show()
            dogNameArea.setSelection(0)
        } else if(dogBirthDate == "") {
            Toast.makeText(this, "반려견 생일을 입력하세요!", Toast.LENGTH_LONG).show()
            dogBirthArea.setSelection(0)
        } else if(dogWeight == "") {
            Toast.makeText(this, "반려견 몸무게를 입력하세요!", Toast.LENGTH_LONG).show()
            dogWeightArea.setSelection(0)
        } else if(userName != "" && userName != "" && nickName != "" && email != "" && pw != "" && dogName != "" && dogBirthDate != "" && dogWeight != "") {
            mFirebaseAuth.createUserWithEmailAndPassword(email, pw).addOnCompleteListener(this)
            { task ->
                if (task.isSuccessful) {
                    var uid : String = task.result.user!!.uid
                    saveDB(uid, "basic_user.png", userName, nickName, email, pw, dogName, dogBirthDate, dogWeight) // DB에 가입한 사용자 정보 저장
                    Toast.makeText(this@JoinActivity, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                } else if (!task.isSuccessful) {
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthWeakPasswordException) {
                        Toast.makeText(this@JoinActivity, "비밀번호가 간단합니다.", Toast.LENGTH_LONG).show()
                        joinBtn.isEnabled=false // 가입하기 버튼 비활성화
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this@JoinActivity, "이메일 형식에 맞지 않습니다.", Toast.LENGTH_LONG)
                            .show()
                        joinBtn.isEnabled=false // 가입하기 버튼 비활성화
                    } catch (e: FirebaseAuthUserCollisionException) {
                        Toast.makeText(this@JoinActivity, "이미 존재하는 이메일입니다.", Toast.LENGTH_LONG)
                            .show()
                        joinBtn.isEnabled=false // 가입하기 버튼 비활성화
                    } catch (e: Exception) {
                        Toast.makeText(this@JoinActivity, "정확히 입력했는지 확인하세요.", Toast.LENGTH_LONG)
                            .show()
                        joinBtn.isEnabled=false // 가입하기 버튼 비활성화
                    }
                }
            }
        }
    }

    private fun saveDB(uid : String, profileImage : String, userName : String, nickName : String, email : String, pw : String, dogName : String, dogBirthDate : String, dogWeight : String) { // DB에 가입한 사용자에 대한 정보 저장
        val userKey = FBRef.userRef.push().key.toString() // 사용자의 키 값을 받아옴
        val dogKey = FBRef.dogRef.push().key.toString() // 반려견의 키 값을 받아옴

        FBRef.userRef.child(uid).setValue(UserInfo(uid, profileImage, userName, nickName, email, pw)) // 사용자 정보 데이터베이스에 저장 // "Users" 안의 사용자 uid 아래에 userInfo 데이터를 삽입
        FBRef.dogRef.child(uid).child(dogKey).setValue(DogModel(dogKey, "basic_user.png", dogName, dogBirthDate, dogSex, dogSpecies, dogWeight, neutralization))
        FBRef.userMainDogRef.child(uid).setValue(UserMainDogModel(dogKey))
        sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE) // sharedPreferences 이름의 기본모드 설정
        val editor = sharedPreferences.edit() //sharedPreferences를 제어할 editor를 선언
        editor.putString(uid, dogKey) // key,value 형식으로 저장
        editor.commit() //최종 커밋. 커밋을 해야 저장됨.
    }
}

