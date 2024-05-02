package com.feibao.catvalve.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

const val REQUEST_ENABLE_BT = 11

class BluetoothUtil(private val context: Context) {
    val manager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
    val adapter: BluetoothAdapter? = manager.adapter

    //是否有蓝牙
    val isAvailable get() = adapter != null

    @SuppressLint("MissingPermission")
    fun turnOnBT() {
        if (adapter?.isEnabled != true) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

            if (isBluetoothPermitted) {
                (context as Activity).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)

            } else {
                requestForBt()
            }
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