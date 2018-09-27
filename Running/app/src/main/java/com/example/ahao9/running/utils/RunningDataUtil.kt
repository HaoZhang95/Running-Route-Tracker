package com.example.ahao9.running.utils

/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 15:53 2018/9/26
 * @ Description：Build for Metropolia project
 */
class RunningDataUtil {

    companion object {
        fun format(i: Int): String {
            return if (i in 0..9) {
                "0" + i.toString()
            } else if (i in 10..99) {
                i.toString()
            } else {
                "XX"
            }
        }

        fun getTime(second: Int): String {
            if (second > 60) {
                if (second > 3600) {
                    val h = second / 3600
                    val ms = second % 3600
                    val m = ms / 60
                    val s = ms % 60
                    return format(h) + ":" + format(m) + ":" + format(s)
                } else if (second < 3600) {
                    val m = second / 60
                    val s = second % 60
                    return "00:" + format(m) + ":" + format(s)
                } else {
                    return "01:00:00"
                }

            } else return if (second < 60) {
                "00:00:" + format(second)
            } else {
                "00:01:00"
            }
        }

        /**
         *
         * @param second 时间：秒
         * @param souce 路程：米
         * @return 速度（分钟/公里） XX'YY''形式的
         */
        fun getSd(second: Int, souce: Double): String {
            val sce = souce / 1000.0
            val sd = (second / sce).toInt()
            val m = sd / 60
            val s = sd % 60
            return m.toString() + "'" + s + "''"
        }
    }
}