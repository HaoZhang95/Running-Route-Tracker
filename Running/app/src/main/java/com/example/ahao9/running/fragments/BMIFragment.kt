package com.example.ahao9.running.fragments

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ahao9.running.R
import com.example.ahao9.running.database.entity.BMIViewModel
import com.example.ahao9.running.utils.Tools
import kotlinx.android.synthetic.main.bmi_layout.*
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.util.ChartUtils
import java.text.DecimalFormat
import kotlin.collections.ArrayList
import android.arch.lifecycle.Observer
import android.graphics.Color
import android.widget.EditText
import com.example.ahao9.running.database.entity.BMIEntity


/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 16:42 2018/9/30
 * @ Description：Build for Metropolia project
 */
class BMIFragment: Fragment() {

    private val numberOfLines = 1
    private val maxNumberOfLines = 4
    private val numberOfPoints = 10
    private val hasLines = true
    private val hasPoints = true
    private val isFilled = false
    private val hasLabels = false
    private val isCubic = false
    private val hasLabelForSelected = false
    private lateinit var lastWeight: String
    private lateinit var bmiViewModel:BMIViewModel
    private lateinit var data: LineChartData
    private val shape = ValueShape.CIRCLE
    private var size0fDotValueArr:Int = 0
    private var randomNumbersTab = Array(maxNumberOfLines) { FloatArray(numberOfPoints) }
    private val bmiLevelColor = arrayOf("#7CBEE7", "#89C600", "#FFB500", "#DE482F")

    private val TAG = "hero"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bmi_layout,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        ivBmiEdit.setOnClickListener { refreshData() }
        ivBmiLevel.setOnClickListener { refreshData() }

        bmiViewModel = ViewModelProviders.of(this).get(BMIViewModel::class.java)
        lastWeight = tvBmiWeight.text.toString()

        fulfillValues()
        bmiLineChart.isViewportCalculationEnabled = false
        refreshViewport()
        refreshUI()
    }

    private fun setUpBMI(height: Double, weight: Double) {
        val bmiValue = weight / (height * height)
        val decimalFormat = DecimalFormat("0.00")
        val bmiStr = decimalFormat.format(bmiValue)
        tvBmiValue.text = bmiStr

        if (bmiValue < 18.5) {
            ivBmiLevel.setImageResource(R.drawable.bmi_1)
            tvBmiLevel.text = getString(R.string.bmiSlim)
            setTextColor(bmiLevelColor[0])
        } else if ((bmiValue >= 18.5) and (bmiValue < 24)) {
            ivBmiLevel.setImageResource(R.drawable.bmi_2)
            tvBmiLevel.text = getString(R.string.bmiHealthy)
            setTextColor(bmiLevelColor[1])
        } else if (bmiValue >= 24 && bmiValue < 30) {
            ivBmiLevel.setImageResource(R.drawable.bmi_3)
            tvBmiLevel.text = getString(R.string.bmiOverweight)
            setTextColor(bmiLevelColor[2])
        } else {
            ivBmiLevel.setImageResource(R.drawable.bmi_4)
            tvBmiLevel.text = getString(R.string.bmiObses)
            setTextColor(bmiLevelColor[3])
        }
    }

    private fun setTextColor(colorStr: String) {
        tvBmiValue.setTextColor(Color.parseColor(colorStr))
        tvBmiLevel.setTextColor(Color.parseColor(colorStr))
    }

    private fun setUpParameters(deltWeight: Double, myWeight: Double, myHeight: Double, time: Long) {
        ivBmiWeightChange.setImageResource(if (deltWeight > 0) R.drawable.icon_arrow_up else R.drawable.icon_arrow_down)
        tvBmiHeight.text = myHeight.toString()
        tvBmiWeight.text = myWeight.toString()
        tvBmiTime.text = Tools.getSimpleDate(time)
        tvBmiDeltWeight.text = Math.abs(deltWeight).toString()
    }

    private fun refreshViewport() {
        // Reset viewport height range to (0,100)
        val v = Viewport(bmiLineChart.maximumViewport)
        v.bottom = 0f
        v.top = 200f
        v.left = 0f
        v.right = (numberOfPoints - 1).toFloat()
        bmiLineChart.maximumViewport = v
        bmiLineChart.currentViewport = v
    }

    private fun refreshUI() {
        val dotValueArr = arrayOfNulls<Int>(10)
        bmiViewModel.findLatest10().observe(this, Observer {

            Log.d(TAG,"BMI findLatest10 size: ${it!!.size}")

            if (it.isNotEmpty()) {
                size0fDotValueArr = it.size
                for (i in (it.size - 1) downTo 0) {
                    val str = it[i].weight
                    dotValueArr[(it.size - 1) - i] = str.toDouble().toInt()
                }

                var lastWeight = 0.0
                val iterator = it.iterator()

                if (iterator.hasNext()) {
                    val bmi = iterator.next()
                    val currentWeight = bmi.weight.toDouble()

                    if (iterator.hasNext()) {
                        lastWeight = iterator.next().weight.toDouble()
                    }

                    val deltWeight = currentWeight - lastWeight
                    this.setUpParameters(deltWeight, bmi.weight.toDouble(),
                            bmi.height.toDouble(), bmi.time.toLong())
                    setUpBMI(bmi.height.toDouble(), bmi.weight.toDouble())
                }
            }
            drawDotOnChart(dotValueArr)
        })
    }

    private fun fulfillValues() {
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

            lines.add(line)
        }

        data = LineChartData(lines)

        val axisX = Axis()
        val axisY = Axis().setHasLines(true)
        axisX.name = "$size0fDotValueArr times recently"
        axisY.name = "Weight (kg)"

        data.axisXBottom = axisX
        data.axisYLeft = axisY

        data.baseValue = java.lang.Float.NEGATIVE_INFINITY
        bmiLineChart.lineChartData = data
    }

    @SuppressLint("InflateParams")
    private fun refreshData() {
        val myDiglogBuilder = AlertDialog.Builder(context!!)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_resetdata, null)
        myDiglogBuilder.setView(dialogView)
        myDiglogBuilder.setPositiveButton("Confirm") { _, _ ->

            val heightEt = dialogView.findViewById(R.id.et_height) as EditText
            val weightEt = dialogView.findViewById(R.id.et_weight) as EditText

            if ( heightEt.text.trim().isNotEmpty() && weightEt.text.trim().isNotEmpty()) {

                var heightInput = heightEt.text.trim().toString().toDouble()
                val weightInput = weightEt.text.trim().toString().toDouble()

                heightInput /= 100.0
                val bmi = weightInput / (heightInput * heightInput)
                val decimalFormat = DecimalFormat("0.00")
                val bmiStr = decimalFormat.format(bmi)

                val bmiEntity = BMIEntity(
                        id = 0,
                        weight = weightInput.toString(),
                        height = heightInput.toString(),
                        bmi = bmiStr,
                        time = System.currentTimeMillis().toString()
                )
                bmiViewModel.insert(bmiEntity)
            }
        }
        myDiglogBuilder.setNegativeButton("Cancel") { _, _ -> }
        myDiglogBuilder.show()
    }
}