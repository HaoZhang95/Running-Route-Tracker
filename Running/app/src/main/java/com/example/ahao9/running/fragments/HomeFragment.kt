package com.example.ahao9.running.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.app.Fragment
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
import com.example.ahao9.running.utils.Tools
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.home_layout.*
import kotlinx.android.synthetic.main.trace_running_layout.*
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 16:42 2018/9/30
 * @ Description：Build for Metropolia project
 */
class HomeFragment: Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private var countNum = 2
    private var runningBottomStartedheight = 0
    private var runningTime:Long = 0
    private var isPause = false
    private val aniTime: Long = 300
    private var runningType = 1

    private lateinit var mMap: GoogleMap
    private lateinit var tvChronometer: Chronometer
    private lateinit var myView: View
    private var TAG = "hero"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        myView = inflater.inflate(R.layout.home_layout,container,false)
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setUpViewClickListeners()
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        val mLayoutParams = mapView.layoutParams as RelativeLayout.LayoutParams
        mLayoutParams.bottomMargin = Tools.transferDipToPx(200)
        mapView.layoutParams = mLayoutParams
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

    /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
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

    /**
     * setUp a bunch of View ClickListeners
     */
    private fun setUpViewClickListeners(){

        this.tvStartRunning.setOnClickListener {
            CountLayout.visibility = View.VISIBLE
            val scaleAnimation = ScaleAnimation(3f, 0.5f, 3f, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            scaleAnimation.duration = 1000
            scaleAnimation.startTime = 100
            scaleAnimation.repeatCount = 2
            tvTimer.startAnimation(scaleAnimation)

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

        tvChronometer = this.view!!.findViewById<Chronometer>(R.id.tvRunningTime)

        tvChronometer.onChronometerTickListener = Chronometer.OnChronometerTickListener {
            chronometer -> runningTime = SystemClock.elapsedRealtime() - chronometer.base
        }

        /*
         *
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
            } else {
                tvChronometer.base = SystemClock.elapsedRealtime() - runningTime
                tvChronometer.start()
                beginAnimateInX(tvRunningLock.left - tvRunningLock.left, 0)
            }
        }

        /**
         *
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
         * lock
         */
        tvRunningLock.setOnClickListener { startActivity<LockScreenActivity>() }
    }

    private fun updateTvBasedOnSportType(mSportType: Int) {
        toast("runningType: $mSportType")
    }

    /**
     * 开始运动后信息布局移动动画
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

    private fun onStopSport() {
        val builder = AlertDialog.Builder(context!!)
        builder.setMessage("Confirm the completion of this exercise?")
        builder.setPositiveButton("Done") { _, _ ->
            resetTvInfo()
        }
        builder.setNegativeButton("Continue Running") { _, _ ->
            // do something
        }
        builder.create().show()
    }

    
    private fun startRunning(){
        tvChronometer.base = SystemClock.elapsedRealtime()
        tvChronometer.start()

        rlRunningBottomLayoutStarted.visibility = View.VISIBLE
        rlRunningBottomLayout.visibility = View.GONE
        if (runningBottomStartedheight == 0) {
            runningBottomStartedheight = rlRunningBottomLayoutStarted.bottom - rlRunningBottomLayoutStarted.top
        }
        (mapView.layoutParams as RelativeLayout.LayoutParams).bottomMargin = runningBottomStartedheight
    }

    /**
     * Reset textviews to default value
     */
    private fun resetTvInfo() {
        tvRunningPeisu.text = getString(R.string.peisu)
        tvRunningAltitude.text = getString(R.string.altitude)
        tvRunningSpeed.text = getString(R.string.speed)
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

    /**
     * draw lines on the map
     */
    private fun updateLocationUI() {
        try {
            val polyline = mMap.addPolyline(PolylineOptions().add(LatLng(-34.747, 145.592),
                    LatLng(-34.364, 147.891), LatLng(-33.501, 150.217),
                    LatLng(-32.306, 149.248), LatLng(-32.491, 147.309)
            ))

            // Store a data object with the polyline, used here to indicate an arbitrary type.
            polyline.tag = "A"
            // stylePolyline(polyline)

            mMap.setOnMyLocationButtonClickListener(this)
            mMap.setOnMyLocationClickListener(this)
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }


    /**
     * Customize polyline style 
     */
    private val colorBlack = 0xff000000
    private val polylineStrokeWidth = 12f
    private fun stylePolyline(polyline: Polyline) {
        var type = ""
        if (polyline.tag != null) {
            type = polyline.tag.toString()
        }

        when (type) {
        // If no type is given, allow the API to use the default.
            "A" -> {
                // Use a custom bitmap as the cap at the start of the line.
                polyline.startCap = CustomCap(
                        BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 10f)
            }
        // Use a round cap at the start of the line.
        //polyline.setStartCap(RoundCap())
        }
        polyline.endCap = RoundCap()
        polyline.width = polylineStrokeWidth
        polyline.color = colorBlack.toInt()
        polyline.jointType = JointType.ROUND
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

    override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}