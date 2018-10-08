package com.example.ahao9.running.activities

import android.app.Activity
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.MotionEvent
import com.example.ahao9.running.R
import com.example.ahao9.running.R.id.iv_unlock
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
        MainActivity.isLocked = true
    }

    private fun isPressOnUnlockView(x: Float, y: Float): Boolean {
        val unlockX = (iv_unlock.x + iv_unlock.width).toDouble()
        val unlockY = (iv_unlock.y + iv_unlock.height).toDouble()
        if (x > iv_unlock.x && x < unlockX && y > iv_unlock.y && y < unlockY) {
            return true
        }
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
                    MainActivity.isLocked = false
                    return true
                }
            }
            else -> { }
        }
        return false
    }

    /**
     * comment the following line, to stop user pressing back button
     */
    override fun onBackPressed() {
        // super.onBackPressed()
    }
}
