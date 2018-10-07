package com.example.ahao9.running.database.entity

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 13:51 2018/10/7
 * @ Description：Build for Metropolia project
 */
@Parcelize
data class RunningRecordEntity(
        val itemType: Int,
        val mileage: Double,
        val startTime: Long,
        val stopTime: Long,
        val timeLast: Long,
        val avgSpeed: Double,
        val altitude: Double,
        val coordinates:MutableList<MyLatLng>
        ): Parcelable{

    constructor() :this(0,0.0,0,0,0,0.0,0.0, mutableListOf<MyLatLng>()){
    }

    private lateinit var mKey: String

    @Exclude
    fun getKey(): String {
        return mKey
    }

    @Exclude
    fun setKey(key: String) {
        mKey = key
    }
}

@Parcelize
data class MyLatLng (val latitude: Double, val longitude:Double):Parcelable {
    constructor(): this (0.0,0.0)
}