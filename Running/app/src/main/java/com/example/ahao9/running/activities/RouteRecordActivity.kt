package com.example.ahao9.running.activities

import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import com.example.ahao9.running.R
import com.example.ahao9.running.fragments.HomeFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_route_record.*
import android.graphics.Bitmap
import android.content.Context
import android.util.Log
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback
import java.io.FileNotFoundException
import java.io.IOException
import java.io.OutputStream
import android.content.Intent
import android.provider.MediaStore
import android.content.ContentValues
import android.view.Menu
import android.view.MenuItem
import com.example.ahao9.running.R.id.*
import org.jetbrains.anko.toast


class RouteRecordActivity : AppCompatActivity(), OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener  {

    private lateinit var mMap: GoogleMap
    private val TAG = "hero"

    private var points = ArrayList<LatLng>()
    private var lineOptions = PolylineOptions()

    private fun setUpFakeRouteRecord() {
        points.add(LatLng(60.226208, 24.771758))
        points.add(LatLng(60.226209, 24.771588))
        points.add(LatLng(60.226207, 24.771218))
        points.add(LatLng(60.226212, 24.770845))
        points.add(LatLng(60.226233, 24.770802))


        points.add(LatLng(60.226331, 24.770987))
        points.add(LatLng(60.226433, 24.771187))
        points.add(LatLng(60.226556, 24.771387))
        points.add(LatLng(60.226688, 24.771591))
        points.add(LatLng(60.226842, 24.771773))

        points.add(LatLng(60.226965, 24.771784))
        points.add(LatLng(60.227085, 24.771666))
        points.add(LatLng(60.227106, 24.771537))
        points.add(LatLng(60.227029, 24.771177))
        points.add(LatLng(60.226842, 24.770534))

        // Adding all the points in the route to LineOptions
        lineOptions.addAll(points);
        lineOptions.width(10f);
        lineOptions.color(resources.getColor(R.color.colorPrimary))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_route_record)

        recordMapView.onCreate(savedInstanceState)
        recordMapView.getMapAsync(this)

        setUpToolbar()
        setUpData()

        setUpFakeRouteRecord()
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        mMap.addPolyline(lineOptions);
        lineOptions.width(10f);
        lineOptions.color(resources.getColor(R.color.colorPrimary))

        mMap.addPolyline(lineOptions)

        if (points.size > 1) {
            val departure = points[0]
            val dest = points[points.size - 1]

            val bitmapStart = BitmapDescriptorFactory.fromResource(R.drawable.start_pin)
            val bitmapEnd = BitmapDescriptorFactory.fromResource(R.drawable.end_pin)
            val markerOptionStart = MarkerOptions().position(departure).icon(bitmapStart).title("2018-10-1")
            val markerOptionsEnd = MarkerOptions().position(dest).icon(bitmapEnd).title("2018-10-2")

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
