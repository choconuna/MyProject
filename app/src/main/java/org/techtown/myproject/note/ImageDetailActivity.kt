package org.techtown.myproject.note

import android.app.Activity
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R

class ImageDetailActivity : AppCompatActivity() {

    lateinit var image : String
    lateinit var imageView : ImageView
    lateinit var backBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)

        image = intent.getStringExtra("image").toString()

        imageView = findViewById(R.id.imageView)

        getImage()

        backBtn = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun getImage() {
        val storageReference = Firebase.storage.reference.child(image) // 메모에 첨부된 사진을 DB의 storage로부터 가져옴 -> 첨부된 사진이 여러 장이라면, 제일 첫 번째 사진을 대표 사진으로 띄움

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if(task.isSuccessful) {
                Glide.with(this).load(task.result).into(imageView!!) // 메모의 사진을 표시함
            } else {
                imageView!!.isVisible = false
            }
        })
    }
}