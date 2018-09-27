package com.example.ahao9.running.activities

import android.app.Activity
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import com.example.ahao9.running.R
import kotlinx.android.synthetic.main.activity_lock_screen.*

class LockScreenActivity : Activity() {

    private var x: Int = 0
    private lateinit var unlockDrawable: AnimationDrawable
    private var flag = false
    private var TAG = "hero"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)

        unlockDrawable = iv_unlock.drawable as AnimationDrawable
        unlockDrawable.start()
    }

    private fun isPressOnUnlockView(x: Float, y: Float): Boolean {
        val unlockX = (iv_unlock.x + iv_unlock.width).toDouble()
        val unlockY = (iv_unlock.y + iv_unlock.height).toDouble()
        if (x > iv_unlock.x && x < unlockX && y > iv_unlock.y && y < unlockY) {
            return true
        }
        Log.e(TAG, "unlockX: $unlockX , unlockY: $unlockY")
        Log.e(TAG, "X:" + iv_unlock.x + ",Y:" + iv_unlock.y)
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                x = event.x.toInt()
                flag = isPressOnUnlockView(event.x, event.y)
            }
            MotionEvent.ACTION_UP -> if (event.x - x > 200) {
                if (flag) {
                    unlockDrawable.stop()
                    finish()
                    return true
                }
            }
            else -> { }
        }
        return false
    }

    override fun onBackPressed() {
        // comment the following line, to stop user pressing back button
        // super.onBackPressed()
    }
}
