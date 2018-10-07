package com.example.ahao9.running.activities


import android.Manifest
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.view.Menu
import android.view.MenuItem
import com.dongdongwu.mypermission.MyPermission
import com.dongdongwu.mypermission.PermissionFailure
import com.dongdongwu.mypermission.PermissionSuccess
import com.example.ahao9.running.R
import com.example.ahao9.running.fragments.*
import com.example.ahao9.running.utils.SharedPref
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val REQUEST_PERMISSION_CODE = 1
    }
    private var selectedPosition = 0
    private lateinit var homeFragment: HomeFragment
    private lateinit var bmiFragment: BMIFragment
    private lateinit var historyFragment: HistoryFragment
    private lateinit var bleFragment: BLEFragment

    private lateinit var fragmentArray:Array<Fragment>
    private lateinit var fragmentTagsArray:Array<String>
    private lateinit var fragmentTransaction: FragmentTransaction
    private lateinit var themeSwitcher: SwitchCompat
    private lateinit var mySharedPref: SharedPref

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

        val menu = nav_view.menu
        val themeItem = menu.findItem(R.id.nav_theme)
        val actionView = MenuItemCompat.getActionView(themeItem)

        themeSwitcher = actionView.findViewById(R.id.theme_switch) as SwitchCompat
        themeSwitcher.isChecked = mySharedPref.loadNightModeState()!!
        themeSwitcher.setOnCheckedChangeListener { view, isChecked ->
            mySharedPref.setNightModeState(isChecked)
            restartApp()
        }

        MyPermission.with(this)
                .setRequestCode(REQUEST_PERMISSION_CODE)
                .setRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .requestPermission();
    }

    private fun restartApp() {
        val i = Intent(applicationContext, MainActivity::class.java)
        startActivity(i)
        finish()
    }

    /**
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
    @PermissionSuccess(requestCode = REQUEST_PERMISSION_CODE)
    private fun callPermissionSuccess() { }

    @PermissionFailure(requestCode = REQUEST_PERMISSION_CODE)
    private fun callPermissionFailure() {
        toast("Permissions are Missing")
        ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_CODE)
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        MyPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
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
}
