package org.techtown.myproject.walk

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import org.techtown.myproject.R
import org.techtown.myproject.utils.*
import java.io.IOException
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.*


class StartWalkActivity : AppCompatActivity(), OnMapReadyCallback {

    private val PERMISSION_REQUEST_CODE = 100
    private var PERMISSIONS : Array<String> = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)

    private lateinit var mLocationSource : FusedLocationSource
    private lateinit var mNaverMap : NaverMap

    private var coords : MutableList<LatLng> = mutableListOf()
    private val path = PathOverlay()

    private lateinit var distanceArea : TextView

    private lateinit var playBtn : Button
    private lateinit var pauseBtn : Button
    private lateinit var stopBtn : Button

    private var isPlaying : Boolean = true

    private lateinit var dogList : Array<String>

    private lateinit var runnable : Runnable
    val handler = Handler()
    var timeValue = 0

    private lateinit var userId : String

    private var distance = 0.0

    private lateinit var timeArea : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_walk)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        val currentTime = System.currentTimeMillis()
        val date = Date(currentTime)
        val currentTimeFormat = SimpleDateFormat("HH:mm:ss").format(date)

        dogList = intent.getStringArrayExtra("checkedDogIdList") as Array<String>
        Log.d("dogList", dogList.contentToString())

        var fm = supportFragmentManager
        var mapFragment : MapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment
        if(mapFragment == null) {
            mapFragment = MapFragment.newInstance()
            fm.beginTransaction().add(R.id.map_fragment, mapFragment).commit()
        }

        mapFragment.getMapAsync(this)

        mLocationSource = FusedLocationSource(this, PERMISSION_REQUEST_CODE) // 위치 반환하는 FusedLocationSource 생성

        runnable = object : Runnable { // 1초마다 실행되게 하는 핸들러
            override fun run() {
                timeValue ++
                //TextView 업데이트 하기
                timeToText(timeValue)?.let {
                    timeArea.text = it
                }
                handler.postDelayed(this, 1000)
            }
        }

        setData()

        playBtn.setOnClickListener { // 시작 버튼 클릭 시
            playBtn.visibility = GONE
            pauseBtn.visibility = VISIBLE

            isPlaying = true

            handler.post(runnable)
        }

        pauseBtn.setOnClickListener { // 중지 버튼 클릭 시
            pauseBtn.visibility = GONE
            playBtn.visibility = VISIBLE

            isPlaying = false

            handler.removeCallbacks(runnable)
        }

        stopBtn.setOnClickListener { // 정지 버튼 클릭 시
            handler.removeCallbacks(runnable)

            isPlaying = false

            var timeSp = timeArea.text.toString().split(":")
            var minute = timeSp[1].toInt() // 분 구하기

            if(minute >= 1) {

                val endTime = System.currentTimeMillis()
                val date = Date(endTime)
                val endTimeFormat = SimpleDateFormat("HH:mm:ss").format(date)

                val today = SimpleDateFormat("yyyy.MM.dd.E").format(endTime) // 오늘 날짜 구하기

                Log.d("walkTime", timeArea.text.toString())

                var key = FBRef.walkRef.child(userId).push().key.toString() // 키 값을 먼저 받아옴

                FBRef.walkRef.child(userId).child(key).setValue(WalkModel(userId, key, dogList!!.size.toString(), today, currentTimeFormat, endTimeFormat, timeArea.text.toString(), distance.toString()))

                for (i in dogList.indices) {
                    var childKey = FBRef.walkDogRef.child(userId).child(dogList!![i]).push().key.toString() // 키 값을 먼저 받아옴
                    FBRef.walkDogRef.child(userId).child(dogList!![i]).child(childKey).setValue(WalkDogModel(userId, key, childKey, dogList!![i].trim(), today, currentTimeFormat, endTimeFormat, timeArea.text.toString(), distance.toString()))
                }

                Toast.makeText(this, "산책 기록이 저장되었습니다!", Toast.LENGTH_SHORT).show()

                finish() // 산책하기 화면 종료
            } else {
                Toast.makeText(this, "산책 시간이 1분 이상이어야 합니다!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setData() {

        playBtn = findViewById(R.id.playBtn)
        pauseBtn = findViewById(R.id.pauseBtn)
        stopBtn = findViewById(R.id.stopBtn)

        distanceArea = findViewById(R.id.distanceArea)
        timeArea = findViewById(R.id.timeArea)

        playBtn.visibility = GONE
        pauseBtn.visibility = VISIBLE

        isPlaying = true

        handler.post(runnable)
    }

    private fun timeToText(time: Int = 0) : String?{ // 시간을 문자열로 변환
        return when {
            time < 0 -> {
                null
            }
            time == 0 -> {
                "00:00:00"
            }
            else -> {
                val h = time / 3600
                val m = time % 3600 / 60
                val s = time % 60
                "%1$02d:%2$02d:%3$02d".format(h, m, s)
            }
        }
    }

    override fun onMapReady(naverMap: NaverMap) {

        mNaverMap = naverMap // NaverMap 객체 받아서 NaverMap 객체에 위치 소스 지정
        mNaverMap.locationSource = mLocationSource
        mNaverMap.locationTrackingMode = LocationTrackingMode.Follow

        // UI 컨드롤 재배치
        var uiSettings = mNaverMap.uiSettings
        uiSettings.isCompassEnabled = true
        uiSettings.isZoomControlEnabled = true
        uiSettings.isLocationButtonEnabled = true

        // 권한 확인. 결과는 onRequestPermissionsResult 콜백 메소드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this) // gps 자동으로 받아오기
        setUpdateLocationListner()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == PERMISSION_REQUEST_CODE) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mNaverMap.locationTrackingMode = LocationTrackingMode.Follow
            }
        }
    }

    // 내 위치를 가져옴
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient //자동으로 gps 값을 받아옴
    private lateinit var locationCallback: LocationCallback //gps 응답 값을 가져옴

    @SuppressLint("MissingPermission")
    fun setUpdateLocationListner() {

        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY //높은 정확도
            interval = 1000 //1초에 한번씩 GPS 요청
        }

        val g = Geocoder(this)
        var address: MutableList<Address> = mutableListOf()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for ((i, location) in locationResult.locations.withIndex()) {
                    Log.d("nowLocation", "${location.latitude}, ${location.longitude}")

                    try {
                        address = g.getFromLocation(location.latitude, location.longitude, 10)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    if (address != null) {
                        if (address.size == 0) {
                            Log.d("getLocation", "위치 찾기 오류")
                        } else {
                            Log.d("getLocation", address[0].toString())
                        }
                    }

                    coords.add(LatLng(location.latitude, location.longitude))
                    if (coords.size > 1) {
                        path.coords = coords
                        path.map = mNaverMap

                        var myLoc = Location(LocationManager.NETWORK_PROVIDER)
                        var targetLoc = Location(LocationManager.NETWORK_PROVIDER)

                        myLoc.latitude = coords[coords.size - 2].latitude
                        myLoc.longitude = coords[coords.size - 2].longitude

                        targetLoc.latitude = location.latitude
                        targetLoc.longitude = location.longitude

//                        distance += abs(distance(myLoc.latitude, myLoc.longitude, targetLoc.latitude, targetLoc.longitude))
                        distance += distance(
                            myLoc.latitude,
                            myLoc.longitude,
                            targetLoc.latitude,
                            targetLoc.longitude,
                            'K'
                        )

                        val df = DecimalFormat("#.##")
                        df.roundingMode = RoundingMode.DOWN
                        val roundoff = df.format(distance)

                        distanceArea.text = roundoff
                    }
                    setLastLocation(location)
                }
            }
        }
        //location 요청 함수 호출 (locationRequest, locationCallback)

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    } //좌표를 주기적으로 갱신

    fun setLastLocation(location: Location) {
        val myLocation = LatLng(location.latitude, location.longitude)
        // coords.add(LatLng(location.latitude, location.longitude))
        if(coords.size > 2) {
            path.coords = coords
            path.map = mNaverMap
            path.width = 15
            path.color = Color.parseColor("#c08457")
        }

        val cameraUpdate = CameraUpdate.scrollTo(myLocation)
        mNaverMap.moveCamera(cameraUpdate)
        mNaverMap.maxZoom = 18.0
        mNaverMap.minZoom = 5.0
    }

    fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double, unit: Char): Double { // GPS의 위도와 경도로 거리 구하기
        val theta: Double
        var dist: Double
        return if (lat1 == lat2 && lon1 == lon2) {
            0.toDouble()
        } else {
            theta = lon1 - lon2
            dist =
                sin(deg2rad(lat1)) * sin(deg2rad(lat2)) + cos(deg2rad(lat1)) * cos(deg2rad(lat2)) * cos(
                    deg2rad(theta)
                )
            dist = acos(dist)
            dist = rad2deg(dist)
            dist *= 60 * 1.1515
            when (unit) {
                'M' -> {}
                'K' -> dist *= 1.609344
                'N' -> dist *= 0.8684
            }
            dist
        }
    }

    fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180
    }

    fun rad2deg(rad: Double): Double {
        return rad * 180 / Math.PI
    }
}