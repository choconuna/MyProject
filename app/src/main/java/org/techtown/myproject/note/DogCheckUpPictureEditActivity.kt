package org.techtown.myproject.note

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.community.GalleryAdapter
import org.techtown.myproject.utils.DogCheckUpPictureModel
import org.techtown.myproject.utils.FBRef
import java.io.File
import java.util.ArrayList

class DogCheckUpPictureEditActivity : AppCompatActivity() {

    private val TAG = DogCheckUpPictureModel::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userId : String
    lateinit var dogId : String
    lateinit var nowDate : String

    private lateinit var date : String
    private lateinit var key : String

    lateinit var checkUpCategorySpinner : Spinner
    lateinit var checkUpCategoryType : String

    private lateinit var yearArea : EditText
    private lateinit var monthArea : EditText
    private lateinit var dayArea : EditText

    private lateinit var nameArea : EditText
    private lateinit var contentArea : EditText

    lateinit var galleryAdapter: GalleryAdapter
    var imageList : ArrayList<Uri> = ArrayList()
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager : RecyclerView.LayoutManager

    private lateinit var imageButton : ImageView
    private lateinit var imageCnt : TextView
    private var count = 0 // 첨부한 사진 수
    private var originCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_check_up_picture_edit)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE)
        dogId = sharedPreferences.getString(userId, "").toString() // 현재 대표 반려견의 id
        date = intent.getStringExtra("date").toString()
        key = intent.getStringExtra("id").toString() // dogCheckUpPicture Id 전송

        yearArea = findViewById(R.id.yearArea)
        monthArea = findViewById(R.id.monthArea)
        dayArea = findViewById(R.id.dayArea)

        nameArea = findViewById(R.id.hospitalArea)
        contentArea = findViewById(R.id.contentArea)

        imageCnt = findViewById(R.id.imageCnt)

        getData()

        checkUpCategorySpinner = findViewById(R.id.checkUpCategorySpinner)
        checkUpCategoryType = checkUpCategorySpinner.getItemAtPosition(0).toString()
        checkUpCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                checkUpCategoryType = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        galleryAdapter = GalleryAdapter(imageList, this) // 어댑터 초기화
        recyclerView = findViewById(R.id.imageRecyclerView)
        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = galleryAdapter

        // 사진 클릭 시 사진 삭제하기
        galleryAdapter.setItemClickListener(object: GalleryAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                Log.d("removeImage", "이미지 삭제")
                imageList.remove(imageList[position]) // imageList에서 해당 사진 삭제
                count -= 1 // 첨부한 사진의 수를 1 줄임
                imageCnt.text = count.toString() // 첨부한 사진의 수를 반영
                Log.d("imageList", imageList.toString())
                galleryAdapter.notifyDataSetChanged()
            }
        })

        imageButton = findViewById(R.id.imageBtn)
        imageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            activityResult.launch(intent)
        }

        val editBtn = findViewById<Button>(R.id.editBtn)
        editBtn.setOnClickListener {

            val month = monthArea.text.toString().toInt()
            val day = dayArea.text.toString().toInt()

            if(nameArea.text.toString() == "") {
                Toast.makeText(this, "병원 이름을 입력하세요!", Toast.LENGTH_LONG).show()
                nameArea.setSelection(0)
            }
            else if(yearArea.text.toString() == "" || monthArea.text.toString() == "" || dayArea.text.toString() == "" || month < 1 || month > 12 || day < 1 || day > 31) {
                if(((month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) && day > 31) || ((month == 2 || month == 4 || month == 6 || month == 9 || month == 11) && day > 30)) {
                    Toast.makeText(this, "날짜를 정확하게 입력하세요!", Toast.LENGTH_LONG).show()
                    yearArea.setSelection(0)
                } else {
                    Toast.makeText(this, "날짜를 정확하게 입력하세요!", Toast.LENGTH_LONG).show()
                    yearArea.setSelection(0)
                }
            }  else {
                val date = yearArea.text.toString() + "." + monthArea.text.toString() + "." + dayArea.text.toString()

                FBRef.checkUpPictureRef.child(userId).child(dogId).child(key).setValue(
                    DogCheckUpPictureModel(key, dogId, date, checkUpCategoryType, nameArea.text.toString().trim(), contentArea.text.toString().trim(), count.toString())
                ) // 반려견 검사 기록 정보 데이터베이스에 저장

                Toast.makeText(this, "검사 사진 수정 완료!", Toast.LENGTH_SHORT).show()

                if(count >= 1) { // 이미지 첨부되었을 시 이미지를 storage로 업로드
                    imageUpload(key)
                }

                finish()
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun getData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val dataModel = dataSnapshot.getValue(DogCheckUpPictureModel::class.java)

                    val dateSp = dataModel!!.date.split(".")
                    yearArea.setText(dateSp[0])
                    monthArea.setText(dateSp[1])
                    dayArea.setText(dateSp[2])

                    nameArea.setText(dataModel!!.hospitalName)

                    contentArea.setText(dataModel!!.content)

                    checkUpCategoryType = dataModel!!.checkUpCategory
                    for(i in 0 until checkUpCategorySpinner.count) {
                        if(checkUpCategorySpinner.getItemAtPosition(i).toString() == checkUpCategoryType)  {
                            checkUpCategorySpinner.setSelection(i)
                            break
                        }
                    }

                    originCount = dataModel!!.count.toInt()
                    count = dataModel!!.count.toInt()
                    imageCnt.text = dataModel!!.count

                    if(dataModel!!.count.toInt() >= 1) { // 기존의 이미지들을 불러옴
                        var fetchedImageCount = 0
                        for(index in 0 until dataModel!!.count.toInt()) {
                            val storageRef = Firebase.storage.reference.child("checkUpImage/$userId/$dogId/$key/$key$index.png")
                            val localFile = File.createTempFile("image", "png")
                            storageRef.getFile(localFile)
                                .addOnSuccessListener {
                                    val uri = FileProvider.getUriForFile(applicationContext, "org.techtown.myproject.fileprovider", localFile)
                                    imageList.add(uri)
                                    fetchedImageCount++

                                    if (fetchedImageCount == dataModel!!.count.toInt()) {
                                        galleryAdapter.notifyDataSetChanged()
                                    }
                                }
                                .addOnFailureListener {
                                    Log.d("imageEditList", "이미지 가져오기 실패")
                                }
                        }
                    }

                } catch (e: Exception) {
                    Log.d(TAG, "검사 사진 기록 삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.checkUpPictureRef.child(userId).child(dogId).child(key).addValueEventListener(postListener)
    }

    private fun imageUpload(key : String) { // 이미지를 storage에 업로드하는 함수
        val storage = Firebase.storage
        val storageRef = storage.reference

        if (originCount >= 1) { // 기존의 이미지가 존재했다면 전부 삭제함
            for (index in 0 until originCount) {
                Firebase.storage.reference.child("checkUpImage/$userId/$dogId/$key/$key$index.png")
                    .delete().addOnSuccessListener { // 사진 삭제
                    }.addOnFailureListener {
                    }
            }
        }

        for(cnt in 0 until count) {
            val mountainsRef = storageRef.child("checkUpImage/$userId/$dogId/$key/$key$cnt.png")

            var uploadTask = mountainsRef.putFile(imageList[cnt])
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
            }.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            }
        }
    }

    private val activityResult : ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {

        if(it.resultCode == RESULT_OK) { // 결과 코드 OK, 아니면 null
            if(it.data!!.clipData != null) { // 멀티 이미지일 경우
                count += it.data!!.clipData!!.itemCount // 선택한 이미지 개수

                for(index in 0 until it.data!!.clipData!!.itemCount) {
                    val imageUri = it.data!!.clipData!!.getItemAt(index).uri // 이미지 담기
                    imageList.add(imageUri) // 이미지 추가
                }
            } else { // 싱글 이미지일 경우
                count += it.data!!.clipData!!.itemCount
                val imageUri = it.data!!.data
                imageList.add(imageUri!!)
            }
            galleryAdapter.notifyDataSetChanged() // 동기화
            imageCnt.text = count.toString()
            Log.d("imageList", imageList.toString())
        }
    }
}