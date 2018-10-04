package com.example.ahao9.running.activities

import android.annotation.SuppressLint
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import com.example.ahao9.running.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_route_record.*

class RouteRecordActivity : AppCompatActivity(), OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener  {

    private lateinit var mMap: GoogleMap
    private val TAG = "hero"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_route_record)

        recordMapView.onCreate(savedInstanceState)
        recordMapView.getMapAsync(this)

        setUpToolbar()
        setUpData()
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        try {
            mMap.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            Log.e(TAG, e.message)
        }
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true

        getCurrentLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(){
        try {
            val sydney = LatLng(-34.0, 151.0)
            mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        } catch(e: SecurityException)  {
            Log.e(TAG, e.message)
        }
    }
    private fun setUpData() {
        // real data shuold come with Intent obj, for now using fake data
        tvRecordLength.text = "2.4 KM"
        tvRecordTime.text = "0:24:59"
        tvRecordDate.text = "04/10/2018"
        tvRecordSpeed.text = "3.07"
        tvRecordAltitude.text = "123"
    }

    private fun setUpToolbar() {
        setSupportActionBar(routeToolbar)
        routeToolbar.setNavigationOnClickListener(View.OnClickListener { finish() })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        recordMapView.onSaveInstanceState(outState)
    }

    override fun onMyLocationClick(location: Location) {

    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    override fun onResume() {
        recordMapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        recordMapView.onPause()
    }
}
