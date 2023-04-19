package org.techtown.myproject.walk

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat


//현재 위치를 찾아 주소로 변환 처리하는 클래스
class GpsTracker(private val mContext: Context) : Service(), LocationListener {
    private var location: Location? = null
    private var latitude = 0.0
    private var longitude = 0.0
    protected var locationManager: LocationManager? = null

    /**
     * Provider
     * 위성을 이용하는 방식 : GPS_PROVIDER
     * 네트워크를 이용하는 방식 : NETWORK_PROVIDER
     * locationManager 서비스를 이용하여 위치가 변경될 때
     */
    fun getLocation(): Location? {
        try {

            //locationManager 객체 참조
            locationManager = mContext.getSystemService(LOCATION_SERVICE) as LocationManager

            //GPS프로바이더와 네트워크 프로바이더 사용가능 여부 변수 선언
            val isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled =
                locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isGPSEnabled && !isNetworkEnabled) {
            } else {
                /**
                 * 안드로이드 6.0부터는 개인정보에 미칠 수 있는 장치들이나 자료등의 접근을 위해서 실시간으로 권한을 획득해야 한다.
                 * ContextCompat.checkSelfPermission으로 권한 설정을 해야한다.
                 * 권한 획득 후 PackageManager.PERMISSION_GRANTED를 받환받아야 한다.
                 */

                //GPS와 네트워크로부터 권한을 받기 위한 변수 선언
                val hasFineLocationPermission = ContextCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )


                //PackageManager.PERMISSION_GRANTED 앱이 필요한 실행을 할 수 있도록 도와준다.
                if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                    hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED
                ) {
                } else return null


                //네트워크 프로바이더
                if (isNetworkEnabled) {
                    //위치 정보를 원하는 시간과 거리 조건을 만족했을 때 실행해준다.
                    locationManager!!.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
                        this
                    )
                    if (locationManager != null) {
                        //가장 최근의 위치정보를 가져온다.
                        location =
                            locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (location != null) {
                            //위도,경도 값을 가져온다.
                            latitude = location!!.latitude
                            longitude = location!!.longitude
                        }
                    }
                }


                //GPS 프로바이더
                if (isGPSEnabled) {
                    if (location == null) {
                        //위치 정보를 원하는 시간과 거리 조건을 만족했을 때 실행해준다.
                        locationManager!!.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
                            (this as LocationListener)
                        )
                        if (locationManager != null) {
                            //가장 최근의 위치정보를 가져온다.
                            location =
                                locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            if (location != null) {
                                //위도,경도 값을 가져온다.
                                latitude = location!!.latitude
                                longitude = location!!.longitude
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("@@@", "" + e.toString())
        }
        return location
    }

    fun getLatitude(): Double {
        if (location != null) {
            latitude = location!!.latitude
        }
        return latitude
    }

    fun getLongitude(): Double {
        if (location != null) {
            longitude = location!!.longitude
        }
        return longitude
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**onLocationChanged
     * 위치가 바뀔 때 새로운 위치값을 받아올 수 있도록 한다.
     */
    override fun onLocationChanged(location: Location) {
        if (location != null) {
            latitude = location.latitude
            longitude = location.longitude
        }
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
    override fun onProviderEnabled(s: String) {}
    override fun onProviderDisabled(s: String) {}

    companion object {
        //거리가 변경되었을 때 위치정보를 받을 수있다.
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10

        //일정 시간이 지나면 위치정보를 받을 수 있다.
        private const val MIN_TIME_BW_UPDATES = (1000 * 60 * 1).toLong()
    }

    init {
        getLocation()
    }
}