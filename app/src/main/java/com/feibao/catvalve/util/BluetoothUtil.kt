package com.feibao.catvalve.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

const val REQUEST_ENABLE_BT = 11
const val DEVICE_SELECTED = 12
val deviceFilter: BluetoothDeviceFilter by lazy {
    BluetoothDeviceFilter.Builder()
//        .setNamePattern(Pattern.compile(LocalData.DEVICE_NAME))
        .build()
}
val pairReq by lazy {
    AssociationRequest.Builder().addDeviceFilter(deviceFilter).setSingleDevice(false).build()
}

class BluetoothUtil(private val context: Context) {
    private val manager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val adapter: BluetoothAdapter? = manager.adapter

    //是否有蓝牙
    val isAvailable get() = adapter != null

    fun showPairDevice() {
        val mgn =
            context.getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
        mgn.associate(pairReq, object : CompanionDeviceManager.Callback() {
            override fun onFailure(error: CharSequence?) {
                "failed search bt".print()
            }

            override fun onDeviceFound(chooserLauncher: IntentSender) {
                (context as Activity).startIntentSenderForResult(
                    chooserLauncher,
                    DEVICE_SELECTED,
                    null,
                    0,
                    0,
                    0
                )
            }

        }, null)
    }

    @SuppressLint("MissingPermission")
    fun connectDevice(address: String) {
        val btDevice = adapter!!.getRemoteDevice(address)
//        if(!isBluetoothPermitted) {
//            return
//        }
        val socket = btDevice.createRfcommSocketToServiceRecord(LocalData.sppUUID)

        CoroutineScope(Dispatchers.IO)
            .launch {
                socket.connect()

                val ips = socket.inputStream
                val ops = socket.outputStream

                ops.write(byteArrayOf('1'.code.toByte()))


            }


    }

    @SuppressLint("MissingPermission")
    fun turnOnBT() {
        if (isBluetoothPermitted) {
            if (adapter?.isEnabled != true) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                (context as Activity).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        } else {
            requestForBt()
        }

    }

    private val isBluetoothPermitted
        get(): Boolean {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

        }

    private fun requestForBt() {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
            REQUEST_ENABLE_BT
        )
    }


}
private fun String.print() {
    Log.d("BT", this)
}

