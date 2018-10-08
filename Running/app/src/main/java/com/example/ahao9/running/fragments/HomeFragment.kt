package com.example.ahao9.running.fragments

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Chronometer
import android.widget.RelativeLayout
import com.example.ahao9.running.R
import com.example.ahao9.running.activities.LockScreenActivity
import com.example.ahao9.running.activities.RouteRecordActivity
import com.example.ahao9.running.model.MyLatLng
import com.example.ahao9.running.model.RunningRecordEntity
import com.example.ahao9.running.services.GPSService
import com.example.ahao9.running.utils.Tools
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.home_layout.*
import kotlinx.android.synthetic.main.trace_running_layout.*
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 16:42 2018/9/30
 * @ Description：Build for Metropolia project
 */
class HomeFragment : Fragment(), OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private var countNum = 2
    private var runningBottomStartedheight = 0
    private var runningTime: Long = 0
    private var startRunningTime: Long = 0
    private var stopRunningTime: Long = 0
    private var altitude: Double = 0.00
    private var distance: Double = 0.00
    private var avgSpeed: Double = 0.00

    private var isPause = false
    private val aniTime: Long = 300
    private var runningType = 1

    private lateinit var mMap: GoogleMap
    private lateinit var tvChronometer: Chronometer
    private lateinit var myView: View

    private var broadcastReceiver: BroadcastReceiver? = null

    private var TAG = "hero"
    private var isRunning = false

    companion object {
        const val DEFAULT_ZOOM_LEVEL = 15f
    }

    private var lineOptions = PolylineOptions()
    private lateinit var mDatabaseRef: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        myView = inflater.inflate(R.layout.home_layout, container, false)
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViewClickListeners()
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("RunningPath")

        val mLayoutParams = mapView.layoutParams as RelativeLayout.LayoutParams
        mLayoutParams.bottomMargin = Tools.transferDipToPx(200)
        mapView.layoutParams = mLayoutParams

        // setUp gps service to trace
        if (!runtimePermissions()) {
            val i = Intent(context!!.applicationContext, GPSService::class.java)
            context!!.startService(i)
        }
    }

    private fun runtimePermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= 23
                && ContextCompat.checkSelfPermission(
                        context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)

            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                val i = Intent(context!!.applicationContext, GPSService::class.java)
                context!!.startService(i)
            } else {
                runtimePermissions();
            }
        }
    }

    /**
     * Setup map components
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try {
            mMap.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            Log.e(TAG, e.message)
        }

        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true

        mMap.addPolyline(lineOptions);
        val zoom = CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_LEVEL)
        mMap.animateCamera(zoom)
    }

    /**
     * setUp a bunch of View ClickListeners
     */
    private fun setUpViewClickListeners() {

        this.tvStartRunning.setOnClickListener {
            CountLayout.visibility = View.VISIBLE
            val scaleAnimation = ScaleAnimation(3f, 0.5f, 3f, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            scaleAnimation.duration = 1000
            scaleAnimation.startTime = 100
            scaleAnimation.repeatCount = 2
            tvTimer.startAnimation(scaleAnimation)

            /**
             * start a timer
             */
            scaleAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }
                override fun onAnimationEnd(animation: Animation) {
                    CountLayout.visibility = View.GONE
                    countNum = 2
                    tvTimer.text = "3"
                    startRunning()
                }
                override fun onAnimationRepeat(animation: Animation) {
                    tvTimer.text = "$countNum"
                    countNum--
                }
            })
        }

        /**
         * scale layout to make map larger
         */
        llRunningBottomLayoutTopPart.setOnClickListener {
            if (llRunningBottomLayoutBottomPart.layoutParams.height != 0) {
                (mapView.layoutParams as RelativeLayout.LayoutParams).bottomMargin = llRunningBottomLayoutTopPart.height
                beginAnimateInY(runningBottomStartedheight - Tools.transferDipToPx(80), 0)
            } else {

                mapView.postDelayed(Runnable {
                    (mapView.layoutParams as RelativeLayout.LayoutParams).bottomMargin = runningBottomStartedheight
                }, aniTime)
                beginAnimateInY(0, runningBottomStartedheight - Tools.transferDipToPx(80))
            }
        }

        tvRunningStop.setOnClickListener { onStopSport() }

        /**
         * time count
         */
        tvChronometer = this.view!!.findViewById<Chronometer>(R.id.tvRunningTime)
        tvChronometer.onChronometerTickListener = Chronometer.OnChronometerTickListener { chronometer ->
            runningTime = SystemClock.elapsedRealtime() - chronometer.base
        }

        /*
         * pause running
         */
        tvRunningPause.setOnClickListener {
            tvRunningStop.isClickable = false
            tvRunningLock.isClickable = false
            tvRunningPause.isClickable = false
            isPause = !isPause
            tvRunningPause.text = if (isPause) "Continue" else "Pause"
            if (isPause) {
                tvChronometer.stop()
                beginAnimateInX(0, tvRunningPause.left - tvRunningLock.left)

                isRunning = false
            } else {
                tvChronometer.base = SystemClock.elapsedRealtime() - runningTime
                tvChronometer.start()
                beginAnimateInX(tvRunningLock.left - tvRunningLock.left, 0)

                isRunning = true
            }
        }

        /**
         * switch running type, 1- represents running  2- represents cycling
         */
        ivRunningType.setOnClickListener {
            ivRunningType.setImageResource(R.drawable.trance_run)
            ivBikeType.setImageResource(R.drawable.trance_bike2)
            runningType = 1
            updateTvBasedOnSportType(runningType)
        }

        ivBikeType.setOnClickListener {
            ivRunningType.setImageResource(R.drawable.trance_run2)
            ivBikeType.setImageResource(R.drawable.trance_bike)
            runningType = 2
            updateTvBasedOnSportType(runningType)
        }

        /**
         * lock screen
         */
        tvRunningLock.setOnClickListener { startActivity<LockScreenActivity>() }
    }

    private fun updateTvBasedOnSportType(mSportType: Int) {
        var tip = ""
        if (mSportType == 1) {
            tip = "Running type selected"
        } else {
            tip = "Cycling type selected"
        }
        toast(tip)
    }

    /**
     * layout animation for y-axis
     *
     * @param y1
     * @param y2
     */
    private fun beginAnimateInY(y1: Int, y2: Int) {
        val mAnimator = ValueAnimator.ofInt(y1, y2)
        tvRunningTime.stop()
        rlRunningBottomLayout.isClickable = false
        mAnimator.addUpdateListener { animation ->
            llRunningBottomLayoutBottomPart.layoutParams.height = animation.animatedValue as Int
            llRunningBottomLayoutBottomPart.requestLayout()
        }
        mAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                rlRunningBottomLayout.isClickable = true
                if (!isPause) {
                    tvChronometer.start()
                }
            }
        })
        mAnimator.setDuration(aniTime).start()
    }

    /**
     * layout animation for x-axis
     * Restore animation by pressing pause button
     * @param x1  start x position
     * @param x2  end x position
     */
    private fun beginAnimateInX(x1: Int, x2: Int) {
        val mAnimator = ValueAnimator.ofInt(x1, x2)
        mAnimator.addUpdateListener { animation ->
            tvRunningLock.translationX = (animation.animatedValue as Int).toFloat()
            tvRunningLock.requestLayout()
        }
        mAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                tvRunningStop.isClickable = true
                tvRunningLock.isClickable = true
                tvRunningPause.isClickable = true
            }
        })
        mAnimator.setDuration(aniTime).start()
    }

    /**
     * stop running and uploading a bunch of coordinate to web firebase database
     */
    private fun onStopSport() {
        val builder = AlertDialog.Builder(context!!)
        builder.setMessage("Confirm the completion of this exercise?")
        builder.setPositiveButton("Done") { _, _ ->

            resetTvInfo()
            val i = Intent(context!!.applicationContext, GPSService::class.java)
            context!!.stopService(i)

            isRunning = false
            val zoom = CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_LEVEL)
            mMap.animateCamera(zoom)
            mMap.clear()

            // begin to upload route to firebase
            stopRunningTime = System.currentTimeMillis()
            uploadRunningRoute()
        }
        builder.setNegativeButton("Continue Running") { _, _ -> }
        builder.create().show()
    }

    private var pointsList = mutableListOf<MyLatLng>()
    private fun uploadRunningRoute() {

        for (temp in lineOptions.points) {
            val point = MyLatLng(temp.latitude, temp.longitude)
            pointsList.add(point)
        }
        val pathID = mDatabaseRef.push().key
        val pathEntity = RunningRecordEntity(runningType, distance, startRunningTime, stopRunningTime,
                runningTime, avgSpeed, altitude, pointsList)
        mDatabaseRef.child(pathID).setValue(pathEntity).addOnSuccessListener{
            toast("Save data successfully")
            startActivity<RouteRecordActivity>("path" to pathEntity )
        }.addOnFailureListener {
            toast(it.message.toString())
        }
    }

    private fun startRunning() {
        isRunning = true
        tvChronometer.base = SystemClock.elapsedRealtime()
        tvChronometer.start()
        startRunningTime = System.currentTimeMillis()

        rlRunningBottomLayoutStarted.visibility = View.VISIBLE
        rlRunningBottomLayout.visibility = View.GONE
        if (runningBottomStartedheight == 0) {
            runningBottomStartedheight = rlRunningBottomLayoutStarted.bottom - rlRunningBottomLayoutStarted.top
        }
        (mapView.layoutParams as RelativeLayout.LayoutParams).bottomMargin = runningBottomStartedheight

        // setUp polyline style
        lineOptions.width(10f);
        lineOptions.color(resources.getColor(R.color.colorPrimary))
    }

    /**
     * Reset textviews to default value
     */
    private fun resetTvInfo() {
        tvRunningAltitude.text = getString(R.string.altitude)
        tvRunningDistance.text = getString(R.string.distance)
        tvRunningAverageSpeed.text = getString(R.string.avgSpeed)
        tvChronometer.stop()

        rlRunningBottomLayout.visibility = View.VISIBLE
        rlRunningBottomLayoutStarted.visibility = View.GONE

        tvRunningPause.text = getString(R.string.pause)
        tvRunningLock.translationX = 0f
        tvRunningLock.requestLayout()

        val mLayoutParams = mapView.layoutParams as RelativeLayout.LayoutParams
        mLayoutParams.bottomMargin = Tools.transferDipToPx(200)
        mapView.layoutParams = mLayoutParams
    }

    override fun onMyLocationClick(location: Location) {
        Log.e(TAG, "latitude: ${location.latitude} --- longitude: ${location.longitude}")
    }

    /**
     * Return false so that we don't consume the event and the default behavior still occurs
     * (the camera animates to the user's current position).
     */
    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    /**
     * Utilizing Service and Broadcast Receiver to update latLng
     */
    override fun onResume() {
        mapView.onResume()
        super.onResume()

        if (broadcastReceiver == null) {
            broadcastReceiver = object : BroadcastReceiver() {
                @SuppressLint("SetTextI18n")
                override fun onReceive(context: Context, intent: Intent) {

                    val location = intent.extras.getParcelable<Location>("locationInfo")
                    val gpsIntensity = intent.extras.getInt("gpsIntensity")
                    val latLng = LatLng(location.latitude, location.longitude)

                    if (isRunning) {
                        mMap.addPolyline(lineOptions.add(latLng))

                        altitude = location.altitude

                        val size = lineOptions.points.size
                        if (size > 1) {
                            val a = lineOptions.points[size - 1]
                            val b = lineOptions.points[size - 2]
                            distance += Tools.getDistance(b.latitude,b.longitude, a.latitude,a.longitude)
                        } else {
                            distance = 0.00
                        }

                        avgSpeed = distance / (runningTime / 1000)  // unit: m/s
                        tvRunningAltitude.text = Tools.getSimpleDecimal(altitude)
                        tvRunningDistance.text = "${Tools.getSimpleDecimal(distance/1000)}KM"
                        tvRunningAverageSpeed.text =Tools.getSimpleDecimal(avgSpeed)

                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                    Log.d(TAG, "isrunning:${isRunning} lat: ${location.latitude} --- lng: ${location.longitude} --- gpsIntensity: $gpsIntensity")
                    Log.d(TAG, "isrunning:${isRunning} speed: ${location.speed} --- altitude: ${location.altitude}")
                    when (gpsIntensity) {
                        3 -> {
                            tvGpsView.text = "GPS ★★★"
                        }
                        2 -> {
                            tvGpsView.text = "GPS ★★☆"
                        }
                        1 -> {
                            tvGpsView.text = "GPS ★☆☆"
                        }
                        0 -> {
                            tvGpsView.text = "GPS ☆☆☆"
                        }
                    }
                }
            }
        }
        context!!.registerReceiver(broadcastReceiver, IntentFilter("location_update"))
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (broadcastReceiver != null) {
            context!!.unregisterReceiver(broadcastReceiver);
        }
    }
}