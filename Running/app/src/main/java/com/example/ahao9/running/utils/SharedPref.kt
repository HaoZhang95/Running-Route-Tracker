package com.example.ahao9.running.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 23:12 2018/10/6
 * @ Description：Build for Metropolia project
 */
class SharedPref(val context: Context) {

    private var mySharedPref: SharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)

    /**
     * this method will save the nightMode State : True or False
     */
    fun setNightModeState(state: Boolean?) {
        val editor = mySharedPref.edit()
        editor.putBoolean("NightMode", state!!)
        editor.apply()
    }

    /**
     * this method will load the Night Mode State
     */
    fun loadNightModeState(): Boolean? {
        return mySharedPref.getBoolean("NightMode", true)
    }

    /**
     * this method will save the auto lock State : True or False
     */
    fun setAutoLockState(state: Boolean?) {
        val editor = mySharedPref.edit()
        editor.putBoolean("AutoLock", state!!)
        editor.apply()
    }

    /**
     * this method will load the Night Mode State
     */
    fun loadAutoLockState(): Boolean? {
        return mySharedPref.getBoolean("AutoLock", false)
    }
}