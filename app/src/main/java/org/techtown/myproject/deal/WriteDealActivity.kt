package org.techtown.myproject.deal

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.community.GalleryAdapter
import org.techtown.myproject.utils.DealModel
import org.techtown.myproject.utils.FBRef
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class WriteDealActivity : AppCompatActivity() {

    private val TAG = WriteDealActivity::class.java.simpleName

    private lateinit var myUid : String

    private lateinit var pullLocationName : String

    private lateinit var locationArea : TextView

    private lateinit var spinner : Spinner
    private lateinit var category : String // 판매할 제품 카테고리
    private lateinit var methodSpinner : Spinner
    private lateinit var method : String // 거래 방법
    private lateinit var titleArea : EditText
    private lateinit var priceArea : EditText
    private lateinit var checkFree : CheckBox
    private lateinit var contentArea : EditText

    private val decimalFormat = DecimalFormat("#,###")
    private var result: String = ""

    lateinit var galleryAdapter: GalleryAdapter
    var imageList : ArrayList<Uri> = ArrayList()
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager : RecyclerView.LayoutManager

    private lateinit var imageButton : ImageView
    private lateinit var imageCnt : TextView
    private var count = 0 // 첨부한 사진 수

    lateinit var writeBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_deal)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        pullLocationName = intent.getStringExtra("지역").toString()

        setData()

        category = "사료"
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                category = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        method = "직거래"
        methodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                method = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        checkFree.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                priceArea.setText("0")
                priceArea.isEnabled = false
            } else {
                priceArea.setText("")
                priceArea.isEnabled = true
            }
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!TextUtils.isEmpty(charSequence.toString()) && charSequence.toString() != result){
                    result = decimalFormat.format(charSequence.toString().replace(",","").toDouble())
                    priceArea.setText(result);
                    priceArea.setSelection(result.length);
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        }

        priceArea.addTextChangedListener(watcher)

        // 사진 클릭 시 사진 삭제하기
        galleryAdapter.setItemClickListener(object: GalleryAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                Log.d("removeImage", "이미지 삭제")
                imageList.remove(imageList[position]) // imageList에서 해당 사진 삭제
                count -= 1 // 첨부한 사진의 수를 1 줄임
                imageCnt.text = count.toString() // 첨부한 사진의 수를 반영
                Log.d("imageList", imageList.toString())
                galleryAdapter.notifyDataSetChanged()
            }
        })

        imageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            activityResult.launch(intent)
        }

        writeBtn.setOnClickListener {
            val title = titleArea.text.toString().trim()
            val price = priceArea.text.toString().trim().replace(",", "")
            val content = contentArea.text.toString().trim()

            val currentDataTime = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentDataTime)

            when {
                title == "" -> {
                    Toast.makeText(this, "제목을 입력하세요!", Toast.LENGTH_SHORT).show()
                    titleArea.setSelection(0)
                }
                price == "" -> {
                    Toast.makeText(this, "가격을 입력하세요!", Toast.LENGTH_SHORT).show()
                    priceArea.setSelection(0)
                }
                content == "" -> {
                    Toast.makeText(this, "내용을 입력하세요!", Toast.LENGTH_SHORT).show()
                    contentArea.setSelection(0)
                }
                else -> {
                    val key = FBRef.dealRef.push().key.toString() // 게시물의 키 값을 먼저 받아옴 -> 게시물에 해당하는 이미지의 이름을 게시물의 키 값으로 설정하기 위함

                    FBRef.dealRef.child(key).setValue(DealModel(key, myUid, pullLocationName, category, price, title, content, count.toString(), method, "판매 중", dateFormat, "", "")) // 거래 정보 데이터베이스에 저장

                    Toast.makeText(this, "게시글 입력 완료", Toast.LENGTH_LONG).show()

                    if(count >= 1) { // 이미지 첨부되었을 시 이미지를 storage로 업로드
                        imageUpload(key)
                    }
                    finish()
                }
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setData() {
        locationArea = findViewById(R.id.locationArea)
        locationArea.text = pullLocationName

        imageButton = findViewById(R.id.imageBtn)
        imageCnt = findViewById(R.id.imageCnt)

        galleryAdapter = GalleryAdapter(imageList, this) // 어댑터 초기화
        recyclerView = findViewById(R.id.imageRecyclerView)
        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = galleryAdapter

        spinner = findViewById(R.id.spinner)
        methodSpinner = findViewById(R.id.methodSpinner)
        titleArea = findViewById(R.id.titleArea)
        priceArea = findViewById(R.id.priceArea)
        checkFree = findViewById(R.id.checkFree)
        contentArea = findViewById(R.id.contentArea)

        writeBtn = findViewById(R.id.writeBtn)
    }

    private fun imageUpload(key : String) { // 이미지를 storage에 업로드하는 함수
        val storage = Firebase.storage
        val storageRef = storage.reference

        for(cnt in 0 until count) {
            val mountainsRef = storageRef.child("dealImage/$key/$key$cnt.png")

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