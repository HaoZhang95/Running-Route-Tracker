package com.example.ahao9.running.utils

import android.app.Application

/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 14:55 2018/9/30
 * @ Description：Build for Metropolia project
 */
class SystemApp: Application() {
    companion object {
        private lateinit var myApp: Application
    }

    override fun onCreate() {
        super.onCreate()
        myApp = this
        Tools.setUpTools(myApp)
    }
}