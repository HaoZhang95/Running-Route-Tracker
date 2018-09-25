package com.example.ahao9.running.activities

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.AdapterView
import android.widget.RelativeLayout
import com.example.ahao9.running.R
import com.example.ahao9.running.adapters.ListOptionAdapter
import com.example.ahao9.running.utils.CircularRingPercentageView
import kotlinx.android.synthetic.main.activity_weekly_running.*
import java.util.ArrayList

class WeeklyRunningActivity : Activity() {

    private lateinit var data: MutableList<OptionBean>
    private val titles = arrayOf("Goal Km","History", "Weight & Height","Accomplishment")
    private val contents = arrayOf("Set up a goal for yourself!",
            "Get recent fitness data, display in a chart",
            "Calculate your (BMI)",
            "Check out your performance and badge")
    private val iconRes = intArrayOf(R.drawable.op_goal, R.drawable.op_history,
            R.drawable.op_weight_height, R.drawable.op_got)
    private val titleColor = intArrayOf(-0x87550d,-0x4c7e05,
            -0xa54fcc,-0x76db5)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_running)

        // setup Progress Circle
        getWidthAndHeight()
        val progressCircle = findViewById<CircularRingPercentageView>(R.id.progress)
        progressCircle.setMaxColorNumber(200)
        val layoutParams = progressCircle.getLayoutParams() as RelativeLayout.LayoutParams
        val progress = (screenWidth * 0.55).toInt()
        layoutParams.width = progress
        layoutParams.height = progress
        progressCircle.layoutParams = layoutParams
        progressCircle.circleWidth = progress
        progressCircle.setProgress(80F)

        totalKmTextView.text = "1.2"

        // setup list
        getListData()
        listview_option.adapter = ListOptionAdapter(this, data)
        listview_option.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            when (position) {
                /*0 -> {
                }
                1 -> startActivity
                2 -> {
                }
                3 -> startActivity
                */
            }
        }

    }

    private var screenWidth: Int = 0
    private var screenHeight:Int = 0
    private fun getWidthAndHeight() {
        val manager = this.windowManager
        val outMetrics = DisplayMetrics()
        manager.defaultDisplay.getMetrics(outMetrics)
        screenWidth = outMetrics.widthPixels
        screenHeight = outMetrics.heightPixels
    }

    private fun getListData() {
        data = ArrayList<OptionBean>()
        for (i in titles.indices) {
            val bean = OptionBean(titles[i], iconRes[i], contents[i], titleColor[i])
            data.add(bean)
        }
    }
}

data class OptionBean(var title: String, var icon_url: Int,
                      var content: String, var title_color: Int)
