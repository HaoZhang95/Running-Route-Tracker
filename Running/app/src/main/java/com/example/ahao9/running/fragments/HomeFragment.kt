package com.example.ahao9.running.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.app.ActivityCompat
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
import com.example.ahao9.running.utils.Tools
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.home_layout.*
import kotlinx.android.synthetic.main.trace_includelayout.*
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 16:42 2018/9/30
 * @ Description：Build for Metropolia project
 */
class HomeFragment: Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private lateinit var mMap: GoogleMap
    private var TAG = "hero"
    private var mRepeatCountNum = 2
    private var mBottomStartedheight = 0
    private var mSportTime:Long = 0
    private lateinit var mBottomTv_Time: Chronometer
    private var isPause = false
    private val mAniTime: Long = 300

    private var mSportType = 1
    private lateinit var myView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        myView = inflater.inflate(R.layout.home_layout,container,false)


        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ###################################################################################

        setUpViewClickListeners()

        // setUpGoogleMap()

        // Gets the MapView from the XML layout and creates it
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        val mLayoutParams = mapView.layoutParams as RelativeLayout.LayoutParams
        mLayoutParams.bottomMargin = Tools.transferDipToPx(200)
        mapView.layoutParams = mLayoutParams


        //add this line to display menu1 when the activity is loaded
        // displaySelectedScreen(R.id.nav_camera);
        // ###################################################################################
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        try {
            mMap.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            Log.e(TAG, e.message);
        }
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true


        getCurrentLocation()
        // updateLocationUI()
    }

    /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(){
        try {
            val sydney = LatLng(-34.0, 151.0);
            mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        } catch(e: SecurityException)  {
            Log.e(TAG, e.message)
        }
    }

    /**
     * setUp a bunch of View ClickListeners
     */
    private fun setUpViewClickListeners(){

        StartSportTV.setOnClickListener {
            CountLayout.visibility = View.VISIBLE
            val scaleAnimation = ScaleAnimation(3f, 0.5f, 3f, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            scaleAnimation.duration = 1000
            scaleAnimation.startTime = 100
            scaleAnimation.repeatCount = 2
            CountTimer.startAnimation(scaleAnimation)

            scaleAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }
                override fun onAnimationEnd(animation: Animation) {
                    CountLayout.visibility = View.GONE
                    mRepeatCountNum = 2
                    CountTimer.text = "3"
                    startSport()
                }

                override fun onAnimationRepeat(animation: Animation) {
                    CountTimer.setText( "$mRepeatCountNum")
                    mRepeatCountNum--
                }
            })
        }


        bottomBt_End.setOnClickListener {
            onStopSport()
        }

        mBottomTv_Time = view!!.findViewById<Chronometer>(R.id.bottomTv_Time)

        mBottomTv_Time.onChronometerTickListener = Chronometer.OnChronometerTickListener {
            chronometer -> mSportTime = SystemClock.elapsedRealtime() - chronometer.base
        }

        /*
         *
         */
        bottomBt_Pause.setOnClickListener {
            bottomBt_End.setClickable(false)
            bottomBt_Lock.setClickable(false)
            bottomBt_Pause.setClickable(false)
            isPause = !isPause
            bottomBt_Pause.text = if (isPause) "继续" else "暂停"
            if (isPause) {
                mBottomTv_Time.stop()
                beginAnimateInX(0, bottomBt_Pause.getLeft() - bottomBt_Lock.getLeft())
            } else {
                mBottomTv_Time.base = SystemClock.elapsedRealtime() - mSportTime
                mBottomTv_Time.start()
                beginAnimateInX(bottomBt_Lock.getLeft() - bottomBt_Lock.getLeft(), 0)
            }
        }

        /**
         *
         */
        SportTypeRun.setOnClickListener(View.OnClickListener {
            SportTypeRun.setImageResource(R.drawable.trance_run)
            SportTypeBike.setImageResource(R.drawable.trance_bike2)
            mSportType = 1
            updateTvBasedOnSportType(mSportType)
        })
        SportTypeBike.setOnClickListener(View.OnClickListener {
            SportTypeRun.setImageResource(R.drawable.trance_run2)
            SportTypeBike.setImageResource(R.drawable.trance_bike)
            mSportType = 2
            updateTvBasedOnSportType(mSportType)
        })

        /**
         * lock
         */
        bottomBt_Lock.setOnClickListener { startActivity<LockScreenActivity>() }
    }

    private fun updateTvBasedOnSportType(mSportType: Int) {

        // 根据类型的切换,重新回去总里程和总时间
        toast("mSportType: $mSportType")

        /*TraceManager.GetRunRecordIndexPage(itemtype, object : onNetCallbackListener() {
            fun onSuccess(requestStr: String, result: String) {
                val mPage = JSON.parseObject(result, Entity_TotalInfo::class.java)
                if (mPage.getCode() === 200 && mPage.getData() != null) {
                    if (itemtype == 1) {
                        mPage_Run = mPage.getData()
                    } else {
                        mPage_Bike = mPage.getData()
                    }
                    RefreshInfo()
                } else {
                    qktool.ToastShout(mPage.getMsg())
                }
            }

            fun onError(throwable: Throwable) {
                super.onError(throwable)
                qktool.ToastShout("总里程总时间等信息获取失败，如有需要请重试")
            }
        })*/

    }


    /**
     * 开始运动后，暂停按钮触发的锁屏按钮的收起恢复动画
     *
     * @param x1
     * @param x2
     */
    private fun beginAnimateInX(x1: Int, x2: Int) {
        val mAnimator = ValueAnimator.ofInt(x1, x2)
        mAnimator.addUpdateListener { animation ->
            bottomBt_Lock.translationX = (animation.animatedValue as Int).toFloat()
            bottomBt_Lock.requestLayout()
        }
        mAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                bottomBt_End.setClickable(true)
                bottomBt_Lock.setClickable(true)
                bottomBt_Pause.setClickable(true)
            }
        })
        mAnimator.setDuration(mAniTime).start()
    }

    private fun onStopSport() {
        val builder = AlertDialog.Builder(context!!)
        builder.setMessage("Confirm the completion of this exercise?")
        builder.setPositiveButton("Done") { _, _ ->

            // 进行存储和上传数据, 并且进行tv重置
            resetTvInfo()

            // val today_record = getTodayData()
            // val intent = Intent(baseContext, RecordShowActivity::class.java)
            // intent.putExtra("recorditem", today_record)
            // startActivity(intent)
        }
        builder.setNegativeButton("Continue Running") { _, _ ->
            //            isPause = false
//            endAnimator()
        }
        builder.create().show()
    }

    private fun startSport(){

        // check gps permission here

        // 初始化速度,计时器,timer等数据

        mBottomTv_Time.base = SystemClock.elapsedRealtime()
        mBottomTv_Time.start()

        bottomLayoutStarted.visibility = View.VISIBLE
        bottomLayout.visibility = View.GONE
        if (mBottomStartedheight == 0) {
            mBottomStartedheight = bottomLayoutStarted.bottom - bottomLayoutStarted.top
        }
        (mapView.layoutParams as RelativeLayout.LayoutParams).bottomMargin = mBottomStartedheight
    }

    // ###################################################################################


    /**
     * 界面记录信息清零
     */
    private fun resetTvInfo() {
        bottomTv_Peisu.setText("00:00")
        bottomTv_Altitude.setText("0.00")//海拔
        bottomTv_Speed.setText("0.00")//即时速度
        bottomTV_Distance.setText("0.00KM")
        bottomTv_AverageSpeed.setText("0.00") // 平均速度
        mBottomTv_Time.stop()

        bottomLayout.setVisibility(View.VISIBLE)
        bottomLayoutStarted.setVisibility(View.GONE)

        /* mMapView.getMap().clear()
         mLocationClient.stop()
 */
        bottomBt_Pause.text = "暂停"
        bottomBt_Lock.translationX = 0f
        bottomBt_Lock.requestLayout()

        val mLayoutParams = mapView.layoutParams as RelativeLayout.LayoutParams
        mLayoutParams.bottomMargin = Tools.transferDipToPx(200)
        mapView.layoutParams = mLayoutParams
    }


    /**
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var mLocationPermissionGranted = false
    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.activity!!.applicationContext,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(activity!!,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] === PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                }
            }
        }
    }

    private fun updateLocationUI() {
        try {
            if (mLocationPermissionGranted) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
                getLocationPermission()
            }

            val polyline = mMap.addPolyline(PolylineOptions().add(LatLng(-34.747, 145.592),
                    LatLng(-34.364, 147.891), LatLng(-33.501, 150.217),
                    LatLng(-32.306, 149.248), LatLng(-32.491, 147.309)
            ))

            // Store a data object with the polyline, used here to indicate an arbitrary type.
            polyline.tag = "A";
            // stylePolyline(polyline)

            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }


    private val COLOR_BLACK_ARGB = 0xff000000;
    private val POLYLINE_STROKE_WIDTH_PX = 12f;
    private fun stylePolyline(polyline: Polyline) {
        var type = ""
        // Get the data object stored with the polyline.
        if (polyline.tag != null) {
            type = polyline.tag.toString();
        }

        when (type) {
        // If no type is given, allow the API to use the default.
            "A" -> {
                // Use a custom bitmap as the cap at the start of the line.
                polyline.startCap = CustomCap(
                        BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 10f);
            }
        // Use a round cap at the start of the line.
        //polyline.setStartCap(RoundCap());
        }
        polyline.endCap = RoundCap();
        polyline.width = POLYLINE_STROKE_WIDTH_PX;
        polyline.color = COLOR_BLACK_ARGB.toInt();
        polyline.jointType = JointType.ROUND;
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

   /* override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }*/

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