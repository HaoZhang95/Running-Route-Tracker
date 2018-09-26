package com.example.ahao9.running.activities

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.example.ahao9.running.R
import kotlinx.android.synthetic.main.activity_time_out.*
import org.jetbrains.anko.startActivity

class TimeOutActivity : Activity(), Animation.AnimationListener {

    private lateinit var animation: Animation
    private var index = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_out)

        // setup timeout animation
        animation = AnimationUtils.loadAnimation(this, R.anim.scale_img)
        animation.setAnimationListener(this)
        iv_number3.startAnimation(animation)
    }

    override fun onAnimationEnd(animation: Animation) {
        index++
        when (index) {
            1 -> {
                iv_number3.clearAnimation()
                iv_number3.visibility = View.GONE
                iv_number2.visibility = View.VISIBLE
                iv_number2.startAnimation(animation)
            }
            2 -> {
                iv_number2.clearAnimation()
                iv_number2.visibility = View.GONE
                iv_number1.visibility = View.VISIBLE
                iv_number1.startAnimation(animation)
            }
            3 -> {
                iv_number1.clearAnimation()
                iv_number1.visibility = View.GONE
                startActivity<RunningActivity>()
                finish()
            }
            else -> { }
        }
    }

    override fun onAnimationRepeat(animation: Animation) {

    }

    override fun onAnimationStart(animation: Animation) {

    }
}
