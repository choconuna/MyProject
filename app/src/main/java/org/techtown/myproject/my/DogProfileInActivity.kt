package org.techtown.myproject.my

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef

class DogProfileInActivity : AppCompatActivity() {

    private val TAG = DogProfileInActivity::class.java.simpleName

    lateinit var userId : String
    private lateinit var key : String

    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor : SharedPreferences.Editor

    private lateinit var dogProfileFile : String

    lateinit var dogName : String

    lateinit var dogBirthDate : String

    lateinit var dogSex : String

    lateinit var dogSpecies : String

    lateinit var dogWeight : String

    lateinit var neutralization : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_profile_in)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        key = intent.getStringExtra("key").toString() // 사용자의 반려견의 id

        val rmBtn = findViewById<ImageView>(R.id.remove)
        rmBtn.setOnClickListener {
            Log.d(TAG, "remove Button Clicked")
            sharedPreferences = this.getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
            if(sharedPreferences.getString(userId, "").toString() == key) { // 대표 반려견일 경우 삭제 불가능하도록 구현해 놓았음
                Toast.makeText(this, "대표 반려견이므로 삭제할 수 없습니다!", Toast.LENGTH_SHORT).show()
            } else {
                Firebase.storage.reference.child("dogProfileImage/$userId/$key.png").delete()
                    .addOnSuccessListener { // 사진 삭제
                    }.addOnFailureListener {
                    }
                FBRef.dogRef.child(userId).child(key)
                    .removeValue() // 파이어베이스에서 반려견 프로필 키 값에 해당되는 데이터 삭제
                editor = sharedPreferences.edit()
                editor.remove(userId)
                editor.commit()
                Toast.makeText(this, "반려견 프로필이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                finish() // 삭제 완료 후 프로필 창 닫기
            }
        }

        getDogData()

        val editBtn = findViewById<ImageView>(R.id.edit)
        editBtn.setOnClickListener {
            val intent = Intent(this, DogProfileEditActivity::class.java)
            intent.putExtra("key", key)
            startActivity(intent)
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun getDogData() { // 프로필 설정
        Log.d("getDogData", "getDogData() is called")
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try { // 프로필 삭제 후 그 키 값에 해당하는 프로필이 삭제되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                    val post = dataSnapshot.getValue(DogModel::class.java)
                    dogName = post!!.dogName
                    findViewById<EditText>(R.id.dogNameArea).setText(dogName)
                    dogBirthDate = post!!.dogBirthDate
                    dogSex = post!!.dogSex
                    dogSpecies = post!!.dogSpecies
                    dogWeight = post!!.dogWeight
                    findViewById<EditText>(R.id.dogWeightArea).setText(dogWeight + "kg")
                    neutralization = post!!.neutralization
                    findViewById<EditText>(R.id.neutralization).setText(neutralization)

                    dogProfileFile = post!!.dogProfileFile // 가져올 유저의 profile 사진

                    val profileFile =
                        FBRef.dogRef.child(userId).child(key).child("dogProfileFile").get()
                            .addOnSuccessListener {
                                val storageReference =
                                    Firebase.storage.reference.child(it.value.toString()) // 반려견의 profile 사진을 DB의 storage로부터 가져옴

                                val imageView = findViewById<ImageView>(R.id.dogImage)
                                storageReference.downloadUrl.addOnCompleteListener(
                                    OnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Glide.with(applicationContext).load(task.result)
                                                .into(imageView) // 반려견의 profile 사진을 표시함
                                        } else {
                                            // findViewById<ImageView>(R.id.profileImageArea).isVisible = false
                                        }
                                    })
                            }

                    val sb = StringBuffer() // 입력된 생년월일이 20100409라면 2010.04.09로 변환하여 화면에 출력하기 위해 StringBuffer() 사용
                    sb.append(dogBirthDate)
                    sb.insert(4, ".")
                    sb.insert(7, ".")
                    findViewById<EditText>(R.id.dogBirthDate).setText(sb)

                    if (dogSex == "수컷") // 수컷, 암컷에 따라 글 색상을 다르게 표시
                        findViewById<EditText>(R.id.dogSexArea).setTextColor(Color.parseColor("#6495ED"))
                    else
                        findViewById<EditText>(R.id.dogSexArea).setTextColor(Color.parseColor("#FA8072"))
                    findViewById<EditText>(R.id.dogSexArea).setText(dogSex)
                    findViewById<EditText>(R.id.dogSpeciesArea).setText(dogSpecies)
                } catch(e : Exception) {
                    Log.d(TAG, "게시글 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        FBRef.dogRef.child(userId).child(key).addValueEventListener(postListener)
    }
}