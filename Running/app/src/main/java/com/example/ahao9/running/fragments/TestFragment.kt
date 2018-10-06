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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.test_layout,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }





}