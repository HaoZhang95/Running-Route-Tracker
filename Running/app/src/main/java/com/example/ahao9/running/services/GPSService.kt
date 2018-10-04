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





/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 20:03 2018/10/4
 * @ Description：Build for Metropolia project
 */
class GPSService: Service() {

    private var listener: LocationListener? = null
    private lateinit var locationManager: LocationManager

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {

        listener = object : LocationListener {

            override fun onLocationChanged(location: Location) {
                val i = Intent("location_update")
                i.putExtra("coordinates", "${location.longitude} \n${location.latitude}")
                sendBroadcast(i)
            }

            override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {

            }

            override fun onProviderEnabled(s: String) {

            }

            override fun onProviderDisabled(s: String) {
                val i = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(i)
            }
        }

        locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000.toLong(), 0f, listener)

    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(listener);
    }

}