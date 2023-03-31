package org.techtown.myproject.my

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import org.techtown.myproject.R

class MyActivity : AppCompatActivity() {

    private val TAG = MyActivity::class.java.simpleName

    lateinit var mDatabaseReference: DatabaseReference
    lateinit var mAuth : FirebaseAuth
    lateinit var uid : String
    lateinit var dogName : String
    lateinit var userName : String
    private lateinit var headerName : TextView
    private lateinit var headerDogName : TextView
    private lateinit var birthDate : String
    private lateinit var dogSex : String
    private lateinit var dogSpecies : String

    lateinit var profileFile : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my)

        /* mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser?.uid.toString()

        mDatabaseReference = FBRef.userRef.child(uid)
        // setProfile()

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }

        val profileEditBtn = findViewById<Button>(R.id.editProfile)
        profileEditBtn.setOnClickListener {
            val intent = Intent(this, ProfileEditActivity::class.java)
            startActivity(intent)
        }

        val setAlarm = findViewById<LinearLayout>(R.id.setAlarm)
        setAlarm.setOnClickListener {
            val intent = Intent(this, AlarmActivity::class.java)
            startActivity(intent)
        } */
    }

    /* private fun setProfile() { // 프로필 설정
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val post = dataSnapshot.getValue(UserInfo::class.java)
                userName = post!!.userName
                headerName = findViewById(R.id.userName)
                headerName.text = userName
                dogName = post!!.dogName
                headerDogName = findViewById(R.id.dogName)
                headerDogName.text = dogName
                birthDate = post!!.dogBirthDate
                dogSex = post!!.dogSex
                dogSpecies = post!!.dogSpecies

                profileFile = post!!.profileFile // 가져올 유저의 profile 사진

                val profileFile =
                    mDatabaseReference.child("profileFile").get().addOnSuccessListener {
                        val storageReference =
                            Firebase.storage.reference.child(it.value.toString()) // 유저의 profile 사진을 DB의 storage로부터 가져옴

                        val imageView = findViewById<ImageView>(R.id.imageView)

                        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Glide.with(applicationContext).load(task.result)
                                    .into(imageView) // 유저의 profile 사진을 게시자 이름의 왼편에 표시함
                            } else {
                                // findViewById<ImageView>(R.id.profileImageArea).isVisible = false
                            }
                        })
                    }

                val sb = StringBuffer() // 입력된 생년월일이 20100409라면 2010.04.09로 변환하여 화면에 출력하기 위해 StringBuffer() 사용
                sb.append(birthDate)
                sb.insert(4, ".")
                sb.insert(7, ".")
                findViewById<TextView>(R.id.birthDate).text = sb

                val age = getDogAge(birthDate)
                Log.d("dogAge", "나이 $age")
                findViewById<TextView>(R.id.dogAge).text = age

                if(dogSex == "수컷") // 수컷, 암컷에 따라 글 색상을 다르게 표시
                    findViewById<TextView>(R.id.dogSex).setTextColor(Color.parseColor("#6495ED"))
                else
                    findViewById<TextView>(R.id.dogSex).setTextColor(Color.parseColor("#FFB6C1"))
                findViewById<TextView>(R.id.dogSex).text = dogSex
                findViewById<TextView>(R.id.dogSpecies).text = dogSpecies
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        mDatabaseReference.addValueEventListener(postListener)
    }

    private fun getDogAge(birthDate : String) : String { // 반려견 나이(몇 살 몇 개월) 구하는 함수
        val formats_year = SimpleDateFormat("yyyy")
        val formats_month = SimpleDateFormat("MM")
        var dogAge : String = ""

        val timeYear: Int = formats_year.format(Calendar.getInstance().time).toInt()
        val birthYear: Int = birthDate.substring(0, 4).toInt() // 생년월일의 연도를 가져옴
        val timeMonth : Int = formats_month.format(Calendar.getInstance().time).toInt()
        val birthMonth : Int = birthDate.substring(4, 6).toInt() // 생년월일의 월을 가져옴

        dogAge = if(birthMonth <= timeMonth) {
            (timeYear - birthYear).toString() + "살 " + (timeMonth - birthMonth).toString() + "개월"
        } else {
            (timeYear - birthYear - 1).toString() + "살 " + (12 + timeMonth - birthMonth).toString() + "개월"
        }

        Log.d("birth", "나이 $dogAge")

        return dogAge
    } */
}