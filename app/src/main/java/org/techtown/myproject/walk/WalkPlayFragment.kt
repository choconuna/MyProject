package org.techtown.myproject.walk


import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.gson.annotations.SerializedName
import org.techtown.myproject.R
import org.techtown.myproject.my.DogProfileInActivity
import org.techtown.myproject.my.DogReVAdapter
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class WalkPlayFragment : Fragment() {

    lateinit var myUid: String

    private val TAG = WalkPlayFragment::class.java.simpleName

    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null // 현재 위치를 가져오기 위한 변수
    lateinit var mLastLocation: Location // 위치 값을 가지고 있는 객체
    private lateinit var mLocationRequest: LocationRequest // 위치 정보 요청의 매개변수를 저장하는
    private val REQUEST_PERMISSION_LOCATION = 10

    var BaseUrl = "https://api.openweathermap.org/data/2.5/"
    var AppId = "0fd049ed291f9069c321e2d10a9c2d4b"
    var lat = ""
    var lon = ""

    private lateinit var hour : String

    lateinit var locationArea : TextView
    lateinit var refresh : ImageView
    lateinit var weatherImage : ImageView
    lateinit var weatherArea : TextView
    lateinit var nowTempArea : TextView
    lateinit var humidityArea : TextView
    lateinit var windSpeedArea : TextView
    lateinit var minTempArea : TextView
    lateinit var maxTempArea : TextView

    lateinit var walkDogRecyclerView: RecyclerView
    private val walkDogReDataList = ArrayList<DogModel>() // 각 반려견의 프로필을 넣는 리스트
    private val dogKeyList = mutableListOf<String>() // 각 반려견의 키값을 넣는 리스트
    lateinit var walkDogReVAdapter: WalkDogReVAdapter
    lateinit var layoutManager: RecyclerView.LayoutManager
    private var isSelected : MutableMap<String, Boolean> = mutableMapOf() // 반려견이 선택되었는지 안 되었는지

    private lateinit var walkStartBtn: Button // 산책하기 버튼

    private lateinit var dogNameArea : TextView

    lateinit var retrofit : Retrofit

    private lateinit var mLocationManager: LocationManager
    private lateinit var mLocationListener: LocationListener

    private var mLocationUpdateState = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(R.layout.fragment_walk_play, container, false)

        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        setData(v!!)

        mLocationRequest =  LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY //높은 정확도
        }

        if (checkPermissionForLocation(v!!.context)) {
            startLocationUpdates()
        }

        refresh.setOnClickListener {
            startLocationUpdates()
        }

//        startLocationUpdates()

        //Create Retrofit Builder
        retrofit = Retrofit.Builder()
            .baseUrl(BaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        getFBDogData()

        walkDogReVAdapter.setItemClickListener(object: WalkDogReVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                if(isSelected[dogKeyList[position]] == false) { // 반려견이 선택되었을 경우
                    isSelected[dogKeyList[position]] = true // 선택되었음을 표시
                    getSelectedDogName(dogKeyList[position].trim())
                    Log.d("isSelected", isSelected.toString())
                } else if(isSelected[dogKeyList[position]] == true) { // 선택되었음에도 다시 선택했을 경우
                    isSelected[dogKeyList[position]] = false // 선택 취소되었음을 표시
                    removeSelectedDogName(dogKeyList[position].trim())
                    Log.d("isSelected", isSelected.toString())
                }
            }
        })

        walkStartBtn.setOnClickListener {
            var totalDogNum = isSelected.size

            var checkedNum = 0
            for((key, value) in isSelected) {
                if(value)
                    checkedNum += 1
            }

            if(checkedNum == 0) { // 산책할 반려견을 선택하지 않았을 경우
                Toast.makeText(v!!.context, "산책할 반려견을 하나 이상 선택하세요!", Toast.LENGTH_SHORT).show()
            } else if(checkedNum > 0) { // 산책할 반려견을 선택했을 경우
                val intent = Intent(v!!.context, StartWalkActivity::class.java)
                var checkedDogId = ArrayList<String>()
                var i = 0
                for((key, value) in isSelected) {
                    if(value) {
                        Log.d("checkedDogId", key)
                        checkedDogId.add(key)
                        i += 1
                    }
                }

                var dogIdString = Array(i) { _ -> "" }
                var index = 0
                for((key, value) in isSelected) {
                    if(value) {
                        dogIdString[index] = key
                        index += 1
                    }
                }

                Log.d("checkedDogId", checkedDogId.toString())
                Log.d("checkedDogId", dogIdString.contentToString())

                intent.putExtra("checkedDogIdList", dogIdString) // 산책할 반려견의 dogId 리스트를 넘겨줌
                startActivity(intent)
            }
        }

        return v
    }

    private fun setData(v : View) {

        locationArea = v.findViewById(R.id.locationArea)
        refresh = v.findViewById(R.id.refresh)
        weatherImage = v.findViewById(R.id.weatherImage)
        weatherArea = v.findViewById(R.id.weatherArea)
        nowTempArea = v.findViewById(R.id.nowTempArea)
        humidityArea = v.findViewById(R.id.humidityArea)
        windSpeedArea = v.findViewById(R.id.windSpeedArea)
        minTempArea = v.findViewById(R.id.minTempArea)
        maxTempArea = v.findViewById(R.id.maxTempArea)

        dogNameArea = v.findViewById(R.id.dogNameArea)

        walkStartBtn = v.findViewById(R.id.walkStartBtn)

        walkDogReVAdapter = WalkDogReVAdapter(walkDogReDataList)
        walkDogRecyclerView = v!!.findViewById(R.id.walkDogRecyclerView)
        walkDogRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        walkDogRecyclerView.layoutManager = layoutManager
        walkDogRecyclerView.adapter = walkDogReVAdapter
    }

    private fun getFBDogData() { // 파이어베이스로부터 반려견 프로필 데이터 불러오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                dogKeyList.clear()
                walkDogReDataList.clear()
                isSelected.clear()
                // dogDataList.clear() // 똑같은 데이터 복사 출력되는 것 막기 위한 초기화

                for(dataModel in dataSnapshot.children) {
                    Log.d(TAG, dataModel.toString())
                    val item = dataModel.getValue(DogModel::class.java)
                    walkDogReDataList.add(item!!)
                    dogKeyList.add(dataModel.key!!)
                    isSelected[dataModel.key!!] = false
                    Log.d("key", dogKeyList.toString())
                    // dogDataList.add(item!!)
                }

                walkDogReVAdapter.notifyDataSetChanged()

                Log.d(TAG, walkDogReDataList.toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dogRef.child(myUid).addValueEventListener(postListener)
    }

    private fun getSelectedDogName(dogId : String) { // 파이어베이스로부터 반려견 이름 불러오기
        Log.d("getSelectedDogName", dogId + "키")
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for(dataModel in dataSnapshot.children) {
                    Log.d(TAG, dataModel.toString())
                    val item = dataModel.getValue(DogModel::class.java)

                    if(item!!.dogId == dogId) {
                        var dogNames = dogNameArea.text.toString() + item!!.dogName + " "
                        dogNameArea.text = dogNames
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dogRef.child(myUid).addValueEventListener(postListener)
    }

    private fun removeSelectedDogName(dogId : String) { // 파이어베이스로부터 반려견 이름 불러와 지우기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for(dataModel in dataSnapshot.children) {
                    Log.d(TAG, dataModel.toString())
                    val item = dataModel.getValue(DogModel::class.java)

                    if(item!!.dogId == dogId) {
                        dogNameArea.text = dogNameArea.text.toString().replace(item!!.dogName, "")
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.dogRef.child(myUid).addValueEventListener(postListener)
    }

    private fun startLocationUpdates() {

        //FusedLocationProviderClient의 인스턴스를 생성.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // 기기의 위치에 관한 정기 업데이트를 요청하는 메서드 실행
        // 지정한 루퍼 스레드(Looper.myLooper())에서 콜백(mLocationCallback)으로 위치 업데이트를 요청
        mLocationRequest =  LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY //높은 정확도
        }

        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
    }

//    override fun onResume() {
//        super.onResume()
//        if (checkPermissionForLocation(requireContext())) {
//            startLocationUpdates()
//        }
//    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        mFusedLocationProviderClient?.removeLocationUpdates(mLocationCallback)
    }

    // 시스템으로부터 위치 정보를 콜백으로 받음
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {

            // 시스템에서 받은 location 정보를 onLocationChanged()에 전달
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }

    // 시스템으로 부터 받은 위치 정보를 화면에 갱신해주는 메소드
    fun onLocationChanged(location: Location) {
        mLastLocation = location

        val g = Geocoder(context)
        var address: MutableList<Address> = mutableListOf()

        try {
            address = g.getFromLocation(location.latitude, location.longitude, 200)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (address != null && address.isNotEmpty()) {
            if (address.size == 0) {
                Log.d("getLocation", "위치 찾기 오류")
            } else if(address != null && address.isNotEmpty() && address[0].getAddressLine(0).split(" ").size > 3 && address[0].getAddressLine(0).split(" ")[2].last() == '구') {
                Log.d("getLocation", address[0].toString())
                locationArea.text = address[0].adminArea + " " + address[0].getAddressLine(0).split(" ")[3]
            } else if(address != null && address.isNotEmpty() && address[0].getAddressLine(0).split(" ").size == 3) {
                locationArea.text = address[0].adminArea + " " + address[0].getAddressLine(0).split(" ")[2]
            }
        }

        lat = mLastLocation.latitude.toString() // 갱신된 위도
        lon  = mLastLocation.longitude.toString() // 갱신된 경도
        Log.d("getLocation", "$lat $lon")

        var nowTime = System.currentTimeMillis()
        val date = Date(nowTime)
        val mFormat = SimpleDateFormat("HH")
        val time = mFormat.format(date)
        hour = time.toString()
        Log.d("nowHour", time.toString())

        val service = retrofit.create(WeatherService::class.java)
        Log.d("getLocation", "$lat $lon")
        val call = service.getCurrentWeatherData(lat, lon, AppId)
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.d("getWeather", "result :" + t.message)
            }

            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if(response.code() == 200){
                    val weatherResponse = response.body()
                    Log.d("getWeather", "result: " + weatherResponse.toString())
                    var cTemp =  weatherResponse!!.main!!.temp - 273.15  //켈빈을 섭씨로 변환
                    var minTemp = weatherResponse!!.main!!.temp_min - 273.15
                    var maxTemp = weatherResponse!!.main!!.temp_max - 273.15
                    val stringBuilder =
                        "현재 기온: " + cTemp.toString().format("###.#") + "°\n" +
                                "최저 기온: " + minTemp + "\n" +
                                "최고 기온: " + maxTemp + "\n" +
                                "하늘" + weatherResponse!!.weather!![0].description + "\n" +
                                "풍속: " + weatherResponse!!.wind!!.speed+ "\n" +
                                "일출 시간: " + weatherResponse!!.sys!!.sunrise + "\n" +
                                "일몰 시간: " + weatherResponse!!.sys!!.sunset + "\n"+
                                "아이콘: " + weatherResponse!!.weather!![0].icon + "\n" +
                                "습도: " + weatherResponse!!.main!!.humidity + "%"

                    Log.d("weatherDescription", weatherResponse!!.weather!![0].description!!)
                    transferWeather(weatherResponse!!.weather!![0].description!!, hour)

                    val df = DecimalFormat("#.#")
                    df.roundingMode = RoundingMode.DOWN
                    val cTempOff = df.format(cTemp.toFloat())
                    nowTempArea.text = "$cTempOff°"

                    val minTempOff = df.format(minTemp.toFloat())
                    minTempArea.text = "$minTempOff°"

                    val maxTempOff = df.format(maxTemp.toFloat())
                    maxTempArea.text = "$maxTempOff°"

                    humidityArea.text = weatherResponse!!.main!!.humidity.toString() + "%"

                    windSpeedArea.text = weatherResponse!!.wind!!.speed.toString() + "m/s"
                }
            }
        })
    }

    private fun transferWeather(weather : String, hour : String) {
        when (weather) {
            "haze" -> {
                weatherArea.text = "안개"
                weatherImage.setImageResource(R.drawable.fog)
            }
            "fog" -> {
                weatherArea.text = "안개"
                weatherImage.setImageResource(R.drawable.fog)
            }
            "drizzle" -> {
                weatherArea.text = "이슬비"
                weatherImage.setImageResource(R.drawable.drizzle)
            }
            "light intensity drizzle" -> {
                weatherArea.text = "가벼운 이슬비"
                weatherImage.setImageResource(R.drawable.drizzle)
            }
            "heavy intensity drizzle" -> {
                weatherArea.text = "심한 이슬비"
                weatherImage.setImageResource(R.drawable.drizzle)
            }
            "light intensity drizzle rain" -> {
                weatherArea.text = "가벼운 이슬비가 동반된 비"
                weatherImage.setImageResource(R.drawable.drizzle)
            }
            "drizzle rain" -> {
                weatherArea.text = "이슬비가 동반된 비"
                weatherImage.setImageResource(R.drawable.drizzle)
            }
            "heavy intensity drizzle rain" -> {
                weatherArea.text = "심한 이슬비가 동반된 비"
                weatherImage.setImageResource(R.drawable.drizzle)
            }
            "shower rain and drizzle" -> {
                weatherArea.text = "소나기와 이슬비"
                weatherImage.setImageResource(R.drawable.drizzle)
            }
            "heavy shower rain and drizzle" -> {
                weatherArea.text = "심한 소나기와 이슬비"
                weatherImage.setImageResource(R.drawable.drizzle)
            }
            "shower drizzle" -> {
                weatherArea.text = "가벼운 소나기와 이슬비"
                weatherImage.setImageResource(R.drawable.drizzle)
            }
            "clouds" -> {
                weatherArea.text = "구름"
                weatherImage.setImageResource(R.drawable.clear_cloud)
            }
            "few clouds" -> {
                weatherArea.text = "구름 조금"
                weatherImage.setImageResource(R.drawable.clear_cloud)
            }
            "scattered clouds" -> {
                weatherArea.text = "구름 낌"
                weatherImage.setImageResource(R.drawable.clear_cloud)
            }
            "broken clouds" -> {
                weatherArea.text = "구름 많음"
                weatherImage.setImageResource(R.drawable.overcast)
            }
            "overcast clouds" -> {
                weatherArea.text = "구름 많음"
                weatherImage.setImageResource(R.drawable.overcast)
            }
            "clear sky" -> {
                weatherArea.text = "맑음"
                if(hour.toInt() >= 18)
                    weatherImage.setImageResource(R.drawable.night)
                else
                    weatherImage.setImageResource(R.drawable.clear)
            }
            "shower rain" -> {
                weatherArea.text = "소나기"
                if(hour.toInt() >= 18)
                    weatherImage.setImageResource(R.drawable.shower)
                else
                    weatherImage.setImageResource(R.drawable.shower_rain)
            }
            "rain" -> {
                weatherArea.text = "비"
                if(hour.toInt() >= 18)
                    weatherImage.setImageResource(R.drawable.dark_rain)
                else
                    weatherImage.setImageResource(R.drawable.rain)
            }
            "light rain" -> {
                weatherArea.text = "가벼운 비"
                if(hour.toInt() >= 18)
                    weatherImage.setImageResource(R.drawable.dark_rain)
                else
                    weatherImage.setImageResource(R.drawable.rain)
            }
            "moderate rain" -> {
                weatherArea.text = "보통 비"
                if(hour.toInt() >= 18)
                    weatherImage.setImageResource(R.drawable.dark_rain)
                else
                    weatherImage.setImageResource(R.drawable.rain)
            }
            "heavy intensity rain" -> {
                weatherArea.text = "심한 비"
                weatherImage.setImageResource(R.drawable.heavy_rain)
            }
            "very heavy rain" -> {
                weatherArea.text = "매우 심한 비"
                weatherImage.setImageResource(R.drawable.heavy_rain)
            }
            "extreme rain" -> {
                weatherArea.text = "극심한 비"
                weatherImage.setImageResource(R.drawable.heavy_rain)
            }
            "freezing rain" -> {
                weatherArea.text = "우박"
                weatherImage.setImageResource(R.drawable.hailstorm)
            }
            "light intensity shower rain" -> {
                weatherArea.text = "가벼운 소나기"
                if(hour.toInt() >= 18)
                weatherImage.setImageResource(R.drawable.dark_rain)
                else
                    weatherImage.setImageResource(R.drawable.shower_rain)
            }
            "ragged shower rain" -> {
                weatherArea.text = "불규칙한 소나기"
                if(hour.toInt() >= 18)
                    weatherImage.setImageResource(R.drawable.dark_rain)
                else
                    weatherImage.setImageResource(R.drawable.shower_rain)
            }
            "thunderstorm" -> {
                weatherArea.text = "뇌우"
                if(hour.toInt() >= 18)
                    weatherImage.setImageResource(R.drawable.night_thunderstorm)
                else
                    weatherImage.setImageResource(R.drawable.thunderstorm)
            }
            "thunderstorm with light rain" -> {
                weatherArea.text = "가벼운 비가 동반된 천둥번개"
                if(hour.toInt() >= 18)
                    weatherImage.setImageResource(R.drawable.night_thunderstorm)
                else
                    weatherImage.setImageResource(R.drawable.thunderstorm)
            }
            "thunderstorm with rain" -> {
                weatherArea.text = "비를 동반한 천둥번개"
                if(hour.toInt() >= 18)
                    weatherImage.setImageResource(R.drawable.night_thunderstorm)
                else
                    weatherImage.setImageResource(R.drawable.thunderstorm)
            }
            "thunderstorm with heavy rain" -> {
                weatherArea.text = "심한 비를 동반한 천둥번개"
                if(hour.toInt() >= 18)
                    weatherImage.setImageResource(R.drawable.night_thunderstorm)
                else
                    weatherImage.setImageResource(R.drawable.thunderstorm)
            }
            "light thunderstorm" -> {
                weatherArea.text = "약한 천둥번개"
                if(hour.toInt() >= 18)
                    weatherImage.setImageResource(R.drawable.thunder)
                else
                    weatherImage.setImageResource(R.drawable.thunder)
            }
            "heavy thunderstorm" -> {
                weatherArea.text = "심한 천둥번개"
                if(hour.toInt() >= 18)
                    weatherImage.setImageResource(R.drawable.thunder)
                else
                    weatherImage.setImageResource(R.drawable.thunder)
            }
            "ragged thunderstorm" -> {
                weatherArea.text = "불규칙한 천둥번개"
                if(hour.toInt() >= 18)
                    weatherImage.setImageResource(R.drawable.thunder)
                else
                    weatherImage.setImageResource(R.drawable.thunder)
            }
            "thunderstorm with light drizzle" -> {
                weatherArea.text = "가벼운 이슬비를 동반한 천둥번개"
                if(hour.toInt() >= 18)
                    weatherImage.setImageResource(R.drawable.night_thunderstorm)
                else
                    weatherImage.setImageResource(R.drawable.thunderstorm)
            }
            "thunderstorm with drizzle" -> {
                weatherArea.text = "이슬비를 동반한 천둥번개"
                if(hour.toInt() >= 18)
                    weatherImage.setImageResource(R.drawable.night_thunderstorm)
                else
                    weatherImage.setImageResource(R.drawable.thunderstorm)
            }
            "thunderstorm with heavy drizzle" -> {
                weatherArea.text = "심한 이슬비를 동반한 천둥번개"
                if(hour.toInt() >= 18)
                    weatherImage.setImageResource(R.drawable.night_thunderstorm)
                else
                    weatherImage.setImageResource(R.drawable.thunderstorm)
            }
            "snow" -> {
                weatherArea.text = "눈"
                weatherImage.setImageResource(R.drawable.snows)
            }
            "mist" -> {
                weatherArea.text = "엷은 안개"
                if(hour.toInt() >= 18)
                    weatherImage.setImageResource(R.drawable.haze)
                else
                    weatherImage.setImageResource(R.drawable.mist)
            }
       }
    }

    // 위치 권한이 있는지 확인하는 메서드
    private fun checkPermissionForLocation(context: Context): Boolean {
        // Android 6.0 Marshmallow 이상에서는 위치 권한에 추가 런타임 권한이 필요
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                // 권한이 없으므로 권한 요청 알림 보내기
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_LOCATION)
                false
            }
        } else {
            true
        }
    }

    // 사용자에게 권한 요청 후 결과에 대한 처리 로직
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()

            } else {
                Log.d("ttt", "onRequestPermissionsResult() _ 권한 허용 거부")
                Toast.makeText(context, "권한이 없어 해당 기능을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

interface WeatherService{

    @GET("weather?")
    fun getCurrentWeatherData(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") appid: String) :
            Call<WeatherResponse>
}

class WeatherResponse {
    @SerializedName("weather") var weather = ArrayList<Weather>()
    @SerializedName("main") var main: Main? = null
    @SerializedName("wind") var wind : Wind? = null
    @SerializedName("sys") var sys: Sys? = null
}

class Weather {
    @SerializedName("id") var id: Int = 0
    @SerializedName("main") var main : String? = null
    @SerializedName("description") var description: String? = null
    @SerializedName("icon") var icon : String? = null
}

class Main {
    @SerializedName("temp")
    var temp: Float = 0.toFloat()
    @SerializedName("humidity")
    var humidity: Float = 0.toFloat()
    @SerializedName("pressure")
    var pressure: Float = 0.toFloat()
    @SerializedName("temp_min")
    var temp_min: Float = 0.toFloat()
    @SerializedName("temp_max")
    var temp_max: Float = 0.toFloat()

}

class Wind {
    @SerializedName("speed")
    var speed: Float = 0.toFloat()
    @SerializedName("deg")
    var deg: Float = 0.toFloat()
}

class Sys {
    @SerializedName("country")
    var country: String? = null
    @SerializedName("sunrise")
    var sunrise: Long = 0
    @SerializedName("sunset")
    var sunset: Long = 0
}
