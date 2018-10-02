package com.example.ahao9.running.activities

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.example.ahao9.running.model.AccDataResponse
import com.example.ahao9.running.model.MyScanResult
import com.google.gson.Gson
import com.movesense.mds.*
import com.polidea.rxandroidble.RxBleClient
import com.polidea.rxandroidble.scan.ScanSettings
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import rx.Subscription
import java.util.ArrayList
import kotlinx.android.synthetic.main.activity_connect_ble.*
import com.example.ahao9.running.R

class ConnectBLEActivity : AppCompatActivity(),
        AdapterView.OnItemLongClickListener,AdapterView.OnItemClickListener {
    private val TAG = "hero"
    private val MY_PERMISSIONS_REQUEST_LOCATION = 1

    // MDS is the official library of movesense sensor
    private lateinit var mMds: Mds
    private val URI_CONNECTEDDEVICES = "suunto://MDS/ConnectedDevices"
    private val URI_EVENTLISTENER = "suunto://MDS/EventListener"
    private val SCHEME_PREFIX = "suunto://"

    // BleClient singleton
    private lateinit var mBleClient:RxBleClient
    // UI
    private var mScanResArrayList = ArrayList<MyScanResult>()
    private lateinit var mScanResultListView: ListView
    private lateinit var mScanResArrayAdapter: ArrayAdapter<MyScanResult>
    // Sensor subscription
    private val URI_MEAS_ACC_13 = "/Meas/Acc/13"
    private var mdsSubscription: MdsSubscription? = null
    private var subscribedDeviceSerial: String? = null
    private var mScanSubscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_ble)

        // Init Scan UI
        mScanResultListView = findViewById<ListView>(R.id.deviceList)
        mScanResArrayAdapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_1, mScanResArrayList)
        mScanResultListView.adapter = mScanResArrayAdapter
        mScanResultListView.onItemClickListener = this
        mScanResultListView.onItemLongClickListener = this

        // Make sure we have all the permissions this app needs
        requestNeededPermissions()

        // Initialize Movesense MDS library
        mMds = Mds.builder().build(this)

        scanBtn.setOnClickListener { onScanClicked() }
        stopBtn .setOnClickListener { onScanStopClicked() }
    }

    /**
     * Init RxAndroidBle (Ble helper library) if not yet initialized
     */
    private fun getBleClient(): RxBleClient {
        mBleClient = RxBleClient.create(this)

        return mBleClient
    }

    /**
     * Request bluetooth permission
     */
    private fun requestNeededPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION)
        }
    }

    /**
     * Start scan && Process scan result here. filter movesense devices.
     */
    private fun onScanClicked() {

        scanBtn.visibility = View.GONE
        stopBtn.visibility = View.VISIBLE
        // Start with empty list
        mScanResArrayList.clear()
        mScanResArrayAdapter.notifyDataSetChanged()

        Log.d(TAG, "getBleClient: ${getBleClient()}")
        mScanSubscription = this.getBleClient().scanBleDevices(ScanSettings.Builder().build())
                .subscribe({ scanResult ->
                            Log.d(TAG, "scanResult: $scanResult")
                            if (scanResult.bleDevice != null &&
                                    scanResult.bleDevice.name != null &&
                                    scanResult.bleDevice.name!!.startsWith("Movesense")) {
                                // replace if exists already, add otherwise
                                val msr = MyScanResult(scanResult)
                                if (mScanResArrayList.contains(msr))
                                    mScanResArrayList[mScanResArrayList.indexOf(msr)] = msr
                                else
                                    mScanResArrayList.add(0, msr)

                                mScanResArrayAdapter.notifyDataSetChanged()
                            }
                        }, { throwable ->
                            toast("scan error: $throwable")
                            // Re-enable scan buttons, just like with ScanStop
                            onScanStopClicked()
                        }
                )
    }

    /**
     * Stop unsubscribe
     */
    private fun onScanStopClicked() {
        if (mScanSubscription != null) {
            mScanSubscription!!.unsubscribe()
            mScanSubscription = null
        }

        scanBtn.visibility = View.VISIBLE
        stopBtn.visibility = View.GONE
    }

    /**
     * Click item in listView
     */
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position < 0 || position >= mScanResArrayList.size) { return }

        val device = mScanResArrayList[position]
        if (!device.isConnected) {
            // Stop scanning
            onScanStopClicked()
            // And connect to the device
            connectBLEDevice(device)
        }
    }

    /**
     * Long click to disconnect sensor
     */
    override fun onItemLongClick(parent: AdapterView<*>, view: View, position: Int, id: Long): Boolean {
        if (position < 0 || position >= mScanResArrayList.size)
            return false

        val device = mScanResArrayList[position]

        // unsubscribe if there
        Log.d(TAG, "onItemLongClick, " + device.connectedSerial + " vs " + subscribedDeviceSerial)
        if (device.connectedSerial == subscribedDeviceSerial) { unsubscribe() }

        Log.i(TAG, "Disconnecting from BLE device: " + device.macAddress)
        mMds.disconnect(device.macAddress)

        return true
    }

    /**
     * Subscribe to Sensor
     */
    private fun subscribeToSensor(connectedSerial: String) {
        // Clean up existing subscription (if there is one)
        if (mdsSubscription != null) {
            unsubscribe()
        }

        // Build JSON doc that describes what resource and device to subscribe
        // Here we subscribe to 13 hertz accelerometer data
        val sb = StringBuilder()
        val strContract = sb.append("{\"Uri\": \"").append(connectedSerial).append(URI_MEAS_ACC_13).append("\"}").toString()
        Log.d(TAG, strContract)

        subscribedDeviceSerial = connectedSerial

        mdsSubscription = Mds.builder().build(this).subscribe(URI_EVENTLISTENER,
                strContract, object : MdsNotificationListener {
            override fun onNotification(data: String) {
                val accResponse = Gson().fromJson(data, AccDataResponse::class.java)
                if (accResponse != null && accResponse.body.array.isNotEmpty()) {

                    val accStr = String.format(
                            "%.02f, %.02f, %.02f",
                            accResponse.body.array[0].x,
                            accResponse.body.array[0].y,
                            accResponse.body.array[0].z
                    )

                    Log.d(TAG, "onNotification(): $accStr")
                }
            }

            override fun onError(error: MdsException) {
                unsubscribe()
                toast("Subscription Error")
            }
        })
    }

    /**
     * Click to establish connection
     */
    private fun connectBLEDevice(device: MyScanResult) {
        val bleDevice = getBleClient().getBleDevice(device.macAddress)

        toast("Connecting to ${bleDevice.macAddress}")
        mMds.connect(bleDevice.macAddress, object : MdsConnectionListener {
            override fun onConnect(s: String) {
                Log.d(TAG, "onConnect:$s")
            }

            override fun onConnectionComplete(macAddress: String, serial: String) {
                for (sr in mScanResArrayList) {
                    if (sr.macAddress == (macAddress)) {
                        sr.markConnected(serial)

                        // start subscribing
                        subscribeToSensor(device.connectedSerial)

                        // Jump to main page
                        startActivity<MainActivity>()
                        break
                    }
                }
                mScanResArrayAdapter.notifyDataSetChanged()
            }

            override fun onError(e: MdsException) {
                Log.e(TAG, "onError:$e")

                showConnectionError(e)
            }

            override fun onDisconnect(bleAddress: String) {

                Log.d(TAG, "onDisconnect: $bleAddress")
                for (sr in mScanResArrayList) {
                    if (bleAddress == sr.macAddress) {
                        // unsubscribe if was subscribed
                        if (sr.connectedSerial != null && sr.connectedSerial == subscribedDeviceSerial)
                            unsubscribe()

                        sr.markDisconnected()
                    }
                }
                mScanResArrayAdapter.notifyDataSetChanged()
            }
        })
    }

    /**
     * Show Connection Error With AlertDialog
     */
    private fun showConnectionError(e: MdsException) {
        val builder = AlertDialog.Builder(this)
                .setTitle("Connection Error:")
                .setMessage(e.message)

        builder.create().show()
    }

    /**
     * Unsubscribe to sensor
     */
    private fun unsubscribe() {
        if (mdsSubscription != null) {
            mdsSubscription!!.unsubscribe()
            mdsSubscription = null
        }

        subscribedDeviceSerial = null
    }
}
