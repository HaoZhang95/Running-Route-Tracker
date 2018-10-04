package com.example.ahao9.running.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ahao9.running.R
import com.example.ahao9.running.services.GPSService
import kotlinx.android.synthetic.main.test_layout.*
import org.jetbrains.anko.support.v4.toast


/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 16:42 2018/9/30
 * @ Description：Build for Metropolia project
 */
class TestFragment: Fragment() {

    private var broadcastReceiver: BroadcastReceiver? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.test_layout,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(!runtimePermissions()) {
            enableButtons();
        } else {
            toast("Please enable GPS")
        }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun enableButtons() {
        btn_start.setOnClickListener {
            val i = Intent(context!!.applicationContext, GPSService::class.java)
            context!!.startService(i)
        }

        btn_stop.setOnClickListener {
            val i = Intent(context!!.applicationContext, GPSService::class.java)
            context!!.stopService(i)
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
        if(requestCode == 100){
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                enableButtons();
            }else {
                runtimePermissions();
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (broadcastReceiver == null) {
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {

                    textView.text = ("\n" + intent.extras!!.get("coordinates")!!)

                }
            }
        }
        context!!.registerReceiver(broadcastReceiver, IntentFilter("location_update"))
    }

    override fun onDestroy() {
        super.onDestroy()
        if(broadcastReceiver != null){
            context!!.unregisterReceiver(broadcastReceiver);
        }
    }

}