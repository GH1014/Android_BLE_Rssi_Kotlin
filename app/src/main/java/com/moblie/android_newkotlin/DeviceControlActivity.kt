package com.moblie.android_newkotlin

import android.app.Activity
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.system.Os.accept
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.*


class DeviceControlActivity(private val context: Context?, private var bluetoothGatt: BluetoothGatt?) {
    private val TAG = "gattClienCallback"
    private var tf = true
    private var device : BluetoothDevice? = null
    val textView = (context as Activity).findViewById<View>(R.id.TV1) as TextView
    val imageView = (context as Activity).findViewById<View>(R.id.white_circle) as ImageView

    val bbtn = (context as Activity).findViewById<View>(R.id.scanOffBtn) as ImageButton

    var mRssi = arrayOfNulls<Int>(10)
    var countRssi:Int = 1
    var countRssi2:Int = 1
    var vRssi:Int ?= null

    var mBluetoothAdapter: BluetoothAdapter? = null

    private var mCharacteristic: BluetoothGattCharacteristic? = null

    lateinit var mainActivity : MainActivity



    private val gattCallback : BluetoothGattCallback = object : BluetoothGattCallback(){



        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)

            mCharacteristic = characteristic

            mCharacteristic?.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            gatt?.writeCharacteristic(mCharacteristic);
        }


        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            Log.i(TAG, rssi.toString())
            var temp:Int = 0

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

            mainActivity = context as MainActivity

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Connected to GATT server.")
                    Log.i(TAG, "Attempting to start service discovery: " +
                            bluetoothGatt?.discoverServices())

                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Disconnected from GATT server.")
                    disconnectGattServer()

                    mainActivity.locationTrue()
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

                    bbtn.setOnClickListener(View.OnClickListener {
                        mCharacteristic = gatt!!.getService(convertFromInteger(LIGHT_SERVICE))?.getCharacteristic(convertFromInteger(LIGHT_CHARACTERISTIC))
                        mCharacteristic!!.setValue("1")
                    })
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
                if (vRssi is Int){
                    if (vRssi!! < 50){
                        imageView.setImageResource(R.drawable.circle_small)
                    }
                    else if (vRssi!! < 80){
                        imageView.setImageResource(R.drawable.circle_medium)
                    }
                    else if (vRssi!! < 100){
                        imageView.setImageResource(R.drawable.circle_large)
                    }
                }

            }
        }
        mHandler.obtainMessage().sendToTarget()
    }

    fun convertFromInteger(i: Int): UUID? {
        val msb = 0x0000000000001000L
        val lsb = -0x7fffff7fa064cb05L
        val value = (i and ((-0x1).toLong()).toInt()).toLong()
        return UUID(msb or (value shl 32), lsb)
    }

    companion object {
        private const val TAG = "Main Activity"
        private const val LIGHT_SERVICE = 0xffb0
        private const val LIGHT_CHARACTERISTIC = 0xffb7
        private const val PASSWORD_CHARACTERISTIC = 0xffba
        private const val REQUEST_ENABLE_BLUETOOTH = 1
    }
}

