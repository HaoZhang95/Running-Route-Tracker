package com.example.ahao9.running.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.LocationListener
import android.os.IBinder
import android.location.LocationManager
import android.location.Location
import android.provider.Settings;
import android.os.Bundle
import android.location.GpsStatus
import android.location.GpsStatus.*
import android.util.Log


/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 20:03 2018/10/4
 * @ Description：Build for Metropolia project
 *
 * GPS Thretical background learned from this blog:
 * https://blog.csdn.net/leng_wen_rou/article/details/53100284
 */
class GPSService: Service() {

    private var listener: LocationListener? = null
    private lateinit var locationManager: LocationManager
    private var intensity:Int = 0

    companion object {
        private const val TIME_INTERVAL:Long = 3000     // gps update time interval
        private const val MIN_DISTANCE:Float = 0f    // min distance change for gps update
    }

    val TAG = "hero"

    /**
     * onGpsStatusChanged() to listen the count of current connected Satellites
     * Not all connected Satellites can be used for location
     * normally only those Satellites with snr >= 30 can be regarded as a valid Satellites
     * if the valid Satellites >= 4, it represents gps has a strong signal
     * snr --- SIGNAL-NOISE RATIO
     */
    inner class GPSListener: GpsStatus.Listener {
        @SuppressLint("MissingPermission")
        override fun onGpsStatusChanged(event: Int) {
            when(event){
                GPS_EVENT_SATELLITE_STATUS -> {

                    val gpsStatus = locationManager.getGpsStatus(null)
                    val maxSatellites = gpsStatus.maxSatellites
                    val iters = gpsStatus.satellites.iterator()
                    var count = 0
                    while (iters.hasNext() && count <= maxSatellites) {
                        val temp =  iters.next();
                        val snr = temp.snr
                        Log.d(TAG,"Snr: ${snr}")

                        if(temp.snr > 30) {
                            count++;
                            if (count >= 4) {
                                intensity = 3
                            } else if (count >= 2 ) {
                                intensity = 2
                            } else if (count == 1){
                                intensity = 1
                            } else {
                                intensity = 0
                            }
                        }
                    }
                    Log.d(TAG, "There are ：$count valid Satellites  max :$maxSatellites");
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * onLocationChanged() to listener location change
     * when location changes, we sendbroadcast to homeFragment, so that homefragment can update ui based on
     * newly location and gps intensity
     */
    @SuppressLint("MissingPermission")
    override fun onCreate() {

        listener = object : LocationListener {

            override fun onLocationChanged(location: Location) {
                val i = Intent("location_update")
                i.putExtra("coordinates", "${location.longitude} --> ${location.latitude}")
                i.putExtra("gps", intensity)
                sendBroadcast(i)
            }

            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

            }

            override fun onProviderEnabled(p0: String?) {

            }

            override fun onProviderDisabled(s: String) {
                val i = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(i)
            }
        }

        locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_INTERVAL,
                MIN_DISTANCE, listener)

        locationManager.addGpsStatusListener(GPSListener())
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(listener);
    }

}