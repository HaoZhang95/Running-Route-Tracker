package com.example.ahao9.running.activities

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import com.example.ahao9.running.R
import com.example.ahao9.running.database.entity.RunningRecordEntity
import com.example.ahao9.running.fragments.HomeFragment
import com.example.ahao9.running.utils.SharedPref
import com.example.ahao9.running.utils.Tools
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_route_record.*
import org.jetbrains.anko.toast
import java.io.FileNotFoundException
import java.io.IOException
import java.io.OutputStream


class RouteRecordActivity : AppCompatActivity(), OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener  {

    private val TAG = "hero"
    private lateinit var mMap: GoogleMap
    private var points = ArrayList<LatLng>()
    private var lineOptions = PolylineOptions()
    private lateinit var mySharedPref: SharedPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mySharedPref = SharedPref(this)
        if (mySharedPref.loadNightModeState()!!) {
            setTheme(R.style.DarkTheme)
        } else {
            setTheme(R.style.AppTheme)
        }
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_route_record)
        setUpToolbar()

        val path = intent.extras["path"] as RunningRecordEntity
        for (temp in path.coordinates) {
            points.add(LatLng(temp.latitude, temp.longitude))
        }
        lineOptions.addAll(points);
        lineOptions.width(10f);
        lineOptions.color(resources.getColor(R.color.colorPrimary))

        tvRecordLength.text = "${Tools.getSimpleDecimal(path.mileage / 1000)} KM"
        tvRecordTime.text = Tools.getSimpleTime(path.timeLast)
        tvRecordSpeed.text = Tools.getSimpleDecimal(path.avgSpeed)
        tvRecordAltitude.text = Tools.getSimpleDecimal(path.altitude)

        recordMapView.onCreate(savedInstanceState)
        recordMapView.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        mMap.addPolyline(lineOptions)

        if (points.size > 1) {
            val departure = points[0]
            val dest = points[points.size - 1]

            val bitmapStart = BitmapDescriptorFactory.fromResource(R.drawable.start_pin)
            val bitmapEnd = BitmapDescriptorFactory.fromResource(R.drawable.end_pin)
            val markerOptionStart = MarkerOptions().position(departure).icon(bitmapStart).title("Start Here")
            val markerOptionsEnd = MarkerOptions().position(dest).icon(bitmapEnd).title("End Here")

            mMap.addMarker(markerOptionStart)
            mMap.addMarker(markerOptionsEnd)
            mMap.moveCamera(CameraUpdateFactory.zoomTo(HomeFragment.DEFAULT_ZOOM_LEVEL))
            mMap.animateCamera(CameraUpdateFactory.newLatLng(dest),1,null)
        }
    }

    /**
     * save a snapshot of route to device
     */
    private fun captureScreen() {
        val callback = SnapshotReadyCallback { snapshot ->
            var fout: OutputStream? = null
            var filePath = System.currentTimeMillis().toString() + ".jpeg"
            try {
                fout = openFileOutput(filePath,
                        Context.MODE_WORLD_READABLE)

                snapshot.compress(Bitmap.CompressFormat.JPEG, 90, fout)
                fout.flush()
                fout.close()

                toast("Saved: $filePath")
                openShareImageDialog(filePath)
            } catch (e: FileNotFoundException) {
                // TODO Auto-generated catch block
                Log.d(TAG, "FileNotFoundException")
                filePath = ""
            } catch (e: IOException) {
                Log.d(TAG, "IOException")
                filePath = ""
            } finally {
                fout?.close()
            }
        }

        mMap.snapshot(callback)
    }

    /**
     * Show an dialog to let user share the snapshot
     */
    private fun openShareImageDialog(filePath: String) {
        val file = this.getFileStreamPath(filePath)

        if (filePath != "") {
            val values = ContentValues(2)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            values.put(MediaStore.Images.Media.DATA, file.absolutePath)
            val contentUriFile = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            val intent = Intent(android.content.Intent.ACTION_SEND)
            intent.type = "image/jpeg"
            intent.putExtra(android.content.Intent.EXTRA_STREAM, contentUriFile)
            startActivity(Intent.createChooser(intent, "Share Image"))
        } else {
            toast("openShareImageDialog failed")
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(routeToolbar)
        routeToolbar.setNavigationOnClickListener(View.OnClickListener { finish() })
    }

    /**
     * Inflate the menu; this adds items to the action bar if it is present.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.share_menu, menu)
        return true
    }

    /**
     * Handle action bar item clicks here. The action bar will
     * automatically handle clicks on the Home/Up button
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> {
                captureScreen()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
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
