package org.techtown.myproject.my

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.LogoutActivity
import org.techtown.myproject.MyNotificationReceiver
import org.techtown.myproject.R
import org.techtown.myproject.comment.CommentModel
import org.techtown.myproject.community.CommunityModel
import org.techtown.myproject.note.DogDungEditActivity
import org.techtown.myproject.note.DogPeeEditActivity
import org.techtown.myproject.note.DogVomitEditActivity
import org.techtown.myproject.receipt.Receipt
import org.techtown.myproject.utils.*
import java.io.ByteArrayOutputStream
import java.util.HashMap

class ProfileEditActivity : AppCompatActivity() {

    private val TAG = ProfileEditActivity::class.java.simpleName

    private lateinit var nameArea : EditText
    private lateinit var nickNameArea : EditText
    private lateinit var profileImage : ImageView

    private lateinit var defaultImage : Bitmap

    lateinit var originName : String
    lateinit var nickName : String
    var nickNameCheckClicked : Boolean = false
    var nickNameChecked : Boolean = false

    private lateinit var email : String
    private lateinit var pw : String

    lateinit var profileFile : String
    var isImageUpload = false

    lateinit var mDatabaseReference: DatabaseReference
    lateinit var mAuth : FirebaseAuth
    lateinit var uid : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        val backBtn = findViewById<ImageView>(R.id.backBtn)
        backBtn.setOnClickListener { // 뒤로 가기 버튼 클릭 시 액티비티 종료
            finish()
        }

        profileImage = findViewById(R.id.imageView)
        nameArea = findViewById(R.id.userName)
        nickNameArea = findViewById(R.id.nickNameArea)

        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser?.uid.toString()

        mDatabaseReference = FBRef.userRef.child(uid)

        getProfileData(uid) // 기존 사용자의 profile 데이터 가져오기

        profileImage.setOnClickListener {
            showDialog()
        }

        val nickNameCheckBtn = findViewById<Button>(R.id.nickNameCheckBtn)
        nickNameCheckBtn.setOnClickListener {
            nickNameCheckClicked = true
            nickName = nickNameArea.text.toString().trim()
            if(originName != nickName) {
                checkNickName(nickName)
            } else {
                Toast.makeText(this, "기존의 닉네임입니다!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.editBtn).setOnClickListener {
            editProfile(uid) // 사용자의 profile 내용 수정
        }

        findViewById<TextView>(R.id.leaveBtn).setOnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.leave_dialog, null)
            val mBuilder = AlertDialog.Builder(this).setView(mDialogView)

            val alertDialog = mBuilder.show()
            val noBtn = alertDialog.findViewById<Button>(R.id.noBtn)
            noBtn?.setOnClickListener { // 아니요 버튼 클릭 시
                Log.d(TAG, "no Button Clicked")

                alertDialog.dismiss()
            }

            val yesBtn = alertDialog.findViewById<Button>(R.id.yesBtn)
            yesBtn?.setOnClickListener {  // 예 버튼 클릭 시
                Log.d(TAG, "yes Button Clicked")
                alertDialog.dismiss()

                removeAllDate()
                val intent = Intent(this, LeaveActivity::class.java)
                startActivity(intent) // 탈퇴 액티비티 실행시키기
            }
        }
    }

    private fun getProfileData(key : String) { // 프로필 데이터 가져옴
        val postListener = object : ValueEventListener { // 파이어베이스 안의 값이 변화할 시 작동됨
            @RequiresApi(Build.VERSION_CODES.S)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dataModel = dataSnapshot.getValue(UserInfo::class.java)

                originName = dataModel!!.nickName
                profileFile = dataModel!!.profileImage
                email = dataModel!!.email
                pw = dataModel!!.password

                val profileFile = mDatabaseReference.child("profileImage").get().addOnSuccessListener {
                    val storageReference = Firebase.storage.reference.child(it.value.toString()) // 유저의 profile 사진을 DB의 storage로부터 가져옴

                    storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                        if(task.isSuccessful) {
                            Glide.with(applicationContext).load(task.result).into(profileImage) // 유저의 profile 사진을 게시자 이름의 왼편에 표시함
                            profileImage.setAllowClickWhenDisabled(true)
                        } else {
                        }
                    })
                }

                val userName = mDatabaseReference.child("userName").get().addOnSuccessListener {
                    nameArea.setText(it.value.toString()) // 작성자의 이름 가져오기
                }

                val nickName = mDatabaseReference.child("nickName").get().addOnSuccessListener {
                    nickNameArea.setText(it.value.toString()) // 닉네임 가져오기
                    originName = it.value.toString()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        mDatabaseReference.addValueEventListener(postListener)
    }

    private fun checkNickName(nickname : String) { // 닉네임이 중복되었는지 확인
        FBRef.userRef.orderByChild("nickName").equalTo(nickname).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                nickNameChecked = if (!dataSnapshot.exists()) {
                    Toast.makeText(applicationContext, "닉네임이 중복되지 않습니다!", Toast.LENGTH_SHORT).show()
                    true
                } else {
                    Toast.makeText(applicationContext, "닉네임이 중복됩니다!", Toast.LENGTH_SHORT).show()
                    false
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(applicationContext, databaseError.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun imageUpload(key : String) { // 이미지를 storage에 업로드하는 함수
        Log.d("IMAGEUPLOAD", "imageUpload() is called")
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child("profileImage/$key.png")
        if(profileFile == "profileImage/$key.png") { // 사용자의 profile 이미지가 기본 이미지가 아니었다면, storage에 저장되어있던 이미지 삭제
            storageRef.child("profileImage/$key.png").delete()
        }

        profileFile = "profileImage/$key.png" // profileImage 아래의 사용자 uid 값 이름으로 되어있는 png 파일을 사용자의 profile file 이름으로 설정

        profileImage.isDrawingCacheEnabled = true
        profileImage.buildDrawingCache()
        val bitmap = (profileImage.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = mountainsRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
        }
    }

    private fun showDialog() { // 프로필 이미지 설정을 위한 다이얼로그 띄우기
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.profile_image_dialog, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)

        val alertDialog = mBuilder.show()
        val galleryBtn = alertDialog.findViewById<Button>(R.id.galleryBtn)
        galleryBtn?.setOnClickListener { // 갤러리에서 가져오기 버튼 클릭 시
            Log.d(TAG, "gallery Button Clicked")

            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)

            isImageUpload = true

            val imageView : ImageView = profileImage
            defaultImage = (imageView.drawable as BitmapDrawable).bitmap

            alertDialog.dismiss()
        }

        val basicBtn = alertDialog.findViewById<Button>(R.id.basicBtn)
        basicBtn?.setOnClickListener {  // 기본 이미지로 설정 버튼 클릭 시
            Log.d(TAG, "basic Button Clicked")

            isImageUpload = false

            val storage = Firebase.storage
            val storageRef = storage.reference
            val mountainsRef = storageRef.child("profileImage/$uid.png")
            if(profileFile == "profileImage/$uid.png") {    // 사용자의 profile 이미지가 기본 이미지가 아니었다면, storage에 저장되어있던 이미지 삭제
                storageRef.child("profileImage/$uid.png").delete()
            }

            profileFile = "basic_user.png"
            profileImage.setImageResource(R.drawable.blankuser)

            alertDialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK && requestCode == 100) {
            profileImage.setImageURI(data?.data) // profileImage에 갤러리에서 선택한 이미지 넣기
            imageUpload(uid)
        }
    }

    private fun editProfile(key : String) {
        Log.d(TAG, originName + " " + nickNameArea.text.toString().trim())
        if(originName == nickNameArea.text.toString().trim() || (nickNameCheckClicked && nickNameChecked)) {
            FBRef.userRef.child(key).setValue(UserInfo(uid, profileFile, nameArea.text.toString().trim(), nickNameArea.text.toString().trim(), email, pw)) // 게시물 정보 데이터베이스에 저장
            Toast.makeText(this, "프로필 수정 완료", Toast.LENGTH_SHORT).show()
            finish() // 창 닫기
        } else if(!nickNameCheckClicked) {
            Toast.makeText(this, "닉네임이 중복되는지 확인하세요!", Toast.LENGTH_LONG).show()
            nickNameArea.setSelection(0)
        } else if(!nickNameChecked) {
            Toast.makeText(this, "닉네임이 중복됩니다. 다시 입력하세요!", Toast.LENGTH_LONG).show()
            nickNameArea.setText("")
            nickNameArea.setSelection(0)
            nickNameCheckClicked = false
        }
    }

    private fun removeAllDate() {
        FBRef.tokenRef.child(uid).removeValue()

        removeEatData()
        FBRef.mealRef.child(uid).removeValue()
        FBRef.snackRef.child(uid).removeValue()
        FBRef.tonicRef.child(uid).removeValue()

        FBRef.waterRef.child(uid).removeValue()
        FBRef.peeRef.child(uid).removeValue()
        FBRef.dungRef.child(uid).removeValue()
        FBRef.vomitRef.child(uid).removeValue()
        FBRef.heartRef.child(uid).removeValue()
        FBRef.medicineRef.child(uid).removeValue()

        removeMedicinePlan()
        FBRef.medicinePlanRef.child(uid).removeValue()

        removeMemoData()
        FBRef.memoRef.child(uid).removeValue()
        FBRef.checkUpInputRef.child(uid).removeValue()

        removeCheckUpPictureData()
        FBRef.checkUpPictureRef.child(uid).removeValue()

        removeReceipt()
        FBRef.receiptRef.child(uid).removeValue()

        FBRef.walkDogRef.child(uid).removeValue()
        FBRef.walkRef.child(uid).removeValue()
        FBRef.userLocationRef.child(uid).removeValue()
        FBRef.blockRef.child(uid).removeValue()

        removeReComment() // 대댓글 삭제
        removeComment() // 댓글 삭제
        removeCommunity() // 게시물 삭제

        removeChat() // 채팅 삭제

        removeDeal() // 거래, 거래 채팅 삭제

        FBRef.userMainDogRef.child(uid).removeValue()
        FBRef.dogRef.child(uid).removeValue()
        FBRef.userRef.child(uid).removeValue()
    }

    private fun removeEatData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogModel::class.java)

                        val postListener1 = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                try {
                                    for (dataModel in dataSnapshot.children) {
                                        val item = dataModel.getValue(DogMealModel::class.java)
                                        var dogId = item!!.dogId
                                        var id = item!!.dogMealId

                                        Firebase.storage.reference.child("dogMealImage/$uid/$dogId/$id.png")
                                            .delete().addOnSuccessListener { // 사진 삭제
                                            }.addOnFailureListener {
                                            }
                                    }

                                } catch (e: Exception) {
                                    Log.e(TAG, "Error while getting community data", e)
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e(TAG, "Error while getting community data", databaseError.toException())
                            }
                        }
                        FBRef.mealRef.child(uid).child(item!!.dogId).addValueEventListener(postListener1)

                        val postListener2 = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                try {
                                    for (dataModel in dataSnapshot.children) {
                                        val item = dataModel.getValue(DogSnackModel::class.java)
                                        var dogId = item!!.dogId
                                        var id = item!!.dogSnackId

                                        Firebase.storage.reference.child("dogSnackImage/$uid/$dogId/$id.png")
                                            .delete().addOnSuccessListener { // 사진 삭제
                                            }.addOnFailureListener {
                                            }
                                    }

                                } catch (e: Exception) {
                                    Log.e(TAG, "Error while getting community data", e)
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e(TAG, "Error while getting community data", databaseError.toException())
                            }
                        }
                        FBRef.snackRef.child(uid).child(item!!.dogId).addValueEventListener(postListener2)

                        val postListener3 = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                try {
                                    for (dataModel in dataSnapshot.children) {
                                        val item = dataModel.getValue(DogTonicModel::class.java)
                                        var dogId = item!!.dogId
                                        var id = item!!.dogTonicId

                                        Firebase.storage.reference.child("dogTonicImage/$uid/$dogId/$id.png")
                                            .delete().addOnSuccessListener { // 사진 삭제
                                            }.addOnFailureListener {
                                            }
                                    }

                                } catch (e: Exception) {
                                    Log.e(TAG, "Error while getting community data", e)
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e(TAG, "Error while getting community data", databaseError.toException())
                            }
                        }
                        FBRef.tonicRef.child(uid).child(item!!.dogId).addValueEventListener(postListener3)
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error while getting community data", e)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error while getting community data", databaseError.toException())
            }
        }
        FBRef.dogRef.child(uid).addValueEventListener(postListener)
    }

    private fun removeMemoData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogModel::class.java)

                        val postListener = object : ValueEventListener { // 파이어베이스 안의 값이 변화할 시 작동됨
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                try { // 사진 삭제 후 그 키 값에 해당하는 사진이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                                    val dataModel = dataSnapshot.getValue(DogMemoModel::class.java)

                                    if (dataModel!!.count.toInt() >= 1) {
                                        for (index in 0 until dataModel.count.toInt()) {
                                            Firebase.storage.reference.child("memoImage/$uid/${dataModel!!.dogId}/${dataModel!!.dogMemoId}/${dataModel!!.dogMemoId}$index.png")
                                                .delete().addOnSuccessListener { // 사진 삭제
                                                }.addOnFailureListener {
                                                }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.d(TAG, "사진 삭제 완료")
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                // Getting Post failed, log a message
                                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                            }
                        }
                        FBRef.memoRef.child(uid).child(item!!.dogId).addValueEventListener(postListener)
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error while getting community data", e)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error while getting community data", databaseError.toException())
            }
        }
        FBRef.dogRef.child(uid).addValueEventListener(postListener)
    }

    private fun removeCheckUpPictureData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogModel::class.java)

                        val postListener = object : ValueEventListener { // 파이어베이스 안의 값이 변화할 시 작동됨
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                try { // 사진 삭제 후 그 키 값에 해당하는 사진이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성
                                    val dataModel = dataSnapshot.getValue(DogCheckUpPictureModel::class.java)

                                    if (dataModel!!.count.toInt() >= 1) {
                                        for (index in 0 until dataModel.count.toInt()) {
                                            Firebase.storage.reference.child("checkUpImage/$uid/${dataModel!!.dogId}/${dataModel!!.dogCheckUpPictureId}/${dataModel!!.dogCheckUpPictureId}$index.png")
                                                .delete().addOnSuccessListener { // 사진 삭제
                                                }.addOnFailureListener {
                                                }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.d(TAG, "사진 삭제 완료")
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                // Getting Post failed, log a message
                                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                            }
                        }
                        FBRef.checkUpPictureRef.child(uid).child(item!!.dogId).addValueEventListener(postListener)
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error while getting community data", e)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error while getting community data", databaseError.toException())
            }
        }
        FBRef.dogRef.child(uid).addValueEventListener(postListener)
    }

    private fun removeMedicinePlan() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogModel::class.java)

                        val postListener = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                try { // 투약 일정 기록 삭제 후 그 키 값에 해당하는 기록이 호출되어 오류가 발생, 오류 발생되어 앱이 종료되는 것을 막기 위한 예외 처리 작성

                                    for (dataModel in dataSnapshot.children) {
                                        val item = dataModel.getValue(DogMedicinePlanModel::class.java)

                                        // 투약 일정에 해당하는 투약 알림 삭제하기
                                        val pendingIntent = PendingIntent.getBroadcast(applicationContext, item!!.dogMedicinePlanId.hashCode(), Intent(applicationContext, MyNotificationReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
                                        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                        alarmManager.cancel(pendingIntent)
                                        pendingIntent.cancel()
                                    }

                                } catch (e: Exception) {
                                    Log.d(TAG, " 기록 삭제 완료")
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Getting Post failed, log a message
                                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                            }
                        }
                        FBRef.medicinePlanRef.child(uid).child(item!!.dogId).addValueEventListener(postListener)
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error while getting community data", e)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error while getting community data", databaseError.toException())
            }
        }
        FBRef.dogRef.child(uid).addValueEventListener(postListener)
    }

    private fun removeReceipt() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(ReceiptModel::class.java)

                        if(item!!.imgCnt.toInt() > 0) {
                            for (index in 0 until item!!.imgCnt.toInt()) {
                                Firebase.storage.reference.child("receiptImage/$uid/${item!!.receiptId}/${item!!.receiptId}$index.png")
                                    .delete().addOnSuccessListener { // 사진 삭제
                                    }.addOnFailureListener {
                                    }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d("receiptError", e.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.d("receiptError", databaseError.toString())
            }
        }
        FBRef.receiptRef.child(uid).addValueEventListener(postListener)
    }

    private fun removeCommunity() {
        val categoryList : List<String> = listOf("정보", "후기", "자유", "질문")

        for (category in categoryList) {
            FBRef.communityRef.child(category).orderByChild("uid").equalTo(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            for (data in snapshot.children) {
                                val item = data.getValue(CommunityModel::class.java)
                                // 해당 Community에 속하는 댓글 데이터 삭제
                                FBRef.commentRef.child(item!!.communityId).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for (commentSnapshot in snapshot.children) {
                                            val comment = commentSnapshot.getValue(CommentModel::class.java)
                                            if (comment?.communityId == item!!.communityId) { // 해당 Community에 속하는 댓글인 경우
                                                // 대댓글 데이터 삭제
                                                FBRef.reCommentRef.child(comment.communityId).child(comment.commentId).addListenerForSingleValueEvent(object : ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        for (reCommentSnapshot in snapshot.children) {
                                                            val reComment = reCommentSnapshot.getValue(ReCommentModel::class.java)
                                                            if (reComment?.communityId == comment.communityId && reComment.commentId == comment.commentId) { // 해당 댓글에 속하는 대댓글인 경우
                                                                FBRef.reCommentRef.child(reComment.communityId).child(reComment.commentId).child(reComment.reCommentId).removeValue() // 대댓글 데이터 삭제
                                                            }
                                                        }
                                                    }

                                                    override fun onCancelled(error: DatabaseError) {}
                                                })

                                                FBRef.commentRef.child(comment.communityId).child(comment.commentId).removeValue() // 댓글 데이터 삭제
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })

                                if (item!!.count.toInt() >= 1) {
                                    var key = item!!.communityId
                                    for (index in 0 until item!!.count.toInt()) {
                                        Firebase.storage.reference.child("communityImage/$key/$key$index.png")
                                            .delete().addOnSuccessListener { // 사진 삭제
                                            }.addOnFailureListener {
                                            }
                                    }
                                }

                                FBRef.communityRef.child(category).child(item!!.communityId).removeValue()
                            }
                        } catch (e: Exception) {

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // 데이터 삭제 실패 시 처리
                    }
                })
        }
    }

    private fun removeComment() {
        val categoryList : List<String> = listOf("정보", "후기", "자유", "질문")

        for (category in categoryList) {
            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {

                        for (dataModel in dataSnapshot.children) {
                            val community = dataModel.getValue(CommunityModel::class.java)

                            val postListener = object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    try {
                                        for (dataModel in dataSnapshot.children) {
                                            val comment = dataModel.getValue(CommentModel::class.java)

                                            if(community!!.communityId == comment!!.communityId) {
                                                FBRef.commentRef.child(comment!!.communityId).child(comment!!.commentId).removeValue()
                                            }
                                        }

                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error while getting community data", e)
                                    }
                                }
                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.e(TAG, "Error while getting community data", databaseError.toException())
                                }
                            }
                            FBRef.commentRef.child(category).addValueEventListener(postListener)
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Error while getting community data", e)
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "Error while getting community data", databaseError.toException())
                }
            }
            FBRef.communityRef.child(category).addValueEventListener(postListener)
        }
    }

    private fun removeReComment() {
        val categoryList : List<String> = listOf("정보", "후기", "자유", "질문")

        for (category in categoryList) {
            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {

                        for (dataModel in dataSnapshot.children) {
                            val community = dataModel.getValue(CommunityModel::class.java)

                            val postListener = object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    try {
                                        for (dataModel in dataSnapshot.children) {
                                            val comment = dataModel.getValue(CommentModel::class.java)

                                            if(community!!.communityId == comment!!.communityId) {

                                                val postListener = object : ValueEventListener {
                                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                        try {

                                                            for (dataModel in dataSnapshot.children) {
                                                                val reComment = dataModel.getValue(ReCommentModel::class.java)

                                                                if(reComment!!.uid == uid) {
                                                                    FBRef.reCommentRef.child(reComment!!.communityId).child(reComment!!.reCommentId).child(reComment!!.reCommentId).removeValue()
                                                                }
                                                            }

                                                        } catch (e: Exception) {
                                                            Log.e(TAG, "Error while getting community data", e)
                                                        }
                                                    }
                                                    override fun onCancelled(databaseError: DatabaseError) {
                                                        Log.e(TAG, "Error while getting community data", databaseError.toException())
                                                    }
                                                }
                                                FBRef.reCommentRef.child(comment!!.communityId).addValueEventListener(postListener)
                                            }
                                        }

                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error while getting community data", e)
                                    }
                                }
                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.e(TAG, "Error while getting community data", databaseError.toException())
                                }
                            }
                            FBRef.commentRef.child(category).addValueEventListener(postListener)
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Error while getting community data", e)
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "Error while getting community data", databaseError.toException())
                }
            }
            FBRef.communityRef.child(category).addValueEventListener(postListener)
        }
    }

    private fun removeChat() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    for(dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(ChatConnection::class.java)

                        if(item!!.userId1 == uid || item!!.userId2 == uid) {

                            val postListener = object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    try {
                                        for(dataModel in dataSnapshot.children) {
                                            val item = dataModel.getValue(MessageModel::class.java)

                                            if(item!!.type == "picture") {
                                                for (index in 0 until item!!.picNum.toInt()) {
                                                    Firebase.storage.reference.child("messageImage/${item!!.chatConnectionId}/${item!!.messageId}/${item!!.messageId}$index.png")
                                                        .delete().addOnSuccessListener { // 사진 삭제
                                                        }.addOnFailureListener {
                                                        }
                                                }
                                            }
                                        }

                                        FBRef.messageRef.child(item!!.chatConnectionId).removeValue()
                                        FBRef.chatConnectionRef.child(item!!.chatConnectionId).removeValue() // 채팅 커넥션 삭제

                                    } catch(e : Exception) {
                                        Log.d("showChatList", e.toString())
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Geting Post failed, log a message
                                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                                }
                            }
                            FBRef.messageRef.child(item!!.chatConnectionId).addValueEventListener(postListener)
                        }
                    }

                } catch(e : Exception) {
                    Log.d("showChatList", e.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Geting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        FBRef.chatConnectionRef.addValueEventListener(postListener)
    }

    private fun removeDeal() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    for(dataModel in dataSnapshot.children) {
                        val deal = dataModel.getValue(DealModel::class.java)

                        if(deal!!.sellerId == uid || deal!!.buyerId == uid) { // 탈퇴할 사용자가 판매자이거나 사용자일 경우
                            val postListener = object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    try {
                                        for(dataModel in dataSnapshot.children) {
                                            val item = dataModel.getValue(DealChatConnection::class.java)

                                            if(item!!.userId1 == uid || item!!.userId2 == uid) { // 탈퇴할 사용자가 판매자이거나 사용자일 경우
                                                val postListener = object : ValueEventListener {
                                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                        try {

                                                            for(dataModel in dataSnapshot.children) {
                                                                val item = dataModel.getValue(DealMessageModel::class.java)

                                                                if(item!!.type == "picture") {
                                                                    for (index in 0 until item!!.picNum.toInt()) {
                                                                        Firebase.storage.reference.child("dealMessageImage/${item!!.chatConnectionId}/${item!!.messageId}/${item!!.messageId}$index.png")
                                                                            .delete().addOnSuccessListener { // 사진 삭제
                                                                            }.addOnFailureListener {
                                                                            }
                                                                    }
                                                                }
                                                            }

                                                            FBRef.dealMessageRef.child(item!!.dealId).child(item!!.chatConnectionId).removeValue()
                                                            FBRef.dealChatConnectionRef.child(item!!.dealId).child(item!!.chatConnectionId).removeValue()

                                                        } catch(e : Exception) { }
                                                    }

                                                    override fun onCancelled(databaseError: DatabaseError) {
                                                        // Geting Post failed, log a message
                                                        Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                                                    }
                                                }
                                                FBRef.dealMessageRef.child(item!!.dealId).child(item!!.chatConnectionId).addValueEventListener(postListener)
                                            }
                                        }

                                    } catch(e : Exception) {
                                        Log.d("showChatList", e.toString())
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Geting Post failed, log a message
                                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                                }
                            }
                            FBRef.dealChatConnectionRef.child(deal!!.dealId).addValueEventListener(postListener)
                        }

                        if(deal!!.sellerId == uid) { // 현재 탈퇴할 사용자가 판매자일 경우
                            if(deal!!.buyerId != "no") { // 구매자가 탈퇴하지 않은 사용자일 경우 -> 거래 모델의 판매자를 "no"로 바꿈
                                FBRef.dealRef.child(deal!!.dealId).setValue(DealModel(deal!!.dealId, "no", deal!!.location, deal!!.category, deal!!.price, deal!!.title, deal!!.content, deal!!.imgCnt, deal!!.method, "거래 완료", deal!!.date, deal!!.buyerId, deal!!.buyDate, deal!!.visitors))
                            } else if(deal!!.buyerId == "no" || deal!!.buyerId == "") { // 구매자가 탈퇴한 사용자이거나 없을 경우
                                FBRef.dealRef.child(deal!!.dealId).removeValue() // 거래 데이터 삭제
                            }
                        } else if(deal!!.buyerId == uid) { // 현재 탈퇴할 사용자가 구매자일 경우
                            if(deal!!.sellerId != "no") { // 판매자가 탈퇴하지 않은 사용자일 경우
                                FBRef.dealRef.child(deal!!.dealId).setValue(DealModel(deal!!.dealId, deal!!.sellerId, deal!!.location, deal!!.category, deal!!.price, deal!!.title, deal!!.content, deal!!.imgCnt, deal!!.method, "거래 완료", deal!!.date, "no", deal!!.buyDate, deal!!.visitors))
                            } else if(deal!!.sellerId == "no") { // 판매자가 탈퇴한 사용자일 경우
                                FBRef.dealRef.child(deal!!.dealId).removeValue() // 거래 데이터 삭제
                            }
                        }
                    }

                } catch(e : Exception) {
                    Log.d("showChatList", e.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Geting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        FBRef.dealRef.addValueEventListener(postListener)
    }
}