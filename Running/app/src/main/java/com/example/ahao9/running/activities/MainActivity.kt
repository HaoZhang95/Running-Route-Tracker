package com.example.ahao9.running.activities


import android.Manifest
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.dongdongwu.mypermission.MyPermission
import com.dongdongwu.mypermission.PermissionFailure
import com.dongdongwu.mypermission.PermissionSuccess
import com.example.ahao9.running.R
import com.example.ahao9.running.fragments.BMIFragment
import com.example.ahao9.running.fragments.HistoryFragment
import com.example.ahao9.running.fragments.HomeFragment
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
    private lateinit var fragmentArray:Array<Fragment>
    private lateinit var fragmentTagsArray:Array<String>
    private lateinit var fragmentTransaction: FragmentTransaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        setUpFragments()
        nav_view.setCheckedItem(R.id.nav_running)

        MyPermission.with(this)
                .setRequestCode(REQUEST_PERMISSION_CODE)
                .setRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .requestPermission();
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
        fragmentArray = arrayOf(homeFragment, bmiFragment, historyFragment)
        fragmentTagsArray = arrayOf("Running", "BMI","History")

        fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)

        if (supportFragmentManager.findFragmentByTag(fragmentTagsArray[selectedPosition]) == null) {
            fragmentTransaction.add(R.id.container, fragmentArray[selectedPosition], fragmentTagsArray[selectedPosition])
        }
        fragmentTransaction.show(fragmentArray[selectedPosition])
        fragmentTransaction.commit()
    }

    /**
     * Inflate the menu; this adds items to the action bar if it is present.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    /**
     * Handle action bar item clicks here. The action bar will
     * automatically handle clicks on the Home/Up button
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
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
