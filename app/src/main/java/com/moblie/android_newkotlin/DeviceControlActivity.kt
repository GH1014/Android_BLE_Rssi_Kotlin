package com.moblie.android_newkotlin

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.TextView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class DeviceControlActivity(private val context: Context?, private var bluetoothGatt: BluetoothGatt?) {
    private val TAG = "gattClienCallback"
    private var tf = true
    private var device : BluetoothDevice? = null
    val textView = (context as Activity).findViewById<View>(R.id.TV1) as TextView
    var mRssi = arrayOfNulls<Int>(10)
    var countRssi:Int = 1
    var countRssi2:Int = 1
    var vRssi:Int ?= null

    private val gattCallback : BluetoothGattCallback = object : BluetoothGattCallback(){
        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            Log.i(TAG, rssi.toString())
            var temp:Int = 0
            //val temp:Double = (-69.0 - rssi.toDouble()) / 20
            //mRssi = Math.pow(0.1, temp).toInt()
            mRssi[countRssi-1] = -rssi

            if (countRssi < 10){
                countRssi++
            }
            else{
                countRssi = 1
            }

            if (countRssi2 < 10){
                countRssi2++
            }

            for (i in 1..countRssi2){
                temp += mRssi[i-1]!!
            }

            vRssi = temp / countRssi2

            temp = 0
            broadcastUpdate(rssi.toString())
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Connected to GATT server.")
                    Log.i(TAG, "Attempting to start service discovery: " +
                            bluetoothGatt?.discoverServices())
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Disconnected from GATT server.")
                    disconnectGattServer()
                }
            }

        }
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.i(TAG, "Connected to GATT_SUCCESS.")

                    if (tf){
                        GlobalScope.launch {
                            tf = false
                            while (true){
                                delay(100)
                                gatt?.readRemoteRssi()
                                delay(100)
                            }
                        }
                    }
                }
                else -> {
                    Log.w(TAG, "Device service discovery failed, status: $status")
                }

            }
        }

        private fun disconnectGattServer() {
            Log.d(TAG, "Closing Gatt connection")
            // disconnect and close the gatt
            if (bluetoothGatt != null) {
                bluetoothGatt?.disconnect()
                bluetoothGatt?.close()
                bluetoothGatt = null
            }
        }
    }

    fun connectGatt(device: BluetoothDevice): BluetoothGatt?{
        this.device = device

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothGatt = device.connectGatt(context, false, gattCallback,
                BluetoothDevice.TRANSPORT_LE)
        }
        else {
            bluetoothGatt = device.connectGatt(context, false, gattCallback)
        }
        return bluetoothGatt
    }

    private fun broadcastUpdate(str: String) {
        val mHandler : Handler = object : Handler(Looper.getMainLooper()){
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                textView.setText((vRssi).toString())
            }
        }
        mHandler.obtainMessage().sendToTarget()
    }
}