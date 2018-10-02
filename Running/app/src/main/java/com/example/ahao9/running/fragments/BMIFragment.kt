package com.example.ahao9.running.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.example.ahao9.running.R
import com.example.ahao9.running.database.entity.BMIViewModel
import com.example.ahao9.running.utils.Tools
import kotlinx.android.synthetic.main.bmi_layout.*
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.util.ChartUtils
import java.text.DecimalFormat
import kotlin.collections.ArrayList
import android.arch.lifecycle.Observer
import com.example.ahao9.running.database.entity.BMIEntity


/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 16:42 2018/9/30
 * @ Description：Build for Metropolia project
 */
class BMIFragment: Fragment() {

    private val bmi_level_color = intArrayOf(-0xae561f, -0x7738fd, -0x4bfe, -0x2be1fe)

    private lateinit var data: LineChartData
    private val numberOfLines = 1
    private val maxNumberOfLines = 4
    private val numberOfPoints = 10
    internal var randomNumbersTab = Array(maxNumberOfLines) { FloatArray(numberOfPoints) }
    private val hasAxes = true
    private val hasAxesNames = true
    private val hasLines = true
    private val hasPoints = true
    private val shape = ValueShape.CIRCLE
    private val isFilled = false
    private val hasLabels = false
    private val isCubic = false
    private val hasLabelForSelected = false
    private val pointsHaveDifferentColor: Boolean = false
    private lateinit var lastWeight: String
    private lateinit var bmiViewModel:BMIViewModel
    private var size0fDotValueArr:Int = 0
    private var TAG = "hero"


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {



        return inflater.inflate(R.layout.bmi_layout,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bmiViewModel = ViewModelProviders.of(this).get(BMIViewModel::class.java)
        bmiViewModel.findAll().observe(this, Observer {
            Log.d(TAG,"BMI size: ${it!!.size}")
            // userAdapter!!.setData((it as MutableList<User>))
        })

        initViews()

        iv_bmi_level.setOnClickListener {
            resetData()
        }
    }

    private fun initViews() {
        iv_editBMI.setOnClickListener {
            resetData();
        }
        generateValues()

        linechart_weight.isViewportCalculationEnabled = false
        resetViewport()
        lastWeight = tv_weight.text.toString()

        resetUI()
    }

    private fun setBMI(sex: String, height: Double, weight: Double) {
        val sg = height / 100.0
        val bmi = weight / (sg * sg)
        val df = DecimalFormat("0.00")
        val str_bmi = df.format(bmi)
        tv_bmi_value.text = str_bmi

        if (bmi < 18.5) {
            iv_bmi_level.setImageResource(R.drawable.bmi_1)
            tv_bmi_level.text = "偏瘦"
            setTextColor(bmi_level_color[0])
        } else if ((bmi >= 18.5) and (bmi < 24)) {
            iv_bmi_level.setImageResource(R.drawable.bmi_2)
            tv_bmi_level.text = "正常"
            setTextColor(bmi_level_color[1])
        } else if (bmi >= 24 && bmi < 30) {
            iv_bmi_level.setImageResource(R.drawable.bmi_3)
            tv_bmi_level.text = "偏胖"
            setTextColor(bmi_level_color[2])
        } else {
            iv_bmi_level.setImageResource(R.drawable.bmi_4)
            tv_bmi_level.text = "重度偏胖"
            setTextColor(bmi_level_color[3])
        }
    }

    private fun setTextColor(color: Int) {
        tv_bmi_value.setTextColor(color)
        tv_bmi_level.setTextColor(color)
    }

    private fun setParameters(delt_weight: Double, myWeight: Double, myHeight: Double, time: Long) {
        iv_weight_how_change.setImageResource(if (delt_weight > 0) R.drawable.icon_arrow_up else R.drawable.icon_arrow_down)
        tv_height.text = myHeight.toString()
        tv_weight.text = myWeight.toString()
        tv_update_time.setText(Tools.getSimpleDate(time))
        tv_delt_weight.text = Math.abs(delt_weight).toString()
    }

    private fun resetViewport() {
        // Reset viewport height range to (0,100)
        val v = Viewport(linechart_weight.getMaximumViewport())
        v.bottom = 0f
        v.top = 200f
        v.left = 0f
        v.right = (numberOfPoints - 1).toFloat()
        linechart_weight.setMaximumViewport(v)
        linechart_weight.setCurrentViewport(v)
    }

    private fun resetUI() {
        val dotValueArr = arrayOfNulls<Int>(10)
        bmiViewModel.findLatest10().observe(this, Observer {
            Log.d(TAG,"BMI findLatest10 size: ${it!!.size}")
            if (it.isNotEmpty()) {

                size0fDotValueArr = it.size
                for (i in it.indices) {
                    val str = it[i].weight
                    dotValueArr[i] = str.toDouble().toInt()
                    //str.toInt()
                }

                var lastWeight: Double = 0.0
                val iterator = it.iterator()

                if (iterator.hasNext()) {
                    val bmi = iterator.next()
                    val currentWeight = bmi.weight.toDouble()

                    if (iterator.hasNext()) {
                        lastWeight = iterator.next().weight.toDouble()
                    }

                    var delt_weight = currentWeight - lastWeight
                    setParameters(delt_weight, bmi.weight.toDouble(),
                            bmi.height.toDouble(), bmi.time.toLong())
                    setBMI("男", bmi.height.toDouble(), bmi.weight.toDouble() / 2)
                }
                // dataList.reverse()
                dotValueArr.reverse()
                drawDotOnChart(dotValueArr)
            }
        })
    }

    private fun generateValues() {
        for (i in 0 until maxNumberOfLines) {
            for (j in 0 until numberOfPoints) {
                randomNumbersTab[i][j] = Math.random().toFloat() * 100f
            }
        }
    }

    private fun drawDotOnChart(dataList: Array<Int?>) {
        val lines = ArrayList<Line>()

        for (i in 0 until numberOfLines) {
            val values = ArrayList<PointValue>()
            for (j in dataList.indices) {
                if (dataList[j] != null) {
                    values.add(PointValue(j.toFloat(), dataList[j]!!.toFloat()))
                    Log.e(TAG, dataList[j].toString() + "dataList  ---")
                }
            }

            val line = Line(values)
            line.color = ChartUtils.COLORS[i]
            line.shape = shape
            line.isCubic = isCubic
            line.isFilled = isFilled
            line.setHasLabels(hasLabels)
            line.setHasLabelsOnlyForSelected(hasLabelForSelected)
            line.setHasLines(hasLines)
            line.setHasPoints(hasPoints)
            if (pointsHaveDifferentColor) {
                line.pointColor = ChartUtils.COLORS[(i + 1) % ChartUtils.COLORS.size]
            }
            lines.add(line)
        }

        data = LineChartData(lines)

        if (hasAxes) {
            val axisX = Axis()
            val axisY = Axis().setHasLines(true)
            //            axisY.setMaxLabelChars(200);
            if (hasAxesNames) {
                axisX.name = "最近" + (size0fDotValueArr) + "天"
                axisY.name = "体重(单位：斤)"
            }
            data.setAxisXBottom(axisX)
            data.setAxisYLeft(axisY)
        } else {
            data.setAxisXBottom(null)
            data.setAxisYLeft(null)
        }

        data.baseValue = java.lang.Float.NEGATIVE_INFINITY
        linechart_weight.lineChartData = data

    }

    private fun resetData() {
        val buider = AlertDialog.Builder(context!!)
        val dialog_view = LayoutInflater.from(context).inflate(R.layout.dialog_resetdata, null)
        buider.setView(dialog_view)
        buider.setPositiveButton("确认") { dialog, which ->
            val et_height = dialog_view.findViewById(R.id.et_height) as EditText
            val et_weight = dialog_view.findViewById(R.id.et_weight) as EditText
            if (!TextUtils.isEmpty(et_height.text.toString()) && !TextUtils.isEmpty(et_weight.text.toString())) {
                val myHeight = Integer.parseInt(et_height.text.toString()).toDouble()
                val myWeight = Integer.parseInt(et_weight.text.toString()).toDouble()
                val delt_weight = myWeight - java.lang.Double.parseDouble(lastWeight)
                // 先存后取

                val sg = myHeight / 100.0
                val bmi = myWeight / (sg * sg)
                val df = DecimalFormat("0.00")
                val str_bmi = df.format(bmi)

                val bmiEntity = BMIEntity(
                        id = 0,
                        weight = myWeight.toString(),
                        height = myHeight.toString(),
                        bmi = str_bmi,
                        time = System.currentTimeMillis().toString()
                )
                bmiViewModel.insert(bmiEntity)

                // resetUI()
            }
        }
        buider.setNegativeButton("取消") { dialog, which -> }
        buider.show()

    }


}