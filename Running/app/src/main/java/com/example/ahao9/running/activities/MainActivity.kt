package com.example.ahao9.running.activities

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.util.Log
import android.view.MenuItem
import com.example.ahao9.running.R
import com.example.ahao9.running.fragments.BLEFragment
import com.example.ahao9.running.fragments.BMIFragment
import com.example.ahao9.running.fragments.HistoryFragment
import com.example.ahao9.running.fragments.HomeFragment
import com.example.ahao9.running.utils.SharedPref
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
        SensorEventListener {

    private var selectedPosition = 0
    private lateinit var homeFragment: HomeFragment
    private lateinit var bmiFragment: BMIFragment
    private lateinit var historyFragment: HistoryFragment
    private lateinit var bleFragment: BLEFragment
    private lateinit var fragmentArray:Array<Fragment>
    private lateinit var fragmentTagsArray:Array<String>
    private lateinit var fragmentTransaction: FragmentTransaction

    private lateinit var themeSwitcher: SwitchCompat
    private lateinit var lockSwitcher: SwitchCompat
    private lateinit var mySharedPref: SharedPref
    private lateinit var sensorManager: SensorManager
    private var proximitySensor: Sensor? = null
    companion object {
        var isLocked = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mySharedPref = SharedPref(this)
        if (mySharedPref.loadNightModeState()!!) {
            setTheme(R.style.DarkTheme)
        } else {
            setTheme(R.style.AppTheme)
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        setUpFragments()
        nav_view.setNavigationItemSelectedListener(this)
        nav_view.setCheckedItem(R.id.nav_running)

        /**
         * setup day/might mode, so that user can change theme
         */
        val menu = nav_view.menu
        val themeItem = menu.findItem(R.id.nav_theme)
        val actionView = MenuItemCompat.getActionView(themeItem)

        themeSwitcher = actionView.findViewById(R.id.theme_switch) as SwitchCompat
        themeSwitcher.isChecked = mySharedPref.loadNightModeState()!!
        themeSwitcher.setOnCheckedChangeListener { view, isChecked ->
            mySharedPref.setNightModeState(isChecked)
            restartApp()
        }

        /**
         * setup auto lock screen
         */
        val lockItem = nav_view.menu.findItem(R.id.nav_lock)
        val lockActionView = MenuItemCompat.getActionView(lockItem)

        lockSwitcher = lockActionView.findViewById(R.id.lock_switch) as SwitchCompat
        lockSwitcher.isChecked = mySharedPref.loadAutoLockState()!!
        lockSwitcher.setOnCheckedChangeListener { view, isChecked ->
            mySharedPref.setAutoLockState(isChecked)
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.values[0] == 0.0f) {
            Log.d("hero","isLocked: $isLocked isAutoLock: ${mySharedPref.loadAutoLockState()}")
            if (!isLocked && mySharedPref.loadAutoLockState()!!) {
                startActivity<LockScreenActivity>()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }


    private fun restartApp() {
        val i = Intent(applicationContext, MainActivity::class.java)
        startActivity(i)
        finish()
    }

    /**
     * show default fragment
     */
    private fun setUpFragments() {
        homeFragment = HomeFragment()
        bmiFragment = BMIFragment()
        historyFragment = HistoryFragment()
        bleFragment = BLEFragment()

        fragmentArray = arrayOf(homeFragment, bmiFragment, historyFragment, bleFragment)
        fragmentTagsArray = arrayOf("Running", "BMI","History", "BLE")

        fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)

        if (supportFragmentManager.findFragmentByTag(fragmentTagsArray[selectedPosition]) == null) {
            fragmentTransaction.add(R.id.container, fragmentArray[selectedPosition], fragmentTagsArray[selectedPosition])
        }
        fragmentTransaction.show(fragmentArray[selectedPosition])
        fragmentTransaction.commit()
    }

    /**
     * Handle navigation view item clicks to switch different fragments.
     * and saving each fragment's state before switching
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_running -> {
                selectedPosition = 0
            }
            R.id.nav_bmi -> {
                selectedPosition = 1
            }
            R.id.nav_history -> {
                selectedPosition = 2
            }
            R.id.nav_manage -> {
                selectedPosition = 3
            }
            R.id.nav_theme -> {
                return false
            }
            R.id.nav_lock -> {
                return false
            }
        }

        fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)

        if (supportFragmentManager.findFragmentByTag(fragmentTagsArray[selectedPosition]) == null) {
            fragmentTransaction.add(R.id.container, fragmentArray[selectedPosition], fragmentTagsArray[selectedPosition])
        }
        for (i in 0 until fragmentArray.size) {
            if (i == selectedPosition) {
                fragmentTransaction.show(fragmentArray[i])
            } else {
                if (supportFragmentManager.findFragmentByTag(fragmentTagsArray[i]) != null) {
                    fragmentTransaction.hide(fragmentArray[i])
                }
            }
        }
        fragmentTransaction.commit()
        title = fragmentTagsArray[selectedPosition]
        this.drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
}
