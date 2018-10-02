package com.example.ahao9.running.model

import com.google.android.gms.maps.model.LatLng

/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 10:05 2018/10/2
 * @ Description：Build for Metropolia project
 */
data class RouteRecord (
        var startPoint: LatLng,
        var endPoint: LatLng,
        var pathLine: MutableList<LatLng>,
        var distance: String,
        var duration: String,
        var averageSpeed: String,
        var date: String,
        var id:Long = 0) {

    fun addPoint(point: LatLng) {
        pathLine.add(point)
    }
}