package com.example.ahao9.running.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.example.ahao9.running.R
import com.example.ahao9.running.activities.MainActivity
import com.example.ahao9.running.model.AccDataResponse
import com.example.ahao9.running.model.MyScanResult
import com.google.gson.Gson
import com.movesense.mds.*
import com.polidea.rxandroidble.RxBleClient
import com.polidea.rxandroidble.scan.ScanSettings
import kotlinx.android.synthetic.main.ble_layout.*
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import rx.Subscription


/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 16:42 2018/9/30
 * @ Description：Build for Metropolia project
 */
class BLEFragment: Fragment(), AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    private val TAG = "hero"

    // MDS is the official library of movesense sensor
    private lateinit var mMds: Mds
    private val URI_EVENTLISTENER = "suunto://MDS/EventListener"

    // BleClient singleton
    private lateinit var mBleClient: RxBleClient
    // UI
    private var mScanResArrayList = java.util.ArrayList<MyScanResult>()
    private lateinit var mScanResultListView: ListView
    private lateinit var mScanResArrayAdapter: ArrayAdapter<MyScanResult>
    // Sensor acceleration subscription
    private val URI_MEAS_ACC_13 = "/Meas/Acc/13"
    private var mdsSubscription: MdsSubscription? = null
    private var subscribedDeviceSerial: String? = null
    private var mScanSubscription: Subscription? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ble_layout,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init Scan UI
        mScanResultListView = view.findViewById<ListView>(R.id.deviceList)
        mScanResArrayAdapter = ArrayAdapter(context,
                android.R.layout.simple_list_item_1, mScanResArrayList)
        mScanResultListView.adapter = mScanResArrayAdapter
        mScanResultListView.onItemClickListener = this
        mScanResultListView.onItemLongClickListener = this


        // Initialize Movesense MDS library
        mMds = Mds.builder().build(context)

        scanBtn.setOnClickListener { onScanClicked() }
        stopBtn .setOnClickListener { onScanStopClicked() }
    }

    /**
     * Init RxAndroidBle (Ble helper library) if not yet initialized
     */
    private fun getBleClient(): RxBleClient {
        mBleClient = RxBleClient.create(context!!)

        return mBleClient
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

        mdsSubscription = Mds.builder().build(context).subscribe(URI_EVENTLISTENER,
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

                    xyzAccr.text = accStr

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
        val builder = AlertDialog.Builder(context!!)
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
