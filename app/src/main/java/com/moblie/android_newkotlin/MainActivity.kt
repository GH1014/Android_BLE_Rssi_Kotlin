package com.moblie.android_newkotlin

import android.Manifest
import android.annotation.TargetApi
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_ALL_PERMISSION = 2
    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    private lateinit var tv1 : TextView

    var scanTf: Boolean = true

    private var bleGatt: BluetoothGatt? = null
    private var mContext: Context? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var scanning: Boolean = false
    private var devicesArr = ArrayList<BluetoothDevice>()
    private val SCAN_PERIOD = 1
    private val handler = Handler()
    private val mLeScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)


    object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("scanCallback", "BLE Scan Failed : " + errorCode)
        }


        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.let {
                // results is not null
                for (result in it) {

                    if (!devicesArr.contains(result.device) && result.device.address.toString()
                            .equals("4C:EB:D6:6E:AD:46")
                    ) {
                        devicesArr?.add(result.device)
                        break
                    }

                    /*
                    if (!devicesArr.contains(result.device) && result.device.address.toString()
                            .equals("0C:2F:B0:8D:EA:B3")
                    ) {
                        devicesArr?.add(result.device)
                        break
                    }
                     */
                }
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                // result is not null

                if (!devicesArr.contains(result.device) && it.device.address.equals("4C:EB:D6:6E:AD:46")) {
                    tv1?.setText(it.device.address)

                    bleGatt = DeviceControlActivity(mContext, bleGatt).connectGatt(it.device)
                    scanTf = false
                }
                /*
                if (!devicesArr.contains(result.device) && it.device.address.equals("0C:2F:B0:8D:EA:B3")) {
                    tv1?.setText(it.device.address)

                    bleGatt = DeviceControlActivity(mContext, bleGatt).connectGatt(it.device)
                    scanTf = false
                }
                 */
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun scanDevice(state: Boolean) = if (state) {
        handler.postDelayed({
            scanning = false
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
        }, SCAN_PERIOD)
        scanning = true
        devicesArr.clear()
        bluetoothAdapter?.bluetoothLeScanner?.startScan(mLeScanCallback)
    } else {
        scanning = false
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
    }

    private fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    // Permission check
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_ALL_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
                } else {
                    requestPermissions(permissions, REQUEST_ALL_PERMISSION)
                    Toast.makeText(this, "Permissions must be granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scanOnBtn: ImageButton = findViewById(R.id.scanOnBtn)
        val scanOffBtn: ImageButton = findViewById(R.id.scanOffBtn)

        mContext = this
        tv1 = findViewById(R.id.TV1)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        scanOnBtn.setOnClickListener { v: View? -> // Scan Button Onclick
            bluetoothOn()
            scanTf = true

            if (!hasPermissions(this, PERMISSIONS)) {
                requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
            }

            val temp = CoroutineScope(Dispatchers.Default).launch {
                val temp = launch {
                    while (scanTf){
                        scanDevice(true)
                        delay(500)
                    }
                    scanDevice(false)
                    v?.visibility = View.VISIBLE
                }
            }
        }

        scanOffBtn.setOnClickListener { v: View? -> // Scan Button Onclick
            bluetoothOff()
            scanTf = false

            v?.visibility = View.VISIBLE
        }
    }

    fun bluetoothOn() {

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.d("bluetoothAdapter", "Device doesn't support Bluetooth")
        } else {
            if (bluetoothAdapter?.isEnabled == false) { // 블루투스 꺼져 있으면 블루투스 활성화
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }
    }

    fun bluetoothOff() {
        if (bluetoothAdapter?.isEnabled == true) { // 블루투스 꺼져 있으면 블루투스 활성화
            bluetoothAdapter?.disable()
        }
    }
}

private fun Handler.postDelayed(function: () -> Unit?, scanPeriod: Int) {

}

