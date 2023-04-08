package org.techtown.myproject.note

import android.graphics.Bitmap
import android.graphics.Point
import android.os.Bundle
import android.view.Display
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import java.util.Collections.max
import java.util.Collections.min


class ImageDetailActivity : AppCompatActivity() {

    private var mScaleGestureDetector: ScaleGestureDetector? = null
    private var scaleFactor = 1.0f

    lateinit var image : String
    lateinit var imageView : ImageView
    lateinit var backBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)

        image = intent.getStringExtra("image").toString()

        imageView = findViewById(R.id.imageView)
        imageView.scaleType = ImageView.ScaleType.MATRIX; // 스케일 타입을 매트릭스로 해줘야 움직인다.


        mScaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

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

    // 제스처 이벤트가 발생하면 실행되는 메소드
    override fun onTouchEvent(motionEvent: MotionEvent?): Boolean {

        // 제스처 이벤트를 처리하는 메소드를 호출
        mScaleGestureDetector!!.onTouchEvent(motionEvent)
        return true
    }

    // 제스처 이벤트를 처리하는 클래스
    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {

            scaleFactor *= scaleGestureDetector.scaleFactor

            // 최소 0.5, 최대 2배
            scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 2.0f))

            // 이미지에 적용
            imageView.scaleX = scaleFactor
            imageView.scaleY = scaleFactor
            return true
        }
    }
}