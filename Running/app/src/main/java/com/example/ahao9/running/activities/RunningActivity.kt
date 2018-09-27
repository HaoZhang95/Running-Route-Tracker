package com.example.ahao9.running.activities

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import com.example.ahao9.running.R
import com.example.ahao9.running.utils.RunningDataUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.android.synthetic.main.activity_running.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class RunningActivity : Activity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener {

    private var isStop = false
    private var isPause = false
    private var endTime:Long = 0
    private var startTime:Long = 0
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var currentLatLng: Location? = null
    private lateinit var mMap: GoogleMap

    private var TAG = "hero"

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

        startTime = System.currentTimeMillis();
        myTimer.start()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        // getCurrentLocation()
    }

    /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(){
        try {
            mFusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    currentLatLng = location
                    val position = LatLng(location.latitude, location.longitude)
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 10f)
                    mMap.animateCamera(cameraUpdate)
                    // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 12f))
                }
            }
        } catch(e: SecurityException)  {
            Log.e(TAG, e.message);
        }
    }

    private val myClickListener = View.OnClickListener {
        when(it) {
            iv_pause -> {
                // start animation, stop counting time and drawing line on the map
                isPause = true
                startAnimator()
            }
            iv_continue -> {
                // stop animation, continue counting time and drawing line on the map
                isPause = false
                endAnimator()
            }
            iv_end -> {
                isPause = true
                endTime = System.currentTimeMillis()
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Confirm the completion of this exercise?")
                builder.setPositiveButton("Done") { _, _ ->
                    isStop = true
                    toast("${tv_mileage.text} km")

                    // saverecord(record)

                    // val today_record = getTodayData()
                    // val intent = Intent(baseContext, RecordShowActivity::class.java)
                    // intent.putExtra("recorditem", today_record)
                    // startActivity(intent)
                }
                builder.setNegativeButton("Continue Running") { _, _ ->
                    isPause = false
                    endAnimator()
                }
                builder.create().show()
            }
            iv_settings -> { }
            iv_lock -> { startActivity<LockScreenActivity>() }
        }
    }

    private var second: Int = 0
    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1 -> {
                    second++
                    tv_time.text = RunningDataUtil.getTime(second)
                }
            }
        }
    }

    private var myTimer = Thread(Runnable {
        while (!isStop) {
            if (!isPause) {
                try {
                    Thread.sleep(1000)
                } catch (e: Exception) {
                    Log.e(TAG, "Timer thread error")
                }

                handler.sendEmptyMessage(1)
            }
        }
    })
    private lateinit var animator3: ObjectAnimator
    private lateinit var animator4: ObjectAnimator
    private fun endAnimator() {
        val x1 = 0
        val x2 = 0
        val px = PropertyValuesHolder.ofFloat("translationX", x1.toFloat())
        animator3 = ObjectAnimator.ofPropertyValuesHolder(iv_continue, px)
        animator3.setDuration(300).start()

        val px2 = PropertyValuesHolder.ofFloat("translationX", x2.toFloat())
        animator4 = ObjectAnimator.ofPropertyValuesHolder(iv_end, px2)
        animator4.setDuration(300).start()
        animator4.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {

                iv_continue.visibility = View.GONE
                iv_end.visibility = View.GONE
                iv_pause.visibility = View.VISIBLE
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
    }

    private lateinit var animator1: ObjectAnimator
    private lateinit var animator2: ObjectAnimator
    private fun startAnimator() {

        iv_pause.visibility = View.GONE
        iv_continue.visibility = View.VISIBLE
        iv_end.visibility = View.VISIBLE
        val x1 = -300
        val x2 = 300
        val px = PropertyValuesHolder.ofFloat("translationX", x1.toFloat())
        animator1 = ObjectAnimator.ofPropertyValuesHolder(iv_continue, px)
        animator1.setDuration(200).start()

        val px2 = PropertyValuesHolder.ofFloat("translationX", x2.toFloat())
        animator2 = ObjectAnimator.ofPropertyValuesHolder(iv_end, px2)
        animator2.setDuration(200).start()
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
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

    /**
     * comment the following line, to stop user pressing back button
     */
    override fun onBackPressed() {
        // super.onBackPressed()
    }

}
