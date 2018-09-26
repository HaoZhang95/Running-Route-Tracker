package com.example.ahao9.running.activities

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.example.ahao9.running.R
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.android.synthetic.main.activity_running.*
import org.jetbrains.anko.toast

class RunningActivity : Activity(), OnMapReadyCallback {

    private val myClickListener = View.OnClickListener {
        when(it) {
            iv_pause -> { toast("pause") }
            iv_continue -> { }
            iv_end -> { }
            iv_settings -> { }
            iv_lock -> { }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_running)

        mapInRunningActivity.onCreate(savedInstanceState);
        mapInRunningActivity.getMapAsync(this)

        iv_pause.setOnClickListener(myClickListener)
        iv_continue.setOnClickListener(myClickListener)
        iv_end.setOnClickListener(myClickListener)
        iv_settings.setOnClickListener(myClickListener)
        iv_lock.setOnClickListener(myClickListener)
    }



    override fun onMapReady(googleMap: GoogleMap) {
        val position = LatLng(37.77493, -122.41942)
        googleMap.addMarker(MarkerOptions().position(position).title("Marker in Sydney"))

        //zoom to position with level 16
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 10f)
        googleMap.animateCamera(cameraUpdate)
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mapInRunningActivity.onSaveInstanceState(outState)
    }


    override fun onResume() {
        mapInRunningActivity.onResume()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapInRunningActivity.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapInRunningActivity.onDestroy()
    }

}
