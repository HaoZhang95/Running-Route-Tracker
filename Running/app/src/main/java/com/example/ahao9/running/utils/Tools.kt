package com.example.ahao9.running.utils

import android.app.Application
import android.location.Location
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 14:45 2018/9/30
 * @ Description：Build for Metropolia project
 */
class Tools {
    companion object {
        private lateinit var app: Application
        private const val EARTH_RADIUS = 6378.137

        fun setUpTools(app: Application) {
            this.app = app
        }

        fun transferDipToPx(dip: Int): Int {
            val scale = app.resources.displayMetrics.density
            return (scale * dip).toInt()
        }

        private fun rad(d:Double):Double{
            return d * Math.PI / 180.0
        }

        fun getDistance(lat1:Double, lng1:Double,
                        lat2:Double, lng2:Double):Double {

            val results = FloatArray(1)
            Location.distanceBetween(lat1,lng1,lat2,lng2, results)

            return results[0].toDouble();
        }

        fun getSimpleDate(time: Long): String {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            return format.format(Date(time))
        }

        fun getSimpleDecimal(num: Double): String {
            val decimalFormat = DecimalFormat("0.00")
            return decimalFormat.format(num)
        }

        /**
         * transfer million seconds to 24:59 this kind of format
         */
        fun getSimpleTime(num: Long): String {
            var time = ""
            var minute = num / 60000
            var seconds = num % 60000
            var second = Math.round (seconds / 1000.0)
            if (minute < 10) { time += "0" }
            time +="$minute:"
            if (second < 10) {time += "0"}
            time += second
            return time
        }

    }
}